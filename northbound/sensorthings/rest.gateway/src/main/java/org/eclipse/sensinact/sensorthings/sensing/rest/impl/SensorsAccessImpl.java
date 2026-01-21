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

import jakarta.ws.rs.core.Response;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.SensorsAccess;
import org.eclipse.sensinact.sensorthings.sensing.rest.delete.SensorsDelete;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensinact.SensorsDelegateSensinact;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensorthings.SensorsDelegateSensorthings;
import org.eclipse.sensinact.sensorthings.sensing.rest.update.SensorsUpdate;

public class SensorsAccessImpl extends AbstractAccess implements SensorsDelete, SensorsAccess, SensorsUpdate {
    private SensorsDelegateSensinact sensinactHandler;
    private SensorsDelegateSensorthings sensorthigHandler;

    public SensorsDelegateSensinact getSensinactHandler() {
        if (sensinactHandler == null)
            sensinactHandler = new SensorsDelegateSensinact(uriInfo, providers, application, requestContext);
        return sensinactHandler;

    }

    public SensorsDelegateSensorthings getSensorthingsHandler() {
        if (sensorthigHandler == null)
            sensorthigHandler = new SensorsDelegateSensorthings(uriInfo, providers, application, requestContext);
        return sensorthigHandler;

    }

    @Override
    public Response updateSensor(String id, Sensor sensor) {
        return getSensorthingsHandler().updateSensor(id, sensor);

    }

    @Override
    public Response patchSensor(String id, Sensor sensor) {
        return getSensorthingsHandler().patchSensor(id, sensor);

    }

    @Override
    public Sensor getSensor(String id) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
       if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getSensor(id);
        } else {
            return getSensorthingsHandler().getSensor(id);

        }
    }

    @Override
    public ResultList<Datastream> getSensorDatastreams(String id) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
       if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getSensorDatastreams(id);
        } else {
            return getSensorthingsHandler().getSensorDatastreams(id);

        }
    }

    @Override
    public Datastream getSensorDatastream(String id, String id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
       if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getSensorDatastream(id, id2);
        } else {
            return getSensorthingsHandler().getSensorDatastream(id, id2);

        }
    }

    @Override
    public ResultList<Observation> getSensorDatastreamObservations(String id, String id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
       if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getSensorDatastreamObservations(id, id2);
        } else {
            return getSensorthingsHandler().getSensorDatastreamObservations(id, id2);

        }
    }

    @Override
    public ObservedProperty getSensorDatastreamObservedProperty(String id, String id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
       if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getSensorDatastreamObservedProperty(id, id2);
        } else {
            return getSensorthingsHandler().getSensorDatastreamObservedProperty(id, id2);

        }
    }

    @Override
    public Sensor getSensorDatastreamSensor(String id, String id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
       if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getSensorDatastreamSensor(id, id2);
        } else {
            return getSensorthingsHandler().getSensorDatastreamSensor(id, id2);

        }
    }

    @Override
    public Thing getSensorDatastreamThing(String id, String id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
       if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getSensorDatastreamThing(id, id2);
        } else {
            return getSensorthingsHandler().getSensorDatastreamThing(id, id2);

        }
    }

    private boolean isSensorthingModel(ProviderSnapshot provider) {
        return DtoMapperSimple.isSensorthingModel(provider);
    }

    @Override
    public Response deleteSensor(String id) {
        return getSensorthingsHandler().deleteSensor(id);

    }

}
