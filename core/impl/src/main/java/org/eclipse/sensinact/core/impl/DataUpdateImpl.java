/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
*   Data In Motion - Addeds Provider Push
**********************************************************************/
package org.eclipse.sensinact.core.impl;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Stream;

import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.command.IndependentCommands;
import org.eclipse.sensinact.core.dto.impl.AbstractUpdateDto;
import org.eclipse.sensinact.core.dto.impl.DataUpdateDto;
import org.eclipse.sensinact.core.dto.impl.FailedMappingDto;
import org.eclipse.sensinact.core.dto.impl.MetadataUpdateDto;
import org.eclipse.sensinact.core.emf.dto.EMFGenericDto;
import org.eclipse.sensinact.core.extract.impl.BulkGenericDtoDataExtractor;
import org.eclipse.sensinact.core.extract.impl.CustomDtoDataExtractor;
import org.eclipse.sensinact.core.extract.impl.DataExtractor;
import org.eclipse.sensinact.core.extract.impl.EMFGenericDtoDataExtractor;
import org.eclipse.sensinact.core.extract.impl.GenericDtoDataExtractor;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.push.DataMappingException;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.push.DataUpdateException;
import org.eclipse.sensinact.core.push.FailedUpdatesException;
import org.eclipse.sensinact.core.push.dto.BulkGenericDto;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.model.core.provider.Provider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.promise.FailedPromisesException;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DataUpdateImpl implements DataUpdate {

    private static final Logger LOG = LoggerFactory.getLogger(DataUpdateImpl.class);

    @Reference
    GatewayThread thread;

    /**
     * We use a weak map so we don't keep classloaders for old bundles
     */
    private final Map<Class<?>, DataExtractor> cachedExtractors = new WeakHashMap<>();

    @Override
    public Promise<?> pushUpdate(Object o) {
        List<AbstractSensinactCommand<?>> commands = toStreamOfCommands(o).collect(toList());
        IndependentCommands<?> multiCommand = new IndependentCommands<>(commands);
        return thread.execute(multiCommand).recoverWith(p -> thread.getPromiseFactory()
                .failed(new FailedUpdatesException(toStreamOfDataUpdateFailures(p.getFailure()))));
    }

    private Stream<DataUpdateException> toStreamOfDataUpdateFailures(Throwable t) {
        if (t instanceof DataUpdateException) {
            return Stream.of((DataUpdateException) t);
        } else if (t instanceof FailedUpdatesException) {
            return ((FailedUpdatesException) t).getFailedUpdates().stream();
        } else if (t instanceof FailedPromisesException) {
            return ((FailedPromisesException) t).getFailedPromises().stream().flatMap(p -> {
                try {
                    return toStreamOfDataUpdateFailures(p.getFailure());
                } catch (InterruptedException e) {
                    // This should never happen
                    LOG.error("An InterruptedException occurred getting failures from completed promises", e);
                    return Stream.empty();
                }
            });
        } else {
            LOG.error("An unexpected exception type occurred getting failures from data updates", t);
            return Stream.empty();
        }
    }

    private Stream<AbstractSensinactCommand<Void>> toStreamOfCommands(Object o) {
        if (o instanceof Provider) {
            return Stream.of(new SaveProviderCommand((Provider) o));
        }

        if (o instanceof List) {
            List<Object> objects = (List) o;
            return objects.stream().flatMap(item -> toStreamOfCommands(item));
        }

        DataExtractor extractor;

        Class<?> updateClazz = o.getClass();

        synchronized (cachedExtractors) {
            extractor = cachedExtractors.computeIfAbsent(updateClazz, this::createDataExtractor);
        }

        List<? extends AbstractUpdateDto> updates = extractor.getUpdates(o);

        return updates.stream().map(this::toCommand);
    }

    private DataExtractor createDataExtractor(Class<?> clazz) {
        if (clazz == GenericDto.class) {
            return new GenericDtoDataExtractor();
        } else if (clazz == EMFGenericDto.class) {
            return new EMFGenericDtoDataExtractor();
        } else if (clazz == BulkGenericDto.class) {
            return new BulkGenericDtoDataExtractor();
        } else {
            return new CustomDtoDataExtractor(clazz);
        }
    }

    private AbstractSensinactCommand<Void> toCommand(AbstractUpdateDto dto) {
        if (dto instanceof DataUpdateDto) {
            return new SetValueCommand((DataUpdateDto) dto);
        } else if (dto instanceof MetadataUpdateDto) {
            return new SetMetadataCommand((MetadataUpdateDto) dto);
        } else if (dto instanceof FailedMappingDto) {
            return new FailureCommand((FailedMappingDto) dto);
        } else {
            throw new IllegalArgumentException("Unknown dto type " + dto.getClass().toString());
        }
    }

    static class FailureCommand extends AbstractSensinactCommand<Void> {

        private final FailedMappingDto dto;

        public FailureCommand(FailedMappingDto dto) {
            this.dto = dto;
        }

        @Override
        protected Promise<Void> call(SensinactDigitalTwin twin, SensinactModelManager modelMgr,
                PromiseFactory promiseFactory) {
            return promiseFactory.failed(new DataMappingException(dto.modelPackageUri, dto.model, dto.provider,
                    dto.service, dto.resource, dto.originalDto, dto.mappingFailure));
        }

    }
}
