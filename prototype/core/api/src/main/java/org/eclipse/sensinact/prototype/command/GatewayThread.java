/*********************************************************************
* Copyright (c) 2022 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.prototype.command;

import org.eclipse.sensinact.prototype.notification.NotificationAccumulator;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

public interface GatewayThread {

    public PromiseFactory getPromiseFactory();

    public <T> Promise<T> execute(AbstractSensinactCommand<T> command);

    public NotificationAccumulator createAccumulator();

    public static GatewayThread getGatewayThread() {
        Thread currentThread = Thread.currentThread();
        if (currentThread instanceof GatewayThread) {
            return (GatewayThread) currentThread;
        } else {
            throw new IllegalStateException("Not running on the gateway thread");
        }
    }
}
