package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessProviderUseCase;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * object
 */
@Component(service = IExtraUseCase.class)
public class ObservedPropertiesExtraUseCase extends AbstractExtraUseCase<ObservedProperty, ResourceSnapshot> {

    @Reference
    DataUpdate dataUpdate;

    @Reference
    IAccessProviderUseCase providerUseCase;

    public ExtraUseCaseResponse<ResourceSnapshot> create(ExtraUseCaseRequest<ObservedProperty> request) {
        try {
            Object obj = dataUpdate.pushUpdate(request.model()).getValue();
            // ProviderSnapshot provider = providerUseCase.read(session,
            // model.providerId());
            return new ExtraUseCaseResponse<ResourceSnapshot>(false, "fail to get providerSnapshot");

        } catch (InvocationTargetException | InterruptedException e) {
            return new ExtraUseCaseResponse<ResourceSnapshot>(false, "fail to get providerSnapshot");

        }

    }

    public ExtraUseCaseResponse<ResourceSnapshot> delete(ExtraUseCaseRequest<ObservedProperty> request) {
        return new ExtraUseCaseResponse<ResourceSnapshot>(false, "fail to get providerSnapshot");

    }

    public ExtraUseCaseResponse<ResourceSnapshot> patch(ExtraUseCaseRequest<ObservedProperty> request) {
        return new ExtraUseCaseResponse<ResourceSnapshot>(false, "fail to get providerSnapshot");

    }

    @Override
    protected List<SensorThingsUpdate> toDtos(ExtraUseCaseRequest<ObservedProperty> request) {
        // TODO Auto-generated method stub
        return null;
    }

    public ExtraUseCaseResponse<ResourceSnapshot> update(ExtraUseCaseRequest<ObservedProperty> request) {
        return new ExtraUseCaseResponse<ResourceSnapshot>(false, "fail to get providerSnapshot");

    }

}
