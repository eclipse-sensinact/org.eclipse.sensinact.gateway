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
package org.eclipse.sensinact.gateway.app.basic.sna;

import org.eclipse.sensinact.gateway.app.api.plugin.PluginHook;
import org.eclipse.sensinact.gateway.app.manager.component.data.ResourceData;
import org.eclipse.sensinact.gateway.core.ActionResource;
import org.eclipse.sensinact.gateway.core.Resource;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.osgi.framework.BundleContext;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * This class is fired at the end of the application
 *
 * @author Remi Druilhe
 * @see ActionFunction
 */
public class ActActionFunction extends ActionFunction {
    private static final String JSON_SCHEMA = "act.json";

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
     * @see PluginHook#fireHook()
     */
    public void fireHook() {
        if (variables.size() != 0) {
            ResourceData resourceVariable = (ResourceData) variables.remove(0);
            Resource resource = resourceVariable.getResource();
            if (resource != null) {
                if (resource instanceof ActionResource) {
                    Object[] arguments = new Object[variables.size()];
                    for (int i = 0; i < variables.size(); i++) {
                        arguments[i] = variables.get(i).getValue();
                    }
                    ((ActionResource) resource).act(arguments);
                } else {
                    throw new RuntimeException("Resource " + resourceVariable.getSourceUri() + " is not an ActionResource");
                }
            } else {
                throw new RuntimeException("Resource " + resourceVariable.getSourceUri() + " does not exist");
            }
        }
    }
}
