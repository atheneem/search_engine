package edu.usfca.cs272;


import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.StringSubstitutor;
import edu.usfca.cs272.InvertedIndex.FoundFile;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Class for servlets to process and return query result html
 * 
 * @author Athene Marston
 *
 */
public class WebServlet extends HttpServlet {
	
	/** Class version for serialization, in [YEAR][TERM] format (unused). */
	private static final long serialVersionUID = 202302;
	
	/** Base path with HTML templates. */
	private static final Path base = Path.of("src", "main", "resources", "html");
	
	/** Base redirect link to other servlets. **/
	private String link;
	
	/** Inverted index to search from. **/
	private final ThreadSafeInvertedIndex index;
	
	/** Template for starting HTML. **/
	private final String doGetTemplate;
	
	/** Template for returned search HTML. **/
	private final String doPostTemplate;
	
	/** Template for location display HTML. **/
	private final String locationsTemplate;
	
	/** Template for index display HTML. **/
	private final String indexTemplate;
	
	/** index.json file for index as a string. **/
	private String indexJSON;
	
	/** html for returning locations stored in index. **/
	private String locations;
	
	/** html for returning a view of the index. **/
	private String indexDisplay;
	
	
	/**
	 * Initializes this servlet
	 * 
	 * @param index to use
	 * @param port server is hosted at
	 * @throws IOException if unable to read templates
	 */
	public WebServlet(ThreadSafeInvertedIndex index, int port) throws IOException {
		super();
		
		link = "http://localhost:" + port;
		this.index = index;

		//load templates
		doGetTemplate = Files.readString(base.resolve("start.html"), UTF_8);
		doPostTemplate = Files.readString(base.resolve("return.html"), UTF_8);
		locationsTemplate = Files.readString(base.resolve("locations.html"), UTF_8);
		indexTemplate = Files.readString(base.resolve("index.html"), UTF_8);
		
		//saved html for returning index data 
		indexJSON = null;
		locations = null; 
		indexDisplay = null;

	}
	
	
	/**
	 * Writes the html for a row in the table of search results
	 * 
	 * @param out PrintWriter to use
	 * @param url of search result
	 * @param title of search result page
	 * @param count of search result page
	 * @param score of search result page
	 */
	public void resultEntry(PrintWriter out, String url, String title, String count, String score) {
        
		String format = "    <tr>\n"
				+ "        <td><a href= %s>%s</a></td>\n"
				+ "        <td>%s</td>\n"
				+ "        <td>%s</td>\n"
				+ "        <td>%s</td>\n"
				+ "        <td>%s</td>\n"
				+ "        <td>%s</td>\n"
				+ "    </tr>";
		String[] stats = index.getHTMLStatistics(url);	//{title, length, time}
		out.write(String.format(format, url, stats[0], stats[1],  stats[2], index.getHTMLSnippet(url), count, score));
	}
	
	/**
	 * Writes the ending html for the doPost call to the server
	 * 
	 * @param out PrintWriter to use
	 */
	private void writeFooter(PrintWriter out) {
		out.write("<!-- counts modal -->\n"
				+ "<div id= \"counts-modal\" class = \"modal\">\n"
				+ "    <div class=\"modal-background\"></div>\n"
				+ "    <div class=\"modal-content\">\n"
				+ "      <div class=\"box\">\n"
				+ "        <p>Total number of matches found</p>\n"
				+ "      </div>\n"
				+ "    </div>\n"
				+ "    <button class=\"modal-close is-large\" aria-label=\"close\"></button>\n"
				+ "</div>\n"
				+ "\n"
				+ "<!-- scores modal -->\n"
				+ "<div id= \"scores-modal\" class = \"modal\">\n"
				+ "        <div class=\"modal-background\"></div>\n"
				+ "        <div class=\"modal-content\">\n"
				+ "          <div class=\"box\">\n"
				+ "            <p>Percentage of total matches / total words</p>\n"
				+ "          </div>\n"
				+ "        </div>\n"
				+ "        <button class=\"modal-close is-large\" aria-label=\"close\"></button>\n"
				+ "    </div>\n"
				+ "</div>\n"
				+ "\n"
				+ "<script src=\"/files/webServer.js\"></script>\n"
				+ "\n"
				+ "</body>\n"
				+ "</html>");
	}
	
	/**
	 * Writes html for the search results table of a query 
	 * 
	 * @param results Found Files from the search
	 * @param out PrintWriter to use
	 */
	public void resultTable(ArrayList<FoundFile> results, PrintWriter out) {
		if (results.isEmpty()) {
			out.write ("<div style=\"text-align: center;\">\n"
					+ "  <p><strong>No found results</strong></p>\n"
					+ "</div>\n");
		} else {
			out.write("    <div class=\"container pl-4 pb-6\">\n"
					+ "        <table class=\"table is-hoverable is-fullwidth\">\n"
					+ "      <thead>\n"
					+ "        <tr>\n"
			        + "		  <th>Location</th>"
			        + "		  <th>Length</th>"
			        + "		  <th>Crawl Timestamp</th>"
			        + "		  <th>Snippet</th>"
					+ "          <th><button class=\"js-modal-trigger button is-white\" data-target=\"counts-modal\">Count</button></th>\n"
					+ "          <th><button class=\"js-modal-trigger button is-white\" data-target=\"scores-modal\">Score</button></th>\n"
					+ "        </tr>\n"
					+ "      </thead>\n"
					+ "    \n"
					+ "      <tbody>\n");
			
			for (FoundFile found : results) { 
				resultEntry(out, found.getPath(), "test title", found.getCount().toString(), found.getScore().toString());
			}
			
			out.write("      </tbody>\n"
					+ "    </table>\n"
					+ "    </div>");
		}
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
		
		Map<String, String> values = new HashMap<>();
		values.put("method", "POST");
		values.put("title", "Search Engine");
		values.put("downloadHTML", link + "/download");
		values.put("locationsLink", link + "/locations");
		values.put("indexLink", link + "/index");
		
		StringSubstitutor replacer = new StringSubstitutor(values);
		String html = replacer.replace(doGetTemplate);

		PrintWriter out = response.getWriter();
		out.write(html);
		out.flush();

		response.setStatus(HttpServletResponse.SC_OK);
	}
	
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");

		ArrayList<FoundFile> results = new ArrayList<FoundFile>();

		String search = request.getParameter("search");
		search = StringEscapeUtils.escapeHtml4(search); // avoid attacks

		boolean isExact = request.getParameter("exact") != null;

		if (search != null && !search.isBlank()) {
			TreeSet<String> queries = FileStemmer.uniqueStems(search); // search words
			results = index.search(queries, !isExact);
		}

		Map<String, String> values = new HashMap<>();

		values.put("downloadHTML", link + "/download");
		values.put("locationsLink", link + "/locations");
		values.put("indexLink", link + "/index");
		values.put("method", "POST");
		values.put("title", "Search Engine");

		StringSubstitutor replacer = new StringSubstitutor(values);
		String html = replacer.replace(doPostTemplate);
		
		PrintWriter out = response.getWriter();
		out.write(html);	//writes html above the results table
		resultTable(results, out);	//write results table
		writeFooter(out);	//writes ending html (modals and closing tags)
		
		out.flush();

		response.setStatus(HttpServletResponse.SC_OK);

	}
	
	/**
	 * Servlet to return a download-able indes.json file
	 * 
	 * @author Athene Marston
	 *
	 */
	public class DownloadServlet extends HttpServlet {
		/** Class version for serialization, in [YEAR][TERM] format (unused). */
		private static final long serialVersionUID = 202302;
		
		/**
		 * Constructor for DownloadServlet
		 */
		public DownloadServlet() {
			super();
		}

		@Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {
			response.setContentType("application/octet-stream");
		    response.setHeader("Content-Disposition", "attachment;filename=index.json");

		    if(indexJSON == null) {	//create index json content if dne
		    	StringWriter writer = new StringWriter();
			    index.outputIndex(writer);
			    
			    indexJSON = writer.toString();	// Get the file contents
		    }

		    // Write the file contents to the response output stream
		    OutputStream out = response.getOutputStream();
		    out.write(indexJSON.getBytes());
		    out.flush();
		    out.close();
		
		    response.setStatus(HttpServletResponse.SC_OK);
        }
	}
	
	/**
	 * Servlet to display inverted index locations
	 * 
	 * @author Athene Marston
	 *
	 */
	public class LocationsServlet extends HttpServlet {
		/** Class version for serialization, in [YEAR][TERM] format (unused). */
		private static final long serialVersionUID = 202302;
		
		/**
		 * Constructor for LocationsServlet
		 */
		public LocationsServlet() {
			super();
		}
		
		/**
		 * Returns html for a row in table of locations in the inverted index 
		 * 
		 * @param number index of entry
		 * @param url location
		 * @return a string of a row for the index results table
		 */
		public String locationsEntry(int number, String url) {
			String format = "        <tr>\n"
					+ "            <td class=\"is-narrow\">%s</td>\n"
					+ "            <td>%s</td>\n"
					+ "            <td><a href=\"%s\">%s</a></td>\n"
					+ "        </tr>";
		return String.format(format, number, index.getHTMLStatistics(url)[0], url, url);
		}
		
		/**
		 * Returns html for a table of locations in the inverted index 
		 * 
		 * @param locations collection of locations stored in the index
		 * @return html for index locations table
		 */
		public String locationsTable(Collection<String> locations) {
				int i = 1;
				StringBuilder builder = new StringBuilder("<div class=\"container pl-4 pt-6 pb-6\">\n"
						+ "        <h2 class=\"title py-1\">Locations in the Source Index:</h2>\n"
						+ "        <table class=\"table is-hoverable is-fullwidth is-bordered is-striped\">\n"
						+ "      <tbody>");
				
				for (String location : locations) { 
					String htmlReturned = locationsEntry(i++, location);
					builder.append(htmlReturned);
				}
				
				builder.append("      </tbody>\n"
						+ "    </table>\n"
						+ "    </div>");
				
				return builder.toString();
		}

		@Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {
			response.setContentType("text/html");
          
			Map<String, String> values = new HashMap<>();
			values.put("title", "Search Engine");
			values.put("back", link);
			
			if (locations == null) {	//build locations html if dne
				String table = locationsTable(index.getLocations());
				
				values.put("table", table);
				StringSubstitutor replacer = new StringSubstitutor(values);
				locations = replacer.replace(locationsTemplate);
			}
			
			PrintWriter out = response.getWriter();
			out.write(locations);
			out.flush();
			response.setStatus(HttpServletResponse.SC_OK);
        }
	}
	
	/**
	 * Servlet to display inverted index
	 * 
	 * @author Athene Marston
	 *
	 */
	public class IndexServlet extends HttpServlet {
		/** Class version for serialization, in [YEAR][TERM] format (unused). */
		private static final long serialVersionUID = 202302;
		
		/**
		 * Constructor for IndexServlet
		 */
		public IndexServlet() {
			super();
		}
		
		/**
		 * Returns html for the display of index
		 * 
		 * @return html for a display of the index
		 */
		public String indexTable() {
			StringBuilder builder = new StringBuilder("          <div class=\"container pl-4 pt-6 pb-6\">\n"
					+ "						       <h2 class=\"title py-1\">Source Index:</h2>\n"
					+ "						        <table class=\"table is-hoverable is-fullwidth is-bordered\">\n"
					+ "						     <tbody>" + "          <thead>\n" + "            <tr>\n"
					+ "              <th>Word Stem</th>\n" + "              <th>Locations</th>\n"
					+ "              <th>Number of Positions Found</th>\n" + "            </tr>\n"
					+ "          </thead>\n" + "          <tbody>");

			Collection<String> locations;
			Collection<Integer> positions;

			for (String stem : index.getWords()) {
				locations = index.getLocations(stem);
				Iterator<String> iter = locations.iterator(); // iterator of locations

				if (iter.hasNext()) { // write first location
					String location = iter.next();
					positions = index.getPositions(stem, location);
					builder.append(firstIndexEntry(locations.size(), stem, location, positions.size()));
				}

				while (iter.hasNext()) { // write remaining locations
					String location = iter.next();
					positions = index.getPositions(stem, location);
					builder.append(indexEntry(location, positions.size()));

				}
			}

			builder.append("      </tbody>\n" + "    </table>\n" + "    </div>");

			return builder.toString();
		}
		
		/**
		 * Returns html for a location in a word stem's value map
		 * 
		 * @param location a url location for word stem key
		 * @param count    number of occurrences of the stem at the location
		 * @return a string of HTML for a url location in the inverted index
		 */
		public String indexEntry(String location, int count) {
			String format = "            <tr>\n"
					+ "              <td><a href= %s > %s </a></td>\n"
					+ "              <td>%s</td>\n"
					+ "            </tr>";
		return String.format(format, location, location, count);
		}
		
		/**
		 *  Returns html for the first location in a word stem's value map
		 * 
		 * @param numLocations size of the set of occurrence positions for the location
		 * @param stem word stem key for this location 
		 * @param location a url location for word stem key
		 * @param count number of occurrences of the stem at the location
		 * @return a string of HTML for first url location for a stem in the inverted index
		 */
		public String firstIndexEntry(int numLocations, String stem,  String location, int count) {
			String format = "            <tr>\n"
					+ "              <th rowspan=\"%s\">%s</th>\n"
					+ "              <td><a href= %s > %s </a></td>\n"
					+ "              <td>%s</td>\n"
					+ "            </tr>";
			
			return String.format(format, numLocations, stem, location, location, count);
		}

		@Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {
			response.setContentType("text/html");
          
			Map<String, String> values = new HashMap<>();
			values.put("title", "Search Engine");
			values.put("back", link);
			
			if(indexDisplay == null) {
				indexDisplay = indexTable();
			}
			values.put("index", indexDisplay);
			
			StringSubstitutor replacer = new StringSubstitutor(values);
			String html = replacer.replace(indexTemplate);

			
			PrintWriter out = response.getWriter();
			out.write(html);
			out.flush();
			response.setStatus(HttpServletResponse.SC_OK);
        }
	}
}
