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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.Iterator;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.eclipse.sensinact.gateway.protocol.http.Headers;
import org.eclipse.sensinact.gateway.util.IOUtils;


/**
 * A Request configuration data structure
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface ConnectionConfiguration<RESPONSE extends Response,
REQUEST extends Request<RESPONSE>> extends Headers
{	
	public static final String READ_TIMEOUT = "Read-timeout";
	public static final String GET = "GET";
	public static final String POST = "POST";
	public static final String PUT = "PUT";
	public static final String DELETE = "DELETE";
	public static final String HEAD = "HEAD";
	public static final String OPTIONS = "OPTIONS";
	public static final String CONNECT_TIMEOUT = "Connect-timeout";
	public static final String CONTENT_TYPE = "Content-type";
	public static final String ACCEPT = "Accept";
	public static final String HTTP_METHOD = "httpMethod";
	public static final String DEFAULT_CONTENT_TYPE = "text/html";
	public static final String DEFAULT_ACCEPT = "text/html";
	public static final String DEFAULT_HTTP_METHOD = "GET";
	
	public static final int DEFAULT_CONNECTION_TIMEOUT = 5000;
	public static final int DEFAULT_READ_TIMEOUT = 5000;
	
	//TODO: handle secured connection for true
	//****************************************
	//Thread safe lazy singleton pattern
	//****************************************
	final class SSLInitializer
	{
		public static final Boolean getSSLInitializer()
		{
			return SSLInitializerHolder.INITIALIZER;
		}
		
		private final static class SSLInitializerHolder
		{		
			public static final Boolean INITIALIZER = new Boolean(
			new BooleanProvider()
			{
				boolean init()
				{
					TrustManager[] TRUST_ALL_CERTS = new TrustManager[]
					{
						new X509TrustManager()
						{
							public java.security.cert.X509Certificate[] getAcceptedIssuers()  
							{ 
								return null; 
							}
							public void checkClientTrusted( 
									java.security.cert.X509Certificate[] certs, 
									String authType)  
							{}
							public void checkServerTrusted( 
									java.security.cert.X509Certificate[] certs, 
									String authType)  
							{}
						}
					};					
				    HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier()
				    {
				        @Override
				        public boolean verify(String hostname, SSLSession session) 
				        {
				            return true;
				        }
				    };
					try
					{
						SSLContext sc = SSLContext.getInstance( "SSL"); // "TLS" "SSL"
						sc.init( null, TRUST_ALL_CERTS, null);
						HttpsURLConnection.setDefaultSSLSocketFactory(
								sc.getSocketFactory());
						HttpsURLConnection.setDefaultHostnameVerifier(
								DO_NOT_VERIFY);
					}
					catch (Exception e) 
					{
						e.printStackTrace();
						return false;
					}
					return true;
				}
			}.init());
		}
	}	

	abstract class BooleanProvider
	{
		abstract boolean init();
	}
	
	final class HttpURLConnectionBuilder
	{
		/**
		 * Returns an appropriate {@link HttpURLConnection} according
		 * to the protocol of the {@link URL} passed as parameter
		 * 
		 * @param url
		 * 		the {@link URL} for which to return an HttpURLConnection
		 * @return
		 * 		an appropriate {@link HttpURLConnection} according
		 * 		to the specified  {@link URL}'s protocol
		 */
		public static <RESPONSE extends Response, REQUEST extends Request<RESPONSE>> 
		HttpURLConnection build(ConnectionConfiguration<RESPONSE,REQUEST> config) 
						throws IOException
		{
			HttpURLConnection connection = null;
			String uri = config.getUri();
			if(uri == null || uri.length() == 0)
			{
				return null;
			}
			URL url = new URL(uri);
			Proxy proxy = config.getProxy();
			
		    if (!url.getProtocol().toLowerCase().equals("https") 
		    		|| SSLInitializer.getSSLInitializer())
		    {
		        connection = (HttpURLConnection)
		        		url.openConnection(proxy);
		    }
		    connection.setConnectTimeout(config.getConnectTimeout());
			connection.setReadTimeout(config.getReadTimeout());
			connection.setDoInput(true);

			Object content = config.getContent();
			boolean doOutput  = content!= null;
			
			connection.setDoOutput(doOutput);
			connection.setRequestMethod(config.getHttpMethod());
			
			String contentType = null;
			if((contentType =  config.getContentType())!=null)
			{
				connection.setRequestProperty("Content-type",contentType);
			}
			String acceptType = null;
			if((acceptType =  config.getAccept())!=null)
			{
				connection.setRequestProperty("Accept",acceptType);
			}	
			String data = doOutput?String.valueOf(content):null;			
			Iterator<String> iterator = config.iterator();
			
			while(iterator.hasNext())
			{
				String header = iterator.next();			
				connection.setRequestProperty(header, 
						config.getHeaderAsString(header));
			}
			//define by default ?
			//connection.setRequestProperty("Connection", "close");
			connection.connect();
			
			if(data != null)
			{
				IOUtils.write(data.getBytes(), 
					connection.getOutputStream());
			}
		    return connection;
		}		
	}
	
	
	/**
	 * Adds a query parameter whose key and value are passed as 
	 * parameters
	 * 
	 * @param key the query parameter key
	 * @param value the query parameter value
	 */
	ConnectionConfiguration<RESPONSE,REQUEST> queryParameter(String key, String value);
	
	/**
	 * Defines the string uri targeted by requests build from {@link 
	 * HttpDiscoveryTask}s configured by this HttpTaskConfiguration
	 * 
	 *  @param uri
	 *  	the targeted string uri by requests
	 */
	ConnectionConfiguration<RESPONSE,REQUEST> setUri(String uri);

	/**
	 * Defines the string uri targeted by requests build from {@link 
	 * HttpDiscoveryTask}s configured by this HttpTaskConfiguration
	 * 
	 *  @param uri
	 *  	the targeted string uri by requests
	 */
	String getUri();
	
	/**
	 * Defines the content object of requests build from {@link 
	 * HttpDiscoveryTask}s configured by this HttpTaskConfiguration
	 * 
	 *  @param content
	 *  	the content object of requests
	 */
	ConnectionConfiguration<RESPONSE,REQUEST> setContent(Object content);
	
	/**
	 * Returns the content object of requests build from {@link 
	 * HttpDiscoveryTask}s configured by this HttpTaskConfiguration
	 * 
	 *  @return
	 *  	the content object of requests
	 */
    Object getContent();

	/**
	 * Defines the provided mime type of requests build from {@link 
	 * HttpDiscoveryTask}s configured by this HttpTaskConfiguration
	 * 
	 * @param acceptType
	 * 		the handled mime type of requests
	 */
    ConnectionConfiguration<RESPONSE,REQUEST> setAccept(String acceptType);

	/**
	 * Returns the handled mime type of requests build from {@link 
	 * HttpDiscoveryTask}s configured by this HttpTaskConfiguration
	 * 
	 * @return
	 * 		the handled mime type of requests
	 */
    String getAccept();
    
	/**
	 * Defines the provided mime type of requests build from {@link 
	 * HttpDiscoveryTask}s configured by this HttpTaskConfiguration
	 * 
	 * @param contentType
	 * 		the provided mime type of requests
	 */
    ConnectionConfiguration<RESPONSE,REQUEST> setContentType(String contentType);

	/**
	 * Returns the provided mime type of requests build from {@link 
	 * HttpDiscoveryTask}s configured by this HttpTaskConfiguration
	 * 
	 * @return
	 * 		the provided mime type of requests
	 */
    String getContentType();

   	/**
   	 * Returns HTTP method to be used by requests build from {@link 
   	 * HttpDiscoveryTask}s configured by this HttpTaskConfiguration
   	 * 
   	 * @return 
   	 * 		the HTTP method to be used by requests
   	 */
    ConnectionConfiguration<RESPONSE,REQUEST> setHttpMethod(String httpMethod);
   	
   	/**
   	 * Returns HTTP method to be used by requests build from {@link 
   	 * HttpDiscoveryTask}s configured by this HttpTaskConfiguration
   	 * 
   	 * @return 
   	 * 		the HTTP method to be used by requests
   	 */
   	String getHttpMethod();

	/**
	 * Defines the timeout value, in milliseconds, to be used when opening a 
	 * communications link. If the timeout expires before the connection can 
	 * be established, a java.net.SocketTimeoutException is raised.A timeout 
	 * of zero is interpreted as an infinite timeout
	 * 
	 * @param connectTimeout
	 * 		the connection timeout value to set
	 */
   	ConnectionConfiguration<RESPONSE,REQUEST> setConnectTimeout(int connectTimeout);
    
	/**
	 * Returns a timeout value, in milliseconds, to be used when opening a 
	 * communications link. If the timeout expires before the connection can 
	 * be established, a java.net.SocketTimeoutException is raised.A timeout 
	 * of zero is interpreted as an infinite timeout
	 * 
	 * @return
	 * 		the connection timeout
	 */
    int getConnectTimeout();
    
    /**
	 * Defines the read timeout in milliseconds. A non-zero value specifies 
	 * the timeout when reading from Input stream when a connection is established 
	 * to a resource. If the timeout expires before there is data available for 
	 * read, a java.net.SocketTimeoutException is raised. A timeout of zero is 
	 * interpreted as an infinite timeout.
	 * 
	 * @param readTimeout
	 * 		the read timeout in milliseconds
	 */
    ConnectionConfiguration<RESPONSE,REQUEST> setReadTimeout(int readTimeout);
    
    /**
	 * Returns the read timeout in milliseconds. A non-zero value specifies 
	 * the timeout when reading from Input stream when a connection is established 
	 * to a resource. If the timeout expires before there is data available for 
	 * read, a java.net.SocketTimeoutException is raised. A timeout of zero is 
	 * interpreted as an infinite timeout.
	 * 
	 * @return
	 * 		the read timeout in milliseconds
	 */
    int getReadTimeout();
    
    /**
	 * Define the proxy host of the proxy used by the Requests
	 * configured by this RequestConfiguration. The host string
	 * authorized formats are the ones allowed by the InetAddress
	 * constructor 
	 * 
	 * @proxyHost
	 * 		the proxy host of the proxy used by the Requests based 
	 * 		on this RequestConfiguration
	 */
    ConnectionConfiguration<RESPONSE,REQUEST> setProxyHost(String proxyHost);

	/**
	 * Define the proxy port of the proxy used by the Requests
	 * configured by this RequestConfiguration.
	 * 
	 * @proxyPort
	 * 		the proxy port of the proxy used by the Requests
	 * 		based on this RequestConfiguration
	 */
    ConnectionConfiguration<RESPONSE,REQUEST> setProxyPort(int proxyPort);
	
	/**
	 * Returns a new {@link Proxy} instance using
	 * the retrieved proxy's host and port 
	 * configuration
	 * 
	 * @return
	 * 		a new {@link Proxy} instance 
	 */
	Proxy getProxy();
	
	/**
	 * Creates and returns a new {@link HttpURLConnection} based
	 * on this RequestConfiguration
	 * 
	 * @return
	 * 		a new {@link AbstractRequest} based on this 
	 * 		RequestConfiguration
	 * @throws IOException 
	 */
	HttpURLConnection connect() throws IOException;
}
