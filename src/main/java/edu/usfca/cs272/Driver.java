package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

/**
 * Class responsible for running this project based on the provided command-line
 * arguments. See the README for details.
 *
 * @author Athene Marston
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2023
 */
public class Driver {
	
	/**
	 * Initializes the classes necessary based on the provided command-line
	 * arguments. This includes (but is not limited to) how to build or search an
	 * inverted index.
	 *
	 * @param args flag/value pairs used to start this program
	 */
	public static void main(String[] args) {
		// store initial start time
		Instant start = Instant.now();

		ArgumentParser argumentParser = new ArgumentParser(args);
		SearchResultsInterface searchResults;
		InvertedIndex invertedIndex;
		WorkQueue queue = null;
		ThreadSafeInvertedIndex safe = null;

		if (argumentParser.hasFlag("-threads") | argumentParser.hasFlag("-html") | argumentParser.hasFlag("-server")) {
			int numThreads = argumentParser.getInteger("-threads", 5, 1);

			queue = new WorkQueue(numThreads);
			safe = new ThreadSafeInvertedIndex();
			invertedIndex = safe;
			searchResults = new MultithreadedSearchResults(safe, queue);

		} else {
			invertedIndex = new InvertedIndex();
			searchResults = new SearchResults(invertedIndex);
		}

		try {
			if (argumentParser.hasFlag("-html")) {
				int crawlNum = argumentParser.getInteger("-crawl", 1);	//total number of urls to crawl (including seed)
				
				//builds inverted index with web crawler from seed url
				try {
					if (safe != null && queue != null) {
						WebCrawler webCrawler = new WebCrawler(queue, safe, crawlNum);
						webCrawler.build(argumentParser.getString("-html"));
					}
				} catch (Exception e) {
					System.out.println("Error parsing url: " + e.getClass().getSimpleName());
				}
			}
			if (argumentParser.hasFlag("-text")) {
				try {
					// builds wordCounts map and invertedIndex
					if (safe != null && queue != null) {
						InvertedIndexBuilder.threadedBuild(queue, argumentParser.getPath("-text", null),
								safe);
					} else {
						InvertedIndexBuilder.build(argumentParser.getPath("-text", null), invertedIndex);
					}
				} catch (NullPointerException e) {
					System.out.println("Unable to find readable text file or directory.");
				} catch (IOException e) {
					System.out.println("Error retrieving list stems.");
				}
			}

			if (argumentParser.hasFlag("-query")) {
				boolean isPartial = argumentParser.hasFlag("-partial");

				try {
					// builds search results map
					searchResults.processFile(argumentParser.getPath("-query", null), isPartial);
				} catch (IOException e) {
					System.out.println("Error retrieving file.");
				} catch (NullPointerException e) {
					System.out.println("Unable to read query path.");
				}
			}
			
			if (argumentParser.hasFlag("-counts")) {
				try {
					invertedIndex.outputCounts(argumentParser.getPath("-counts", Path.of("counts.json")));
				} catch (IOException e) {
					System.out.println("Error writing to file (JsonWriter).");
				}
			}

			if (argumentParser.hasFlag("-index")) {
				try {
					invertedIndex.outputIndex(argumentParser.getPath("-index", Path.of("index.json")));
				} catch (IOException e) {
					System.out.println("Error writing to file (JsonWriter).");
				}
			}

			if (argumentParser.hasFlag("-results")) {
				try {
					searchResults.output(argumentParser.getPath("-results", Path.of("results.json")));
				} catch (IOException e) {
					System.out.println("Error writing to file (JsonWriter).");
				}
			}
			
			if (argumentParser.hasFlag("-server")) {
				try {
					if(safe != null && queue != null) {
						int port = argumentParser.getInteger("-server", 8080);
						WebServer server = new WebServer(port);
						server.initialize(safe);
					}
				} catch (Exception e) {
					System.out.println("Server error: " + e.getClass().getSimpleName());
				}
			}
		} finally {
			if (safe != null && queue != null) {
				queue.join();
			}
		}

		// calculate time elapsed and output
		long elapsed = Duration.between(start, Instant.now()).toMillis();
		double seconds = (double) elapsed / Duration.ofSeconds(1).toMillis();
		System.out.printf("Elapsed: %f seconds%n", seconds);
	}

}