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

import org.eclipse.sensinact.gateway.app.api.exception.ApplicationFactoryException;
import org.eclipse.sensinact.gateway.app.api.exception.InvalidApplicationException;
import org.eclipse.sensinact.gateway.app.api.lifecycle.ApplicationStatus;
import org.eclipse.sensinact.gateway.app.manager.AppConstant;
import org.eclipse.sensinact.gateway.app.manager.application.Application;
import org.eclipse.sensinact.gateway.app.manager.checker.ArchitectureChecker;
import org.eclipse.sensinact.gateway.app.manager.factory.ApplicationFactory;
import org.eclipse.sensinact.gateway.app.manager.application.ApplicationService;
import org.eclipse.sensinact.gateway.app.manager.checker.JsonValidator;
import org.eclipse.sensinact.gateway.app.manager.json.AppJsonConstant;
import org.eclipse.sensinact.gateway.app.manager.osgi.AppServiceMediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.core.DataResource;
import org.eclipse.sensinact.gateway.core.ServiceProviderImpl;
import org.eclipse.sensinact.gateway.core.method.AccessMethodExecutor;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResult;

import org.eclipse.sensinact.gateway.app.manager.json.AppContainer;
import org.everit.json.schema.ValidationException;
import org.json.JSONObject;

/**
 * @see AccessMethodExecutor
 *
 * @author Remi Druilhe
 */
class AppInstallExecutor implements AccessMethodExecutor {

    private final AppServiceMediator mediator;
    private final ServiceProviderImpl device;

    /**
     * Constructor
     * @param mediator the mediator
     * @param device the AppManager service provider
     */
    AppInstallExecutor(AppServiceMediator mediator, ServiceProviderImpl device) {
        this.mediator = mediator;
        this.device = device;
    }

    /**
     * @see Executable#execute(java.lang.Object)
     */
    public Void execute(AccessMethodResult jsonObjects) throws Exception {
        String name = (String) jsonObjects.getParameter(0);
        JSONObject content = (JSONObject) jsonObjects.getParameter(1);

        // Test the JSON parameters
        if(name == null) {
            throw new InvalidApplicationException("Unable to install the application: application 'name' is null");
        }

        if(content == null) {
            throw new InvalidApplicationException("Unable to install the application: application 'content' is null");
        }

        // Validate the JSON application against the JSON schema of the application
        try {
            JsonValidator.validateApplication(mediator, content);
        } catch (ValidationException exception) {
            if(mediator.isErrorLoggable()) {
                mediator.error("The JSON of the application is not valid", exception);
            }

            throw exception;
        }

        // Validate the JSON components against the JSON schema of each components
        try {
            JsonValidator.validateFunctionsParameters(mediator, content.getJSONArray(AppJsonConstant.APPLICATION));
        } catch (ValidationException exception) {
            if(mediator.isErrorLoggable()) {
                mediator.error("The JSON of the application is not valid", exception);
            }

            throw exception;
        } catch (InvalidApplicationException exception) {
            if(mediator.isErrorLoggable()) {
                mediator.error("Problem in the application architecture", exception);
            }

            throw exception;
        }

        // Transform JSON to Java object
        AppContainer appContainer = new AppContainer(mediator, name, content);

        // Check that the application is correctly constructed
        ArchitectureChecker.checkApplication(appContainer.getApplicationName(), appContainer.getComponents());

        // Delete existing application if it exists
        if(device.getService(name) != null) {
            ApplicationService applicationService = (ApplicationService) device.getService(name);

            if (ApplicationStatus.ACTIVE.equals(applicationService.getResource(AppConstant.STATUS)
                    .getAttribute(DataResource.VALUE).getValue())) {
                if(mediator.isErrorLoggable()) {
                    mediator.error("The application " + name + " is active. Unable to update the application.");
                }

                throw new InvalidApplicationException("The application " + name + " is active. " +
                        "Unable to update the application.");
            }

            device.removeService(applicationService.getName());
        }

        // Create the sNa application service
        ApplicationService applicationService = (ApplicationService) device.addService(name);
        Application application;

        try {
            application = ApplicationFactory.createApplication(mediator, appContainer, applicationService);
        } catch (ApplicationFactoryException e) {
            if(mediator.isErrorLoggable()) {
                mediator.error("Unable to create the application " + name + " > " + e.getMessage());
            }

            device.removeService(name);

            throw e;
        }

        try {
            applicationService.createSnaService(appContainer, application);
        } catch (InvalidValueException e) {
            e.printStackTrace();
        }

        if(mediator.isInfoLoggable()) {
            mediator.info("Application " + name + " successfully installed.");
        }

        jsonObjects.push(new JSONObject().put("message", "Application " + name + " successfully installed."));

        return null;
    }
}
