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
package org.eclipse.sensinact.gateway.southbound.http.factory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.sensinact.gateway.southbound.device.factory.dto.DeviceMappingConfigurationDTO;
import org.eclipse.sensinact.gateway.southbound.http.factory.config.HttpDeviceFactoryConfigurationTaskDTO;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Parsed HTTP task configuration
 */
public class ParsedHttpTask {

    /**
     * Minimum buffer size (in KB)
     */
    private static final int MIN_BUFFER_SIZE = 512;

    /**
     * Simple Key/value pair
     */
    public static class KeyValue<K, V> {
        private final K key;
        private final V value;

        public KeyValue(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K key() {
            return key;
        }

        public V value() {
            return value;
        }
    }

    /**
     * Request timeout in seconds
     */
    public final int timeout;

    /**
     * Response buffer size
     */
    private final int bufferSize;

    /**
     * HTTP method
     */
    public final String method;

    /**
     * Target URL
     */
    public final String url;

    /**
     * SSL options
     */
    public final boolean ignoreSslErrors;
    public final String keystorePath;
    public final String keystorePassword;
    public final String trustStorePath;
    public final String trustStorePassword;

    /**
     * Basic authentication options
     */
    public final String authUser;
    public final String authPassword;

    /**
     * Follow HTTP redirections
     */
    public final boolean followHttpRedirect;

    /**
     * HTTP request headers
     */
    private final List<KeyValue<String, String>> headers = new ArrayList<>();

    /**
     * Response content parsing configuration
     */
    public final DeviceMappingConfigurationDTO mapping;

    public ParsedHttpTask(final HttpDeviceFactoryConfigurationTaskDTO task)
            throws JsonMappingException, JsonProcessingException {
        this.method = task.method != null && !task.method.isBlank() ? task.method : "GET";

        this.url = task.url;
        if (this.url == null || this.url.isBlank()) {
            throw new IllegalArgumentException("No URL given");
        }

        this.followHttpRedirect = task.httpFollowRedirect;

        this.ignoreSslErrors = task.sslIgnoreErrors;
        this.keystorePath = task.sslKeyStore;
        this.keystorePassword = task.sslKeyStorePassword;
        this.trustStorePath = task.sslTrustStore;
        this.trustStorePassword = task.sslTrustStorePassword;

        this.authUser = task.authUser;
        this.authPassword = task.authPassword;

        this.timeout = task.timeout;
        this.bufferSize = task.bufferSize < MIN_BUFFER_SIZE ? MIN_BUFFER_SIZE : task.bufferSize;

        // Parsed configuration
        final JsonNode jsonHeaders = task.headers;
        if (jsonHeaders != null) {
            parseHeaders(jsonHeaders);
        }

        mapping = task.mapping;
        if (mapping == null) {
            throw new IllegalArgumentException("No mapping given");
        }
    }

    /**
     * Parses the configured Headers
     */
    private void parseHeaders(final JsonNode headersRoot) throws JsonMappingException, JsonProcessingException {
        if (headersRoot.isArray()) {
            for (int i = 0; i < headersRoot.size(); i++) {
                final JsonNode header = headersRoot.get(i);
                if (!header.isObject()) {
                    throw new IllegalArgumentException("Headers should be an array of objects");
                }

                this.headers.add(new KeyValue<>(header.get("header").asText(), header.get("value").asText()));
            }
        } else {
            final Iterator<Entry<String, JsonNode>> iterator = headersRoot.fields();
            while (iterator.hasNext()) {
                final Entry<String, JsonNode> entry = iterator.next();
                final String header = entry.getKey();
                if (header != null && !header.isBlank()) {
                    this.headers.add(new KeyValue<String, String>(header, entry.getValue().asText()));
                }
            }
        }
    }

    /**
     * Configured HTTP request headers
     */
    public List<KeyValue<String, String>> getHeaders() {
        return List.copyOf(headers);
    }

    /**
     * Returns buffer size in bytes
     */
    public int getBufferSize() {
        return bufferSize * 1024;
    }
}
