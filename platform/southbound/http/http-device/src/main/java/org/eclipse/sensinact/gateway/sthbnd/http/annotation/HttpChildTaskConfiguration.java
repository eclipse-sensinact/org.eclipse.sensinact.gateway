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

import org.eclipse.sensinact.gateway.sthbnd.http.HttpPacket;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpTaskConfigurator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Retention(RUNTIME)
public @interface HttpChildTaskConfiguration {
    public static final String DEFAULT_ACCEPT_TYPE = "#EMPTY#";
    public static final String DEFAULT_CONTENT_TYPE = "#EMPTY#";
    public static final String DEFAULT_SCHEME = "#EMPTY#";
    public static final String DEFAULT_HTTP_METHOD = "#EMPTY#";
    public static final String DEFAULT_PORT = "#EMPTY#";
    public static final String DEFAULT_PATH = "#EMPTY#";
    public static final String DEFAULT_HOST = "#EMPTY#";

    String acceptType() default DEFAULT_ACCEPT_TYPE;

    String contentType() default DEFAULT_CONTENT_TYPE;

    String httpMethod() default DEFAULT_HTTP_METHOD;

    String scheme() default DEFAULT_SCHEME;

    String host() default DEFAULT_HOST;

    String port() default DEFAULT_PORT;

    String path() default DEFAULT_PATH;

    boolean direct() default false;

    String identifier();
    
    String clientSSLCertificate() default HttpTaskConfiguration.DEFAULT_CLIENT_SSL_CERTIFICATE;

    String serverSSLCertificate() default HttpTaskConfiguration.DEFAULT_SERVER_SSL_CERTIFICATE;

    KeyValuePair[] query() default {};

    KeyValuePair[] headers() default {};

    Class<? extends HttpTaskConfigurator> content() default HttpTaskConfigurator.class;
    
    Class<? extends HttpPacket> packet() default HttpPacket.class;
}
