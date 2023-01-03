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
*   Data In Motion - initial API and implementation
*   Kentyou - fixes and updates to start basic testing
**********************************************************************/
package org.eclipse.sensinact.prototype.model.nexus.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.sensinact.model.core.SensiNactPackage;
import org.eclipse.sensinact.prototype.emf.util.EMFTestUtil;
import org.eclipse.sensinact.prototype.notification.NotificationAccumulator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 *
 * @author Juergen Albert
 * @since 10 Oct 2022
 */
@ExtendWith(MockitoExtension.class)
public class SubscriptionTest {

    @Mock
    NotificationAccumulator accumulator;

    private ResourceSet resourceSet;

    @BeforeEach
    void start() {
        resourceSet = EMFTestUtil.createResourceSet();
    }

    @Nested
    public class BasicEventsTest {

        private static final String TEST_PROVIDER = "testprovider";
        private static final String TEST_SERVICE = "testservice";
        private static final String TEST_SERVICE_2 = "testservice2";
        private static final String TEST_RESOURCE = "testValue";
        private static final String TEST_RESOURCE_2 = "testValue2";
        private static final String TEST_VALUE = "test";
        private static final String TEST_VALUE_2 = "test2";

        @Test
        void basicTest() {

            ModelNexus nexus = new ModelNexus(resourceSet, SensiNactPackage.eINSTANCE, () -> accumulator);

            Instant now = Instant.now();
            nexus.handleDataUpdate("TestModel", TEST_PROVIDER, TEST_SERVICE, TEST_RESOURCE, String.class, TEST_VALUE,
                    now);

            Mockito.verify(accumulator).addProvider(TEST_PROVIDER);
            Mockito.verify(accumulator).addService(TEST_PROVIDER, TEST_SERVICE);
            // TODO - this is missing
            Mockito.verify(accumulator).addResource(TEST_PROVIDER, TEST_SERVICE, TEST_RESOURCE);
            Mockito.verify(accumulator).resourceValueUpdate(TEST_PROVIDER, TEST_SERVICE, TEST_RESOURCE, null,
                    TEST_VALUE, now);
            // TODO - the value is in here, which is surprising, as is the timestamp being a
            // date
            Mockito.verify(accumulator).metadataValueUpdate(TEST_PROVIDER, TEST_SERVICE, TEST_RESOURCE, null,
                    Map.of("value", TEST_VALUE, "timestamp", now), now);

            Mockito.verifyNoMoreInteractions(accumulator);
        }

        @Test
        void basicServiceExtensionTest() {

            ModelNexus nexus = new ModelNexus(resourceSet, SensiNactPackage.eINSTANCE, () -> accumulator);

            Instant now = Instant.now();
            Instant before = now.minus(Duration.ofHours(1));

            nexus.handleDataUpdate("TestModel", TEST_PROVIDER, TEST_SERVICE, TEST_RESOURCE, String.class, TEST_VALUE,
                    before);
            nexus.handleDataUpdate("TestModel", TEST_PROVIDER, TEST_SERVICE, "testValue2", String.class, TEST_VALUE,
                    now);

            Mockito.verify(accumulator).addProvider(TEST_PROVIDER);
            Mockito.verify(accumulator).addService(TEST_PROVIDER, TEST_SERVICE);
            // TODO - these are missing
            Mockito.verify(accumulator).addResource(TEST_PROVIDER, TEST_SERVICE, TEST_RESOURCE);
            Mockito.verify(accumulator).addResource(TEST_PROVIDER, TEST_SERVICE, TEST_RESOURCE_2);
            Mockito.verify(accumulator).resourceValueUpdate(TEST_PROVIDER, TEST_SERVICE, TEST_RESOURCE, null,
                    TEST_VALUE, before);
            Mockito.verify(accumulator).resourceValueUpdate(TEST_PROVIDER, TEST_SERVICE, TEST_RESOURCE_2, null,
                    TEST_VALUE, now);

            Mockito.verify(accumulator).metadataValueUpdate(TEST_PROVIDER, TEST_SERVICE, TEST_RESOURCE, null,
                    Map.of("value", TEST_VALUE, "timestamp", before), before);
            Mockito.verify(accumulator).metadataValueUpdate(TEST_PROVIDER, TEST_SERVICE, TEST_RESOURCE_2, null,
                    Map.of("value", TEST_VALUE, "timestamp", now), now);

            Mockito.verifyNoMoreInteractions(accumulator);
        }

        @Test
        void basicSecondServiceTest() {

            ModelNexus nexus = new ModelNexus(resourceSet, SensiNactPackage.eINSTANCE, () -> accumulator);

            Instant now = Instant.now();
            Instant before = now.minus(Duration.ofHours(1));

            nexus.handleDataUpdate("TestModel", TEST_PROVIDER, TEST_SERVICE, TEST_RESOURCE, String.class, TEST_VALUE,
                    before);
            nexus.handleDataUpdate("TestModel", TEST_PROVIDER, TEST_SERVICE_2, TEST_RESOURCE_2, String.class,
                    TEST_VALUE_2, now);

            Mockito.verify(accumulator).addProvider(TEST_PROVIDER);
            Mockito.verify(accumulator).addService(TEST_PROVIDER, TEST_SERVICE);
            Mockito.verify(accumulator).addService(TEST_PROVIDER, TEST_SERVICE_2);
            // TODO - these are missing
            Mockito.verify(accumulator).addResource(TEST_PROVIDER, TEST_SERVICE, TEST_RESOURCE);
            Mockito.verify(accumulator).addResource(TEST_PROVIDER, TEST_SERVICE_2, TEST_RESOURCE_2);
            Mockito.verify(accumulator).resourceValueUpdate(TEST_PROVIDER, TEST_SERVICE, TEST_RESOURCE, null,
                    TEST_VALUE, before);
            Mockito.verify(accumulator).resourceValueUpdate(TEST_PROVIDER, TEST_SERVICE_2, TEST_RESOURCE_2, null,
                    TEST_VALUE_2, now);

            Mockito.verify(accumulator).metadataValueUpdate(TEST_PROVIDER, TEST_SERVICE, TEST_RESOURCE, null,
                    Map.of("value", TEST_VALUE, "timestamp", before), before);
            Mockito.verify(accumulator).metadataValueUpdate(TEST_PROVIDER, TEST_SERVICE_2, TEST_RESOURCE_2, null,
                    Map.of("value", TEST_VALUE_2, "timestamp", now), now);

            Mockito.verifyNoMoreInteractions(accumulator);
        }
    }
}

