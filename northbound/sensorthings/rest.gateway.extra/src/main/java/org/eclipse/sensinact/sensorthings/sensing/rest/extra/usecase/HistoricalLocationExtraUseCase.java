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
import org.eclipse.sensinact.core.command.AbstractTwinCommand;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.ext.Providers;

/**
 * UseCase that manage the create, update, delete use case for sensorthing Thing
 */
public class HistoricalLocationExtraUseCase
        extends AbstractExtraUseCaseDtoDelete<HistoricalLocation, ProviderSnapshot> {

    public HistoricalLocationExtraUseCase(Providers providers) {
        super(providers);
    }

    public ExtraUseCaseResponse<ProviderSnapshot> create(ExtraUseCaseRequest<HistoricalLocation> request) {

        return new ExtraUseCaseResponse<ProviderSnapshot>(false, "cant create historical location");

    }

    public ExtraUseCaseResponse<ProviderSnapshot> update(ExtraUseCaseRequest<HistoricalLocation> request) {

        List<SensorThingsUpdate> listDtoModels = dtosToCreateUpdate(request);

        // update/create provider
        try {
            dataUpdate.pushUpdate(listDtoModels).getValue();

        } catch (InvocationTargetException | InterruptedException e) {
            throw new InternalServerErrorException(e);

        }

        ProviderSnapshot snapshot = providerUseCase.read(request.session(), request.id());
        if (snapshot != null) {
            return new ExtraUseCaseResponse<ProviderSnapshot>(request.id(), snapshot);
        }
        return new ExtraUseCaseResponse<ProviderSnapshot>(false, "not implemented");

    }

    @Override
    public AbstractSensinactCommand<?> dtoToDelete(ExtraUseCaseRequest<HistoricalLocation> request) {
        // delete value location for the thing

        return new AbstractTwinCommand<Void>() {

            @Override
            protected Promise<Void> call(SensinactDigitalTwin twin, PromiseFactory pf) {
                String thingId = DtoMapperSimple.extractFirstIdSegment(request.id());

                SensinactProvider sp = twin.getProvider(thingId);
                SensinactResource resource = sp.getResource(DtoMapperSimple.SERVICE_ADMIN, "location");

                return resource.setValue(null);
            }

        };

    }

    @Override
    public List<SensorThingsUpdate> dtosToCreateUpdate(ExtraUseCaseRequest<HistoricalLocation> request) {
        // do nothing
        return List.of();
    }

}
