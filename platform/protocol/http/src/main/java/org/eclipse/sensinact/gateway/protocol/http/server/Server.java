/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.protocol.http.server;


import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import org.eclipse.sensinact.gateway.util.IOUtils;

/**
 * Simple HTTP Server
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@SuppressWarnings("restriction")
public class Server
{
	private static Logger LOG = Logger.getLogger(Server.class.getName());
	
	private static final String ROOT_CONTEXT = "/";	
	private static final int DEFAULT_PORT = 8789;

	private final HttpServer server;
	
	private final AtomicBoolean running;
	
	private int handled;
	private int binded;
	
	private RequestHandler requestHandler;

	public Server(RequestHandler requestHandler) 
		throws IOException
	{
		this.requestHandler = requestHandler;
		this.server =  HttpServer.create();
		this.server.setExecutor(null);
		
		this.running = new AtomicBoolean(false);
		this.handled = 0;
		this.binded = 0;
	}

	/**
	 * Defines the running state of the HTTP
	 * server
	 * 
	 * @param running
	 *      the running state of the HTTP
	 * 		server
	 */
	private void running(boolean running) 
	{
		synchronized (this)
		{
			this.running.set(running);
		}
	}

	/**
	 * Returns the running state of the 
	 * HTTP server
	 * 
	 * @return 
	 * 		the running state of the 
	 * 		HTTP server
	 */
	boolean running()
	{
		boolean running = false;
		synchronized (this)
		{
			running = this.running.get();
		}
		return running;
	}
	
	/**
	 * Attaches a new HttpHandler to the root HttpContext
	 */
	protected void handle()
	{
		this.handle(null);
	}

	/**
	 * Attaches a new HttpHandler to the HttpContext whose
	 * path is passed as parameter
	 * 
	 * @param path
	 * 		the path of the HttpContext for which to attach
	 * 		an new HttpHandler
	 */
	protected void handle(String path)
	{
		if(path == null)
		{
			path = ROOT_CONTEXT;
		}	
		final HttpContext context = this.server.createContext(path);
		context.setHandler(new HttpHandler()
		{
			/**
			 * @inheritDoc
			 *
			 * @see com.sun.net.httpserver.HttpHandler#
			 * handle(com.sun.net.httpserver.HttpExchange)
			 */
			@Override
			public void handle(HttpExchange httpExchange) 
					throws IOException 
			{
				int responseCode = (!Server.this.running()
						?HttpURLConnection.HTTP_NOT_FOUND
						:HttpURLConnection.HTTP_ACCEPTED);
				
				Headers headers = httpExchange.getRequestHeaders();				
				URI uri = httpExchange.getRequestURI();
				String method = httpExchange.getRequestMethod();
				Map<String,Object> attributes = httpExchange.getHttpContext(
						).getAttributes();
				
				HttpRequestContent httpRequestContent =
						new HttpRequestContent(uri, method, headers);
				
				int length = -1;				
				try
				{
					length = Integer.parseInt(
						httpExchange.getResponseHeaders().getFirst(
							"Content-Length"));
					
				}catch(Exception e)
				{}					
				byte[] content = length  > 0?IOUtils.read(
					httpExchange.getRequestBody(),length, false)
					:IOUtils.read(httpExchange.getRequestBody(), false);
				httpRequestContent.setContent(content);		
				
				ResponseContent response = Server.this.requestHandler.handle(
						httpRequestContent);
				int code = responseCode;
				length = response.getContent().length;
				try
				{
					httpExchange.sendResponseHeaders(code, length);
					httpExchange.getResponseHeaders().putAll(response.getHeaders());
					httpExchange.getResponseBody().write(response.getContent());
					httpExchange.getResponseBody().flush();
					httpExchange.getResponseBody().close();
					
				} catch(IOException e)
				{
					code = HttpURLConnection.HTTP_INTERNAL_ERROR;
					if(LOG.isLoggable(Level.SEVERE))
					{
						LOG.log(Level.SEVERE, e.getMessage(), e);
					}
				}
			}
		});
		this.handled++;
	}
	
	/**
	 * Starts this CallbackServer
	 * 
	 * @throws IOException
	 */
	public void start() throws IOException
	{
		if(handled == 0)
		{
			this.handle();
		}
		if(binded == 0)
		{
			this.bind();
		}
		this.running(true);
		this.server.start();
	}

	/**
	 * @param clientURL
	 * @throws IOException
	 */
	protected void start(String clientURL) throws IOException 
	{
		this.bind(new URL(clientURL));
		this.start();
	}

	/**
	 * @param ipAddress
	 * @param port
	 * @throws IOException
	 */
	protected void start(String ipAddress, int port) throws IOException
	{
		this.bind(ipAddress, port);
		this.start();
	}

	/**
	 * @throws IOException
	 */
	protected void bind() throws IOException
	{
		Enumeration<NetworkInterface> networkInterfaces = 
				NetworkInterface.getNetworkInterfaces();
		
		while(networkInterfaces.hasMoreElements())
		{
			NetworkInterface networkInterface = networkInterfaces.nextElement();
			if(networkInterface.isLoopback())
			{
				continue;
			}
			Enumeration<InetAddress> inetAddresses =
					networkInterface.getInetAddresses();

			while(inetAddresses.hasMoreElements())
			{
				InetAddress inetAddress = inetAddresses.nextElement();
				this.bind(inetAddress.getHostAddress(), 
						DEFAULT_PORT);
			}
		}
	}
	
	/**
	 * @param url
	 * @throws IOException
	 */
	public void bind(URL url) throws IOException
	{
		InetAddress remote = InetAddress.getByName(url.getHost());

		Enumeration<NetworkInterface> networkInterfaces = 
				NetworkInterface.getNetworkInterfaces();
		
		while(networkInterfaces.hasMoreElements())
		{
			NetworkInterface networkInterface = networkInterfaces.nextElement();
			if(!remote.isReachable(networkInterface, 0, 1000))
			{
				continue;
			}
			Enumeration<InetAddress> inetAddresses =
					networkInterface.getInetAddresses();

			while(inetAddresses.hasMoreElements())
			{
				InetAddress inetAddress = inetAddresses.nextElement();
				this.bind(inetAddress.getHostAddress(), 
						DEFAULT_PORT);
			}
		}
	}

	/**
	 * @param ipAddress
	 * @param port
	 * @throws IOException
	 */
	public void bind(String ipAddress, int port) throws IOException
	{
		this.server.bind(new InetSocketAddress(ipAddress,port), 0);
		this.binded++;

		if(LOG.isLoggable(Level.INFO))
		{
			LOG.log(Level.INFO, new StringBuilder(
			).append("server binded on "
			).append(ipAddress).append(" / port "
			).append(port).toString());
		}
	}

	/**
	 * Stops this CallbackServer
	 */
	public void stop()
	{
		this.running(false);
		this.server.stop(0);
	}
}
