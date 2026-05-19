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
package org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.paths.sensinact;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.UnsupportedRuleException;

public class PathUtils {

    public static String timestampToString(Instant timestamp) {
        return Long.toString(timestamp.toEpochMilli(), 16);
    }

    public static ResourceSnapshot findResource(final ProviderSnapshot provider, final String service,
            final String resource) {
        for (final ServiceSnapshot svc : provider.getServices()) {
            if (svc.getName().equals(service)) {
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

    public static Optional<Object> getResourceValue(final ProviderSnapshot provider, final String service,
            final String resource) {
        final ResourceSnapshot rc = findResource(provider, service, resource);
        if (rc != null && rc.getValue() != null) {
            return Optional.ofNullable(rc.getValue().getValue());
        } else {
            return Optional.empty();
        }
    }

    public static Object getResourceLevelField(final ProviderSnapshot provider, final ResourceSnapshot resource,
            final String path) {
        final TimedValue<?> rcValue = resource.getValue();
        switch (path) {
        case "result":
            if (rcValue != null) {
                return rcValue.getValue();
            }
            return null;

        case "name":
            if (resource.getName().equals("friendlyName")) {
                if (rcValue != null) {
                    return rcValue.getValue();
                }
            }
            return null;

        case "description":
            if (resource.getName().equals("description")) {
                if (rcValue != null) {
                    return rcValue.getValue();
                }
            }
            return null;

        case "resulttime":
        case "phenomenontime":
        case "time":
        case "validtime":
            if (rcValue != null) {
                return rcValue.getTimestamp();
            }
            return null;

        case "resultquality":
            // FIXME: requires access to metadata from snapshot
            return "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Observation";
        case "observationtype":
            // FIXME: requires access to metadata from snapshot
            // -> sensorthings.observation.quality
            return null;
        case "encodingtype":
            // FIXME: requires access to metadata from snapshot
            // -> sensorthings.observation.quality
            return null;
        case "observedarea":
            // TODO: convert to GeoJSON?
            return getResourceValue(provider, "admin", "location").orElse(null);

        case "properties":
            // FIXME: requires access to metadata from snapshot
            // -> Copy of metadata
            return Map.of();

        default:
            return null;
        }
    }

    public static Object getProviderLevelField(final ProviderSnapshot provider,
            final List<? extends ResourceSnapshot> resources, final String path) {
        switch (path) {
        case "name":
            return getResourceValue(provider, "admin", "friendlyName").orElse(provider.getName());

        case "description":
            return getResourceValue(provider, "admin", "description").orElse(provider.getName());

        case "location":
            return getResourceValue(provider, "admin", "location").orElse(null);
        case "encodingtype":
            // need to return the same order
            return getResourceValue(provider, "admin", "friendlyName").orElse(provider.getName());
        case "time":
            ResourceSnapshot rc = findResource(provider, "admin", "location");
            return rc != null && rc.getValue() != null ? rc.getValue().getTimestamp() : null;

        default:
            final Optional<Object> value = getResourceValue(provider, "admin", path);
            if (value.isPresent()) {
                return value.get();
            } else {
                throw new UnsupportedRuleException("Unexpected provider level field: " + path);
            }
        }
    }
}
