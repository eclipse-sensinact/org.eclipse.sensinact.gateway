/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.nthbnd.endpoint.format;

import java.io.StringReader;

import org.eclipse.sensinact.gateway.common.primitive.JSONable;
import org.eclipse.sensinact.gateway.util.CastUtils;
import org.eclipse.sensinact.gateway.util.JSONUtils;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;

import jakarta.json.JsonObject;

public class JSONResponseFormat implements ResponseFormat<JsonObject> {

    @Override
    public JsonObject format(Object object) {
        if (object == null) {
            return null;
        }
        if (JsonObject.class.isAssignableFrom(object.getClass())) {
            return (JsonObject) object;
        }
        if (JSONable.class.isAssignableFrom(object.getClass())) {
        	return JsonProviderFactory.getProvider()
        			.createReader(new StringReader(((JSONable)object).getJSON()))
        			.readObject();
        }
        String json = JSONUtils.toJSONFormat(object);
        try {
            return CastUtils.cast(JsonObject.class, json);

        } catch (ClassCastException e) {
        	return JsonProviderFactory.getProvider().createObjectBuilder()
        		.add("response", json)
        		.build();
        }
    }
}
