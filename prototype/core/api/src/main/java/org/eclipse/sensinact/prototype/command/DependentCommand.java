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
package org.eclipse.sensinact.prototype.command;

import static org.eclipse.sensinact.prototype.command.GatewayThread.getGatewayThread;

import org.eclipse.sensinact.prototype.model.SensinactModelManager;
import org.eclipse.sensinact.prototype.twin.SensinactDigitalTwin;
import org.osgi.util.promise.Deferred;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

/**
 * A {@link DependentCommand} provides a simple way to write a command which
 * depends upon the result of a previous parent command. This command will
 * correctly delay execution of the dependent command until the parent command
 * has completed. It will do this without blocking the Gateway Thread.
 *
 * Notifications will not be delivered until the child command has completed.
 *
 * @param <P>
 * @param <T>
 */
public abstract class DependentCommand<P, T> extends AbstractSensinactCommand<T> {

    private AbstractSensinactCommand<P> parent;

    @Override
    protected Promise<T> call(SensinactDigitalTwin twin, SensinactModelManager modelMgr,
            PromiseFactory promiseFactory) {
        Promise<P> parentResult = safeCall(parent, twin, modelMgr, promiseFactory);
        if (parentResult.isDone()) {
            return safeCall(() -> call(parentResult, twin, modelMgr, promiseFactory), promiseFactory);
        } else {
            GatewayThread gateway = getGatewayThread();
            Deferred<T> d = promiseFactory.deferred();

            parentResult.onResolve(() -> d.resolveWith(gateway.execute(new ChildCommand(parentResult))));

            return d.getPromise();
        }
    }

    protected abstract Promise<T> call(Promise<P> parentResult, SensinactDigitalTwin twin,
            SensinactModelManager modelMgr, PromiseFactory pf);

    private class ChildCommand extends AbstractSensinactCommand<T> {

        private final Promise<P> parentResult;

        public ChildCommand(Promise<P> parentResult) {
            super(DependentCommand.this.getAccumulator());
            this.parentResult = parentResult;
        }

        @Override
        protected Promise<T> call(SensinactDigitalTwin twin, SensinactModelManager modelMgr,
                PromiseFactory promiseFactory) {
            return DependentCommand.this.call(parentResult, twin, modelMgr, promiseFactory);
        }
    }
}
