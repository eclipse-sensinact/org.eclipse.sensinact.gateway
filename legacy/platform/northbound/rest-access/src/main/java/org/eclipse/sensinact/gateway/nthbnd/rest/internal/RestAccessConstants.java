/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.nthbnd.rest.internal;

/**
 * The set of constants used in the restful access module
 *
 * @author <a href="mailto:cmunilla@kentyou.com">Christophe Munilla</a>
 */
public abstract class RestAccessConstants {
    public static final String NORTHBOUND_ENDPOINTS = "org.eclipse.sensiNact.rest.northbound.endpoints";
    public static final String CORS_HEADER = "org.eclipse.sensinact.http.corsheader";
    public static final String OPTIONS = "OPTIONS";
    public static final String PARTIAL_JSON_CONTENT_TYPE = "application/json";

    public static final String ANY_CONTENT_TYPE = "*/*";
    public static String JSON_CONTENT_TYPE = "application/json; charset=utf-8";
    public static final String TEXT_CONTENT_TYPE = "text/plain; charset=utf-8";
    public static final String HTTP_ROOT = "/sensinact/*";
    public static final String WS_ROOT = "/ws/sensinact";
    public static final String LOGIN_ENDPOINT = "/sensinact.login";
    public static final String REGISTERING_ENDPOINT = "/sensinact.register";

}
