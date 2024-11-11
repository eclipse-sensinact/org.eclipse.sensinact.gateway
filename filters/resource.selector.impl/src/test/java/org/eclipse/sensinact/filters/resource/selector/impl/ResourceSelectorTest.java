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
package org.eclipse.sensinact.filters.resource.selector.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.sensinact.core.model.ResourceType;
import org.eclipse.sensinact.core.model.ValueType;
import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceValueFilter;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.filters.resource.selector.api.ResourceSelector;
import org.eclipse.sensinact.filters.resource.selector.api.Selection;
import org.eclipse.sensinact.filters.resource.selector.api.Selection.MatchType;
import org.eclipse.sensinact.filters.resource.selector.api.ValueSelection;
import org.eclipse.sensinact.filters.resource.selector.api.ValueSelection.CheckType;
import org.eclipse.sensinact.filters.resource.selector.api.ValueSelection.OperationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class ResourceSelectorTest {

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
            public Class<?> getType() {
                return Object.class;
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

                    @SuppressWarnings("unchecked")
                    @Override
                    public ResourceSnapshot getResource(String name) {
                        return getRc();
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

                            @SuppressWarnings("unchecked")
                            @Override
                            public ServiceSnapshot getService(String name) {
                                return getSvc();
                            }

                            @SuppressWarnings("unchecked")
                            @Override
                            public ResourceSnapshot getResource(String service, String resource) {
                                return getRc();
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

    private boolean testResource(final ResourceSelector selector, final ResourceSnapshot... rc) {
        final ICriterion criterion = new ResourceSelectorCriterion(selector, false);
        assertNotNull(criterion, "No criterion returned for: " + selector);

        final ResourceValueFilter valueFilter = criterion.getResourceValueFilter();
        assertNotNull(valueFilter, "No resource value filter parsed from " + selector);
        return valueFilter.test(rc[0].getService().getProvider(), Arrays.asList(rc));
    }

    private void assertQueryTrue(final ResourceSelector selector, final ResourceSnapshot... rc) {
        assertTrue(testResource(selector, rc), selector + "; rc:" + Arrays.toString(rc));
    }

    private void assertQueryFalse(final ResourceSelector selector, final ResourceSnapshot... rc) {
        assertFalse(testResource(selector, rc), selector + "; rc:" + Arrays.toString(rc));
    }

    private ResourceSelector makeBasicResourceSelector(String model, String provider, String service, String resource) {
        ResourceSelector rs = new ResourceSelector();
        rs.model = model == null ? null : makeExactSelection(model);
        rs.provider = provider == null ? null :makeExactSelection(provider);
        rs.service = service == null ? null : makeExactSelection(service);
        rs.resource = resource == null ? null : makeExactSelection(resource);
        return rs;
    }

    private Selection makeExactSelection(String name) {
        Selection s = new Selection();
        s.type = MatchType.EXACT;
        s.value = name;
        return s;
    }

    private ValueSelection makeValueSelection(String value, CheckType check, OperationType operation) {
        ValueSelection s = new ValueSelection();
        s.value = value;
        s.operation = operation;
        s.checkType = check;
        return s;
    }

    static Stream<Object> testValues() {
        return Stream.of("test", 42, 51L, 43.5);
    }

    @ParameterizedTest
    @MethodSource("testValues")
    void testPresence(Object value) throws Exception {
        ResourceSnapshot rc = makeResource("test", "hello", value);
        ResourceSelector rs = makeBasicResourceSelector(null, null, "test", "hello");
        rs.value = List.of(makeValueSelection(null, null, OperationType.IS_SET));
        ICriterion filter = new ResourceSelectorCriterion(rs, false);

        // ... value level
        ResourceValueFilter rcPredicate = filter.getResourceValueFilter();
        assertNotNull(rcPredicate);
        assertTrue(rcPredicate.test(rc.getService().getProvider(), List.of(rc)));

        // Invalid resource
        ResourceSnapshot invalid = makeResource("test", "bye", value);
        rcPredicate = filter.getResourceValueFilter();
        assertFalse(rcPredicate.test(invalid.getService().getProvider(), List.of(invalid)));
    }

    @ParameterizedTest
    @MethodSource("testValues")
    void testEquality(Object value) throws Exception {
        ResourceSnapshot rc = makeResource("svc", "value", value);
        ResourceSelector rs = makeBasicResourceSelector(null, null, "svc", "value");
        rs.value = List.of(makeValueSelection(String.valueOf(value), null, OperationType.EQUALS));
        assertQueryTrue(rs, rc);

        // Invalid resource
        final ResourceSnapshot invalid = makeResource("test", "bye", value);
        assertQueryFalse(rs, invalid);

        // Check case comparison (we are case sensitive)
        rc = makeResource("svc", "value", "TeSt");
        rs.value = List.of(makeValueSelection("TeSt", null, OperationType.EQUALS));
        assertQueryTrue(rs, rc);
        rs.value = List.of(makeValueSelection("test", null, OperationType.EQUALS));
        assertQueryFalse(rs, rc);

        // Check resource in array
        rc = makeResource("svc", "value", List.of("foo", "bar"));
        rs.value = List.of(makeValueSelection("foobar", null, OperationType.EQUALS));
        assertQueryFalse(rs, rc);
        rs.value = List.of(makeValueSelection("foo", null, OperationType.EQUALS));
        assertQueryTrue(rs, rc);
        rs.value = List.of(makeValueSelection("bar", null, OperationType.EQUALS));
        assertQueryTrue(rs, rc);

        rc = makeResource("svc", "value", new String[] { "foo", "bar" });
        rs.value = List.of(makeValueSelection("foobar", null, OperationType.EQUALS));
        assertQueryFalse(rs, rc);
        rs.value = List.of(makeValueSelection("foo", null, OperationType.EQUALS));
        assertQueryTrue(rs, rc);
        rs.value = List.of(makeValueSelection("bar", null, OperationType.EQUALS));
        assertQueryTrue(rs, rc);

        rc = makeResource("svc", "value", new int[] { 4, 2 });
        rs.value = List.of(makeValueSelection("42", null, OperationType.EQUALS));
        assertQueryFalse(rs, rc);
        rs.value = List.of(makeValueSelection("4", null, OperationType.EQUALS));
        assertQueryTrue(rs, rc);
        rs.value = List.of(makeValueSelection("2", null, OperationType.EQUALS));
        assertQueryTrue(rs, rc);
    }

    @Test
    void testLessEq() throws Exception {
        ResourceSnapshot rc = makeResource("test", "value", 42);
        ResourceSelector rs = makeBasicResourceSelector(null, null, "test", "value");
        rs.value = List.of(makeValueSelection("42", null, OperationType.LESS_THAN_OR_EQUAL));
        assertQueryTrue(rs, rc);
        rs.value = List.of(makeValueSelection("50", null, OperationType.LESS_THAN_OR_EQUAL));
        assertQueryTrue(rs, rc);
        rs.value = List.of(makeValueSelection("41", null, OperationType.LESS_THAN_OR_EQUAL));
        assertQueryFalse(rs, rc);

        rc = makeResource("test", "value", "Test");
        rs.value = List.of(makeValueSelection("y", null, OperationType.LESS_THAN_OR_EQUAL));
        assertQueryTrue(rs, rc);
        rs.value = List.of(makeValueSelection("Y", null, OperationType.LESS_THAN_OR_EQUAL));
        assertQueryTrue(rs, rc);
        rs.value = List.of(makeValueSelection("Abc", null, OperationType.LESS_THAN_OR_EQUAL));
        assertQueryFalse(rs, rc);
        rs.value = List.of(makeValueSelection("abc", null, OperationType.LESS_THAN_OR_EQUAL));
        assertQueryTrue(rs, rc);
        rs.value = List.of(makeValueSelection("Test", null, OperationType.LESS_THAN_OR_EQUAL));
        assertQueryTrue(rs, rc);
        rs.value = List.of(makeValueSelection("test", null, OperationType.LESS_THAN_OR_EQUAL));
        assertQueryTrue(rs, rc);

        rc = makeResource("test", "value", "test");
        rs.value = List.of(makeValueSelection("y", null, OperationType.LESS_THAN_OR_EQUAL));
        assertQueryTrue(rs, rc);
        rs.value = List.of(makeValueSelection("Y", null, OperationType.LESS_THAN_OR_EQUAL));
        assertQueryFalse(rs, rc);
        rs.value = List.of(makeValueSelection("Abc", null, OperationType.LESS_THAN_OR_EQUAL));
        assertQueryFalse(rs, rc);
        rs.value = List.of(makeValueSelection("abc", null, OperationType.LESS_THAN_OR_EQUAL));
        assertQueryFalse(rs, rc);
        rs.value = List.of(makeValueSelection("Test", null, OperationType.LESS_THAN_OR_EQUAL));
        assertQueryFalse(rs, rc);
        rs.value = List.of(makeValueSelection("test", null, OperationType.LESS_THAN_OR_EQUAL));
        assertQueryTrue(rs, rc);
    }

    @Test
    void testLess() throws Exception {
        ResourceSnapshot rc = makeResource("test", "value", 42);
        ResourceSelector rs = makeBasicResourceSelector(null, null, "test", "value");
        rs.value = List.of(makeValueSelection("42", null, OperationType.LESS_THAN));
        assertQueryFalse(rs, rc);
        rs.value = List.of(makeValueSelection("50", null, OperationType.LESS_THAN));
        assertQueryTrue(rs, rc);
        rs.value = List.of(makeValueSelection("41", null, OperationType.LESS_THAN));
        assertQueryFalse(rs, rc);

        rc = makeResource("test", "value", "Test");
        rs.value = List.of(makeValueSelection("y", null, OperationType.LESS_THAN));
        assertQueryTrue(rs, rc);
        rs.value = List.of(makeValueSelection("Y", null, OperationType.LESS_THAN));
        assertQueryTrue(rs, rc);
        rs.value = List.of(makeValueSelection("Abc", null, OperationType.LESS_THAN));
        assertQueryFalse(rs, rc);
        rs.value = List.of(makeValueSelection("abc", null, OperationType.LESS_THAN));
        assertQueryTrue(rs, rc);
        rs.value = List.of(makeValueSelection("Test", null, OperationType.LESS_THAN));
        assertQueryFalse(rs, rc);
        rs.value = List.of(makeValueSelection("test", null, OperationType.LESS_THAN));
        assertQueryTrue(rs, rc);

        rc = makeResource("test", "value", "test");
        rs.value = List.of(makeValueSelection("y", null, OperationType.LESS_THAN));
        assertQueryTrue(rs, rc);
        rs.value = List.of(makeValueSelection("Y", null, OperationType.LESS_THAN));
        assertQueryFalse(rs, rc);
        rs.value = List.of(makeValueSelection("Abc", null, OperationType.LESS_THAN));
        assertQueryFalse(rs, rc);
        rs.value = List.of(makeValueSelection("abc", null, OperationType.LESS_THAN));
        assertQueryFalse(rs, rc);
        rs.value = List.of(makeValueSelection("Test", null, OperationType.LESS_THAN));
        assertQueryFalse(rs, rc);
        rs.value = List.of(makeValueSelection("test", null, OperationType.LESS_THAN));
        assertQueryFalse(rs, rc);
    }

    @Test
    void testGreaterEq() throws Exception {
        ResourceSnapshot rc = makeResource("test", "value", 42);
        ResourceSelector rs = makeBasicResourceSelector(null, null, "test", "value");
        rs.value = List.of(makeValueSelection("42", null, OperationType.GREATER_THAN_OR_EQUAL));
        assertQueryTrue(rs, rc);
        rs.value = List.of(makeValueSelection("41", null, OperationType.GREATER_THAN_OR_EQUAL));
        assertQueryTrue(rs, rc);
        rs.value = List.of(makeValueSelection("50", null, OperationType.GREATER_THAN_OR_EQUAL));
        assertQueryFalse(rs, rc);

        rc = makeResource("test", "value", "Test");
        rs.value = List.of(makeValueSelection("A", null, OperationType.GREATER_THAN_OR_EQUAL));
        assertQueryTrue(rs, rc);
        rs.value = List.of(makeValueSelection("Y", null, OperationType.GREATER_THAN_OR_EQUAL));
        assertQueryFalse(rs, rc);
        rs.value = List.of(makeValueSelection("t", null, OperationType.GREATER_THAN_OR_EQUAL));
        assertQueryFalse(rs, rc);
        rs.value = List.of(makeValueSelection("Test", null, OperationType.GREATER_THAN_OR_EQUAL));
        assertQueryTrue(rs, rc);
        rs.value = List.of(makeValueSelection("test", null, OperationType.GREATER_THAN_OR_EQUAL));
        assertQueryFalse(rs, rc);

        rc = makeResource("test", "value", "test");
        rs.value = List.of(makeValueSelection("a", null, OperationType.GREATER_THAN_OR_EQUAL));
        assertQueryTrue(rs, rc);
        rs.value = List.of(makeValueSelection("y", null, OperationType.GREATER_THAN_OR_EQUAL));
        assertQueryFalse(rs, rc);
        rs.value = List.of(makeValueSelection("Abc", null, OperationType.GREATER_THAN_OR_EQUAL));
        assertQueryTrue(rs, rc);
        rs.value = List.of(makeValueSelection("Test", null, OperationType.GREATER_THAN_OR_EQUAL));
        assertQueryTrue(rs, rc);
        rs.value = List.of(makeValueSelection("test", null, OperationType.GREATER_THAN_OR_EQUAL));
        assertQueryTrue(rs, rc);
    }

    @Test
    void testGreater() throws Exception {
        ResourceSnapshot rc = makeResource("test", "value", 42);
        ResourceSelector rs = makeBasicResourceSelector(null, null, "test", "value");
        rs.value = List.of(makeValueSelection("42", null, OperationType.GREATER_THAN));
        assertQueryFalse(rs, rc);
        rs.value = List.of(makeValueSelection("41", null, OperationType.GREATER_THAN));
        assertQueryTrue(rs, rc);
        rs.value = List.of(makeValueSelection("50", null, OperationType.GREATER_THAN));
        assertQueryFalse(rs, rc);

        rc = makeResource("test", "value", "Test");
        rs.value = List.of(makeValueSelection("A", null, OperationType.GREATER_THAN));
        assertQueryTrue(rs, rc);
        rs.value = List.of(makeValueSelection("Y", null, OperationType.GREATER_THAN));
        assertQueryFalse(rs, rc);
        rs.value = List.of(makeValueSelection("t", null, OperationType.GREATER_THAN));
        assertQueryFalse(rs, rc);
        rs.value = List.of(makeValueSelection("Test", null, OperationType.GREATER_THAN));
        assertQueryFalse(rs, rc);
        rs.value = List.of(makeValueSelection("test", null, OperationType.GREATER_THAN));
        assertQueryFalse(rs, rc);

        rc = makeResource("test", "value", "test");
        rs.value = List.of(makeValueSelection("a", null, OperationType.GREATER_THAN));
        assertQueryTrue(rs, rc);
        rs.value = List.of(makeValueSelection("y", null, OperationType.GREATER_THAN));
        assertQueryFalse(rs, rc);
        rs.value = List.of(makeValueSelection("Abc", null, OperationType.GREATER_THAN));
        assertQueryTrue(rs, rc);
        rs.value = List.of(makeValueSelection("Test", null, OperationType.GREATER_THAN));
        assertQueryTrue(rs, rc);
        rs.value = List.of(makeValueSelection("test", null, OperationType.GREATER_THAN));
        assertQueryFalse(rs, rc);
    }

    @Test
    void testRegex() throws Exception {
        ResourceSnapshot rc = makeResource("svc", "value", "bb");
        ResourceSelector rs = makeBasicResourceSelector(null, null, "svc", "value");
        rs.value = List.of(makeValueSelection("a?b+", null, OperationType.REGEX));
        assertQueryTrue(rs, rc);
        rs.value = List.of(makeValueSelection("a+b+", null, OperationType.REGEX));
        assertQueryFalse(rs, rc);

        rc = makeResource("svc", "value", "aabbaabbb");
        rs.value = List.of(makeValueSelection("a?b+", null, OperationType.REGEX));
        assertQueryFalse(rs, rc);
        rs.value = List.of(makeValueSelection("a+b+", null, OperationType.REGEX));
        assertQueryFalse(rs, rc);
        rs.value = List.of(makeValueSelection("a?b+", null, OperationType.REGEX_REGION));
        assertQueryTrue(rs, rc);
        rs.value = List.of(makeValueSelection("a+b+", null, OperationType.REGEX_REGION));
        assertQueryTrue(rs, rc);
        rs.value = List.of(makeValueSelection("ab+a+b", null, OperationType.REGEX_REGION));
        assertQueryTrue(rs, rc);
    }
}
