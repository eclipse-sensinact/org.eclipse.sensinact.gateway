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

import java.util.List;

import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.sensorthings.sensing.dto.Id;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.mapper.DtoToModelMapper;

import jakarta.ws.rs.HttpMethod;

/**
 * abstract use case
 *
 * @param <M>
 * @param <S>
 */
public abstract class AbstractExtraUseCaseDto<M extends Id, S> extends AbstractExtraUseCase<M, S> {

    public AbstractExtraUseCaseDto() {
    }

    public abstract List<SensorThingsUpdate> dtosToCreateUpdate(ExtraUseCaseRequest<M> request);

    public abstract List<AbstractSensinactCommand<?>> dtoToDelete(ExtraUseCaseRequest<M> request);

    protected void checkRequireField(ExtraUseCaseRequest<M> request) {
        if (HttpMethod.POST.equals(request.method()) || HttpMethod.PUT.equals(request.method())) {
            DtoToModelMapper.checkRequireField(request.model());
        }
    }

    /**
     * get id field for EMF
     *
     * @param aDto
     * @return
     */

    public abstract String getId(ExtraUseCaseRequest<M> request);
}
