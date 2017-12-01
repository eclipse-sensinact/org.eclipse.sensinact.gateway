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
import org.eclipse.sensinact.gateway.common.primitive.Modifiable;
import org.eclipse.sensinact.gateway.core.DataResource;
import org.eclipse.sensinact.gateway.core.PropertyResource;
import org.eclipse.sensinact.gateway.core.Resource;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.osgi.framework.BundleContext;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * This class is fired at the end of the application
 *
 * @see ActionFunction
 *
 * @author Remi Druilhe
 */
public class SetActionFunction extends ActionFunction {

    private static final String JSON_SCHEMA = "set.json";

    /**
     * Gets the JSON schema of the function from the plugin
     * @param context the context of the bundle
     * @return the JSON schema of the function
     */
    public static JSONObject getJSONSchemaFunction(BundleContext context) {
        try {
            return new JSONObject(new JSONTokener(
                    new InputStreamReader(context.getBundle().getResource("/" + JSON_SCHEMA).openStream())));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * @see PluginHook#fireHook()
     */
    public void fireHook() {
        ResourceData resourceVariable = (ResourceData) variables.remove(0);
        Resource resource = resourceVariable.getResource();

        if(resource != null) {
            Object[] params = new Object[variables.size()];

            for (int i = 0; i < variables.size(); i++) {
                params[i] = variables.get(i).getValue();
            }

            if (Modifiable.MODIFIABLE.equals(resource.element(DataResource.VALUE
            		).getModifiable()))
            {
                ((PropertyResource) resource).set(params[0]);
                
            } else {
                throw new RuntimeException("Resource " + resourceVariable.getSourceUri()
                        + " is not a modifiable PropertyResource");
            }
        } else {
            throw new RuntimeException("Resource " + resourceVariable.getSourceUri()
                    + " does not exist");
        }
    }
}
