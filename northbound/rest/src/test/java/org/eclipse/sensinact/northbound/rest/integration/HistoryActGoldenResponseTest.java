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
package org.eclipse.sensinact.northbound.rest.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.core.annotation.verb.ACT;
import org.eclipse.sensinact.core.annotation.verb.ActParam;
import org.eclipse.sensinact.core.command.AbstractTwinCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.twin.DefaultTimedValue;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.northbound.query.dto.result.ResultActDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.config.WithConfiguration;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

import jakarta.ws.rs.core.Application;
import tools.jackson.databind.JsonNode;

/**
 * Pins the exact REST wire format of the history provider ACT actions
 * (single/range/count) before the history provider rework. The stub below
 * returns the same Java shapes as the TimescaleDB implementation
 * (TimescaleDatabaseWorker.toTimedValue): Long for scale<=0 numerics, Double
 * otherwise, String, GeoJsonObject, an empty DefaultTimedValue when no data
 * matches, and the empty 501st "more data" marker in range results. The
 * reworked facade must keep these responses byte-identical.
 */
@WithConfiguration(pid = "sensinact.session.manager", properties = @Property(key = "auth.policy", value = "ALLOW_ALL"))
@WithConfiguration(pid = "sensinact.northbound.rest", location = "?", properties = {
        @Property(key = "allow.anonymous", value = "true"), @Property(key = "foobar", value = "history-golden") })
class HistoryActGoldenResponseTest {

    private static final String HISTORY_PROVIDER = "goldenHistory";
    private static final Instant TS_1 = Instant.parse("2012-01-01T00:00:00Z");
    private static final Instant TS_2 = Instant.parse("2012-01-02T00:00:00Z");

    public static class HistoryQueriesStub {

        @ACT(model = "sensiNactHistory", service = "history", resource = "single")
        public TimedValue<?> getSingleValue(@ActParam("provider") String provider,
                @ActParam("service") String service, @ActParam("resource") String resource,
                @ActParam("time") ZonedDateTime time) {
            return switch (resource) {
            case "longValue" -> new DefaultTimedValue<>(42L, TS_1);
            case "doubleValue" -> new DefaultTimedValue<>(4.2d, TS_1);
            case "stringValue" -> new DefaultTimedValue<>("fizzbuzz", TS_1);
            case "geoValue" -> new DefaultTimedValue<>(new Point(5.7d, 12.3d), TS_1);
            default -> new DefaultTimedValue<>();
            };
        }

        @ACT(model = "sensiNactHistory", service = "history", resource = "range")
        public List<TimedValue<?>> getValueRange(@ActParam("provider") String provider,
                @ActParam("service") String service, @ActParam("resource") String resource,
                @ActParam("fromTime") ZonedDateTime fromTime, @ActParam("toTime") ZonedDateTime toTime,
                @ActParam("skip") Integer skip) {
            return List.of(new DefaultTimedValue<>(42L, TS_1), new DefaultTimedValue<>(43L, TS_2),
                    new DefaultTimedValue<>());
        }

        @ACT(model = "sensiNactHistory", service = "history", resource = "count")
        public Long getStoredValueCount(@ActParam("provider") String provider, @ActParam("service") String service,
                @ActParam("resource") String resource, @ActParam("fromTime") ZonedDateTime fromTime,
                @ActParam("toTime") ZonedDateTime toTime) {
            return 1234L;
        }
    }

    @InjectService
    GatewayThread gatewayThread;

    final TestUtils utils = new TestUtils();

    @BeforeEach
    void await(@InjectService(filter = "(foobar=history-golden)", cardinality = 0) ServiceAware<Application> a,
            @InjectBundleContext BundleContext context) throws Exception {
        a.waitForService(5000);
        waitForRestApi(Duration.ofSeconds(5));

        context.registerService(HistoryQueriesStub.class, new HistoryQueriesStub(),
                new Hashtable<>(Map.of("sensiNact.whiteboard.resource", true, "sensiNact.provider.name",
                        HISTORY_PROVIDER)));

        gatewayThread.execute(new AbstractTwinCommand<Void>() {
            @Override
            protected Promise<Void> call(SensinactDigitalTwin twin, PromiseFactory pf) {
                if (twin.getProvider(HISTORY_PROVIDER) == null) {
                    twin.createProvider("https://eclipse.org/sensinact/sensiNactHistory", "sensiNactHistory",
                            HISTORY_PROVIDER);
                }
                return pf.resolved(null);
            }
        }).getValue();
    }

    private void waitForRestApi(Duration timeout) throws InterruptedException {
        Instant deadline = Instant.now().plus(timeout);
        while (Instant.now().isBefore(deadline)) {
            try {
                if (utils.queryStatus("/").statusCode() == 200) {
                    return;
                }
            } catch (Exception e) {
                // not reachable yet, retry until the deadline
            }
            Thread.sleep(100);
        }
        throw new AssertionFailedError("REST API did not appear within " + timeout);
    }

    private JsonNode actResponse(String action, Map<String, Object> extraParams) throws Exception {
        Map<String, Object> parameters = new HashMap<>(
                Map.of("provider", "sensor", "service", "data", "resource", "longValue"));
        parameters.putAll(extraParams);

        List<Map<String, Object>> params = parameters.entrySet().stream()
                .<Map<String, Object>>map(e -> Map.of("name", e.getKey(), "type", String.class.getName(), "value",
                        e.getValue()))
                .toList();

        ResultActDTO response = utils.queryJson(String.join("/", "providers", HISTORY_PROVIDER, "services", "history",
                "resources", action, "ACT"), params, ResultActDTO.class);
        assertNotNull(response);
        assertEquals(200, response.statusCode, () -> "action " + action + " failed: " + response.error);
        return utils.mapper.valueToTree(response.response);
    }

    private void assertGoldenJson(String expectedJson, JsonNode actual) throws Exception {
        assertEquals(utils.mapper.readTree(expectedJson), actual);
    }

    @Test
    void singleLongValue() throws Exception {
        JsonNode actual = actResponse("single", Map.of("resource", "longValue"));
        assertGoldenJson("""
                {"timestamp":"2012-01-01T00:00:00Z","value":42}
                """, actual);
    }

    @Test
    void singleDoubleValue() throws Exception {
        JsonNode actual = actResponse("single", Map.of("resource", "doubleValue"));
        assertGoldenJson("""
                {"timestamp":"2012-01-01T00:00:00Z","value":4.2}
                """, actual);
    }

    @Test
    void singleStringValue() throws Exception {
        JsonNode actual = actResponse("single", Map.of("resource", "stringValue"));
        assertGoldenJson("""
                {"timestamp":"2012-01-01T00:00:00Z","value":"fizzbuzz"}
                """, actual);
    }

    @Test
    void singleGeoValue() throws Exception {
        JsonNode actual = actResponse("single", Map.of("resource", "geoValue"));
        assertGoldenJson("""
                {"timestamp":"2012-01-01T00:00:00Z","value":{"type":"Point","coordinates":[5.7,12.3]}}
                """, actual);
    }

    @Test
    void singleWithoutDataIsEmptyTimedValue() throws Exception {
        JsonNode actual = actResponse("single", Map.of("resource", "unknown"));
        assertGoldenJson("""
                {"timestamp":null,"value":null}
                """, actual);
    }

    @Test
    void rangeWithMoreDataMarker() throws Exception {
        JsonNode actual = actResponse("range", Map.of());
        assertGoldenJson("""
                [{"timestamp":"2012-01-01T00:00:00Z","value":42},
                {"timestamp":"2012-01-02T00:00:00Z","value":43},
                {"timestamp":null,"value":null}]
                """, actual);
    }

    @Test
    void countValue() throws Exception {
        JsonNode actual = actResponse("count", Map.of());
        assertGoldenJson("1234", actual);
    }
}
