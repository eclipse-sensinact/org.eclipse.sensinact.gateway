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

package org.eclipse.sensinact.gateway.app.manager.internal;

import org.eclipse.sensinact.gateway.app.api.exception.InvalidApplicationException;
import org.eclipse.sensinact.gateway.app.manager.application.ApplicationService;
import org.eclipse.sensinact.gateway.app.manager.osgi.AppServiceMediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.ServiceProviderImpl;
import org.eclipse.sensinact.gateway.core.method.AccessMethodExecutor;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponseBuilder;

import org.json.JSONObject;

/**
 * @see AccessMethodExecutor
 *
 * @author Remi Druilhe
 */
class AppUninstallExecutor implements AccessMethodExecutor {

    private final AppServiceMediator mediator;
    private final ServiceProviderImpl device;

    /**
     * Constructor
     * @param device the AppManager service provider
     */
    AppUninstallExecutor(AppServiceMediator mediator, ServiceProviderImpl device) {
        this.mediator = mediator;
        this.device = device;
    }

    /**
     * @see Executable#execute(java.lang.Object)
     */
    public Void execute(AccessMethodResponseBuilder jsonObjects) throws Exception {
        String name = (String) jsonObjects.getParameter(0);

        if(name != null) {
            if(device.getService(name) != null) {
                ApplicationService applicationService = (ApplicationService) device.getService(name);

                if (applicationService != null) {
                    if (!jsonObjects.hasError()) {
                        //TODO: stop the application first
                        applicationService.getApplication().uninstall();

                        if(device.removeService(applicationService.getName())) {
                            if (mediator.isInfoLoggable()) {
                                mediator.info("Application " + name + " successfully installed.");
                            }

                            jsonObjects.push(new JSONObject().put("message", "The application " + name +
                                    " has been uninstalled"));
                        } else {
                            throw new InvalidApplicationException("Unable to uninstall the application.");
                        }
                    }
                } else {
                    throw new InvalidApplicationException("Unable to uninstall the application." +
                            "Application " + name + " does not exist");
                }
            } else {
                throw new InvalidApplicationException("Unable to uninstall the application." +
                        "Application " + name + " does not exist");
            }
        } else {
            throw new InvalidApplicationException("Wrong parameters. Unable to uninstall the application");
        }

        return null;
    }
}
