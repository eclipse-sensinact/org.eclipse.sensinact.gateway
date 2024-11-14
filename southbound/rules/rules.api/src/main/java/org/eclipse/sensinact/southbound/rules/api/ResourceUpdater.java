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
package org.eclipse.sensinact.southbound.rules.api;

import java.time.Instant;

public interface ResourceUpdater {

    public void updateResource(String provider, String service, String resource, Object value);

    public void updateResource(String provider, String service, String resource, Object value, Instant timestamp);

    public BatchUpdate updateBatch();

    public interface BatchUpdate {
        public BatchUpdate updateResource(String provider, String service, String resource, Object value);
        public BatchUpdate updateResource(String provider, String service, String resource, Object value, Instant timestamp);
        public void completeBatch();
    }
}
