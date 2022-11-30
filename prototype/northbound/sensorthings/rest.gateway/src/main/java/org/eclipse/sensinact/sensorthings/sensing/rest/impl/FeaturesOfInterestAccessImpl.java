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

import org.eclipse.sensinact.prototype.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.rest.FeaturesOfInterestAccess;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Providers;

public class FeaturesOfInterestAccessImpl implements FeaturesOfInterestAccess {

    @Context
    UriInfo uriInfo;

    @Context
    Providers providers;

    /**
     * Returns a user session
     */
    private SensiNactSession getSession() {
        return providers.getContextResolver(SensiNactSession.class, MediaType.WILDCARD_TYPE).getContext(null);
    }

    /**
     * Returns an object mapper
     */
    private ObjectMapper getMapper() {
        return providers.getContextResolver(ObjectMapper.class, MediaType.APPLICATION_JSON_TYPE).getContext(null);
    }

    @Override
    public FeatureOfInterest getFeatureOfInterest(String id) {
        String provider = extractFirstIdSegment(id);

        FeatureOfInterest foi;
        try {
            foi = DtoMapper.toFeatureOfInterest(getSession(), uriInfo, getMapper(), provider);
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException("No feature of interest with id");
        }
        if (!foi.id.equals(id)) {
            throw new NotFoundException();
        }
        return foi;
    }

    @Override
    public ResultList<Observation> getFeatureOfInterestObservations(String id) {
        String provider = extractFirstIdSegment(id);

        SensiNactSession userSession = getSession();
        ResultList<Observation> list = new ResultList<>();
        list.value = userSession.describeProvider(provider).services.stream()
                .map(s -> userSession.describeService(provider, s))
                .flatMap(s -> s.resources.stream().map(r -> userSession.describeResource(s.provider, s.service, r)))
                .map(r -> DtoMapper.toObservation(uriInfo, r)).collect(toList());

        return list;
    }

    @Override
    public Observation getFeatureOfInterestObservation(String id, String id2) {
        String provider = extractFirstIdSegment(id);
        String provider2 = extractFirstIdSegment(id2);
        if (!provider.equals(provider2)) {
            throw new BadRequestException("The ids for the FeatureOfInterest and the Observation are inconsistent");
        }

        String service = extractFirstIdSegment(id2.substring(provider2.length() + 1));
        String resource = extractFirstIdSegment(id2.substring(provider.length() + service.length() + 2));

        Observation o;
        try {
            o = DtoMapper.toObservation(uriInfo, getSession().describeResource(provider2, service, resource));
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

        String service = extractFirstIdSegment(id2.substring(provider2.length() + 1));
        String resource = extractFirstIdSegment(id2.substring(provider.length() + service.length() + 2));

        SensiNactSession userSession = getSession();
        Datastream d;
        try {
            d = DtoMapper.toDatastream(userSession, getMapper(), uriInfo,
                    userSession.describeResource(provider2, service, resource));
        } catch (Exception e) {
            throw new NotFoundException();
        }

        if (!String.join("~", provider, service, resource).equals(d.id)) {
            throw new NotFoundException();
        }

        return d;
    }

}
