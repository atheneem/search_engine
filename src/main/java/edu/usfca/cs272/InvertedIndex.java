package edu.usfca.cs272;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Class for Map data structure (stores file names and counts)
 * 
 * @author Athene Marston
 */
public class InvertedIndex {

	/**
	 * Stores paths and their word counts (key = path, val = word count)
	 */
	private final Map<String, Integer> counts;

	/**
	 * Stores word stems and a map of files they exist in and the indexes they occur
	 */
	private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> index;

	/**
	 * Initializes map
	 */
	public InvertedIndex() {
		this.index = new TreeMap<>();
		this.counts = new TreeMap<>();
	}

	/**
	 * Updates file path's word stem count in "counts" map
	 * 
	 * @param location file path
	 */
	private void addCounts(String location) {
		Integer count = counts.get(location);
		if (count == null) {
			counts.put(location, 1);
		} else {
			counts.put(location, count + 1);
		}
	}

	/**
	 * Adds word stem position if not in list
	 * 
	 * @param word     word stem
	 * @param location string of path
	 * @param position position of word stem
	 * @return true if added, else false
	 */
	public boolean addInvertedIndex(String word, String location, Integer position) {
		TreeMap<String, TreeSet<Integer>> map = index.get(word);

		if (map == null) {
			map = new TreeMap<>();
			index.put(word, map);
		}

		TreeSet<Integer> set = map.get(location);

		if (set == null) {
			set = new TreeSet<>();
			map.put(location, set);
		}

		boolean modified = set.add(position);

		// update word in counts map
		if (modified) {
			addCounts(location);
		}

		return modified;

	}

	/**
	 * Adds a list of word stems in a file to "counts" and "index"
	 * 
	 * @param words    list of word stems in file
	 * @param location String of file name
	 * @return true if all added, else false
	 */
	public boolean addAll(List<String> words, String location) {
		boolean result = true;

		// add to inverted index
		Integer i = 1; // counter for wordstem position
		for (String s : words) { // for every wordstem in file
			if (addInvertedIndex(s, location, i) == false) {
				result = false;
			}
			i++;
		}
		return result;
	}

	/**
	 * Adds another inverted index to this Should not be used with overlapping
	 * positions for the same location
	 * 
	 * @param other InvertedIndex to merge
	 */
	public void addAll(InvertedIndex other) {

		for (var entry : other.index.entrySet()) {
			String word = entry.getKey(); // for every word
			TreeMap<String, TreeSet<Integer>> wordMap = index.get(word);
			TreeMap<String, TreeSet<Integer>> otherWordMap = entry.getValue();

			if (wordMap == null) { // if not in map, add all data
				index.put(word, otherWordMap);
			} else {
				for (var wordEntry : otherWordMap.entrySet()) {
					String file = wordEntry.getKey(); // else, if in map, for every file
					TreeSet<Integer> locations = wordMap.get(file);

					if (locations == null) { // if not in map, add file and locations
						wordMap.put(file, wordEntry.getValue());
					} else { // else, if in map, add locations to set
						locations.addAll(wordEntry.getValue());
					}
				}
			}
		}

		for (var entry : other.counts.entrySet()) {
			String path = entry.getKey();
			Integer count = counts.get(path);

			if (count == null) {
				counts.put(path, entry.getValue());
			} else {
				counts.put(path, entry.getValue() + count);
			}
		}

	}

	/**
	 * Check if file in counts map
	 * 
	 * @param location String of file name
	 * @return true if counts map contains file, else false
	 */
	public boolean countsContains(String location) {
		return counts.containsKey(location);
	}

	/**
	 * Checks if word stem exists in index
	 * 
	 * @param word word stem
	 * @return true if index contains the word stem, else false
	 */
	public boolean indexContains(String word) {
		return index.containsKey(word);
	}

	/**
	 * Checks if file exists in the word stem map
	 * 
	 * @param word     word stem
	 * @param location string of path
	 * @return true if file exists in map of word stem, else false
	 */
	public boolean indexContains(String word, String location) {
		return indexContains(word) && index.get(word).containsKey(location);
	}

	/**
	 * Checks if position exists in Set of file positions in stem map
	 * 
	 * @param word     word stem
	 * @param location string of path
	 * @param position position in file
	 * @return true if position in set for file, else false
	 */
	public boolean indexContains(String word, String location, int position) {
		return indexContains(word, location) && index.get(word).get(location).contains(position);
	}

	/**
	 * Writes counts to given output file
	 * 
	 * @param location path to output file
	 * @throws IOException error in JsonWriter.writeObject
	 */
	public void outputCounts(Path location) throws IOException {
		JsonWriter.writeObject(counts, location);
	}

	/**
	 * Writes index to given output file
	 * 
	 * @param location path to output file
	 * @throws IOException IOException error in JsonWriter.writeInvertedIndex
	 */
	public void outputIndex(Path location) throws IOException {
		JsonWriter.writeInvertedIndex(index, location);
	}

	/**
	 * Writes index in json format using given writer
	 * 
	 * @param writer writer to correct output location
	 * @throws IOException IOException error in JsonWriter.writeInvertedIndex
	 */
	public void outputIndex(Writer writer) throws IOException {
		JsonWriter.writeInvertedIndex(index, writer, 0);
	}

	/**
	 * size of index map
	 * 
	 * @return size of index map
	 */
	public Integer size() {
		return index.size();
	}

	/**
	 * returns the size of the word's map (0 if word not in map)
	 * 
	 * @param word word stem
	 * @return size of the word's map, null if DNE
	 */
	public int size(String word) {
		TreeMap<String, TreeSet<Integer>> map = index.get(word);
		if (map == null) {
			return 0;
		} else
			return map.size();
	}

	/**
	 * return the size of set of indexes for a given word in a file
	 * 
	 * @param word     word stem
	 * @param location String of file path
	 * @return number of occurrences of word in file
	 */
	public int size(String word, String location) {
		TreeMap<String, TreeSet<Integer>> map = index.get(word);
		if (map == null) {
			return 0;
		}

		TreeSet<Integer> set = map.get(location);
		if (set == null) {
			return 0;
		}

		return set.size();
	}

	/**
	 * return the size of counts map
	 * 
	 * @return size of counts map
	 */
	public int countsSize() {
		return counts.size();
	}

	/**
	 * Returns the word stem count for given file (null if file not in map)
	 * 
	 * @param location String of file path
	 * @return number of word stems in file, null if file DNE
	 */
	public int getWordCount(String location) {
		return counts.getOrDefault(location, 0);
	}

	/**
	 * Returns an unmodifiable view of word stems in index
	 * 
	 * @return unmodifiable view of the words in inverted index
	 */
	public Collection<String> getWords() {
		return Collections.unmodifiableCollection(index.keySet());
	}

	/**
	 * Returns an unmodifiable view of counts key set
	 * 
	 * @return unmodifiable view of the counts map
	 */
	public Collection<String> getLocations() {
		return Collections.unmodifiableCollection(counts.keySet());
	}

	/**
	 * Returns an unmodifiable view of counts map
	 * 
	 * @return unmodifiable view of the counts map
	 */
	public Map<String, Integer> getCounts() {
		return Collections.unmodifiableMap(counts);
	}

	/**
	 * Returns unmodifiable view of stem's key set (emptySet if stem not in map)
	 * 
	 * @param word word stem
	 * @return unmodifiable view of stem's map, emptySet if DNE
	 */
	public Collection<String> getLocations(String word) {
		if (index.get(word) == null) { // if stem not in map, return emptyList
			return Collections.emptySet();
		}
		return Collections.unmodifiableCollection(index.get(word).keySet());
	}

	/**
	 * Returns unmodifiable view of stem's positions in given file (emptySet if stem
	 * or file not in map)
	 * 
	 * @param word     word stem
	 * @param location string of file name
	 * @return unmodifiable view of stem's positions in given file, emptySet if stem
	 *         or file DNE
	 */
	public Collection<Integer> getPositions(String word, String location) {
		TreeMap<String, TreeSet<Integer>> getWord = index.get(word);

		if (getWord != null) { // if word in map
			TreeSet<Integer> getLocation = getWord.get(location);

			if (getLocation != null) { // if location in word map
				return Collections.unmodifiableCollection(getLocation);
			}
		}

		return Collections.emptySet(); // else return empty set

	}

	@Override
	public String toString() {
		return "Inverted Index:\n" + index.toString() + "\n\nWord Counts:\n" + counts.toString();
	}

	/**
	 * Calls the correct search method and returns a list of FoundFiles for given
	 * query line
	 * 
	 * @param queries   set of query words to find exact search results for
	 * @param isPartial true if partial search, false if exact
	 * @return a list of FoundFiles associated with given query
	 */
	public ArrayList<FoundFile> search(Set<String> queries, boolean isPartial) {
		return isPartial ? partialSearch(queries) : exactSearch(queries);
	}

	/**
	 * Helper function for search that finds results for each corresponding file to
	 * query word
	 * 
	 * @param matches  map from file name to FoundFile object
	 * @param toReturn list of FoundFiles associated with given query
	 * @param word     the word key to map to given result
	 * @param loop     set of files to loop through
	 */
	private void searchHelper(Map<String, FoundFile> matches, ArrayList<FoundFile> toReturn, String word,
			Set<String> loop) {
		for (String file : loop) { // for every file
			FoundFile found = matches.get(file);
			if (found == null) {
				found = new FoundFile(file);
				toReturn.add(found);
				matches.put(file, found);
			}
			found.update(word); // add/update counts
		}
	}

	/**
	 * Returns a list of FoundFiles for given query line
	 * 
	 * @param queries set of query words to find exact search results for
	 * @return a list of FoundFiles associated with given query
	 */
	public ArrayList<FoundFile> exactSearch(Set<String> queries) { // just one line

		Map<String, FoundFile> matches = new HashMap<>();
		ArrayList<FoundFile> toReturn = new ArrayList<>();

		for (String word : queries) { // for every query word
			var locations = index.get(word);
			if (locations != null) { // if word in index
				searchHelper(matches, toReturn, word, locations.keySet());
			}
		}

		Collections.sort(toReturn);
		return toReturn;
	}

	/**
	 * Returns a list of FoundFiles for given query line
	 * 
	 * @param queries set of query words to find partial search results for
	 * @return a list of FoundFiles associated with given query
	 */
	public ArrayList<FoundFile> partialSearch(Set<String> queries) {

		Map<String, FoundFile> matches = new HashMap<>();
		ArrayList<FoundFile> toReturn = new ArrayList<>();

		for (String word : queries) { // for every query word
			for (var indexStem : index.tailMap(word).entrySet()) { // for words in index, add files if stem contains
																	// query
				String stem = indexStem.getKey();
				if (stem.startsWith(word)) {
					searchHelper(matches, toReturn, stem, indexStem.getValue().keySet());

				} else { // out of range of associated stems
					break;
				}
			}
		}

		Collections.sort(toReturn);
		return toReturn;
	}

	/**
	 * Class for FoundFile objects (stores its word count, score, and path)
	 * 
	 * @author Athene Marston
	 *
	 */
	public class FoundFile implements Comparable<FoundFile> {
		/**
		 * File word count
		 */
		private int count;
		/**
		 * Score associated with query (total matches / total words)
		 */
		private double score;
		/**
		 * String representation of file path
		 */
		private final String path;

		/**
		 * FoundFile constructor
		 * 
		 * @param path string of file path
		 */
		public FoundFile(String path) {
			this.count = 0;
			this.score = 0.0;
			this.path = path;
		}

		/**
		 * Updates the count and score of given FoundFile object
		 * 
		 * @param word query word
		 */
		private void update(String word) {
			this.count += index.get(word).get(path).size();
			this.score = (double) count / counts.get(path);
		}

		/**
		 * Gets FoundFile's word count
		 * 
		 * @return FoundFile's word count
		 */
		public Integer getCount() {
			return this.count;
		}

		/**
		 * Gets FoundFile's score
		 * 
		 * @return FoundFile's score
		 */
		public Double getScore() {
			return this.score;
		}

		/**
		 * Gets FoundFile's path
		 * 
		 * @return FoundFile's path
		 */
		public String getPath() {
			return this.path;
		}

		@Override
		public int compareTo(FoundFile o) {
			if (this.score == o.score) {
				if (this.count == o.count) { // if same score and count
					return String.CASE_INSENSITIVE_ORDER.compare(this.path, o.path);
				}
				return Integer.compare(o.count, this.count); // if same score (from biggest to smallest)
			}
			return Double.compare(o.score, this.score); // sort from biggest to smallest score
		}

		@Override
		public String toString() {
			return "\nCount: " + count + ", Score: " + score + ", Path: " + this.path;
		}
	}

}