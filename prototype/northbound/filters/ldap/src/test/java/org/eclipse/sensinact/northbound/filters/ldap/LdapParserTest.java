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
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.northbound.filters.ldap.impl.LdapParser;
import org.eclipse.sensinact.prototype.snapshot.ICriterion;
import org.eclipse.sensinact.prototype.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.prototype.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.prototype.snapshot.ResourceValueFilter;
import org.eclipse.sensinact.prototype.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.prototype.twin.TimedValue;
import org.junit.jupiter.api.Test;

public class LdapParserTest {

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
                            public String getModelName() {
                                return "model1";
                            }
                        };
                    }
                };
            }
        };
    }

    @Test
    void testPresence() throws Exception {
        for (Object value : List.of("test", 42, 51L, 43.5)) {
            ResourceSnapshot rc = makeResource("test", "hello", value);
            ICriterion filter = LdapParser.parse("(test.hello=*)");

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
            ResourceSnapshot rc = makeResource("svc", "value", value);
            ICriterion filter = LdapParser.parse(String.format("(svc.value=%s)", value));

            // ... value level
            ResourceValueFilter rcPredicate = filter.getResourceValueFilter();
            assertNotNull(rcPredicate);
            assertTrue(rcPredicate.test(rc.getService().getProvider(), List.of(rc)));

            // Invalid resource
            ResourceSnapshot invalid = makeResource("test", "bye", value);

            // ... value level
            rcPredicate = filter.getResourceValueFilter();
            assertFalse(rcPredicate.test(invalid.getService().getProvider(), List.of(invalid)));
        }

        // Check case comparison
        ResourceSnapshot rc = makeResource("svc", "value", "TeSt");
        assertTrue(LdapParser.parse("(svc.value=TeSt)").getResourceValueFilter().test(rc.getService().getProvider(),
                List.of(rc)));
        assertFalse(LdapParser.parse("(svc.value=test)").getResourceValueFilter().test(rc.getService().getProvider(),
                List.of(rc)));
    }

    @Test
    void testApprox() throws Exception {
        for (Object value : List.of("test", 42, 51L, 43.5)) {
            ResourceSnapshot rc = makeResource("test", "value", value);
            ICriterion filter = LdapParser.parse(String.format("(test.value~=%s)", value));

            // ... value level
            ResourceValueFilter rcPredicate = filter.getResourceValueFilter();
            assertNotNull(rcPredicate);
            assertTrue(rcPredicate.test(rc.getService().getProvider(), List.of(rc)));

            // Invalid resource
            ResourceSnapshot invalid = makeResource("test", "bye", value);

            // ... value level
            rcPredicate = filter.getResourceValueFilter();
            assertFalse(rcPredicate.test(invalid.getService().getProvider(), List.of(invalid)));
        }

        // Check case comparison
        ResourceSnapshot rc = makeResource("test", "value", "TeSt");
        assertTrue(LdapParser.parse("(test.value~=TeSt)").getResourceValueFilter().test(rc.getService().getProvider(),
                List.of(rc)));
        assertTrue(LdapParser.parse("(test.value~=test)").getResourceValueFilter().test(rc.getService().getProvider(),
                List.of(rc)));

        // Check float comparison
        rc = makeResource("test", "value", 42);
        assertTrue(LdapParser.parse("(test.value~=42)").getResourceValueFilter().test(rc.getService().getProvider(),
                List.of(rc)));
        assertTrue(LdapParser.parse("(test.value~=41.99999)").getResourceValueFilter()
                .test(rc.getService().getProvider(), List.of(rc)));
        assertTrue(LdapParser.parse("(test.value~=42.00001)").getResourceValueFilter()
                .test(rc.getService().getProvider(), List.of(rc)));
    }

    @Test
    void testLessEq() throws Exception {
        ResourceSnapshot rc = makeResource("test", "value", 42);
        assertTrue(LdapParser.parse("(test.value<=42)").getResourceValueFilter().test(rc.getService().getProvider(),
                List.of(rc)));
        assertTrue(LdapParser.parse("(test.value<=50)").getResourceValueFilter().test(rc.getService().getProvider(),
                List.of(rc)));
        assertFalse(LdapParser.parse("(test.value<=41)").getResourceValueFilter().test(rc.getService().getProvider(),
                List.of(rc)));

        rc = makeResource("test", "value", "Test");
        assertTrue(LdapParser.parse("(test.value<=y)").getResourceValueFilter().test(rc.getService().getProvider(),
                List.of(rc)));
        assertTrue(LdapParser.parse("(test.value<=Y)").getResourceValueFilter().test(rc.getService().getProvider(),
                List.of(rc)));
        assertFalse(LdapParser.parse("(test.value<=Abc)").getResourceValueFilter().test(rc.getService().getProvider(),
                List.of(rc)));
        assertTrue(LdapParser.parse("(test.value<=abc)").getResourceValueFilter().test(rc.getService().getProvider(),
                List.of(rc)));

        rc = makeResource("test", "value", "test");
        assertTrue(LdapParser.parse("(test.value<=y)").getResourceValueFilter().test(rc.getService().getProvider(),
                List.of(rc)));
        assertFalse(LdapParser.parse("(test.value<=Y)").getResourceValueFilter().test(rc.getService().getProvider(),
                List.of(rc)));
        assertFalse(LdapParser.parse("(test.value<=Abc)").getResourceValueFilter().test(rc.getService().getProvider(),
                List.of(rc)));
        assertFalse(LdapParser.parse("(test.value<=abc)").getResourceValueFilter().test(rc.getService().getProvider(),
                List.of(rc)));
    }

    @Test
    void testGreaterEq() throws Exception {
        ResourceSnapshot rc = makeResource("test", "value", 42);
        assertTrue(LdapParser.parse("(test.value>=42)").getResourceValueFilter().test(rc.getService().getProvider(),
                List.of(rc)));
        assertTrue(LdapParser.parse("(test.value>=41)").getResourceValueFilter().test(rc.getService().getProvider(),
                List.of(rc)));
        assertFalse(LdapParser.parse("(test.value>=50)").getResourceValueFilter().test(rc.getService().getProvider(),
                List.of(rc)));

        rc = makeResource("test", "value", "Test");
        assertTrue(LdapParser.parse("(test.value>=A)").getResourceValueFilter().test(rc.getService().getProvider(),
                List.of(rc)));
        assertFalse(LdapParser.parse("(test.value>=Y)").getResourceValueFilter().test(rc.getService().getProvider(),
                List.of(rc)));
        assertFalse(LdapParser.parse("(test.value>=t)").getResourceValueFilter().test(rc.getService().getProvider(),
                List.of(rc)));

        rc = makeResource("test", "value", "test");
        assertTrue(LdapParser.parse("(test.value>=a)").getResourceValueFilter().test(rc.getService().getProvider(),
                List.of(rc)));
        assertFalse(LdapParser.parse("(test.value>=y)").getResourceValueFilter().test(rc.getService().getProvider(),
                List.of(rc)));
        assertTrue(LdapParser.parse("(test.value>=Abc)").getResourceValueFilter().test(rc.getService().getProvider(),
                List.of(rc)));
    }

    @Test
    void testComplex() throws Exception {
        ResourceSnapshot rc = makeResource("test", "value", 42);
        assertTrue(LdapParser.parse("(&(test.value>=40)(test.value<=50))").getResourceValueFilter()
                .test(rc.getService().getProvider(), List.of(rc)));
        assertFalse(LdapParser.parse("(&(test.value<=40)(test.value>=50))").getResourceValueFilter()
                .test(rc.getService().getProvider(), List.of(rc)));
        assertFalse(LdapParser.parse("(&(test.value>=40)(test.value>=50))").getResourceValueFilter()
                .test(rc.getService().getProvider(), List.of(rc)));
        assertFalse(LdapParser.parse("(&(test.value<=40)(test.value<=50))").getResourceValueFilter()
                .test(rc.getService().getProvider(), List.of(rc)));

        assertTrue(LdapParser.parse("(|(test.value>=40)(test.value<=50))").getResourceValueFilter()
                .test(rc.getService().getProvider(), List.of(rc)));
        assertFalse(LdapParser.parse("(|(test.value<=40)(test.value>=50))").getResourceValueFilter()
                .test(rc.getService().getProvider(), List.of(rc)));
        assertTrue(LdapParser.parse("(|(test.value>=40)(test.value>=50))").getResourceValueFilter()
                .test(rc.getService().getProvider(), List.of(rc)));
        assertTrue(LdapParser.parse("(|(test.value<=40)(test.value<=50))").getResourceValueFilter()
                .test(rc.getService().getProvider(), List.of(rc)));
    }

    @Test
    void testValue() throws Exception {
        for (String value : List.of("°C", "a.b.c", "a*b.c", "a*b*c", "a|b", "a/b")) {
            ResourceSnapshot rc = makeResource("test", "value", value);
            assertTrue(LdapParser.parse(String.format("(test.value=%s)", value)).getResourceValueFilter()
                    .test(rc.getService().getProvider(), List.of(rc)));
        }
    }
}
