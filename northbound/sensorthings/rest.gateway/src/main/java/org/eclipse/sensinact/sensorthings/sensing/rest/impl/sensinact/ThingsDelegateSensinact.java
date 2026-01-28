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
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensinact.DtoMapper.getTimestampFromId;
import java.util.List;
import java.util.Optional;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
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

public class ThingsDelegateSensinact extends AbstractDelegate {

    public ThingsDelegateSensinact(UriInfo uriInfo, Providers providers, Application application,
            ContainerRequestContext requestContext) {
        super(uriInfo, providers, application, requestContext);
        // TODO Auto-generated constructor stub
    }

    public Thing getThing(String id) {
        ProviderSnapshot providerSnapshot = validateAndGetProvider(id);

        return DtoMapper.toThing(getSession(), application, getMapper(), uriInfo, getExpansions(), parseFilter(THINGS),
                providerSnapshot);
    }

    public ResultList<Datastream> getThingDatastreams(String id) {
        ProviderSnapshot providerSnapshot = validateAndGetProvider(id);

        return DatastreamsDelegateSensinact.getDataStreams(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(DATASTREAMS), providerSnapshot);
    }

    public Datastream getThingDatastream(String id, String id2) {
        String provider = extractFirstIdSegment(id2);

        if (!id.equals(provider)) {
            throw new NotFoundException();
        }

        Datastream d = DtoMapper.toDatastream(getSession(), application, getMapper(), uriInfo, getExpansions(),
                validateAndGetResourceSnapshot(id2), parseFilter(DATASTREAMS));

        if (!id2.equals(d.id())) {
            throw new NotFoundException();
        }
        return d;
    }

    @PaginationLimit(500)

    public ResultList<Observation> getThingDatastreamObservations(String id, String id2) {
        String provider = extractFirstIdSegment(id2);

        if (!id.equals(provider)) {
            throw new NotFoundException();
        }

        return RootResourceDelegateSensinact.getObservationList(getSession(), application, getMapper(), uriInfo,
                requestContext, validateAndGetResourceSnapshot(id2), parseFilter(OBSERVATIONS));
    }

    public ObservedProperty getThingDatastreamObservedProperty(String id, String id2) {
        String provider = extractFirstIdSegment(id2);

        if (!id.equals(provider)) {
            throw new NotFoundException();
        }

        ObservedProperty o = DtoMapper.toObservedProperty(getSession(), application, getMapper(), uriInfo,
                getExpansions(), parseFilter(OBSERVED_PROPERTIES), validateAndGetResourceSnapshot(id2));

        if (!id2.equals(o.id())) {
            throw new NotFoundException();
        }

        return o;
    }

    public Sensor getThingDatastreamSensor(String id, String id2) {
        String provider = extractFirstIdSegment(id2);

        if (!id.equals(provider)) {
            throw new NotFoundException();
        }

        Sensor s = DtoMapper.toSensor(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(SENSORS), validateAndGetResourceSnapshot(id2));

        if (!id2.equals(s.id())) {
            throw new NotFoundException();
        }

        return s;
    }

    public Thing getThingDatastreamThing(String id, String id2) {
        String provider = extractFirstIdSegment(id2);

        if (!id.equals(provider)) {
            throw new NotFoundException();
        }

        return getThing(id);
    }

    public ResultList<HistoricalLocation> getThingHistoricalLocations(String id) {
        String provider = extractFirstIdSegment(id);

        try {
            ICriterion filter = parseFilter(HISTORICAL_LOCATIONS);
            ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);
            ResultList<HistoricalLocation> list = HistoryResourceHelperSensinact.loadHistoricalLocations(getSession(),
                    application, getMapper(), uriInfo, getExpansions(), filter, providerSnapshot, 0);
            if (list.value().isEmpty()) {
                list = new ResultList<>(null, null,
                        DtoMapper.toHistoricalLocation(getSession(), application, getMapper(), uriInfo, getExpansions(),
                                filter, providerSnapshot).map(List::of).orElse(List.of()));
            }
            return list;
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException();
        }
    }

    public HistoricalLocation getThingHistoricalLocation(String id, String id2) {
        String provider = extractFirstIdSegment(id2);

        if (!id.equals(provider)) {
            throw new NotFoundException();
        }

        getTimestampFromId(id2);

        try {
            Optional<HistoricalLocation> hl = DtoMapper.toHistoricalLocation(getSession(), application, getMapper(),
                    uriInfo, getExpansions(), parseFilter(HISTORICAL_LOCATIONS), validateAndGetProvider(provider));
            if (hl.isEmpty()) {
                throw new NotFoundException();
            }
            return hl.get();
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException();
        }
    }

    public Thing getThingHistoricalLocationsThing(String id, String id2) {
        String provider = extractFirstIdSegment(id2);

        if (!id.equals(provider)) {
            throw new NotFoundException();
        }
        return getThing(id);
    }

    public ResultList<Location> getThingHistoricalLocationLocations(String id, String id2) {
        String provider = extractFirstIdSegment(id2);

        if (!id.equals(provider)) {
            throw new NotFoundException();
        }

        getTimestampFromId(id2);

        ResultList<Location> list = new ResultList<>(null, null, List.of(DtoMapper.toLocation(getSession(), application,
                getMapper(), uriInfo, getExpansions(), parseFilter(LOCATIONS), validateAndGetProvider(provider))));

        return list;
    }

    public ResultList<Location> getThingLocations(String id) {
        String provider = extractFirstIdSegment(id);

        ResultList<Location> list = new ResultList<>(null, null, List.of(DtoMapper.toLocation(getSession(), application,
                getMapper(), uriInfo, getExpansions(), parseFilter(LOCATIONS), validateAndGetProvider(provider))));

        return list;
    }

    public Location getThingLocation(String id, String id2) {
        String provider = extractFirstIdSegment(id2);

        if (!id.equals(provider)) {
            throw new NotFoundException();
        }

        getTimestampFromId(id2);

        Location l = DtoMapper.toLocation(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(LOCATIONS), validateAndGetProvider(provider));

        if (!id2.equals(l.id())) {
            throw new NotFoundException();
        }
        return l;
    }

    public ResultList<Thing> getThingLocationThings(String id, String id2) {
        String provider = extractFirstIdSegment(id2);

        if (!id.equals(provider)) {
            throw new NotFoundException();
        }
        return new ResultList<>(null, null, List.of(getThing(id)));
    }

    public ResultList<HistoricalLocation> getThingLocationHistoricalLocations(String id, String id2) {
        String provider = extractFirstIdSegment(id2);
        try {
            ICriterion filter = parseFilter(HISTORICAL_LOCATIONS);
            ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);
            ResultList<HistoricalLocation> list = HistoryResourceHelperSensinact.loadHistoricalLocations(getSession(),
                    application, getMapper(), uriInfo, getExpansions(), filter, providerSnapshot, 0);
            if (list.value().isEmpty()) {
                list = new ResultList<>(null, null,
                        DtoMapper.toHistoricalLocation(getSession(), application, getMapper(), uriInfo, getExpansions(),
                                filter, providerSnapshot).map(List::of).orElse(List.of()));
            }
            return list;
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException();
        }
    }

}
