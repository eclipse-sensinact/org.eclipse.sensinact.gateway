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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class OneM2MModel {
    private static Logger LOG = LoggerFactory.getLogger(OneM2MModel.class.getCanonicalName());
    private String cseBase;
    private Map<String, OneM2MModelResource> model = new HashMap<String, OneM2MModelResource>();
    private static OneM2MModel instance;
    
    private final ObjectMapper mapper = new ObjectMapper();

    private OneM2MModel(String cseBase) {
        this.cseBase = cseBase;
    }

    public static OneM2MModel getInstance(String cseBase) {
        if (instance == null) {
            LOG.debug("Creating OneM2M model singleton to manage created instance values");
            instance = new OneM2MModel(cseBase);
        }
        return instance;
    }

    private void createModel(String provider, String service, String resource, String value) {
        if (!model.keySet().contains(provider)) {
            Map<String, Object> m2mmodel = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            content.put("rn", provider);
            content.put("api", provider);
            content.put("lbl", Arrays.asList("key1","key2"));
            content.put("rr", false);
            m2mmodel.put("m2m:ae", content);
            try {
                Util.createRequest(cseBase, "POST", "Kentyou" + provider.toUpperCase(), null, "application/json;ty=2", 
                		mapper.writeValueAsString(m2mmodel));
            } catch (IOException e) {
                LOG.debug("Failed to create application container in OneM2M server", e);
            }
        } else {
            LOG.warn("Container for provider {} already exists, just integrating reading.", provider);
        }
    }

    public void integrateReading(String provider, String service, String resource, String value) {
        if (!model.containsKey(provider)) {
            createModel(provider, service, resource, value);
            OneM2MModelResource resourceModel = new OneM2MModelResource(provider, cseBase, mapper);
            model.put(provider, resourceModel);
        }
        model.get(provider).addResourceInfo(service, resource, value);
    }

    public void removeProvider(String provider) {
        OneM2MModelResource resourceModel = model.remove(provider);
        if (resourceModel != null) {
            try {
                resourceModel.removeResource();
            } catch (Exception e) {
                LOG.error("Failed to remove resource container {}", provider);
            }
            Map<String, Object> content = new HashMap<>();
            content.put("rn", provider);
            try {
                Util.createRequest(cseBase, "DELETE", provider, null, "application/json;ty=2", 
                		mapper.writeValueAsString(Collections.singletonMap("m2m:ae", content)));
            } catch (IOException e) {
                LOG.error("Failed to remove AE", e);
            }
        } else {
            LOG.warn("Impossible to remove provider {}, it does not exist in managed list", provider);
        }
    }
}
