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
package org.eclipse.sensinact.gateway.southbound.history.storage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.osgi.annotation.versioning.ConsumerType;

/**
 * Whiteboard service deciding which resource updates are historized. The
 * built-in ConfigAdmin factory ({@code sensinact.history.filter}) registers
 * one instance per configuration; third parties (e.g. a rule-driven
 * notification proxy) register their own implementations.
 *
 * A record is stored for a targeted history provider when it matches at least
 * one applicable filter's {@link #include()} (and that filter's
 * {@link #shouldStore(HistoricalRecord, Optional)} returns true) and no
 * applicable filter's {@link #exclude()}. The engine unions the include
 * criteria's {@code dataTopics()} per provider, so unwanted events are never
 * delivered — topics are the escaped form (see {@code TopicUtils}).
 */
@ConsumerType
public interface HistoryIngestFilter {

    String name();

    /**
     * History provider names this filter applies to; empty means all
     * providers.
     */
    default Collection<String> targets() {
        return List.of();
    }

    /** Selects the resources this filter admits; never null. */
    ICriterion include();

    /** Optionally rejects resources admitted by any include; may be null. */
    default ICriterion exclude() {
        return null;
    }

    /**
     * Stateful storage decision, consulted after {@link #include()} matched.
     * {@code lastStored} is the value most recently stored for this record's
     * resource by the targeted provider (empty when none exists) — supplied
     * by the engine, which tracks it for deadband filtering. Implementations
     * needing counters or timers keep that state themselves.
     */
    default boolean shouldStore(HistoricalRecord record, Optional<TimedValue<?>> lastStored) {
        return true;
    }
}
