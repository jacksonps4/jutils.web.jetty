package com.minorityhobbies.jutils.web.jetty;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;

import com.minorityhobbies.util.web.HttpRequestHandler;
import com.minorityhobbies.util.web.HttpServer;

public class HttpServerJettyImpl implements HttpServer {
	static {
		String portNumber = System.getProperty("com.minorityhobbies.jutils.web.port");
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
	private int port = DEFAULT_PORT;
	private String root = ROOT;
	private final List<HttpRequestHandler> handlers = new LinkedList<HttpRequestHandler>();
	
	public HttpServerJettyImpl() {
		super();
	}

	public HttpServerJettyImpl(int port, String root) {
		super();
		this.port = port;
		this.root = root;
	}

	public void run() {
		server = new Server();
		SelectChannelConnector connector = new SelectChannelConnector();
		connector.setPort(port);
		server.addConnector(connector);

		ResourceHandler resourceHandler = new ResourceHandler();
		resourceHandler.setDirectoriesListed(true);
		resourceHandler.setWelcomeFiles(new String[] { "index.html", "index.html" });
		resourceHandler.setResourceBase(root);

		HandlerList handlers = new HandlerList();
		handlers.addHandler(new SessionHandler(new HashSessionManager()));
		for (Handler handler : wrapHandlers()) {
			handlers.addHandler(handler);
		}
		handlers.addHandler(resourceHandler);
		handlers.addHandler(new DefaultHandler());

		server.setHandler(handlers);

		try {
			server.start();
			server.join();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	private List<Handler> wrapHandlers() {
		List<Handler> handlers = new LinkedList<Handler>();
		for (final HttpRequestHandler h : this.handlers) {
			handlers.add(new AbstractHandler() {
				@Override
				public void handle(String target, Request baseRequest,
						HttpServletRequest request, HttpServletResponse response)
						throws IOException, ServletException {
					if (h.serviceRequest(request, response)) {
						baseRequest.setHandled(true);
						return;
					}
				}
			});
		}
		return handlers;
	}

	@Override
	public void close() throws IOException {
		try {
			server.stop();
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	@Override
	public void addRequestHandler(HttpRequestHandler handler) {
		handlers.add(handler);
	}

	@Override
	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public void setRootPath(String root) {
		this.root = root;
	}
}
