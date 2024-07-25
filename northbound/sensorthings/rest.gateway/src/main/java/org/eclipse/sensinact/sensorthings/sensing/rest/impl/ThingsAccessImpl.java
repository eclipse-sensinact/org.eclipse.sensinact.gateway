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
package org.eclipse.sensinact.sensorthings.sensing.rest.impl;

import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.DtoMapper.extractFirstIdSegment;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.DtoMapper.getTimestampFromId;

import java.util.List;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.rest.ThingsAccess;
import org.eclipse.sensinact.sensorthings.sensing.rest.annotation.PaginationLimit;

import jakarta.ws.rs.NotFoundException;

public class ThingsAccessImpl extends AbstractAccess implements ThingsAccess {

    @Override
    public Thing getThing(String id) {
        ProviderSnapshot providerSnapshot = validateAndGetProvider(id);

        return DtoMapper.toThing(getSession(), application, getMapper(), uriInfo,
                getExpansions(), providerSnapshot);
    }

    @Override
    public ResultList<Datastream> getThingDatastreams(String id) {
        ProviderSnapshot providerSnapshot = validateAndGetProvider(id);

        return DatastreamsAccessImpl.getDataStreams(getSession(), application, getMapper(),
                uriInfo, getExpansions(), providerSnapshot);
    }

    @Override
    public Datastream getThingDatastream(String id, String id2) {
        String provider = extractFirstIdSegment(id2);

        if (!id.equals(provider)) {
            throw new NotFoundException();
        }

        Datastream d = DtoMapper.toDatastream(getSession(), application, getMapper(), uriInfo,
                getExpansions(), validateAndGetResourceSnapshot(id2));

        if (!id2.equals(d.id)) {
            throw new NotFoundException();
        }
        return d;
    }

    @PaginationLimit(500)
    @Override
    public ResultList<Observation> getThingDatastreamObservations(String id, String id2) {
        String provider = extractFirstIdSegment(id2);

        if (!id.equals(provider)) {
            throw new NotFoundException();
        }

        return RootResourceAccessImpl.getObservationList(getSession(), application, getMapper(), uriInfo,
                getExpansions(), validateAndGetResourceSnapshot(id2), 0);
    }

    @Override
    public ObservedProperty getThingDatastreamObservedProperty(String id, String id2) {
        String provider = extractFirstIdSegment(id2);

        if (!id.equals(provider)) {
            throw new NotFoundException();
        }

        ObservedProperty o = DtoMapper.toObservedProperty(getSession(), application, getMapper(), uriInfo,
                getExpansions(), validateAndGetResourceSnapshot(id2));

        if (!id2.equals(o.id)) {
            throw new NotFoundException();
        }

        return o;
    }

    @Override
    public Sensor getThingDatastreamSensor(String id, String id2) {
        String provider = extractFirstIdSegment(id2);

        if (!id.equals(provider)) {
            throw new NotFoundException();
        }

        Sensor s = DtoMapper.toSensor(getSession(), application, getMapper(), uriInfo, getExpansions(),
                validateAndGetResourceSnapshot(id2));

        if (!id2.equals(s.id)) {
            throw new NotFoundException();
        }

        return s;
    }

    @Override
    public Thing getThingDatastreamThing(String id, String id2) {
        String provider = extractFirstIdSegment(id2);

        if (!id.equals(provider)) {
            throw new NotFoundException();
        }

        return getThing(id);
    }

    @Override
    public ResultList<HistoricalLocation> getThingHistoricalLocations(String id) {
        String provider = extractFirstIdSegment(id);

        HistoricalLocation hl;
        try {
            hl = DtoMapper.toHistoricalLocation(getSession(), application, getMapper(), uriInfo,
                    getExpansions(), validateAndGetProvider(provider));
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException();
        }

        ResultList<HistoricalLocation> list = new ResultList<>();
        list.value = List.of(hl);
        return list;
    }

    @Override
    public HistoricalLocation getThingHistoricalLocation(String id, String id2) {
        String provider = extractFirstIdSegment(id2);

        if (!id.equals(provider)) {
            throw new NotFoundException();
        }

        getTimestampFromId(id2);

        HistoricalLocation hl;
        try {
            hl = DtoMapper.toHistoricalLocation(getSession(), application, getMapper(), uriInfo,
                    getExpansions(), validateAndGetProvider(provider));
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException();
        }

        return hl;
    }

    @Override
    public Thing getThingHistoricalLocationsThing(String id, String id2) {
        String provider = extractFirstIdSegment(id2);

        if (!id.equals(provider)) {
            throw new NotFoundException();
        }
        return getThing(id);
    }

    @Override
    public ResultList<Location> getThingHistoricalLocationLocations(String id, String id2) {
        String provider = extractFirstIdSegment(id2);

        if (!id.equals(provider)) {
            throw new NotFoundException();
        }

        getTimestampFromId(id2);

        ResultList<Location> list = new ResultList<>();
        list.value = List.of(DtoMapper.toLocation(getSession(), application, getMapper(), uriInfo,
                getExpansions(), validateAndGetProvider(provider)));

        return list;
    }

    @Override
    public ResultList<Location> getThingLocations(String id) {
        String provider = extractFirstIdSegment(id);

        ResultList<Location> list = new ResultList<>();
        list.value = List.of(DtoMapper.toLocation(getSession(), application, getMapper(), uriInfo,
                getExpansions(), validateAndGetProvider(provider)));

        return list;
    }

    @Override
    public Location getThingLocation(String id, String id2) {
        String provider = extractFirstIdSegment(id2);

        if (!id.equals(provider)) {
            throw new NotFoundException();
        }

        getTimestampFromId(id2);

        Location l = DtoMapper.toLocation(getSession(), application, getMapper(), uriInfo,
                getExpansions(), validateAndGetProvider(provider));

        if (!id2.equals(l.id)) {
            throw new NotFoundException();
        }
        return l;
    }

    @Override
    public ResultList<Thing> getThingLocationThings(String id, String id2) {
        String provider = extractFirstIdSegment(id2);

        if (!id.equals(provider)) {
            throw new NotFoundException();
        }
        ResultList<Thing> list = new ResultList<>();
        list.value = List.of(getThing(id));
        return list;
    }

    @Override
    public ResultList<HistoricalLocation> getThingLocationHistoricalLocations(String id, String id2) {
        String provider = extractFirstIdSegment(id2);

        HistoricalLocation hl;
        try {
            hl = DtoMapper.toHistoricalLocation(getSession(), application, getMapper(), uriInfo,
                    getExpansions(), validateAndGetProvider(provider));
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException();
        }

        ResultList<HistoricalLocation> list = new ResultList<>();
        list.value = List.of(hl);
        return list;
    }
}
