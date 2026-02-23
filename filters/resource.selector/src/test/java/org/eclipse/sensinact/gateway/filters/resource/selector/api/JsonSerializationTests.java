/*********************************************************************
* Copyright (c) 2025 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.gateway.filters.resource.selector.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.eclipse.sensinact.filters.resource.selector.api.CompactResourceSelector;
import org.eclipse.sensinact.filters.resource.selector.api.ResourceSelector;
import org.eclipse.sensinact.filters.resource.selector.api.ResourceSelector.ProviderSelection;
import org.eclipse.sensinact.filters.resource.selector.api.ResourceSelector.ResourceSelection;
import org.eclipse.sensinact.filters.resource.selector.api.Selection;
import org.eclipse.sensinact.filters.resource.selector.api.Selection.MatchType;
import org.eclipse.sensinact.filters.resource.selector.api.ValueSelection;
import org.eclipse.sensinact.filters.resource.selector.api.ValueSelection.CheckType;
import org.eclipse.sensinact.filters.resource.selector.api.ValueSelection.OperationType;
import org.eclipse.sensinact.filters.resource.selector.api.ValueSelection.ValueSelectionMode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.json.JsonMapper;

class JsonSerializationTests {

    private final JsonMapper mapper = JsonMapper.builder().build();
    private final Path selectorsFolder = Paths.get("src/test/resources/selectors");

    @Nested
    class CompactTests {
        @Test
        void explicitNull() throws Exception {
            final String value = "{\"provider\":{\"value\":\"foobar\", \"type\": null},\"service\":{\"value\":\"admin\"}}";
            CompactResourceSelector selector = mapper.readValue(value, CompactResourceSelector.class);
            assertEquals("foobar", selector.provider().value());
            assertEquals(MatchType.EXACT, selector.provider().type());
            assertFalse(selector.provider().negate());
            assertEquals("admin", selector.service().value());
            assertEquals(MatchType.EXACT, selector.service().type());
            assertFalse(selector.service().negate());
        }

        @Test
        void compact() throws StreamReadException, DatabindException, IOException {
            ResourceSelector selector = mapper.readValue(selectorsFolder.resolve("compact.json").toFile(),
                    ResourceSelector.class);

            assertEquals(1, selector.providers().size());
            assertEquals(0, selector.resources().size());

            ProviderSelection ps = selector.providers().get(0);
            assertNotNull(ps);

            checkSelection(ps.modelUri(), "foo", MatchType.EXACT, false);
            checkSelection(ps.model(), "bar", MatchType.EXACT, false);
            checkSelection(ps.provider(), "test.+", MatchType.REGEX, false);

            assertEquals(List.of(), ps.location());

            assertEquals(1, ps.resources().size());

            ResourceSelection rs = ps.resources().get(0);
            assertNotNull(rs);

            checkSelection(rs.service(), "\\d\\d", MatchType.REGEX_REGION, false);
            checkSelection(rs.resource(), "look", MatchType.EXACT, false);
            assertEquals(1, rs.value().size());

            ValueSelection vs = rs.value().get(0);
            checkValueSelection(vs, "17", OperationType.EQUALS, false, CheckType.VALUE, ValueSelectionMode.ANY_MATCH);
        }

        @Test
        void minimal() throws StreamReadException, DatabindException, IOException {
            ResourceSelector selector = mapper.readValue(selectorsFolder.resolve("minimal.json").toFile(),
                    ResourceSelector.class);

            assertEquals(1, selector.providers().size());
            assertEquals(1, selector.resources().size());

            ProviderSelection ps = selector.providers().get(0);
            assertNotNull(ps);

            assertNull(ps.modelUri());
            checkSelection(ps.model(), "foo", MatchType.EXACT, false);
            assertNull(ps.provider());

            assertEquals(List.of(), ps.location());
            assertEquals(List.of(), ps.resources());

            ResourceSelection rs = selector.resources().get(0);
            assertNotNull(rs);

            assertNull(rs.service());
            assertNull(rs.resource());
            assertEquals(List.of(), rs.value());
        }

        @Test
        void compactMulti() throws StreamReadException, DatabindException, IOException {
            ResourceSelector selector = mapper.readValue(selectorsFolder.resolve("compact-multi.json").toFile(),
                    ResourceSelector.class);

            assertEquals(1, selector.providers().size());
            assertEquals(0, selector.resources().size());

            ProviderSelection ps = selector.providers().get(0);
            assertNotNull(ps);

            assertNull(ps.modelUri());
            checkSelection(ps.model(), "foo", MatchType.EXACT, false);
            checkSelection(ps.provider(), "test.+", MatchType.REGEX, false);

            assertEquals(List.of(), ps.location());

            assertEquals(1, ps.resources().size());

            ResourceSelection rs = ps.resources().get(0);
            assertNotNull(rs);

            checkSelection(rs.service(), "\\d\\d", MatchType.REGEX_REGION, false);
            checkSelection(rs.resource(), "look", MatchType.EXACT, false);
            assertEquals(2, rs.value().size());

            checkValueSelection(rs.value().get(0), "5", OperationType.GREATER_THAN, false, CheckType.VALUE,
                    ValueSelectionMode.ANY_MATCH);
            checkValueSelection(rs.value().get(1), "17", OperationType.LESS_THAN, false, CheckType.VALUE,
                    ValueSelectionMode.ANY_MATCH);
        }

    }

    @Nested
    class FullTests {

        @ParameterizedTest
        @ValueSource(strings = { "full.json", "full-single-elements.json" })
        void full(String file) throws StreamReadException, DatabindException, IOException {
            ResourceSelector selector = mapper.readValue(selectorsFolder.resolve(file).toFile(),
                    ResourceSelector.class);

            assertEquals(1, selector.providers().size());
            assertEquals(1, selector.resources().size());

            ProviderSelection ps = selector.providers().get(0);
            assertNotNull(ps);

            assertNull(ps.modelUri());
            checkSelection(ps.model(), "foo", MatchType.EXACT, false);
            checkSelection(ps.provider(), "test.+", MatchType.REGEX, false);

            assertEquals(List.of(), ps.location());

            assertEquals(1, ps.resources().size());

            ResourceSelection rs = ps.resources().get(0);
            assertNotNull(rs);

            checkSelection(rs.service(), "\\d\\d", MatchType.REGEX_REGION, false);
            checkSelection(rs.resource(), "look", MatchType.EXACT, false);
            assertEquals(1, rs.value().size());

            ValueSelection vs = rs.value().get(0);
            checkValueSelection(vs, "17", OperationType.EQUALS, false, CheckType.VALUE, ValueSelectionMode.ANY_MATCH);

            rs = selector.resources().get(0);
            assertNotNull(rs);

            checkSelection(rs.service(), "svc", MatchType.EXACT, false);
            checkSelection(rs.resource(), "look", MatchType.EXACT, false);
            assertEquals(0, rs.value().size());
        }

        @Test
        void fullValues() throws StreamReadException, DatabindException, IOException {
            ResourceSelector selector = mapper.readValue(selectorsFolder.resolve("full-many.json").toFile(),
                    ResourceSelector.class);

            assertEquals(1, selector.providers().size());
            assertEquals(1, selector.resources().size());

            ProviderSelection ps = selector.providers().get(0);
            assertNotNull(ps);

            assertNull(ps.modelUri());
            checkSelection(ps.model(), "foo", MatchType.EXACT, false);
            checkSelection(ps.provider(), "test.+", MatchType.REGEX, false);

            assertEquals(List.of(), ps.location());

            assertEquals(1, ps.resources().size());

            ResourceSelection rs = ps.resources().get(0);
            assertNotNull(rs);

            checkSelection(rs.service(), "\\d\\d", MatchType.REGEX_REGION, false);
            checkSelection(rs.resource(), "look", MatchType.EXACT, false);
            assertEquals(1, rs.value().size());

            ValueSelection vs = rs.value().get(0);
            checkValueSelection(vs, List.of("a", "b", "c"), OperationType.EQUALS, false, CheckType.VALUE,
                    ValueSelectionMode.ALL_MATCH);
        }
    }

    private void checkSelection(Selection s, String value, MatchType type, boolean negate) {
        assertNotNull(s);
        assertEquals(value, s.value());
        assertEquals(type, s.type());
        assertEquals(negate, s.negate());
    }

    private void checkValueSelection(ValueSelection vs, String value, OperationType type, boolean negate,
            CheckType check, ValueSelectionMode mode) {
        checkValueSelection(vs, List.of(value), type, negate, check, mode);
    }

    private void checkValueSelection(ValueSelection vs, List<String> value, OperationType type, boolean negate,
            CheckType check, ValueSelectionMode mode) {
        assertNotNull(vs);
        assertEquals(value, vs.value());
        assertEquals(type, vs.operation());
        assertFalse(vs.negate());
        assertEquals(check, vs.checkType());
        assertEquals(mode, vs.valueSelectionMode());
    }

}
