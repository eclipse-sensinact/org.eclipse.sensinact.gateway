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
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.eclipse.sensinact.gateway.protocol.http.Headers;
import org.eclipse.sensinact.gateway.util.IOUtils;

/**
 * A Request configuration data structure
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface ConnectionConfiguration<RESPONSE extends Response, REQUEST extends Request<RESPONSE>> extends Headers {
    
	static final Logger LOG = Logger.getLogger(ConnectionConfiguration.class.getName());
	
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
    
    public final String TRUST_ALL = "TRUST_ALL";
    
    final static Map<String,TrustManager[]> TRUST_MANAGERS = new HashMap<>();
    final static Map<String, KeyManager[]> KEY_MANAGERS = new HashMap<>();  
    
    abstract class BooleanProvider {
        abstract boolean init();
    }

    final class HttpURLConnectionBuilder {
        /**
         * Returns an appropriate {@link HttpURLConnection} according
         * to the protocol of the {@link URL} passed as parameter
         *
         * @param url the {@link URL} for which to return an HttpURLConnection
         * @return an appropriate {@link HttpURLConnection} according
         * to the specified  {@link URL}'s protocol
         */
        public static <RESPONSE extends Response, REQUEST extends Request<RESPONSE>> HttpURLConnection build(ConnectionConfiguration<RESPONSE, REQUEST> config) throws IOException {
            HttpURLConnection connection = null;            
            String uri = config.getUri();
            if (uri == null || uri.length() == 0) {
                return null;
            }
            URL url = new URL(uri);
            Proxy proxy = config.getProxy();

            if (url.getProtocol().toLowerCase().equals("https")) {
                connection = (HttpsURLConnection) url.openConnection(proxy);
                String host = url.getHost();
                
            	TrustManager[] trusteds = TRUST_MANAGERS.get(host);
            	KeyManager[] keys = KEY_MANAGERS.get(host);
            	
                if(trusteds == null) {
                	String serverCertificateStr = config.getServerSSLCertificate();
                	URL serverCertificate = null;
                	if(!TRUST_ALL.equals(serverCertificateStr)) {
                		try {
                			serverCertificate = new URL(config.getServerSSLCertificate());
                		} catch(NullPointerException|IOException e) {
                			LOG.log(Level.CONFIG, e.getMessage());
                		}
                	}
	            	if(serverCertificate != null) {
	            		try {
	            			InputStream is = serverCertificate.openStream();
	               		 	CertificateFactory cf = CertificateFactory.getInstance("X.509");
	               		 	X509Certificate cert = (X509Certificate)cf.generateCertificate(is);
	               		 	TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
	               		 	KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
	               		 	ks.load(null);
	               		 	ks.setCertificateEntry("caCert", cert);              		 
	               		 	tmf.init(ks);                		 
	               		 	trusteds = tmf.getTrustManagers();
	               		    TRUST_MANAGERS.put(host,trusteds);	               		    
	            		} catch(Exception e) {
	            			trusteds = null;
	            		}
	                	if(trusteds == null && TRUST_ALL.equals(serverCertificateStr)) {
	                       trusteds = new TrustManager[]{new X509TrustManager() {
	                            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
	                                return null;
	                            }

	                            public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
	                            }

	                            public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
	                            }
	                        }};
	                	}
	            	}
                }
                if(keys == null) {
                    URL clientCertificate = null;
            		try {
            			clientCertificate = new URL(config.getClientSSLCertificate());
            		} catch(NullPointerException|IOException e) {
            			LOG.log(Level.CONFIG, e.getMessage());
            		}                    
                	if(clientCertificate != null) {
                		try {
                		 InputStream is = clientCertificate.openStream();                		 
                		 KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                		 KeyStore ks = KeyStore.getInstance("PKCS12");
                		 ks.load(is,new char[] {});              		 
                		 kmf.init(ks, new char[] {});
                		 keys = kmf.getKeyManagers();
                		 KEY_MANAGERS.put(host,keys);
                		} catch(NoSuchAlgorithmException | CertificateException | KeyStoreException | IOException | UnrecoverableKeyException e) {
                			LOG.log(Level.SEVERE,e.getMessage(), e);
                        	keys = null;
                		}                		  
                	}
                }                
                try {
                    SSLContext sc = SSLContext.getInstance("TLS"); // "TLS" "SSL"
                    sc.init(keys, trusteds, null);
                    ((HttpsURLConnection)connection).setSSLSocketFactory(sc.getSocketFactory());
                } catch (Exception e) {
                	LOG.log(Level.SEVERE,e.getMessage(), e);
                    return null;
                }                
            } else {
            	connection = (HttpURLConnection) url.openConnection(proxy);
            }
            connection.setConnectTimeout(config.getConnectTimeout());
            connection.setReadTimeout(config.getReadTimeout());
            connection.setDoInput(true);
            Object content = config.getContent();
            boolean doOutput = content != null;

            connection.setDoOutput(doOutput);
            connection.setRequestMethod(config.getHttpMethod());

            String contentType = null;
            if ((contentType = config.getContentType()) != null) {
                connection.setRequestProperty("Content-type", contentType);
            }
            String acceptType = null;
            if ((acceptType = config.getAccept()) != null) {
                connection.setRequestProperty("Accept", acceptType);
            }
            String data = doOutput ? String.valueOf(content) : null;
            Iterator<String> iterator = config.iterator();

            while (iterator.hasNext()) {
                String header = iterator.next();
                connection.setRequestProperty(header, config.getHeaderAsString(header));
            }
            connection.connect();

            if (data != null) {
                IOUtils.write(data.getBytes(), connection.getOutputStream());
            }
            return connection;
        }
    }

    /**
     * Adds a query parameter whose key and value are passed as
     * parameters
     *
     * @param key   the query parameter key
     * @param value the query parameter value
     */
    ConnectionConfiguration<RESPONSE, REQUEST> queryParameter(String key, String value);

    /**
     * Returns the String path to the server certificate to be used for TLS configuration.
     * A TRUST_ALL constant return means that all server certificates will be considered as 
     * valid
     * 
     * @return the String path to the server certificate or TRUST_ALL constant value to 
     * configure TLS connection
     */
    String getServerSSLCertificate();
    
    /**
     * Defines the String path to the server certificate to be used for TLS configuration.
     * If set to TRUST_ALL constant, all server certificates will be considered as 
     * valid
     * 
     * @param serverCertificate the String path to the server certificate to configure TLS connection
     */
    void setServerSSLCertificate(String serverCertificate);

    /**
     * Returns the String path to the client certificate to be used for TLS configuration and 
     * validation by the remote connected server.
     * 
     * @return the String path to the client certificate to configure TLS connection if any
     */
	String getClientSSLCertificate();

    /**
     * Defines the String path to the client certificate to be used for TLS configuration
     * 
     * @param clientCertificate the String path to the client certificate to configure TLS connection
     */
    void setClientSSLCertificate(String clientCertificate);

	/**
     * Defines the string uri targeted by requests build from {@link
     * HttpDiscoveryTask}s configured by this HttpTaskConfiguration
     *
     * @param uri the targeted string uri by requests
     */
    ConnectionConfiguration<RESPONSE, REQUEST> setUri(String uri);

    /**
     * Defines the string uri targeted by requests build from {@link
     * HttpDiscoveryTask}s configured by this HttpTaskConfiguration
     *
     * @param uri the targeted string uri by requests
     */
    String getUri();

    /**
     * Defines the content object of requests build from {@link
     * HttpDiscoveryTask}s configured by this HttpTaskConfiguration
     *
     * @param content the content object of requests
     */
    ConnectionConfiguration<RESPONSE, REQUEST> setContent(Object content);

    /**
     * Returns the content object of requests build from {@link
     * HttpDiscoveryTask}s configured by this HttpTaskConfiguration
     *
     * @return the content object of requests
     */
    Object getContent();

    /**
     * Defines the provided mime type of requests build from {@link
     * HttpDiscoveryTask}s configured by this HttpTaskConfiguration
     *
     * @param acceptType the handled mime type of requests
     */
    ConnectionConfiguration<RESPONSE, REQUEST> setAccept(String acceptType);

    /**
     * Returns the handled mime type of requests build from {@link
     * HttpDiscoveryTask}s configured by this HttpTaskConfiguration
     *
     * @return the handled mime type of requests
     */
    String getAccept();

    /**
     * Defines the provided mime type of requests build from {@link
     * HttpDiscoveryTask}s configured by this HttpTaskConfiguration
     *
     * @param contentType the provided mime type of requests
     */
    ConnectionConfiguration<RESPONSE, REQUEST> setContentType(String contentType);

    /**
     * Returns the provided mime type of requests build from {@link
     * HttpDiscoveryTask}s configured by this HttpTaskConfiguration
     *
     * @return the provided mime type of requests
     */
    String getContentType();

    /**
     * Returns HTTP method to be used by requests build from {@link
     * HttpDiscoveryTask}s configured by this HttpTaskConfiguration
     *
     * @return the HTTP method to be used by requests
     */
    ConnectionConfiguration<RESPONSE, REQUEST> setHttpMethod(String httpMethod);

    /**
     * Returns HTTP method to be used by requests build from {@link
     * HttpDiscoveryTask}s configured by this HttpTaskConfiguration
     *
     * @return the HTTP method to be used by requests
     */
    String getHttpMethod();

    /**
     * Defines the timeout value, in milliseconds, to be used when opening a
     * communications link. If the timeout expires before the connection can
     * be established, a java.net.SocketTimeoutException is raised.A timeout
     * of zero is interpreted as an infinite timeout
     *
     * @param connectTimeout the connection timeout value to set
     */
    ConnectionConfiguration<RESPONSE, REQUEST> setConnectTimeout(int connectTimeout);

    /**
     * Returns a timeout value, in milliseconds, to be used when opening a
     * communications link. If the timeout expires before the connection can
     * be established, a java.net.SocketTimeoutException is raised.A timeout
     * of zero is interpreted as an infinite timeout
     *
     * @return the connection timeout
     */
    int getConnectTimeout();

    /**
     * Defines the read timeout in milliseconds. A non-zero value specifies
     * the timeout when reading from Input stream when a connection is established
     * to a resource. If the timeout expires before there is data available for
     * read, a java.net.SocketTimeoutException is raised. A timeout of zero is
     * interpreted as an infinite timeout.
     *
     * @param readTimeout the read timeout in milliseconds
     */
    ConnectionConfiguration<RESPONSE, REQUEST> setReadTimeout(int readTimeout);

    /**
     * Returns the read timeout in milliseconds. A non-zero value specifies
     * the timeout when reading from Input stream when a connection is established
     * to a resource. If the timeout expires before there is data available for
     * read, a java.net.SocketTimeoutException is raised. A timeout of zero is
     * interpreted as an infinite timeout.
     *
     * @return the read timeout in milliseconds
     */
    int getReadTimeout();

    /**
     * Define the proxy host of the proxy used by the Requests
     * configured by this RequestConfiguration. The host string
     * authorized formats are the ones allowed by the InetAddress
     * constructor
     *
     * @proxyHost the proxy host of the proxy used by the Requests based
     * on this RequestConfiguration
     */
    ConnectionConfiguration<RESPONSE, REQUEST> setProxyHost(String proxyHost);

    /**
     * Define the proxy port of the proxy used by the Requests
     * configured by this RequestConfiguration.
     *
     * @proxyPort the proxy port of the proxy used by the Requests
     * based on this RequestConfiguration
     */
    ConnectionConfiguration<RESPONSE, REQUEST> setProxyPort(int proxyPort);

    /**
     * Returns a new {@link Proxy} instance using
     * the retrieved proxy's host and port
     * configuration
     *
     * @return a new {@link Proxy} instance
     */
    Proxy getProxy();

    /**
     * Creates and returns a new {@link HttpURLConnection} based
     * on this RequestConfiguration
     *
     * @return a new {@link AbstractRequest} based on this
     * RequestConfiguration
     * @throws IOException
     */
    HttpURLConnection connect() throws IOException;
    
}
