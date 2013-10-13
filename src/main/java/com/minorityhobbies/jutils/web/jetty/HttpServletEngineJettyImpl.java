package com.minorityhobbies.jutils.web.jetty;

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.http.HttpServlet;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.SessionManager;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.minorityhobbies.util.web.HttpServletContext;
import com.minorityhobbies.util.web.HttpServletEngine;

public class HttpServletEngineJettyImpl implements HttpServletEngine {
	static {
		String portNumber = System
				.getProperty("com.minorityhobbies.jutils.web.port");
		if (portNumber != null) {
			DEFAULT_PORT = Integer.parseInt(portNumber);
		}
		String root = System.getProperty("com.minorityhobbies.jutils.web.root");
		if (root != null) {
			ROOT = root;
		}
	}

	private static int DEFAULT_PORT = 9090;
	private static String ROOT;
	private Server server;
	private List<HttpServletContext> contexts;
	private final Map<String, Filter> rootFilters = new HashMap<String, Filter>();
	private int port = DEFAULT_PORT;
	private String root = ROOT;

	public HttpServletEngineJettyImpl() {
		super();
		contexts = new LinkedList<HttpServletContext>();
	}

	@Override
	public void run() {
		server = new Server();
		SelectChannelConnector connector = new SelectChannelConnector();
		connector.setPort(port);
		server.addConnector(connector);

		SessionManager manager = new HashSessionManager();

		List<Handler> handlers = new LinkedList<Handler>();
		for (HttpServletContext context : contexts) {
			ServletContextHandler contextHandler = new ServletContextHandler();
			contextHandler.setContextPath(context.getContextPath());
			for (Map.Entry<String, HttpServlet> servletEntry : context
					.getServlets().entrySet()) {
				contextHandler.addServlet(
						new ServletHolder(servletEntry.getValue()),
						servletEntry.getKey());
			}
			for (Map.Entry<String, Filter> filterEntry : context.getFilters()
					.entrySet()) {
				contextHandler.addFilter(
						new FilterHolder(filterEntry.getValue()),
						filterEntry.getKey(),
						EnumSet.allOf(DispatcherType.class));
			}
			SessionHandler sessionHandler = new SessionHandler(manager);
			sessionHandler.setHandler(contextHandler);
			handlers.add(sessionHandler);
		}

		ServletContextHandler htmlHandler = new ServletContextHandler();
		htmlHandler.setContextPath("/");
		for (Map.Entry<String, Filter> filterEntry : rootFilters.entrySet()) {
			htmlHandler.addFilter(new FilterHolder(filterEntry.getValue()),
					filterEntry.getKey(), EnumSet.allOf(DispatcherType.class));
		}
		ServletHolder htmlHolder = new ServletHolder(new DefaultServlet());
		htmlHolder.setInitParameter("resourceBase", root);
		htmlHolder.setInitParameter("dirAllowed", "false");
		htmlHandler.addServlet(htmlHolder, "/*");
		SessionHandler sessionHandler = new SessionHandler(manager);
		sessionHandler.setHandler(htmlHandler);
		handlers.add(sessionHandler);
		
		ContextHandlerCollection rootContextHandler = new ContextHandlerCollection();
		rootContextHandler.setHandlers(handlers.toArray(new Handler[handlers
				.size()]));

		server.setHandler(rootContextHandler);

		try {
			server.start();
			server.join();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() throws IOException {
		if (server != null) {
			try {
				server.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void addContext(HttpServletContext context) {
		contexts.add(context);
	}

	public void addRootFilter(Filter filter, String pathSpec) {
		rootFilters.put(pathSpec, filter);
	}

	@Override
	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public void setHtmlDocRoot(String root) {
		this.root = root;
	}
}
