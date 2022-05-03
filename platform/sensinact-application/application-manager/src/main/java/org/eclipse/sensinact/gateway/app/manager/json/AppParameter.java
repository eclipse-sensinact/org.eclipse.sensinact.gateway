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
import org.json.JSONObject;

import java.util.Objects;

/**
 * Wrapper of a parameter
 *
 * @author Remi Druilhe
 */
public class AppParameter implements JSONable {
    protected Object value;
    protected String type;

    /**
     * Java constructor of a parameter.
     *
     * @param value the value of the parameter
     * @param type  the string type of the parameter. It include AppManager specifics types
     *              (resource, variable, event, operator).
     */
    public AppParameter(Object value, String type) {
        this.value = value;
        this.type = type;
    }

    /**
     * JSON constructor of a parameter
     *
     * @param json the json value of the parameter
     */
    public AppParameter(JSONObject json) {
        this.value = json.get(AppJsonConstant.VALUE);
        this.type = json.getString(AppJsonConstant.TYPE);
    }

    /**
     * Get the value of the parameter
     *
     * @return the value
     */
    public Object getValue() {
        return this.value;
    }

    /**
     * Get the type of the parameter
     *
     * @return the type
     */
    public String getType() {
        return this.type;
    }

    /**
     * @see Object#equals(Object)
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AppParameter parameter = (AppParameter) o;
        if (!value.equals(parameter.value)) {
            return false;
        }
        return type.equals(parameter.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, type);
    }

    /**
     * @see JSONable#getJSON()
     */
    public String getJSON() {
        return new JSONObject().put(AppJsonConstant.VALUE, value).put(AppJsonConstant.TYPE, type).toString();
    }
}
