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

    @Override
    protected IAccessProviderUseCase getProviderUseCase() {
        return providerUseCase;
    }

    public ExtraUseCaseResponse<ProviderSnapshot> create(ExtraUseCaseRequest<HistoricalLocation> request) {
        try {
            Object obj = dataUpdate.pushUpdate(request.model()).getValue();
            // ProviderSnapshot provider = providerUseCase.read(session,
            // model.providerId());
            return new ExtraUseCaseResponse<ProviderSnapshot>(false, "fail to get providerSnapshot");

        } catch (InvocationTargetException | InterruptedException e) {
            return new ExtraUseCaseResponse<ProviderSnapshot>(false, "fail to get providerSnapshot");

        }

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

}
