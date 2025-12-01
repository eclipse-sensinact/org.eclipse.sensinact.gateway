package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.Snapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.Id;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessProviderUseCase;

public abstract class AbstractExtraUseCase<M extends Id, S extends Snapshot> implements IExtraUseCase<M, S> {

    private Class<M> type;

    protected abstract IAccessProviderUseCase getProviderUseCase();

    @SuppressWarnings("unchecked")
    public AbstractExtraUseCase() {
        var superclass = getClass().getGenericSuperclass();
        var param = ((ParameterizedType) superclass).getActualTypeArguments()[0];
        this.type = (Class<M>) param;
    }

    public abstract String getId(M aDto);

    protected ProviderSnapshot getProviderSnapshot(ExtraUseCaseRequest<M> request, String thingId) {
        ProviderSnapshot provider = getProviderUseCase().read(request.session(), thingId);
        if (provider == null) {
            throw new IllegalStateException("Provider not found for thing ID: " + thingId);
        }
        return provider;
    }

    public Class<M> getType() {
        return type;
    }

    protected abstract List<SensorThingsUpdate> toDtos(ExtraUseCaseRequest<M> request);

}
