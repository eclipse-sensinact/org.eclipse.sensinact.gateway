package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessProviderUseCase;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * object
 */
@Component(service = IExtraUseCase.class)
public class HistoricalLocationsExtraUseCase extends AbstractExtraUseCase<HistoricalLocation, ProviderSnapshot> {
    @Reference
    IAccessProviderUseCase providerUseCase;

    @Reference
    DataUpdate dataUpdate;

    public ExtraUseCaseResponse<ProviderSnapshot> create(ExtraUseCaseRequest<HistoricalLocation> request) {
        // TODO what is the id if not setted ?
        String id = getId(request.model());
        List<SensorThingsUpdate> listDtoModels = toDtos(request);

        // update/create provider
        try {
            dataUpdate.pushUpdate(listDtoModels).getValue();

        } catch (InvocationTargetException | InterruptedException e) {
            return new ExtraUseCaseResponse<ProviderSnapshot>(false, "fail to create");

        }

        ProviderSnapshot snapshot = providerUseCase.read(request.session(), id);
        if (snapshot != null) {
            return new ExtraUseCaseResponse<ProviderSnapshot>(id, snapshot);
        }
        return new ExtraUseCaseResponse<ProviderSnapshot>(false, "fail to get Snapshot");
    }

    public ExtraUseCaseResponse<ProviderSnapshot> delete(ExtraUseCaseRequest<HistoricalLocation> request) {
        return new ExtraUseCaseResponse<ProviderSnapshot>(false, "fail to get providerSnapshot");

    }

    public ExtraUseCaseResponse<ProviderSnapshot> patch(ExtraUseCaseRequest<HistoricalLocation> request) {
        return new ExtraUseCaseResponse<ProviderSnapshot>(false, "fail to get providerSnapshot");

    }

    @Override
    protected List<SensorThingsUpdate> toDtos(ExtraUseCaseRequest<HistoricalLocation> request) {
        // TODO Auto-generated method stub
        return null;
    }

    public ExtraUseCaseResponse<ProviderSnapshot> update(ExtraUseCaseRequest<HistoricalLocation> request) {
        return new ExtraUseCaseResponse<ProviderSnapshot>(false, "fail to get providerSnapshot");

    }

    @Override
    public String getId(HistoricalLocation dto) {
        return DtoMapper.sanitizeId(dto.id() != null ? dto.id() : dto.time());
    }

}
