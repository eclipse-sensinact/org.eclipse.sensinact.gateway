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

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.rest.UtilIds;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IDtoMemoryCache;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.ObservedPropertiesAccess;
import org.eclipse.sensinact.sensorthings.sensing.rest.annotation.PaginationLimit;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.extended.DtoMapper;
import org.eclipse.sensinact.sensorthings.sensing.rest.update.ObservedPropertiesUpdate;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;

public class ObservedPropertiesAccessImpl extends AbstractAccess
        implements ObservedPropertiesAccess, ObservedPropertiesUpdate {

    @Override
    public ObservedProperty getObservedProperty(String id) {
        IDtoMemoryCache<?> wCache = getCache(ExpandedObservedProperty.class);
        if (wCache != null && wCache.getDto(id) != null) {
            ExpandedObservedProperty op = (ExpandedObservedProperty) getCache(ExpandedObservedProperty.class)
                    .getDto(id);
            return new ObservedProperty(DtoMapper.getLink(uriInfo, DtoMapper.VERSION, "/ObservedProperties", id),
                    op.id(), op.name(), op.description(), op.definition(), op.properties(), null);
        } else {
            ProviderSnapshot provider = validateAndGetProvider(extractFirstIdSegment(id));
            return DtoMapper.toObservedProperty(getSession(), application, getMapper(), uriInfo, getExpansions(),
                    parseFilter(OBSERVED_PROPERTIES), UtilIds.getDatastreamService(provider));

        }
    }

    @Override
    public ResultList<Datastream> getObservedPropertyDatastreams(String id) {
        return new ResultList<>(null, null, List.of(getObservedPropertyDatastream(id, id)));
    }

    @Override
    public Datastream getObservedPropertyDatastream(String id, String id2) {
        String idDatastream = extractFirstIdSegment(id);
        ProviderSnapshot datastremProvider = validateAndGetProvider(idDatastream);

        ServiceSnapshot service = UtilIds.getDatastreamService(datastremProvider);
        if (service == null) {
            throw new NotFoundException();
        }
        return DtoMapper.toDatastream(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(DATASTREAMS), service);
    }

    @PaginationLimit(500)
    @Override
    public ResultList<Observation> getObservedPropertyDatastreamObservations(String id, String id2) {
        if (!id.equals(id2)) {
            throw new NotFoundException();
        }
        String providerId = extractFirstIdSegment(id2);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        return RootResourceAccessImpl.getObservationList(getSession(), application, getMapper(), uriInfo,
                getExpansions(), UtilIds.getDatastreamService(provider), parseFilter(OBSERVATIONS), 0);
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
        String idDatastream = extractFirstIdSegment(id);
        if (!idDatastream.equals(id2)) {
            throw new NotFoundException();
        }
        ProviderSnapshot datastremProvider = validateAndGetProvider(idDatastream);

        return DtoMapper.toSensor(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(SENSORS), UtilIds.getDatastreamService(datastremProvider));
    }

    @Override
    public Thing getObservedPropertyDatastreamThing(String id, String id2) {
        String idDatastream = extractFirstIdSegment(id);
        if (!idDatastream.equals(id2)) {
            throw new NotFoundException();
        }
        ProviderSnapshot datastremProvider = validateAndGetProvider(idDatastream);
        ServiceSnapshot service = UtilIds.getDatastreamService(datastremProvider);
        String thingId = UtilIds.getResourceField(service, "thingId", String.class);
        ProviderSnapshot thingProvider = validateAndGetProvider(thingId);

        return DtoMapper.toThing(getSession(), application, getMapper(), uriInfo, getExpansions(), parseFilter(THINGS),
                UtilIds.getThingService(thingProvider));
    }

    @Override
    public Response updateObservedProperties(String id, ExpandedObservedProperty observedProperty) {
        getExtraDelegate().update(getSession(), getMapper(), uriInfo, requestContext.getMethod(), id, observedProperty);

        return Response.noContent().build();
    }

    @Override
    public Response patchObservedProperties(String id, ExpandedObservedProperty observedProperty) {
        return updateObservedProperties(id, observedProperty);
    }

}
