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
import java.util.List;

import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.IndependentCommands;
import org.eclipse.sensinact.sensorthings.sensing.dto.Id;
import jakarta.ws.rs.ext.Providers;

/**
 * abstract use case
 *
 * @param <M>
 * @param <S>
 */
public abstract class AbstractExtraUseCaseDtoDelete<M extends Id, S> extends AbstractExtraUseCaseDto<M, S> {

    public abstract List<AbstractSensinactCommand<?>> dtoToDelete(ExtraUseCaseRequest<M> request);

    public AbstractExtraUseCaseDtoDelete(Providers providers) {
        super(providers);
    }

    public ExtraUseCaseResponse<S> delete(ExtraUseCaseRequest<M> request) {
        try {
            List<AbstractSensinactCommand<?>> commands = dtoToDelete(request);

            IndependentCommands<?> multiCommand = new IndependentCommands<>(commands);
            gatewayThread.execute(multiCommand).getValue();
        } catch (InvocationTargetException | InterruptedException e) {
            return new ExtraUseCaseResponse<S>(false, "fail to delete");
        }
        return new ExtraUseCaseResponse<S>(true, "datastream deleted");

    }
}
