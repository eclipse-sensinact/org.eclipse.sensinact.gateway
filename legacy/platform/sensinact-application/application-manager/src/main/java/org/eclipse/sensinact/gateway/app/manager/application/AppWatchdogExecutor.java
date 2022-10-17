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

import org.eclipse.sensinact.gateway.app.api.exception.ResourceNotFoundException;
import org.eclipse.sensinact.gateway.app.api.lifecycle.ApplicationStatus;
import org.eclipse.sensinact.gateway.app.manager.AppConstant;
import org.eclipse.sensinact.gateway.app.manager.osgi.AppServiceMediator;
import org.eclipse.sensinact.gateway.app.manager.watchdog.AppResourceLifecycleWatchDog;
import org.eclipse.sensinact.gateway.core.Attribute;
import org.eclipse.sensinact.gateway.core.DataResource;
import org.eclipse.sensinact.gateway.core.ResourceImpl;
import org.eclipse.sensinact.gateway.core.message.SnaErrorMessage;
import org.eclipse.sensinact.gateway.core.method.AccessMethodExecutor;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponseBuilder;

import java.util.Collection;

class AppWatchdogExecutor implements AccessMethodExecutor {
    private final ApplicationService service;
    private final AppResourceLifecycleWatchDog resourceWatchDog;

    AppWatchdogExecutor(AppServiceMediator mediator, ApplicationService service, Collection<String> resources) {
        this.service = service;
        this.resourceWatchDog = new AppResourceLifecycleWatchDog(mediator, service, resources);
    }

    /**
     * @see Executable#execute(java.lang.Object)
     */
    public Void execute(AccessMethodResponseBuilder snaResult) throws Exception {
        String uri = snaResult.getPath();
        ApplicationStatus status = getApplicationState(service.getResource(AppConstant.STATUS));
        if (uri.endsWith(AppConstant.START)) {
            if (!snaResult.hasError()) {
                if (ApplicationStatus.INSTALLED.equals(status)) {
                    SnaErrorMessage message = resourceWatchDog.start(service.getApplication().getSession());
                    if (message != null) {
                        snaResult.registerException(new ResourceNotFoundException("Unable to start the application: " + message.getJSON()));
                    }
                }
            }
        } else if (uri.endsWith(AppConstant.STOP)) {
            if (ApplicationStatus.ACTIVE.equals(status)) {
                SnaErrorMessage message = this.resourceWatchDog.stop(service.getApplication().getSession());
                if (message != null) {
                    snaResult.registerException(new ResourceNotFoundException("Unable to stop the application: " + message.getJSON()));
                }
            }
        }
        return null;
    }

    /**
     * Gets the current state of the application.
     */
    private ApplicationStatus getApplicationState(ResourceImpl state) {
        if (state != null) {
            Attribute stateAttribute = state.getAttribute(DataResource.VALUE);
            if (stateAttribute != null) {
                return (ApplicationStatus) stateAttribute.getValue();
            }
        }
        return null;
    }
}
