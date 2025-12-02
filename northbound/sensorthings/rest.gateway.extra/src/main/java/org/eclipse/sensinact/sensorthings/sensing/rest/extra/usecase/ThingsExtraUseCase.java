package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessProviderUseCase;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * object
 */
@Component(service = IExtraUseCase.class)
public class ThingsExtraUseCase extends AbstractExtraUseCase<ExpandedThing, ProviderSnapshot> {

    @Reference
    DataUpdate dataUpdate;

    @Reference
    IAccessProviderUseCase providerUseCase;

    public ExtraUseCaseResponse<ProviderSnapshot> create(ExtraUseCaseRequest<ExpandedThing> request) {
        String id = getId(request.model());
        List<SensorThingsUpdate> listDtoModels = toDtos(request);

        // update/create provider
        try {
            dataUpdate.pushUpdate(listDtoModels).getValue();

        } catch (InvocationTargetException | InterruptedException e) {
            return new ExtraUseCaseResponse<ProviderSnapshot>(false, "fail to create");

        }

        ProviderSnapshot provider = providerUseCase.read(request.session(), id);
        if (provider != null) {
            return new ExtraUseCaseResponse<ProviderSnapshot>(id, provider);
        }
        return new ExtraUseCaseResponse<ProviderSnapshot>(false, "fail to get Snapshot");

    }

    public ExtraUseCaseResponse<ProviderSnapshot> delete(ExtraUseCaseRequest<ExpandedThing> request) {
        return new ExtraUseCaseResponse<ProviderSnapshot>(false, "fail to get Snapshot");

    }

    public ExtraUseCaseResponse<ProviderSnapshot> patch(ExtraUseCaseRequest<ExpandedThing> request) {
        return new ExtraUseCaseResponse<ProviderSnapshot>(false, "fail to get Snapshot");

    }

    @Override
    protected List<SensorThingsUpdate> toDtos(ExtraUseCaseRequest<ExpandedThing> request) {
        return DtoMapper.toUpdates(request.model()).toList();
    }

    public ExtraUseCaseResponse<ProviderSnapshot> update(ExtraUseCaseRequest<ExpandedThing> request) {

        List<SensorThingsUpdate> listDtoModels = toDtos(request);

        // update/create provider
        try {
            dataUpdate.pushUpdate(listDtoModels).getValue();

        } catch (InvocationTargetException | InterruptedException e) {
            return new ExtraUseCaseResponse<ProviderSnapshot>(false, "fail to create");

        }

        ProviderSnapshot snapshot = providerUseCase.read(request.session(), request.id());
        if (snapshot != null) {
            return new ExtraUseCaseResponse<ProviderSnapshot>(request.id(), snapshot);
        }
        return new ExtraUseCaseResponse<ProviderSnapshot>(false, "fail to get Snapshot");

    }

    @Override
    public String getId(ExpandedThing dto) {
        return DtoMapper.sanitizeId(dto.id() != null ? dto.id() : dto.name());
    }

}
