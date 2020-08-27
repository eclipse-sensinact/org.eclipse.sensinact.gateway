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
    public static final String DEFAULT_SERVER_SSL_CERTIFICATE = NO_CERTIFICATE;
    public static final String DEFAULT_ACCEPT_TYPE = "text/plain";
    public static final String DEFAULT_CONTENT_TYPE = "text/plain";
    public static final String DEFAULT_SCHEME = "http";
    public static final String DEFAULT_HTTP_METHOD = "GET";
    public static final String DEFAULT_PORT = "80";
    public static final String DEFAULT_PATH = "/";

    String acceptType() default DEFAULT_ACCEPT_TYPE;

    String contentType() default DEFAULT_CONTENT_TYPE;

    String httpMethod() default DEFAULT_HTTP_METHOD;

    String scheme() default DEFAULT_SCHEME;

    String host();

    String port() default DEFAULT_PORT;

    String path() default DEFAULT_PATH;
    
    String clientSSLCertificate() default DEFAULT_CLIENT_SSL_CERTIFICATE;

    String serverSSLCertificate() default DEFAULT_SERVER_SSL_CERTIFICATE;

    boolean direct() default false;

    KeyValuePair[] query() default {};

    KeyValuePair[] headers() default {};

    Class<? extends HttpTaskConfigurator> content() default HttpTaskConfigurator.class;
    
    Class<? extends HttpPacket> packet() default HttpPacket.class;
}
