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

import java.io.IOException;
import java.util.List;

import org.eclipse.sensinact.gateway.app.api.exception.NotAReadableResourceException;
import org.eclipse.sensinact.gateway.app.api.exception.ResourceNotFoundException;
import org.eclipse.sensinact.gateway.app.api.exception.ServiceNotFoundException;
import org.eclipse.sensinact.gateway.app.api.function.DataItf;
import org.eclipse.sensinact.gateway.util.CastUtils;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;
import org.osgi.framework.BundleContext;

import jakarta.json.JsonObject;

public class BetweenFunction extends ConditionFunction {
    private static final String JSON_SCHEMA = "between.json";

    /**
     * Gets the JSON schema of the function from the plugin
     *
     * @param context the context of the bundle
     * @return the JSON schema of the function
     */
    public static JsonObject getJSONSchemaFunction(BundleContext context) {
        try {
        	return JsonProviderFactory.getProvider().createReader(context.getBundle().getResource("/" + JSON_SCHEMA).openStream()).readObject();
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
