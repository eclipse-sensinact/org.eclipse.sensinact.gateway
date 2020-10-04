/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.nthbnd.rest.server;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.AsyncContext;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.ExecutorThreadPool;
import org.eclipse.sensinact.gateway.util.IOUtils;

public class JettyTestServer implements Runnable {

    private final Server server;
    private ServerConnector connector ;
	private int port;
    private String message;
	private boolean available;

    public JettyTestServer(int port) throws Exception {

    	this.port = port;      
        this.server = new Server(new ExecutorThreadPool(10));
    }

    public boolean isStarted() {
        return this.running;
    }

    public void start() throws Exception {  
        ServletHolder holder = new ServletHolder(new JettyTestServerCallbackServlet());
        holder.setName("callbackServlet");
        
        ServletHandler handler = new ServletHandler();
        handler.addServletWithMapping(holder, "/");
        
        this.server.setHandler(handler);
        connector = new ServerConnector(server);
        connector.setPort(port);
        connector.addConnectionFactory(new ConnectionFactory() {

			@Override
			public String getProtocol() {
				return "HTTP";
			}

			@Override
			public Connection newConnection(Connector connector, EndPoint endpoint) {
				return endpoint.getConnection();
			}

			@Override
			public List<String> getProtocols() {
				return Arrays.asList("HTTP");
			}
        	
        });
        server.setConnectors(new Connector[] { connector });
        this.server.start();
    	
    }

    public void stop() throws Exception {
    	if(this.connector != null) {
    		this.connector.stop();
    	}
    	this.server.stop();
    }

    public void dump() throws Exception {
        this.server.dumpStdErr();
    }
    
    public void join() throws Exception {
    	if(this.connector != null) {
        	connector.join();
    	}
        Thread.sleep(2000);
    }

    @SuppressWarnings("serial")
    public class JettyTestServerCallbackServlet extends HttpServlet {

		/**
         * @throws IOException
         * @inheritDoc
         * @see javax.servlet.http.HttpServlet#
         * service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
         */
    	@Override
        public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        	System.out.println("GETTING ...");
    		if (response.isCommitted()) {
                return;
            }
            final AsyncContext asyncContext;
            if (request.isAsyncStarted()) {
                asyncContext = request.getAsyncContext();

            } else {
                asyncContext = request.startAsync(request, response);
            }
            response.getOutputStream().setWriteListener(new WriteListener() {
                @Override
                public void onWritePossible() throws IOException {
                    HttpServletRequest request = (HttpServletRequest) asyncContext.getRequest();
                    HttpServletResponse response = (HttpServletResponse) asyncContext.getResponse();
                     try {
                    	JettyTestServer.this.message = null;
             	        JettyTestServer.this.setAvailable(true);
             	        response.setStatus(200);

                    }catch (Exception e) {
                        e.printStackTrace();
                        response.sendError(520, "Internal server error");

                    } finally {                        
                        if (request.isAsyncStarted()) {
                            asyncContext.complete();
                        }
                    }
                }

                @Override
                public void onError(Throwable t) {
                   t.printStackTrace();;
                }

            });
        }

        /**
         * @throws IOException
         * @inheritDoc
         * @see javax.servlet.http.HttpServlet#
         * service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
         */
        @Override
        public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        	if (response.isCommitted()) {
                return;
            }
            final AsyncContext asyncContext;
            if (request.isAsyncStarted()) {
                asyncContext = request.getAsyncContext();

            } else {
                asyncContext = request.startAsync(request, response);
            }
            //System.out.println(request);
            response.getOutputStream().setWriteListener(new WriteListener() {
                @Override
                public void onWritePossible() throws IOException {
                    HttpServletRequest request = (HttpServletRequest) asyncContext.getRequest();
                    HttpServletResponse response = (HttpServletResponse) asyncContext.getResponse();
                    try {
                    	JettyTestServer.this.message = null;
             	        int length = request.getContentLength();
             	        if (length > -1) {
             	            byte[] content = IOUtils.read(request.getInputStream(), length, false);
             	            JettyTestServer.this.message = new String(content);
             	        } else {
             	            byte[] content = IOUtils.read(request.getInputStream(), false);
             	            JettyTestServer.this.message = new String(content);
             	        }
             	        JettyTestServer.this.setAvailable(true);
             	        response.setStatus(200);

                    }catch (Exception e) {
                        e.printStackTrace();
                        response.sendError(520, "Internal server error");

                    } finally {                        
                        if (request.isAsyncStarted()) {
                            asyncContext.complete();
                        }
                    	
                    }
                }

                @Override
                public void onError(Throwable t) {
                   t.printStackTrace();;
                }

            });
        }
    }

	public boolean isAvailable() {
		return this.available;
	}
	
	public void setAvailable(boolean available) {
		this.available = available;
	}

    public String getResponseMessage() {
        return this.message;
    }
    
    private boolean running = false;

    @Override
    public void run() {
        running = true;
        try {
            this.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        running = false;

    }
}
