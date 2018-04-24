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
import org.eclipse.sensinact.gateway.app.api.persistence.ApplicationPersistenceService;
import org.eclipse.sensinact.gateway.app.api.persistence.exception.ApplicationPersistenceException;
import org.eclipse.sensinact.gateway.app.api.persistence.listener.ApplicationAvailabilityListenerAbstract;
import org.eclipse.sensinact.gateway.app.manager.application.ApplicationService;
import org.eclipse.sensinact.gateway.app.manager.osgi.AppServiceMediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.ServiceProviderImpl;
import org.eclipse.sensinact.gateway.core.method.AccessMethodExecutor;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponseBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @see AccessMethodExecutor
 *
 * @author Remi Druilhe
 */
public class AppUninstallExecutor extends ApplicationAvailabilityListenerAbstract implements AccessMethodExecutor {
    private static Logger LOG= LoggerFactory.getLogger(AppUninstallExecutor.class);
    private final AppServiceMediator mediator;
    private final ServiceProviderImpl device;
    private final ApplicationPersistenceService persistenceService;

    /**
     * Constructor
     * @param device the AppManager service provider
     */
    AppUninstallExecutor(AppServiceMediator mediator, ServiceProviderImpl device, ApplicationPersistenceService persistenceService) {
        this.mediator = mediator;
        this.device = device;
        this.persistenceService=persistenceService;
    }

    /**
     * @see Executable#execute(java.lang.Object)
     */
    public Void execute(AccessMethodResponseBuilder jsonObjects) {
        String name = (String) jsonObjects.getParameter(0);

        try{
            uninstall(name);

            jsonObjects.push(new JSONObject().put("message", "The application " + name +
                    " has been uninstalled"));
        }catch (Exception e){
            jsonObjects.setAccessMethodObjectResult(new JSONObject().put(
                    "message", "The application " + name +" has failed to be uninstalled"));
        }

        return null;
    }

    public void uninstall(String name) throws InvalidApplicationException {

        if (name == null)
            throw new InvalidApplicationException("Wrong parameters. Unable to uninstall the application");
        else {

            try {
                persistenceService.delete(name);
            } catch (ApplicationPersistenceException e) {
                LOG.warn("Impossible to remove application '{}' from persistence system");
            }

/*
            if (device.getService(name) != null) {

                ApplicationService applicationService = (ApplicationService) device.getService(name);
                if (applicationService != null && applicationService.getApplication() != null) {

                    applicationService.getApplication().stop();
                    applicationService.getApplication().uninstall();
                    if (device.removeService(applicationService.getName())) {
                        LOG.info("Application " + name + " successfully uninstalled.");
                    } else {
                        LOG.warn("Failed to remove application '{}'",name);
                    }
                } else {
                    throw new InvalidApplicationException("Unable to uninstall the application.");
                }
            } else {
                throw new InvalidApplicationException("Unable to uninstall the application." +
                        "Application " + name + " does not exist");
            }
*/


        }

    }

}
