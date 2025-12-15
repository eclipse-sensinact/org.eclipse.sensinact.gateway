package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.eclipse.sensinact.sensorthings.sensing.dto.Id;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;

public abstract class AbstractExtraUseCase<M extends Id, S> implements IExtraUseCase<M, S> {

    private Class<M> type;

    @SuppressWarnings("unchecked")
    public AbstractExtraUseCase() {
        var superclass = getClass().getGenericSuperclass();
        var param = ((ParameterizedType) superclass).getActualTypeArguments()[0];
        this.type = (Class<M>) param;
    }

    public abstract String getId(M aDto);

    public Class<M> getType() {
        return type;
    }

    protected abstract List<SensorThingsUpdate> toDtos(ExtraUseCaseRequest<M> request);

}
