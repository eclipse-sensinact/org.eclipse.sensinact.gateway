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
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.RefId;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.rest.ODataId;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.ObservationsAccess;
import org.eclipse.sensinact.sensorthings.sensing.rest.delete.ObservationsDelete;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensinact.ObservationsDelegateSensinact;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensorthings.ObservationsDelegateSensorthings;
import org.eclipse.sensinact.sensorthings.sensing.rest.update.ObservationsUpdate;

public class ObservationsAccessImpl extends AbstractAccess
        implements ObservationsDelete, ObservationsAccess, ObservationsUpdate {
    private ObservationsDelegateSensinact sensinactHandler;
    private ObservationsDelegateSensorthings sensorthigHandler;

    public ObservationsDelegateSensinact getSensinactHandler() {
        if (sensinactHandler == null)
            sensinactHandler = new ObservationsDelegateSensinact(uriInfo, providers, application, requestContext);
        return sensinactHandler;

    }

    public ObservationsDelegateSensorthings getSensorthingsHandler() {
        if (sensorthigHandler == null)
            sensorthigHandler = new ObservationsDelegateSensorthings(uriInfo, providers, application, requestContext);
        return sensorthigHandler;

    }

    @Override
    public Response updateObservationDatastreamRef(ODataId id, RefId datastream) {

        return getSensorthingsHandler().updateObservationDatastreamRef(id.value(), datastream);

    }

    @Override
    public Response updateObservationFeatureOfInterestRef(ODataId id, RefId foi) {

        return getSensorthingsHandler().updateObservationFeatureOfInterestRef(id.value(), foi);
    }

    @Override
    public Observation getObservation(ODataId id) {
        Observation obs = null;

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            obs = getSensinactHandler().getObservation(id.value());
        } else {
            obs = getSensorthingsHandler().getObservation(id.value());
        }
        return obs;

    }

    @Override
    public Datastream getObservationDatastream(ODataId id) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getObservationDatastream(id.value());
        } else {
            return getSensorthingsHandler().getObservationDatastream(id.value());

        }
    }

    @Override
    public ResultList<Observation> getObservationDatastreamObservations(ODataId id) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getObservationDatastreamObservations(id.value());
        } else {
            return getSensorthingsHandler().getObservationDatastreamObservations(id.value());

        }
    }

    @Override
    public ObservedProperty getObservationDatastreamObservedProperty(ODataId id) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getObservationDatastreamObservedProperty(id.value());
        } else {
            return getSensorthingsHandler().getObservationDatastreamObservedProperty(id.value());

        }
    }

    @Override
    public Sensor getObservationDatastreamSensor(ODataId id) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getObservationDatastreamSensor(id.value());
        } else {
            return getSensorthingsHandler().getObservationDatastreamSensor(id.value());

        }
    }

    @Override
    public Thing getObservationDatastreamThing(ODataId id) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getObservationDatastreamThing(id.value());
        } else {
            return getSensorthingsHandler().getObservationDatastreamThing(id.value());

        }
    }

    @Override
    public ResultList<Datastream> getObservationDatastreamThingDataastreams(ODataId id) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getObservationDatastreamThingDataastreams(id.value());
        } else {
            return getSensorthingsHandler().getObservationDatastreamThingDataastreams(id.value());

        }
    }

    @Override
    public FeatureOfInterest getObservationFeatureOfInterest(ODataId id) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getObservationFeatureOfInterest(id.value());
        } else {
            return getSensorthingsHandler().getObservationFeatureOfInterest(id.value());

        }
    }

    @Override
    public ResultList<Observation> getObservationFeatureOfInterestObservations(ODataId id) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getObservationFeatureOfInterestObservations(id.value());
        } else {
            return getSensorthingsHandler().getObservationFeatureOfInterestObservations(id.value());

        }
    }

    private boolean isSensorthingModel(ProviderSnapshot provider) {
        return DtoMapperSimple.isSensorthingModel(provider);
    }

    @Override
    public Response deleteObservation(ODataId id) {
        return getSensorthingsHandler().deleteObservation(id.value());

    }

    @Override
    public Response deleteObservationFeatureOfInterest(ODataId id) {

        return getSensorthingsHandler().deleteObservationFeatureOfInterest(id.value());

    }

    @Override
    public ResultList<HistoricalLocation> getObservationDatastreamThingHistoricalLocations(ODataId id) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getObservationDatastreamThingHistoricalLocations(id.value());
        } else {
            return getSensorthingsHandler().getObservationDatastreamThingHistoricalLocations(id.value());

        }
    }

    @Override
    public ResultList<Location> getObservationDatastreamThingLocations(ODataId id) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getObservationDatastreamThingLocations(id.value());
        } else {
            return getSensorthingsHandler().getObservationDatastreamThingLocations(id.value());

        }
    }

    @Override
    public Response updateObservation(ODataId id, ExpandedObservation obs) {
        return getSensorthingsHandler().updateObservation(id.value(), obs);

    }

    @Override
    public Response patchObservation(ODataId id, ExpandedObservation obs) {
        return updateObservation(id, obs);

    }

}
