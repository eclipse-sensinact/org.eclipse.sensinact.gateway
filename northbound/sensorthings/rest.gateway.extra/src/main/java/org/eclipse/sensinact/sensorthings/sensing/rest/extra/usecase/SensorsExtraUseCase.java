package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessProviderUseCase;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * object
 */
@Component(service = IExtraUseCase.class)
public class SensorsExtraUseCase extends AbstractExtraUseCase<Sensor, ResourceSnapshot> {

    @Reference
    DataUpdate dataUpdate;

    @Reference
    IAccessProviderUseCase providerUseCase;

    public ExtraUseCaseResponse<ResourceSnapshot> create(ExtraUseCaseRequest<Sensor> request) {
        try {
            Object obj = dataUpdate.pushUpdate(request.model()).getValue();
            // ProviderSnapshot provider = providerUseCase.read(session,
            // model.providerId());
            return new ExtraUseCaseResponse<ResourceSnapshot>(false, "fail to get providerSnapshot");

        } catch (InvocationTargetException | InterruptedException e) {
            return new ExtraUseCaseResponse<ResourceSnapshot>(false, "fail to get providerSnapshot");

        }

    }

    public ExtraUseCaseResponse<ResourceSnapshot> delete(ExtraUseCaseRequest<Sensor> request) {
        return new ExtraUseCaseResponse<ResourceSnapshot>(false, "fail to get providerSnapshot");

    }

    public ExtraUseCaseResponse<ResourceSnapshot> patch(ExtraUseCaseRequest<Sensor> request) {
        return new ExtraUseCaseResponse<ResourceSnapshot>(false, "fail to get providerSnapshot");

    }

    @Override
    protected List<SensorThingsUpdate> toDtos(ExtraUseCaseRequest<Sensor> request) {

        // TODO
        return null;
    }

    public ExtraUseCaseResponse<ResourceSnapshot> update(ExtraUseCaseRequest<Sensor> request) {
        return new ExtraUseCaseResponse<ResourceSnapshot>(false, "fail to get providerSnapshot");

    }

}
