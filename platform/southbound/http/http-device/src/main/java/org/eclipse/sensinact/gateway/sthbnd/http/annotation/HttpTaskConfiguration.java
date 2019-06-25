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
public @interface HttpTaskConfiguration {
    public static final String DEFAULT_ACCEPT_TYPE = "text/plain";
    public static final String DEFAULT_CONTENT_TYPE = "text/plain";
    public static final String DEFAULT_SCHEME = "http";
    public static final String DEFAULT_HTTP_METHOD = "GET";
    public static final String DEFAULT_PORT = "80";
    public static final String DEFAULT_PATH = "/";

    String acceptType() default DEFAULT_ACCEPT_TYPE;

    String contentType() default DEFAULT_CONTENT_TYPE;

    String httpMethod() default DEFAULT_HTTP_METHOD;

    String scheme() default "http";

    String host();

    String port() default "80";

    String path() default "/";

    boolean direct() default false;

    KeyValuePair[] query() default {};

    KeyValuePair[] headers() default {};

    Class<? extends HttpTaskConfigurator> content() default HttpTaskConfigurator.class;
    
    Class<? extends HttpPacket> packet() default HttpPacket.class;
}
