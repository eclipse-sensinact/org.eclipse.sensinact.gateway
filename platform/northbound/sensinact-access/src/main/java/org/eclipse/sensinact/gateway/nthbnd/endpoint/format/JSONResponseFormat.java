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

import org.eclipse.sensinact.gateway.common.primitive.JSONable;
import org.eclipse.sensinact.gateway.util.CastUtils;
import org.eclipse.sensinact.gateway.util.JSONUtils;
import org.json.JSONObject;

public class JSONResponseFormat implements ResponseFormat<JSONObject> {

    @Override
    public JSONObject format(Object object) {
        if (object == null) {
            return null;
        }
        if (JSONObject.class.isAssignableFrom(object.getClass())) {
            return (JSONObject) object;
        }
        if (JSONable.class.isAssignableFrom(object.getClass())) {
            return new JSONObject(((JSONable) object).getJSON());
        }
        String json = JSONUtils.toJSONFormat(object);
        try {
            return CastUtils.cast(JSONObject.class, json);

        } catch (ClassCastException e) {
            return new JSONObject().put("response", json);
        }
    }
}
