/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial implementation
 */
package org.eclipse.sensinact.gateway.agent.http.onem2m.internal;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class OneM2MModel {
    private static Logger LOG = LoggerFactory.getLogger(OneM2MModel.class.getCanonicalName());
    private String cseBase;
    private Map<String, OneM2MModelResource> model = new HashMap<String, OneM2MModelResource>();
    private static OneM2MModel instance;

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
            JSONObject m2mmodel = new JSONObject();
            JSONObject content = new JSONObject();
            content.put("rn", provider);
            content.put("api", provider);
            content.put("lbl", new JSONArray().put("key1").put("key2"));
            content.put("rr", false);
            m2mmodel.put("m2m:ae", content);
            try {
                Util.createRequest(cseBase, "POST", "CEA" + provider.toUpperCase(), null, "application/json;ty=2", m2mmodel);
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
            OneM2MModelResource resourceModel = new OneM2MModelResource(provider, cseBase);
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
            JSONObject content = new JSONObject();
            content.put("rn", provider);
            try {
                Util.createRequest(cseBase, "DELETE", provider, null, "application/json;ty=2", new JSONObject().put("m2m:ae", content));
            } catch (IOException e) {
                LOG.error("Failed to remove AE", e);
            }
        } else {
            LOG.warn("Impossible to remove provider {}, it does not exist in managed list", provider);
        }
    }
}
