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
package org.eclipse.sensinact.sensorthings.sensing.rest.integration.sensinact;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.rest.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.service.cm.Configuration;
import org.osgi.test.common.annotation.config.InjectConfiguration;
import org.osgi.test.common.annotation.config.WithConfiguration;

import tools.jackson.core.type.TypeReference;

/**
 * $top/$skip/$orderby and $filter constraint pushdown against the in-memory
 * history backend — no docker required. The pushed-down page must match what
 * the in-memory pagination filters would have produced, and $filter requests
 * beyond time ranges and numeric value conditions must keep using the
 * in-memory path.
 */
public class ObservationPushdownInMemoryTest extends AbstractIntegrationTest {

    private static final TypeReference<ResultList<Observation>> OBSERVATIONS = new TypeReference<>() {
    };

    private static final Instant BASE_TIME = Instant.parse("2024-01-01T00:00:00Z");
    private static final String DATASTREAM_PATH = "/Datastreams(pushdownSensor~data~temperature)/Observations";
    private static final int SEEDED_OBSERVATIONS = 20;

    private Configuration historyProviderConfig;

    @BeforeEach
    void setupTest(
            @InjectConfiguration(withConfig = @WithConfiguration(pid = "sensinact.history.inmemory", location = "?")) Configuration historyConfig,
            @InjectConfiguration(withConfig = @WithConfiguration(pid = "sensinact.sensorthings.northbound.rest", location = "?")) Configuration sensorthingsConfig)
            throws Exception {

        historyProviderConfig = historyConfig;
        // the configuration only reaches the store component once updated
        historyConfig.update(new Hashtable<>(Map.of("provider", "inmemory-history")));

        Hashtable<String, Object> newProps = new Hashtable<>();
        newProps.put("history.provider", "inmemory-history");
        Dictionary<String, Object> properties = sensorthingsConfig.getProperties();
        Enumeration<String> keys = properties.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            newProps.put(key, properties.get(key));
        }
        sensorthingsConfig.update(newProps);

        waitForHistoryChain();
        seedObservations();
    }

    @AfterEach
    void cleanupTest() throws Exception {
        if (historyProviderConfig != null) {
            historyProviderConfig.delete();
            historyProviderConfig = null;
        }
    }

    /**
     * Proves the ingestion chain is wired before seeding: pushes probe values
     * until at least two of them are readable through the API — a single
     * observation could also be the live-value fall-back.
     */
    private void waitForHistoryChain() throws Exception {
        final long deadline = System.currentTimeMillis() + 10000;
        int probeValue = 0;
        Object lastState = null;
        while (System.currentTimeMillis() < deadline) {
            try {
                createResource("pushdownProbe", "probe", "probe", ++probeValue, Instant.now());
                ResultList<Observation> list = utils
                        .queryJson("/Datastreams(pushdownProbe~probe~probe)/Observations?$count=true", OBSERVATIONS);
                if (list.count() != null && list.count() >= 2) {
                    return;
                }
                lastState = list;
            } catch (Exception e) {
                // the configuration update restarts the application; requests
                // in that window get error pages instead of JSON
                lastState = e;
            }
            Thread.sleep(200);
        }
        fail("The history ingestion chain did not come up: " + lastState);
    }

    /** Values 0..19 with strictly increasing timestamps, one minute apart. */
    private void seedObservations() throws Exception {
        for (int i = 0; i < SEEDED_OBSERVATIONS; i++) {
            createResource("pushdownSensor", "data", "temperature", i, BASE_TIME.plus(i, ChronoUnit.MINUTES));
        }

        final long deadline = System.currentTimeMillis() + 10000;
        Object lastState = null;
        while (System.currentTimeMillis() < deadline) {
            try {
                ResultList<Observation> list = utils.queryJson(DATASTREAM_PATH + "?$count=true", OBSERVATIONS);
                if (list.count() != null && list.count() == SEEDED_OBSERVATIONS) {
                    return;
                }
                lastState = list;
            } catch (Exception e) {
                lastState = e;
            }
            Thread.sleep(200);
        }
        fail("The seeded observations did not all reach the history: " + lastState);
    }

    private static int result(Observation observation) {
        return ((Number) observation.result()).intValue();
    }

    @Test
    void firstPageIsOldestChronologicalWithCountAndNextLink() throws Exception {
        ResultList<Observation> page = utils.queryJson(DATASTREAM_PATH + "?$top=5&$count=true", OBSERVATIONS);

        assertEquals(SEEDED_OBSERVATIONS, page.count());
        assertEquals(List.of(0, 1, 2, 3, 4), page.value().stream().map(ObservationPushdownInMemoryTest::result).toList());
        assertEquals(BASE_TIME, page.value().get(0).phenomenonTime());
        assertNotNull(page.nextLink());
        assertTrue(page.nextLink().contains("skip=5"), "Unexpected nextLink: " + page.nextLink());
    }

    @Test
    void skipMovesTheWindow() throws Exception {
        ResultList<Observation> page = utils.queryJson(DATASTREAM_PATH + "?$top=5&$skip=12&$count=true", OBSERVATIONS);

        assertEquals(SEEDED_OBSERVATIONS, page.count());
        assertEquals(List.of(12, 13, 14, 15, 16),
                page.value().stream().map(ObservationPushdownInMemoryTest::result).toList());
        assertTrue(page.nextLink().contains("skip=17"), "Unexpected nextLink: " + page.nextLink());
    }

    @Test
    void lastPageHasNoNextLink() throws Exception {
        ResultList<Observation> page = utils.queryJson(DATASTREAM_PATH + "?$top=5&$skip=15", OBSERVATIONS);

        assertEquals(List.of(15, 16, 17, 18, 19),
                page.value().stream().map(ObservationPushdownInMemoryTest::result).toList());
        assertNull(page.nextLink());
    }

    @Test
    void descendingTimeOrderIsPushedDown() throws Exception {
        ResultList<Observation> page = utils
                .queryJson(DATASTREAM_PATH + "?$orderby=phenomenonTime%20desc&$top=3", OBSERVATIONS);

        assertEquals(List.of(19, 18, 17), page.value().stream().map(ObservationPushdownInMemoryTest::result).toList());
    }

    @Test
    void ascendingResultTimeOrderPages() throws Exception {
        ResultList<Observation> page = utils
                .queryJson(DATASTREAM_PATH + "?$orderby=resultTime%20asc&$top=2&$skip=6", OBSERVATIONS);

        assertEquals(List.of(6, 7), page.value().stream().map(ObservationPushdownInMemoryTest::result).toList());
    }

    @Test
    void skipBeyondTheDatasetYieldsAnEmptyPageNotTheLiveValue() throws Exception {
        ResultList<Observation> page = utils.queryJson(DATASTREAM_PATH + "?$top=5&$skip=25&$count=true", OBSERVATIONS);

        assertEquals(SEEDED_OBSERVATIONS, page.count());
        assertTrue(page.value().isEmpty());
        assertNull(page.nextLink());
    }

    @Test
    void timeOnlyFilterIsPushedDownWithRangeScopedCount() throws Exception {
        String query = "?$filter=phenomenonTime%20ge%202024-01-01T00:06:00Z%20and%20phenomenonTime%20lt%202024-01-01T00:12:00Z"
                + "&$top=4&$count=true";
        ResultList<Observation> page = utils.queryJson(DATASTREAM_PATH + query, OBSERVATIONS);

        assertEquals(6, page.count());
        assertEquals(List.of(6, 7, 8, 9), page.value().stream().map(ObservationPushdownInMemoryTest::result).toList());
        assertNotNull(page.nextLink());
        assertTrue(page.nextLink().contains("skip=4"), "Unexpected nextLink: " + page.nextLink());

        ResultList<Observation> next = utils.queryJson(DATASTREAM_PATH + query + "&$skip=4", OBSERVATIONS);
        assertEquals(6, next.count());
        assertEquals(List.of(10, 11), next.value().stream().map(ObservationPushdownInMemoryTest::result).toList());
        assertNull(next.nextLink());
    }

    @Test
    void combinedValueAndTimeFilterIsPushedDown() throws Exception {
        ResultList<Observation> page = utils.queryJson(DATASTREAM_PATH
                + "?$filter=result%20ge%2010%20and%20phenomenonTime%20lt%202024-01-01T00:15:00Z&$top=3&$count=true",
                OBSERVATIONS);

        assertEquals(5, page.count());
        assertEquals(List.of(10, 11, 12), page.value().stream().map(ObservationPushdownInMemoryTest::result).toList());
        assertNotNull(page.nextLink());
    }

    @Test
    void numericValueFilterIsPushedDownWithFilteredCount() throws Exception {
        ResultList<Observation> page = utils.queryJson(
                DATASTREAM_PATH + "?$filter=result%20ge%2010&$top=5&$count=true", OBSERVATIONS);

        assertEquals(10, page.count());
        assertEquals(List.of(10, 11, 12, 13, 14),
                page.value().stream().map(ObservationPushdownInMemoryTest::result).toList());
        assertNotNull(page.nextLink());
        assertTrue(page.nextLink().contains("skip=5"), "Unexpected nextLink: " + page.nextLink());

        ResultList<Observation> next = utils
                .queryJson(DATASTREAM_PATH + "?$filter=result%20ge%2010&$top=5&$count=true&$skip=5", OBSERVATIONS);
        assertEquals(10, next.count());
        assertEquals(List.of(15, 16, 17, 18, 19),
                next.value().stream().map(ObservationPushdownInMemoryTest::result).toList());
        assertNull(next.nextLink());
    }

    @Test
    void disjunctiveFilterFallsBackWithCorrectResults() throws Exception {
        ResultList<Observation> page = utils.queryJson(
                DATASTREAM_PATH + "?$filter=result%20lt%203%20or%20result%20gt%2016&$top=4&$count=true", OBSERVATIONS);

        assertEquals(6, page.count());
        assertEquals(List.of(0, 1, 2, 17), page.value().stream().map(ObservationPushdownInMemoryTest::result).toList());
        assertNotNull(page.nextLink());
    }

    @Test
    void nonTimeOrderByFallsBackToInMemorySorting() throws Exception {
        ResultList<Observation> page = utils.queryJson(DATASTREAM_PATH + "?$orderby=result%20desc&$top=3",
                OBSERVATIONS);

        assertEquals(List.of(19, 18, 17), page.value().stream().map(ObservationPushdownInMemoryTest::result).toList());
    }
}
