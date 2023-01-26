/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.gateway.southbound.http.factory.config;

import org.eclipse.sensinact.gateway.southbound.device.factory.dto.DeviceMappingConfigurationDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Root configuration of a device factory
 */
public class HttpDeviceFactoryConfigurationTaskDTO {

    /**
     * HTTP request timeout (in seconds)
     */
    public int timeout = 30;

    /**
     * Response buffer size (in KB)
     */
    public int bufferSize;

    /**
     * HTTP method
     */
    public String method;

    /**
     * Target URL
     */
    public String url;

    /**
     * Device factory mapping
     */
    public DeviceMappingConfigurationDTO mapping;

    /**
     * HTTP headers mapping
     */
    public JsonNode headers;

    /**
     * Ignore SSL errors
     */
    @JsonProperty("ssl.ignoreErrors")
    public boolean sslIgnoreErrors;

    /**
     * Path to SSL key store
     */
    @JsonProperty("ssl.keystore")
    public String sslKeyStore;

    /**
     * Password of the SSL key store
     */
    @JsonProperty("ssl.keystore.password")
    public String sslKeyStorePassword;

    /**
     * Path to SSL trust store
     */
    @JsonProperty("ssl.truststore")
    public String sslTrustStore;

    /**
     * Password of the SSL trust store
     */
    @JsonProperty("ssl.truststore.password")
    public String sslTrustStorePassword;

    /**
     * Follow HTTP redirections
     */
    @JsonProperty("http.followRedirect")
    public boolean httpFollowRedirect;

    /**
     * HTTP basic authentication login
     */
    @JsonProperty("auth.user")
    public String authUser;

    /**
     * HTTP basic authentication password
     */
    @JsonProperty("auth.password")
    public String authPassword;
}
