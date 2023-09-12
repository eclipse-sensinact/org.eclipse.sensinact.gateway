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
package org.eclipse.sensinact.core.integration.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;

import org.eclipse.sensinact.core.command.AbstractTwinCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.push.dto.BulkGenericDto;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.service.ServiceExtension;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

@ExtendWith(ServiceExtension.class)
public class FilterTest {

    @InjectService
    DataUpdate push;

    @InjectService
    GatewayThread thread;

    @Test
    void testBasicQuery() throws Exception {
        BulkGenericDto bulk = new BulkGenericDto();
        bulk.dtos = new ArrayList<>();

        for (int svcIdx = 0; svcIdx < 4; svcIdx++) {
            for (int modelIdx = 0; modelIdx < 2; modelIdx++) {
                GenericDto dto = new GenericDto();
                dto.model = "model_" + modelIdx;
                dto.provider = "provider_" + modelIdx;
                dto.service = "service_" + svcIdx;
                dto.resource = "resource";
                dto.value = svcIdx + 1;
                dto.type = Integer.class;
                bulk.dtos.add(dto);
            }
        }
        push.pushUpdate(bulk).getValue();

        Predicate<ProviderSnapshot> providerFilter = p -> "model_1".equals(p.getModelName());
        Predicate<ServiceSnapshot> svcFilter = s -> "service_1".equals(s.getName());
        Predicate<ResourceSnapshot> rcFilter = r -> "resource".equals(r.getName());

        Collection<ProviderSnapshot> providers = thread
                .execute(new AbstractTwinCommand<Collection<ProviderSnapshot>>() {
                    protected Promise<Collection<ProviderSnapshot>> call(SensinactDigitalTwin model,
                            PromiseFactory pf) {
                        return pf.resolved(model.filteredSnapshot(null, providerFilter, svcFilter, rcFilter));
                    };
                }).getValue();

        assertEquals(1, providers.size());
        final ProviderSnapshot provider = providers.iterator().next();
        assertEquals("model_1", provider.getModelName());
        assertEquals("provider_1", provider.getName());
        // We should have all 4 services + admin
        assertEquals(5, provider.getServices().size());

        final ServiceSnapshot service = provider.getServices().stream().filter(s -> "service_1".equals(s.getName()))
                .findFirst().get();
        assertEquals(1, service.getResources().size());

        final ResourceSnapshot resource = service.getResources().get(0);
        assertEquals("resource", resource.getName());
        assertEquals(2, resource.getValue().getValue());
        assertNotNull(resource.getValue().getTimestamp());
    }
}
