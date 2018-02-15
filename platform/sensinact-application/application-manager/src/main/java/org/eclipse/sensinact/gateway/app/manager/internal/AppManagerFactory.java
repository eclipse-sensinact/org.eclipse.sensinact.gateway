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

import org.eclipse.sensinact.gateway.app.manager.AppConstant;
import org.eclipse.sensinact.gateway.app.manager.application.ApplicationService;
import org.eclipse.sensinact.gateway.app.manager.osgi.AppServiceMediator;
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.core.*;
import org.eclipse.sensinact.gateway.core.method.Signature;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.method.AccessMethodExecutor;
import org.eclipse.sensinact.gateway.core.security.Sessions;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This factory create the AppManager service provider and its resources in the admin service.
 *
 * @author Remi Druilhe
 */
public class AppManagerFactory {

    private final ModelInstance<ModelConfiguration> modelInstance;
    private final ServiceProviderImpl serviceProvider;
    private final AppJsonSchemaListener jsonSchemaListener;

    /**
     * Constructor of the AppManager
     * @param mediator the mediator
     * @throws InvalidResourceException
     * @throws InvalidServiceProviderException
     * @throws InvalidServiceException
     * @throws InvalidValueException
     */
    public AppManagerFactory(AppServiceMediator mediator) 
    		throws InvalidResourceException,
            InvalidServiceProviderException,
            InvalidServiceException, InvalidValueException
    {
        this.modelInstance = new ModelInstanceBuilder(
                mediator, ModelInstance.class, ModelConfiguration.class)
                .withStartAtInitializationTime(true)
                .build(AppConstant.DEVICE_NAME, null);

        this.modelInstance.configuration().setServiceImplmentationType(
        		ApplicationService.class);
        
        this.serviceProvider = this.modelInstance.getRootElement();

        ServiceImpl adminService =  this.serviceProvider.getAdminService();

        ResourceImpl installResource = adminService.addActionResource(AppConstant.INSTALL, ActionResource.class);
        AccessMethod.Type act = AccessMethod.Type.valueOf(AccessMethod.ACT);
        installResource.registerExecutor(
                new Signature(mediator, act, 
                new Class[]{String.class, JSONObject.class}, null),
                new AppInstallExecutor(mediator, this.serviceProvider),
                AccessMethodExecutor.ExecutionPolicy.AFTER);

        ResourceImpl uninstallResource = adminService.addActionResource(AppConstant.UNINSTALL, ActionResource.class);
        
        uninstallResource.registerExecutor(
                new Signature(mediator, act, new Class[]{String.class}, null),
                new AppUninstallExecutor(mediator, this.serviceProvider),
                AccessMethodExecutor.ExecutionPolicy.AFTER);

        ResourceImpl resource = adminService.addDataResource(PropertyResource.class,
                AppConstant.KEYWORDS, JSONArray.class, null);

        this.jsonSchemaListener = new AppJsonSchemaListener(mediator, resource);
    }

    /**
     * Uninstall properly the AppManager service provider
     */
    public void deleteAppManager() throws Exception 
    {
        for(ServiceImpl service : serviceProvider.getServices())
        {
            if (service instanceof ApplicationService) 
            {
                ((ApplicationService) service).getApplication().stop();
            }
        }
        modelInstance.unregister();
        jsonSchemaListener.stop();
    }
}
