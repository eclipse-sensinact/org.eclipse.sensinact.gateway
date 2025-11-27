package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.snapshot.Snapshot;
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
public class HistoricalLocationsExtraUseCase extends AbstractExtraUseCase<HistoricalLocation> {

    @Reference
    DataUpdate dataUpdate;

    @Reference
    IAccessProviderUseCase providerUseCase;

    public ExtraUseCaseResponse<Snapshot> create(ExtraUseCaseRequest<HistoricalLocation> request) {
        try {
            Object obj = dataUpdate.pushUpdate(request.model()).getValue();
            // ProviderSnapshot provider = providerUseCase.read(session,
            // model.providerId());
            return new ExtraUseCaseResponse<Snapshot>(false, "fail to get providerSnapshot");

        } catch (InvocationTargetException | InterruptedException e) {
            return new ExtraUseCaseResponse<Snapshot>(false, "fail to get providerSnapshot");

        }

    }

    public ExtraUseCaseResponse<Snapshot> delete(ExtraUseCaseRequest<HistoricalLocation> request) {
        return new ExtraUseCaseResponse<Snapshot>(false, "fail to get providerSnapshot");

    }

    public ExtraUseCaseResponse<Snapshot> patch(ExtraUseCaseRequest<HistoricalLocation> request) {
        return new ExtraUseCaseResponse<Snapshot>(false, "fail to get providerSnapshot");

    }

    @Override
    protected List<SensorThingsUpdate> toDtos(ExtraUseCaseRequest<HistoricalLocation> request) {
        // TODO Auto-generated method stub
        return null;
    }

    public ExtraUseCaseResponse<Snapshot> update(ExtraUseCaseRequest<HistoricalLocation> request) {
        return new ExtraUseCaseResponse<Snapshot>(false, "fail to get providerSnapshot");

    }

}
