/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.app.basic.sna;

import java.io.IOException;

import org.eclipse.sensinact.gateway.app.api.plugin.PluginHook;
import org.eclipse.sensinact.gateway.app.manager.component.data.ResourceData;
import org.eclipse.sensinact.gateway.core.Resource;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;
import org.osgi.framework.BundleContext;

import jakarta.json.JsonObject;

/**
 * This class is fired at the end of the application
 *
 * @author Remi Druilhe
 * @see ActionFunction
 */
public class SetActionFunction extends ActionFunction {
    private static final String JSON_SCHEMA = "set.json";

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
     * @see PluginHook#fireHook()
     */
    public void fireHook() {
        ResourceData resourceVariable = (ResourceData) variables.remove(0);String resourceType = resourceVariable.getResourceType();
        if(resourceType == null) {
            throw new RuntimeException("Resource " + resourceVariable.getSourceUri() + " does not exist");
        }
        if(!Resource.Type.ACTION.name().equals(resourceType)){
            Object[] params = new Object[variables.size()];
            for (int i = 0; i < variables.size(); i++) {
                params[i] = variables.get(i).getValue();
            }
            resourceVariable.set(params[0]);
        } else {
            throw new RuntimeException("Resource " + resourceVariable.getSourceUri() + " is not an DataResource");
        }
    }
}
