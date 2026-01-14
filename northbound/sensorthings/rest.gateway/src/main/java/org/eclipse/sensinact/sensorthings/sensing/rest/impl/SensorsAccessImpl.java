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

import java.util.List;

import jakarta.ws.rs.core.Response;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedSensor;
import org.eclipse.sensinact.sensorthings.sensing.rest.UtilDto;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.SensorsAccess;
import org.eclipse.sensinact.sensorthings.sensing.rest.annotation.PaginationLimit;
import org.eclipse.sensinact.sensorthings.sensing.rest.delete.SensorsDelete;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.extended.DtoMapper;
import org.eclipse.sensinact.sensorthings.sensing.rest.update.SensorsUpdate;

import jakarta.ws.rs.NotFoundException;

public class SensorsAccessImpl extends AbstractAccess implements SensorsDelete, SensorsAccess, SensorsUpdate {

    @Override
    public Sensor getSensor(String id) {
        String providerId = UtilDto.extractFirstIdSegment(id);
        return DtoMapper.toSensor(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(EFilterContext.SENSORS), validateAndGetProvider(providerId));
    }

    @Override
    public ResultList<Datastream> getSensorDatastreams(String id) {
        ResultList<Datastream> list = new ResultList<>(null, null, List.of(getSensorDatastream(id, id)));
        return list;
    }

    @Override
    public Datastream getSensorDatastream(String id, String id2) {
        String providerId = UtilDto.extractFirstIdSegment(id);
        String providerId2 = UtilDto.extractFirstIdSegment(id2);

        if (!providerId.equals(providerId2)) {
            throw new NotFoundException();
        }

        return DtoMapper.toDatastream(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(EFilterContext.DATASTREAMS), validateAndGetProvider(providerId2));
    }

    @PaginationLimit(500)
    @Override
    public ResultList<Observation> getSensorDatastreamObservations(String id, String id2) {
        String providerId = UtilDto.extractFirstIdSegment(id);
        String providerId2 = UtilDto.extractFirstIdSegment(id2);
        if (!providerId.equals(providerId2)) {
            throw new NotFoundException();
        }
        return RootResourceAccessImpl.getObservationList(getSession(), application, getMapper(), uriInfo,
                getExpansions(), validateAndGetResourceSnapshot(id), parseFilter(EFilterContext.OBSERVATIONS), 0);
    }

    @Override
    public ObservedProperty getSensorDatastreamObservedProperty(String id, String id2) {
        String providerId = UtilDto.extractFirstIdSegment(id);
        String providerId2 = UtilDto.extractFirstIdSegment(id2);
        if (!providerId.equals(providerId2)) {
            throw new NotFoundException();
        }

        ObservedProperty o = DtoMapper.toObservedProperty(getSession(), application, getMapper(), uriInfo,
                getExpansions(), parseFilter(EFilterContext.OBSERVED_PROPERTIES), validateAndGetProvider(providerId2));

        if (!id.equals(o.id())) {
            throw new NotFoundException();
        }

        return o;
    }

    @Override
    public Sensor getSensorDatastreamSensor(String id, String id2) {
        String providerId = UtilDto.extractFirstIdSegment(id);
        String providerId2 = UtilDto.extractFirstIdSegment(id2);
        if (!providerId.equals(providerId2)) {
            throw new NotFoundException();
        }

        return getSensor(id);
    }

    @Override
    public Thing getSensorDatastreamThing(String id, String id2) {
        String thingId = getThingIdFromDatastream(id);
        ProviderSnapshot providerSnapshot = validateAndGetProvider(thingId);

        Thing t = DtoMapper.toThing(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(EFilterContext.THINGS), providerSnapshot);
        if (!thingId.equals(t.id())) {
            throw new NotFoundException();
        }
        return t;
    }

    @Override
    public Response updateSensor(String id, ExpandedSensor sensor) {
        getExtraDelegate().update(getSession(), getMapper(), uriInfo, requestContext.getMethod(), id, sensor);
        return Response.noContent().build();
    }

    @Override
    public Response patchSensor(String id, ExpandedSensor sensor) {
        return updateSensor(id, sensor);
    }

    @Override
    public Response deleteSensor(String id) {
        getExtraDelegate().delete(getSession(), getMapper(), uriInfo, id, ExpandedSensor.class);

        return Response.noContent().build();
    }

}
