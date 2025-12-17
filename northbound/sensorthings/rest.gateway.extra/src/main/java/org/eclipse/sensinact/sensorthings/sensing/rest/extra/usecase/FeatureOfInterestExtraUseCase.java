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

import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessResourceUseCase;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * FeatureOfInterest
 */
@Component(service = { IExtraUseCase.class, IFeatureOfInterestExtraUseCase.class })
public class FeatureOfInterestExtraUseCase extends AbstractExtraUseCase<FeatureOfInterest, FeatureOfInterest>
        implements IFeatureOfInterestExtraUseCase {

    @Reference
    DataUpdate dataUpdate;

    @Reference
    IAccessResourceUseCase resourceUseCase;

    Map<String, FeatureOfInterest> featureOfInterestById = new ConcurrentHashMap<String, FeatureOfInterest>();

    public ExtraUseCaseResponse<FeatureOfInterest> create(ExtraUseCaseRequest<FeatureOfInterest> request) {
        String id = getId(request.model());
        FeatureOfInterest featureOfInterest = request.model();
        DtoToModelMapper.checkRequireField(featureOfInterest);
        String featureOfInterestId = getId(featureOfInterest);
        FeatureOfInterest createFoi = new FeatureOfInterest(featureOfInterest.selfLink(), id, featureOfInterest.name(),
                featureOfInterest.description(), featureOfInterest.encodingType(), featureOfInterest.feature(), null);
        featureOfInterestById.put(featureOfInterestId, createFoi);
        return new ExtraUseCaseResponse<FeatureOfInterest>(id, createFoi);

    }

    @Override
    public String getId(FeatureOfInterest dto) {
        return DtoToModelMapper.sanitizeId(dto.id() != null ? dto.id() : dto.name());
    }

    public ExtraUseCaseResponse<FeatureOfInterest> delete(ExtraUseCaseRequest<FeatureOfInterest> request) {
        return new ExtraUseCaseResponse<FeatureOfInterest>(false, "not implemented");

    }

    public ExtraUseCaseResponse<FeatureOfInterest> patch(ExtraUseCaseRequest<FeatureOfInterest> request) {
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

    @Override
    public FeatureOfInterest getInMemoryFeatureOfInterest(String id) {
        return featureOfInterestById.get(id);
    }
}
