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

import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.DtoMapperGet.extractFirstIdSegment;

import java.util.List;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.SensorsAccess;
import org.eclipse.sensinact.sensorthings.sensing.rest.annotation.PaginationLimit;
import org.eclipse.sensinact.sensorthings.sensing.rest.update.SensorsUpdate;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;

public class SensorsAccessImpl extends AbstractAccess implements SensorsAccess, SensorsUpdate {

    @Override
    public Sensor getSensor(String id) {
        return DtoMapperGet.toSensor(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(EFilterContext.SENSORS), validateAndGetResourceSnapshot(id));
    }

    @Override
    public ResultList<Datastream> getSensorDatastreams(String id) {
        ResultList<Datastream> list = new ResultList<>(null, null, List.of(getSensorDatastream(id, id)));
        return list;
    }

    @Override
    public Datastream getSensorDatastream(String id, String id2) {
        if (!id.equals(id2)) {
            throw new NotFoundException();
        }

        return DtoMapperGet.toDatastream(getSession(), application, getMapper(), uriInfo, getExpansions(),
                validateAndGetResourceSnapshot(id), parseFilter(EFilterContext.DATASTREAMS));
    }

    @PaginationLimit(500)
    @Override
    public ResultList<Observation> getSensorDatastreamObservations(String id, String id2) {
        if (!id.equals(id2)) {
            throw new NotFoundException();
        }
        return RootResourceAccessImpl.getObservationList(getSession(), application, getMapper(), uriInfo,
                getExpansions(), validateAndGetResourceSnapshot(id), parseFilter(EFilterContext.OBSERVATIONS), 0);
    }

    @Override
    public ObservedProperty getSensorDatastreamObservedProperty(String id, String id2) {
        if (!id.equals(id2)) {
            throw new NotFoundException();
        }
        ResourceSnapshot resource = validateAndGetResourceSnapshot(id);

        ObservedProperty o = DtoMapperGet.toObservedProperty(getSession(), application, getMapper(), uriInfo,
                getExpansions(), parseFilter(EFilterContext.OBSERVED_PROPERTIES), resource);

        if (!id.equals(o.id())) {
            throw new NotFoundException();
        }

        return o;
    }

    @Override
    public Sensor getSensorDatastreamSensor(String id, String id2) {
        if (!id.equals(id2)) {
            throw new NotFoundException();
        }
        return getSensor(id);
    }

    @Override
    public Thing getSensorDatastreamThing(String id, String id2) {
        String provider = extractFirstIdSegment(id);
        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);

        Thing t = DtoMapperGet.toThing(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(EFilterContext.THINGS), providerSnapshot);
        if (!provider.equals(t.id())) {
            throw new NotFoundException();
        }
        return t;
    }

    @Override
    public Response updateSensor(String id, Sensor sensor) {
        // TODO Auto-generated method stub
        return null;
    }

}
