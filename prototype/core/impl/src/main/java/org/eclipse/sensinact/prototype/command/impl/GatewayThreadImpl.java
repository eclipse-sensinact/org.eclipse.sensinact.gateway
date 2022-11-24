/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.prototype.command.impl;

import static org.eclipse.sensinact.prototype.command.GatewayThread.getGatewayThread;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.sensinact.model.core.SensiNactPackage;
import org.eclipse.sensinact.prototype.command.AbstractSensinactCommand;
import org.eclipse.sensinact.prototype.command.GatewayThread;
import org.eclipse.sensinact.prototype.model.nexus.impl.ModelNexus;
import org.eclipse.sensinact.prototype.notification.NotificationAccumulator;
import org.eclipse.sensinact.prototype.notification.impl.NotificationAccumulatorImpl;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.typedevent.TypedEventBus;
import org.osgi.util.promise.Deferred;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

//TODO decide if this is the right level to be a component
@Component
public class GatewayThreadImpl extends Thread implements GatewayThread {

    private final TypedEventBus typedEventBus;

    private final ModelNexus nexusImpl;

    // TODO decide if we should just use an infinite queue
    private final BlockingQueue<WorkItem<?>> work = new ArrayBlockingQueue<>(4096);

    private final AtomicBoolean run = new AtomicBoolean(true);

    private final PromiseFactory promiseFactory = new PromiseFactory(null, null);

    private final AtomicReference<WorkItem<?>> currentItem = new AtomicReference<>();

    @Activate
    public GatewayThreadImpl(@Reference TypedEventBus typedEventBus, @Reference ResourceSet resourceSet,
            @Reference SensiNactPackage sensinactPackage) {
        this.typedEventBus = typedEventBus;
        nexusImpl = new ModelNexus(resourceSet, sensinactPackage, this::getCurrentAccumulator);
        start();
    }

    @Deactivate
    void deactivate() {
        run.set(false);
        interrupt();
        try {
            join(500);
        } catch (InterruptedException e) {
            // Just keep going and reset our interrupt status
            Thread.currentThread().interrupt();
        }
        nexusImpl.shutDown();
    }

    private NotificationAccumulator getCurrentAccumulator() {
        WorkItem<?> workItem = currentItem.get();
        return workItem == null ? null : workItem.command.getAccumulator();
    }

    @Override
    public PromiseFactory getPromiseFactory() {
        return promiseFactory;
    }

    @Override
    public NotificationAccumulator createAccumulator() {
        return new NotificationAccumulatorImpl(typedEventBus);
    }

    @Override
    public <T> Promise<T> execute(AbstractSensinactCommand<T> command) {
        Deferred<T> d = getPromiseFactory().deferred();
        work.add(new WorkItem<>(d, command, nexusImpl));
        return d.getPromise();
    }

    @Override
    public void run() {
        while (run.get()) {
            try {
                WorkItem<?> item = work.take();
                currentItem.set(item);
                item.doWork();
            } catch (InterruptedException e) {
                continue;
            } finally {
                currentItem.set(null);
            }
        }
    }

    private class WorkItem<T> {
        private final Deferred<T> d;
        private final AbstractSensinactCommand<T> command;
        private final ModelNexus nexusImpl;

        public WorkItem(Deferred<T> d, AbstractSensinactCommand<T> command, ModelNexus nexusImpl) {
            this.d = d;
            this.command = command;
            this.nexusImpl = nexusImpl;
        }

        void doWork() {
            try {
                SensinactModelImpl modelImpl = new SensinactModelImpl(command.getAccumulator(), nexusImpl,
                        getGatewayThread().getPromiseFactory());
                Promise<T> promise;
                try {
                    promise = command.call(modelImpl);
                } finally {
                    modelImpl.invalidate();
                }
                d.resolveWith(promise);
            } catch (Exception e) {
                d.fail(e);
            }
        }
    }
}
