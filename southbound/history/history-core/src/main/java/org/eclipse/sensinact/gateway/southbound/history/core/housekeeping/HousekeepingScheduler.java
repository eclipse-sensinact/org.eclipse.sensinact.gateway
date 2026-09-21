/*********************************************************************
* Copyright (c) 2026 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Data In Motion - initial API and implementation
**********************************************************************/
package org.eclipse.sensinact.gateway.southbound.history.core.housekeeping;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.sensinact.gateway.southbound.history.storage.HistoryStorage;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs housekeeping policies on their configured period. The first run of a
 * policy happens one full period after it appears — never at startup, so a
 * (re)deployed gateway cannot surprise-delete anything. Runs are serialized
 * on a single named thread.
 */
@Component(immediate = true)
public class HousekeepingScheduler {

    private static final Logger logger = LoggerFactory.getLogger(HousekeepingScheduler.class);

    private final ScheduledExecutorService executor;
    private final Map<HousekeepingPolicy, ScheduledFuture<?>> jobs = new LinkedHashMap<>();
    private final Map<String, HistoryStorage> storagesByName = new HashMap<>();

    public HousekeepingScheduler() {
        this(Executors.newSingleThreadScheduledExecutor(
                runnable -> new Thread(runnable, "sensiNact history housekeeping")));
    }

    HousekeepingScheduler(ScheduledExecutorService executor) {
        this.executor = executor;
    }

    @Deactivate
    void stop() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @Reference(service = HistoryStorage.class, cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    synchronized void addStorage(HistoryStorage storage, Map<String, Object> properties) {
        if (properties.get(HistoryStorage.PROP_NAME) instanceof String name && !name.isBlank()) {
            storagesByName.put(name, storage);
        }
    }

    synchronized void removeStorage(HistoryStorage storage) {
        storagesByName.values().remove(storage);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    synchronized void addPolicy(HousekeepingPolicy policy) {
        long periodMillis = policy.schedule().toMillis();
        jobs.put(policy, executor.scheduleAtFixedRate(() -> runPolicy(policy), periodMillis, periodMillis,
                TimeUnit.MILLISECONDS));
        logger.info("Housekeeping policy {} scheduled every {}", policy.name(), policy.schedule());
    }

    synchronized void removePolicy(HousekeepingPolicy policy) {
        ScheduledFuture<?> job = jobs.remove(policy);
        if (job != null) {
            job.cancel(false);
        }
    }

    private void runPolicy(HousekeepingPolicy policy) {
        Map<String, HistoryStorage> snapshot;
        synchronized (this) {
            snapshot = Map.copyOf(storagesByName);
        }
        HousekeepingRunner.run(policy, snapshot, Instant.now());
    }
}
