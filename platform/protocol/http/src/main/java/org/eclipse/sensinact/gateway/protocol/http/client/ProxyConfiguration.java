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
package org.eclipse.sensinact.gateway.protocol.http.client;

import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * Configuration service of a {@link Proxy}
 */
public final class ProxyConfiguration
{
	private static final int DEFAULT_HTTP_PORT = 80;
	
	private String proxyHost;
	private int proxyPort;
	
	/**
	 * Constructor
	 */
	public ProxyConfiguration()
	{
		String httpProxy;
		String httpPort;
		int port;
		
		httpProxy = System.getProperty("org.eclipse.sensinact.gateway.http.proxyHost");
		httpPort = System.getProperty("org.eclipse.sensinact.gateway.http.proxyPort");
		
		if(httpPort != null)
		{
			try
			{
				port = Integer.parseInt(httpPort);
				
			} catch(NumberFormatException e)
			{
				port = DEFAULT_HTTP_PORT;
			}
		} else
		{
			port = DEFAULT_HTTP_PORT;
		}
		this.proxyHost = httpProxy;
		this.proxyPort = port;
	}
	
	/**
	 * Define the proxy host of the proxies configured
	 * by this ProxyConfiguration. The host string
	 * authorized formats are the ones allowed by the
	 * InetAddress constructor 
	 * 
	 * @param proxyHost
	 * 		the proxy host of the proxies based on 
	 * 		this ProxyConfiguration
	 */
	public void setProxyHost(String proxyHost)
	{
		this.proxyHost = proxyHost;
	}

	/**
	 * Define the proxy port of the proxies configured
	 * by this ProxyConfiguration.
	 * 
	 * @param proxyPort
	 * 		the proxy port of the proxies based on 
	 * 		this ProxyConfiguration
	 */
	public void setProxyPort(int proxyPort)
	{
		this.proxyPort = proxyPort;
	}
	
	/**
	 * Returns a new {@link Proxy} instance using
	 * the retrieved proxy's host and port 
	 * configuration
	 * 
	 * @return
	 * 		a new {@link Proxy} instance 
	 */
	public Proxy getProxy()
	{
		if(proxyHost!=null)
		{
			 return new Proxy(Proxy.Type.HTTP, 
				new InetSocketAddress(
				proxyHost, proxyPort));
		}
		return Proxy.NO_PROXY;
	}
}