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
package org.eclipse.sensinact.northbound.filters.ldap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.eclipse.sensinact.core.model.ResourceType;
import org.eclipse.sensinact.core.model.ValueType;
import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceValueFilter;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.northbound.filters.ldap.antlr.LdapFilterLexer;
import org.eclipse.sensinact.northbound.filters.ldap.antlr.LdapFilterParser;
import org.eclipse.sensinact.northbound.filters.ldap.antlr.LdapFilterParser.FilterContext;
import org.eclipse.sensinact.northbound.filters.ldap.antlr.impl.FilterVisitor;
import org.eclipse.sensinact.northbound.filters.ldap.antlr.impl.ILdapCriterion;
import org.junit.jupiter.api.Test;

public class LdapParserTest {

    public static ICriterion parse(final String query) {
        try {
            final CharStream inStream = CharStreams.fromString(query);
            final LdapFilterLexer markupLexer = new LdapFilterLexer(inStream);
            final CommonTokenStream commonTokenStream = new CommonTokenStream(markupLexer);
            final LdapFilterParser parser = new LdapFilterParser(commonTokenStream);
            final FilterContext parsedContext = parser.filter();
            final FilterVisitor visitor = new FilterVisitor(parser);
            ILdapCriterion result = visitor.visit(parsedContext);
            return result;
        } catch (Exception e) {
            System.err.println("Error: " + e);
            e.printStackTrace();
            return null;
        }
    }

    ResourceSnapshot makeResource(final String svcName, final String rcName, final Object value) {
        return new ResourceSnapshot() {
            @Override
            public String getName() {
                return rcName;
            }

            @Override
            public Map<String, Object> getMetadata() {
                return Map.of();
            }

            @Override
            public String toString() {
                return String.format("%s.%s=%s", svcName, rcName, value);
            }

            @Override
            public TimedValue<?> getValue() {
                return new TimedValue<Object>() {
                    @Override
                    public Instant getTimestamp() {
                        return Instant.now();
                    }

                    @Override
                    public Object getValue() {
                        return value;
                    }
                };
            }

            @Override
            public Instant getSnapshotTime() {
                return Instant.now();
            }

            ResourceSnapshot getRc() {
                return this;
            }

            @Override
            public boolean isSet() {
                return true;
            }

            @Override
            public ServiceSnapshot getService() {
                return new ServiceSnapshot() {

                    @Override
                    public Instant getSnapshotTime() {
                        return getRc().getSnapshotTime();
                    }

                    @Override
                    public String getName() {
                        return svcName;
                    }

                    @Override
                    public List<ResourceSnapshot> getResources() {
                        return List.of(getRc());
                    }

                    ServiceSnapshot getSvc() {
                        return this;
                    }

                    @Override
                    public ProviderSnapshot getProvider() {
                        return new ProviderSnapshot() {

                            @Override
                            public Instant getSnapshotTime() {
                                return getRc().getSnapshotTime();
                            }

                            @Override
                            public String getName() {
                                return "provider1";
                            }

                            @Override
                            public List<ServiceSnapshot> getServices() {
                                return List.of(getSvc());
                            }

                            @Override
                            public String getModelPackageUri() {
                                return "https://eclipse.org/sensinact/test/";
                            }

                            @Override
                            public String getModelName() {
                                return "model1";
                            }
                        };
                    }
                };
            }

            @Override
            public ResourceType getResourceType() {
                return ResourceType.PROPERTY;
            }

            @Override
            public ValueType getValueType() {
                return ValueType.UPDATABLE;
            }
        };
    }

    private boolean testResource(final String ldapQuery, final ResourceSnapshot... rc) {
        final ICriterion criterion = parse(ldapQuery);
        assertNotNull(criterion, "No criterion returned for: " + ldapQuery);

        final ResourceValueFilter valueFilter = criterion.getResourceValueFilter();
        assertNotNull(valueFilter, "No resource value filter parsed from " + ldapQuery);
        return valueFilter.test(rc[0].getService().getProvider(), Arrays.asList(rc));
    }

    private void assertQueryTrue(final String ldapQuery, final ResourceSnapshot... rc) {
        assertTrue(testResource(ldapQuery, rc), ldapQuery + "; rc:" + Arrays.toString(rc));
    }

    private void assertQueryFalse(final String ldapQuery, final ResourceSnapshot... rc) {
        assertFalse(testResource(ldapQuery, rc), ldapQuery + "; rc:" + Arrays.toString(rc));
    }

    @Test
    void testPresence() throws Exception {
        for (Object value : List.of("test", 42, 51L, 43.5)) {
            ResourceSnapshot rc = makeResource("test", "hello", value);
            ICriterion filter = parse("(test.hello=*)");

            // ... value level
            ResourceValueFilter rcPredicate = filter.getResourceValueFilter();
            assertNotNull(rcPredicate);
            assertTrue(rcPredicate.test(rc.getService().getProvider(), List.of(rc)));

            // Invalid resource
            ResourceSnapshot invalid = makeResource("test", "bye", value);
            rcPredicate = filter.getResourceValueFilter();
            assertFalse(rcPredicate.test(invalid.getService().getProvider(), List.of(invalid)));
        }
    }

    @Test
    void testEquality() throws Exception {
        for (Object value : List.of("test", 42, 51L, 43.5)) {
            final ResourceSnapshot rc = makeResource("svc", "value", value);
            final String query = String.format("(svc.value=%s)", value);
            assertQueryTrue(query, rc);

            // Invalid resource
            final ResourceSnapshot invalid = makeResource("test", "bye", value);
            assertQueryFalse(query, invalid);
        }

        // Check case comparison (we are case insensitive)
        ResourceSnapshot rc = makeResource("svc", "value", "TeSt");
        assertQueryTrue("(svc.value=TeSt)", rc);
        assertQueryTrue("(svc.value=test)", rc);

        // Check resource in array
        rc = makeResource("svc", "value", List.of("foo", "bar"));
        assertQueryFalse("(svc.value=foobar)", rc);
        assertQueryTrue("(svc.value=foo)", rc);
        assertQueryTrue("(svc.value=bar)", rc);

        rc = makeResource("svc", "value", new String[] { "foo", "bar" });
        assertQueryFalse("(svc.value=foobar)", rc);
        assertQueryTrue("(svc.value=foo)", rc);
        assertQueryTrue("(svc.value=bar)", rc);

        rc = makeResource("svc", "value", new int[] { 4, 2 });
        assertQueryFalse("(svc.value=42)", rc);
        assertQueryTrue("(svc.value=4)", rc);
        assertQueryTrue("(svc.value=2)", rc);
    }

    @Test
    void testApprox() throws Exception {
        for (Object value : List.of("test", 42, 51L, 43.5)) {
            final ResourceSnapshot rc = makeResource("test", "value", value);
            final String query = String.format("(test.value~=%s)", value);
            assertQueryTrue(query, rc);

            // Invalid resource
            final ResourceSnapshot invalid = makeResource("test", "bye", value);
            assertQueryFalse(query, invalid);
        }

        // Check case comparison
        ResourceSnapshot rc = makeResource("test", "value", "TeSt");
        assertQueryTrue("(test.value~=TeSt)", rc);
        assertQueryTrue("(test.value~=test)", rc);

        // Check float comparison
        rc = makeResource("test", "value", 42);
        assertQueryTrue("(test.value~=42)", rc);
        assertQueryTrue("(test.value~=41.99999)", rc);
        assertQueryTrue("(test.value~=42.00001)", rc);
    }

    @Test
    void testLessEq() throws Exception {
        ResourceSnapshot rc = makeResource("test", "value", 42);
        assertQueryTrue("(test.value<=42)", rc);
        assertQueryTrue("(test.value<=50)", rc);
        assertQueryFalse("(test.value<=41)", rc);

        rc = makeResource("test", "value", "Test");
        assertQueryTrue("(test.value<=y)", rc);
        assertQueryTrue("(test.value<=Y)", rc);
        assertQueryFalse("(test.value<=Abc)", rc);
        assertQueryTrue("(test.value<=abc)", rc);

        rc = makeResource("test", "value", "test");
        assertQueryTrue("(test.value<=y)", rc);
        assertQueryFalse("(test.value<=Y)", rc);
        assertQueryFalse("(test.value<=Abc)", rc);
        assertQueryFalse("(test.value<=abc)", rc);
    }

    @Test
    void testGreaterEq() throws Exception {
        ResourceSnapshot rc = makeResource("test", "value", 42);
        assertQueryTrue("(test.value>=42)", rc);
        assertQueryTrue("(test.value>=41)", rc);
        assertQueryFalse("(test.value>=50)", rc);

        rc = makeResource("test", "value", "Test");
        assertQueryTrue("(test.value>=A)", rc);
        assertQueryFalse("(test.value>=Y)", rc);
        assertQueryFalse("(test.value>=t)", rc);

        rc = makeResource("test", "value", "test");
        assertQueryTrue("(test.value>=a)", rc);
        assertQueryFalse("(test.value>=y)", rc);
        assertQueryTrue("(test.value>=Abc)", rc);
    }

    @Test
    void testComplex() throws Exception {
        ResourceSnapshot rc = makeResource("test", "value", 42);

        // Test AND
        assertQueryTrue("(&(test.value>=40)(test.value<=50))", rc);
        assertQueryFalse("(&(test.value<=40)(test.value>=50))", rc);
        assertQueryFalse("(&(test.value>=40)(test.value>=50))", rc);
        assertQueryFalse("(&(test.value<=40)(test.value<=50))", rc);

        // Test OR
        assertQueryTrue("(|(test.value>=40)(test.value<=50))", rc);
        assertQueryFalse("(|(test.value<=40)(test.value>=50))", rc);
        assertQueryTrue("(|(test.value>=40)(test.value>=50))", rc);
        assertQueryTrue("(|(test.value<=40)(test.value<=50))", rc);
    }

    @Test
    void testValue() throws Exception {
        for (String value : List.of("°C", "a.b.c", "a|b", "a/b")) {
            final ResourceSnapshot rc = makeResource("test", "value", value);
            // Quotes or no quotes
            for (final String pattern : List.of("(test.value=\"%s\")", "(test.value=%s)")) {
                final String query = String.format(pattern, value);
                assertQueryTrue(query, rc);
            }
        }
    }

    /**
     * Test inspired from examples in RFC 4515
     */
    @Test
    void testSyntax() throws Exception {
        String query = "(cn=Babs Jensen)";
        assertQueryTrue(query, makeResource("test", "cn", "Babs Jensen"));
        assertQueryFalse(query, makeResource("test", "cn", "Tim Howes"));

        query = "(!(cn=Tim Howes))";
        assertQueryTrue(query, makeResource("test", "cn", "Babs Jensen"));
        assertQueryFalse(query, makeResource("test", "cn", "Tim Howes"));

        query = "(&(objectClass=Person)(|(sn=Jensen)(cn=Babs J*)))";
        assertQueryTrue(query, makeResource("test", "cn", "Babs Jensen"), makeResource("test", "sn", "Jensen"),
                makeResource("test", "objectClass", "Person"));
        assertQueryTrue(query, makeResource("test", "sn", "Jensen"), makeResource("test", "objectClass", "Person"));
        assertQueryTrue(query, makeResource("test", "sn", "Jensen"), makeResource("test", "objectClass", "Person"));
        assertQueryTrue(query, makeResource("test", "cn", "Tim Howes"), makeResource("test", "sn", "Jensen"),
                makeResource("test", "objectClass", "Person"));

        assertQueryFalse(query, makeResource("test", "cn", "Tim Howes"), makeResource("test", "sn", "Howes"),
                makeResource("test", "objectClass", "Person"));
        assertQueryFalse(query, makeResource("test", "sn", "Howes"), makeResource("test", "objectClass", "Person"));
        assertQueryFalse(query, makeResource("test", "cn", "Tim Howes"), makeResource("test", "objectClass", "Person"));
        assertQueryFalse(query, makeResource("test", "cn", "Tim Howes"), makeResource("test", "sn", "Jensen"),
                makeResource("test", "objectClass", "Student"));

        query = "(o=univ*of*mich*)";
        assertQueryTrue(query, makeResource("test", "o", "university of michigan"));
        assertQueryTrue(query, makeResource("test", "o", "University of Michigan"));
        assertQueryFalse(query, makeResource("test", "o", "Université Grenoble Alpes"));
        assertQueryFalse(query, makeResource("test", "o", "université Grenoble Alpes"));
        assertQueryFalse(query, makeResource("test", "o", "New University of Michigan"));
        assertQueryFalse(query, makeResource("test", "o", "new university of michigan"));

        query = "(o=*of*)";
        assertQueryTrue(query, makeResource("test", "o", "university of michigan"));
        assertQueryTrue(query, makeResource("test", "o", "University of Michigan"));
        assertQueryFalse(query, makeResource("test", "o", "Université Grenoble Alpes"));
        assertQueryFalse(query, makeResource("test", "o", "université Grenoble Alpes"));
        assertQueryTrue(query, makeResource("test", "o", "New University of Michigan"));
        assertQueryTrue(query, makeResource("test", "o", "new university of michigan"));

        query = "(o=univ*gre*)";
        assertQueryFalse(query, makeResource("test", "o", "university of michigan"));
        assertQueryFalse(query, makeResource("test", "o", "University of Michigan"));
        assertQueryTrue(query, makeResource("test", "o", "Université Grenoble Alpes"));
        assertQueryTrue(query, makeResource("test", "o", "université Grenoble Alpes"));
        assertQueryFalse(query, makeResource("test", "o", "New University of Michigan"));
        assertQueryFalse(query, makeResource("test", "o", "new university of michigan"));

        query = "(o=Parens R Us \\28for all your parenthetical needs\\29)";
        assertQueryTrue(query, makeResource("test", "o", "Parens R Us (for all your parenthetical needs)"));

        query = "(cn=*\\2A*)";
        assertQueryTrue(query, makeResource("test", "cn", "Hello*World"));

        query = "(filename=C:\\5cMyFile)";
        assertQueryTrue(query, makeResource("test", "filename", "C:\\MyFile"));
    }

    @Test
    void testSpecificCriteria() throws Exception {
        ResourceSnapshot rc = makeResource("test", "test", 42);
        assertQueryTrue("(MODEL=model1)", rc);
        assertQueryFalse("(MODEL=model2)", rc);
        assertQueryTrue("(MODEL=model*)", rc);

        assertQueryTrue("(PACKAGE=https://eclipse.org/sensinact/test/)", rc);
        assertQueryFalse("(PACKAGE=https://eclipse.org/sensinact/test)", rc);
        assertQueryFalse("(PACKAGE=https://eclipse.org/sensinact/)", rc);
        assertQueryFalse("(PACKAGE=https://eclipse.org/sensinact/test2)", rc);
        assertQueryTrue("(PACKAGE=https://eclipse.org/sensinact/*)", rc);
        assertQueryTrue("(PACKAGE=https://eclipse.org/*)", rc);
        assertQueryTrue("(PACKAGE=*test*)", rc);

        assertQueryTrue("(PROVIDER=provider1)", rc);
        assertQueryFalse("(PROVIDER=provider2)", rc);
        assertQueryTrue("(PROVIDER=provider*)", rc);
    }
}
