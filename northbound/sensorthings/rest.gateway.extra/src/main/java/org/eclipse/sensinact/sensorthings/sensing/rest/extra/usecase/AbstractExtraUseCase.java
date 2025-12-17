/*********************************************************************
* Copyright (c) 2025 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.eclipse.sensinact.sensorthings.sensing.dto.Id;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;

/**
 * abstract use case
 *
 * @param <M>
 * @param <S>
 */
public abstract class AbstractExtraUseCase<M extends Id, S> implements IExtraUseCase<M, S> {

    private Class<M> type;

    @SuppressWarnings("unchecked")
    public AbstractExtraUseCase() {
        var superclass = getClass().getGenericSuperclass();
        var param = ((ParameterizedType) superclass).getActualTypeArguments()[0];
        this.type = (Class<M>) param;
    }

    /**
     * get id field for EMF
     *
     * @param aDto
     * @return
     */
    public abstract String getId(M aDto);

    /**
     * get generic type (dto) for use case
     */
    public Class<M> getType() {
        return type;
    }

    /**
     * return list of record update
     *
     * @param request
     * @return
     */
    protected abstract List<SensorThingsUpdate> toDtos(ExtraUseCaseRequest<M> request);

}
