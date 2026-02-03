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

import java.lang.reflect.InvocationTargetException;
import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.Id;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ext.Providers;

/**
 * abstract use case
 *
 * @param <M>
 * @param <S>
 */
public abstract class AbstractExtraUseCaseDtoDelete<M extends Id, S> extends AbstractExtraUseCaseDto<M, S> {

    public abstract AbstractSensinactCommand<?> dtoToDelete(ExtraUseCaseRequest<M> request);

    public AbstractExtraUseCaseDtoDelete(Providers providers) {
        super(providers);
    }

    protected static void checkRequireField(Id ds) {
        try {
            DtoMapperSimple.checkRequireField(ds);
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    protected static void checkRequireLink(Object... obs) {
        try {
            DtoMapperSimple.checkRequireLink(obs);
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    public ExtraUseCaseResponse<S> delete(ExtraUseCaseRequest<M> request) {
        try {
            String providerId = DtoMapperSimple.extractFirstIdSegment(request.id());
            ProviderSnapshot provider = providerUseCase.read(request.session(), providerId);
            if (provider == null) {
                throw new NotFoundException();
            }

            gatewayThread.execute(dtoToDelete(request)).getValue();
        } catch (InvocationTargetException | InterruptedException e) {
            throw new InternalServerErrorException(e);
        }
        return new ExtraUseCaseResponse<S>(true, "datastream deleted");

    }
}
