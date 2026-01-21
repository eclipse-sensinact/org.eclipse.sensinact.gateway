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
package org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensorthings;

import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.DATASTREAMS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.OBSERVATIONS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.OBSERVED_PROPERTIES;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.SENSORS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.THINGS;

import java.util.List;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Providers;

import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.rest.annotation.PaginationLimit;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.AbstractDelegate;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.container.ContainerRequestContext;

public class ObservedPropertiesDelegateSensorthings extends AbstractDelegate {

    public ObservedPropertiesDelegateSensorthings(UriInfo uriInfo, Providers providers, Application application,
            ContainerRequestContext requestContext) {
        super(uriInfo, providers, application, requestContext);
        // TODO Auto-generated constructor stub
    }

    public ObservedProperty getObservedProperty(String id) {
        String datastreamId = DtoMapperSimple.extractFirstIdSegment(id);
        ObservedProperty o = DtoMapper.toObservedProperty(getSession(), application, getMapper(), uriInfo,
                getExpansions(), parseFilter(OBSERVED_PROPERTIES), validateAndGetProvider(datastreamId));

        if (!id.equals(o.id())) {
            throw new NotFoundException();
        }

        return o;
    }

    public ResultList<Datastream> getObservedPropertyDatastreams(String id) {
        return new ResultList<>(null, null, List.of(getObservedPropertyDatastream(id, id)));
    }

    public Datastream getObservedPropertyDatastream(String id, String id2) {
        String datastreamId = DtoMapperSimple.extractFirstIdSegment(id);
        String datastreamId2 = DtoMapperSimple.extractFirstIdSegment(id2);

        if (!datastreamId.equals(datastreamId2)) {
            throw new NotFoundException();
        }

        return DtoMapper.toDatastream(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(DATASTREAMS), validateAndGetProvider(datastreamId));
    }

    @PaginationLimit(500)

    public ResultList<Observation> getObservedPropertyDatastreamObservations(String id, String id2) {
        String datastreamId = DtoMapperSimple.extractFirstIdSegment(id);
        String datastreamId2 = DtoMapperSimple.extractFirstIdSegment(id2);
        if (!datastreamId.equals(datastreamId2)) {
            throw new NotFoundException();
        }
        return RootResourceDelegateSensorthings.getObservationList(getSession(), application, getMapper(), uriInfo,
                getExpansions(), getObservationResourceSnapshot(datastreamId2), parseFilter(OBSERVATIONS), 0);
    }

    public ObservedProperty getObservedPropertyDatastreamObservedProperty(String id, String id2) {
        String providerDatastream2 = DtoMapperSimple.extractFirstIdSegment(id2);
        String providerDatastream = DtoMapperSimple.extractFirstIdSegment(id);

        if (!providerDatastream.equals(providerDatastream2)) {
            throw new NotFoundException();
        }
        return getObservedProperty(id);
    }

    public Sensor getObservedPropertyDatastreamSensor(String id, String id2) {
        if (!id.equals(id2)) {
            throw new NotFoundException();
        }

        return DtoMapper.toSensor(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(SENSORS), validateAndGetProvider(id2));
    }

    public Thing getObservedPropertyDatastreamThing(String id, String id2) {
        String provider = DtoMapperSimple.extractFirstIdSegment(id);
        String provider2 = DtoMapperSimple.extractFirstIdSegment(id2);
        if (!provider.equals(provider2)) {
            throw new NotFoundException();
        }
        return DtoMapper.toThing(getSession(), application, getMapper(), uriInfo, getExpansions(), parseFilter(THINGS),
                validateAndGetProvider(provider));
    }

    public Response updateObservedProperties(String id, ObservedProperty observedProperty) {
        getExtraDelegate().update(getSession(), getMapper(), uriInfo, requestContext.getMethod(), id, observedProperty);

        return Response.noContent().build();
    }

    public Response patchObservedProperties(String id, ObservedProperty observedProperty) {
        return updateObservedProperties(id, observedProperty);
    }

    public Response deleteObservedProperty(String id) {
        getExtraDelegate().delete(getSession(), getMapper(), uriInfo, id, ObservedProperty.class);

        return Response.noContent().build();
    }

}
