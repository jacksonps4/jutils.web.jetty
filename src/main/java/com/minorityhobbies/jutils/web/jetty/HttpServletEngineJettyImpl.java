package com.minorityhobbies.jutils.web.jetty;

import java.io.IOException;

import javax.servlet.http.HttpServlet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

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
	private ServletContextHandler servletContextHandler;
	private int port = DEFAULT_PORT;
	private String root = ROOT;

	public HttpServletEngineJettyImpl() {
		super();
		servletContextHandler = new ServletContextHandler();
		servletContextHandler.setContextPath("/");
		servletContextHandler.setResourceBase(root);
	}

	@Override
	public void run() {
		server = new Server(port);
		server.setHandler(servletContextHandler);
		
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
	public void addServlet(HttpServlet servlet, String pathSpec) {
		ServletHolder holder = new ServletHolder(servlet);
		servletContextHandler.addServlet(holder, pathSpec);
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
