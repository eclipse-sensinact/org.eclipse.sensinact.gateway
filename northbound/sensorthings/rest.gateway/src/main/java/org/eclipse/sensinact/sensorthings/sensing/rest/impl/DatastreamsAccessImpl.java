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

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.RefId;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.DatastreamsAccess;
import org.eclipse.sensinact.sensorthings.sensing.rest.annotation.PaginationLimit;
import org.eclipse.sensinact.sensorthings.sensing.rest.create.DatastreamsCreate;
import org.eclipse.sensinact.sensorthings.sensing.rest.delete.DatastreamsDelete;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensinact.DatastreamsDelegateSensinact;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensorthings.DatastreamsDelegateSensorthings;
import org.eclipse.sensinact.sensorthings.sensing.rest.update.DatastreamsUpdate;
import jakarta.ws.rs.core.Response;

public class DatastreamsAccessImpl extends AbstractAccess
        implements DatastreamsDelete, DatastreamsAccess, DatastreamsCreate, DatastreamsUpdate {
    private DatastreamsDelegateSensinact sensinactHandler;
    private DatastreamsDelegateSensorthings sensorthigHandler;

    public DatastreamsDelegateSensinact getSensinactHandler() {
        if (sensinactHandler == null)
            sensinactHandler = new DatastreamsDelegateSensinact(uriInfo, providers, application, requestContext);
        return sensinactHandler;

    }

    public DatastreamsDelegateSensorthings getSensorthingsHandler() {
        if (sensorthigHandler == null)
            sensorthigHandler = new DatastreamsDelegateSensorthings(uriInfo, providers, application, requestContext);
        return sensorthigHandler;

    }

    @Override
    public Datastream getDatastream(String id) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getDatastream(id);
        } else {
            return getSensorthingsHandler().getDatastream(id);

        }
    }

    @PaginationLimit(500)
    @Override
    public ResultList<Observation> getDatastreamObservations(String id) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getDatastreamObservations(id);
        } else {
            return getSensorthingsHandler().getDatastreamObservations(id);

        }
    }

    @Override
    public Observation getDatastreamObservation(String id, String id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getDatastreamObservation(id, id2);
        } else {
            return getSensorthingsHandler().getDatastreamObservation(id, id2);

        }
    }

    @Override
    public Datastream getDatastreamObservationDatastream(String id, String id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getDatastreamObservationDatastream(id, id2);
        } else {
            return getSensorthingsHandler().getDatastreamObservationDatastream(id, id2);

        }
    }

    @Override
    public FeatureOfInterest getDatastreamObservationFeatureOfInterest(String id, String id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getDatastreamObservationFeatureOfInterest(id, id2);
        } else {
            return getSensorthingsHandler().getDatastreamObservationFeatureOfInterest(id, id2);

        }
    }

    @Override
    public ObservedProperty getDatastreamObservedProperty(String id) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getDatastreamObservedProperty(id);
        } else {
            return getSensorthingsHandler().getDatastreamObservedProperty(id);

        }
    }

    @Override
    public ResultList<Datastream> getDatastreamObservedPropertyDatastreams(String id) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getDatastreamObservedPropertyDatastreams(id);
        } else {
            return getSensorthingsHandler().getDatastreamObservedPropertyDatastreams(id);

        }
    }

    @Override
    public Sensor getDatastreamSensor(String id) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getDatastreamSensor(id);
        } else {
            return getSensorthingsHandler().getDatastreamSensor(id);

        }
    }

    @Override
    public ResultList<Datastream> getDatastreamSensorDatastreams(String id) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getDatastreamSensorDatastreams(id);
        } else {
            return getSensorthingsHandler().getDatastreamSensorDatastreams(id);

        }
    }

    @Override
    public Thing getDatastreamThing(String id) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getDatastreamThing(id);
        } else {
            return getSensorthingsHandler().getDatastreamThing(id);

        }

    }

    @Override
    public ResultList<Datastream> getDatastreamThingDatastreams(String id) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getDatastreamThingDatastreams(id);
        } else {
            return getSensorthingsHandler().getDatastreamThingDatastreams(id);

        }
    }

    @Override
    public ResultList<HistoricalLocation> getDatastreamThingHistoricalLocations(String id) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getDatastreamThingHistoricalLocations(id);
        } else {
            return getSensorthingsHandler().getDatastreamThingHistoricalLocations(id);

        }
    }

    @Override
    public ResultList<Location> getDatastreamThingLocations(String id) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getDatastreamThingLocations(id);
        } else {
            return getSensorthingsHandler().getDatastreamThingLocations(id);

        }
    }

    private boolean isSensorthingModel(ProviderSnapshot provider) {
        return DtoMapperSimple.isSensorthingModel(provider);
    }

    @Override
    public Response createDatastreamsObservation(String id, ExpandedObservation observation) {
        return getSensorthingsHandler().createDatastreamsObservation(id, observation);

    }

    @Override
    public Response createObservationRef(String id, RefId observation) {
        return getSensorthingsHandler().createObservationRef(id, observation);

    }

    @Override
    public Response updateDatastreams(String id, ExpandedDataStream dataStream) {
        return getSensorthingsHandler().updateDatastreams(id, dataStream);

    }

    @Override
    public Response updateDatastreamsObservation(String id, String id2, Observation observation) {
        return getSensorthingsHandler().updateDatastreamsObservation(id, id2, observation);

    }

    @Override
    public Response updateDatastreamThingRef(String id, RefId thing) {
        return getSensorthingsHandler().updateDatastreamThingRef(id, thing);

    }

    @Override
    public Response updateDatastreamSensorRef(String id, RefId sensor) {
        return getSensorthingsHandler().updateDatastreamSensorRef(id, sensor);

    }

    @Override
    public Response updateDatastreamObservedPropertyRef(String id, RefId observedProperty) {
        return getSensorthingsHandler().updateDatastreamObservedPropertyRef(id, observedProperty);

    }

    @Override
    public Response patchDatastreams(String id, ExpandedDataStream dataStream) {
        return getSensorthingsHandler().patchDatastreams(id, dataStream);

    }

    @Override
    public Response patchDatastreamsObservation(String id, String id2, Observation observation) {
        return getSensorthingsHandler().patchDatastreamsObservation(id, id2, observation);

    }

    @Override
    public Response deleteDatastream(String id) {
        return getSensorthingsHandler().deleteDatastream(id);

    }

    @Override
    public Response deleteDatastreamSensorRef(String id) {
        return getSensorthingsHandler().deleteDatastreamSensorRef(id);

    }

    @Override
    public Response deleteDatastreamObservedPropertyRef(String id) {
        return getSensorthingsHandler().deleteDatastreamObservedPropertyRef(id);

    }

    @Override
    public Response deleteDatastreamObservationsRef(String id) {
        return getSensorthingsHandler().deleteDatastreamObservationsRef(id);

    }
}
