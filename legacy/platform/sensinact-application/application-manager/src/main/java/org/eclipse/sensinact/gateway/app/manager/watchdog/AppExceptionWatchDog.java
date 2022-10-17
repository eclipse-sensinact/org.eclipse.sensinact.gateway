/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.app.manager.watchdog;

import org.eclipse.sensinact.gateway.app.manager.application.ApplicationService;
import org.eclipse.sensinact.gateway.app.manager.osgi.AppServiceMediator;
import org.eclipse.sensinact.gateway.core.Session;
import org.eclipse.sensinact.gateway.core.message.SnaErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class triggers an alert that stops the application when an exception is throw
 * during the runtime of the application
 *
 * @author Remi Druilhe
 * @see AbstractAppWatchDog
 */
public class AppExceptionWatchDog extends AbstractAppWatchDog implements Thread.UncaughtExceptionHandler {
    private static Logger LOG = LoggerFactory.getLogger(AppExceptionWatchDog.class);

    /**
     * Constructor
     *
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
        LOG.error("Watch dog exception", exception);
        super.alert(exception.getMessage());
    }
}
