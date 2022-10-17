/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.app.manager.application;

import org.eclipse.sensinact.gateway.app.api.lifecycle.ApplicationStatus;
import org.eclipse.sensinact.gateway.app.manager.AppConstant;
import org.eclipse.sensinact.gateway.app.manager.json.AppContainer;
import org.eclipse.sensinact.gateway.app.manager.osgi.AppServiceMediator;
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.common.primitive.Modifiable;
import org.eclipse.sensinact.gateway.core.ActionResource;
import org.eclipse.sensinact.gateway.core.Attribute;
import org.eclipse.sensinact.gateway.core.InvalidResourceException;
import org.eclipse.sensinact.gateway.core.InvalidServiceException;
import org.eclipse.sensinact.gateway.core.ModelInstance;
import org.eclipse.sensinact.gateway.core.PropertyResource;
import org.eclipse.sensinact.gateway.core.ResourceImpl;
import org.eclipse.sensinact.gateway.core.ServiceImpl;
import org.eclipse.sensinact.gateway.core.ServiceProviderImpl;
import org.eclipse.sensinact.gateway.core.StateVariableResource;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.method.AccessMethodExecutor;
import org.eclipse.sensinact.gateway.core.method.LinkedActMethod;
import org.eclipse.sensinact.gateway.core.method.Parameter;
import org.eclipse.sensinact.gateway.core.method.Shortcut;
import org.eclipse.sensinact.gateway.core.method.Signature;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.json.JsonObject;

import java.util.HashMap;

/**
 * This class is the service representation of the application in sensiNact data json.
 * It wraps the application.
 *
 * @author Remi Druilhe
 * @see ServiceImpl
 */
public class ApplicationService extends ServiceImpl {
    private static Logger LOG = LoggerFactory.getLogger(ApplicationService.class);
    private Application application;

    /**
     * The constructor creates the components and the resources in the sNa json.
     *
     * @param modelInstance the json instance of the AppManager
     * @param name          the name of the new application
     * @param device        the appmanager device
     * @throws InvalidServiceException
     */
    public ApplicationService(ModelInstance<?> modelInstance, String name, ServiceProviderImpl device) throws InvalidServiceException {
        super(modelInstance, name, device);
    }

    /**
     * Create the sNa resources related to this service, the components of the application and its watchdog
     *
     * @param appContainer the application container
     * @param application  the built application
     * @throws InvalidResourceException
     * @throws InvalidValueException
     */
    public final void createSnaService(final AppContainer appContainer, final Application application) throws InvalidResourceException, InvalidValueException {
        AccessMethod.Type act = AccessMethod.Type.valueOf(AccessMethod.ACT);
        this.application = application;
        final ResourceImpl startResource = this.addActionResource(AppConstant.START, ActionResource.class);

        startResource.registerExecutor(new Signature(super.modelInstance.mediator(), act, null, null), new AppStartExecutor(this), AccessMethodExecutor.ExecutionPolicy.AFTER);
        final ResourceImpl stopResource = this.addActionResource(AppConstant.STOP, ActionResource.class);

        stopResource.registerExecutor(new Signature(super.modelInstance.mediator(), act, null, null), new AppStopExecutor(this), AccessMethodExecutor.ExecutionPolicy.AFTER);
        // TODO: hide this resource + it should not be used by users/admins
        ResourceImpl exceptionResource = this.addActionResource(AppConstant.EXCEPTION, ActionResource.class);

        exceptionResource.registerExecutor(new Signature(super.modelInstance.mediator(), act, null, null), new AppExceptionExecutor(this), AccessMethodExecutor.ExecutionPolicy.AFTER);
        ResourceImpl uninstallResource = ((ServiceProviderImpl) super.parent).getAdminService().getResource(AppConstant.UNINSTALL);

        ResourceImpl uninstallLinkedResource = this.addLinkedActionResource(AppConstant.UNINSTALL, uninstallResource, false);
        LinkedActMethod method = (LinkedActMethod) uninstallLinkedResource.getAccessMethod(act);
        method.createShortcut(new Signature(super.modelInstance.mediator(), act, new Class[]{String.class}, null), new Shortcut(super.modelInstance.mediator(), act, new Class[]{}, null, new HashMap<Integer, Parameter>() {
			private static final long serialVersionUID = 1L;
			{
            this.put(0, new Parameter(ApplicationService.super.modelInstance.mediator(), "name", String.class, name));
        }}));
        ResourceImpl status = this.addDataResource(StateVariableResource.class, AppConstant.STATUS, ApplicationStatus.class, ApplicationStatus.INSTALLED);

        status.addAttribute(new Attribute(super.modelInstance.mediator(), status, AppConstant.STATUS_MESSAGE, String.class, "Application installed", Modifiable.UPDATABLE, false));
        AppLifecycleTrigger appLifecycleTrigger = new AppLifecycleTrigger(this);
        this.addTrigger(AppConstant.START, AppConstant.STATUS, new Signature(super.modelInstance.mediator(), act, null, null), appLifecycleTrigger, AccessMethodExecutor.ExecutionPolicy.BEFORE);
        this.addTrigger(AppConstant.START, AppConstant.STATUS, new Signature(super.modelInstance.mediator(), act, null, null), appLifecycleTrigger, AccessMethodExecutor.ExecutionPolicy.AFTER);
        this.addTrigger(AppConstant.STOP, AppConstant.STATUS, new Signature(super.modelInstance.mediator(), act, null, null), appLifecycleTrigger, AccessMethodExecutor.ExecutionPolicy.AFTER);
        this.addTrigger(AppConstant.UNINSTALL, AppConstant.STATUS, new Signature(super.modelInstance.mediator(), act, null, null), appLifecycleTrigger, AccessMethodExecutor.ExecutionPolicy.BEFORE);
        this.addTrigger(AppConstant.EXCEPTION, AppConstant.STATUS, new Signature(super.modelInstance.mediator(), act, null, null), appLifecycleTrigger, AccessMethodExecutor.ExecutionPolicy.AFTER);
        this.addDataResource(PropertyResource.class, AppConstant.CONTENT, JsonObject.class, JsonProviderFactory.readObject(appContainer.getJSON()));
        this.addDataResource(PropertyResource.class, AppConstant.RESET_ON_STOP, boolean.class, appContainer.getInitialize().getOptions().getResetOnStop());
        this.addDataResource(PropertyResource.class, AppConstant.AUTORESTART, boolean.class, appContainer.getInitialize().getOptions().getAutoStart());
        // Creation of the AppResourceLifecycleWatchdog
        AppWatchdogExecutor appWatchdogExecutor = new AppWatchdogExecutor((AppServiceMediator) super.modelInstance.mediator(), this, appContainer.getResourceUris());
        startResource.registerExecutor(new Signature(super.modelInstance.mediator(), act, null, null), appWatchdogExecutor, AccessMethodExecutor.ExecutionPolicy.BEFORE);
        stopResource.registerExecutor(new Signature(super.modelInstance.mediator(), act, null, null), appWatchdogExecutor, AccessMethodExecutor.ExecutionPolicy.AFTER);
        if (appContainer.getInitialize().getOptions().getAutoStart()) {
            LOG.debug("Application autostart option is activated, instantiating dependency manager");
        } else {
            LOG.debug("Application autostart option is NOT activated");
        }
    }

    /**
     * Get the application manager
     *
     * @return the application manager
     */
    public Application getApplication() {
        return application;
    }

    @Override
    public void stop() {
        super.stop();
        application.stop();
    }
}
