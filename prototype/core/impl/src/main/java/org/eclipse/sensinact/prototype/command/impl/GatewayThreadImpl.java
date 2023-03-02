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
package org.eclipse.sensinact.prototype.command.impl;

import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.concurrent.Executors.newScheduledThreadPool;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.eclipse.sensinact.prototype.command.GatewayThread.getGatewayThread;
import static org.osgi.service.component.annotations.ReferenceCardinality.MULTIPLE;
import static org.osgi.service.component.annotations.ReferencePolicy.DYNAMIC;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.sensinact.model.core.SensiNactPackage;
import org.eclipse.sensinact.prototype.command.AbstractSensinactCommand;
import org.eclipse.sensinact.prototype.command.GatewayThread;
import org.eclipse.sensinact.prototype.model.impl.SensinactModelManagerImpl;
import org.eclipse.sensinact.prototype.model.nexus.ModelNexus;
import org.eclipse.sensinact.prototype.notification.NotificationAccumulator;
import org.eclipse.sensinact.prototype.notification.impl.ImmediateNotificationAccumulator;
import org.eclipse.sensinact.prototype.notification.impl.NotificationAccumulatorImpl;
import org.eclipse.sensinact.prototype.twin.impl.SensinactDigitalTwinImpl;
import org.eclipse.sensinact.prototype.whiteboard.impl.SensinactWhiteboard;
import org.osgi.service.component.AnyService;
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

    private final SensinactWhiteboard whiteboard;

    private final ModelNexus nexusImpl;

    // TODO decide if we should just use an infinite queue
    private final BlockingQueue<WorkItem<?>> work = new ArrayBlockingQueue<>(4096);

    private final AtomicBoolean run = new AtomicBoolean(true);

    private final PromiseFactory promiseFactory = new PromiseFactory(
            newCachedThreadPool(r -> new Thread(r, "Eclipse sensiNact Gateway Worker")),
            newScheduledThreadPool(3, r -> new Thread(r, "Eclipse sensiNact Scheduler")));

    private final AtomicReference<WorkItem<?>> currentItem = new AtomicReference<>();

    @Activate
    public GatewayThreadImpl(@Reference TypedEventBus typedEventBus, @Reference ResourceSet resourceSet,
            @Reference SensiNactPackage sensinactPackage) {
        this.typedEventBus = typedEventBus;
        this.whiteboard = new SensinactWhiteboard(this);
        nexusImpl = new ModelNexus(resourceSet, sensinactPackage, this::getCurrentAccumulator, this::performAction);
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

        ExecutorService executor = (ExecutorService) promiseFactory.executor();
        ScheduledExecutorService scheduledExecutor = promiseFactory.scheduledExecutor();

        executor.shutdown();
        scheduledExecutor.shutdown();

        try {
            if (!executor.awaitTermination(2, SECONDS)) {
                executor.shutdownNow();
            }
            if (!scheduledExecutor.awaitTermination(2, SECONDS)) {
                scheduledExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            // Just keep going and reset our interrupt status
            Thread.currentThread().interrupt();
            executor.shutdownNow();
            scheduledExecutor.shutdown();
        }
    }

    @Reference(service = AnyService.class, target = "(sensiNact.whiteboard.resource=true)", cardinality = MULTIPLE, policy = DYNAMIC)
    void addWhiteboardService(Object service, Map<String, Object> props) {
        whiteboard.addWhiteboardService(service, props);
    }

    void updatedWhiteboardService(Object service, Map<String, Object> props) {
        whiteboard.updatedWhiteboardService(service, props);
    }

    void removeWhiteboardService(Object service, Map<String, Object> props) {
        whiteboard.removeWhiteboardService(service, props);
    }

    private NotificationAccumulator getCurrentAccumulator() {
        WorkItem<?> workItem = currentItem.get();
        return workItem == null ? new ImmediateNotificationAccumulator(typedEventBus)
                : workItem.command.getAccumulator();
    }

    private Promise<Object> performAction(String model, String provider, String service, String resource,
            Map<String, Object> args) {
        Deferred<Object> d = getPromiseFactory().deferred();
        try {
            this.promiseFactory.executor().execute(() -> {
                try {
                    Object result = this.whiteboard.act(model, provider, service, resource, args);
                    if (result instanceof Promise) {
                        d.resolveWith((Promise<?>) result);
                    } else {
                        d.resolve(result);
                    }
                } catch (Exception e) {
                    d.fail(e);
                }
            });
        } catch (Exception e) {
            d.fail(e);
        }

        return d.getPromise();
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
                SensinactDigitalTwinImpl twinImpl = new SensinactDigitalTwinImpl(nexusImpl,
                        getGatewayThread().getPromiseFactory());
                SensinactModelManagerImpl mgrImpl = new SensinactModelManagerImpl(nexusImpl);
                Promise<T> promise;
                try {
                    promise = command.call(twinImpl, mgrImpl);
                } finally {
                    twinImpl.invalidate();
                    mgrImpl.invalidate();
                }
                d.resolveWith(promise);
            } catch (Exception e) {
                d.fail(e);
            }
        }
    }
}
