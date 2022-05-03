/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
/**
 *
 */
package org.eclipse.sensinact.gateway.sthbnd.http.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import org.eclipse.sensinact.gateway.sthbnd.http.HttpPacket;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpTaskConfigurator;

@Documented
@Retention(RUNTIME)
public @interface HttpTaskConfiguration {
	public static final String NO_CERTIFICATE = "#NO_CERTIFICATE#";
    public static final String DEFAULT_CLIENT_SSL_CERTIFICATE = NO_CERTIFICATE;
    public static final String DEFAULT_CLIENT_SSL_CERTIFICATE_PASSWORD = NO_CERTIFICATE;
    public static final String DEFAULT_SERVER_SSL_CERTIFICATE = NO_CERTIFICATE;
    public static final String DEFAULT_ACCEPT_TYPE = "text/plain";
    public static final String DEFAULT_CONTENT_TYPE = "text/plain";
    public static final String DEFAULT_SCHEME = "http";
    public static final String DEFAULT_HTTP_METHOD = "GET";
    public static final String DEFAULT_PORT = "80";
    public static final String DEFAULT_PATH = "/";
    public static final int DEFAULT_CONNECTION_TIMEOUT = 300000;
    public static final int DEFAULT_READ_TIMEOUT = 300000;

    String acceptType() default DEFAULT_ACCEPT_TYPE;

    String contentType() default DEFAULT_CONTENT_TYPE;

    String httpMethod() default DEFAULT_HTTP_METHOD;

    String scheme() default DEFAULT_SCHEME;

    String host();

    String port() default DEFAULT_PORT;

    String path() default DEFAULT_PATH;

    String clientSSLCertificate() default DEFAULT_CLIENT_SSL_CERTIFICATE;
    
    String clientSSLCertificatePassword() default DEFAULT_CLIENT_SSL_CERTIFICATE_PASSWORD;

    String serverSSLCertificate() default DEFAULT_SERVER_SSL_CERTIFICATE;

    boolean direct() default false;

    KeyValuePair[] query() default {};

    KeyValuePair[] headers() default {};

    Class<? extends HttpTaskConfigurator> content() default HttpTaskConfigurator.class;
    
    Class<? extends HttpPacket> packet() default HttpPacket.class;

    int connectTimeout() default DEFAULT_CONNECTION_TIMEOUT;

    int readTimeout() default DEFAULT_READ_TIMEOUT;
}
