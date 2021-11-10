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

import org.eclipse.sensinact.gateway.app.api.lifecycle.ApplicationStatus;
import org.eclipse.sensinact.gateway.app.api.persistence.ApplicationPersistenceService;
import org.eclipse.sensinact.gateway.app.api.persistence.listener.ApplicationAvailabilityListenerAbstract;
import org.eclipse.sensinact.gateway.app.manager.AppConstant;
import org.eclipse.sensinact.gateway.app.manager.application.ApplicationService;
import org.eclipse.sensinact.gateway.app.manager.osgi.AppServiceMediator;
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.core.ActionResource;
import org.eclipse.sensinact.gateway.core.DataResource;
import org.eclipse.sensinact.gateway.core.InvalidResourceException;
import org.eclipse.sensinact.gateway.core.InvalidServiceException;
import org.eclipse.sensinact.gateway.core.InvalidServiceProviderException;
import org.eclipse.sensinact.gateway.core.ModelConfiguration;
import org.eclipse.sensinact.gateway.core.ModelConfigurationBuilder;
import org.eclipse.sensinact.gateway.core.ModelInstance;
import org.eclipse.sensinact.gateway.core.ModelInstanceBuilder;
import org.eclipse.sensinact.gateway.core.PropertyResource;
import org.eclipse.sensinact.gateway.core.ResourceImpl;
import org.eclipse.sensinact.gateway.core.ServiceImpl;
import org.eclipse.sensinact.gateway.core.ServiceProviderImpl;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.method.AccessMethodExecutor;
import org.eclipse.sensinact.gateway.core.method.Signature;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This factory create the AppManager service provider and its resources in the admin service.
 *
 * @author Remi Druilhe
 */
public class AppManagerFactory extends ApplicationAvailabilityListenerAbstract {
    private static Logger LOG = LoggerFactory.getLogger(AppManagerFactory.class);
    private final ModelInstance<ModelConfiguration> modelInstance;
    private final ServiceProviderImpl serviceProvider;
    private final AppJsonSchemaListener jsonSchemaListener;
    private final ApplicationPersistenceService persistenceService;
    private AppInstallExecutor installExecutor;
    private AppUninstallExecutor uninstallExecutor;

    /**
     * Constructor of the AppManager
     *
     * @param mediator the mediator
     * @throws InvalidResourceException
     * @throws InvalidServiceProviderException
     * @throws InvalidServiceException
     * @throws InvalidValueException
     */
    public AppManagerFactory(AppServiceMediator mediator, ApplicationPersistenceService persistenceService) throws InvalidResourceException, InvalidServiceProviderException, InvalidServiceException, InvalidValueException {
        this.persistenceService = persistenceService;
        
        ModelConfiguration config = new ModelConfigurationBuilder(mediator, 
        		ModelConfiguration.class, ModelInstance.class
        		).withStartAtInitializationTime(true).build();
        this.modelInstance = new ModelInstanceBuilder(mediator).build(
        		AppConstant.DEVICE_NAME, null, config);
        
        this.modelInstance.configuration().setServiceImplmentationType(ApplicationService.class);

        this.serviceProvider = this.modelInstance.getRootElement();
        ServiceImpl adminService = this.serviceProvider.getAdminService();
        ResourceImpl installResource = adminService.addActionResource(AppConstant.INSTALL, ActionResource.class);
        AccessMethod.Type act = AccessMethod.Type.valueOf(AccessMethod.ACT);
        installExecutor = new AppInstallExecutor(mediator, this.serviceProvider, persistenceService);
        installResource.registerExecutor(new Signature(mediator, act, new Class[]{String.class, JSONObject.class}, null), installExecutor, AccessMethodExecutor.ExecutionPolicy.AFTER);
        //installResource.getAccessMethod(act).invoke()
        ResourceImpl uninstallResource = adminService.addActionResource(AppConstant.UNINSTALL, ActionResource.class);
        uninstallExecutor = new AppUninstallExecutor(this.serviceProvider, persistenceService);
        uninstallResource.registerExecutor(new Signature(mediator, act, new Class[]{String.class}, null), uninstallExecutor, AccessMethodExecutor.ExecutionPolicy.AFTER);
        ResourceImpl resource = adminService.addDataResource(PropertyResource.class, AppConstant.KEYWORDS, JSONArray.class, null);
        this.jsonSchemaListener = new AppJsonSchemaListener(mediator, resource);
        this.persistenceService.registerServiceAvailabilityListener(uninstallExecutor);
        this.persistenceService.registerServiceAvailabilityListener(installExecutor);
    }

    /**
     * Uninstall properly the AppManager service provider
     */
    public void deleteAppManager() throws Exception {
        deleteApplication(null);
        modelInstance.unregister();
        jsonSchemaListener.stop();
    }

    /**
     * stop and delete application container
     *
     * @param name name of application to be deleted , or all of them if null
     * @throws Exception
     */
    public void deleteApplication(String name) throws Exception {
        for (ServiceImpl service : serviceProvider.getServices()) {
            if (service instanceof ApplicationService) {
                ApplicationService applicationContainer = ((ApplicationService) service);
                if (name == null || applicationContainer.getApplication().getName().equals(name)) {
                    applicationContainer.getApplication().stop();
                }
            }
        }
    }

    private ApplicationService getApplicationService(String name) {
        for (ServiceImpl service : serviceProvider.getServices()) {
            if (service instanceof ApplicationService) {
                ApplicationService applicationContainer = ((ApplicationService) service);
                if (name != null && applicationContainer.getApplication().getName().equals(name)) {
                    return applicationContainer;
                }
            }
        }
        return null;
    }

    @Override
    public void applicationFound(String applicationName, String content) {
        try {
            LOG.info("Installing new application '{}'", applicationName);
            installExecutor.install(applicationName, new JSONObject(content).getJSONArray("parameters").getJSONObject(1).getJSONObject("value"));
        } catch (Exception e) {
            LOG.error("Failed to install application '{}'", applicationName, e);
        }
    }

    @Override
    public void applicationChanged(String applicationName, String content) {
        try {
            LOG.info("Updating application content '{}'", applicationName);
            ApplicationService as = getApplicationService(applicationName);
            as.getApplication().stop();
            as.getResource(AppConstant.STATUS).getAttribute(DataResource.VALUE).setValue(ApplicationStatus.INSTALLED);
            as.stop();
            installExecutor.install(applicationName, new JSONObject(content).getJSONArray("parameters").getJSONObject(1).getJSONObject("value"));
        } catch (Exception e) {
            LOG.error("Failed to uninstall application {}", applicationName, e);
        }
    }

    @Override
    public void applicationRemoved(String applicationName) {
        try {
            LOG.info("Removing application '{}'", applicationName);
            //deleteApplication(applicationName);
            //uninstallExecutor.uninstall(applicationName);
            ApplicationService as = getApplicationService(applicationName);
            as.getResource(AppConstant.STATUS).getAttribute(DataResource.VALUE).setValue(ApplicationStatus.UNINSTALLED);
            as.getApplication().stop();
            as.getApplication().uninstall();
            as.stop();
        } catch (Exception e) {
            LOG.error("Failed to uninstall application", applicationName, e);
        }
    }
}
