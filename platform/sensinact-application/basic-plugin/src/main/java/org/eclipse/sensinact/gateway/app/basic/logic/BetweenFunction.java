/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.app.basic.logic;

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

public class BetweenFunction extends ConditionFunction {
    private static final String JSON_SCHEMA = "between.json";

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
        double variable = CastUtils.castPrimitive(double.class, datas.get(0).getValue());
        double lowerLimit = CastUtils.castPrimitive(double.class, datas.get(1).getValue());
        double higherLimit = CastUtils.castPrimitive(double.class, datas.get(2).getValue());
        if ((variable >= lowerLimit) && (variable <= higherLimit)) {
            result = true;
        }
        return result;
    }
}
