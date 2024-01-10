package edu.usfca.cs272;

import java.nio.file.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Class for managing web server for inverted index searching
 * 
 * @author Athene Marston
 *
 */
public class WebServer {
	
	/**
	 * The logger to use (Jetty is configured via the pom.xml to use Log4j2 as well)
	 */
	public static Logger log = LogManager.getLogger();
	
	/**
	 * Server port
	 */
	private int port;
	
	
	/**
	 * Constructor for web server
	 * 
	 * @param port to hold server on
	 */
	public WebServer(int port) {
		this.port = port;
	}
	
	/**
	 * Starts up a web server with the given index
	 * 
	 * @param index ThreadSafeInverted index to use for search
	 * @throws Exception if exception occurred in server creation
	 */
	public void initialize(ThreadSafeInvertedIndex index) throws Exception {
		Server server = new Server(port);
		System.out.println("initilized at port " + port);

		// context to serve JS
		ResourceHandler resourceHander = new ResourceHandler();
		resourceHander.setDirectoriesListed(false);
		resourceHander.setResourceBase(Path.of("src", "main", "resources", "static resources").toString()); // link to JS file
																											

		ContextHandler resourceContext = new ContextHandler("/files");
		resourceContext.setHandler(resourceHander);

		// context for servlets
		ServletContextHandler servletContext = new ServletContextHandler();
		servletContext.setContextPath("/");
		WebServlet mainServlet = new WebServlet(index, port);
		servletContext.addServlet(new ServletHolder(mainServlet), "/");
		servletContext.addServlet(new ServletHolder(mainServlet.new DownloadServlet()), "/download");
		servletContext.addServlet(new ServletHolder(mainServlet.new LocationsServlet()), "/locations");
		servletContext.addServlet(new ServletHolder(mainServlet.new IndexServlet()), "/index");

		HandlerList handlers = new HandlerList();
		handlers.addHandler(resourceContext);
		handlers.addHandler(servletContext);

		server.setHandler(handlers);

		server.start();
		server.join();
	}
}
