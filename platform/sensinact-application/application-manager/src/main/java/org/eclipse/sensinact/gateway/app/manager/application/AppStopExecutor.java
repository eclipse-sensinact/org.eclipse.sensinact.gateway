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
package org.eclipse.sensinact.gateway.app.manager.application;

import org.eclipse.sensinact.gateway.app.api.exception.ResourceNotFoundException;
import org.eclipse.sensinact.gateway.app.api.lifecycle.ApplicationStatus;
import org.eclipse.sensinact.gateway.app.manager.AppConstant;
import org.eclipse.sensinact.gateway.core.Attribute;
import org.eclipse.sensinact.gateway.core.DataResource;
import org.eclipse.sensinact.gateway.core.ResourceImpl;
import org.eclipse.sensinact.gateway.core.message.SnaErrorMessage;
import org.eclipse.sensinact.gateway.core.method.AccessMethodExecutor;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponseBuilder;
import org.json.JSONObject;

/**
 * @author Remi Druilhe
 * @see AccessMethodExecutor
 */
class AppStopExecutor implements AccessMethodExecutor {
    private final ApplicationService service;

    AppStopExecutor(ApplicationService service) {
        this.service = service;
    }

    /**
     * @see Executable#execute(java.lang.Object)
     */
    public Void execute(AccessMethodResponseBuilder jsonObjects) throws Exception {
        ApplicationStatus status = getApplicationState(service.getResource(AppConstant.STATUS));
        if (ApplicationStatus.ACTIVE.equals(status)) {
            Application application = service.getApplication();
            SnaErrorMessage message = application.stop();
            if (message.getType() == SnaErrorMessage.Error.NO_ERROR) {
                jsonObjects.push(new JSONObject().put("message", "Application " + service.getName() + " stopped"));
            } else {
                jsonObjects.registerException(new ResourceNotFoundException("Unable to stop the application: " + message.getJSON()));
            }
        } else {
            jsonObjects.push(new JSONObject().put("message", "Unable to stop the application " + service.getName()));
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
