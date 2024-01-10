package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import edu.usfca.cs272.InvertedIndex.FoundFile;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * shared interface for search result classes
 * 
 * @author Athene Marston
 *
 */
public interface SearchResultsInterface {

	/**
	 * Reads query file and adds found results to results map
	 * 
	 * @param location  path to query file
	 * @param isPartial true if partial search, false if exact
	 * @throws IOException error in FileFinder
	 */
	public default void processFile(Path location, boolean isPartial) throws IOException {
		try (BufferedReader in = Files.newBufferedReader(location, UTF_8)) {
			String line = null;
			while ((line = in.readLine()) != null) {
				processLine(line, isPartial);
			}
		}
	}

	/**
	 * Adds a line of query and FoundFiles to results map
	 * 
	 * @param line      query line
	 * @param isPartial true if partial search, false if exact
	 */
	public void processLine(String line, boolean isPartial);

	/**
	 * Returns unmodifiable view of queries in the search results map
	 * 
	 * @return unmodifiable view of queries in map
	 */
	public Set<String> getQueries();

	/**
	 * Returns unmodifiable view of query's found files (emptySet if query not in
	 * map)
	 * 
	 * @param query the query who's results to get
	 * @return unmodifiable view of query's file set, emptySet if DNE
	 */
	public List<FoundFile> getFiles(String query);

	/**
	 * Writes search results map to given output file
	 * 
	 * @param location path to output file
	 * @throws IOException error in JsonWriter
	 */
	public void output(Path location) throws IOException;

	/**
	 * returns a string of cleaned query line
	 * 
	 * @param query to use
	 * @return cleaned query line
	 */
	public static String cleanQuery(String query) {
		TreeSet<String> getQueries = FileStemmer.uniqueStems(query);
		return String.join(" ", getQueries);
	}

	/**
	 * returns a string of cleaned query line (using given stemmer)
	 * 
	 * @param query   to use
	 * @param stemmer to use
	 * @return cleaned query line
	 */
	public static String cleanQuery(String query, SnowballStemmer stemmer) {
		TreeSet<String> getQueries = FileStemmer.uniqueStems(query, stemmer);
		return String.join(" ", getQueries);
	}

}
