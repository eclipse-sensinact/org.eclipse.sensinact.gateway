/*********************************************************************
* Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package org.eclipse.sensinact.gateway.southbound.wot.http;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.dynamic.HttpClientTransportDynamic;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

@Component(immediate = true, service = SharedHttpClient.class)
public class SharedHttpClient {

    private final static Logger logger = LoggerFactory.getLogger(SharedHttpClient.class);

    /**
     * Jetty HTTP client
     */
    HttpClient client;

    /**
     * s Shared object mapper
     */
    final ObjectMapper mapper = JsonMapper.builder().build();

    @Activate
    public SharedHttpClient() throws Exception {
        client = new HttpClient(new HttpClientTransportDynamic());
        try {
            client.start();
        } catch (Exception e) {
            logger.error("Error starting HTTP client", e);
            throw e;
        }
    }

    @Deactivate
    void deactivate() {
        try {
            client.stop();
        } catch (Exception e) {
            logger.error("Error stopping HTTP client", e);
        } finally {
            client = null;
        }
    }

    public HttpClient getClient() {
        return client;
    }

    public ObjectMapper getMapper() {
        return mapper;
    }
}
