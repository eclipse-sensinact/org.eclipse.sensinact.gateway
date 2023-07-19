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
package org.eclipse.sensinact.northbound.filters.ldap.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.push.PrototypePush;
import org.eclipse.sensinact.core.push.dto.BulkGenericDto;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.northbound.filters.api.FilterCommandHelper;
import org.eclipse.sensinact.northbound.filters.api.IFilterHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.service.ServiceExtension;

@ExtendWith(ServiceExtension.class)
public class LdapComponentTest {

    @InjectService
    PrototypePush push;

    @InjectService
    GatewayThread thread;

    @InjectService
    IFilterHandler filterHandler;

    private GenericDto makeRc(final String model, final String provider, final String service, final String resource,
            final Object value) {
        GenericDto dto = new GenericDto();
        dto.model = model;
        dto.provider = provider;
        dto.service = service;
        dto.resource = resource;
        dto.value = value;
        dto.type = value != null ? value.getClass() : null;
        dto.timestamp = Instant.now();
        return dto;
    }

    @BeforeEach
    void setup() throws Exception {
        BulkGenericDto dtos = new BulkGenericDto();
        dtos.dtos = new ArrayList<>();
        dtos.dtos.add(makeRc("ldap-test-component", "ldap-test-component", "X", "X", 4));
        dtos.dtos.add(makeRc("ldap-test-component", "ldap-test-component-2", "X", "X", 5));
        push.pushUpdate(dtos).getValue();
    }

    @Test
    void testComponent() throws Exception {
        ICriterion filter = filterHandler.parseFilter("ldap", "(PROVIDER=ldap-test-component)");
        assertNotNull(filter);

        Collection<ProviderSnapshot> providers = FilterCommandHelper.executeFilter(thread, filter);
        assertEquals(1, providers.size());
        assertEquals("ldap-test-component", providers.iterator().next().getName());
    }
}
