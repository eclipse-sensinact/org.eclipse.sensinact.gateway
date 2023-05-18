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

import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.prototype.dto.impl.MetadataUpdateDto;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

public class SetMetadataCommand extends AbstractSensinactCommand<Void> {

    @SuppressWarnings("unused")
    private final MetadataUpdateDto metadataUpdateDto;

    public SetMetadataCommand(MetadataUpdateDto metadataUpdateDto) {
        this.metadataUpdateDto = metadataUpdateDto;
    }

    @Override
    protected Promise<Void> call(SensinactDigitalTwin twin, SensinactModelManager modelMgr,
            PromiseFactory promiseFactory) {

        // TODO set the metadata in the model

        return promiseFactory.resolved(null);
    }

}
