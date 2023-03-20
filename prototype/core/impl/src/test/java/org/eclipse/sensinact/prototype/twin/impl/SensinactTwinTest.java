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
package org.eclipse.sensinact.prototype.twin.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.Instant;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.sensinact.model.core.provider.ProviderPackage;
import org.eclipse.sensinact.prototype.command.impl.ActionHandler;
import org.eclipse.sensinact.prototype.emf.util.EMFTestUtil;
import org.eclipse.sensinact.prototype.model.ResourceType;
import org.eclipse.sensinact.prototype.model.impl.SensinactModelManagerImpl;
import org.eclipse.sensinact.prototype.model.nexus.ModelNexus;
import org.eclipse.sensinact.prototype.notification.NotificationAccumulator;
import org.eclipse.sensinact.prototype.twin.SensinactProvider;
import org.eclipse.sensinact.prototype.twin.TimedValue;
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

            Mockito.when(actionHandler.act(TEST_MODEL, TEST_PROVIDER, TEST_SERVICE, TEST_ACTION_RESOURCE, arguments))
                    .thenReturn(promiseFactory.<Object>resolved(4.2D).delay(1000));

            Promise<Object> act = resource.act(arguments);

            assertFalse(act.isDone());
            assertEquals(4.2D, act.getValue());
        }

    }
}
