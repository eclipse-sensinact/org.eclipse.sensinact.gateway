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

import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.DATASTREAMS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.HISTORICAL_LOCATIONS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.LOCATIONS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.THINGS;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensinact.DtoMapper.extractFirstIdSegment;

import java.util.List;
import java.util.Optional;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.AbstractDelegate;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Providers;

public class LocationsDelegateSensinact extends AbstractDelegate {

    public LocationsDelegateSensinact(UriInfo uriInfo, Providers providers, Application application,
            ContainerRequestContext requestContext) {
        super(uriInfo, providers, application, requestContext);
        // TODO Auto-generated constructor stub
    }

    public ResultList<Location> getThingLocations(String id) {
        // TODO
        return new ResultList<Location>(null, null, List.of());
    }

    public Location getLocation(String id) {
        String provider = DtoMapper.extractFirstIdSegment(id);
        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);
        Location l = DtoMapper.toLocation(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(LOCATIONS), providerSnapshot);

        if (!id.equals(l.id())) {
            throw new NotFoundException();
        }

        return l;
    }

    public ResultList<HistoricalLocation> getLocationHistoricalLocations(String id) {
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

    public HistoricalLocation getLocationHistoricalLocation(String id, String id2) {
        String provider = DtoMapper.extractFirstIdSegment(id);
        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);
        Optional<HistoricalLocation> hl = DtoMapper.toHistoricalLocation(getSession(), application, getMapper(),
                uriInfo, getExpansions(), parseFilter(HISTORICAL_LOCATIONS), providerSnapshot);

        if (hl.isEmpty() || !id2.equals(hl.get().id())) {
            throw new NotFoundException();
        }
        return hl.get();
    }

    public Thing getLocationHistoricalLocationsThing(String id, String id2) {
        if (!id2.equals(id)) {
            throw new NotFoundException();
        }
        String provider = DtoMapper.extractFirstIdSegment(id);
        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);
        return DtoMapper.toThing(getSession(), application, getMapper(), uriInfo, getExpansions(), parseFilter(THINGS),
                providerSnapshot);
    }

    public ResultList<Location> getLocationHistoricalLocationLocations(String id, String id2) {
        return new ResultList<>(null, null, List.of(getLocation(id)));
    }

    public ResultList<Thing> getLocationThings(String id) {
        return new ResultList<>(null, null, List.of(getLocationThing(id, id)));
    }

    public Thing getLocationThing(String id, String id2) {
        String provider = DtoMapper.extractFirstIdSegment(id);
        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);
        return DtoMapper.toThing(getSession(), application, getMapper(), uriInfo, getExpansions(), parseFilter(THINGS),
                providerSnapshot);
    }

    public ResultList<Datastream> getLocationThingDatastreams(String id, String id2) {
        String provider = extractFirstIdSegment(id);
        return DatastreamsDelegateSensinact.getDataStreams(getSession(), application, getMapper(), uriInfo,
                getExpansions(), parseFilter(DATASTREAMS), validateAndGetProvider(provider));
    }

    public ResultList<HistoricalLocation> getLocationThingHistoricalLocations(String id, String id2) {
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

    public ResultList<Location> getLocationThingLocations(String id, String id2) {
        return new ResultList<>(null, null, List.of(getLocation(id)));
    }

    public Sensor getLocationThingDatastreamSensor(String id) {
        return DtoMapper.toSensor(getSession(), application, getMapper(), uriInfo, getExpansions(), null,
                validateAndGetResourceSnapshot(id));
    }

    public ObservedProperty getLocationThingDatastreamObservedProperty(String id) {
        return DtoMapper.toObservedProperty(getSession(), application, getMapper(), uriInfo, getExpansions(), null,
                validateAndGetResourceSnapshot(id));
    }

    public ResultList<Observation> getLocationThingDatastreamObservations(String id) {
        return RootResourceDelegateSensinact.getObservationList(getSession(), application, getMapper(), uriInfo,
                requestContext, validateAndGetResourceSnapshot(id), null);
    }

    public FeatureOfInterest getLocationThingDatastreamObservationFeatureOfInterest(String id) {
        return DtoMapper.toFeatureOfInterest(getSession(), application, getMapper(), uriInfo, getExpansions(), null,
                validateAndGetProvider(DtoMapperSimple.extractFirstIdSegment(id)));
    }

    public Thing getLocationThingHistoricalLocation(String id) {
        return DtoMapper.toThing(getSession(), application, getMapper(), uriInfo, getExpansions(), null,
                validateAndGetProvider(DtoMapperSimple.extractFirstIdSegment(id)));
    }

}
