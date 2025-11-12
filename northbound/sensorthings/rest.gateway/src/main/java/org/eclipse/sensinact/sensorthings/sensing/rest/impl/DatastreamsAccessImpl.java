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
package org.eclipse.sensinact.sensorthings.sensing.rest.impl;

import static java.util.stream.Collectors.toList;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.DATASTREAMS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.FEATURES_OF_INTEREST;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.HISTORICAL_LOCATIONS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.LOCATIONS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.OBSERVED_PROPERTIES;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.SENSORS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.THINGS;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.DtoMapper.extractFirstIdSegment;

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
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.rest.DatastreamsAccess;
import org.eclipse.sensinact.sensorthings.sensing.rest.ExpansionSettings;
import org.eclipse.sensinact.sensorthings.sensing.rest.annotation.PaginationLimit;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.UriInfo;

public class DatastreamsAccessImpl extends AbstractAccess implements DatastreamsAccess {

    @Override
    public Datastream getDatastream(String id) {
        return DtoMapper.toDatastream(getSession(), application, getMapper(), uriInfo, getExpansions(),
                validateAndGetResourceSnapshot(id), parseFilter(DATASTREAMS));
    }

    @PaginationLimit(500)
    @Override
    public ResultList<Observation> getDatastreamObservations(String id) {
        ICriterion filter = parseFilter(EFilterContext.OBSERVATIONS);
        ResultList<Observation> observationList = RootResourceAccessImpl.getObservationList(getSession(), application, getMapper(), uriInfo,
                requestContext, validateAndGetResourceSnapshot(id), filter);
        return observationList;
    }

    @Override
    public Observation getDatastreamObservation(String id, String id2) {
        ICriterion filter = parseFilter(EFilterContext.OBSERVATIONS);
        Optional<Observation> o = DtoMapper.toObservation(getSession(), application, getMapper(), uriInfo, getExpansions(),
                filter, validateAndGetResourceSnapshot(id));

        if (o.isEmpty() || !id2.equals(o.get().id)) {
            throw new NotFoundException();
        }
        return o.get();
    }

    @Override
    public Datastream getDatastreamObservationDatastream(String id, String id2) {
        return getDatastream(id);
    }

    @Override
    public FeatureOfInterest getDatastreamObservationFeatureOfInterest(String id, String id2) {
        String provider = extractFirstIdSegment(id);
        return DtoMapper.toFeatureOfInterest(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(FEATURES_OF_INTEREST), validateAndGetProvider(provider));
    }

    @Override
    public ObservedProperty getDatastreamObservedProperty(String id) {
        ObservedProperty o = DtoMapper.toObservedProperty(getSession(), application, getMapper(),
                uriInfo, getExpansions(), parseFilter(OBSERVED_PROPERTIES), validateAndGetResourceSnapshot(id));

        if (!id.equals(o.id)) {
            throw new NotFoundException();
        }
        return o;
    }

    @Override
    public ResultList<Datastream> getDatastreamObservedPropertyDatastreams(String id) {
        ResultList<Datastream> list = new ResultList<>();
        list.value = List.of(getDatastream(id));
        return list;
    }

    @Override
    public Sensor getDatastreamSensor(String id) {
        Sensor s = DtoMapper.toSensor(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(SENSORS), validateAndGetResourceSnapshot(id));

        if (!id.equals(s.id)) {
            throw new NotFoundException();
        }
        return s;
    }

    @Override
    public ResultList<Datastream> getDatastreamSensorDatastreams(String id) {
        return getDatastreamObservedPropertyDatastreams(id);
    }

    @Override
    public Thing getDatastreamThing(String id) {
        String provider = extractFirstIdSegment(id);
        return DtoMapper.toThing(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(THINGS), validateAndGetProvider(provider));
    }

    @Override
    public ResultList<Datastream> getDatastreamThingDatastreams(String id) {
        String provider = extractFirstIdSegment(id);

        return getDataStreams(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(DATASTREAMS), validateAndGetProvider(provider));
    }

    @Override
    public ResultList<HistoricalLocation> getDatastreamThingHistoricalLocations(String id) {
        String provider = extractFirstIdSegment(id);
        try {
            ICriterion filter = parseFilter(HISTORICAL_LOCATIONS);
            ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);
            ResultList<HistoricalLocation> list = HistoryResourceHelper.loadHistoricalLocations(getSession(),
                    application, getMapper(), uriInfo, getExpansions(), filter, providerSnapshot, 0);
            if (list.value.isEmpty())
                DtoMapper.toHistoricalLocation(getSession(), application, getMapper(), uriInfo,
                        getExpansions(), filter, providerSnapshot).ifPresent(list.value::add);
            return list;
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException();
        }
    }

    @Override
    public ResultList<Location> getDatastreamThingLocations(String id) {
        String provider = extractFirstIdSegment(id);

        Location hl;
        try {
            hl = DtoMapper.toLocation(getSession(), application, getMapper(), uriInfo,
                    getExpansions(), parseFilter(LOCATIONS), validateAndGetProvider(provider));
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException();
        }

        ResultList<Location> list = new ResultList<>();
        list.value = List.of(hl);
        return list;
    }

    static ResultList<Datastream> getDataStreams(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter,
            ProviderSnapshot providerSnapshot) {
        ResultList<Datastream> list = new ResultList<>();
        list.value = providerSnapshot.getServices().stream()
                .flatMap(s -> s.getResources().stream())
                .filter(r -> !r.getMetadata().containsKey(SensorthingsAnnotations.SENSORTHINGS_OBSERVEDAREA))
                .map(r -> DtoMapper.toDatastream(userSession, application, mapper, uriInfo, expansions, r, filter)).collect(toList());
        return list;
    }
}
