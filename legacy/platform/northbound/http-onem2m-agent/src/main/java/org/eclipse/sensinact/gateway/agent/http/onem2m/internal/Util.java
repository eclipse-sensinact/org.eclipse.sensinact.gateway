/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.agent.http.onem2m.internal;

import org.eclipse.sensinact.gateway.protocol.http.client.ConnectionConfiguration;
import org.eclipse.sensinact.gateway.protocol.http.client.ConnectionConfigurationImpl;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleRequest;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Util {
    private static Logger LOG = LoggerFactory.getLogger(Util.class.getCanonicalName());

    public static final int createRequest(String cseBase, String method, String origin, String path, String contentType, String content) throws IOException {
        ConnectionConfiguration<SimpleResponse, SimpleRequest> configuration = new ConnectionConfigurationImpl<SimpleResponse, SimpleRequest>();
        configuration.setHttpMethod(method);
        if (path != null) {
            configuration.setUri(new StringBuilder().append(cseBase).append(path).toString());
        } else {
            configuration.setUri(cseBase);
        }
        if (origin != null) {
            // this should be sent only in case of AE or CSE
            configuration.addHeader("X-M2M-Origin", origin);
        } else {
            configuration.addHeader("X-M2M-Origin", "C");
        }
        configuration.setAccept("application/json");
        configuration.setContentType(contentType);
        configuration.setContent(content);
        LOG.debug("Sending request to URI {}", configuration.getUri());
        LOG.debug("Headers from request to {}", configuration.getUri());
        for (Map.Entry<String,List<String>> entry : configuration.getHeaders().entrySet()) {
            LOG.debug("{} : {}", entry.getKey(), entry.getValue().toString());
        }
        LOG.debug("Request body: {}", content);
        SimpleRequest req = new SimpleRequest(configuration);
        SimpleResponse resp = req.send();
        Integer status = resp.getStatusCode();
        LOG.debug("HTTP response code {}, with payload '{}'", status, resp.toString());
        return status;
    }
}
