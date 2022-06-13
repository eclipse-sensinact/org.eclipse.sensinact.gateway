/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.app.manager.json;

import org.eclipse.sensinact.gateway.common.primitive.JSONable;

import jakarta.json.JsonObject;

/**
 * Finalize is not used for now
 *
 * @author Rémi Druilhe
 */
public class AppFinalize implements JSONable {
    public AppFinalize(JsonObject finalize) {
    }

    /**
     * @see JSONable#getJSON()
     */
    public String getJSON() {
        return JsonObject.EMPTY_JSON_OBJECT.toString();
    }
}
