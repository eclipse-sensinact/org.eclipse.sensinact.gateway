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

import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.DATASTREAMS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.OBSERVATIONS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.OBSERVED_PROPERTIES;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.SENSORS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.THINGS;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.DtoMapperGet.extractFirstIdSegment;

import java.util.List;

import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.ObservedPropertiesAccess;
import org.eclipse.sensinact.sensorthings.sensing.rest.annotation.PaginationLimit;
import org.eclipse.sensinact.sensorthings.sensing.rest.update.ObservedPropertiesUpdate;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;

public class ObservedPropertiesAccessImpl extends AbstractAccess
        implements ObservedPropertiesAccess, ObservedPropertiesUpdate {

    @Override
    public ObservedProperty getObservedProperty(String id) {
        ObservedProperty o = DtoMapperGet.toObservedProperty(getSession(), application, getMapper(), uriInfo,
                getExpansions(), parseFilter(OBSERVED_PROPERTIES), validateAndGetResourceSnapshot(id));

        if (!id.equals(o.id())) {
            throw new NotFoundException();
        }

        return o;
    }

    @Override
    public ResultList<Datastream> getObservedPropertyDatastreams(String id) {
        return new ResultList<>(null, null, List.of(getObservedPropertyDatastream(id, id)));
    }

    @Override
    public Datastream getObservedPropertyDatastream(String id, String id2) {
        if (!id.equals(id2)) {
            throw new NotFoundException();
        }

        return DtoMapperGet.toDatastream(getSession(), application, getMapper(), uriInfo, getExpansions(),
                validateAndGetResourceSnapshot(id2), parseFilter(DATASTREAMS));
    }

    @PaginationLimit(500)
    @Override
    public ResultList<Observation> getObservedPropertyDatastreamObservations(String id, String id2) {
        if (!id.equals(id2)) {
            throw new NotFoundException();
        }
        return RootResourceAccessImpl.getObservationList(getSession(), application, getMapper(), uriInfo,
                getExpansions(), validateAndGetResourceSnapshot(id), parseFilter(OBSERVATIONS), 0);
    }

    @Override
    public ObservedProperty getObservedPropertyDatastreamObservedProperty(String id, String id2) {
        if (!id.equals(id2)) {
            throw new NotFoundException();
        }
        return getObservedProperty(id);
    }

    @Override
    public Sensor getObservedPropertyDatastreamSensor(String id, String id2) {
        if (!id.equals(id2)) {
            throw new NotFoundException();
        }

        return DtoMapperGet.toSensor(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(SENSORS), validateAndGetResourceSnapshot(id2));
    }

    @Override
    public Thing getObservedPropertyDatastreamThing(String id, String id2) {
        String provider = extractFirstIdSegment(id);
        String provider2 = extractFirstIdSegment(id2);
        if (!provider.equals(provider2)) {
            throw new NotFoundException();
        }
        return DtoMapperGet.toThing(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(THINGS), validateAndGetProvider(provider));
    }

    @Override
    public Response updateObservedProperties(String id, ObservedProperty observedProperty) {
        getExtraDelegate().update(getSession(), getMapper(), uriInfo, requestContext.getMethod(), id, observedProperty);

        return Response.noContent().build();
    }

}
