package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;
import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Class responsible for accessing readable text files opens files, stems, and
 * add to the data structure
 * 
 * @author Athene Marston
 */
public class InvertedIndexBuilder {

	/**
	 * Builds counts map and InvertedIndex
	 * 
	 * @param location path path to input text/s
	 * @param index    InvertedIndex for inverted index of word stems and counts map
	 * @throws IOException          error in FileFinder
	 * @throws NullPointerException null path provided
	 */
	public static void build(Path location, InvertedIndex index) throws IOException, NullPointerException {
		List<Path> list = FileFinder.listText(location); // text files in path

		for (Path p : list) { // for every text file, stem and addAll to index
			parseFile(p, index);
		}
	}

	/**
	 * Parses stems and adds the file and words to InvertedIndex
	 * 
	 * @param location a file in path
	 * @param index    InvertedIndex
	 * @throws IOException error in FileStemmer
	 */
	public static void parseFile(Path location, InvertedIndex index) throws IOException {
		Stemmer stemmer = new SnowballStemmer(ENGLISH);
		Integer i = 1;
		String path = location.toString();

		try (BufferedReader in = Files.newBufferedReader(location, UTF_8)) {
			String line = null;
			while ((line = in.readLine()) != null) { // add to list line by line
				String[] words = FileStemmer.parse(line);

				for (String word : words) {
					String stem = stemmer.stem(word).toString();
					index.addInvertedIndex(stem, path, i);
					i++;
				}
			}
		}
	}

	/**
	 * A multithreaded builder method for counts map and inverted index
	 * 
	 * @param location path path to input text/s
	 * @param index    InvertedIndex for inverted index of word stems and counts map
	 * @param queue    WorkQueue to use
	 * @throws IOException          error in FileFinder
	 * @throws NullPointerException null path provided
	 */
	public static void threadedBuild(WorkQueue queue, Path location, ThreadSafeInvertedIndex index)
			throws IOException, NullPointerException {
		List<Path> list = FileFinder.listText(location); // text files in path

		for (Path path : list) {
			BuildTask task = new BuildTask(path, index);
			queue.execute(task);
		}
		queue.finish();
	}

	/**
	 * Class for Runnable 'parse file' task objects to execute
	 * 
	 * @author Athene Marston
	 *
	 */
	private static class BuildTask implements Runnable {

		/**
		 * text file to parse
		 */
		private final Path path;

		/**
		 * InvertedIndex to add to
		 */
		private final ThreadSafeInvertedIndex index;

		/**
		 * Stores local data from parsed file
		 */
		private final InvertedIndex local;

		/**
		 * Constructor for tasks
		 * 
		 * @param path  path of text file to parse
		 * @param index InvertedIndex to add to
		 */
		public BuildTask(Path path, ThreadSafeInvertedIndex index) {
			this.path = path;
			this.index = index;
			this.local = new InvertedIndex();
		}

		@Override
		public void run() {
			try {
				parseFile(path, local);
				index.addAll(local);
			} catch (IOException e) {
				System.out.printf("Error stemming file: %s", path.toString());
			}
		}

		@Override
		public String toString() {
			return path.toString();
		}

	}

	/**
	 * Builds counts map and inverted index from a url's html
	 * 
	 * @param seed  initial crawl url
	 * @param index InvertedIndex
	 * @throws URISyntaxException    error normalizing url
	 * @throws MalformedURLException error normalizing url
	 */
	public static void buildHtml(String seed, InvertedIndex index) throws MalformedURLException, URISyntaxException {
		Stemmer stemmer = new SnowballStemmer(ENGLISH);
		int i = 1;

		URL url = LinkFinder.normalize(new URL(seed)); // location

		String html = HtmlFetcher.fetch(url, 3);

		if (html != null) { // if valid html
			html = HtmlCleaner.stripHtml(html);

			String[] htmlLines = FileStemmer.parse(html);

			for (String line : htmlLines) { // for every word, stem + add to index
				String stem = stemmer.stem(line).toString();
				index.addInvertedIndex(stem, url.toString(), i);
				i++;
			}
		}
	}

}
