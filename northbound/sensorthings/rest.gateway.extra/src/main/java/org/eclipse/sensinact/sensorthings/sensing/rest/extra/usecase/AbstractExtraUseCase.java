package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import java.lang.reflect.ParameterizedType;

import org.eclipse.sensinact.sensorthings.sensing.dto.Id;

public abstract class AbstractExtraUseCase<D extends Id> implements IExtraUseCase<D> {

    private Class<D> type;

    @SuppressWarnings("unchecked")
    public AbstractExtraUseCase() {
        var superclass = getClass().getGenericSuperclass();
        var param = ((ParameterizedType) superclass).getActualTypeArguments()[0];
        this.type = (Class<D>) param;
    }

    public Class<D> getType() {
        return type;
    }
}
