package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.Id;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.Helpers;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessProviderUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.utils.IDtoMapper;
import org.osgi.service.component.annotations.Reference;

public abstract class AbstractExtraUseCase<M extends Id> implements IExtraUseCase<M> {

    private Class<M> type;

    @Reference
    DataUpdate dataUpdate;

    @Reference
    IDtoMapper dtoMapper;

    @Reference
    IAccessProviderUseCase providerUseCase;

    @Reference
    IAccessProviderUseCase resourceUseCase;

    @SuppressWarnings("unchecked")
    public AbstractExtraUseCase() {
        var superclass = getClass().getGenericSuperclass();
        var param = ((ParameterizedType) superclass).getActualTypeArguments()[0];
        this.type = (Class<M>) param;
    }

    protected ProviderSnapshot getProviderSnapshot(ExtraUseCaseRequest<M> request, Id thingId) {
        ProviderSnapshot provider = providerUseCase.read(request.session(), (String) thingId.id);
        if (provider == null) {
            throw new IllegalStateException("Provider not found for thing ID: " + thingId.id);
        }
        return provider;
    }

    public Class<M> getType() {
        return type;
    }

    protected String sanitizeId(Object object) {
        return Helpers.sanitizeId(object);
    }

    protected abstract List<SensorThingsUpdate> toDtos(ExtraUseCaseRequest<M> request);

}
