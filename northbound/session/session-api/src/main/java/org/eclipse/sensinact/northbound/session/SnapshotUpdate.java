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
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.northbound.session;

import java.util.Map;
import java.util.Set;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;

public record SnapshotUpdate(
        /**
         * Providers that were not previously selected by
         * the snapshot, but are now as the result of a
         * provider being created or updated
         */
        Map<String, ProviderSnapshot> arriving,
        /**
         * Providers that were selected before, have been
         * updated, and still match the selection filter
         */
        Map<String, ProviderSnapshot> modified,
        /**
         * Providers that were previously selected by the
         * snapshot, but are no longer selected because
         * of the provider being update or deleted
         */
        Set<String> departing) {

}
