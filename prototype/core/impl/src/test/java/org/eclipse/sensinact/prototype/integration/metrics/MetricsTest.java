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
package org.eclipse.sensinact.prototype.integration.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.sensinact.core.metrics.IMetricsManager;
import org.eclipse.sensinact.core.push.dto.BulkGenericDto;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.config.WithConfiguration;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 *
 */
@ExtendWith({ ServiceExtension.class, ConfigurationExtension.class })
public class MetricsTest {

    /**
     * Tested service
     */
    @InjectService
    IMetricsManager metrics;

    @AfterEach
    void cleanUp() {
        metrics.clear();
    }

    @Test
    @WithConfiguration(pid = "sensinact.metrics", location = "?", properties = {
            @Property(key = "enabled", value = "false"), @Property(key = "metrics.rate", value = "1") })
    void testDisabled() throws Exception {
        try {
            final AtomicBoolean listenerCalled = new AtomicBoolean();
            final AtomicBoolean gaugeCalled = new AtomicBoolean();

            // Register to metrics
            metrics.registerListener((dto) -> listenerCalled.set(true));
            metrics.registerGauge("sample", () -> {
                gaugeCalled.set(true);
                return 42;
            });
            metrics.getCounter("toto").inc();

            // Wait for 1.5 seconds (configured as 1 second update rate)
            Thread.sleep(1500);

            assertFalse(listenerCalled.get(), "Listener was notified");
            assertFalse(gaugeCalled.get(), "Gauge was called");
        } finally {
            metrics.unregisterGauge("sample");
        }
    }

    @Test
    @WithConfiguration(pid = "sensinact.metrics", location = "?", properties = {
            @Property(key = "enabled", value = "true"), @Property(key = "metrics.rate", value = "1") })
    void testEnabled() throws Exception {
        try {
            final BlockingQueue<BulkGenericDto> queue = new ArrayBlockingQueue<>(4);
            final AtomicBoolean gaugeCalled = new AtomicBoolean();

            // Register to metrics
            metrics.registerListener((d) -> queue.offer(d));
            metrics.registerGauge("sample", () -> {
                gaugeCalled.set(true);
                return 42;
            });
            metrics.getCounter("toto").inc();

            // Wait for a bit
            final BulkGenericDto bulk = queue.poll(5, TimeUnit.SECONDS);
            assertNotNull(bulk, "Listener was not notified");
            assertTrue(gaugeCalled.get(), "Gauge was not called");

            final Map<String, GenericDto> dtos = new HashMap<>();
            for (GenericDto dto : bulk.dtos) {
                dtos.put(dto.resource, dto);
            }

            GenericDto dto = dtos.get("toto");
            assertNotNull(dto, "Resource not found");
            assertEquals(dto.model, "sensiNact-metrics");
            assertEquals(dto.provider, "sensiNact-metrics");
            assertEquals(dto.service, "metrics");
            assertEquals(dto.type, Long.class);
            assertEquals(dto.value, 1L);

            dto = dtos.get("sample");
            assertNotNull(dto, "Resource not found");
            assertEquals(dto.model, "sensiNact-metrics");
            assertEquals(dto.provider, "sensiNact-metrics");
            assertEquals(dto.service, "metrics");
            assertEquals(dto.type, Integer.class);
            assertEquals(dto.value, 42);
        } finally {
            metrics.unregisterGauge("sample");
        }
    }
}
