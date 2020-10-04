/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
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
