package edu.usfca.cs272;

import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import edu.usfca.cs272.InvertedIndex.FoundFile;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Class for Search Results data structure (stores queries and associated file
 * data)
 * 
 * @author Athene Marston
 *
 */
public class SearchResults implements SearchResultsInterface {

	/**
	 * Stores String of queries and List of FoundFile data
	 */
	private final TreeMap<String, ArrayList<FoundFile>> results;

	/**
	 * The inverted index of data to search
	 */
	private final InvertedIndex index;

	/**
	 * Stemmer to use for this class
	 */
	private final SnowballStemmer stemmer = new SnowballStemmer(ENGLISH);

	/**
	 * Search Results constructor
	 * 
	 * @param index InvertedIndex to use
	 */
	public SearchResults(InvertedIndex index) {
		this.results = new TreeMap<>();
		this.index = index;
	}

	@Override
	public void processLine(String line, boolean isPartial) {
		TreeSet<String> getQueries = FileStemmer.uniqueStems(line, stemmer);
		String queries = String.join(" ", getQueries);

		if (!results.containsKey(queries)) {
			if (!getQueries.isEmpty()) {
				ArrayList<FoundFile> searchResults = index.search(getQueries, isPartial);
				results.put(queries, searchResults);
			}
		}
	}

	@Override
	public Set<String> getQueries() {
		return Collections.unmodifiableSet(results.keySet());
	}

	@Override
	public List<FoundFile> getFiles(String query) {
		String queries = SearchResultsInterface.cleanQuery(query, stemmer);

		var files = results.get(queries);
		if (files == null) { // if query not in map, return emptyList
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(files);
	}

	@Override
	public void output(Path location) throws IOException {
		JsonWriter.writeSearchResults(results, location);
	}

	@Override
	public String toString() {
		return results.toString();
	}
}