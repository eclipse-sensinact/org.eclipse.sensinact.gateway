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

import java.util.List;

import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.rest.ObservedPropertiesAccess;
import org.eclipse.sensinact.sensorthings.sensing.rest.annotation.PaginationLimit;

import jakarta.ws.rs.NotFoundException;

public class ObservedPropertiesAccessImpl extends AbstractAccess implements ObservedPropertiesAccess {

    @Override
    public ObservedProperty getObservedProperty(String id) {
        ObservedProperty o = DtoMapper.toObservedProperty(getSession(), application, getMapper(),
                uriInfo, getExpansions(), validateAndGetResourceSnapshot(id));

        if (!id.equals(o.id)) {
            throw new NotFoundException();
        }

        return o;
    }

    @Override
    public ResultList<Datastream> getObservedPropertyDatastreams(String id) {
        ResultList<Datastream> list = new ResultList<>();
        list.value = List.of(getObservedPropertyDatastream(id, id));
        return list;
    }

    @Override
    public Datastream getObservedPropertyDatastream(String id, String id2) {
        if (!id.equals(id2)) {
            throw new NotFoundException();
        }

        return DtoMapper.toDatastream(getSession(), application, getMapper(), uriInfo,
                getExpansions(), validateAndGetResourceSnapshot(id2));
    }

    @PaginationLimit(500)
    @Override
    public ResultList<Observation> getObservedPropertyDatastreamObservations(String id, String id2) {
        if (!id.equals(id2)) {
            throw new NotFoundException();
        }
        return RootResourceAccessImpl.getObservationList(getSession(), application, getMapper(), uriInfo,
                requestContext, validateAndGetResourceSnapshot(id));
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

        return DtoMapper.toSensor(getSession(), application, getMapper(), uriInfo,
                getExpansions(), validateAndGetResourceSnapshot(id2));
    }

    @Override
    public Thing getObservedPropertyDatastreamThing(String id, String id2) {
        String provider = extractFirstIdSegment(id);
        String provider2 = extractFirstIdSegment(id2);
        if (!provider.equals(provider2)) {
            throw new NotFoundException();
        }
        return DtoMapper.toThing(getSession(), application, getMapper(), uriInfo,
                getExpansions(), validateAndGetProvider(provider));
    }
}
