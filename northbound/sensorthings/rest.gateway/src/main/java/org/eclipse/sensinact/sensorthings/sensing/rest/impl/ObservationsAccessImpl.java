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
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.RefId;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
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
    public Response updateObservationDatastreamRef(String id, RefId datastream) {
        return getSensorthingsHandler().updateObservationDatastreamRef(id, datastream);

    }

    @Override
    public Response updateObservationFeatureOfInterestRef(String id, RefId foi) {
        return getSensorthingsHandler().updateObservationFeatureOfInterestRef(id, foi);
    }

    @Override
    public Observation getObservation(String id) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
       if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getObservation(id);
        } else {
            return getSensorthingsHandler().getObservation(id);

        }
    }

    @Override
    public Datastream getObservationDatastream(String id) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
       if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getObservationDatastream(id);
        } else {
            return getSensorthingsHandler().getObservationDatastream(id);

        }
    }

    @Override
    public ResultList<Observation> getObservationDatastreamObservations(String id) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
       if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getObservationDatastreamObservations(id);
        } else {
            return getSensorthingsHandler().getObservationDatastreamObservations(id);

        }
    }

    @Override
    public ObservedProperty getObservationDatastreamObservedProperty(String id) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
       if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getObservationDatastreamObservedProperty(id);
        } else {
            return getSensorthingsHandler().getObservationDatastreamObservedProperty(id);

        }
    }

    @Override
    public Sensor getObservationDatastreamSensor(String id) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
       if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getObservationDatastreamSensor(id);
        } else {
            return getSensorthingsHandler().getObservationDatastreamSensor(id);

        }
    }

    @Override
    public Thing getObservationDatastreamThing(String id) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
       if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getObservationDatastreamThing(id);
        } else {
            return getSensorthingsHandler().getObservationDatastreamThing(id);

        }
    }

    @Override
    public FeatureOfInterest getObservationFeatureOfInterest(String id) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
       if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getObservationFeatureOfInterest(id);
        } else {
            return getSensorthingsHandler().getObservationFeatureOfInterest(id);

        }
    }

    @Override
    public ResultList<Observation> getObservationFeatureOfInterestObservations(String id) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
       if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getObservationFeatureOfInterestObservations(id);
        } else {
            return getSensorthingsHandler().getObservationFeatureOfInterestObservations(id);

        }
    }

    private boolean isSensorthingModel(ProviderSnapshot provider) {
        return DtoMapperSimple.isSensorthingModel(provider);
    }

    @Override
    public Response deleteObservation(String id) {
        return getSensorthingsHandler().deleteObservation(id);

    }

    @Override
    public Response deleteObservationFeatureOfInterest(String id) {
        return getSensorthingsHandler().deleteObservationFeatureOfInterest(id);

    }

}
