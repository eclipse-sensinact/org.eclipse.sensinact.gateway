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
package org.eclipse.sensinact.gateway.southbound.history.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import org.eclipse.sensinact.gateway.southbound.history.provider.AggregateFunction;
import org.eclipse.sensinact.gateway.southbound.history.provider.AggregationQuery;
import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryCapability;
import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryPage;
import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryQuery;
import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryQueryException;
import org.eclipse.sensinact.gateway.southbound.history.provider.ResourcePath;
import org.eclipse.sensinact.gateway.southbound.history.provider.TimeRange;
import org.eclipse.sensinact.gateway.southbound.history.provider.UnsupportedHistoryOperationException;
import org.eclipse.sensinact.gateway.southbound.history.provider.ValueFilter;
import org.eclipse.sensinact.gateway.southbound.history.provider.ValueFilter.Op;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoryStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class HistoryProviderEngineTest {

    private static final ResourcePath PATH = new ResourcePath("p", "s", "r");

    private HistoryStorage storage;
    private HistoryProviderEngine engine;

    @BeforeEach
    void setUp() {
        storage = mock(HistoryStorage.class);
        when(storage.maxPageSize()).thenReturn(500);
        when(storage.values(any())).thenReturn(HistoryPage.of(List.of(), 0, false));
        engine = new HistoryProviderEngine("test", storage);
    }

    @Test
    void defaultLimitResolvesToMaxPageSize() {
        engine.getValues(HistoryQuery.builder(PATH).build());

        ArgumentCaptor<HistoryQuery> captor = ArgumentCaptor.forClass(HistoryQuery.class);
        verify(storage).values(captor.capture());
        assertEquals(500, captor.getValue().limit());
    }

    @Test
    void explicitLimitIsClampedToMaxPageSize() {
        engine.getValues(HistoryQuery.builder(PATH).limit(9_999).build());

        ArgumentCaptor<HistoryQuery> captor = ArgumentCaptor.forClass(HistoryQuery.class);
        verify(storage).values(captor.capture());
        assertEquals(500, captor.getValue().limit());
    }

    @Test
    void smallExplicitLimitPassesThrough() {
        engine.getValues(HistoryQuery.builder(PATH).limit(7).build());

        ArgumentCaptor<HistoryQuery> captor = ArgumentCaptor.forClass(HistoryQuery.class);
        verify(storage).values(captor.capture());
        assertEquals(7, captor.getValue().limit());
    }

    @Test
    void valueFilterWithoutCapabilityIsRejected() {
        when(storage.capabilities()).thenReturn(Set.of());

        HistoryQuery query = HistoryQuery.builder(PATH).valueFilter(ValueFilter.of(Op.GT, 5L)).build();

        assertThrows(UnsupportedHistoryOperationException.class, () -> engine.getValues(query));
    }

    @Test
    void aggregationWithoutCapabilityIsRejected() {
        when(storage.capabilities()).thenReturn(Set.of(HistoryCapability.VALUE_FILTERING));

        AggregationQuery query = new AggregationQuery(PATH, TimeRange.ALL, Duration.ofMinutes(1),
                Set.of(AggregateFunction.AVG));

        assertThrows(UnsupportedHistoryOperationException.class, () -> engine.aggregate(query));
    }

    @Test
    void backendFailuresAreWrappedInHistoryQueryException() {
        when(storage.firstValue(any())).thenThrow(new IllegalStateException("db gone"));

        HistoryQueryException wrapped = assertThrows(HistoryQueryException.class,
                () -> engine.getFirstValue(PATH));
        assertEquals(IllegalStateException.class, wrapped.getCause().getClass());
    }
}
