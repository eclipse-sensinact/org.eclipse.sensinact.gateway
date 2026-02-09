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
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.rest.ODataId;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.FeaturesOfInterestAccess;
import org.eclipse.sensinact.sensorthings.sensing.rest.delete.FeaturesOfInterestDelete;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensinact.FeaturesOfInterestDeletegateSensinact;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensorthings.FeaturesOfInterestDelegateSensorthings;
import org.eclipse.sensinact.sensorthings.sensing.rest.update.FeaturesOfInterestUpdate;
import jakarta.ws.rs.core.Response;

public class FeaturesOfInterestAccessImpl extends AbstractAccess
        implements FeaturesOfInterestDelete, FeaturesOfInterestAccess, FeaturesOfInterestUpdate {
    private FeaturesOfInterestDeletegateSensinact sensinact;
    private FeaturesOfInterestDelegateSensorthings sensorthings;

    public FeaturesOfInterestDeletegateSensinact getSensinactHandler() {
        if (sensinact == null)
            sensinact = new FeaturesOfInterestDeletegateSensinact(uriInfo, providers, application, requestContext);
        return sensinact;

    }

    public FeaturesOfInterestDelegateSensorthings getSensorthingsHandler() {
        if (sensorthings == null)
            sensorthings = new FeaturesOfInterestDelegateSensorthings(uriInfo, providers, application, requestContext);
        return sensorthings;

    }

    @Override
    public Response updateFeaturesOfInterest(ODataId id, FeatureOfInterest foi) {

        return getSensorthingsHandler().updateFeaturesOfInterest(id.value(), foi);

    }

    @Override
    public Response patchFeaturesOfInterest(ODataId id, FeatureOfInterest foi) {

        return getSensorthingsHandler().patchFeaturesOfInterest(id.value(), foi);

    }

    @Override
    public FeatureOfInterest getFeatureOfInterest(ODataId id) {
        if (getCacheFeatureOfInterest().getDto(id.value()) != null) {
            return getCacheFeatureOfInterest().getDto(id.value());
        } else {
            String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
            ProviderSnapshot provider = validateAndGetProvider(providerId);
            if (!isSensorthingModel(provider)) {
                return getSensinactHandler().getFeatureOfInterest(id.value());
            } else {
                return getSensorthingsHandler().getFeatureOfInterest(id.value());

            }
        }
    }

    @Override
    public ResultList<Observation> getFeatureOfInterestObservations(ODataId id) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getFeatureOfInterestObservations(id.value());
        } else {
            return getSensorthingsHandler().getFeatureOfInterestObservations(id.value());

        }
    }

    @Override
    public Observation getFeatureOfInterestObservation(ODataId id, ODataId id2) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getFeatureOfInterestObservation(id.value(), id2.value());
        } else {
            return getSensorthingsHandler().getFeatureOfInterestObservation(id.value(), id2.value());

        }
    }

    @Override
    public Datastream getFeatureOfInterestObservationDatastream(ODataId id, ODataId id2) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getFeatureOfInterestObservationDatastream(id.value(), id2.value());
        } else {
            return getSensorthingsHandler().getFeatureOfInterestObservationDatastream(id.value(), id2.value());

        }
    }

    private boolean isSensorthingModel(ProviderSnapshot provider) {
        return DtoMapperSimple.isSensorthingModel(provider);
    }

    @Override
    public Response deleteFeatureOfInterest(ODataId id) {

        return getSensorthingsHandler().deleteFeatureOfInterest(id.value());

    }

    @Override
    public Thing getFeatureOfInterestObservationDatastreamThing(ODataId id, ODataId id2) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getFeatureOfInterestObservationDatastreamThing(id.value(), id2.value());
        } else {
            return getSensorthingsHandler().getFeatureOfInterestObservationDatastreamThing(id.value(), id2.value());

        }
    }

    @Override
    public Sensor getFeatureOfInterestObservationDatastreamSensor(ODataId id, ODataId id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getFeatureOfInterestObservationDatastreamSensor(id.value(), id2.value());
        } else {
            return getSensorthingsHandler().getFeatureOfInterestObservationDatastreamSensor(id.value(), id2.value());

        }
    }

    @Override
    public ObservedProperty getFeatureOfInterestObservationDatastreamObservedProperty(ODataId id, ODataId id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getFeatureOfInterestObservationDatastreamObservedProperty(id.value(),
                    id2.value());
        } else {
            return getSensorthingsHandler().getFeatureOfInterestObservationDatastreamObservedProperty(id.value(),
                    id2.value());

        }
    }

    @Override
    public ResultList<Observation> getFeatureOfInterestObservationDatastreamObservations(ODataId id, ODataId id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getFeatureOfInterestObservationDatastreamObservations(id.value(), id2.value());
        } else {
            return getSensorthingsHandler().getFeatureOfInterestObservationDatastreamObservations(id.value(),
                    id2.value());

        }
    }

}
