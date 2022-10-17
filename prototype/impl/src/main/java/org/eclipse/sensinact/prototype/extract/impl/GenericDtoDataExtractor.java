/*********************************************************************
* Copyright (c) 2022 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.prototype.extract.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.sensinact.prototype.dto.impl.AbstractUpdateDto;
import org.eclipse.sensinact.prototype.dto.impl.DataUpdateDto;
import org.eclipse.sensinact.prototype.dto.impl.MetadataUpdateDto;
import org.eclipse.sensinact.prototype.generic.dto.GenericDto;

public class GenericDtoDataExtractor implements DataExtractor {

    @Override
    public List<? extends AbstractUpdateDto> getUpdates(Object update) {

        GenericDto dto = check(update);

        List<AbstractUpdateDto> list = new ArrayList<>();

        Instant instant = null;

        if (dto.value != null) {
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
        dud.model = dto.model;
        dud.provider = dto.provider;
        dud.service = dto.service;
        dud.resource = dto.resource;
        dud.timestamp = instant == null ? instant = Instant.now() : instant;
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
