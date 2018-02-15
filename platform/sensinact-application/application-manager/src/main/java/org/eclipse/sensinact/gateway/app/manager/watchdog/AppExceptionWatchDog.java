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

import org.eclipse.sensinact.gateway.app.manager.application.ApplicationService;
import org.eclipse.sensinact.gateway.app.manager.osgi.AppServiceMediator;
import org.eclipse.sensinact.gateway.core.Session;
import org.eclipse.sensinact.gateway.core.message.SnaErrorMessage;

/**
 * This class triggers an alert that stops the application when an exception is throw
 * during the runtime of the application
 *
 * @see AbstractAppWatchDog
 *
 * @author Remi Druilhe
 */
public class AppExceptionWatchDog extends AbstractAppWatchDog implements Thread.UncaughtExceptionHandler {

    /**
     * Constructor
     * @param service the application to watch
     */
    public AppExceptionWatchDog(AppServiceMediator mediator, ApplicationService service) {
        super(mediator, service);
    }

    /**
     * @see AbstractAppWatchDog#start(Session)
     */
    public SnaErrorMessage start(Session session) {
        return null;
    }

    /**
     * @see AbstractAppWatchDog#stop(Session)
     */
    public SnaErrorMessage stop(Session session) {
        return null;
    }

    /**
     * @see Thread.UncaughtExceptionHandler#uncaughtException(java.lang.Thread, java.lang.Throwable)
     */
    public void uncaughtException(Thread thread, Throwable exception) {
        if(mediator.isErrorLoggable()) {
            mediator.error(exception.getMessage(), exception);
        }

        super.alert(exception.getMessage());
    }
}
