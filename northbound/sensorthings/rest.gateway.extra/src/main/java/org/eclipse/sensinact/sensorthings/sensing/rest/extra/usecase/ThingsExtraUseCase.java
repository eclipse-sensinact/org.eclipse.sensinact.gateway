package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.snapshot.Snapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.Helpers;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessProviderUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessResourceUseCase;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * object
 */
@Component(service = IExtraUseCase.class)
public class ThingsExtraUseCase extends AbstractExtraUseCase<ExpandedThing> {

    @Reference
    DataUpdate dataUpdate;

    @Reference
    IAccessProviderUseCase providerUseCase;

    @Reference
    IAccessResourceUseCase resourceUseCase;

    public ExtraUseCaseResponse<Snapshot> create(ExtraUseCaseRequest<ExpandedThing> request) {
        String id = sanitizeId(request.model().id != null ? request.model().id : request.model().name);
        List<SensorThingsUpdate> listDtoModels = toDtos(request);

        // update/create provider
        try {
            dataUpdate.pushUpdate(listDtoModels).getValue();

        } catch (InvocationTargetException | InterruptedException e) {
            return new ExtraUseCaseResponse<Snapshot>(false, "fail to create");

        }

        Snapshot provider = providerUseCase.read(request.session(), id);
        if (provider != null) {
            return new ExtraUseCaseResponse<Snapshot>(id, provider);
        }
        return new ExtraUseCaseResponse<Snapshot>(false, "fail to get Snapshot");

    }

    public ExtraUseCaseResponse<Snapshot> delete(ExtraUseCaseRequest<ExpandedThing> request) {
        return new ExtraUseCaseResponse<Snapshot>(false, "fail to get Snapshot");

    }

    public ExtraUseCaseResponse<Snapshot> patch(ExtraUseCaseRequest<ExpandedThing> request) {
        return new ExtraUseCaseResponse<Snapshot>(false, "fail to get Snapshot");

    }

    @Override
    protected List<SensorThingsUpdate> toDtos(ExtraUseCaseRequest<ExpandedThing> request) {
        return Helpers.toUpdates(request.model()).toList();
    }

    public ExtraUseCaseResponse<Snapshot> update(ExtraUseCaseRequest<ExpandedThing> request) {

        List<SensorThingsUpdate> listDtoModels = toDtos(request);

        // update/create provider
        try {
            dataUpdate.pushUpdate(listDtoModels).getValue();

        } catch (InvocationTargetException | InterruptedException e) {
            return new ExtraUseCaseResponse<Snapshot>(false, "fail to create");

        }

        Snapshot snapshot = providerUseCase.read(request.session(), request.id());
        if (snapshot != null) {
            return new ExtraUseCaseResponse<Snapshot>(request.id(), snapshot);
        }
        return new ExtraUseCaseResponse<Snapshot>(false, "fail to get Snapshot");

    }

}
