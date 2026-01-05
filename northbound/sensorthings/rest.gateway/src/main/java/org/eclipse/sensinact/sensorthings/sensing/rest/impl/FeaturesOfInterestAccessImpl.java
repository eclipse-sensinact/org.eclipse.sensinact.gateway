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

import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.FEATURES_OF_INTEREST;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.DtoMapperGet.extractFirstIdSegment;

import java.util.List;
import java.util.Optional;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.rest.ExpansionSettings;
import org.eclipse.sensinact.sensorthings.sensing.rest.UtilIds;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.FeaturesOfInterestAccess;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.extended.DtoMapper;
import org.eclipse.sensinact.sensorthings.sensing.rest.update.FeaturesOfInterestUpdate;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

public class FeaturesOfInterestAccessImpl extends AbstractAccess
        implements FeaturesOfInterestAccess, FeaturesOfInterestUpdate {

    @Override
    public FeatureOfInterest getFeatureOfInterest(String id) {
        if (getCache(ExpandedObservedProperty.class).getDto(id) != null) {
            FeatureOfInterest op = (FeatureOfInterest) getCache(FeatureOfInterest.class).getDto(id);
            return new FeatureOfInterest(DtoMapper.getLink(uriInfo, DtoMapper.VERSION, "/FeatureOfInterests", id),
                    op.id(), op.name(), op.description(), op.encodingType(), op.feature(), null);
        } else {
            String provider = extractFirstIdSegment(id);
            ProviderSnapshot providerSnapshot = validateAndGetProvider(getSession(), provider);

            FeatureOfInterest foi;
            try {
                foi = DtoMapperGet.toFeatureOfInterest(getSession(), application, getMapper(), uriInfo, getExpansions(),
                        parseFilter(FEATURES_OF_INTEREST), providerSnapshot);
            } catch (IllegalArgumentException iae) {
                throw new NotFoundException("No feature of interest with id");
            }
            if (!foi.id().equals(id)) {
                throw new NotFoundException();
            }
            return foi;
        }
    }

    // No history as it is *live* observation data not a data stream
    @Override
    public ResultList<Observation> getFeatureOfInterestObservations(String id) {
        String provider = extractFirstIdSegment(id);
        ProviderSnapshot providerDatastream = validateAndGetProvider(getSession(), provider);

        return getLiveObservations(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(EFilterContext.OBSERVATIONS), UtilIds.getDatastreamService(providerDatastream));
    }

    static ResultList<Observation> getLiveObservations(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter,
            ServiceSnapshot serviceSnapshot) {
        ExpandedObservation observation = (ExpandedObservation) UtilIds.getResourceField(serviceSnapshot,
                "lastObservation", Object.class);
        return new ResultList<>(null, null, List.of(DtoMapper.toObservation(userSession, application, mapper, uriInfo,
                expansions, filter, serviceSnapshot, observation)));
    }

    @Override
    public Observation getFeatureOfInterestObservation(String id, String id2) {
        String provider = extractFirstIdSegment(id);
        String provider2 = extractFirstIdSegment(id2);
        if (!provider.equals(provider2)) {
            throw new BadRequestException("The ids for the FeatureOfInterest and the Observation are inconsistent");
        }

        ResourceSnapshot resourceSnapshot = validateAndGetResourceSnapshot(getSession(), id2);

        Optional<Observation> o;
        try {
            o = DtoMapperGet.toObservation(getSession(), application, getMapper(), uriInfo, getExpansions(),
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

        ResourceSnapshot resourceSnapshot = validateAndGetResourceSnapshot(getSession(), id2);

        Datastream d;
        try {
            d = DtoMapperGet.toDatastream(getSession(), application, getMapper(), uriInfo, getExpansions(),
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
    public Response updateFeaturesOfInterest(String id, FeatureOfInterest foi) {
        getExtraDelegate().update(getSession(), getMapper(), uriInfo, requestContext.getMethod(), id, foi);

        return Response.noContent().build();
    }

    @Override
    public Response patchFeaturesOfInterest(String id, FeatureOfInterest foi) {
        return updateFeaturesOfInterest(id, foi);
    }

}
