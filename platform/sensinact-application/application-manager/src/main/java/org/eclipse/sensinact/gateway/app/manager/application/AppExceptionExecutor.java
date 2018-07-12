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

import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.method.AccessMethodExecutor;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponseBuilder;
import org.json.JSONObject;

/**
 * This class is called by the AppManager when an exception occurs at runtime
 *
 * @author Rémi Druilhe
 */
class AppExceptionExecutor implements AccessMethodExecutor {
    private final ApplicationService service;

    AppExceptionExecutor(ApplicationService service) {
        this.service = service;
    }

    /**
     * @see Executable#execute(java.lang.Object)
     */
    public Void execute(AccessMethodResponseBuilder jsonObjects) throws Exception {
        service.getApplication().stop();
        jsonObjects.push(new JSONObject().put("message", "The application " + service.getName() + " has throw an exception. Stopping it."));
        return null;
    }
}
