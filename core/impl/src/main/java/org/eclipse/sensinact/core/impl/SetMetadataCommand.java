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
package org.eclipse.sensinact.core.impl;

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.core.dto.impl.MetadataUpdateDto;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

public class SetMetadataCommand extends AbstractSensinactCommand<Void> {

    private final MetadataUpdateDto metadataUpdateDto;

    public SetMetadataCommand(MetadataUpdateDto metadataUpdateDto) {
        this.metadataUpdateDto = metadataUpdateDto;
    }

    @Override
    protected Promise<Void> call(SensinactDigitalTwin twin, SensinactModelManager modelMgr,
            PromiseFactory promiseFactory) {

        SensinactResource resource = metadataUpdateDto.model != null
                ? twin.getResource(metadataUpdateDto.modelPackageUri, metadataUpdateDto.model, metadataUpdateDto.provider, metadataUpdateDto.service,
                        metadataUpdateDto.resource)
                : twin.getResource(metadataUpdateDto.provider, metadataUpdateDto.service, metadataUpdateDto.resource);

        if (metadataUpdateDto.removeMissingValues) {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        List<Promise<Void>> updates = metadataUpdateDto.metadata.entrySet().stream()
                .map(e -> resource.setMetadataValue(e.getKey(), e.getValue(), metadataUpdateDto.timestamp))
                .collect(toList());

        return promiseFactory.all(updates).map(x -> null);
    }

}
