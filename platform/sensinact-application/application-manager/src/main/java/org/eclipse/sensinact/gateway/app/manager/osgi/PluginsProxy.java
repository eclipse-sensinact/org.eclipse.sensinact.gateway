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
package org.eclipse.sensinact.gateway.app.manager.osgi;

import org.eclipse.sensinact.gateway.app.api.exception.FunctionNotFoundException;
import org.eclipse.sensinact.gateway.app.api.function.AbstractFunction;
import org.eclipse.sensinact.gateway.app.api.plugin.PluginInstaller;
import org.eclipse.sensinact.gateway.app.manager.json.AppComponent;
import org.eclipse.sensinact.gateway.app.manager.json.AppFunction;
import org.eclipse.sensinact.gateway.app.manager.json.AppParameter;
import org.json.JSONObject;
import org.osgi.framework.ServiceReference;

public class PluginsProxy {
    public static final String APP_INSTALL_HOOK_FILTER = "(objectClass=" + PluginInstaller.class.getCanonicalName() + ")";

    /**
     * Search in the plugins for the JSON schema of the specified function
     *
     * @param mediator the mediator
     * @param function the function
     * @return the corresponding {@link AppParameter}
     * @throws FunctionNotFoundException when no plugins can return the corresponding function
     */
    public static JSONObject getComponentJSONSchema(AppServiceMediator mediator, String function) throws FunctionNotFoundException {
        JSONObject schema = null;
        ServiceReference[] serviceReferences = mediator.getServiceReferences(APP_INSTALL_HOOK_FILTER);
        for (ServiceReference serviceReference : serviceReferences) {
            schema = ((PluginInstaller) mediator.getService(serviceReference)).getComponentJSONSchema(function);
            if (schema != null) {
                break;
            }
        }
        if (schema == null) {
            throw new FunctionNotFoundException("Function " + function + " not found");
        }
        return schema;
    }

    /**
     * Search for the {@link AbstractFunction} in the plugins that is described in the {@link AppComponent}
     *
     * @param mediator the mediator
     * @param function the function to construct from the plugins
     * @return the corresponding {@link AbstractFunction}
     * @throws FunctionNotFoundException when no plugins can return the corresponding function
     */
    public static AbstractFunction getFunction(AppServiceMediator mediator, AppFunction function) throws FunctionNotFoundException {
        AbstractFunction functionBlock = null;
        ServiceReference[] serviceReferences = mediator.getServiceReferences(APP_INSTALL_HOOK_FILTER);
        if (serviceReferences != null) {
            for (ServiceReference serviceReference : serviceReferences) {
                functionBlock = ((PluginInstaller) mediator.getService(serviceReference)).getFunction(function);
                if (functionBlock != null) {
                    break;
                }
            }
        } else {
            throw new FunctionNotFoundException("Function " + function.getName() + " not found");
        }
        return functionBlock;
    }
}
