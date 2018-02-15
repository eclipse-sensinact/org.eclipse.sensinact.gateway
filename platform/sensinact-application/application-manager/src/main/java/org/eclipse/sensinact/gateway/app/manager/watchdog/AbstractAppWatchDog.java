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
package org.eclipse.sensinact.gateway.app.manager.watchdog;

import org.eclipse.sensinact.gateway.app.manager.AppConstant;
import org.eclipse.sensinact.gateway.app.manager.application.ApplicationService;
import org.eclipse.sensinact.gateway.app.manager.osgi.AppServiceMediator;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.Session;
import org.eclipse.sensinact.gateway.core.message.SnaErrorMessage;

/**
 * This abstract class is implemented by the watchdogs of the AppManager
 *
 * @author Remi Druilhe
 */
abstract class AbstractAppWatchDog {

    protected AppServiceMediator mediator;
    protected ApplicationService service;

    /**
     * Abstract constructor
     * @param service the application to watch
     */
    AbstractAppWatchDog(AppServiceMediator mediator, ApplicationService service) {
        this.mediator = mediator;
        this.service = service;
    }

    /**
     * Start the watchdog
     * @param session the session used to start the application
     */
    public abstract SnaErrorMessage start(Session session);

    /**
     * Stop the watchdog
     * @param session the session used to stop the application
     */
    public abstract SnaErrorMessage stop(Session session);

    /**
     * This function is used byt the various watchdogs in order to
     * trigger the UNRESOLVED status of the application when a problem occurs
     * @param message the message that triggered this alert
     */
    void alert(String message) {
        if(mediator.isDebugLoggable()) {
            mediator.debug("Application exception: " + message);
        }

        service.getResource(AppConstant.EXCEPTION).getAccessMethod(
        	AccessMethod.Type.valueOf(AccessMethod.ACT)
        		).invoke(null);
    }
}
