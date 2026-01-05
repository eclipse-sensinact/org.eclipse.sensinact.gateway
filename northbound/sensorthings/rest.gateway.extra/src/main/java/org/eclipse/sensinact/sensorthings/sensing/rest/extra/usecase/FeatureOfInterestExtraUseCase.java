/*********************************************************************
* Copyright (c) 2025 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;

import jakarta.ws.rs.ext.Providers;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * FeatureOfInterest
 */
public class FeatureOfInterestExtraUseCase extends AbstractExtraUseCase<FeatureOfInterest, FeatureOfInterest> {

    Map<String, FeatureOfInterest> featureOfInterestById = new ConcurrentHashMap<String, FeatureOfInterest>();

    public FeatureOfInterestExtraUseCase(Providers providers) {
    }

    public ExtraUseCaseResponse<FeatureOfInterest> create(ExtraUseCaseRequest<FeatureOfInterest> request) {
        FeatureOfInterest featureOfInterest = request.model();
        checkRequireField(request);
        String featureOfInterestId = getId(request);
        FeatureOfInterest createFoi = new FeatureOfInterest(featureOfInterest.selfLink(), featureOfInterestId,
                featureOfInterest.name(), featureOfInterest.description(), featureOfInterest.encodingType(),
                featureOfInterest.feature(), null);
        featureOfInterestById.put(featureOfInterestId, createFoi);
        return new ExtraUseCaseResponse<FeatureOfInterest>(featureOfInterestId, createFoi);

    }

    @Override
    public String getId(ExtraUseCaseRequest<FeatureOfInterest> request) {
        return request.id() != null ? request.id()
                : DtoToModelMapper
                        .sanitizeId(request.model().id() != null ? request.model().id() : request.model().name());
    }

    public ExtraUseCaseResponse<FeatureOfInterest> delete(ExtraUseCaseRequest<FeatureOfInterest> request) {
        return new ExtraUseCaseResponse<FeatureOfInterest>(false, "not implemented");

    }

    @Override
    protected List<SensorThingsUpdate> toDtos(ExtraUseCaseRequest<FeatureOfInterest> request) {
        // read thing for each location and update it
        return null;
    }

    public ExtraUseCaseResponse<FeatureOfInterest> update(ExtraUseCaseRequest<FeatureOfInterest> request) {
        return new ExtraUseCaseResponse<FeatureOfInterest>(false, "not implemented");

    }

    public FeatureOfInterest removeInMemoryFeatureOfInterest(String id) {
        return featureOfInterestById.remove(id);

    }

    public FeatureOfInterest getInMemoryFeatureOfInterest(String id) {
        return featureOfInterestById.get(id);
    }
}
