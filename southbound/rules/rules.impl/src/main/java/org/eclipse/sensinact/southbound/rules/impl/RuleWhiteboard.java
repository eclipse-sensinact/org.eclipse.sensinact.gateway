/*********************************************************************
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Kentyou - initial implementation
 **********************************************************************/
package org.eclipse.sensinact.southbound.rules.impl;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.metrics.IMetricsManager;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.southbound.rules.api.RuleDefinition;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.util.promise.PromiseFactory;

@Component
public class RuleWhiteboard {

    private final BundleContext context;
    private final GatewayThread gateway;
    private final IMetricsManager metrics;
    private final DataUpdate update;
    private final ExecutorService workers;
    private final ScheduledExecutorService scheduler;
    private final PromiseFactory promiseFactory;

    @Activate
    public RuleWhiteboard(BundleContext context, @Reference GatewayThread gateway,
            @Reference IMetricsManager metrics, @Reference DataUpdate update) {
        super();
        this.context = context;
        this.gateway = gateway;
        this.metrics = metrics;
        this.update = update;

        ThreadGroup group = new ThreadGroup("Eclipse sensiNact Gateway Rules Whiteboard");
        this.workers = Executors.newFixedThreadPool(4, r -> new Thread(group, r));
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> new Thread(group, r, "Rules Whiteboard Scheduler"));
        this.promiseFactory = new PromiseFactory(Executors.unconfigurableExecutorService(workers),
                Executors.unconfigurableScheduledExecutorService(scheduler));
    }

    private final Map<String, RuleProcessor> processors = new ConcurrentHashMap<>();

    @Deactivate
    void stop() {
        for(String k : processors.keySet()) {
            try {
                Optional.ofNullable(processors.remove(k))
                    .ifPresent(RuleProcessor::close);
            } catch (Exception e) {
                // Swallow this
            }
        }
        scheduler.shutdown();
        workers.shutdown();
        try {
            scheduler.awaitTermination(100, TimeUnit.MILLISECONDS);
            workers.awaitTermination(100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            // Maintain the interrupt status
            Thread.currentThread().interrupt();
        } finally {
            scheduler.shutdownNow();
            workers.shutdownNow();
        }
    }

    private String getKey(Map<String, Object> params) {
        return String.valueOf(params.get(Constants.SERVICE_ID));
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    void addRuleDefinition(RuleDefinition rd, Map<String, Object> props) {
        processors.put(getKey(props), new RuleProcessor(context, gateway, metrics, promiseFactory,
                new RuleResourceUpdater(update), rd, props));
    }

    void modifiedRuleDefinition(RuleDefinition rd, Map<String, Object> props) {
        // No op
    }

    void removeRuleDefinition(RuleDefinition rd, Map<String, Object> props) {
        Optional.ofNullable(processors.remove(getKey(props)))
            .ifPresent(RuleProcessor::close);
    }
}
