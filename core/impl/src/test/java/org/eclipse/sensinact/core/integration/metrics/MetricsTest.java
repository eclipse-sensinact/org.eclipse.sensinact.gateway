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
package org.eclipse.sensinact.core.integration.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.sensinact.core.metrics.IMetricsGauge;
import org.eclipse.sensinact.core.metrics.IMetricsListener;
import org.eclipse.sensinact.core.metrics.IMetricsManager;
import org.eclipse.sensinact.core.metrics.IMetricsMultiGauge;
import org.eclipse.sensinact.core.push.dto.BulkGenericDto;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.config.WithConfiguration;
import org.osgi.test.junit5.context.BundleContextExtension;

/**
 * Tests the metrics service
 */
public class MetricsTest {

    private static final String GAUGE_NAME = "test.gauge";

    private static final String GAUGES_1 = "gauges-1";
    private static final String GAUGES_2 = "gauges-2";
    private static final String[] GAUGES_NAMES = new String[] { GAUGES_1, GAUGES_2 };

    /**
     * Blocking queue to wait for events
     */
    final BlockingQueue<BulkGenericDto> queue = new ArrayBlockingQueue<>(4);

    /**
     * Flag to indicate if a gauge callback has been called
     */
    final AtomicBoolean gaugeCalled = new AtomicBoolean();

    /**
     * Flags to indicate which gauge callback were called
     */
    final Map<String, Boolean> gaugesCalled = new ConcurrentHashMap<>();

    /**
     * Test gauge class
     */
    public class TestGauge implements IMetricsGauge, IMetricsMultiGauge {
        @Override
        public Object gauge() {
            gaugeCalled.set(true);
            return 42;
        }

        @Override
        public Object gauge(String name) {
            gaugesCalled.put(name, true);
            switch (name) {
            case GAUGES_1:
                return 1;

            case GAUGES_2:
                return 2L;

            default:
                return null;
            }
        }
    }

    /**
     * Test listener
     */
    public class TestListener implements IMetricsListener {
        @Override
        public void onMetricsReport(BulkGenericDto dto) {
            queue.offer(dto);
        }
    }

    /**
     * Registers a listener and gauges
     */
    @BeforeEach
    void setUp(@InjectBundleContext BundleContext context) throws Exception {
        context.registerService(IMetricsListener.class, new TestListener(), null);

        TestGauge gauge = new TestGauge();
        context.registerService(IMetricsGauge.class, gauge,
                new Hashtable<>(Map.of(IMetricsGauge.NAME, GAUGE_NAME)));
        context.registerService(IMetricsMultiGauge.class, gauge,
                new Hashtable<>(Map.of(IMetricsMultiGauge.NAMES, GAUGES_NAMES)));
    }

    /**
     * Registered services will be cleaned up by the {@link BundleContextExtension}.
     * instance fields will be cleared out by the standard per-test instance lifecycle
     * @param metrics
     */
    @AfterEach
    void cleanUp(@InjectService IMetricsManager metrics) {
        metrics.clear();
    }

    @Test
    @WithConfiguration(pid = "sensinact.metrics", location = "?", properties = {
            @Property(key = "enabled", value = "false"), @Property(key = "metrics.rate", value = "1") })
    void testDisabled(@InjectService(filter = "(enabled=false)") IMetricsManager metrics) throws Exception {
        // At this point, listener and gauges should be registered
        metrics.getCounter("toto").inc();

        assertNull(queue.poll(2, TimeUnit.SECONDS), "Listener was notified.");
        assertFalse(gaugeCalled.get(), "Gauge was called");
        assertTrue(gaugesCalled.isEmpty(), "Multigauge was called");
    }

    @Test
    @WithConfiguration(pid = "sensinact.metrics", location = "?", properties = {
            @Property(key = "enabled", value = "true"), @Property(key = "metrics.rate", value = "1") })
    void testEnabled(@InjectService(filter = "(enabled=true)") IMetricsManager metrics) throws Exception {
        metrics.getCounter("toto").inc();

        // Wait for a bit
        final BulkGenericDto bulk = queue.poll(5, TimeUnit.SECONDS);
        assertNotNull(bulk, "Listener was not notified");
        assertTrue(gaugeCalled.get(), "Gauge was not called");
        assertFalse(gaugesCalled.isEmpty(), "Multigauge was not called");

        // Check metrics values
        final Map<String, GenericDto> dtos = new HashMap<>();
        for (GenericDto dto : bulk.dtos) {
            dtos.put(dto.resource, dto);
        }

        // Counter
        GenericDto dto = dtos.get("toto");
        assertNotNull(dto, "Resource not found");
        assertEquals(dto.model, "sensiNact-metrics");
        assertEquals(dto.provider, "sensiNact-metrics");
        assertEquals(dto.service, "metrics");
        assertEquals(dto.type, Long.class);
        assertEquals(dto.value, 1L);

        // Gauge: test.gauge becomes service=test, resource=gauge
        dto = dtos.get("gauge");
        assertNotNull(dto, "Resource not found");
        assertEquals(dto.model, "sensiNact-metrics");
        assertEquals(dto.provider, "sensiNact-metrics");
        assertEquals(dto.service, "test");
        assertEquals(dto.type, Integer.class);
        assertEquals(dto.value, 42);

        // Multigauge
        dto = dtos.get(GAUGES_1);
        assertNotNull(dto, "Resource not found");
        assertEquals(dto.model, "sensiNact-metrics");
        assertEquals(dto.provider, "sensiNact-metrics");
        assertEquals(dto.service, "metrics");
        assertEquals(dto.type, Integer.class);
        assertEquals(dto.value, 1);

        dto = dtos.get(GAUGES_2);
        assertNotNull(dto, "Resource not found");
        assertEquals(dto.model, "sensiNact-metrics");
        assertEquals(dto.provider, "sensiNact-metrics");
        assertEquals(dto.service, "metrics");
        assertEquals(dto.type, Long.class);
        assertEquals(dto.value, 2L);
    }
}
