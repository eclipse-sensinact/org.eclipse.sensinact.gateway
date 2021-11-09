/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.app.manager.internal;

import org.eclipse.sensinact.gateway.app.api.exception.ApplicationFactoryException;
import org.eclipse.sensinact.gateway.app.api.exception.InvalidApplicationException;
import org.eclipse.sensinact.gateway.app.api.lifecycle.ApplicationStatus;
import org.eclipse.sensinact.gateway.app.api.persistence.ApplicationPersistenceService;
import org.eclipse.sensinact.gateway.app.api.persistence.listener.ApplicationAvailabilityListenerAbstract;
import org.eclipse.sensinact.gateway.app.api.plugin.PluginInstaller;
import org.eclipse.sensinact.gateway.app.manager.AppConstant;
import org.eclipse.sensinact.gateway.app.manager.application.Application;
import org.eclipse.sensinact.gateway.app.manager.application.ApplicationService;
import org.eclipse.sensinact.gateway.app.manager.application.dependency.DependencyManager;
import org.eclipse.sensinact.gateway.app.manager.application.dependency.DependencyManagerCallback;
import org.eclipse.sensinact.gateway.app.manager.checker.ArchitectureChecker;
import org.eclipse.sensinact.gateway.app.manager.component.Component;
import org.eclipse.sensinact.gateway.app.manager.component.ResourceDataProvider;
import org.eclipse.sensinact.gateway.app.manager.factory.ApplicationFactory;
import org.eclipse.sensinact.gateway.app.manager.json.AppContainer;
import org.eclipse.sensinact.gateway.app.manager.json.AppParameter;
import org.eclipse.sensinact.gateway.app.manager.osgi.AppServiceMediator;
import org.eclipse.sensinact.gateway.app.manager.osgi.PluginsProxy;
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.core.Attribute;
import org.eclipse.sensinact.gateway.core.DataResource;
import org.eclipse.sensinact.gateway.core.InvalidResourceException;
import org.eclipse.sensinact.gateway.core.InvalidServiceException;
import org.eclipse.sensinact.gateway.core.ServiceProviderImpl;
import org.eclipse.sensinact.gateway.core.method.AccessMethodExecutor;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponseBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

/**
 * @author Remi Druilhe
 * @see AccessMethodExecutor
 */
public class AppInstallExecutor extends ApplicationAvailabilityListenerAbstract implements AccessMethodExecutor {
    private static Logger LOG = LoggerFactory.getLogger(AppInstallExecutor.class);
    private final AppServiceMediator mediator;
    private final ServiceProviderImpl device;
    private final ApplicationPersistenceService persistenceService;
    private Boolean persist = false;

    /**
     * Constructor
     *
     * @param mediator the mediator
     * @param device   the AppManager service provider
     */
    AppInstallExecutor(AppServiceMediator mediator, ServiceProviderImpl device, ApplicationPersistenceService persistenceService) {
        this.mediator = mediator;
        this.device = device;
        this.persistenceService = persistenceService;
    }

    /**
     * @see Executable#execute(java.lang.Object)
     */
    public Void execute(AccessMethodResponseBuilder jsonObjects) throws Exception {
        String name = (String) jsonObjects.getParameter(0);
        JSONObject content = (JSONObject) jsonObjects.getParameter(1);
        //install(name,content); this will pass directly by the persistence mechanism
        if (persist) {
            JSONObject httpJSONObject = new JSONObject();
            JSONArray parametersArray = new JSONArray();
            JSONObject appName = new JSONObject();
            appName.put("name", "name");
            appName.put("type", "string");
            appName.put("value", name);
            JSONObject appContent = new JSONObject();
            appContent.put("name", "content");
            appContent.put("type", "object");
            appContent.put("value", content);
            parametersArray.put(appName);
            parametersArray.put(appContent);
            httpJSONObject.put("parameters", parametersArray);
            persistenceService.persist(new org.eclipse.sensinact.gateway.app.api.persistence.dao.Application(name, httpJSONObject));
        }
        jsonObjects.setAccessMethodObjectResult(new JSONObject().put("message", "Application " + name + " successfully installed."));
        return null;
    }

    public synchronized void install(final String name, JSONObject content) throws Exception {
        // Test the JSON parameters
        if (name == null) {
            throw new InvalidApplicationException("Unable to install the application: application 'name' is null");
        }
        if (content == null) {
            throw new InvalidApplicationException("Unable to install the application: application 'content' is null");
        }
        // Validate the JSON application against the JSON schema of the application
        /*try {
            JsonValidator.validateApplication(mediator, content);
        } catch (ValidationException exception) {
            if(LOG.isErrorEnabled()) {
                LOG.error("The JSON of the application is not valid", exception);
            }
            throw exception;
        }*/
        // Validate the JSON components against the JSON schema of each components
        /*try {
            JsonValidator.validateFunctionsParameters(mediator, content.getJSONArray(AppJsonConstant.APPLICATION));
        } catch (ValidationException exception) {
            if(LOG.isErrorEnabled()) {
                LOG.error("The JSON of the application is not valid", exception);
            }
            throw exception;
        } catch (FileNotFoundException exception) {
            if(LOG.isErrorEnabled()) {
                LOG.error("Unable to find the JSON schema", exception);
            }
            throw exception;
        }*/
        // Transform JSON to Java object
        final AppContainer appContainer = new AppContainer(mediator, name, content);
        // Check that the application is correctly constructed
        ArchitectureChecker.checkApplication(appContainer.getApplicationName(), appContainer.getComponents());
        // Delete existing application if it exists
        if (device.getService(name) != null) {
            final ApplicationService applicationService = (ApplicationService) device.getService(name);
            if (ApplicationStatus.ACTIVE.equals(applicationService.getResource(AppConstant.STATUS).getAttribute(DataResource.VALUE).getValue())) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("The application " + name + " is active. Unable to update the application.");
                }
                throw new InvalidApplicationException("The application " + name + " is active. " + "Unable to update the application.");
            }
            device.removeService(applicationService.getName());
        }
        // Create the sNa application service
        ServiceReference<?>[] references = mediator.getContext().getServiceReferences(PluginInstaller.class.getCanonicalName(), "(objectClass=*)");
        if (references == null) {
            mediator.getContext().addServiceListener(new ServiceListener() {
                @Override
                public void serviceChanged(ServiceEvent serviceEvent) {
                    startApplication(name, appContainer, mediator);
                }
            }, PluginsProxy.APP_INSTALL_HOOK_FILTER);
        } else {
            startApplication(name, appContainer, mediator);
        }
    }

    private void startApplication(String name, final AppContainer appContainer, AppServiceMediator mediator) {
        try {
            final ApplicationService applicationService = (ApplicationService) device.addService(name);
            final Application application = ApplicationFactory.createApplication(mediator, appContainer, applicationService);
            applicationService.createSnaService(appContainer, application);
            if (appContainer.getInitialize().getOptions().getAutoStart()) {
                LOG.warn("Application {} activated the SAR service", name);
                final Collection<ResourceDataProvider> dependenciesURI = new HashSet<ResourceDataProvider>(application.getResourceSubscriptions().keySet());
                final Collection<String> dependenciesURIString = new ArrayList<String>();
                for (ResourceDataProvider dp : dependenciesURI) {
                    dependenciesURIString.add(dp.getUri());
                }
                for (Map.Entry<String, Component> ac : application.getComponents().entrySet()) {
                    for (AppParameter param : ac.getValue().getFunctionParameters()) {
                        if (param.getType().equals("resource")) {
                            dependenciesURIString.add(param.getValue().toString());
                        }
                    }
                }
                DependencyManager dependencyManager = new DependencyManager(application, mediator, dependenciesURIString, new DependencyManagerCallback() {
                    @Override
                    public void ready(String applicationName) {
                        try {
                            LOG.info("Application {} is valid, resource all present", applicationName);
                            application.start();
                            Attribute attribute = applicationService.getResource(AppConstant.STATUS).getAttribute(DataResource.VALUE);
                            attribute.setValue(ApplicationStatus.ACTIVE);
                        } catch (Exception e) {
                            LOG.warn("Failed to start application {}", applicationName, e);
                        }
                    }

                    @Override
                    public void unready(String applicationName) {
                        LOG.info("Application {} is NO longer valid, resource are missing", applicationName);
                        try {
                            Attribute attribute = applicationService.getResource(AppConstant.STATUS).getAttribute(DataResource.VALUE);
                            attribute.setValue(ApplicationStatus.INSTALLED);
                            application.stop();
                            //applicationService.stop();
                        } catch (Exception e) {
                            //We can ignore an error here, the application might have been stop by the watch doc mechanism
                        }
                    }
                });
                dependencyManager.start();
            } else {
                LOG.warn("Application {} did not activate SAR service", name);
            }
        } catch (ApplicationFactoryException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Unable to create the application " + name + " > " + e.getMessage());
            }
            device.removeService(name);
            e.printStackTrace();
        } catch (InvalidValueException|InvalidServiceException|InvalidResourceException e) {
            e.printStackTrace();
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("Application " + name + " successfully installed.");
        }
    }

    @Override
    public void serviceOnline() {
        persist = true;
    }
}
