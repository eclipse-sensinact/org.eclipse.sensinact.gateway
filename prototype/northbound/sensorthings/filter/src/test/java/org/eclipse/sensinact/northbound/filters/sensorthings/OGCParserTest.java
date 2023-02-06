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
package org.eclipse.sensinact.northbound.filters.sensorthings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterLexer;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.BoolcommonexprContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.BoolCommonExprVisitor;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.ResourceValueFilterInputHolder;
import org.eclipse.sensinact.prototype.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.prototype.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.prototype.snapshot.ServiceSnapshot;
import org.junit.jupiter.api.Test;

public class OGCParserTest {

    private void assertQuery(final boolean expected, final String query,
            final ResourceValueFilterInputHolder testProvider) throws Exception {
        ANTLRInputStream inStream = new ANTLRInputStream(query);
        ODataFilterLexer markupLexer = new ODataFilterLexer(inStream);
        CommonTokenStream commonTokenStream = new CommonTokenStream(markupLexer);
        ODataFilterParser parser = new ODataFilterParser(commonTokenStream);
        BoolcommonexprContext context = parser.boolcommonexpr();

        try {
            BoolCommonExprVisitor visitor = new BoolCommonExprVisitor(parser);
            final Predicate<ResourceValueFilterInputHolder> predicate = visitor.visit(context);
            if (predicate != null) {
                assertEquals(expected, predicate.test(testProvider),
                        String.format("Expected %s for query: %s", expected, query));
            } else {
                fail("Coudln't parse '" + query + "'");
            }
        } catch (Throwable e) {
            e.printStackTrace();
            System.out.println(context.toStringTree(parser));
            throw e;
        }
    }

    private void assertQueries(final Map<String, Boolean> expectations, final ResourceValueFilterInputHolder testObject)
            throws Exception {
        for (Entry<String, Boolean> expectation : expectations.entrySet()) {
            assertQuery(expectation.getValue(), expectation.getKey(), testObject);
        }
    }

    private void assertQueries(final Map<String, Boolean> expectations) throws Exception {
        assertQueries(expectations, null);
    }

    @Test
    void testNumbersComparison() throws Exception {

        final Map<String, Boolean> expectations = new LinkedHashMap<>();
        // Equal
        expectations.put("1 eq 1", true);
        expectations.put("1 eq 2", false);
        expectations.put("1 eq -1", false);
        // Not equal
        expectations.put("1 ne 1", false);
        expectations.put("1 ne 2", true);
        // Less or equal
        expectations.put("1 le 1", true);
        expectations.put("1 le 2", true);
        expectations.put("2 le 1", false);
        // Less than
        expectations.put("1 lt 1", false);
        expectations.put("1 lt 2", true);
        expectations.put("2 lt 1", false);
        // Greater or equal
        expectations.put("1 ge 1", true);
        expectations.put("1 ge 2", false);
        expectations.put("2 ge 1", true);
        // Greater than
        expectations.put("1 gt 1", false);
        expectations.put("1 gt 2", false);
        expectations.put("2 gt 1", true);

        // Complex
        expectations.put("42 ge 21 and 45 ge 42", true);
        expectations.put("42 ge 21 and 21 ge 42", false);
        expectations.put("42 ge 21 or 21 ge 42", true);
        assertQueries(expectations);
    }

    @Test
    void testNumbersOperations() throws Exception {
        final Map<String, Boolean> expectations = new LinkedHashMap<>();
        expectations.put("10 add 3 eq 13", true);
        expectations.put("10 add 4 ge 13", true);
        expectations.put("10 sub 3 eq 7", true);
        expectations.put("10 mul 3 eq 30", true);
        expectations.put("10 div 2 eq 5", true);
        expectations.put("15 div 2 eq 7.5", true);
        expectations.put("12 mod 10 eq 2", true);
        assertQueries(expectations);
    }

    @Test
    void testStringOperations() throws Exception {
        final Map<String, Boolean> expectations = new LinkedHashMap<>();
        expectations.put("length('hello') eq 5", true);
        expectations.put("indexof(' Sensor Things','Sensor') eq 1", true);

        expectations.put("substring('Sensor Things',1) eq 'ensor Things'", true);
        expectations.put("substring('Sensor Things',2,4) eq 'nsor'", true);

        expectations.put("tolower('Sensor Things') eq 'sensor things'", true);
        expectations.put("toupper('Sensor Things') eq 'SENSOR THINGS'", true);

        expectations.put("trim('  Sensor Things  ') eq 'Sensor Things'", true);
        expectations.put("concat(concat('degree', ', '), 'Celsius') eq 'degree, Celsius'", true);

        assertQueries(expectations);
    }

    @Test
    void testStringBoolOps() throws Exception {
        final Map<String, Boolean> expectations = new LinkedHashMap<>();
        expectations.put("startswith('SensorThings', 'Sensor')", true);
        expectations.put("endswith('SensorThings', 'Things')", true);
        expectations.put("contains('SensorThings', 'Things')", true);
        expectations.put("substringof('SensorThings', 'Things')", true);
        assertQueries(expectations);
    }

    @Test
    void testPath() throws Exception {
        final Map<String, Boolean> expectations = new LinkedHashMap<>();
        expectations.put("Datastream/id eq 'testProvider'", true);
        expectations.put("result lt 10.00", true);

        ProviderSnapshot provider = RcUtils.makeProvider("testProvider");
        ServiceSnapshot svc = RcUtils.addService(provider, "test");
        ResourceSnapshot rc = RcUtils.addResource(svc, "result", 5.0);

        ResourceValueFilterInputHolder holder = new ResourceValueFilterInputHolder(provider, List.of(rc));
        assertQueries(expectations, holder);
    }
}
