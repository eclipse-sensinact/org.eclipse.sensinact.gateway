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
package org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensinact;

import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.DATASTREAMS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.HISTORICAL_LOCATIONS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.LOCATIONS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.OBSERVATIONS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.OBSERVED_PROPERTIES;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.SENSORS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.THINGS;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensinact.DtoMapper.extractFirstIdSegment;
import java.util.List;
import java.util.Optional;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.rest.annotation.PaginationLimit;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.AbstractDelegate;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Providers;

public class ObservedPropertiesDelegateSensinact extends AbstractDelegate {

    public ObservedPropertiesDelegateSensinact(UriInfo uriInfo, Providers providers, Application application,
            ContainerRequestContext requestContext) {
        super(uriInfo, providers, application, requestContext);
        // TODO Auto-generated constructor stub
    }

    public ObservedProperty getObservedProperty(String id) {
        ObservedProperty o = DtoMapper.toObservedProperty(getSession(), application, getMapper(), uriInfo,
                getExpansions(), parseFilter(OBSERVED_PROPERTIES), validateAndGetResourceSnapshot(id));

        if (!id.equals(o.id())) {
            throw new NotFoundException();
        }

        return o;
    }

    public ResultList<Datastream> getObservedPropertyDatastreams(String id) {
        return new ResultList<>(null, null, List.of(getObservedPropertyDatastream(id, id)));
    }

    public Datastream getObservedPropertyDatastream(String id, String id2) {
        if (!id.equals(id2)) {
            throw new NotFoundException();
        }

        return DtoMapper.toDatastream(getSession(), application, getMapper(), uriInfo, getExpansions(),
                validateAndGetResourceSnapshot(id2), parseFilter(DATASTREAMS));
    }

    @PaginationLimit(500)

    public ResultList<Observation> getObservedPropertyDatastreamObservations(String id, String id2) {
        if (!id.equals(id2)) {
            throw new NotFoundException();
        }
        return RootResourceDelegateSensinact.getObservationList(getSession(), application, getMapper(), uriInfo,
                getExpansions(), validateAndGetResourceSnapshot(id), parseFilter(OBSERVATIONS), 0);
    }

    public ObservedProperty getObservedPropertyDatastreamObservedProperty(String id, String id2) {
        if (!id.equals(id2)) {
            throw new NotFoundException();
        }
        return getObservedProperty(id);
    }

    public Sensor getObservedPropertyDatastreamSensor(String id, String id2) {
        if (!id.equals(id2)) {
            throw new NotFoundException();
        }

        return DtoMapper.toSensor(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(SENSORS), validateAndGetResourceSnapshot(id2));
    }

    public Thing getObservedPropertyDatastreamThing(String id, String id2) {
        String provider = extractFirstIdSegment(id);
        String provider2 = extractFirstIdSegment(id2);
        if (!provider.equals(provider2)) {
            throw new NotFoundException();
        }
        return DtoMapper.toThing(getSession(), application, getMapper(), uriInfo, getExpansions(), parseFilter(THINGS),
                validateAndGetProvider(provider));
    }

    public ResultList<Datastream> getObservedPropertyDatastreamThingDatastreams(String value, String value2) {
        return new ResultList<Datastream>(null, null, List.of(getObservedPropertyDatastream(value, value2)));
    }

    public ResultList<HistoricalLocation> getObservedPropertyDatastreamThingHistoricalLocations(String value,
            String value2) {
        String provider = extractFirstIdSegment(value2);
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

    public ResultList<Location> getObservedPropertyDatastreamThingLocations(String value, String value2) {
        String provider = extractFirstIdSegment(value2);

        ResultList<Location> list = new ResultList<>(null, null, List.of(DtoMapper.toLocation(getSession(), application,
                getMapper(), uriInfo, getExpansions(), parseFilter(LOCATIONS), validateAndGetProvider(provider))));

        return list;
    }

    public ResultList<Observation> getObservedPropertyDatastreamObservationFeatureOfInterestObservations(String value,
            String value2, String value3) {
        String provider = extractFirstIdSegment(value);
        String provider2 = extractFirstIdSegment(value2);
        String provider3 = extractFirstIdSegment(value3);

        if (!provider.equals(provider2) || !provider3.equals(provider)) {
            throw new NotFoundException();
        }
        // refactor TODO same part as datastream
        ICriterion filter = parseFilter(EFilterContext.OBSERVATIONS);
        ResultList<Observation> observationList = RootResourceDelegateSensinact.getObservationList(getSession(),
                application, getMapper(), uriInfo, requestContext, validateAndGetResourceSnapshot(provider), filter);
        return observationList;
    }

    public FeatureOfInterest getObservedPropertyDatastreamObservationFeatureOfInterest(String value, String value2,
            String value3) {
        String provider = extractFirstIdSegment(value);
        String provider2 = extractFirstIdSegment(value2);
        String provider3 = extractFirstIdSegment(value3);

        if (!provider.equals(provider2) || !provider3.equals(provider)) {
            throw new NotFoundException();
        }
        // TODO refacto same as datastream endpoint
        ICriterion filter = parseFilter(EFilterContext.OBSERVATIONS);
        return DtoMapper.toFeatureOfInterest(getSession(), application, getMapper(), uriInfo, getExpansions(), filter,
                validateAndGetProvider(provider3));
    }

    public Observation getObservedPropertyDatastreamObservation(String value, String value2, String value3) {
        String provider = extractFirstIdSegment(value);
        String provider2 = extractFirstIdSegment(value2);
        String provider3 = extractFirstIdSegment(value3);

        if (!provider.equals(provider2) || !provider3.equals(provider)) {
            throw new NotFoundException();
        }
        // TODO refacto same as datastream endpoint
        ICriterion filter = parseFilter(EFilterContext.OBSERVATIONS);
        Optional<Observation> o = DtoMapper.toObservation(getSession(), application, getMapper(), uriInfo,
                getExpansions(), filter, validateAndGetResourceSnapshot(value3));

        return o.get();
    }

}
