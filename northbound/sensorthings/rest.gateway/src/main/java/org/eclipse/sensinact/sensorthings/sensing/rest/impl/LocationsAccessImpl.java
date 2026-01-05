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

import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.DATASTREAMS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.HISTORICAL_LOCATIONS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.LOCATIONS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.THINGS;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.DtoMapperGet.extractFirstIdSegment;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedLocation;
import org.eclipse.sensinact.sensorthings.sensing.rest.UtilIds;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.LocationsAccess;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.extended.DtoMapper;
import org.eclipse.sensinact.sensorthings.sensing.rest.update.LocationsUpdate;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;

public class LocationsAccessImpl extends AbstractAccess implements LocationsAccess, LocationsUpdate {

    @Override
    public Location getLocation(String id) {
        ProviderSnapshot providerSnapshot = validateAndGetProvider(id);
        Location l = DtoMapper.toLocation(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(LOCATIONS), providerSnapshot.getService(UtilIds.SERVICE_LOCATON));

        if (!id.equals(l.id())) {
            throw new NotFoundException();
        }

        return l;
    }

    @Override
    public ResultList<HistoricalLocation> getLocationHistoricalLocations(String id) {
        try {
            ICriterion filter = parseFilter(HISTORICAL_LOCATIONS);
            ProviderSnapshot providerSnapshot = validateAndGetProvider(id);
            ServiceSnapshot serviceSnapshot = UtilIds.getLocationService(providerSnapshot);
            ResultList<HistoricalLocation> list = HistoryResourceHelper.loadHistoricalLocations(getSession(),
                    application, getMapper(), uriInfo, getExpansions(), filter, providerSnapshot, 0);
            if (list.value().isEmpty()) {
                list = new ResultList<>(null, null,
                        DtoMapper.toHistoricalLocation(getSession(), application, getMapper(), uriInfo, getExpansions(),
                                filter, serviceSnapshot).map(List::of).orElse(List.of()));
            }
            return list;
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException();
        }
    }

    @Override
    public HistoricalLocation getLocationHistoricalLocation(String id, String id2) {
        ProviderSnapshot providerSnapshot = validateAndGetProvider(id);
        ServiceSnapshot service = UtilIds.getLocationService(providerSnapshot);
        if (service == null) {
            throw new NotFoundException();
        }
        Optional<HistoricalLocation> hl = DtoMapper.toHistoricalLocation(getSession(), application, getMapper(),
                uriInfo, getExpansions(), parseFilter(HISTORICAL_LOCATIONS), service);

        if (hl.isEmpty() || !id2.equals(hl.get().id())) {
            throw new NotFoundException();
        }
        return hl.get();
    }

    @Override
    public Thing getLocationHistoricalLocationsThing(String id, String id2) {
        if (!id2.equals(id)) {
            throw new NotFoundException();
        }
        Optional<ServiceSnapshot> thing = listProviders(parseFilter(THINGS)).stream().map(UtilIds::getThingService)
                .filter(Objects::nonNull).findAny();
        if (thing.isEmpty()) {
            throw new NotFoundException();
        }
        ServiceSnapshot service = thing.get();
        @SuppressWarnings("unchecked")
        List<String> locationIds = (List<String>) UtilIds.getResourceField(service, "locationIds", Object.class);
        if (!locationIds.contains(id)) {
            throw new NotFoundException();
        }
        return DtoMapper.toThing(getSession(), application, getMapper(), uriInfo, getExpansions(), parseFilter(THINGS),
                thing.get());
    }

    @Override
    public ResultList<Location> getLocationHistoricalLocationLocations(String id, String id2) {
        return new ResultList<>(null, null, List.of(getLocation(id)));
    }

    @Override
    public ResultList<Thing> getLocationThings(String id) {

        ICriterion criterion = parseFilter(EFilterContext.THINGS);
        List<ProviderSnapshot> providers = listProviders(criterion);
        return new ResultList<>(null, null,
                providers.stream().map(p -> UtilIds.getThingService(p)).filter(Objects::nonNull).filter(s -> {
                    @SuppressWarnings("unchecked")
                    List<String> locationIdsThing = (List<String>) UtilIds.getResourceField(s, "locationIds",
                            Object.class);
                    return locationIdsThing.contains(id);
                }).map(s -> DtoMapper.toThing(getSession(), application, getMapper(), uriInfo, getExpansions(),
                        criterion, s)).toList());
    }

    @Override
    public Thing getLocationThing(String id, String id2) {
        ProviderSnapshot providerSnapshot = validateAndGetProvider(id2);
        if (providerSnapshot == null) {
            throw new NotFoundException();
        }
        ServiceSnapshot service = UtilIds.getThingService(providerSnapshot);
        if (service == null) {
            throw new NotFoundException();
        }
        @SuppressWarnings("unchecked")
        List<String> locationIds = (List<String>) UtilIds.getResourceField(service, "locationIds", Object.class);
        if (!locationIds.contains(id)) {
            throw new NotFoundException();

        }
        return DtoMapper.toThing(getSession(), application, getMapper(), uriInfo, getExpansions(), parseFilter(THINGS),
                service);
    }

    @Override
    public ResultList<Datastream> getLocationThingDatastreams(String id, String id2) {

        return DatastreamsAccessImpl.getDataStreams(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(DATASTREAMS), DatastreamsAccessImpl.getListDatastreamServices(getSession(), id2));
    }

    @Override
    public ResultList<HistoricalLocation> getLocationThingHistoricalLocations(String id, String id2) {
        String provider = extractFirstIdSegment(id);
        try {
            ICriterion filter = parseFilter(HISTORICAL_LOCATIONS);
            ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);
            ResultList<HistoricalLocation> list = HistoryResourceHelper.loadHistoricalLocations(getSession(),
                    application, getMapper(), uriInfo, getExpansions(), filter, providerSnapshot, 0);
            if (list.value().isEmpty()) {
                list = new ResultList<>(null, null,
                        DtoMapperGet.toHistoricalLocation(getSession(), application, getMapper(), uriInfo,
                                getExpansions(), filter, providerSnapshot).map(List::of).orElse(List.of()));
            }
            return list;
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException();
        }
    }

    @Override
    public ResultList<Location> getLocationThingLocations(String id, String id2) {
        return new ResultList<>(null, null, List.of(getLocation(id)));
    }

    @Override
    public Response updateLocation(String id, ExpandedLocation location) {
        getExtraDelegate().update(getSession(), getMapper(), uriInfo, requestContext.getMethod(), id, location);

        return Response.noContent().build();
    }

    @Override
    public Response patchLocation(String id, ExpandedLocation location) {
        return updateLocation(id, location);
    }

}
