/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensinact;

import static java.util.stream.Collectors.toList;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.DATASTREAMS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.FEATURES_OF_INTEREST;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.HISTORICAL_LOCATIONS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.LOCATIONS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.OBSERVED_PROPERTIES;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.SENSORS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.THINGS;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensinact.DtoMapper.extractFirstIdSegment;

import java.util.List;
import java.util.Optional;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.SensorthingsAnnotations;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.rest.ExpansionSettings;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.AbstractDelegate;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Providers;

public class DatastreamsDelegateSensinact extends AbstractDelegate {

    public DatastreamsDelegateSensinact(UriInfo uriInfo, Providers providers, Application application,
            ContainerRequestContext requestContext) {
        super(uriInfo, providers, application, requestContext);
    }

    public Datastream getDatastream(String id) {
        return DtoMapper.toDatastream(getSession(), application, getMapper(), uriInfo, getExpansions(),
                validateAndGetResourceSnapshot(id), parseFilter(DATASTREAMS));
    }

    public ResultList<Observation> getDatastreamObservations(String id) {
        ICriterion filter = parseFilter(EFilterContext.OBSERVATIONS);
        ResultList<Observation> observationList = RootResourceDelegateSensinact.getObservationList(getSession(), application,
                getMapper(), uriInfo, requestContext, validateAndGetResourceSnapshot(id), filter);
        return observationList;
    }

    public Observation getDatastreamObservation(String id, String id2) {
        ICriterion filter = parseFilter(EFilterContext.OBSERVATIONS);
        Optional<Observation> o = DtoMapper.toObservation(getSession(), application, getMapper(), uriInfo,
                getExpansions(), filter, validateAndGetResourceSnapshot(id));

        if (o.isEmpty() || !id2.equals(o.get().id())) {
            throw new NotFoundException();
        }
        return o.get();
    }

    public Datastream getDatastreamObservationDatastream(String id, String id2) {
        return getDatastream(id);
    }

    public FeatureOfInterest getDatastreamObservationFeatureOfInterest(String id, String id2) {
        String provider = extractFirstIdSegment(id);
        return DtoMapper.toFeatureOfInterest(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(FEATURES_OF_INTEREST), validateAndGetProvider(provider));
    }

    public ObservedProperty getDatastreamObservedProperty(String id) {
        ObservedProperty o = DtoMapper.toObservedProperty(getSession(), application, getMapper(), uriInfo,
                getExpansions(), parseFilter(OBSERVED_PROPERTIES), validateAndGetResourceSnapshot(id));

        if (!id.equals(o.id())) {
            throw new NotFoundException();
        }
        return o;
    }

    public ResultList<Datastream> getDatastreamObservedPropertyDatastreams(String id) {
        return new ResultList<>(null, null, List.of(getDatastream(id)));
    }

    public Sensor getDatastreamSensor(String id) {
        Sensor s = DtoMapper.toSensor(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(SENSORS), validateAndGetResourceSnapshot(id));

        if (!id.equals(s.id())) {
            throw new NotFoundException();
        }
        return s;
    }

    public ResultList<Datastream> getDatastreamSensorDatastreams(String id) {
        return getDatastreamObservedPropertyDatastreams(id);
    }

    public Thing getDatastreamThing(String id) {
        String provider = extractFirstIdSegment(id);
        return DtoMapper.toThing(getSession(), application, getMapper(), uriInfo, getExpansions(), parseFilter(THINGS),
                validateAndGetProvider(provider));
    }

    public ResultList<Datastream> getDatastreamThingDatastreams(String id) {
        String provider = extractFirstIdSegment(id);

        return getDataStreams(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(DATASTREAMS), validateAndGetProvider(provider));
    }

    public ResultList<HistoricalLocation> getDatastreamThingHistoricalLocations(String id) {
        String provider = extractFirstIdSegment(id);
        try {
            ICriterion filter = parseFilter(HISTORICAL_LOCATIONS);
            ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);
            ResultList<HistoricalLocation> list = HistoryResourceHelperSensinact.loadHistoricalLocations(getSession(),
                    application, getMapper(), uriInfo, getExpansions(), filter, providerSnapshot, 0);
            if (list.value().isEmpty())
                list = new ResultList<>(null, null,
                        DtoMapper.toHistoricalLocation(getSession(), application, getMapper(), uriInfo, getExpansions(),
                                filter, providerSnapshot).map(List::of).orElse(List.of()));
            return list;
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException();
        }
    }

    public ResultList<Location> getDatastreamThingLocations(String id) {
        String provider = extractFirstIdSegment(id);

        Location hl;
        try {
            hl = DtoMapper.toLocation(getSession(), application, getMapper(), uriInfo, getExpansions(),
                    parseFilter(LOCATIONS), validateAndGetProvider(provider));
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException();
        }

        return new ResultList<>(null, null, List.of(hl));
    }

    static ResultList<Datastream> getDataStreams(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter,
            ProviderSnapshot providerSnapshot) {
        return new ResultList<>(null, null, providerSnapshot.getServices().stream()
                .flatMap(s -> s.getResources().stream())
                .filter(r -> !r.getMetadata().containsKey(SensorthingsAnnotations.SENSORTHINGS_OBSERVEDAREA))
                .map(r -> DtoMapper.toDatastream(userSession, application, mapper, uriInfo, expansions, r, filter))
                .collect(toList()));
    }

}
