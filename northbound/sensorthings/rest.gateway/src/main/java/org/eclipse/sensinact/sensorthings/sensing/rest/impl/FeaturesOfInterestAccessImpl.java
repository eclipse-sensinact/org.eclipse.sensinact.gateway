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
import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.rest.ExpansionSettings;
import org.eclipse.sensinact.sensorthings.sensing.rest.UtilIds;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.FeaturesOfInterestAccess;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IDtoMemoryCache;
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
        String idObservation = UtilIds.extractSecondIdSegment(id);
        String idFoi = UtilIds.extractThirdIdSegment(id);
        IDtoMemoryCache<?> wCache = getCache(ExpandedObservation.class);
        if (wCache != null && wCache.getDto(idObservation) != null) {
            FeatureOfInterest op = (FeatureOfInterest) getCache(FeatureOfInterest.class).getDto(idObservation);
            String foiLink = DtoMapper.getLink(uriInfo, DtoMapper.VERSION, "/FeaturesOfInterest", idFoi);
            String observationLink = DtoMapper.getLink(uriInfo, foiLink, "/Observations", idFoi);
            return new FeatureOfInterest(foiLink, op.id(), op.name(), op.description(), op.encodingType(), op.feature(),
                    observationLink);
        } else {
            String provider = extractFirstIdSegment(id);
            ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);

            FeatureOfInterest foi;
            try {
                ServiceSnapshot service = UtilIds.getDatastreamService(providerSnapshot);
                foi = DtoMapper.toFeatureOfInterest(getSession(), application, getMapper(), uriInfo, getExpansions(),
                        parseFilter(FEATURES_OF_INTEREST), service);
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
        ProviderSnapshot providerDatastream = validateAndGetProvider(provider);

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
        String providerDatastream = extractFirstIdSegment(id);
        String providerDatastream2 = extractFirstIdSegment(id2);
        if (!providerDatastream.equals(providerDatastream2)) {
            throw new BadRequestException("The ids for the FeatureOfInterest and the Observation are inconsistent");
        }

        ProviderSnapshot providerSnapshot = validateAndGetProvider(providerDatastream);
        ServiceSnapshot service = UtilIds.getDatastreamService(providerSnapshot);
        if (service != null) {
            throw new NotFoundException();
        }
        Observation o = DtoMapper.toObservation(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(EFilterContext.FEATURES_OF_INTEREST), service);

        return o;
    }

    @Override
    public Datastream getFeatureOfInterestObservationDatastream(String id, String id2) {
        String provider = extractFirstIdSegment(id);
        String provider2 = extractFirstIdSegment(id2);
        if (!provider.equals(provider2)) {
            throw new BadRequestException("The ids for the FeatureOfInterest and the Observation are inconsistent");
        }

        String providerDatastream = extractFirstIdSegment(id);
        String providerDatastream2 = extractFirstIdSegment(id2);
        if (!providerDatastream.equals(providerDatastream2)) {
            throw new BadRequestException("The ids for the FeatureOfInterest and the Observation are inconsistent");
        }

        ProviderSnapshot providerSnapshot = validateAndGetProvider(providerDatastream);
        ServiceSnapshot service = UtilIds.getDatastreamService(providerSnapshot);
        if (service != null) {
            throw new NotFoundException();
        }
        Datastream o = DtoMapper.toDatastream(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(EFilterContext.FEATURES_OF_INTEREST), service);

        return o;
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
