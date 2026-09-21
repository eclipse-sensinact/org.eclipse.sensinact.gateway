/*********************************************************************
* Copyright (c) 2026 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Data In Motion - initial API and implementation
**********************************************************************/
package org.eclipse.sensinact.gateway.southbound.history.core.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.command.ResourceCommand;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryProvider;
import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryQuery;
import org.eclipse.sensinact.gateway.southbound.history.provider.ResourcePath;
import org.eclipse.sensinact.gateway.southbound.history.provider.TimeRange;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoryIngestFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.osgi.service.cm.Configuration;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.config.InjectConfiguration;
import org.osgi.test.common.annotation.config.WithConfiguration;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

/**
 * End-to-end test of the history engine on the in-memory backend: resource
 * updates flow through the typed event bus into the storage, are queryable
 * through the {@link HistoryProvider} service and the legacy ACT facade, and
 * historization filters take effect and disappear at runtime without
 * restarting the backend.
 */
public class HistoryCoreIntegrationTest {

    private static final String PROVIDER_NAME = "inmemory-test";
    private static final Instant TS_1 = Instant.parse("2026-01-01T00:00:00Z");
    private ResourcePath path;

    @InjectService
    DataUpdate push;

    @InjectService
    GatewayThread gatewayThread;

    HistoryProvider provider;

    @BeforeEach
    void setUp(
            @InjectConfiguration(withConfig = @WithConfiguration(pid = "sensinact.history.inmemory", location = "?", properties = {
                    @Property(key = "provider", value = PROVIDER_NAME) })) Configuration storageConfig,
            @InjectService(filter = "(" + HistoryProvider.PROP_NAME + "=" + PROVIDER_NAME
                    + ")", cardinality = 0) ServiceAware<HistoryProvider> providerAware)
            throws Exception {
        provider = providerAware.waitForService(5000);
    }

    private void pushTemperature(Object value, Instant timestamp) throws Exception {
        GenericDto dto = new GenericDto();
        dto.model = "testModel";
        dto.provider = path.provider();
        dto.service = path.service();
        dto.resource = path.resource();
        dto.value = value;
        dto.type = value.getClass();
        dto.timestamp = timestamp;
        push.pushUpdate(dto).getValue();
    }

    private void awaitCount(long expected) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5000;
        long current = -1;
        while (System.currentTimeMillis() < deadline) {
            current = provider.getValueCount(path, TimeRange.ALL);
            if (current == expected) {
                return;
            }
            Thread.sleep(50);
        }
        throw new AssertionFailedError("Expected " + expected + " stored values but found " + current);
    }

    @Test
    void updatesFlowIntoHistoryAndOutThroughServiceAndFacade() throws Exception {
        path = new ResourcePath("sensorFlow", "data", "temperature");
        pushTemperature(21.5d, TS_1);
        pushTemperature(22.0d, TS_1.plusSeconds(60));
        pushTemperature(23.5d, TS_1.plusSeconds(120));
        awaitCount(3);

        List<TimedValue<?>> values = provider.getValues(HistoryQuery.builder(path).build()).values();
        assertEquals(List.of(21.5d, 22.0d, 23.5d), values.stream().map(TimedValue::getValue).toList());

        Object actResult = gatewayThread.execute(new ResourceCommand<Object>(
                "https://eclipse.org/sensinact/sensiNactHistory", "sensiNactHistory", PROVIDER_NAME, "history",
                "single") {
            @Override
            protected Promise<Object> call(SensinactResource resource, PromiseFactory promiseFactory) {
                return resource.act(Map.of("provider", path.provider(), "service", path.service(), "resource",
                        path.resource())).map(Object.class::cast);
            }
        }).getValue();

        assertTrue(actResult instanceof TimedValue<?>, "ACT facade did not return a TimedValue: " + actResult);
        assertEquals(21.5d, ((TimedValue<?>) actResult).getValue());
    }

    @Test
    void deadbandFilterSuppressesAtRuntimeAndItsRemovalNeedsNoBackendRestart(
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = "sensinact.history.filter", name = "deadband-it", location = "?", properties = {
                    @Property(key = "name", value = "deadband-it"),
                    @Property(key = "target", value = PROVIDER_NAME),
                    @Property(key = "change.mode", value = "deadband"),
                    @Property(key = "change.threshold", value = "0.5") })) Configuration filterConfig,
            @InjectService(cardinality = 0) ServiceAware<HistoryIngestFilter> filterAware) throws Exception {
        path = new ResourcePath("sensorDeadband", "data", "temperature");
        assertNotNull(filterAware.waitForService(5000), "historization filter service did not appear");

        pushTemperature(21.0d, TS_1);
        awaitCount(1);
        pushTemperature(21.2d, TS_1.plusSeconds(60));
        pushTemperature(22.0d, TS_1.plusSeconds(120));
        awaitCount(2);

        filterConfig.delete();
        long deadline = System.currentTimeMillis() + 5000;
        while (!filterAware.isEmpty() && System.currentTimeMillis() < deadline) {
            Thread.sleep(50);
        }
        assertTrue(filterAware.isEmpty(), "Filter service did not disappear after config deletion");

        pushTemperature(22.1d, TS_1.plusSeconds(180));
        awaitCount(3);

        assertEquals(List.of(21.0d, 22.0d, 22.1d),
                provider.getFirstValues(path, 10, 0).stream().map(TimedValue::getValue).toList());
    }
}
