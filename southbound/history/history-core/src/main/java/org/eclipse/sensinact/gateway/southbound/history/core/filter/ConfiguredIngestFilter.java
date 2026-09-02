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
package org.eclipse.sensinact.gateway.southbound.history.core.filter;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoricalRecord;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoryIngestFilter;

/**
 * {@link HistoryIngestFilter} built from one {@code sensinact.history.filter}
 * configuration: declarative selectors plus an optional change condition.
 */
public class ConfiguredIngestFilter implements HistoryIngestFilter {

    private final String name;
    private final List<String> targets;
    private final ICriterion include;
    private final ICriterion exclude;
    private final ChangeCondition changeCondition;

    public ConfiguredIngestFilter(String name, List<String> targets, ICriterion include, ICriterion exclude,
            ChangeCondition changeCondition) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.targets = targets == null ? List.of() : List.copyOf(targets);
        this.include = Objects.requireNonNull(include, "include must not be null");
        this.exclude = exclude;
        this.changeCondition = changeCondition == null ? ChangeCondition.STORE_ALL : changeCondition;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Collection<String> targets() {
        return targets;
    }

    @Override
    public ICriterion include() {
        return include;
    }

    @Override
    public ICriterion exclude() {
        return exclude;
    }

    @Override
    public boolean shouldStore(HistoricalRecord record, Optional<TimedValue<?>> lastStored) {
        return changeCondition.shouldStore(record, lastStored);
    }
}
