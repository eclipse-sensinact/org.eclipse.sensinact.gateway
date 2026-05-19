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

import static org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage.eNS_URI;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.ResourceValueFilterInputHolder;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.UnsupportedRuleException;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.paths.sensinact.DatastreamPathHandler;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.paths.sensinact.FeatureOfInterestPathHandler;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.paths.sensinact.HistoricalLocationPathHandler;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.paths.sensinact.LocationPathHandler;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.paths.sensinact.ObservationPathHandler;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.paths.sensinact.ObservedPropertyPathHandler;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.paths.sensinact.SensorPathHandler;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.paths.sensinact.ThingPathHandler;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.paths.sensorthing.DatastreamPathHandlerSensorthings;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.paths.sensorthing.FeatureOfInterestPathHandlerSensorthings;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.paths.sensorthing.HistoricalLocationPathHandlerSensorthings;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.paths.sensorthing.LocationPathHandlerSensorthings;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.paths.sensorthing.ObservationPathHandlerSensorthings;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.paths.sensorthing.ObservedPropertyPathHandlerSensorthings;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.paths.sensorthing.SensorPathHandlerSensorthings;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.paths.sensorthing.ThingPathHandlerSensorthings;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.IDtoMemoryCache;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

public class PathHandler {
    public record PathContext(ObjectMapper mapper, ProviderSnapshot provider, SensiNactSession session,
            ResourceSnapshot resource, Map<String, Object> configProperties,
            IDtoMemoryCache<ExpandedObservation> cacheObs, IDtoMemoryCache<Instant> cacheHl) {
    }

    protected static ObjectMapper MAPPER = JsonMapper.builder().build();

    private final String path;

    public PathHandler(final String path) {
        this.path = path;

    }

    public Object handle(final ResourceValueFilterInputHolder holder) {
        final ProviderSnapshot provider = holder.getProvider();
        final List<? extends ResourceSnapshot> resources = holder.getResources();
        final ResourceSnapshot resource = holder.getResource();
        final SensiNactSession session = holder.getSession();
        final Map<String, Object> configProperties = holder.getConfigProperties();
        final IDtoMemoryCache<ExpandedObservation> cacheObs = holder.getCacheObs();
        final IDtoMemoryCache<Instant> cacheHl = holder.getCacheHl();

        PathContext pathContext = new PathContext(MAPPER, provider, session, resource, configProperties, cacheObs,
                cacheHl);
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
            ExpandedObservation obs = null;
            if (resource != null && resource.getName().equals("lastObservation") && resource.getValue() != null) {
                obs = DtoMapperSimple.parseExpandObservation(MAPPER, resource.getValue().getValue());
            }
            switch (holder.getContext()) {
            case THINGS:
                return new ThingPathHandlerSensorthings(pathContext).handle(path);
            case FEATURES_OF_INTEREST:
                if (obs == null)
                    return null;
                return new FeatureOfInterestPathHandlerSensorthings(pathContext, obs).handle(path);
            case HISTORICAL_LOCATIONS:
                return new HistoricalLocationPathHandlerSensorthings(pathContext).handle(path);
            case LOCATIONS:
                return new LocationPathHandlerSensorthings(pathContext).handle(path);
            case OBSERVATIONS:
                if (obs == null)
                    return null;
                return new ObservationPathHandlerSensorthings(pathContext, obs).handle(path);
            case DATASTREAMS:
                return new DatastreamPathHandlerSensorthings(pathContext).handle(path);
            case OBSERVED_PROPERTIES:
                return new ObservedPropertyPathHandlerSensorthings(pathContext).handle(path);
            case SENSORS:
                return new SensorPathHandlerSensorthings(pathContext).handle(path);
            }
        }
        throw new UnsupportedRuleException("Path of " + holder.getContext() + " is not yet supported");
    }

    private boolean isSensorthingModel(final ProviderSnapshot provider) {
        return eNS_URI.equals(provider.getModelPackageUri());
    }
}
