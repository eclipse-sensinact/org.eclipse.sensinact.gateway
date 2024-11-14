/*********************************************************************
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Kentyou - initial implementation
 **********************************************************************/
package org.eclipse.sensinact.southbound.rules.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Objects;

import org.eclipse.sensinact.core.annotation.dto.DuplicateAction;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.push.dto.BulkGenericDto;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.southbound.rules.api.ResourceUpdater;

public class RuleResourceUpdater implements ResourceUpdater {

    private final DataUpdate update;

    public RuleResourceUpdater(DataUpdate update) {
        this.update = update;
    }

    private GenericDto toDto(String provider, String service, String resource, Object value, Instant timestamp) {
        Objects.requireNonNull(provider, "The provider must not be null");
        Objects.requireNonNull(service, "The service must not be null");
        Objects.requireNonNull(resource, "The resource must not be null");
        Objects.requireNonNull(value, "The value must not be null");
        GenericDto dto = new GenericDto();
        dto.provider = provider;
        dto.service = service;
        dto.resource = resource;
        dto.value = value;
        dto.timestamp = timestamp;
        dto.duplicateDataAction = DuplicateAction.UPDATE_ALWAYS;
        return dto;
    }

    @Override
    public void updateResource(String provider, String service, String resource, Object value) {
        updateResource(provider, service, resource, value, null);

    }

    @Override
    public void updateResource(String provider, String service, String resource, Object value, Instant timestamp) {
        update.pushUpdate(toDto(provider, service, resource, value, timestamp));
    }

    @Override
    public BatchUpdate updateBatch() {
        return new RuleBatchUpdate();
    }

    class RuleBatchUpdate implements BatchUpdate {

        private final BulkGenericDto dto = new BulkGenericDto();

        @Override
        public BatchUpdate updateResource(String provider, String service, String resource, Object value) {
            updateResource(provider, service, resource, value, null);
            return this;
        }

        @Override
        public BatchUpdate updateResource(String provider, String service, String resource, Object value,
                Instant timestamp) {
            if(dto.dtos == null) {
                dto.dtos = new ArrayList<>();
            }
            dto.dtos.add(toDto(provider, service, resource, value, timestamp));
            return this;
        }

        @Override
        public void completeBatch() {
            update.pushUpdate(dto);
        }
    }
}
