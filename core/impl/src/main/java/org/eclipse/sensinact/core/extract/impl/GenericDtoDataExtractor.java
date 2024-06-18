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
import org.eclipse.sensinact.core.dto.impl.AbstractUpdateDto;
import org.eclipse.sensinact.core.dto.impl.DataUpdateDto;
import org.eclipse.sensinact.core.dto.impl.FailedMappingDto;
import org.eclipse.sensinact.core.dto.impl.MetadataUpdateDto;
import org.eclipse.sensinact.core.push.dto.GenericDto;

public class GenericDtoDataExtractor implements DataExtractor {

    @Override
    public List<? extends AbstractUpdateDto> getUpdates(Object update) {

        GenericDto dto;

        try {
            dto = GenericDto.class.cast(update);
        } catch (ClassCastException e) {
            FailedMappingDto fmd = getFailedMappingDto(update,
                    new IllegalArgumentException("The supplied update dto is not of the correct type to extract", e));
            fmd.timestamp = Instant.now();
            return List.of(fmd);
        }

        Instant instant = dto.timestamp == null ? Instant.now() : dto.timestamp;

        if (dto.provider == null) {
            return List.of(copyCommonFields(dto, instant,
                    getFailedMappingDto(dto, new IllegalArgumentException("No provider is defined"))));
        }
        if (dto.service == null) {
            return List.of(copyCommonFields(dto, instant,
                    getFailedMappingDto(dto, new IllegalArgumentException("No service is defined"))));
        }
        if (dto.resource == null) {
            return List.of(copyCommonFields(dto, instant,
                    getFailedMappingDto(dto, new IllegalArgumentException("No resource is defined"))));
        }

        List<AbstractUpdateDto> list = new ArrayList<>();

        // Accept a null value if this is not a pure metadata update
        if (dto.value != null || dto.nullAction != NullAction.IGNORE) {
            DataUpdateDto dud = copyCommonFields(dto, instant, new DataUpdateDto());
            if (dto.type != null)
                dud.type = dto.type;
            dud.data = dto.value;
            list.add(dud);
        }

        if (dto.metadata != null) {
            MetadataUpdateDto dud = copyCommonFields(dto, instant, new MetadataUpdateDto());
            dud.metadata = dto.metadata;
            dud.removeNullValues = true;
            list.add(dud);
        }

        return list;
    }

    private FailedMappingDto getFailedMappingDto(Object originalDto, Throwable mappingFailure) {
        FailedMappingDto fmd = new FailedMappingDto();
        fmd.mappingFailure = mappingFailure;
        fmd.originalDto = originalDto;
        return fmd;
    }

    private <T extends AbstractUpdateDto> T copyCommonFields(GenericDto dto, Instant instant, T dud) {
        dud.modelPackageUri = dto.modelPackageUri;
        dud.model = dto.model;
        dud.provider = dto.provider;
        dud.service = dto.service;
        dud.resource = dto.resource;
        dud.timestamp = instant;
        dud.actionOnNull = dto.nullAction;
        dud.originalDto = dto;
        return dud;
    }

}
