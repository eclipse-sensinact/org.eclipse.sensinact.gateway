/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.prototype.impl;

import org.eclipse.sensinact.prototype.command.SensinactResource;
import org.eclipse.sensinact.prototype.command.impl.AbstractInternalSensinactCommand;
import org.eclipse.sensinact.prototype.command.impl.SensinactModelImpl;
import org.eclipse.sensinact.prototype.dto.impl.DataUpdateDto;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

public class SetValueCommand extends AbstractInternalSensinactCommand<Void> {

    private final DataUpdateDto dataUpdateDto;

    public SetValueCommand(DataUpdateDto dataUpdateDto) {
        this.dataUpdateDto = dataUpdateDto;
    }

    @Override
    protected Promise<Void> call(SensinactModelImpl model, PromiseFactory promiseFactory) {

        SensinactResource resource = model.getOrCreateResource(dataUpdateDto.model, dataUpdateDto.provider,
                dataUpdateDto.service, dataUpdateDto.resource, dataUpdateDto.type);

        return resource.setValue(dataUpdateDto.data, dataUpdateDto.timestamp);
    }

}
