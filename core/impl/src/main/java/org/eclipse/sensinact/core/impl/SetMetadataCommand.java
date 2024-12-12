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
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.sensinact.core.annotation.dto.DuplicateAction;
import org.eclipse.sensinact.core.annotation.dto.NullAction;
import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.push.DataUpdateException;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.core.dto.impl.MetadataUpdateDto;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetMetadataCommand extends AbstractSensinactCommand<Void> {

    private static final Logger LOG = LoggerFactory.getLogger(SetValueCommand.class);

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

        Stream<Promise<Void>> stream;
        if (metadataUpdateDto.removeMissingValues) {
            stream = Stream.of(promiseFactory.failed(new UnsupportedOperationException("Not yet implemented")));
        } else {
            stream = metadataUpdateDto.metadata.entrySet().stream()
                    .map(e -> updateMetdataValue(resource, e.getKey(), e.getValue(), metadataUpdateDto, promiseFactory));
        }

        List<Promise<Void>> updates = stream.map(p -> p.recoverWith(pf -> {
                            return promiseFactory.failed(new DataUpdateException(metadataUpdateDto.modelPackageUri,
                                    metadataUpdateDto.model, metadataUpdateDto.provider, metadataUpdateDto.service,
                                    metadataUpdateDto.resource, metadataUpdateDto.originalDto, pf.getFailure()));
                        }))
                .collect(toList());

        return promiseFactory.all(updates).map(x -> null);
    }

    private Promise<Void> updateMetdataValue(SensinactResource resource, String key, Object value, MetadataUpdateDto metadataUpdateDto,
            PromiseFactory promiseFactory) {

        try {
            Function<TimedValue<Object>, Promise<Void>> cachedValueAction = null;
            if(value == null && metadataUpdateDto.actionOnNull == NullAction.UPDATE_IF_PRESENT) {
                cachedValueAction = v -> v.getTimestamp() == null ? promiseFactory.resolved(null) :
                    resource.setMetadataValue(key, value, metadataUpdateDto.timestamp);
            } else if(metadataUpdateDto.actionOnDuplicate == DuplicateAction.UPDATE_IF_DIFFERENT) {
                cachedValueAction = v -> {
                    if(v.getValue() == null) {
                        return value == null ? promiseFactory.resolved(null) :
                            resource.setMetadataValue(key, value, metadataUpdateDto.timestamp);
                    } else {
                        return v.getValue().equals(value) ? promiseFactory.resolved(null) :
                            resource.setMetadataValue(key, value, metadataUpdateDto.timestamp);
                    }
                };
            }

            if(cachedValueAction != null) {
                Promise<TimedValue<Object>> p = resource.getMetadataValue(key).timeout(0);
                try {
                    Throwable t = p.getFailure();
                    if(t != null) {
                        LOG.error("Unable to retrieve cached value for {}/{}/{}", metadataUpdateDto.provider,
                                metadataUpdateDto.service, metadataUpdateDto.resource, t);
                        return promiseFactory.failed(t);
                    } else {
                        return cachedValueAction.apply(p.getValue());
                    }
                } catch (Exception e) {
                    return promiseFactory.failed(e);
                }
            } else {
                return resource.setMetadataValue(key, value, metadataUpdateDto.timestamp);
            }
        } catch (Exception e) {
            LOG.error("An unexpected error ocurred setting metadata for {}/{}/{}", metadataUpdateDto.provider,
                    metadataUpdateDto.service, metadataUpdateDto.resource, e);
            return promiseFactory.failed(e);
        }
    }

}
