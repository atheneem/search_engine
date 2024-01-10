package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import edu.usfca.cs272.InvertedIndex.FoundFile;

/**
 * Class for thread safe version of Search Results data structure
 * 
 * @author Athene Marston
 *
 */
public class MultithreadedSearchResults implements SearchResultsInterface {

	/**
	 * Stores String of queries and List of FoundFile data
	 */
	private final TreeMap<String, ArrayList<FoundFile>> results;

	/**
	 * The inverted index of data to search
	 */
	private final ThreadSafeInvertedIndex index;

	/**
	 * The work queue to use in multithreading
	 */
	private final WorkQueue queue;

	/**
	 * Search Results constructor
	 * 
	 * @param index InvertedIndex to use
	 * @param queue WorkQueue to use
	 */
	public MultithreadedSearchResults(ThreadSafeInvertedIndex index, WorkQueue queue) {
		this.results = new TreeMap<>();
		this.index = index;
		this.queue = queue;
	}

	/**
	 * Checks if query exists in results map
	 * 
	 * @param query to search for
	 * @return true if query exists in key set, else false
	 */
	private synchronized boolean hasQuery(String query) {
		return results.containsKey(query);
	}

	/**
	 * Adds results of a search query to SearchResults
	 * 
	 * @param queries       to add
	 * @param searchResults FoundFile results
	 */
	private synchronized void add(String queries, ArrayList<FoundFile> searchResults) {
		results.put(queries, searchResults);
	}

	@Override
	public synchronized Set<String> getQueries() {
		return Collections.unmodifiableSet(results.keySet());
	}

	@Override
	public List<FoundFile> getFiles(String query) {
		String queries = SearchResultsInterface.cleanQuery(query);

		synchronized (this) {
			var files = results.get(queries);
			if (files == null) { // if query not in map, return emptyList
				return Collections.emptyList();
			}
			return Collections.unmodifiableList(files);
		}
	}

	@Override
	public synchronized void output(Path location) throws IOException {
		JsonWriter.writeSearchResults(results, location);
	}

	@Override
	public synchronized String toString() {
		return results.toString();
	}

	@Override
	public void processFile(Path location, boolean isPartial) throws IOException {
		SearchResultsInterface.super.processFile(location, isPartial);
		queue.finish();

	}

	@Override
	public void processLine(String line, boolean isPartial) {
		SearchTask task = new SearchTask(line, isPartial);
		queue.execute(task);

	}

	/**
	 * Class for Runnable 'process line' task objects to execute
	 * 
	 * @author Athene Marston
	 *
	 */
	private class SearchTask implements Runnable {

		/**
		 * query line to process
		 */
		private final String line;

		/**
		 * True if partial search, else false
		 */
		private final boolean isPartial;

		/**
		 * Constructor for tasks
		 * 
		 * @param line      query line to search
		 * @param isPartial true if partial search, false if exact search
		 */
		public SearchTask(String line, boolean isPartial) {
			this.line = line;
			this.isPartial = isPartial;
		}

		@Override
		public void run() {
			TreeSet<String> getQueries = FileStemmer.uniqueStems(line);
			String queries = String.join(" ", getQueries);

			if (!hasQuery(queries)) {
				if (!getQueries.isEmpty()) {
					ArrayList<FoundFile> searchResults = index.search(getQueries, isPartial);
					add(queries, searchResults);
				}
			}
		}

		@Override
		public String toString() {
			return line.toString();
		}
	}
}