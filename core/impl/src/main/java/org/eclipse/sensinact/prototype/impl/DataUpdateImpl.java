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
package org.eclipse.sensinact.prototype.impl;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.command.IndependentCommands;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.push.dto.BulkGenericDto;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.model.core.provider.Provider;
import org.eclipse.sensinact.prototype.dto.impl.AbstractUpdateDto;
import org.eclipse.sensinact.prototype.dto.impl.DataUpdateDto;
import org.eclipse.sensinact.prototype.dto.impl.MetadataUpdateDto;
import org.eclipse.sensinact.prototype.extract.impl.BulkGenericDtoDataExtractor;
import org.eclipse.sensinact.prototype.extract.impl.CustomDtoDataExtractor;
import org.eclipse.sensinact.prototype.extract.impl.DataExtractor;
import org.eclipse.sensinact.prototype.extract.impl.GenericDtoDataExtractor;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.promise.Promise;

@Component
public class DataUpdateImpl implements DataUpdate {
    // TODO wrap this in a more pleasant type?
    @Reference
    GatewayThread thread;

    /**
     * We use a weak map so we don't keep classloaders for old bundles
     */
    private final Map<Class<?>, DataExtractor> cachedExtractors = new WeakHashMap<>();

    @Override
    public Promise<?> pushUpdate(Object o) {

        if (o instanceof Provider) {
            return thread.execute(new SaveProviderCommand((Provider) o));
        }

        DataExtractor extractor;

        Class<?> updateClazz = o.getClass();

        synchronized (cachedExtractors) {
            extractor = cachedExtractors.computeIfAbsent(updateClazz, this::createDataExtractor);
        }

        List<? extends AbstractUpdateDto> updates = extractor.getUpdates(o);

        return thread.execute(new IndependentCommands<>(updates.stream().map(this::toCommand).collect(toList())));
    }

    private DataExtractor createDataExtractor(Class<?> clazz) {
        if (clazz == GenericDto.class) {
            return new GenericDtoDataExtractor();
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
        } else {
            throw new IllegalArgumentException("Unknown dto type " + dto.getClass().toString());
        }
    }

}
