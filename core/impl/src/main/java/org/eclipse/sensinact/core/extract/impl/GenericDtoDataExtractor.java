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
**********************************************************************/
package org.eclipse.sensinact.core.extract.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.sensinact.core.annotation.dto.NullAction;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.core.dto.impl.AbstractUpdateDto;
import org.eclipse.sensinact.core.dto.impl.DataUpdateDto;
import org.eclipse.sensinact.core.dto.impl.MetadataUpdateDto;

public class GenericDtoDataExtractor implements DataExtractor {

    @Override
    public List<? extends AbstractUpdateDto> getUpdates(Object update) {

        GenericDto dto = check(update);

        List<AbstractUpdateDto> list = new ArrayList<>();

        Instant instant = dto.timestamp == null ? Instant.now() : dto.timestamp;

        // Accept a null value if this is not a metadata update
        if (dto.value != null || dto.nullAction == NullAction.UPDATE) {
            DataUpdateDto dud = new DataUpdateDto();
            instant = copyCommonFields(dto, instant, dud);
            if (dto.type != null)
                dud.type = dto.type;
            dud.data = dto.value;
            list.add(dud);
        }

        if (dto.metadata != null) {
            MetadataUpdateDto dud = new MetadataUpdateDto();
            instant = copyCommonFields(dto, instant, dud);
            dud.metadata = dto.metadata;
            dud.removeNullValues = true;
            list.add(dud);
        }

        return list;
    }

    private Instant copyCommonFields(GenericDto dto, Instant instant, AbstractUpdateDto dud) {
        dud.modelPackageUri = dto.modelPackageUri;
        dud.model = dto.model;
        dud.provider = dto.provider;
        dud.service = dto.service;
        dud.resource = dto.resource;
        dud.timestamp = instant;
        return instant;
    }

    private GenericDto check(Object update) {
        GenericDto dto;
        try {
            dto = GenericDto.class.cast(update);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("The supplied update dto is not of the correct type to extract", e);
        }

        if (dto.provider == null) {
            throw new IllegalArgumentException("No provider is defined");
        }
        if (dto.service == null) {
            throw new IllegalArgumentException("No service is defined");
        }
        if (dto.resource == null) {
            throw new IllegalArgumentException("No resource is defined");
        }

        return dto;
    }
}
