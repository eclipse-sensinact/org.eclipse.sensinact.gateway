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
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.DtoMapper.extractFirstIdSegment;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.rest.ExpansionSettings;
import org.eclipse.sensinact.sensorthings.sensing.rest.FeaturesOfInterestAccess;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.UriInfo;

public class FeaturesOfInterestAccessImpl extends AbstractAccess implements FeaturesOfInterestAccess {

    @Override
    public FeatureOfInterest getFeatureOfInterest(String id) {
        String provider = extractFirstIdSegment(id);
        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);

        FeatureOfInterest foi;
        try {
            foi = DtoMapper.toFeatureOfInterest(getSession(), application, getMapper(), uriInfo,
                    getExpansions(), providerSnapshot);
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException("No feature of interest with id");
        }
        if (!foi.id.equals(id)) {
            throw new NotFoundException();
        }
        return foi;
    }

    // No history as it is *live* observation data not a data stream
    @Override
    public ResultList<Observation> getFeatureOfInterestObservations(String id) {
        String provider = extractFirstIdSegment(id);

        return getLiveObservations(getSession(), application, getMapper(), uriInfo,
                getExpansions(), validateAndGetProvider(provider));
    }

    static ResultList<Observation> getLiveObservations(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ProviderSnapshot provider) {
        ResultList<Observation> list = new ResultList<>();
        list.value = provider.getServices().stream()
                .flatMap(s -> s.getResources().stream())
                .filter(ResourceSnapshot::isSet)
                .map(r -> DtoMapper.toObservation(userSession, application, mapper, uriInfo, expansions, r)).collect(toList());
        return list;
    }

    @Override
    public Observation getFeatureOfInterestObservation(String id, String id2) {
        String provider = extractFirstIdSegment(id);
        String provider2 = extractFirstIdSegment(id2);
        if (!provider.equals(provider2)) {
            throw new BadRequestException("The ids for the FeatureOfInterest and the Observation are inconsistent");
        }

        ResourceSnapshot resourceSnapshot = validateAndGetResourceSnapshot(id2);

        Observation o;
        try {
            o = DtoMapper.toObservation(getSession(), application, getMapper(), uriInfo, getExpansions(),
                    resourceSnapshot);
        } catch (Exception e) {
            throw new NotFoundException();
        }

        if (!id2.equals(o.id)) {
            throw new NotFoundException();
        }

        return o;
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
                    resourceSnapshot);
        } catch (Exception e) {
            throw new NotFoundException();
        }

        if (!id2.startsWith(String.valueOf(d.id))) {
            throw new NotFoundException();
        }

        return d;
    }

}
