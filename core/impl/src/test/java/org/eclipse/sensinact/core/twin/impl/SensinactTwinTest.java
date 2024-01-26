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
package org.eclipse.sensinact.core.twin.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.sensinact.core.model.ResourceType;
import org.eclipse.sensinact.core.notification.NotificationAccumulator;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.model.core.provider.ProviderPackage;
import org.eclipse.sensinact.core.command.impl.ActionHandler;
import org.eclipse.sensinact.core.emf.util.EMFTestUtil;
import org.eclipse.sensinact.core.model.impl.SensinactModelManagerImpl;
import org.eclipse.sensinact.core.model.nexus.ModelNexus;
import org.eclipse.sensinact.core.model.nexus.emf.EMFUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

/**
 *
 * @author Juergen Albert
 * @since 10 Oct 2022
 */
@ExtendWith(MockitoExtension.class)
public class SensinactTwinTest {

    private static final String TEST_MODEL = "testmodel";
    private static final String TEST_PROVIDER = "testprovider";
    private static final String TEST_SERVICE = "testservice";
    private static final String TEST_RESOURCE = "testValue";
    private static final String TEST_ACTION_RESOURCE = "testAction";

    @Mock
    NotificationAccumulator accumulator;

    @Mock
    ActionHandler actionHandler;

    private PromiseFactory promiseFactory = new PromiseFactory(PromiseFactory.inlineExecutor());

    private ResourceSet resourceSet;

    private ModelNexus nexus;

    private SensinactModelManagerImpl manager;

    private SensinactDigitalTwinImpl twinImpl;

    @BeforeEach
    void start() {
        resourceSet = EMFTestUtil.createResourceSet();
        nexus = new ModelNexus(resourceSet, ProviderPackage.eINSTANCE, () -> accumulator, actionHandler);
        manager = new SensinactModelManagerImpl(nexus);
        twinImpl = new SensinactDigitalTwinImpl(nexus, promiseFactory);

        manager.createModel(TEST_MODEL).withService(TEST_SERVICE).withResource(TEST_RESOURCE).withType(Integer.class)
                .build().withResource(TEST_ACTION_RESOURCE).withType(Double.class)
                .withAction(List.of(new SimpleEntry<>("foo", String.class), new SimpleEntry<>("bar", Instant.class)))
                .buildAll();
    }

    @Nested
    public class ProviderTests {

        @Test
        void testEmptyModelNexus() {
            assertEquals(List.of("sensiNact"),
                    twinImpl.getProviders().stream().map(SensinactProvider::getName).collect(Collectors.toList()));
        }

        @Test
        void testCreateProvider() {
            SensinactProvider provider = twinImpl.createProvider(TEST_MODEL, TEST_PROVIDER);

            assertEquals(TEST_MODEL, provider.getModelName());
            assertEquals(TEST_PROVIDER, provider.getName());
            assertEquals(Set.of(TEST_SERVICE, "admin"), provider.getServices().keySet());
            assertEquals(Set.of(TEST_RESOURCE, TEST_ACTION_RESOURCE),
                    provider.getServices().get(TEST_SERVICE).getResources().keySet());
        }

        @Test
        void basicResourceSet() throws Exception {
            twinImpl.createProvider(TEST_MODEL, TEST_PROVIDER);
            SensinactResourceImpl resource = twinImpl.getResource(TEST_PROVIDER, TEST_SERVICE, TEST_RESOURCE);

            assertEquals(Integer.class, resource.getType());
            assertEquals(ResourceType.SENSOR, resource.getResourceType());
            Promise<TimedValue<?>> value = resource.getValue();
            assertEquals(null, value.getValue().getValue());
            assertEquals(null, value.getValue().getTimestamp());

            Instant timestamp = Instant.parse("2023-02-14T15:00:00.000Z");
            resource.setValue(42, timestamp);
            value = resource.getValue();
            assertEquals(42, value.getValue().getValue());
            assertEquals(timestamp, value.getValue().getTimestamp());
        }

        @Test
        void basicActionResource() throws Exception {
            twinImpl.createProvider(TEST_MODEL, TEST_PROVIDER);
            SensinactResourceImpl resource = twinImpl.getResource(TEST_PROVIDER, TEST_SERVICE, TEST_ACTION_RESOURCE);

            assertEquals(Double.class, resource.getType());
            assertEquals(ResourceType.ACTION, resource.getResourceType());
            Promise<TimedValue<?>> value = resource.getValue();
            assertEquals(IllegalArgumentException.class, value.getFailure().getClass());

            assertEquals(List.of(new SimpleEntry<>("foo", String.class), new SimpleEntry<>("bar", Instant.class)),
                    resource.getArguments());

            Map<String, Object> arguments = Map.of("foo", "bar", "foobar", Instant.now());

            Mockito.when(actionHandler.act(EMFUtil.constructPackageUri(TEST_MODEL), TEST_MODEL, TEST_PROVIDER, TEST_SERVICE, TEST_ACTION_RESOURCE, arguments))
                    .thenReturn(promiseFactory.<Object>resolved(4.2D).delay(1000));

            Promise<Object> act = resource.act(arguments);

            assertFalse(act.isDone());
            assertEquals(4.2D, act.getValue());
        }
    }

    @Nested
    public class FilterTests {

        @Test
        void simpleEmptyFilter() {
            List<ProviderSnapshot> list = twinImpl.filteredSnapshot(null, null, null, null);
            assertEquals(1, list.size());

            twinImpl.createProvider(TEST_MODEL, TEST_PROVIDER);
            list = twinImpl.filteredSnapshot(null, null, null, null);
            assertEquals(2, list.size());
        }

        @Test
        void simpleProviderNameFilter() {
            twinImpl.createProvider(TEST_MODEL, TEST_PROVIDER);

            List<ProviderSnapshot> list = twinImpl.filteredSnapshot(null, p -> "foo".equals(p.getName()), null, null);
            assertTrue(list.isEmpty());

            list = twinImpl.filteredSnapshot(null, p -> TEST_PROVIDER.equals(p.getName()), null, null);
            assertEquals(1, list.size());
        }

        @Test
        void simpleResourceValueFilter() throws Exception {
            twinImpl.createProvider(TEST_MODEL, TEST_PROVIDER);
            twinImpl.getResource(TEST_PROVIDER, TEST_SERVICE, TEST_RESOURCE).setValue(5).getValue();

            Predicate<ResourceSnapshot> p = r -> TEST_RESOURCE.equals(r.getName());

            List<ProviderSnapshot> list = twinImpl.filteredSnapshot(null, null, null, p);
            assertEquals(1, list.size());
            assertEquals(2, list.get(0).getServices().size());
            assertEquals(5, list.get(0).getServices().get(1).getResources().get(0).getValue().getValue());
        }
    }
}
