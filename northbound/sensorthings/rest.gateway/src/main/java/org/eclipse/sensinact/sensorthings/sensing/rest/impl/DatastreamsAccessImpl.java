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
import org.eclipse.sensinact.sensorthings.sensing.rest.ODataId;
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
    public Datastream getDatastream(ODataId id) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getDatastream(id.value());
        } else {
            return getSensorthingsHandler().getDatastream(id.value());

        }
    }

    @PaginationLimit(500)
    @Override
    public ResultList<Observation> getDatastreamObservations(ODataId id) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getDatastreamObservations(id.value());
        } else {
            return getSensorthingsHandler().getDatastreamObservations(id.value());

        }
    }

    @Override
    public Observation getDatastreamObservation(ODataId id, ODataId id2) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getDatastreamObservation(id.value(), id2.value());
        } else {
            return getSensorthingsHandler().getDatastreamObservation(id.value(), id2.value());

        }
    }

    @Override
    public Datastream getDatastreamObservationDatastream(ODataId id, ODataId id2) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getDatastreamObservationDatastream(id.value(), id2.value());
        } else {
            return getSensorthingsHandler().getDatastreamObservationDatastream(id.value(), id2.value());

        }
    }

    @Override
    public FeatureOfInterest getDatastreamObservationFeatureOfInterest(ODataId id, ODataId id2) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getDatastreamObservationFeatureOfInterest(id.value(), id2.value());
        } else {
            return getSensorthingsHandler().getDatastreamObservationFeatureOfInterest(id.value(), id2.value());

        }
    }

    @Override
    public ObservedProperty getDatastreamObservedProperty(ODataId id) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getDatastreamObservedProperty(id.value());
        } else {
            return getSensorthingsHandler().getDatastreamObservedProperty(id.value());

        }
    }

    @Override
    public ResultList<Datastream> getDatastreamObservedPropertyDatastreams(ODataId id) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getDatastreamObservedPropertyDatastreams(id.value());
        } else {
            return getSensorthingsHandler().getDatastreamObservedPropertyDatastreams(id.value());

        }
    }

    @Override
    public Sensor getDatastreamSensor(ODataId id) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getDatastreamSensor(id.value());
        } else {
            return getSensorthingsHandler().getDatastreamSensor(id.value());

        }
    }

    @Override
    public ResultList<Datastream> getDatastreamSensorDatastreams(ODataId id) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getDatastreamSensorDatastreams(id.value());
        } else {
            return getSensorthingsHandler().getDatastreamSensorDatastreams(id.value());

        }
    }

    @Override
    public Thing getDatastreamThing(ODataId id) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getDatastreamThing(id.value());
        } else {
            return getSensorthingsHandler().getDatastreamThing(id.value());

        }

    }

    @Override
    public ResultList<Datastream> getDatastreamThingDatastreams(ODataId id) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getDatastreamThingDatastreams(id.value());
        } else {
            return getSensorthingsHandler().getDatastreamThingDatastreams(id.value());

        }
    }

    @Override
    public ResultList<HistoricalLocation> getDatastreamThingHistoricalLocations(ODataId id) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getDatastreamThingHistoricalLocations(id.value());
        } else {
            return getSensorthingsHandler().getDatastreamThingHistoricalLocations(id.value());

        }
    }

    @Override
    public ResultList<Location> getDatastreamThingLocations(ODataId id) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getDatastreamThingLocations(id.value());
        } else {
            return getSensorthingsHandler().getDatastreamThingLocations(id.value());

        }
    }

    private boolean isSensorthingModel(ProviderSnapshot provider) {
        return DtoMapperSimple.isSensorthingModel(provider);
    }

    @Override
    public Response createDatastreamsObservation(ODataId id, ExpandedObservation observation) {

        return getSensorthingsHandler().createDatastreamsObservation(id.value(), observation);

    }

    @Override
    public Response createObservationRef(ODataId id, RefId observation) {

        return getSensorthingsHandler().createObservationRef(id.value(), observation);

    }

    @Override
    public Response updateDatastreams(ODataId id, ExpandedDataStream dataStream) {

        return getSensorthingsHandler().updateDatastreams(id.value(), dataStream);

    }

    @Override
    public Response updateDatastreamsObservation(ODataId id, ODataId id2, Observation observation) {

        return getSensorthingsHandler().updateDatastreamsObservation(id.value(), id2.value(), observation);

    }

    @Override
    public Response updateDatastreamThingRef(ODataId id, RefId thing) {

        return getSensorthingsHandler().updateDatastreamThingRef(id.value(), thing);

    }

    @Override
    public Response updateDatastreamSensorRef(ODataId id, RefId sensor) {

        return getSensorthingsHandler().updateDatastreamSensorRef(id.value(), sensor);

    }

    @Override
    public Response updateDatastreamObservedPropertyRef(ODataId id, RefId observedProperty) {

        return getSensorthingsHandler().updateDatastreamObservedPropertyRef(id.value(), observedProperty);

    }

    @Override
    public Response patchDatastreams(ODataId id, ExpandedDataStream dataStream) {

        return getSensorthingsHandler().patchDatastreams(id.value(), dataStream);

    }

    @Override
    public Response patchDatastreamsObservation(ODataId id, ODataId id2, Observation observation) {

        return getSensorthingsHandler().patchDatastreamsObservation(id.value(), id2.value(), observation);

    }

    @Override
    public Response deleteDatastream(ODataId id) {

        return getSensorthingsHandler().deleteDatastream(id.value());

    }

    @Override
    public Response deleteDatastreamSensorRef(ODataId id) {

        return getSensorthingsHandler().deleteDatastreamSensorRef(id.value());

    }

    @Override
    public Response deleteDatastreamObservedPropertyRef(ODataId id) {

        return getSensorthingsHandler().deleteDatastreamObservedPropertyRef(id.value());

    }

    @Override
    public Response deleteDatastreamObservationsRef(ODataId id) {

        return getSensorthingsHandler().deleteDatastreamObservationsRef(id.value());

    }

    @Override
    public ResultList<Observation> getDatastreamThingDatastreamObservations(ODataId id, ODataId id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        String providerId2 = DtoMapperSimple.extractFirstIdSegment(id2.value());

        ProviderSnapshot provider = validateAndGetProvider(providerId);

        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getDatastreamThingDatastreamObserations(providerId, providerId2);
        } else {
            return getSensorthingsHandler().getDatastreamThingDatastreamObserations(providerId, providerId2);

        }
    }

    @Override
    public ResultList<Thing> getDatastreamThingLocationThings(ODataId id, ODataId id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        String providerId2 = DtoMapperSimple.extractFirstIdSegment(id2.value());

        ProviderSnapshot provider = validateAndGetProvider(providerId);

        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getDatastreamThingLocationThings(providerId, providerId2);
        } else {
            return getSensorthingsHandler().getDatastreamThingLocationThings(providerId, providerId2);

        }
    }
}
