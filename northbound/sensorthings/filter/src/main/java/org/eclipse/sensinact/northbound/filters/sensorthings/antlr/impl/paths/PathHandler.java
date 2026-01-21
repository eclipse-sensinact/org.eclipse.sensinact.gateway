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

import java.util.List;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.ResourceValueFilterInputHolder;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.UnsupportedRuleException;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.paths.sensorthing.DatastreamPathHandlerSensorthings;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.paths.sensorthing.FeatureOfInterestPathHandlerSensorthings;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.paths.sensorthing.HistoricalLocationPathHandlerSensorthings;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.paths.sensorthing.LocationPathHandlerSensorthings;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.paths.sensorthing.ObservationPathHandlerSensorthings;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.paths.sensorthing.ObservedPropertyPathHandlerSensorthings;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.paths.sensorthing.SensorPathHandlerSensorthings;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.paths.sensorthing.ThingPathHandlerSensorthings;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import static org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage.eNS_URI;

public class PathHandler {

    private final String path;

    public PathHandler(final String path) {
        this.path = path;
    }

    public Object handle(final ResourceValueFilterInputHolder holder) {
        final ProviderSnapshot provider = holder.getProvider();
        final List<? extends ResourceSnapshot> resources = holder.getResources();
        final ResourceSnapshot resource = holder.getResource();
        final SensiNactSession session = holder.getSession();
        if (!isSensorthingModel(provider)) {
            switch (holder.getContext()) {
            case THINGS:
                return new ThingPathHandler(provider, resources).handle(path);
            case FEATURES_OF_INTEREST:
                return new FeatureOfInterestPathHandler(provider, resources).handle(path);
            case HISTORICAL_LOCATIONS:
                return new HistoricalLocationPathHandler(provider, resources).handle(path);
            case LOCATIONS:
                return new LocationPathHandler(provider, resources).handle(path);
            case OBSERVATIONS:
                return new ObservationPathHandler(provider, resource).handle(path);
            case DATASTREAMS:
                return new DatastreamPathHandler(provider, resource).handle(path);
            case OBSERVED_PROPERTIES:
                return new ObservedPropertyPathHandler(provider, resource).handle(path);
            case SENSORS:
                return new SensorPathHandler(provider, resource).handle(path);
            }
        } else {
            switch (holder.getContext()) {
            case THINGS:
                return new ThingPathHandlerSensorthings(provider, session).handle(path);
            case FEATURES_OF_INTEREST:
                return new FeatureOfInterestPathHandlerSensorthings(provider, session).handle(path);
            case HISTORICAL_LOCATIONS:
                return new HistoricalLocationPathHandlerSensorthings(provider, session).handle(path);
            case LOCATIONS:
                return new LocationPathHandlerSensorthings(provider, session).handle(path);

            case OBSERVATIONS:
                return new ObservationPathHandlerSensorthings(provider, session).handle(path);
            case DATASTREAMS:
                return new DatastreamPathHandlerSensorthings(provider, session).handle(path);
            case OBSERVED_PROPERTIES:
                return new ObservedPropertyPathHandlerSensorthings(provider, session).handle(path);
            case SENSORS:
                return new SensorPathHandlerSensorthings(provider, session).handle(path);
            }
        }
        throw new UnsupportedRuleException("Path of " + holder.getContext() + " is not yet supported");
    }

    private boolean isSensorthingModel(final ProviderSnapshot provider) {
        return eNS_URI.equals(provider.getModelPackageUri());
    }
}
