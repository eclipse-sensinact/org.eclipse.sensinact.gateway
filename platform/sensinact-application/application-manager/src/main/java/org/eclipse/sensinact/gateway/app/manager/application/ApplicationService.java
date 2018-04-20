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

package org.eclipse.sensinact.gateway.app.manager.application;

import org.eclipse.sensinact.gateway.app.api.lifecycle.ApplicationStatus;
import org.eclipse.sensinact.gateway.app.manager.AppConstant;
import org.eclipse.sensinact.gateway.app.manager.application.persistence.DependencyManager;
import org.eclipse.sensinact.gateway.app.manager.application.persistence.DependencyManagerCallback;
import org.eclipse.sensinact.gateway.app.manager.json.AppContainer;
import org.eclipse.sensinact.gateway.app.manager.osgi.AppServiceMediator;
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.common.primitive.Modifiable;
import org.eclipse.sensinact.gateway.core.*;
import org.eclipse.sensinact.gateway.core.method.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * This class is the service representation of the application in sensiNact data json.
 * It wraps the application.
 *
 * @see ServiceImpl
 *
 * @author Remi Druilhe
 */
public class ApplicationService extends ServiceImpl 
{
    private static Logger LOG= LoggerFactory.getLogger(ApplicationService.class);
    private Application application;

    /**
     * The constructor creates the components and the resources in the sNa json.
     * @param modelInstance the json instance of the AppManager
     * @param name the name of the new application
     * @param device the appmanager device
     * @throws InvalidServiceException
     */
    public ApplicationService(ModelInstance<?> modelInstance, 
    	String name, ServiceProviderImpl device)
           throws InvalidServiceException 
    {
        super(modelInstance, name, device);
    }

    /**
     * Create the sNa resources related to this service, the components of the application and its watchdog
     * @param appContainer the application container
     * @param application the built application
     * @throws InvalidResourceException
     * @throws InvalidValueException
     */
    public final void createSnaService(AppContainer appContainer, final Application application)
            throws InvalidResourceException, InvalidValueException
    {
    	AccessMethod.Type act = AccessMethod.Type.valueOf(AccessMethod.ACT);
        this.application = application;

        final ResourceImpl startResource = this.addActionResource(AppConstant.START,
        		ActionResource.class);
        
        startResource.registerExecutor(new Signature(super.modelInstance.mediator(),
        		act, null, null), new AppStartExecutor(this),
                AccessMethodExecutor.ExecutionPolicy.AFTER);

        final ResourceImpl stopResource = this.addActionResource(AppConstant.STOP,
        		ActionResource.class);
        
        stopResource.registerExecutor(new Signature(super.modelInstance.mediator(), act,
        		null, null), new AppStopExecutor(this),
                AccessMethodExecutor.ExecutionPolicy.AFTER);

        // TODO: hide this resource + it should not be used by users/admins
        ResourceImpl exceptionResource = this.addActionResource(AppConstant.EXCEPTION, 
        		ActionResource.class);
        
        exceptionResource.registerExecutor(
                new Signature(super.modelInstance.mediator(), act, null, null),
                new AppExceptionExecutor(this),
                AccessMethodExecutor.ExecutionPolicy.AFTER);

        ResourceImpl uninstallResource = ((ServiceProviderImpl) super.parent).getAdminService()
                .getResource(AppConstant.UNINSTALL);
        
        ResourceImpl uninstallLinkedResource = this.addLinkedActionResource(
        		AppConstant.UNINSTALL,  uninstallResource, false);

        LinkedActMethod method = (LinkedActMethod) 
        		uninstallLinkedResource.getAccessMethod(
        		act);

        method.createShortcut(new Signature(super.modelInstance.mediator(),
        		act, new Class[]{String.class}, null),
                new Shortcut(super.modelInstance.mediator(),act, new Class[]{},  null,
                new HashMap<Integer, Parameter>()
                {{
                    this.put(0, new Parameter(
                        ApplicationService.super.modelInstance.mediator(), 
                            "name", String.class, name));
                }}));

        ResourceImpl status = this.addDataResource(StateVariableResource.class,
                AppConstant.STATUS,
                ApplicationStatus.class,
                ApplicationStatus.INSTALLED);
        
        status.addAttribute(new Attribute(super.modelInstance.mediator(), status, 
        		AppConstant.STATUS_MESSAGE, String.class,  "Application installed",
                Modifiable.UPDATABLE, false));

        AppLifecycleTrigger appLifecycleTrigger = new AppLifecycleTrigger(this);
        this.addActionTrigger(AppConstant.START,
                AppConstant.STATUS,
                new Signature(super.modelInstance.mediator(), act, null, null),
                appLifecycleTrigger,
                AccessMethodExecutor.ExecutionPolicy.BEFORE);
        this.addActionTrigger(AppConstant.START,
                AppConstant.STATUS,
                new Signature(super.modelInstance.mediator(), act, null, null),
                appLifecycleTrigger,
                AccessMethodExecutor.ExecutionPolicy.AFTER);
        this.addActionTrigger(AppConstant.STOP,
                AppConstant.STATUS,
                new Signature(super.modelInstance.mediator(), act, null, null),
                appLifecycleTrigger,
                AccessMethodExecutor.ExecutionPolicy.AFTER);
        this.addActionTrigger(AppConstant.UNINSTALL,
                AppConstant.STATUS,
                new Signature(super.modelInstance.mediator(), act, null, null),
                appLifecycleTrigger,
                AccessMethodExecutor.ExecutionPolicy.BEFORE);
        this.addActionTrigger(AppConstant.EXCEPTION,
                AppConstant.STATUS,
                new Signature(super.modelInstance.mediator(), act, null, null),
                appLifecycleTrigger,
                AccessMethodExecutor.ExecutionPolicy.AFTER);

        this.addDataResource(PropertyResource.class,
                AppConstant.CONTENT,
                JSONObject.class,
                new JSONObject(appContainer.getJSON()));
        this.addDataResource(PropertyResource.class,
                AppConstant.RESET_ON_STOP,
                boolean.class,
                appContainer.getInitialize().getOptions().getResetOnStop());
        this.addDataResource(PropertyResource.class,
                AppConstant.AUTORESTART,
                boolean.class,
                appContainer.getInitialize().getOptions().getAutorestart());

        // Creation of the AppResourceLifecycleWatchdog
        AppWatchdogExecutor appWatchdogExecutor = new AppWatchdogExecutor(
                (AppServiceMediator) super.modelInstance.mediator(), this,
                appContainer.getResourceUris());
        startResource.registerExecutor(new Signature(super.modelInstance.mediator(), 
        		act, null, null), appWatchdogExecutor,
                AccessMethodExecutor.ExecutionPolicy.BEFORE);
        stopResource.registerExecutor(new Signature(super.modelInstance.mediator(), 
        		act, null, null), appWatchdogExecutor,
                AccessMethodExecutor.ExecutionPolicy.AFTER);

        final Collection<String> dependenciesURI=new ArrayList<String>(appContainer.getResourceUris());

        if(appContainer.getInitialize().getOptions().getAutorestart()){

            LOG.debug("Application autostart option is activated, instantiating dependency manager");

            DependencyManager dm=new DependencyManager(application.getName(), modelInstance.mediator(), dependenciesURI, new DependencyManagerCallback() {
                @Override
                public void ready(String applicationName) {
                    application.doStart();
                }

                @Override
                public void unready(String applicationName) {
                    try {
                        application.doStop();
                    }catch(Exception e){
                        //We can ignore an error here, the application might have been stop by the watch doc mechanism
                    }

                }
            });

            dm.start();

        }else {
            LOG.debug("Application autostart option is NOT activated");
        }

        /*
        super.modelInstance.mediator().callService(Core.class,
                new Executable<Core,Void>()
                {
                    @Override
                    public Void execute(Core service)
                            throws Exception
                    {
                        for(String resourceUri :dependenciesURI)
                        {
                            Mediator mediator=ApplicationService.this.modelInstance.mediator();
                            final SnaFilter filter = new SnaFilter(mediator, resourceUri);
                            filter.addHandledType(SnaMessage.Type.LIFECYCLE);

                            AppResourceLifecycleWatchDog.this.registrations.add(
                            service.registerAgent(mediator, new AppResourceLifecycleWatchDog.AppResourceLifeCycleSnaAgent(mediator, AppResourceLifecycleWatchDog.this), filter)
                            );
                        }
                        return null;
                    }
                });*/

    }

    /**
     * Get the application manager
     * @return the application manager
     */
    public Application getApplication() {
        return application;
    }
}
