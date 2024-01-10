package edu.usfca.cs272;

import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
* Class to build inverted index from urls' html content
* 
* @author Athene Marston
*
*/
public class WebCrawler {
	/**
	 * The inverted index of data to search
	 */
	private final ThreadSafeInvertedIndex index;

	/**
	 * The work queue to use in multithreading
	 */
	private final WorkQueue queue;
	
	/**
	 * Total number of URLs to crawl (including the seed URL)
	 * Decremented to 0 with each crawl 
	 */
	private int crawls;
	
	/**
	 * Web pages that have already had their headers are fetched from a web server
	 */
	private final HashSet<URL> visited;
	
	/**
	 * Web crawler constructor
	 * 
	 * @param queue WorkQueue for this class
	 * @param index ThreadSafeInvertedIndex for this class
	 * @param maxCrawl the maximum number of links to crawls
	 */
	public WebCrawler(WorkQueue queue, ThreadSafeInvertedIndex index, int maxCrawl) {
		this.index = index;
		this.queue = queue;
		this.crawls = maxCrawl -1;
		this.visited = new HashSet<URL>(); 
	}
	
	/**
	 * Builds counts map and inverted index from a urls' html
	 * 
	 * @param seed  initial crawl url
	 * @throws URISyntaxException    error normalizing url
	 * @throws MalformedURLException error normalizing url
	 */
	public void build(String seed) throws MalformedURLException, URISyntaxException {
		URL url = LinkFinder.normalize(new URL(seed)); // start location
		
		visited.add(url); //if url not visited, add to visited...
		CrawlTask task = new CrawlTask(url);	//new crawl task
		queue.execute(task);
		
		queue.finish();
	}

	/**
	 * Parses url's html content and adds stems to index
	 * 
	 * @param html  string of url's htm
	 * @param local ThreadSafeInvertedIndex
	 * @param url   name
	 * @return true if html content added to index, else false
	 */
	public static boolean addHtml(String html, InvertedIndex local, URL url) {
		Stemmer stemmer = new SnowballStemmer(ENGLISH);
		int i = 1;
		String[] htmlLines = FileStemmer.parse(html);
		
		boolean added = true;

		for (String line : htmlLines) { // for every word, stem + add to index
			String stem = stemmer.stem(line).toString();
			if (local.addInvertedIndex(stem, url.toString(), i) == false) {
				added = false;
			}
			i++;
		}
		
		return (added && i>1);	//if no lines in html (not added)
	}
	
	/**
	 * Adds page snippet from given url's html to index's HTMLSnippet map
	 * 
	 * @param html to use
	 * @param snippetLength length of snippet to return 
	 * @param url location
	 * @param index ThreadSafeInvertedIndex to add snippet info to 
	 */
	public static void addPageSnippet(String html, int snippetLength, URL url, ThreadSafeInvertedIndex index) {
		String snippet = html.replaceAll("\\s+", " ");
		
		if(snippet == null || snippet.isBlank()) {	//if null, no content found
			snippet = "no content found";
		}
		if (snippet.length() > snippetLength) {
			snippet = snippet.substring(0, snippetLength - 3) + "...";
        }
		index.addHTMLSnippet(url.toString(), snippet);
    
	}
	
	/**
	 * Class for Runnable 'build html' task objects to execute
	 * 
	 * @author Athene Marston
	 *
	 */
	private class CrawlTask implements Runnable {

		/**
		 * Url html to parse
		 */
		private final URL url;

		/**
		 * Stores local data from parsed file
		 */
		private final InvertedIndex local;

		/**
		 * Constructor for tasks
		 * 
		 * @param url  url html to parse
		 */
		public CrawlTask(URL url) {
			this.url = url;
			this.local = new InvertedIndex();
		}

		@Override
		public void run() {
			String html = HtmlFetcher.fetch(url, 3, index);

			if (html != null) { // if valid html
				html = HtmlCleaner.stripBlockElements(html); // strip block elms and get found urls
				ArrayList<URL> foundUrls = LinkFinder.listUrls(url, html);

				if (foundUrls != null) {
					synchronized (visited) {
						for (URL found : foundUrls) { // for every found url, if numCrawls < maxCrawls
							if (crawls > 0) {
								if (!visited.contains(found)) { // if url not visited, add to visited, create new task
									visited.add(found);
									CrawlTask task = new CrawlTask(found); // new crawl task
									crawls--;
									queue.execute(task);
								}
							} else {
								break;
							}
						}
					}
				}

				html = HtmlCleaner.stripEntities(HtmlCleaner.stripTags(html)); // finish clean
				if (addHtml(html, local, url)) { // process this url
					addPageSnippet(html, 50, url, index); // add snippet of 50 chars
				}
				
			}

			index.addAll(local);
		}

		@Override
		public String toString() {
			return url.toString();
		}
		
	}
	
}