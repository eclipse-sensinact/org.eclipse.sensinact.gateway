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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class PruneRequestTest {

    private static final Instant CUTOFF = Instant.parse("2020-01-01T00:00:00Z");

    @Test
    void atLeastOneBoundIsRequired() {
        assertThrows(IllegalArgumentException.class, () -> new PruneRequest(null, null, null, null));
    }

    @Test
    void negativeKeepCountIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new PruneRequest(null, null, -1L, null));
    }

    @Test
    void nonPositiveMaxDeleteIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new PruneRequest(null, CUTOFF, null, 0L));
        assertThrows(IllegalArgumentException.class, () -> new PruneRequest(null, CUTOFF, null, -10L));
    }

    @Test
    void convenienceConstructorHasNoSafetyCap() {
        PruneRequest request = new PruneRequest(null, CUTOFF, null);

        assertNull(request.maxDelete());
        assertEquals(CUTOFF, request.olderThan());
    }
}
