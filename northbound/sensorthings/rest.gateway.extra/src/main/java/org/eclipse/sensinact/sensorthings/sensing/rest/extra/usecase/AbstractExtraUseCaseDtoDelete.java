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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.Id;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IDtoMemoryCache;
import org.osgi.util.promise.Promise;

import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.ext.Providers;

/**
 * abstract use case
 *
 * @param <M>
 * @param <S>
 */
public abstract class AbstractExtraUseCaseDtoDelete<M extends Id, S> extends AbstractExtraUseCaseDto<M, S> {

    public abstract AbstractSensinactCommand<?> dtoToDelete(ExtraUseCaseRequest<M> request);

    public AbstractExtraUseCaseDtoDelete(Providers providers, Application application) {
        super(providers, application);
    }

    protected List<Promise<Void>> removeDatastream(SensinactDigitalTwin twin, String providerId) {
        ArrayList<Promise<Void>> list = new ArrayList<Promise<Void>>();

        SensinactResource resource = twin.getResource(providerId, DtoMapperSimple.SERVICE_DATASTREAM, "id");
        if (resource != null) {
            list.add(resource.setValue(null));
        }
        resource = twin.getResource(providerId, DtoMapperSimple.SERVICE_DATASTREAM, "lastObservation");
        if (resource != null) {
            list.add(resource.setValue(null));
        }
        return list;
    }

    public ExtraUseCaseResponse<S> delete(ExtraUseCaseRequest<M> request) {
        try {
            String providerId = DtoMapperSimple.extractFirstIdSegment(request.id());
            ProviderSnapshot provider = providerUseCase.read(request.session(), providerId);
            if (provider == null) {
                throw new NotFoundException();
            }
            AbstractSensinactCommand<?> command = dtoToDelete(request);
            if (command != null)
                gatewayThread.execute(command).getValue();
        } catch (InvocationTargetException | InterruptedException e) {
            throw new InternalServerErrorException(e);
        }
        return new ExtraUseCaseResponse<S>(true, "datastream deleted");

    }
}
