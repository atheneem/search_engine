package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A specialized version of {@link HttpsFetcher} that follows redirects and
 * returns HTML content if possible.
 *
 * @see HttpsFetcher
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2023
 */
public class HtmlFetcher {

	/** Format used for all date output. */
	public static final String longDateFormat = "MMMM dd yyyy, HH:mm:ss.SSS";

	/** Used to format dates (already thread-safe). */
	public static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(longDateFormat);

	/**
	 * Returns {@code true} if and only if there is a "Content-Type" header and the
	 * first value of that header starts with the value "text/html"
	 * (case-insensitive).
	 *
	 * @param headers the HTTP/1.1 headers to parse
	 * @return {@code true} if the headers indicate the content type is HTML
	 */
	public static boolean isHtml(Map<String, List<String>> headers) {
		List<String> contentType = headers.get("Content-Type");
		if (contentType == null) {
			return false;
		} else if (contentType.size() > 0 && contentType.get(0).startsWith("text/html")) {
			return true;
		}

		return false;
	}

	/**
	 * Parses the HTTP status code from the provided HTTP headers, assuming the
	 * status line is stored under the {@code null} key.
	 *
	 * @param headers the HTTP/1.1 headers to parse
	 * @return the HTTP status code or -1 if unable to parse for any reasons
	 */
	public static int getStatusCode(Map<String, List<String>> headers) {
		List<String> statusCode = headers.get(null);

		try {
			String[] components = statusCode.get(0).split(" ");
			return Integer.valueOf(components[1]);
		} catch (Exception e) {
			return -1;
		}
	}

	/**
	 * Returns the length of the page from the HTTP headers, null if not found
	 * 
	 * @param headers the HTTP/1.1 headers to parse
	 * @return the length of the fetched web page, null if header not found
	 */
	public static String getContentLength(Map<String, List<String>> headers) {
		List<String> length = headers.get("Content-Length");

		if (length == null) {
			return "No length provided.";
		}
		return length.get(0);
	}

	/**
	 * Returns the title of the web page or "Untitled" if can't be found
	 * 
	 * @param html to find title from
	 * @return title in "title" html tag, "Untitled" if not found
	 */
	public static String getTitle(String html) {
		Pattern pattern = Pattern.compile("<title>(.*?)</title>");
		String title = null;

		try {
			Matcher matcher = pattern.matcher(html);
			if (matcher.find()) {
				title = matcher.group(1);
			}

			if (title == null || title.isBlank()) {
				title = "Untitled";
			}
			return title;

		} catch (Exception e) {
			return "Untitled";
		}
	}

	/**
	 * If the HTTP status code is between 300 and 399 (inclusive) indicating a
	 * redirect, returns the first redirect location if it is provided. Otherwise
	 * returns {@code null}.
	 *
	 * @param headers the HTTP/1.1 headers to parse
	 * @return the first redirected location if the headers indicate a redirect
	 */
	public static String getRedirect(Map<String, List<String>> headers) {
		int code = getStatusCode(headers);

		if (code <= 399 && code >= 300) {
			try {
				return headers.get("Location").get(0);
			} catch (Exception e) {
				return null;
			}

		}
		return null;
	}

	/**
	 * Fetches the resource at the URL using HTTP/1.1 and sockets. If the status
	 * code is 200 and the content type is HTML, returns the HTML as a single
	 * string. If the status code is a valid redirect, will follow that redirect if
	 * the number of redirects is greater than 0. Otherwise, returns {@code null}.
	 *
	 * @param url       the url to fetch
	 * @param redirects the number of times to follow redirects
	 * @return the html or {@code null} if unable to fetch the resource or the
	 *         resource is not html
	 *
	 * @see HttpsFetcher#openConnection(URL)
	 * @see HttpsFetcher#printGetRequest(PrintWriter, URL)
	 * @see HttpsFetcher#getHeaderFields(BufferedReader)
	 *
	 * @see String#join(CharSequence, CharSequence...)
	 *
	 * @see #isHtml(Map)
	 * @see #getRedirect(Map)
	 */
	public static String fetch(URL url, int redirects) {
		return fetch(url, redirects, null);
	}

	/**
	 * Fetches url content and additionally adds statistics to index (if provided/not null).
	 * 
	 * If the status code is 200 and the content type is HTML, returns the HTML as a
	 * single string. If the status code is a valid redirect, will follow that
	 * redirect if the number of redirects is greater than 0. Otherwise, returns
	 * {@code null}.
	 * 
	 * @param url       the url to fetch
	 * @param redirects the number of times to follow redirects
	 * @param index     thread safe index to add statistics to, if provided (non-null)
	 * @return the html or {@code null} if unable to fetch the resource or the
	 *         resource is not html
	 */
	public static String fetch(URL url, int redirects, ThreadSafeInvertedIndex index) {
		String html = null;

		try (Socket socket = HttpsFetcher.openConnection(url);
				PrintWriter request = new PrintWriter(socket.getOutputStream());
				InputStreamReader input = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);
				BufferedReader response = new BufferedReader(input);) {

			HttpsFetcher.printGetRequest(request, url);

			Map<String, List<String>> headers = HttpsFetcher.getHeaderFields(response);

			int code = getStatusCode(headers);
			boolean isHtml = isHtml(headers);
			if (isHtml) {
				if (code == 200) {
					List<String> content = response.lines().toList();
					String joinedContent = String.join("\n", content);

					html = joinedContent;
				} else if (redirects > 0) { // redirect
					String redirect = getRedirect(headers);
					if (redirect != null) {
						html = fetch(redirect, --redirects); // redirect if redirects > 0
					}
				}
			}

			if (index != null && html != null && !html.isEmpty()) { // if add statistics (index not null) and valid html
				String length = getContentLength(headers);
				String time = dateFormatter.format(LocalDateTime.now());
				String title = getTitle(html);
				String[] statistics = { title, length, time };

				index.addHTMLStatistics(url.toString(), statistics);	//add statistics to index
			}
			
			return html;	// if not html, still null

		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Converts the {@link String} url into a {@link URL} object and then calls
	 * {@link #fetch(URL, int)}.
	 *
	 * @param url       the url to fetch
	 * @param redirects the number of times to follow redirects
	 * @return the html or {@code null} if unable to fetch the resource or the
	 *         resource is not html
	 *
	 * @see #fetch(URL, int)
	 */
	public static String fetch(String url, int redirects) {
		try {
			return fetch(new URL(url), redirects);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	/**
	 * Converts the {@link String} url into a {@link URL} object and then calls
	 * {@link #fetch(URL, int)} with 0 redirects.
	 *
	 * @param url the url to fetch
	 * @return the html or {@code null} if unable to fetch the resource or the
	 *         resource is not html
	 *
	 * @see #fetch(URL, int)
	 */
	public static String fetch(String url) {
		return fetch(url, 0);
	}

	/**
	 * Calls {@link #fetch(URL, int)} with 0 redirects.
	 *
	 * @param url the url to fetch
	 * @return the html or {@code null} if unable to fetch the resource or the
	 *         resource is not html
	 */
	public static String fetch(URL url) {
		return fetch(url, 0);
	}
}
