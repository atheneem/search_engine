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

/**
 * Thread save version of InvertedIndex
 * 
 * @author Athene Marston
 *
 */
public class ThreadSafeInvertedIndex extends InvertedIndex {
	
	/** The lock used to protect concurrent access to the underlying index. */
	private final MultiReaderLock lock;

	/**
	 * Map from url in the index to a page snippet
	 */
	private final Map<String, String> HTMLSnippet;

	/**
	 * Map from url to String[title, content length, time stamp]
	 */
	private final Map<String, String[]> HTMLStatistics;

	/**
	 * Initializes index
	 */
	public ThreadSafeInvertedIndex() {
		super();
		lock = new MultiReaderLock();
		HTMLSnippet = new HashMap<>();
		HTMLStatistics = new HashMap<>();
	}

	/**
	 * Adds a url and statistics to map
	 * 
	 * @param url   string
	 * @param stats statistics to add, in the form {title, length, time}
	 */
	public void addHTMLStatistics(String url, String[] stats) {
		lock.writeLock().lock();
		try {
			HTMLStatistics.put(url, stats);
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Returns an array of the title, length, and access time-stamp of a url
	 * 
	 * @param url to get statistics for
	 * @return an array of the statistics in the form {title, length, time}
	 */
	public String[] getHTMLStatistics(String url) {
		lock.readLock().lock();
		try {
			return HTMLStatistics.get(url);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Returns an unmodifiable view of HTMLStatistics map
	 * 
	 * @return unmodifiable view of the HTMLStatistics map
	 */
	public Map<String, String[]> getHTMLStatisticsMap() {
		lock.readLock().lock();
		try {
			return Collections.unmodifiableMap(HTMLStatistics);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Adds a url and snippet to map
	 * 
	 * @param url     string
	 * @param snippet to add
	 */
	public void addHTMLSnippet(String url, String snippet) {
		lock.writeLock().lock();
		try {
			HTMLSnippet.put(url, snippet);
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * returns the page snippet associated with the url or null if not in map
	 * 
	 * @param url to get info for
	 * @return the String[title, content length, page snippet], null if DNE
	 */
	public String getHTMLSnippet(String url) {
		lock.readLock().lock();
		try {
			return HTMLSnippet.get(url);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Returns an unmodifiable view of HTMLInfo map
	 * 
	 * @return unmodifiable view of the HTMLInfo map
	 */
	public Map<String, String> getHTMLSnippetMap() {
		lock.readLock().lock();
		try {
			return Collections.unmodifiableMap(HTMLSnippet);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean addInvertedIndex(String word, String location, Integer position) {
		lock.writeLock().lock();
		try {
			return super.addInvertedIndex(word, location, position);
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public boolean addAll(List<String> words, String location) {
		lock.writeLock().lock();
		try {
			return super.addAll(words, location);
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public void addAll(InvertedIndex other) {
		lock.writeLock().lock();
		try {
			super.addAll(other);
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public boolean countsContains(String location) {
		lock.readLock().lock();
		try {
			return super.countsContains(location);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean indexContains(String word) {
		lock.readLock().lock();
		try {
			return super.indexContains(word);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean indexContains(String word, String location) {
		lock.readLock().lock();
		try {
			return super.indexContains(word, location);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean indexContains(String word, String location, int position) {
		lock.readLock().lock();
		try {
			return super.indexContains(word, location, position);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public void outputCounts(Path location) throws IOException {
		lock.readLock().lock();
		try {
			super.outputCounts(location);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public void outputIndex(Path location) throws IOException {
		lock.readLock().lock();
		try {
			super.outputIndex(location);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public void outputIndex(Writer writer) throws IOException {
		lock.readLock().lock();
		try {
			super.outputIndex(writer);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Integer size() {
		lock.readLock().lock();
		try {
			return super.size();
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public int size(String word) {
		lock.readLock().lock();
		try {
			return super.size(word);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public int size(String word, String location) {
		lock.readLock().lock();
		try {
			return super.size(word, location);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public int countsSize() {
		lock.readLock().lock();
		try {
			return super.countsSize();
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public int getWordCount(String location) {
		lock.readLock().lock();
		try {
			return super.getWordCount(location);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Collection<String> getWords() {
		lock.readLock().lock();
		try {
			return super.getWords();
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Collection<String> getLocations() {
		lock.readLock().lock();
		try {
			return super.getLocations();
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Map<String, Integer> getCounts() {
		lock.readLock().lock();
		try {
			return super.getCounts();
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Collection<String> getLocations(String word) {
		lock.readLock().lock();
		try {
			return super.getLocations(word);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Collection<Integer> getPositions(String word, String location) {
		lock.readLock().lock();
		try {
			return super.getPositions(word, location);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public String toString() {
		lock.readLock().lock();
		try {
			return super.toString();
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public ArrayList<FoundFile> exactSearch(Set<String> queries) {
		lock.readLock().lock();
		try {
			return super.exactSearch(queries);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public ArrayList<FoundFile> partialSearch(Set<String> queries) {
		lock.readLock().lock();
		try {
			return super.partialSearch(queries);
		} finally {
			lock.readLock().unlock();
		}
	}

}
