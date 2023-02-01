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
package org.eclipse.sensinact.sensorthings.sensing.rest.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.RootResponse;
import org.eclipse.sensinact.sensorthings.sensing.dto.RootResponse.NameUrl;
import org.eclipse.sensinact.sensorthings.sensing.dto.Self;
import org.junit.jupiter.api.Test;

/**
 * Tests the ref, count, ... filters
 */
public class FiltersTest extends AbstractIntegrationTest {

    final Random random = new Random();

    @Test
    void testCountFilter() throws IOException, InterruptedException {
        // Create providers
        int nbProviders = 4;
        for (int i = 0; i < nbProviders; i++) {
            createResource("countTester_" + (i + 1), "sensor", "rc", random.nextInt());
        }

        final RootResponse rootResponse = utils.queryJson("/", RootResponse.class);
        for (NameUrl url : rootResponse.value) {
            // No count by default
            ResultList<AnyIdDTO> resultList = utils.queryJson(url.url, RESULT_ANY);
            assertNull(resultList.count);
            assertNotNull(resultList.value);

            // Explicit no count
            resultList = utils.queryJson(url.url + "?$count=false", RESULT_ANY);
            assertNull(resultList.count);
            assertNotNull(resultList.value);

            // Explicit count
            resultList = utils.queryJson(url.url + "?$count=true", RESULT_ANY);
            assertTrue(resultList.value.size() >= nbProviders);
            assertEquals(resultList.value.size(), resultList.count);

            // Invalid value
            utils.assertURLStatus(url.url + "?$count=yes", 400);
            utils.assertURLStatus(url.url + "?$count=no", 400);
            utils.assertURLStatus(url.url + "?$count=1", 400);
            utils.assertURLStatus(url.url + "?$count=0", 400);
            utils.assertURLStatus(url.url + "?$count=", 400);
        }
    }

    private List<String> extractProviderIds(ResultList<AnyIdDTO> resultList) {
        return resultList.value.stream().map(item -> {
            String rawId = (String) item.id;
            if (rawId == null) {
                return null;
            } else {
                return rawId.split("~")[0];
            }
        }).distinct().collect(Collectors.toList());
    }

    @Test
    void testOrderBy() throws IOException, InterruptedException {
        final String prefix = "orderTester_";
        final List<String> sortedProviderIds = Stream
                .concat(IntStream.rangeClosed(0, 9).boxed().map(id -> prefix + id), Stream.of("sensiNact"))
                .sorted(Comparator.naturalOrder()).collect(Collectors.toList());
        final int nbProviders = sortedProviderIds.size();

        final List<String> reversedProviderIds = new ArrayList<>(sortedProviderIds);
        Collections.reverse(reversedProviderIds);

        sortedProviderIds.stream().filter(id -> id.startsWith(prefix)).forEach(id -> {
            createResource(id, "svcA", "rcA", id);
            createResource(id, "svcB", "rcA", id + 256);
            createResource(id, "svcA", "rcB", id);
            createResource(id, "svcB", "rcB", id + 256);
        });

        final RootResponse rootResponse = utils.queryJson("/", RootResponse.class);
        for (NameUrl url : rootResponse.value) {
            // Order by ID by default
            ResultList<AnyIdDTO> resultList = utils.queryJson(url.url, RESULT_ANY);
            assertNull(resultList.count);
            assertEquals(sortedProviderIds, extractProviderIds(resultList));

            // Explicit order by ID
            resultList = utils.queryJson(url.url + "?$orderby=id", RESULT_ANY);
            assertNull(resultList.count);
            assertEquals(sortedProviderIds, extractProviderIds(resultList));

            // Explicit ascending order by ID
            resultList = utils.queryJson(url.url + "?$orderby=id%20asc", RESULT_ANY);
            assertNull(resultList.count);
            assertEquals(sortedProviderIds, extractProviderIds(resultList));

            // Order + count
            resultList = utils.queryJson(url.url + "?$orderby=id&$count=true", RESULT_ANY);
            assertTrue(resultList.count >= nbProviders);
            assertEquals(sortedProviderIds, extractProviderIds(resultList));

            // Reverse order by ID
            resultList = utils.queryJson(url.url + "?$orderby=id%20desc", RESULT_ANY);
            assertNull(resultList.count);
            assertEquals(reversedProviderIds, extractProviderIds(resultList));

            // Reverse order by ID, with multiple space characters
            resultList = utils.queryJson(url.url + "?$orderby=id%20%20%20desc", RESULT_ANY);
            assertNull(resultList.count);
            assertEquals(reversedProviderIds, extractProviderIds(resultList));

            // Reverse order + count
            resultList = utils.queryJson(url.url + "?$orderby=id%20desc&$count=true", RESULT_ANY);
            assertTrue(resultList.count >= nbProviders);
            assertEquals(reversedProviderIds, extractProviderIds(resultList));
        }
    }

    @Test
    void testRef() throws IOException, InterruptedException {
        final String provider = "refTester";
        final String svc = "sensor";
        final String rc = "rc";
        createResource(provider, svc, rc, 42);

        // Parsing will fail if there is any other JSON property
        ResultList<Self> resultList = utils.queryJson(String.format("/Things(%s)/Datastreams/$ref", provider),
                RESULT_SELF);
        assertNull(resultList.count);

        List<Self> references = resultList.value.stream()
                .filter(s -> s.selfLink.endsWith(String.format("Datastreams(%s)", String.join("~", provider, svc, rc))))
                .collect(Collectors.toList());
        assertEquals(1, references.size());

        // Add the count
        resultList = utils.queryJson(String.format("/Things(%s)/Datastreams/$ref?$count=true", provider), RESULT_SELF);
        assertEquals(resultList.value.size(), resultList.count);
        references = resultList.value.stream()
                .filter(s -> s.selfLink.endsWith(String.format("Datastreams(%s)", String.join("~", provider, svc, rc))))
                .collect(Collectors.toList());
        assertEquals(1, references.size());
    }

    @Test
    void testProp() throws IOException, InterruptedException {
        final Instant creationTime = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        final String provider = "propTester";
        final String svc = "sensor";
        final String rc = "rc";
        final int value = random.nextInt();
        createResource(provider, svc, rc, value, creationTime);

        ResultList<Self> observations = utils
                .queryJson(String.format("/FeaturesOfInterest(%s)/Observations/$ref", provider), RESULT_SELF);
        String baseUrl = observations.value.stream()
                .filter(s -> s.selfLink.contains(String.join("~", provider, svc, rc))).findFirst().get().selfLink;

        String property = "resultTime";
        Map<?, ?> rawResult = utils.queryJson(baseUrl + "/" + property, Map.class);
        assertEquals(Set.of(property), rawResult.keySet());
        assertEquals(creationTime.toString(), rawResult.get(property));

        property = "result";
        rawResult = utils.queryJson(baseUrl + "/" + property, Map.class);
        assertEquals(Set.of(property), rawResult.keySet());
        assertEquals(value, rawResult.get(property));
    }

    @Test
    void testSelect() throws IOException, InterruptedException {
        final String provider = "selectTester";
        final String svc = "sensor";
        final String rc = "rc";
        createResource(provider, svc, rc, 42);

        Set<String> selectedFields = Set.of("result", "resultTime");
        Map<?, ?> rawResultList = utils.queryJson("/Observations/?$select=" + String.join(",", selectedFields),
                Map.class);
        List<?> items = (List<?>) rawResultList.get("value");
        assertFalse(items.isEmpty());
        for (Object rawItem : items) {
            Map<?, ?> item = utils.getMapper().convertValue(rawItem, Map.class);
            assertEquals(selectedFields, item.keySet());
        }
    }

    @Test
    void testSkipTop() throws IOException, InterruptedException {
        // Register the resources
        final String provider = "skipTopFilter";
        final String svc = "sensor";
        final String rcPrefix = "rc";
        final int nbRc = 5;
        for (int i = 0; i < nbRc; i++) {
            createResource(provider, svc, rcPrefix + i, i);
        }

        // List all datastreams (should be more or as many as our resources)
        final ResultList<AnyIdDTO> allStreams = utils
                .queryJson(String.format("/Things(%s)/Datastreams?$count=true", provider), RESULT_ANY);
        final int nbIds = allStreams.count;
        final List<String> allIds = allStreams.value.stream().map(s -> (String) s.id).collect(Collectors.toList());
        assertTrue(allIds.size() >= nbRc);
        assertEquals(allIds.size(), nbIds);

        // Test top
        int topVal = 2;
        ResultList<AnyIdDTO> subStreams = utils
                .queryJson(String.format("/Things(%s)/Datastreams?$top=%d", provider, topVal), RESULT_ANY);
        List<String> ids = subStreams.value.stream().map(s -> (String) s.id).collect(Collectors.toList());
        assertNull(subStreams.count);
        assertEquals(topVal, ids.size());
        assertEquals(allIds.subList(0, topVal), ids);

        subStreams = utils.queryJson(String.format("/Things(%s)/Datastreams?$top=%d&$count=true", provider, topVal),
                RESULT_ANY);
        assertEquals(ids, subStreams.value.stream().map(s -> (String) s.id).collect(Collectors.toList()));
        assertEquals(nbIds, subStreams.count);

        // Test skip
        int skipVal = 2;
        subStreams = utils.queryJson(String.format("/Things(%s)/Datastreams?$skip=%d", provider, skipVal), RESULT_ANY);
        ids = subStreams.value.stream().map(s -> (String) s.id).collect(Collectors.toList());
        assertNull(subStreams.count);
        assertEquals(nbIds - skipVal, ids.size());
        assertEquals(allIds.subList(skipVal, allIds.size()), ids);

        subStreams = utils.queryJson(String.format("/Things(%s)/Datastreams?$skip=%d&$count=true", provider, skipVal),
                RESULT_ANY);
        assertEquals(ids, subStreams.value.stream().map(s -> (String) s.id).collect(Collectors.toList()));
        assertEquals(nbIds, subStreams.count);

        // Test both
        subStreams = utils.queryJson(
                String.format("/Things(%s)/Datastreams?$top=%d&$skip=%d", provider, topVal, skipVal), RESULT_ANY);
        ids = subStreams.value.stream().map(s -> (String) s.id).collect(Collectors.toList());
        assertNull(subStreams.count);
        assertEquals(topVal, ids.size());
        assertEquals(allIds.subList(skipVal, skipVal + topVal), ids);

        subStreams = utils.queryJson(
                String.format("/Things(%s)/Datastreams?$top=%d&$skip=%d&$count=true", provider, topVal, skipVal),
                RESULT_ANY);
        assertEquals(ids, subStreams.value.stream().map(s -> (String) s.id).collect(Collectors.toList()));
        assertEquals(nbIds, subStreams.count);
    }
}
