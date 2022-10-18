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

import static org.eclipse.sensinact.prototype.command.GatewayThread.getGatewayThread;

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
    protected Promise<T> call(SensinactModel model, PromiseFactory promiseFactory) {
        Promise<P> parentResult = safeCall(parent, model, promiseFactory);
        if (parentResult.isDone()) {
            return safeCall(() -> call(parentResult, model, promiseFactory), promiseFactory);
        } else {
            GatewayThread gateway = getGatewayThread();
            Deferred<T> d = promiseFactory.deferred();

            parentResult.onResolve(() -> d.resolveWith(gateway.execute(new ChildCommand(parentResult))));

            return d.getPromise();
        }
    }

    protected abstract Promise<T> call(Promise<P> parentResult, SensinactModel model, PromiseFactory pf);

    private class ChildCommand extends AbstractSensinactCommand<T> {

        private final Promise<P> parentResult;

        public ChildCommand(Promise<P> parentResult) {
            super(DependentCommand.this.getAccumulator());
            this.parentResult = parentResult;
        }

        @Override
        protected Promise<T> call(SensinactModel model, PromiseFactory promiseFactory) {
            return DependentCommand.this.call(parentResult, model, promiseFactory);
        }
    }
}
