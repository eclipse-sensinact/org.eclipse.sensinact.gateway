package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessProviderUseCase;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import jakarta.ws.rs.BadRequestException;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * object
 */
@Component(service = IExtraUseCase.class)
public class ObservedPropertiesExtraUseCase extends AbstractExtraUseCase<ExpandedObservedProperty, ResourceSnapshot> {

    @Reference
    IAccessProviderUseCase providerUseCase;

    @Reference
    DataUpdate dataUpdate;

    @Override
    protected IAccessProviderUseCase getProviderUseCase() {
        return providerUseCase;
    }

    public ExtraUseCaseResponse<ResourceSnapshot> create(ExtraUseCaseRequest<ExpandedObservedProperty> request) {
        try {
            Object obj = dataUpdate.pushUpdate(request.model()).getValue();
            // ProviderSnapshot provider = providerUseCase.read(session,
            // model.providerId());
            return new ExtraUseCaseResponse<ResourceSnapshot>(false, "fail to get providerSnapshot");

        } catch (InvocationTargetException | InterruptedException e) {
            return new ExtraUseCaseResponse<ResourceSnapshot>(false, "fail to get providerSnapshot");

        }

    }

    public ExtraUseCaseResponse<ResourceSnapshot> delete(ExtraUseCaseRequest<ExpandedObservedProperty> request) {
        return new ExtraUseCaseResponse<ResourceSnapshot>(false, "fail to get providerSnapshot");

    }

    public ExtraUseCaseResponse<ResourceSnapshot> patch(ExtraUseCaseRequest<ExpandedObservedProperty> request) {
        return new ExtraUseCaseResponse<ResourceSnapshot>(false, "fail to get providerSnapshot");

    }

    @Override
    protected List<SensorThingsUpdate> toDtos(ExtraUseCaseRequest<ExpandedObservedProperty> request) {
        // read thing for each location and update it
        ExpandedObservedProperty observedProperty = request.model();
        String idDatastream = observedProperty.datastream() != null ? (String) observedProperty.datastream().id()
                : request.parentId();

        if (idDatastream == null) {
            throw new BadRequestException("can't find datastream parent ");
        }
        String providerId = DtoMapper.extractFirstIdSegment(idDatastream);

        return List.of(DtoMapper.toObservedPropertyUpdate(providerId, idDatastream, observedProperty));
    }

    public ExtraUseCaseResponse<ResourceSnapshot> update(ExtraUseCaseRequest<ExpandedObservedProperty> request) {
        return new ExtraUseCaseResponse<ResourceSnapshot>(false, "fail to get providerSnapshot");

    }

}
