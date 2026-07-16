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
package org.eclipse.sensinact.gateway.southbound.history.spikes.spi;

import java.util.Objects;

public record ResourcePath(String provider, String service, String resource) {
    public ResourcePath {
        Objects.requireNonNull(provider);
        Objects.requireNonNull(service);
        Objects.requireNonNull(resource);
    }
}
