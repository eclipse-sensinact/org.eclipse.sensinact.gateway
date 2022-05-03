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

import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse;
import org.json.JSONObject;

public class StringResponseFormat implements ResponseFormat<String> {
    @Override
    public String format(Object object) {
        if (object == null) {
            return null;
        }
        if (JSONObject.class.isAssignableFrom(object.getClass())) {
            return ((JSONObject) object).toString();
        }
        if (AccessMethodResponse.class.isAssignableFrom(object.getClass())) {
            return ((AccessMethodResponse<?>) object).getJSON();
        }
        return object.toString();
    }
}
