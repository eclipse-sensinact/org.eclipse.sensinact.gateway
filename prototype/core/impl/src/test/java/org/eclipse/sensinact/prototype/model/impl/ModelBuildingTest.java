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
*   Data In Motion - initial API and implementation
*   Kentyou - fixes and updates to start basic testing
**********************************************************************/
package org.eclipse.sensinact.prototype.model.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.sensinact.model.core.SensiNactPackage;
import org.eclipse.sensinact.prototype.emf.util.EMFTestUtil;
import org.eclipse.sensinact.prototype.model.Model;
import org.eclipse.sensinact.prototype.model.Resource;
import org.eclipse.sensinact.prototype.model.ResourceType;
import org.eclipse.sensinact.prototype.model.Service;
import org.eclipse.sensinact.prototype.model.nexus.impl.ModelNexus;
import org.eclipse.sensinact.prototype.notification.NotificationAccumulator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 *
 * @author Juergen Albert
 * @since 10 Oct 2022
 */
@ExtendWith(MockitoExtension.class)
public class ModelBuildingTest {

    @Mock
    NotificationAccumulator accumulator;

    private ResourceSet resourceSet;

    private ModelNexus nexus;

    private SensinactModelManagerImpl manager;

    @BeforeEach
    void start() {
        resourceSet = EMFTestUtil.createResourceSet();
        nexus = new ModelNexus(resourceSet, SensiNactPackage.eINSTANCE, () -> accumulator, null);
        manager = new SensinactModelManagerImpl(accumulator, nexus);
    }

    @Nested
    public class ModelTest {

        private static final String TEST_MODEL = "testmodel";
        private static final String TEST_SERVICE = "testservice";
        private static final String TEST_RESOURCE = "testValue";

        @Test
        void testEmptyModel() {
            Model model = manager.createModel(TEST_MODEL).build();
            assertEquals(TEST_MODEL, model.getName());
            assertEquals(1, model.getServices().size());
            assertTrue(model.getServices().containsKey("admin"));
            Service admin = model.getServices().get("admin");
            assertEquals("admin", admin.getName());
            assertFalse(admin.getResources().isEmpty());
        }

        @Test
        void basicResource() {
            Model model = manager.createModel(TEST_MODEL).withService(TEST_SERVICE).withResource(TEST_RESOURCE)
                    .withType(Integer.class).build().build().build();

            assertTrue(model.getServices().containsKey(TEST_SERVICE));

            Service service = model.getServices().get(TEST_SERVICE);

            assertEquals(TEST_SERVICE, service.getName());
            assertTrue(service.getResources().containsKey(TEST_RESOURCE));

            Resource resource = service.getResources().get(TEST_RESOURCE);

            assertEquals(TEST_RESOURCE, resource.getName());
            assertEquals(Integer.class, resource.getType());
            assertEquals(ResourceType.SENSOR, resource.getResourceType());
        }

        @Test
        void actionResource() {
            List<Entry<String, Class<?>>> parameters = List.of(new SimpleEntry<>("foo", Double.class),
                    new SimpleEntry<>("bar", Long.class));
            Model model = manager.createModel(TEST_MODEL).withService(TEST_SERVICE).withResource(TEST_RESOURCE)
                    .withType(Integer.class).withAction(parameters).build().build().build();

            assertTrue(model.getServices().containsKey(TEST_SERVICE));

            Service service = model.getServices().get(TEST_SERVICE);

            assertEquals(TEST_SERVICE, service.getName());
            assertTrue(service.getResources().containsKey(TEST_RESOURCE));

            Resource resource = service.getResources().get(TEST_RESOURCE);

            assertEquals(TEST_RESOURCE, resource.getName());
            assertEquals(Integer.class, resource.getType());
            assertEquals(ResourceType.ACTION, resource.getResourceType());
            assertEquals(parameters, resource.getArguments());
        }

    }
}
