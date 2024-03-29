package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Iterator;
import java.util.List;
import edu.usfca.cs272.InvertedIndex.FoundFile;

/**
 * Outputs several simple data structures in "pretty" JSON format where newlines
 * are used to separate elements and nested elements are indented using spaces.
 *
 * Warning: This class is not thread-safe. If multiple threads access this class
 * concurrently, access must be synchronized externally.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2023
 */
public class JsonWriter {
	/**
	 * Indents the writer by the specified number of times. Does nothing if the
	 * indentation level is 0 or less.
	 *
	 * @param writer the writer to use
	 * @param indent the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeIndent(Writer writer, int indent) throws IOException {
		while (indent-- > 0) {
			writer.write("  ");
		}
	}

	/**
	 * Indents and then writes the String element.
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @param indent  the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeIndent(String element, Writer writer, int indent) throws IOException {
		writeIndent(writer, indent);
		writer.write(element);
	}

	/**
	 * Indents and then writes the text element surrounded by {@code " "} quotation
	 * marks.
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @param indent  the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeQuote(String element, Writer writer, int indent) throws IOException {
		writeIndent(writer, indent);
		writer.write('"');
		writer.write(element);
		writer.write('"');
	}

	/**
	 * Writes the elements as a pretty JSON array.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param indent   the initial indent level; the first bracket is not indented,
	 *                 inner elements are indented by one, and the last bracket is
	 *                 indented at the initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 */
	public static void writeArray(Collection<? extends Number> elements, Writer writer, int indent) throws IOException {
		writer.write("[");

		Iterator<? extends Number> iter = elements.iterator();

		if (iter.hasNext()) {
			writer.write("\n");
			writeIndent(writer, indent + 1);
			writer.write(iter.next().toString());
		}

		while (iter.hasNext()) {
			writer.write(",\n");
			writeIndent(writer, indent + 1);
			writer.write(iter.next().toString());
		}

		writer.write("\n");
		writeIndent(writer, indent);
		writer.write("]");
	}

	/**
	 * Writes the elements as a pretty JSON array to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeArray(Collection, Writer, int)
	 */
	public static void writeArray(Collection<? extends Number> elements, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeArray(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON array.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeArray(Collection, Writer, int)
	 */
	public static String writeArray(Collection<? extends Number> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeArray(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes an entry for the write object function
	 * 
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param indent   the initial indent level; the first bracket is not indented,
	 *                 inner elements are indented by one, and the last bracket is
	 *                 indented at the initial indentation level
	 * @param curr     the current element to write
	 * @throws IOException if an IO error occurs
	 */
	public static void writeObjectEntry(Map<String, ? extends Number> elements, Writer writer, int indent,
			Entry<String, ? extends Number> curr) throws IOException {
		writer.write("\n");
		writeIndent(writer, indent + 1);
		writer.write('"' + curr.getKey().toString() + '"' + ": ");
		writer.write(curr.getValue().toString());
	}

	/**
	 * Writes the elements as a pretty JSON object.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param indent   the initial indent level; the first bracket is not indented,
	 *                 inner elements are indented by one, and the last bracket is
	 *                 indented at the initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 */
	public static void writeObject(Map<String, ? extends Number> elements, Writer writer, int indent)
			throws IOException {
		writer.write("{");

		var iter = elements.entrySet().iterator();

		if (iter.hasNext()) {
			Entry<String, ? extends Number> curr = iter.next();
			writeObjectEntry(elements, writer, indent, curr);
		}

		while (iter.hasNext()) {
			writer.write(",");
			Entry<String, ? extends Number> curr = iter.next();
			writeObjectEntry(elements, writer, indent, curr);
		}

		writer.write("\n");
		writeIndent(writer, indent);
		writer.write("}");
	}

	/**
	 * Writes the elements as a pretty JSON object to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeObject(Map, Writer, int)
	 */
	public static void writeObject(Map<String, ? extends Number> elements, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeObject(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON object.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeObject(Map, Writer, int)
	 */
	public static String writeObject(Map<String, ? extends Number> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeObject(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes an entry for the write Index function
	 * 
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param indent   the initial indent level; the first bracket is not indented,
	 *                 inner elements are indented by one, and the last bracket is
	 *                 indented at the initial indentation level
	 * @param curr     the current element to write
	 * @throws IOException if an IO error occurs
	 */
	public static void writeInvertedIndexEntry(
			Map<String, ? extends Map<String, ? extends Collection<? extends Number>>> elements, Writer writer,
			int indent, Entry<String, ? extends Map<String, ? extends Collection<? extends Number>>> curr)
			throws IOException {
		writer.write("\n");
		writeIndent(writer, indent + 1);
		writer.write('"' + curr.getKey().toString() + '"' + ": ");
		writeObjectArrays(curr.getValue(), writer, indent + 1);
	}

	/**
	 * Writes the elements as a pretty JSON object.
	 *
	 * @param index  the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *               inner elements are indented by one, and the last bracket is
	 *               indented at the initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 */
	public static void writeInvertedIndex(
			Map<String, ? extends Map<String, ? extends Collection<? extends Number>>> index, Writer writer, int indent)
			throws IOException {
		writer.write("{");

		var iter = index.entrySet().iterator();

		if (iter.hasNext()) {
			var curr = iter.next();
			writeInvertedIndexEntry(index, writer, indent, curr);
		}

		while (iter.hasNext()) {
			writer.write(",");
			var curr = iter.next();
			writeInvertedIndexEntry(index, writer, indent, curr);
		}

		writer.write("\n");
		writeIndent(writer, indent);
		writer.write("}\n");

	}

	/**
	 * Writes the elements as a pretty JSON object to file.
	 *
	 * @param index the elements to write
	 * @param path  the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeObject(Map, Writer, int)
	 */
	public static void writeInvertedIndex(
			Map<String, ? extends Map<String, ? extends Collection<? extends Number>>> index, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeInvertedIndex(index, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON object.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeObject(Map, Writer, int)
	 */
	public static String writeInvertedIndex(
			Map<String, ? extends Map<String, ? extends Collection<? extends Number>>> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeInvertedIndex(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes a single Query instance in JSON format.
	 * 
	 * @param query  Query object to write
	 * @param writer writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *               inner elements are indented by one, and the last bracket is
	 *               indented at the initial indentation level
	 * @throws IOException if an IO error occurs
	 */
	public static void writeQuery(String query, Writer writer, int indent) throws IOException {
		List<String> words = Arrays.asList(query.split(" ")); // ArrayList of query words
		writeIndent(writer, indent);
		writer.write('"');

		var iter = words.iterator();
		if (iter.hasNext()) {
			writer.write(iter.next());
		}
		while (iter.hasNext()) {
			writer.write(" " + iter.next());
		}
		writer.write('"' + ": ");
	}

	/**
	 * Writes a list of file instances in JSON format.
	 * 
	 * @param list   list of FoundFiles
	 * @param writer writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *               inner elements are indented by one, and the last bracket is
	 *               indented at the initial indentation level
	 * @throws IOException if an IO error occurs
	 */
	public static void writeFoundFile(List<FoundFile> list, Writer writer, int indent) throws IOException {
		writer.write("[");

		Iterator<FoundFile> iter = list.iterator();

		if (iter.hasNext()) {
			writer.write("\n");
			writeFile(iter.next(), writer, indent + 1);
		}

		while (iter.hasNext()) {
			writer.write(",\n");
			writeFile(iter.next(), writer, indent + 1);
		}

		writer.write("\n");
		writeIndent(writer, indent);
		writer.write("]");
	}

	/**
	 * Writes a single file instance in JSON format.
	 * 
	 * @param file   FoundFile object to write
	 * @param writer writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *               inner elements are indented by one, and the last bracket is
	 *               indented at the initial indentation level
	 * @throws IOException if an IO error occurs
	 */
	public static void writeFile(FoundFile file, Writer writer, int indent) throws IOException {
		writeIndent(writer, indent);
		writer.write("{\n");
		writeIndent(writer, indent + 1);
		writer.write('"' + "count" + '"' + ": " + file.getCount() + ",\n");
		writeIndent(writer, indent + 1);
		writer.write('"' + "score" + '"' + ": " + String.format("%.8f", file.getScore()) + ",\n");
		writeIndent(writer, indent + 1);
		writer.write('"' + "where" + '"' + ": " + '"' + file.getPath() + '"' + '\n');
		writeIndent(writer, indent);
		writer.write("}");
	}

	/**
	 * Writes an entry for the write search results function
	 * 
	 * @param results the elements to write
	 * @param writer  the writer to use
	 * @param indent  the initial indent level; the first bracket is not indented,
	 *                inner elements are indented by one, and the last bracket is
	 *                indented at the initial indentation level
	 * @param curr    the current element to write
	 * @throws IOException if an IO error occurs
	 */
	public static void writeSearchResultsEntry(Map<String, ? extends List<FoundFile>> results, Writer writer,
			int indent, Entry<String, ? extends List<FoundFile>> curr) throws IOException {
		writer.write("\n");
		writeIndent(writer, indent);
		writeQuery(curr.getKey(), writer, indent + 1);
		writeFoundFile(curr.getValue(), writer, indent + 1);
	}

	/**
	 * Writes the elements as a pretty JSON object.
	 *
	 * @param results the elements to write
	 * @param writer  the writer to use
	 * @param indent  the initial indent level; the first bracket is not indented,
	 *                inner elements are indented by one, and the last bracket is
	 *                indented at the initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 */
	public static void writeSearchResults(Map<String, ? extends List<FoundFile>> results, Writer writer, int indent)
			throws IOException {
		writer.write("{");

		var iter = results.entrySet().iterator();
		Entry<String, ? extends List<FoundFile>> curr;

		if (iter.hasNext()) {
			curr = iter.next();
			writeSearchResultsEntry(results, writer, indent, curr);
		}

		while (iter.hasNext()) {
			writer.write(",");
			curr = iter.next();
			writeSearchResultsEntry(results, writer, indent, curr);

		}
		writer.write("\n");
		writeIndent(writer, indent);
		writer.write("}");
	}

	/**
	 * Writes the elements as a pretty JSON object to file.
	 *
	 * @param results the elements to write
	 * @param path    the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeObject(Map, Writer, int)
	 */
	public static void writeSearchResults(Map<String, ? extends List<FoundFile>> results, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeSearchResults(results, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON object.
	 *
	 * @param results the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeObject(Map, Writer, int)
	 */
	public static String writeSearchResults(Map<String, ? extends List<FoundFile>> results) {
		try {
			StringWriter writer = new StringWriter();
			writeSearchResults(results, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes an entry for the write object arrays function
	 * 
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param indent   the initial indent level; the first bracket is not indented,
	 *                 inner elements are indented by one, and the last bracket is
	 *                 indented at the initial indentation level
	 * @param curr     the current element to write
	 * @throws IOException if an IO error occurs
	 */
	public static void writeObjectArraysEntry(Map<String, ? extends Collection<? extends Number>> elements,
			Writer writer, int indent, Entry<String, ? extends Collection<? extends Number>> curr) throws IOException {
		writer.write("\n");
		writeIndent(writer, indent + 1);
		writer.write('"' + curr.getKey().toString() + '"' + ": ");
		writeArray(curr.getValue(), writer, indent + 1);
	}

	/**
	 * Writes the elements as a pretty JSON object with nested arrays. The generic
	 * notation used allows this method to be used for any type of map with any type
	 * of nested collection of number objects.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param indent   the initial indent level; the first bracket is not indented,
	 *                 inner elements are indented by one, and the last bracket is
	 *                 indented at the initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 * @see #writeArray(Collection)
	 */
	public static void writeObjectArrays(Map<String, ? extends Collection<? extends Number>> elements, Writer writer,
			int indent) throws IOException {
		writer.write("{");

		var iter = elements.entrySet().iterator();

		if (iter.hasNext()) {
			var curr = iter.next();
			writeObjectArraysEntry(elements, writer, indent, curr);
		}

		while (iter.hasNext()) {
			writer.write(",");
			var curr = iter.next();
			writeObjectArraysEntry(elements, writer, indent, curr);
		}

		writer.write("\n");
		writeIndent(writer, indent);
		writer.write("}");
	}

	/**
	 * Writes the elements as a pretty JSON object with nested arrays to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeObjectArrays(Map, Writer, int)
	 */
	public static void writeObjectArrays(Map<String, ? extends Collection<? extends Number>> elements, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeObjectArrays(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON object with nested arrays.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeObjectArrays(Map, Writer, int)
	 */
	public static String writeObjectArrays(Map<String, ? extends Collection<? extends Number>> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeObjectArrays(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a pretty JSON array with nested objects. The generic
	 * notation used allows this method to be used for any type of collection with
	 * any type of nested map of String keys to number objects.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param indent   the initial indent level; the first bracket is not indented,
	 *                 inner elements are indented by one, and the last bracket is
	 *                 indented at the initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 * @see #writeObject(Map)
	 */
	public static void writeArrayObjects(Collection<? extends Map<String, ? extends Number>> elements, Writer writer,
			int indent) throws IOException {

		writer.write("[");

		var iter = elements.iterator(); // iterate through elms
		// collection of maps, string to num
		if (iter.hasNext()) {
			writer.write("\n");
			writeIndent(writer, indent + 1);
			writeObject(iter.next(), writer, indent + 1);
		}

		while (iter.hasNext()) {
			writer.write(",\n");
			writeIndent(writer, indent + 1);
			writeObject(iter.next(), writer, indent + 1);
		}
		writer.write("\n");
		writeIndent(writer, indent);
		writer.write("]");
	}

	/**
	 * Writes the elements as a pretty JSON array with nested objects to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeArrayObjects(Collection)
	 */
	public static void writeArrayObjects(Collection<? extends Map<String, ? extends Number>> elements, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeArrayObjects(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON array with nested objects.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeArrayObjects(Collection)
	 */
	public static String writeArrayObjects(Collection<? extends Map<String, ? extends Number>> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeArrayObjects(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

}