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
package org.eclipse.sensinact.core.model.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.sensinact.core.emf.util.EMFTestUtil;
import org.eclipse.sensinact.core.model.Model;
import org.eclipse.sensinact.core.model.Resource;
import org.eclipse.sensinact.core.model.ResourceType;
import org.eclipse.sensinact.core.model.Service;
import org.eclipse.sensinact.core.model.nexus.ModelNexus;
import org.eclipse.sensinact.core.notification.impl.NotificationAccumulator;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.impl.SensinactDigitalTwinImpl;
import org.eclipse.sensinact.model.core.provider.ProviderPackage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.util.promise.PromiseFactory;

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
        nexus = new ModelNexus(resourceSet, ProviderPackage.eINSTANCE, () -> accumulator);
        manager = new SensinactModelManagerImpl(nexus);
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
        void testResourcePresenceOnNewInstance() {
            manager.createModel(TEST_MODEL).withService(TEST_SERVICE).withResource(TEST_RESOURCE)
                    .withType(Integer.class).build().build().build();

            final String providerName = "foobar";
            nexus.createProviderInstance(TEST_MODEL, providerName);

            List<ProviderSnapshot> filteredSnapshot = new SensinactDigitalTwinImpl(nexus,
                    new PromiseFactory(PromiseFactory.inlineExecutor())).filteredSnapshot(null, null, null, null);

            ProviderSnapshot provider = filteredSnapshot.stream().filter(p -> p.getName().equals(providerName))
                    .findFirst().get();

            ServiceSnapshot svc = provider.getServices().stream().filter(s -> TEST_SERVICE.equals(s.getName()))
                    .findFirst().get();
            assertNotNull(svc);

            ResourceSnapshot rc = svc.getResources().stream().filter(r -> TEST_RESOURCE.equals(r.getName())).findFirst()
                    .get();
            assertNotNull(rc);
            assertNull(rc.getValue());
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

        @Test
        void testDeleteModel() {
            Model model = manager.createModel(TEST_MODEL).build();
            String packageUri = model.getPackageUri();
            String name = model.getName();

            assertTrue(nexus.getModel(packageUri, name).isPresent());

            manager.deleteModel(packageUri, name);

            assertFalse(nexus.getModel(packageUri, name).isPresent());

            assertThrows(IllegalStateException.class, () -> model.getPackageUri());
        }

        @Test
        void resourceWithDefaultMetadata() {
            Model model = manager.createModel(TEST_MODEL).withService(TEST_SERVICE).withResource(TEST_RESOURCE)
                    .withType(Integer.class).withDefaultMetadata(Map.of("foo", "bar", "foobar", 42)).build().build().build();

            Resource resource = model.getServices().get(TEST_SERVICE).getResources().get(TEST_RESOURCE);

            assertEquals(TEST_RESOURCE, resource.getName());
            assertEquals(Integer.class, resource.getType());
            assertEquals(ResourceType.SENSOR, resource.getResourceType());

            Map<String, Object> metadata = resource.getDefaultMetadata();

            assertNotNull(metadata);
            assertEquals("bar", metadata.get("foo"));
            assertEquals(42, metadata.get("foobar"));
        }

        @Test
        void actionWithDefaultMetadata() {
            List<Entry<String, Class<?>>> parameters = List.of(new SimpleEntry<>("foo", Double.class),
                    new SimpleEntry<>("bar", Long.class));
            Model model = manager.createModel(TEST_MODEL).withService(TEST_SERVICE).withResource(TEST_RESOURCE)
                    .withType(Integer.class).withAction(parameters)
                    .withDefaultMetadata(Map.of("foo", "bar", "foobar", 42)).build().build().build();

            Resource resource = model.getServices().get(TEST_SERVICE).getResources().get(TEST_RESOURCE);

            assertEquals(TEST_RESOURCE, resource.getName());
            assertEquals(Integer.class, resource.getType());
            assertEquals(ResourceType.ACTION, resource.getResourceType());
            assertEquals(parameters, resource.getArguments());

            Map<String, Object> metadata = resource.getDefaultMetadata();

            assertNotNull(metadata);
            assertEquals("bar", metadata.get("foo"));
            assertEquals(42, metadata.get("foobar"));
        }
    }
}
