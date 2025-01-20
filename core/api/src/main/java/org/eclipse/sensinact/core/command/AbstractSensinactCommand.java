/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.core.command;

import static org.eclipse.sensinact.core.command.GatewayThread.getGatewayThread;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

public abstract class AbstractSensinactCommand<T> {

    private final AtomicBoolean canRun = new AtomicBoolean(true);

    protected AbstractSensinactCommand() {
    }

    public final Promise<T> call(SensinactDigitalTwin twin, SensinactModelManager modelMgr) throws Exception {
        GatewayThread gateway = getGatewayThread();
        if (canRun.getAndSet(false)) {
            PromiseFactory promiseFactory = gateway.getPromiseFactory();
            return safeCall(this, twin, modelMgr, promiseFactory);
        } else {
            throw new IllegalStateException("Commands can only be executed once");
        }
    }

    protected abstract Promise<T> call(SensinactDigitalTwin twin, SensinactModelManager modelMgr,
            PromiseFactory promiseFactory);

    protected static <R> Promise<R> safeCall(AbstractSensinactCommand<R> command, SensinactDigitalTwin twin,
            SensinactModelManager modelMgr, PromiseFactory pf) {
        try {
            return command.call(twin, modelMgr, pf);
        } catch (Throwable t) {
            return pf.failed(t);
        }
    }

    protected static <R> Promise<R> safeCall(Supplier<Promise<R>> supplier, PromiseFactory pf) {
        try {
            return supplier.get();
        } catch (Exception e) {
            return pf.failed(e);
        }
    }
}
