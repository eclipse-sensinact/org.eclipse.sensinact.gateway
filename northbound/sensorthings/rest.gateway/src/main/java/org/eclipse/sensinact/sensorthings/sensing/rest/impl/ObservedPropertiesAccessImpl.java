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
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.rest.ODataId;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.ObservedPropertiesAccess;
import org.eclipse.sensinact.sensorthings.sensing.rest.delete.ObservedPropertiesDelete;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensinact.ObservedPropertiesDelegateSensinact;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensorthings.ObservedPropertiesDelegateSensorthings;
import org.eclipse.sensinact.sensorthings.sensing.rest.update.ObservedPropertiesUpdate;

public class ObservedPropertiesAccessImpl extends AbstractAccess
        implements ObservedPropertiesDelete, ObservedPropertiesAccess, ObservedPropertiesUpdate {
    private ObservedPropertiesDelegateSensinact sensinactHandler;
    private ObservedPropertiesDelegateSensorthings sensorthigHandler;

    public ObservedPropertiesDelegateSensinact getSensinactHandler() {
        if (sensinactHandler == null)
            sensinactHandler = new ObservedPropertiesDelegateSensinact(uriInfo, providers, application, requestContext);
        return sensinactHandler;

    }

    public ObservedPropertiesDelegateSensorthings getSensorthingsHandler() {
        if (sensorthigHandler == null)
            sensorthigHandler = new ObservedPropertiesDelegateSensorthings(uriInfo, providers, application,
                    requestContext);
        return sensorthigHandler;

    }

    @Override
    public Response updateObservedProperties(ODataId id, ObservedProperty observedProperty) {

        return getSensorthingsHandler().updateObservedProperties(id.value(), observedProperty);

    }

    @Override
    public Response patchObservedProperties(ODataId id, ObservedProperty observedProperty) {

        return getSensorthingsHandler().patchObservedProperties(id.value(), observedProperty);

    }

    @Override
    public ObservedProperty getObservedProperty(ODataId id) {
        if (getCacheObservedProperty().getDto(id.value()) != null) {
            return getCacheObservedProperty().getDto(id.value());
        } else {
            String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
            ProviderSnapshot provider = validateAndGetProvider(providerId);
            if (!isSensorthingModel(provider)) {
                return getSensinactHandler().getObservedProperty(id.value());
            } else {
                return getSensorthingsHandler().getObservedProperty(id.value());

            }
        }
    }

    @Override
    public ResultList<Datastream> getObservedPropertyDatastreams(ODataId id) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getObservedPropertyDatastreams(id.value());
        } else {
            return getSensorthingsHandler().getObservedPropertyDatastreams(id.value());

        }
    }

    @Override
    public Datastream getObservedPropertyDatastream(ODataId id, ODataId id2) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getObservedPropertyDatastream(id.value(), id2.value());
        } else {
            return getSensorthingsHandler().getObservedPropertyDatastream(id.value(), id2.value());

        }
    }

    @Override
    public ResultList<Observation> getObservedPropertyDatastreamObservations(ODataId id, ODataId id2) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getObservedPropertyDatastreamObservations(id.value(), id2.value());
        } else {
            return getSensorthingsHandler().getObservedPropertyDatastreamObservations(id.value(), id2.value());

        }
    }

    @Override
    public ObservedProperty getObservedPropertyDatastreamObservedProperty(ODataId id, ODataId id2) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getObservedPropertyDatastreamObservedProperty(id.value(), id2.value());
        } else {
            return getSensorthingsHandler().getObservedPropertyDatastreamObservedProperty(id.value(), id2.value());

        }
    }

    @Override
    public Sensor getObservedPropertyDatastreamSensor(ODataId id, ODataId id2) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getObservedPropertyDatastreamSensor(id.value(), id2.value());
        } else {
            return getSensorthingsHandler().getObservedPropertyDatastreamSensor(id.value(), id2.value());

        }
    }

    @Override
    public Thing getObservedPropertyDatastreamThing(ODataId id, ODataId id2) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getObservedPropertyDatastreamThing(id.value(), id2.value());
        } else {
            return getSensorthingsHandler().getObservedPropertyDatastreamThing(id.value(), id2.value());

        }
    }

    @Override
    public ResultList<Datastream> getObservedPropertyDatastreamThingDatastreams(ODataId id, ODataId id2) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getObservedPropertyDatastreamThingDatastreams(id.value(), id2.value());
        } else {
            return getSensorthingsHandler().getObservedPropertyDatastreamThingDatastreams(id.value(), id2.value());

        }
    }

    private boolean isSensorthingModel(ProviderSnapshot provider) {
        return DtoMapperSimple.isSensorthingModel(provider);
    }

    @Override
    public Response deleteObservedProperty(ODataId id) {

        return getSensorthingsHandler().deleteObservedProperty(id.value());

    }

    @Override
    public ResultList<HistoricalLocation> getObservedPropertyDatastreamThingHistoricalLocations(ODataId id,
            ODataId id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getObservedPropertyDatastreamThingHistoricalLocations(id.value(), id2.value());
        } else {
            return getSensorthingsHandler().getObservedPropertyDatastreamThingHistoricalLocations(id.value(),
                    id2.value());

        }
    }

    @Override
    public ResultList<Location> getObservedPropertyDatastreamThingLocations(ODataId id, ODataId id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getObservedPropertyDatastreamThingLocations(id.value(), id2.value());
        } else {
            return getSensorthingsHandler().getObservedPropertyDatastreamThingLocations(id.value(), id2.value());

        }
    }

    @Override
    public Observation getObservedPropertyDatastreamObservation(ODataId id, ODataId id2, ODataId id3) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getObservedPropertyDatastreamObservation(id.value(), id2.value(), id3.value());
        } else {
            return getSensorthingsHandler().getObservedPropertyDatastreamObservation(id.value(), id2.value(),
                    id3.value());

        }
    }

    @Override
    public FeatureOfInterest getObservedPropertyDatastreamObservationFeatureOfInterest(ODataId id, ODataId id2,
            ODataId id3) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getObservedPropertyDatastreamObservationFeatureOfInterest(id.value(),
                    id2.value(), id3.value());
        } else {
            return getSensorthingsHandler().getObservedPropertyDatastreamObservationFeatureOfInterest(id.value(),
                    id2.value(), id3.value());

        }
    }

    @Override
    public ResultList<Observation> getObservedPropertyDatastreamObservationFeatureOfInterestObservations(ODataId id,
            ODataId id2, ODataId id3) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getObservedPropertyDatastreamObservationFeatureOfInterestObservations(
                    id.value(), id2.value(), id3.value());
        } else {
            return getSensorthingsHandler().getObservedPropertyDatastreamObservationFeatureOfInterestObservations(
                    id.value(), id2.value(), id3.value());

        }
    }

}
