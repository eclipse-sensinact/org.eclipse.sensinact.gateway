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

import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.sensorthings.sensing.dto.Id;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessProviderUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessServiceUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.mapper.DtoToModelMapper;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.ext.Providers;

/**
 * abstract use case
 *
 * @param <M>
 * @param <S>
 */
public abstract class AbstractExtraUseCaseDto<M extends Id, S> extends AbstractExtraUseCase<M, S> {

    public abstract List<SensorThingsUpdate> dtosToCreateUpdate(ExtraUseCaseRequest<M> request);

    protected final DataUpdate dataUpdate;
    protected final IAccessProviderUseCase providerUseCase;
    protected final IAccessServiceUseCase serviceUseCase;
    protected final GatewayThread gatewayThread;

    protected void checkRequireField(ExtraUseCaseRequest<M> request) {
        if (HttpMethod.POST.equals(request.method()) || HttpMethod.PUT.equals(request.method())) {
            DtoToModelMapper.checkRequireField(request.model());
        }
    }

    public AbstractExtraUseCaseDto(Providers providers) {
        dataUpdate = resolve(providers, DataUpdate.class);
        providerUseCase = resolve(providers, IAccessProviderUseCase.class);
        serviceUseCase = resolve(providers, IAccessServiceUseCase.class);
        gatewayThread = resolve(providers, GatewayThread.class);
    }
}
