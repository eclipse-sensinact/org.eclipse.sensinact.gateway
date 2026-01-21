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
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
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
    public Response updateFeaturesOfInterest(String id, FeatureOfInterest foi) {

        return getSensorthingsHandler().updateFeaturesOfInterest(id, foi);

    }

    @Override
    public Response patchFeaturesOfInterest(String id, FeatureOfInterest foi) {
        return getSensorthingsHandler().patchFeaturesOfInterest(id, foi);

    }

    @Override
    public FeatureOfInterest getFeatureOfInterest(String id) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
       if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getFeatureOfInterest(id);
        } else {
            return getSensorthingsHandler().getFeatureOfInterest(id);

        }
    }

    @Override
    public ResultList<Observation> getFeatureOfInterestObservations(String id) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
       if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getFeatureOfInterestObservations(id);
        } else {
            return getSensorthingsHandler().getFeatureOfInterestObservations(id);

        }
    }

    @Override
    public Observation getFeatureOfInterestObservation(String id, String id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
       if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getFeatureOfInterestObservation(id, id2);
        } else {
            return getSensorthingsHandler().getFeatureOfInterestObservation(id, id2);

        }
    }

    @Override
    public Datastream getFeatureOfInterestObservationDatastream(String id, String id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
       if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getFeatureOfInterestObservationDatastream(id, id2);
        } else {
            return getSensorthingsHandler().getFeatureOfInterestObservationDatastream(id, id2);

        }
    }

    private boolean isSensorthingModel(ProviderSnapshot provider) {
        return DtoMapperSimple.isSensorthingModel(provider);
    }

    @Override
    public Response deleteFeatureOfInterest(String id) {
        return getSensorthingsHandler().deleteFeatureOfInterest(id);

    }

}
