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
import org.eclipse.sensinact.gateway.app.api.plugin.PluginInstaller;
import org.eclipse.sensinact.gateway.app.manager.AppConstant;
import org.eclipse.sensinact.gateway.app.manager.application.Application;
import org.eclipse.sensinact.gateway.app.manager.checker.ArchitectureChecker;
import org.eclipse.sensinact.gateway.app.manager.factory.ApplicationFactory;
import org.eclipse.sensinact.gateway.app.manager.application.ApplicationService;
import org.eclipse.sensinact.gateway.app.manager.osgi.AppServiceMediator;
import org.eclipse.sensinact.gateway.app.manager.osgi.PluginsProxy;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.core.DataResource;
import org.eclipse.sensinact.gateway.core.InvalidResourceException;
import org.eclipse.sensinact.gateway.core.InvalidServiceException;
import org.eclipse.sensinact.gateway.core.ServiceProviderImpl;
import org.eclipse.sensinact.gateway.core.method.AccessMethodExecutor;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponseBuilder;

import org.eclipse.sensinact.gateway.app.manager.json.AppContainer;
import org.json.JSONObject;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

/**
 * @see AccessMethodExecutor
 *
 * @author Remi Druilhe
 */
public class AppInstallExecutor implements AccessMethodExecutor {

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
    public Void execute(AccessMethodResponseBuilder jsonObjects) throws Exception {
        String name = (String) jsonObjects.getParameter(0);
        JSONObject content = (JSONObject) jsonObjects.getParameter(1);
        install(name,content);

        jsonObjects.push(new JSONObject().put("message", "Application " + name + " successfully installed."));

        return null;
    }

    public Void install(final String name,JSONObject content) throws Exception{

        // Test the JSON parameters
        if(name == null) {
            throw new InvalidApplicationException("Unable to install the application: application 'name' is null");
        }

        if(content == null) {
            throw new InvalidApplicationException("Unable to install the application: application 'content' is null");
        }

        // Validate the JSON application against the JSON schema of the application
        /*try {
            JsonValidator.validateApplication(mediator, content);
        } catch (ValidationException exception) {
            if(mediator.isErrorLoggable()) {
                mediator.error("The JSON of the application is not valid", exception);
            }

            throw exception;
        }*/

        // Validate the JSON components against the JSON schema of each components
        /*try {
            JsonValidator.validateFunctionsParameters(mediator, content.getJSONArray(AppJsonConstant.APPLICATION));
        } catch (ValidationException exception) {
            if(mediator.isErrorLoggable()) {
                mediator.error("The JSON of the application is not valid", exception);
            }

            throw exception;
        } catch (FileNotFoundException exception) {
            if(mediator.isErrorLoggable()) {
                mediator.error("Unable to find the JSON schema", exception);
            }

            throw exception;
        }*/

        // Transform JSON to Java object
        final AppContainer appContainer = new AppContainer(mediator, name, content);

        // Check that the application is correctly constructed
        ArchitectureChecker.checkApplication(appContainer.getApplicationName(), appContainer.getComponents());

        // Delete existing application if it exists
        if(device.getService(name) != null) {
            final ApplicationService applicationService = (ApplicationService) device.getService(name);

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
        ServiceReference[] references=mediator.getContext().getServiceReferences(PluginInstaller.class.getCanonicalName(),"(objectClass=*)");

        if(references==null){

            mediator.getContext().addServiceListener(new ServiceListener() {
                @Override
                public void serviceChanged(ServiceEvent serviceEvent) {
                    startApplication(name,appContainer, mediator);
                }
            }, PluginsProxy.APP_INSTALL_HOOK_FILTER);

        }else {
            startApplication(name,appContainer, mediator);
        }

        return null;

    }

    private void startApplication(String name,AppContainer appContainer, AppServiceMediator mediator){
        ApplicationService applicationService=null;
        Application application=null;
        try {
            applicationService = (ApplicationService) device.addService(name);
            application = ApplicationFactory.createApplication(mediator, appContainer, applicationService);
        } catch (ApplicationFactoryException e) {
            if(mediator.isErrorLoggable()) {
                mediator.error("Unable to create the application " + name + " > " + e.getMessage());
            }

            device.removeService(name);

            e.printStackTrace();
        } catch (InvalidValueException e) {
            e.printStackTrace();
        } catch (InvalidServiceException e) {
            e.printStackTrace();
        } catch (InvalidResourceException e) {
            e.printStackTrace();
        }

        try {
            applicationService.createSnaService(appContainer, application);
        } catch (InvalidValueException e) {
            e.printStackTrace();
        } catch (InvalidResourceException e) {
            e.printStackTrace();
        }

        if(mediator.isInfoLoggable()) {
            mediator.info("Application " + name + " successfully installed.");
        }
    }
}
