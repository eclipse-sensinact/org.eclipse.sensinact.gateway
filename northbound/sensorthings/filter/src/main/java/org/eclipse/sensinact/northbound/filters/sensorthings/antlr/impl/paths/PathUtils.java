/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.paths;

import java.time.Instant;
import java.util.Optional;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;

public class PathUtils {

    public static String timestampToString(Instant timestamp) {
        return Long.toString(timestamp.toEpochMilli(), 16);
    }

    public static ResourceSnapshot findResource(final ProviderSnapshot provider, final String service,
            final String resource) {
        for (final ServiceSnapshot svc : provider.getServices()) {
            if (service == null || svc.getName().equals(service)) {
                for (final ResourceSnapshot rc : svc.getResources()) {
                    if (rc.getName().equals(resource)) {
                        return rc;
                    }
                }
                break;
            }
        }
        return null;
    }

    public static ResourceSnapshot findResource(final ProviderSnapshot provider, final String resource) {
        return findResource(provider, null, resource);
    }

    public static Optional<Object> getResourceValue(final ProviderSnapshot provider, final String service,
            final String resource) {
        final ResourceSnapshot rc = findResource(provider, service, resource);
        if (rc != null && rc.getValue() != null) {
            return Optional.ofNullable(rc.getValue().getValue());
        } else {
            return Optional.empty();
        }
    }

    public static Optional<Object> getResourceValue(final ProviderSnapshot provider, final String resource) {
        final ResourceSnapshot rc = findResource(provider, resource);
        if (rc != null && rc.getValue() != null) {
            return Optional.ofNullable(rc.getValue().getValue());
        } else {
            return Optional.empty();
        }
    }

}
