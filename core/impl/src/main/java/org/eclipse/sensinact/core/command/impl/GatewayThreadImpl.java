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
package org.eclipse.sensinact.core.command.impl;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.eclipse.sensinact.core.command.GatewayThread.getGatewayThread;
import static org.osgi.service.component.annotations.ReferenceCardinality.MULTIPLE;
import static org.osgi.service.component.annotations.ReferencePolicy.DYNAMIC;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.metrics.IMetricTimer;
import org.eclipse.sensinact.core.metrics.IMetricsManager;
import org.eclipse.sensinact.core.model.impl.SensinactModelManagerImpl;
import org.eclipse.sensinact.core.model.nexus.ModelNexus;
import org.eclipse.sensinact.core.notification.impl.ImmediateNotificationAccumulator;
import org.eclipse.sensinact.core.notification.impl.NotificationAccumulator;
import org.eclipse.sensinact.core.notification.impl.NotificationAccumulatorImpl;
import org.eclipse.sensinact.core.twin.impl.SensinactDigitalTwinImpl;
import org.eclipse.sensinact.core.whiteboard.WhiteboardHandler;
import org.eclipse.sensinact.core.whiteboard.impl.SensinactWhiteboard;
import org.eclipse.sensinact.model.core.provider.ProviderPackage;
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
@Component(immediate = true)
public class GatewayThreadImpl extends Thread implements GatewayThread {

    private final TypedEventBus typedEventBus;

    private final SensinactWhiteboard whiteboard;

    private final ModelNexus nexusImpl;

    // TODO decide if we should just use an infinite queue
    private final BlockingQueue<WorkItem<?>> work = new ArrayBlockingQueue<>(4096);

    private final AtomicBoolean run = new AtomicBoolean(true);

    // We single thread promises from this promise factory to avoid excessive
    // out-of-order rearrangement from chaining.
    private final PromiseFactory promiseFactory = new PromiseFactory(
            newSingleThreadExecutor(r -> new Thread(r, "Eclipse sensiNact Gateway Worker")),
            newSingleThreadScheduledExecutor(r -> new Thread(r, "Eclipse sensiNact Scheduler")));

    private final AtomicReference<NotificationAccumulator> currentAccumulator = new AtomicReference<>();

    private IMetricsManager metrics;

    @Activate
    public GatewayThreadImpl(@Reference IMetricsManager metrics, @Reference TypedEventBus typedEventBus,
            @Reference ResourceSet resourceSet, @Reference ProviderPackage providerPackage) {
        this.metrics = metrics;
        this.typedEventBus = typedEventBus;
        this.whiteboard = new SensinactWhiteboard(this, metrics);
        nexusImpl = new ModelNexus(resourceSet, providerPackage, this::getCurrentAccumulator, whiteboard);
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

    @Reference(cardinality = MULTIPLE, policy = DYNAMIC)
    void addEPackage(EPackage ePackage) {
        nexusImpl.addEPackage(ePackage);
    }

    void removeEPackage(EPackage ePackage) {
        nexusImpl.removeEPackage(ePackage);
    }

    @Reference(service = WhiteboardHandler.class, cardinality = MULTIPLE, policy = DYNAMIC)
    void addWhiteboardResourceHandler(WhiteboardHandler wbHandler, Map<String, Object> props) {
        whiteboard.addWhiteboardHandler(wbHandler, props);
    }

    void updatedWhiteboardResourceHandler(WhiteboardHandler wbHandler, Map<String, Object> props) {
        whiteboard.updatedWhiteboardHandler(wbHandler, props);
    }

    void removeWhiteboardResourceHandler(WhiteboardHandler wbHandler, Map<String, Object> props) {
        whiteboard.removeWhiteboardHandler(wbHandler, props);
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
        NotificationAccumulator accumulator = currentAccumulator.get();
        return accumulator == null ? new ImmediateNotificationAccumulator(typedEventBus) : accumulator;
    }

    @Override
    public PromiseFactory getPromiseFactory() {
        return promiseFactory;
    }

    private NotificationAccumulator createAccumulator() {
        return new NotificationAccumulatorImpl(typedEventBus);
    }

    @Override
    public <T> Promise<T> execute(AbstractSensinactCommand<T> command) {
        Deferred<T> d = getPromiseFactory().deferred();
        work.add(new WorkItem<>(d, command, nexusImpl));
        metrics.getCounter("sensinact.tasks.pending").inc();
        metrics.getHistogram("sensinact.tasks.pending.hist").update(work.size());
        return d.getPromise();
    }

    @Override
    public void run() {
        while (run.get()) {
            try {
                WorkItem<?> item = work.take();
                metrics.getCounter("sensinact.tasks.pending").dec();
                metrics.getHistogram("sensinact.tasks.pending.hist").update(work.size());
                try (IMetricTimer timer = metrics.withTimer("sensinact.task.time")) {
                    item.doWork();
                }
            } catch (InterruptedException e) {
                continue;
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
                final NotificationAccumulator accumulator = createAccumulator();
                currentAccumulator.set(accumulator);

                SensinactDigitalTwinImpl twinImpl = new SensinactDigitalTwinImpl(nexusImpl,
                        getGatewayThread().getPromiseFactory());
                SensinactModelManagerImpl mgrImpl = new SensinactModelManagerImpl(nexusImpl);
                Promise<T> promise;
                try {
                    promise = command.call(twinImpl, mgrImpl).onResolve(accumulator::completeAndSend);
                } finally {
                    twinImpl.invalidate();
                    mgrImpl.invalidate();
                }
                d.resolveWith(promise);
            } catch (Exception e) {
                d.fail(e);
            } finally {
                currentAccumulator.set(null);
            }
        }
    }
}
