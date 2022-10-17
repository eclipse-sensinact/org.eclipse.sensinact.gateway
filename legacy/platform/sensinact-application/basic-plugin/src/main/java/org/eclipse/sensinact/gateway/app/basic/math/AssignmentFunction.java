/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.app.basic.math;

import java.io.IOException;
import java.util.List;

import org.eclipse.sensinact.gateway.app.api.function.AbstractFunction;
import org.eclipse.sensinact.gateway.app.api.function.DataItf;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;
import org.osgi.framework.BundleContext;

import jakarta.json.JsonObject;

/**
 * This class implements the assignment function
 *
 * @author Remi Druilhe
 * @see MathFunction
 */
public class AssignmentFunction extends MathFunction<Object> {
    private static final String JSON_SCHEMA = "assignment.json";

    public AssignmentFunction() {
        super();
    }

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
     * @see AbstractFunction#process(List)
     */
    public void process(List<DataItf> variables) {
        /*if(variables.size() == 2) {
            ((ComponentData) variables.get(1)).setValue(variables.get(0).getValue());
        }*/
        super.update(variables.get(0).getValue());
    }
}
