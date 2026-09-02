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
import java.util.Map;

import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryCapability;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoryStorage;
import org.eclipse.sensinact.gateway.southbound.history.storage.PruneRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes one housekeeping policy run against the targeted backends.
 * Deletions are irreversible: every run is logged with its counts, and a run
 * that saturates the {@code max.delete} cap is logged as a warning because
 * the policy is probably misconfigured.
 */
public final class HousekeepingRunner {

    private static final Logger logger = LoggerFactory.getLogger(HousekeepingRunner.class);

    private HousekeepingRunner() {
    }

    public static long run(HousekeepingPolicy policy, Map<String, HistoryStorage> storagesByName, Instant now) {
        long total = 0;
        for (Map.Entry<String, HistoryStorage> entry : storagesByName.entrySet()) {
            if (!policy.appliesTo(entry.getKey())) {
                continue;
            }
            if (!entry.getValue().capabilities().contains(HistoryCapability.PRUNING)) {
                logger.warn("Housekeeping policy {} skips provider {}: its backend does not support pruning",
                        policy.name(), entry.getKey());
                continue;
            }
            Instant cutoff = policy.retention() == null ? null : now.minus(policy.retention());
            PruneRequest request = new PruneRequest(null, cutoff, policy.keepCount(), policy.maxDelete());
            try {
                long deleted = entry.getValue().prune(request);
                total += deleted;
                if (policy.maxDelete() != null && deleted >= policy.maxDelete()) {
                    logger.warn(
                            "Housekeeping policy {} hit its max.delete cap of {} on provider {} — the policy may be misconfigured",
                            policy.name(), policy.maxDelete(), entry.getKey());
                } else {
                    logger.info("Housekeeping policy {} deleted {} records on provider {} (cutoff: {}, keep: {})",
                            policy.name(), deleted, entry.getKey(), cutoff, policy.keepCount());
                }
            } catch (RuntimeException e) {
                logger.error("Housekeeping policy {} failed on provider {}", policy.name(), entry.getKey(), e);
            }
        }
        return total;
    }
}
