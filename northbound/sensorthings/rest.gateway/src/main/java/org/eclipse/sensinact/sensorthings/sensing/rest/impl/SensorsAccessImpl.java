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
import org.eclipse.sensinact.sensorthings.sensing.rest.ODataId;
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
    public Response updateSensor(ODataId id, Sensor sensor) {
        
        return getSensorthingsHandler().updateSensor(id.value(), sensor);

    }

    @Override
    public Response patchSensor(ODataId id, Sensor sensor) {
        

        return getSensorthingsHandler().patchSensor(id.value(), sensor);

    }

    @Override
    public Sensor getSensor(ODataId id) {


        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getSensor(id.value());
        } else {
            return getSensorthingsHandler().getSensor(id.value());

        }
    }

    @Override
    public ResultList<Datastream> getSensorDatastreams(ODataId id) {


        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getSensorDatastreams(id.value());
        } else {
            return getSensorthingsHandler().getSensorDatastreams(id.value());

        }
    }

    @Override
    public Datastream getSensorDatastream(ODataId id, ODataId id2) {


        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getSensorDatastream(id.value(), id2.value());
        } else {
            return getSensorthingsHandler().getSensorDatastream(id.value(), id2.value());

        }
    }

    @Override
    public ResultList<Observation> getSensorDatastreamObservations(ODataId id, ODataId id2) {


        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getSensorDatastreamObservations(id.value(), id2.value());
        } else {
            return getSensorthingsHandler().getSensorDatastreamObservations(id.value(), id2.value());

        }
    }

    @Override
    public ObservedProperty getSensorDatastreamObservedProperty(ODataId id, ODataId id2) {


        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getSensorDatastreamObservedProperty(id.value(), id2.value());
        } else {
            return getSensorthingsHandler().getSensorDatastreamObservedProperty(id.value(), id2.value());

        }
    }

    @Override
    public Sensor getSensorDatastreamSensor(ODataId id, ODataId id2) {


        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getSensorDatastreamSensor(id.value(), id2.value());
        } else {
            return getSensorthingsHandler().getSensorDatastreamSensor(id.value(), id2.value());

        }
    }

    @Override
    public Thing getSensorDatastreamThing(ODataId id, ODataId id2) {


        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getSensorDatastreamThing(id.value(), id2.value());
        } else {
            return getSensorthingsHandler().getSensorDatastreamThing(id.value(), id2.value());

        }
    }

    private boolean isSensorthingModel(ProviderSnapshot provider) {
        return DtoMapperSimple.isSensorthingModel(provider);
    }

    @Override
    public Response deleteSensor(ODataId id) {
        

        validateAndGetProvider(DtoMapperSimple.extractFirstIdSegment(id.value()));
        return getSensorthingsHandler().deleteSensor(id.value());

    }

}
