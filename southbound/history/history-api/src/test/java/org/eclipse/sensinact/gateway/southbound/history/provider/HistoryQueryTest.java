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
package org.eclipse.sensinact.gateway.southbound.history.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class HistoryQueryTest {

    private static final ResourcePath PATH = new ResourcePath("p", "s", "r");

    @Test
    void builderDefaults() {
        HistoryQuery query = HistoryQuery.builder(PATH).build();

        assertEquals(PATH, query.path());
        assertEquals(TimeRange.ALL, query.range());
        assertEquals(SortOrder.ASCENDING, query.order());
        assertEquals(0, query.offset());
        assertEquals(HistoryQuery.PROVIDER_DEFAULT_LIMIT, query.limit());
        assertNull(query.valueFilter());
    }

    @Test
    void negativeOffsetIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> HistoryQuery.builder(PATH).offset(-1).build());
    }

    @Test
    void nonPositiveExplicitLimitIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> HistoryQuery.builder(PATH).limit(0));
        assertThrows(IllegalArgumentException.class, () -> HistoryQuery.builder(PATH).limit(-5));
    }

    @Test
    void blankPathSegmentsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new ResourcePath("p", " ", "r"));
        assertThrows(NullPointerException.class, () -> new ResourcePath("p", "s", null));
    }

    @Test
    void valueFilterRequiresConditions() {
        assertThrows(IllegalArgumentException.class, () -> new ValueFilter(java.util.List.of()));
        assertThrows(NullPointerException.class, () -> ValueFilter.of(ValueFilter.Op.GT, null));
    }
}
