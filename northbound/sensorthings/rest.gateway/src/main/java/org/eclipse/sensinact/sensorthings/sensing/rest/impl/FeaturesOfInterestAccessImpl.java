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

import static java.util.stream.Collectors.toList;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.FEATURES_OF_INTEREST;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.DtoMapper.extractFirstIdSegment;

import java.util.Optional;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedFeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.rest.ExpansionSettings;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.FeaturesOfInterestAccess;
import org.eclipse.sensinact.sensorthings.sensing.rest.create.FeaturesOfInterestCreate;
import org.eclipse.sensinact.sensorthings.sensing.rest.update.FeaturesOfInterestUpdate;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.UriInfo;

public class FeaturesOfInterestAccessImpl extends AbstractAccess
        implements FeaturesOfInterestAccess, FeaturesOfInterestCreate, FeaturesOfInterestUpdate {

    @Override
    public FeatureOfInterest getFeatureOfInterest(String id) {
        String provider = extractFirstIdSegment(id);
        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);

        FeatureOfInterest foi;
        try {
            foi = DtoMapper.toFeatureOfInterest(getSession(), application, getMapper(), uriInfo, getExpansions(),
                    parseFilter(FEATURES_OF_INTEREST), providerSnapshot);
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException("No feature of interest with id");
        }
        if (!foi.id().equals(id)) {
            throw new NotFoundException();
        }
        return foi;
    }

    // No history as it is *live* observation data not a data stream
    @Override
    public ResultList<Observation> getFeatureOfInterestObservations(String id) {
        String provider = extractFirstIdSegment(id);

        return getLiveObservations(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(EFilterContext.OBSERVATIONS), validateAndGetProvider(provider));
    }

    static ResultList<Observation> getLiveObservations(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter,
            ProviderSnapshot provider) {
        return new ResultList<>(null, null, provider.getServices().stream().flatMap(s -> s.getResources().stream())
                .filter(ResourceSnapshot::isSet)
                .map(r -> DtoMapper.toObservation(userSession, application, mapper, uriInfo, expansions, filter, r))
                .filter(Optional::isPresent).map(Optional::get).collect(toList()));
    }

    @Override
    public Observation getFeatureOfInterestObservation(String id, String id2) {
        String provider = extractFirstIdSegment(id);
        String provider2 = extractFirstIdSegment(id2);
        if (!provider.equals(provider2)) {
            throw new BadRequestException("The ids for the FeatureOfInterest and the Observation are inconsistent");
        }

        ResourceSnapshot resourceSnapshot = validateAndGetResourceSnapshot(id2);

        Optional<Observation> o;
        try {
            o = DtoMapper.toObservation(getSession(), application, getMapper(), uriInfo, getExpansions(),
                    parseFilter(EFilterContext.FEATURES_OF_INTEREST), resourceSnapshot);
        } catch (Exception e) {
            throw new NotFoundException();
        }

        if (o.isEmpty() || !id2.equals(o.get().id())) {
            throw new NotFoundException();
        }

        return o.get();
    }

    @Override
    public Datastream getFeatureOfInterestObservationDatastream(String id, String id2) {
        String provider = extractFirstIdSegment(id);
        String provider2 = extractFirstIdSegment(id2);
        if (!provider.equals(provider2)) {
            throw new BadRequestException("The ids for the FeatureOfInterest and the Observation are inconsistent");
        }

        ResourceSnapshot resourceSnapshot = validateAndGetResourceSnapshot(id2);

        Datastream d;
        try {
            d = DtoMapper.toDatastream(getSession(), application, getMapper(), uriInfo, getExpansions(),
                    resourceSnapshot, parseFilter(EFilterContext.DATASTREAMS));
        } catch (Exception e) {
            throw new NotFoundException();
        }

        if (!id2.startsWith(String.valueOf(d.id()))) {
            throw new NotFoundException();
        }

        return d;
    }

    @Override
    public Response createFeaturesOfInterestCObservation(String id, Observation observation) {
        ExpandedFeatureOfInterest featureOfInterest = getFeatureOfInterest(id, ExpandedFeatureOfInterest.class);
        featureOfInterest.observations = getFeatureOfInterestObservations(id).value;
        if (featureOfInterest.observations == null || featureOfInterest.observations.size() == 0) {
            featureOfInterest.observations = List.of(observation);
        } else {
            featureOfInterest.observations.add(observation);
        }
        return getExtraDelegate().update(getSession(), getMapper(), uriInfo, id, featureOfInterest,
                ExpandedFeatureOfInterest.class);
    }

    @Override
    public Response updateFeaturesOfInterest(String id, FeatureOfInterest featureOfInterest) {

        return getExtraDelegate().update(getSession(), getMapper(), uriInfo, id, featureOfInterest,
                FeatureOfInterest.class);

    }

    @Override
    public Response updateFeaturesOfInterestObservation(String featureId, String observationId,
            Observation observation) {
        ExpandedFeatureOfInterest featureOfInterest = getFeatureOfInterest(featureId, ExpandedFeatureOfInterest.class);
        List<Observation> observations = featureOfInterest.observations;
        if (observations == null || observations.isEmpty()) {
            throw new UnsupportedOperationException(String.format("No Observation %s found", observationId));
        }
        boolean updated = false;
        for (int i = 0; i < observations.size(); i++) {
            Observation obs = observations.get(i);
            if (obs.id.equals(observationId)) {
                observations.set(i, observation); // Replace the existing observation
                updated = true;
                break; // No need to continue looping
            }
        }
        if (!updated) {
            throw new UnsupportedOperationException(String.format("No Observation %s found", observationId));
        }
        return getExtraDelegate().update(getSession(), getMapper(), uriInfo, featureId, featureOfInterest,
                ExpandedFeatureOfInterest.class);
    }
}
