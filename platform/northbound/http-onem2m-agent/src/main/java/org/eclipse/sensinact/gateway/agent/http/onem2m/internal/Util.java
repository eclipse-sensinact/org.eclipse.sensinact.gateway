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
package org.eclipse.sensinact.gateway.agent.http.onem2m.internal;

import org.eclipse.sensinact.gateway.protocol.http.client.ConnectionConfiguration;
import org.eclipse.sensinact.gateway.protocol.http.client.ConnectionConfigurationImpl;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleRequest;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleResponse;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class Util {
    private static Logger LOG = LoggerFactory.getLogger(Util.class.getCanonicalName());

    public static final int createRequest(String cseBase, String method, String origin, String path, String contentType, JSONObject content) throws IOException {
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
        configuration.setContent(content.toString());
        LOG.debug("Sending request to URI {}", configuration.getUri());
        LOG.debug("Headers from request to {}", configuration.getUri());
        for (Map.Entry entry : configuration.getHeaders().entrySet()) {
            LOG.debug("{} : {}", entry.getKey(), entry.getValue().toString());
        }
        LOG.debug("Request body: {}", content.toString());
        SimpleRequest req = new SimpleRequest(configuration);
        SimpleResponse resp = req.send();
        Integer status = resp.getStatusCode();
        LOG.debug("HTTP response code {}, with payload '{}'", status, resp.toString());
        return status;
    }
}
