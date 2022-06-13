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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OneM2MModelResource {
    private static Logger LOG = LoggerFactory.getLogger(OneM2MModelResource.class.getCanonicalName());
    private final String cseBase;
    private final String provider;
    private Set<String> resources = new HashSet<String>();
    private final String origin;
    
    private final ObjectMapper mapper;

    public OneM2MModelResource(String provider, String cseBase, ObjectMapper mapper) {
        this.cseBase = cseBase;
        this.provider = provider;
        this.origin = "Kentyou" + provider.toUpperCase();
        this.mapper = mapper;
    }

    public void addResourceInfo(String service, String resource, String value) {
        LOG.debug("Integrating reading from provider '{}' resource '{}' with value '{}'. The server is '{}'.", provider, resource, value, cseBase);
        final String fullId = String.format("%s/%s", service, resource);
        if (!resources.contains(fullId)) {
            resources.add(fullId);
            Map<String, Object> resourceContainer = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            content.put("rn", resource);
            content.put("lbl", Arrays.asList(provider));
            resourceContainer.put("m2m:cnt", content);
            try {
                Util.createRequest(cseBase, "POST", "Kentyou" + provider.toUpperCase(), "/" + provider, "application/json;ty=3", 
                		mapper.writeValueAsString(resourceContainer));
            } catch (IOException e) {
                LOG.error("Failed to process http request to OneM2M server {} to update device location", cseBase, e);
            }
        }
        Map<String, Object> resourceValueContent = new HashMap<>();
        Map<String, Object> content = new HashMap<>();
        content.put("con", value.toString());
        resourceValueContent.put("m2m:cin", content);
        try {
            Util.createRequest(cseBase, "POST", "Kentyou" + provider.toUpperCase(), "/" + provider + "/" + resource, "application/json;ty=4", 
            		mapper.writeValueAsString(resourceValueContent));
        } catch (IOException e) {
            LOG.error("Failed to process http request to OneM2M server {} to update device info", cseBase, e);
        }
    }

    private void removeResource(String service, String resource) {
        try {
            // final String fullId=String.format("%s/%s",service,resource);
            Map<String, Object> content = new HashMap<>();
            content.put("rn", resource);
            Util.createRequest(cseBase, "DELETE", origin, "/" + provider, "application/json;ty=3", 
            		mapper.writeValueAsString(Collections.singletonMap("m2m:cnt", content)));
        } catch (IOException e) {
            LOG.debug("Failed to remove resource", e);
        }
    }

    public void removeResource() {
        for (String vl : resources) {
            final String[] splitName = vl.split("/");
            final String service = splitName[0];
            final String resource = splitName[1];
            removeResource(service, resource);
            resources.remove(vl);
        }
    }
}
