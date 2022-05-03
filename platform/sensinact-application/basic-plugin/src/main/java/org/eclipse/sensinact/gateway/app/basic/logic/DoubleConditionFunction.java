/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.app.basic.logic;

import org.eclipse.sensinact.gateway.app.api.exception.InvalidApplicationException;
import org.eclipse.sensinact.gateway.app.api.exception.NotAReadableResourceException;
import org.eclipse.sensinact.gateway.app.api.exception.ResourceNotFoundException;
import org.eclipse.sensinact.gateway.app.api.exception.ServiceNotFoundException;
import org.eclipse.sensinact.gateway.app.api.function.DataItf;
import org.eclipse.sensinact.gateway.util.CastUtils;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.osgi.framework.BundleContext;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * A component that tests if the condition of both parent nodes is satisfied
 *
 * @author Remi Druilhe
 * @see ConditionFunction
 */
public class DoubleConditionFunction extends ConditionFunction {
    private final String function;
    private static final String JSON_SCHEMA = "double_condition.json";

    public DoubleConditionFunction(String function) {
        this.function = function;
    }

    /**
     * Gets the JSON schema of the function from the plugin
     *
     * @param context the context of the bundle
     * @return the JSON schema of the function
     */
    public static JSONObject getJSONSchemaFunction(BundleContext context) {
        try {
            return new JSONObject(new JSONTokener(new InputStreamReader(context.getBundle().getResource("/" + JSON_SCHEMA).openStream())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Test that the condition from both parent nodes are satisfied according to the operator
     *
     * @see ConditionFunction#process(List)
     */
    public Boolean testCondition(List<DataItf> datas) throws NotAReadableResourceException, ResourceNotFoundException, ServiceNotFoundException {
        boolean result = false;
        if (function.equals("and")) {
            result = true;
        }
        for (DataItf data : datas) {
            if (data != null) {
                if (data.getValue() != null) {
                    if (function.equals("and")) {
                        if (!CastUtils.castPrimitive(boolean.class, data.getValue())) {
                            result = false;
                            break;
                        }
                        result = true;
                    } else if (function.equals("or")) {
                        if (CastUtils.castPrimitive(boolean.class, data.getValue())) {
                            result = true;
                            break;
                        }
                        result = false;
                    } else {
                        try {
                            throw new InvalidApplicationException("It should never happened");
                        } catch (InvalidApplicationException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                } else {
                    result = false;
                    break;
                }
            } else {
                return null;
            }
        }
        return result;
    }
}
