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

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.eclipse.sensinact.core.snapshot.LinkedProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceValueFilter;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.DefaultTimedValue;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.filters.resource.selector.api.ResourceSelector;
import org.eclipse.sensinact.filters.resource.selector.api.ResourceSelector.ProviderSelection;
import org.eclipse.sensinact.filters.resource.selector.api.ResourceSelector.ResourceSelection;
import org.eclipse.sensinact.filters.resource.selector.api.Selection;
import org.eclipse.sensinact.filters.resource.selector.api.Selection.MatchType;
import org.eclipse.sensinact.filters.resource.selector.api.ValueSelection;
import org.eclipse.sensinact.filters.resource.selector.api.ValueSelection.CheckType;
import org.eclipse.sensinact.filters.resource.selector.api.ValueSelection.OperationType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class ResourceSelectorTest {

    ResourceSnapshot makeResource(final String svcName, final String rcName, final Object value) {
        return makeResource(svcName, rcName, value, false);
    }

    ResourceSnapshot makeResource(final String svcName, final String rcName, final Object value, boolean unset) {
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
                return unset ? new DefaultTimedValue<>() : new DefaultTimedValue<>(value);
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

                            @Override
                            public List<LinkedProviderSnapshot> getLinkedProviders() {
                                // No support for linked providers
                                return List.of();
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
        List<ResourceSelection> resources = List.of(new ResourceSelection(service == null ? null : makeExactSelection(service),
                resource == null ? null : makeExactSelection(resource), List.of()));
        ProviderSelection ps = new ProviderSelection(null,
                model == null ? null : makeExactSelection(model),
                provider == null ? null : makeExactSelection(provider),
                resources, List.of());
        return new ResourceSelector(List.of(ps), List.of());
    }

    private Selection makeExactSelection(String name) {
        return new Selection(name, MatchType.EXACT, false);
    }

    private ValueSelection makeValueSelection(String value, CheckType check, OperationType operation) {
        return makeValueSelection(value, check, operation, false);
    }

    private ValueSelection makeValueSelection(String value, CheckType check, OperationType operation, boolean negate) {
        return new ValueSelection(value, operation, negate, check);
    }

    private ResourceSelector updateValueTest(ResourceSelector rs, ValueSelection vs) {
        ProviderSelection existingP = rs.providers().get(0);
        ResourceSelection exisitingR = existingP.resources().get(0);
        ProviderSelection newer = new ProviderSelection(existingP.modelUri(), existingP.model(), existingP.provider(),
                List.of(new ResourceSelection(exisitingR.service(), exisitingR.resource(), List.of(vs))), existingP.location());
        return new ResourceSelector(List.of(newer), rs.resources());
    }

    static Stream<Object> testValues() {
        return Stream.of("test", 42, 51L, 43.5);
    }

    @ParameterizedTest
    @MethodSource("testValues")
    void testPresence(Object value) throws Exception {
        ResourceSnapshot rc = makeResource("test", "hello", value);
        ResourceSelector rs = makeBasicResourceSelector(null, null, "test", "hello");
        rs = updateValueTest(rs, makeValueSelection(null, null, OperationType.IS_SET));
        ICriterion filter = new ResourceSelectorCriterion(rs, false);
        ResourceSelector rsNeg = makeBasicResourceSelector(null, null, "test", "hello");
        rsNeg = updateValueTest(rs, makeValueSelection(null, null, OperationType.IS_SET, true));
        ICriterion filterNeg = new ResourceSelectorCriterion(rsNeg, false);

        // ... value level
        ResourceValueFilter rcPredicate = filter.getResourceValueFilter();
        ResourceValueFilter rcPredicateNeg = filterNeg.getResourceValueFilter();
        assertNotNull(rcPredicate);
        assertNotNull(rcPredicateNeg);
        assertTrue(rcPredicate.test(rc.getService().getProvider(), List.of(rc)));
        assertFalse(rcPredicateNeg.test(rc.getService().getProvider(), List.of(rc)));

        // Invalid resource
        ResourceSnapshot invalid = makeResource("test", "bye", value);
        assertFalse(rcPredicate.test(invalid.getService().getProvider(), List.of(invalid)));
        assertFalse(rcPredicateNeg.test(invalid.getService().getProvider(), List.of(invalid)));

        // Unset resource
        ResourceSnapshot unset = makeResource("test", "hello", null, true);
        assertFalse(rcPredicate.test(unset.getService().getProvider(), List.of(unset)));
        assertTrue(rcPredicateNeg.test(unset.getService().getProvider(), List.of(unset)));
    }

    @ParameterizedTest
    @MethodSource("testValues")
    void testEquals(Object value) throws Exception {
        ResourceSnapshot rc = makeResource("test", "hello", value);
        ResourceSelector rs = makeBasicResourceSelector(null, null, "test", "hello");
        // Test for value
        rs = updateValueTest(rs, makeValueSelection(String.valueOf(value), null, OperationType.EQUALS));
        ICriterion filter = new ResourceSelectorCriterion(rs, false);
        ResourceSelector rsWrong = makeBasicResourceSelector(null, null, "test", "hello");
        // Test for wrong value
        rsWrong = updateValueTest(rs, makeValueSelection("wrong", null, OperationType.EQUALS));
        ICriterion filterWrong = new ResourceSelectorCriterion(rsWrong, false);

        // ... value level
        ResourceValueFilter rcPredicate = filter.getResourceValueFilter();
        assertNotNull(rcPredicate);
        ResourceValueFilter rcPredicateWrong = filterWrong.getResourceValueFilter();
        assertNotNull(rcPredicate);
        assertNotNull(rcPredicateWrong);
        assertTrue(rcPredicate.test(rc.getService().getProvider(), List.of(rc)));
        assertFalse(rcPredicateWrong.test(rc.getService().getProvider(), List.of(rc)));

        // Invalid resource
        ResourceSnapshot invalid = makeResource("test", "bye", null);
        assertFalse(rcPredicate.test(invalid.getService().getProvider(), List.of(invalid)));
        assertFalse(rcPredicateWrong.test(invalid.getService().getProvider(), List.of(invalid)));

        // Unset resource
        ResourceSnapshot unset = makeResource("test", "hello", null, true);
        assertFalse(rcPredicate.test(unset.getService().getProvider(), List.of(unset)));
        assertFalse(rcPredicateWrong.test(unset.getService().getProvider(), List.of(unset)));

        // Null resource
        ResourceSnapshot nil = makeResource("test", "hello", null, false);
        assertFalse(rcPredicate.test(nil.getService().getProvider(), List.of(nil)));
        assertFalse(rcPredicateWrong.test(nil.getService().getProvider(), List.of(nil)));
    }

    @ParameterizedTest
    @MethodSource("testValues")
    void testEqualsNegated(Object value) throws Exception {
        ResourceSnapshot rc = makeResource("test", "hello", value);
        ResourceSelector rs = makeBasicResourceSelector(null, null, "test", "hello");
        // Test for value
        rs = updateValueTest(rs, makeValueSelection(String.valueOf(value), null, OperationType.EQUALS, true));
        ICriterion filter = new ResourceSelectorCriterion(rs, false);
        ResourceSelector rsWrong = makeBasicResourceSelector(null, null, "test", "hello");
        // Test for wrong value
        rsWrong = updateValueTest(rs, makeValueSelection("wrong", null, OperationType.EQUALS, true));
        ICriterion filterWrong = new ResourceSelectorCriterion(rsWrong, false);

        // ... value level
        ResourceValueFilter rcPredicate = filter.getResourceValueFilter();
        assertNotNull(rcPredicate);
        ResourceValueFilter rcPredicateWrong = filterWrong.getResourceValueFilter();
        assertNotNull(rcPredicate);
        assertNotNull(rcPredicateWrong);
        assertFalse(rcPredicate.test(rc.getService().getProvider(), List.of(rc)));
        assertTrue(rcPredicateWrong.test(rc.getService().getProvider(), List.of(rc)));

        // Invalid resource
        ResourceSnapshot invalid = makeResource("test", "bye", null);
        assertFalse(rcPredicate.test(invalid.getService().getProvider(), List.of(invalid)));
        assertFalse(rcPredicateWrong.test(invalid.getService().getProvider(), List.of(invalid)));

        // Unset resource
        ResourceSnapshot unset = makeResource("test", "hello", null, true);
        assertFalse(rcPredicate.test(unset.getService().getProvider(), List.of(unset)));
        assertFalse(rcPredicateWrong.test(unset.getService().getProvider(), List.of(unset)));

        // Null resource
        ResourceSnapshot nil = makeResource("test", "hello", null, false);
        assertFalse(rcPredicate.test(nil.getService().getProvider(), List.of(nil)));
        assertFalse(rcPredicateWrong.test(nil.getService().getProvider(), List.of(nil)));
    }

    @Test
    void testIsNotNull() throws Exception {
        ResourceSnapshot rc = makeResource("test", "hello", null);
        ResourceSelector rs = makeBasicResourceSelector(null, null, "test", "hello");
        // Test for null value (should always be false)
        rs = updateValueTest(rs, makeValueSelection(null, null, OperationType.IS_NOT_NULL));
        ICriterion filter = new ResourceSelectorCriterion(rs, false);

        // ... value level
        ResourceValueFilter rcPredicate = filter.getResourceValueFilter();
        assertNotNull(rcPredicate);
        assertFalse(rcPredicate.test(rc.getService().getProvider(), List.of(rc)));

        // Invalid resource
        ResourceSnapshot invalid = makeResource("test", "bye", null);
        rcPredicate = filter.getResourceValueFilter();
        assertFalse(rcPredicate.test(invalid.getService().getProvider(), List.of(invalid)));

        // ... non-null values
        for (Object value : testValues().toList()) {
            assertTrue(rcPredicate.test(rc.getService().getProvider(), List.of(makeResource("test", "hello", value))));
        }
    }

    void testEquality() throws Exception {
        ResourceSelector rs = makeBasicResourceSelector(null, null, "svc", "value");
        // Check case comparison (we are case sensitive)
        ResourceSnapshot rc = makeResource("svc", "value", "TeSt");
        rs = updateValueTest(rs, makeValueSelection("TeSt", null, OperationType.EQUALS));
        assertQueryTrue(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("test", null, OperationType.EQUALS));
        assertQueryFalse(rs, rc);

        // Check resource in array
        rc = makeResource("svc", "value", List.of("foo", "bar"));
        rs = updateValueTest(rs, makeValueSelection("foobar", null, OperationType.EQUALS));
        assertQueryFalse(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("foo", null, OperationType.EQUALS));
        assertQueryTrue(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("bar", null, OperationType.EQUALS));
        assertQueryTrue(rs, rc);

        rc = makeResource("svc", "value", new String[] { "foo", "bar" });
        rs = updateValueTest(rs, makeValueSelection("foobar", null, OperationType.EQUALS));
        assertQueryFalse(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("foo", null, OperationType.EQUALS));
        assertQueryTrue(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("bar", null, OperationType.EQUALS));
        assertQueryTrue(rs, rc);

        rc = makeResource("svc", "value", new int[] { 4, 2 });
        rs = updateValueTest(rs, makeValueSelection("42", null, OperationType.EQUALS));
        assertQueryFalse(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("4", null, OperationType.EQUALS));
        assertQueryTrue(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("2", null, OperationType.EQUALS));
        assertQueryTrue(rs, rc);
    }

    @Test
    void testLessEq() throws Exception {
        ResourceSnapshot rc = makeResource("test", "value", 42);
        ResourceSelector rs = makeBasicResourceSelector(null, null, "test", "value");
        rs = updateValueTest(rs, makeValueSelection("42", null, OperationType.LESS_THAN_OR_EQUAL));
        assertQueryTrue(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("50", null, OperationType.LESS_THAN_OR_EQUAL));
        assertQueryTrue(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("41", null, OperationType.LESS_THAN_OR_EQUAL));
        assertQueryFalse(rs, rc);

        rc = makeResource("test", "value", "Test");
        rs = updateValueTest(rs, makeValueSelection("y", null, OperationType.LESS_THAN_OR_EQUAL));
        assertQueryTrue(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("Y", null, OperationType.LESS_THAN_OR_EQUAL));
        assertQueryTrue(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("Abc", null, OperationType.LESS_THAN_OR_EQUAL));
        assertQueryFalse(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("abc", null, OperationType.LESS_THAN_OR_EQUAL));
        assertQueryTrue(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("Test", null, OperationType.LESS_THAN_OR_EQUAL));
        assertQueryTrue(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("test", null, OperationType.LESS_THAN_OR_EQUAL));
        assertQueryTrue(rs, rc);

        rc = makeResource("test", "value", "test");
        rs = updateValueTest(rs, makeValueSelection("y", null, OperationType.LESS_THAN_OR_EQUAL));
        assertQueryTrue(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("Y", null, OperationType.LESS_THAN_OR_EQUAL));
        assertQueryFalse(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("Abc", null, OperationType.LESS_THAN_OR_EQUAL));
        assertQueryFalse(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("abc", null, OperationType.LESS_THAN_OR_EQUAL));
        assertQueryFalse(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("Test", null, OperationType.LESS_THAN_OR_EQUAL));
        assertQueryFalse(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("test", null, OperationType.LESS_THAN_OR_EQUAL));
        assertQueryTrue(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("test", null, OperationType.LESS_THAN_OR_EQUAL, true));
        assertQueryFalse(rs, rc);

        // Null is always false
        rc = makeResource("test", "value", null);
        assertQueryFalse(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("test", null, OperationType.LESS_THAN_OR_EQUAL));
        assertQueryFalse(rs, rc);

        // Unset is always false
        rc = makeResource("test", "value", null, true);
        assertQueryFalse(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("test", null, OperationType.LESS_THAN_OR_EQUAL, true));
        assertQueryFalse(rs, rc);
    }

    @Test
    void testLess() throws Exception {
        ResourceSnapshot rc = makeResource("test", "value", 42);
        ResourceSelector rs = makeBasicResourceSelector(null, null, "test", "value");
        rs = updateValueTest(rs, makeValueSelection("42", null, OperationType.LESS_THAN));
        assertQueryFalse(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("50", null, OperationType.LESS_THAN));
        assertQueryTrue(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("41", null, OperationType.LESS_THAN));
        assertQueryFalse(rs, rc);

        rc = makeResource("test", "value", "Test");
        rs = updateValueTest(rs, makeValueSelection("y", null, OperationType.LESS_THAN));
        assertQueryTrue(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("Y", null, OperationType.LESS_THAN));
        assertQueryTrue(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("Abc", null, OperationType.LESS_THAN));
        assertQueryFalse(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("abc", null, OperationType.LESS_THAN));
        assertQueryTrue(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("Test", null, OperationType.LESS_THAN));
        assertQueryFalse(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("test", null, OperationType.LESS_THAN));
        assertQueryTrue(rs, rc);

        rc = makeResource("test", "value", "test");
        rs = updateValueTest(rs, makeValueSelection("y", null, OperationType.LESS_THAN));
        assertQueryTrue(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("Y", null, OperationType.LESS_THAN));
        assertQueryFalse(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("Abc", null, OperationType.LESS_THAN));
        assertQueryFalse(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("abc", null, OperationType.LESS_THAN));
        assertQueryFalse(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("Test", null, OperationType.LESS_THAN));
        assertQueryFalse(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("test", null, OperationType.LESS_THAN));
        assertQueryFalse(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("test", null, OperationType.LESS_THAN, true));
        assertQueryTrue(rs, rc);

        // Null is always false
        rc = makeResource("test", "value", null);
        assertQueryFalse(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("test", null, OperationType.LESS_THAN));
        assertQueryFalse(rs, rc);

        // Unset is always false
        rc = makeResource("test", "value", null, true);
        assertQueryFalse(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("test", null, OperationType.LESS_THAN, true));
        assertQueryFalse(rs, rc);
    }

    @Test
    void testGreaterEq() throws Exception {
        ResourceSnapshot rc = makeResource("test", "value", 42);
        ResourceSelector rs = makeBasicResourceSelector(null, null, "test", "value");
        rs = updateValueTest(rs, makeValueSelection("42", null, OperationType.GREATER_THAN_OR_EQUAL));
        assertQueryTrue(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("41", null, OperationType.GREATER_THAN_OR_EQUAL));
        assertQueryTrue(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("50", null, OperationType.GREATER_THAN_OR_EQUAL));
        assertQueryFalse(rs, rc);

        rc = makeResource("test", "value", "Test");
        rs = updateValueTest(rs, makeValueSelection("A", null, OperationType.GREATER_THAN_OR_EQUAL));
        assertQueryTrue(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("Y", null, OperationType.GREATER_THAN_OR_EQUAL));
        assertQueryFalse(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("t", null, OperationType.GREATER_THAN_OR_EQUAL));
        assertQueryFalse(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("Test", null, OperationType.GREATER_THAN_OR_EQUAL));
        assertQueryTrue(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("test", null, OperationType.GREATER_THAN_OR_EQUAL));
        assertQueryFalse(rs, rc);

        rc = makeResource("test", "value", "test");
        rs = updateValueTest(rs, makeValueSelection("a", null, OperationType.GREATER_THAN_OR_EQUAL));
        assertQueryTrue(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("y", null, OperationType.GREATER_THAN_OR_EQUAL));
        assertQueryFalse(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("Abc", null, OperationType.GREATER_THAN_OR_EQUAL));
        assertQueryTrue(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("Test", null, OperationType.GREATER_THAN_OR_EQUAL));
        assertQueryTrue(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("test", null, OperationType.GREATER_THAN_OR_EQUAL));
        assertQueryTrue(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("test", null, OperationType.GREATER_THAN_OR_EQUAL, true));
        assertQueryFalse(rs, rc);

        // Null is always false
        rc = makeResource("test", "value", null);
        assertQueryFalse(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("test", null, OperationType.GREATER_THAN_OR_EQUAL));
        assertQueryFalse(rs, rc);

        // Unset is always false
        rc = makeResource("test", "value", null, true);
        assertQueryFalse(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("test", null, OperationType.GREATER_THAN_OR_EQUAL, true));
        assertQueryFalse(rs, rc);
    }

    @Test
    void testGreater() throws Exception {
        ResourceSnapshot rc = makeResource("test", "value", 42);
        ResourceSelector rs = makeBasicResourceSelector(null, null, "test", "value");
        rs = updateValueTest(rs, makeValueSelection("42", null, OperationType.GREATER_THAN));
        assertQueryFalse(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("41", null, OperationType.GREATER_THAN));
        assertQueryTrue(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("50", null, OperationType.GREATER_THAN));
        assertQueryFalse(rs, rc);

        rc = makeResource("test", "value", "Test");
        rs = updateValueTest(rs, makeValueSelection("A", null, OperationType.GREATER_THAN));
        assertQueryTrue(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("Y", null, OperationType.GREATER_THAN));
        assertQueryFalse(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("t", null, OperationType.GREATER_THAN));
        assertQueryFalse(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("Test", null, OperationType.GREATER_THAN));
        assertQueryFalse(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("test", null, OperationType.GREATER_THAN));
        assertQueryFalse(rs, rc);

        rc = makeResource("test", "value", "test");
        rs = updateValueTest(rs, makeValueSelection("a", null, OperationType.GREATER_THAN));
        assertQueryTrue(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("y", null, OperationType.GREATER_THAN));
        assertQueryFalse(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("Abc", null, OperationType.GREATER_THAN));
        assertQueryTrue(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("Test", null, OperationType.GREATER_THAN));
        assertQueryTrue(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("test", null, OperationType.GREATER_THAN));
        assertQueryFalse(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("test", null, OperationType.GREATER_THAN, true));
        assertQueryTrue(rs, rc);

        // Null is always false
        rc = makeResource("test", "value", null);
        assertQueryFalse(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("test", null, OperationType.GREATER_THAN));
        assertQueryFalse(rs, rc);

        // Unset is always false
        rc = makeResource("test", "value", null, true);
        assertQueryFalse(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("test", null, OperationType.GREATER_THAN, true));
        assertQueryFalse(rs, rc);
    }

    @Test
    void testRegex() throws Exception {
        ResourceSnapshot rc = makeResource("svc", "value", "bb");
        ResourceSelector rs = makeBasicResourceSelector(null, null, "svc", "value");
        rs = updateValueTest(rs, makeValueSelection("a?b+", null, OperationType.REGEX));
        assertQueryTrue(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("a+b+", null, OperationType.REGEX));
        assertQueryFalse(rs, rc);

        rc = makeResource("svc", "value", "aabbaabbb");
        rs = updateValueTest(rs, makeValueSelection("a?b+", null, OperationType.REGEX));
        assertQueryFalse(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("a+b+", null, OperationType.REGEX));
        assertQueryFalse(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("a?b+", null, OperationType.REGEX_REGION));
        assertQueryTrue(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("a+b+", null, OperationType.REGEX_REGION));
        assertQueryTrue(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("ab+a+b", null, OperationType.REGEX_REGION));
        assertQueryTrue(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("ab+a+b", null, OperationType.REGEX_REGION, true));
        assertQueryFalse(rs, rc);

        // Null is always false
        rc = makeResource("test", "value", null);
        assertQueryFalse(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("ab+a+b", null, OperationType.REGEX_REGION));
        assertQueryFalse(rs, rc);

        // Unset is always false
        rc = makeResource("test", "value", null, true);
        assertQueryFalse(rs, rc);
        rs = updateValueTest(rs, makeValueSelection("ab+a+b", null, OperationType.REGEX_REGION, true));
        assertQueryFalse(rs, rc);
    }

    @Nested
    class TopicFilterTests {

        ICriterion makeCriterion(ResourceSelector rs, boolean allowSingleLevelWildcard) {
            return new ResourceSelectorCriterion(rs, allowSingleLevelWildcard);
        }

        @Test
        void testNoSingleLevel() {
            assertEquals(List.of("DATA/a/b/c/d"),
                    makeCriterion(makeBasicResourceSelector("a", "b", "c", "d"), false).dataTopics());
        }

        @Test
        void testSingleLevel() {
            assertEquals(List.of("DATA/a/b/c/d"),
                    makeCriterion(makeBasicResourceSelector("a", "b", "c", "d"), true).dataTopics());
        }

        @Test
        void testNoSingleLevelNoModel() {
            assertEquals(List.of("DATA/*"),
                    makeCriterion(makeBasicResourceSelector(null, null, null, null), false).dataTopics());
            assertEquals(List.of("DATA/*"),
                    makeCriterion(makeBasicResourceSelector(null, "b", null, null), false).dataTopics());
            assertEquals(List.of("DATA/*"),
                    makeCriterion(makeBasicResourceSelector(null, "b", "c", null), false).dataTopics());
            assertEquals(List.of("DATA/*"),
                    makeCriterion(makeBasicResourceSelector(null, "b", "c", "d"), false).dataTopics());
            assertEquals(List.of("DATA/*"),
                    makeCriterion(makeBasicResourceSelector(null, "b", null, "d"), false).dataTopics());
            assertEquals(List.of("DATA/*"),
                    makeCriterion(makeBasicResourceSelector(null, null, "c", "d"), false).dataTopics());
            assertEquals(List.of("DATA/*"),
                    makeCriterion(makeBasicResourceSelector(null, null, null, "d"), false).dataTopics());
        }

        @Test
        void testSingleLevelNoModel() {
            assertEquals(List.of("DATA/+/+/+/+"),
                    makeCriterion(makeBasicResourceSelector(null, null, null, null), true).dataTopics());
            assertEquals(List.of("DATA/+/b/+/+"),
                    makeCriterion(makeBasicResourceSelector(null, "b", null, null), true).dataTopics());
            assertEquals(List.of("DATA/+/b/c/+"),
                    makeCriterion(makeBasicResourceSelector(null, "b", "c", null), true).dataTopics());
            assertEquals(List.of("DATA/+/b/c/d"),
                    makeCriterion(makeBasicResourceSelector(null, "b", "c", "d"), true).dataTopics());
            assertEquals(List.of("DATA/+/b/+/d"),
                    makeCriterion(makeBasicResourceSelector(null, "b", null, "d"), true).dataTopics());
            assertEquals(List.of("DATA/+/+/c/d"),
                    makeCriterion(makeBasicResourceSelector(null, null, "c", "d"), true).dataTopics());
            assertEquals(List.of("DATA/+/+/+/d"),
                    makeCriterion(makeBasicResourceSelector(null, null, null, "d"), true).dataTopics());
        }

        @Test
        void testNoSingleLevelNoProvider() {
            assertEquals(List.of("DATA/*"),
                    makeCriterion(makeBasicResourceSelector(null, null, null, null), false).dataTopics());
            assertEquals(List.of("DATA/a/*"),
                    makeCriterion(makeBasicResourceSelector("a", null, null, null), false).dataTopics());
            assertEquals(List.of("DATA/a/*"),
                    makeCriterion(makeBasicResourceSelector("a", null, "c", null), false).dataTopics());
            assertEquals(List.of("DATA/a/*"),
                    makeCriterion(makeBasicResourceSelector("a", null, "c", "d"), false).dataTopics());
            assertEquals(List.of("DATA/a/*"),
                    makeCriterion(makeBasicResourceSelector("a", null, null, "d"), false).dataTopics());
            assertEquals(List.of("DATA/*"),
                    makeCriterion(makeBasicResourceSelector(null, null, "c", "d"), false).dataTopics());
            assertEquals(List.of("DATA/*"),
                    makeCriterion(makeBasicResourceSelector(null, null, null, "d"), false).dataTopics());
        }

        @Test
        void testSingleLevelNoProvider() {
            assertEquals(List.of("DATA/+/+/+/+"),
                    makeCriterion(makeBasicResourceSelector(null, null, null, null), true).dataTopics());
            assertEquals(List.of("DATA/a/+/+/+"),
                    makeCriterion(makeBasicResourceSelector("a", null, null, null), true).dataTopics());
            assertEquals(List.of("DATA/a/+/c/+"),
                    makeCriterion(makeBasicResourceSelector("a", null, "c", null), true).dataTopics());
            assertEquals(List.of("DATA/a/+/c/d"),
                    makeCriterion(makeBasicResourceSelector("a", null, "c", "d"), true).dataTopics());
            assertEquals(List.of("DATA/a/+/+/d"),
                    makeCriterion(makeBasicResourceSelector("a", null, null, "d"), true).dataTopics());
            assertEquals(List.of("DATA/+/+/c/d"),
                    makeCriterion(makeBasicResourceSelector(null, null, "c", "d"), true).dataTopics());
            assertEquals(List.of("DATA/+/+/+/d"),
                    makeCriterion(makeBasicResourceSelector(null, null, null, "d"), true).dataTopics());
        }

        @Test
        void testNoSingleLevelNoService() {
            assertEquals(List.of("DATA/*"),
                    makeCriterion(makeBasicResourceSelector(null, null, null, null), false).dataTopics());
            assertEquals(List.of("DATA/a/*"),
                    makeCriterion(makeBasicResourceSelector("a", null, null, null), false).dataTopics());
            assertEquals(List.of("DATA/a/b/*"),
                    makeCriterion(makeBasicResourceSelector("a", "b", null, null), false).dataTopics());
            assertEquals(List.of("DATA/a/b/*"),
                    makeCriterion(makeBasicResourceSelector("a", "b", null, "d"), false).dataTopics());
            assertEquals(List.of("DATA/a/*"),
                    makeCriterion(makeBasicResourceSelector("a", null, null, "d"), false).dataTopics());
            assertEquals(List.of("DATA/*"),
                    makeCriterion(makeBasicResourceSelector(null, "b", null, "d"), false).dataTopics());
            assertEquals(List.of("DATA/*"),
                    makeCriterion(makeBasicResourceSelector(null, null, null, "d"), false).dataTopics());
        }

        @Test
        void testSingleLevelNoService() {
            assertEquals(List.of("DATA/+/+/+/+"),
                    makeCriterion(makeBasicResourceSelector(null, null, null, null), true).dataTopics());
            assertEquals(List.of("DATA/a/+/+/+"),
                    makeCriterion(makeBasicResourceSelector("a", null, null, null), true).dataTopics());
            assertEquals(List.of("DATA/a/b/+/+"),
                    makeCriterion(makeBasicResourceSelector("a", "b", null, null), true).dataTopics());
            assertEquals(List.of("DATA/a/b/+/d"),
                    makeCriterion(makeBasicResourceSelector("a", "b", null, "d"), true).dataTopics());
            assertEquals(List.of("DATA/a/+/+/d"),
                    makeCriterion(makeBasicResourceSelector("a", null, null, "d"), true).dataTopics());
            assertEquals(List.of("DATA/+/b/+/d"),
                    makeCriterion(makeBasicResourceSelector(null, "b", null, "d"), true).dataTopics());
            assertEquals(List.of("DATA/+/+/+/d"),
                    makeCriterion(makeBasicResourceSelector(null, null, null, "d"), true).dataTopics());
        }

        @Test
        void testNoSingleLevelNoResource() {
            assertEquals(List.of("DATA/*"),
                    makeCriterion(makeBasicResourceSelector(null, null, null, null), false).dataTopics());
            assertEquals(List.of("DATA/a/*"),
                    makeCriterion(makeBasicResourceSelector("a", null, null, null), false).dataTopics());
            assertEquals(List.of("DATA/a/b/*"),
                    makeCriterion(makeBasicResourceSelector("a", "b", null, null), false).dataTopics());
            assertEquals(List.of("DATA/a/b/c/*"),
                    makeCriterion(makeBasicResourceSelector("a", "b", "c", null), false).dataTopics());
            assertEquals(List.of("DATA/a/*"),
                    makeCriterion(makeBasicResourceSelector("a", null, "c", null), false).dataTopics());
            assertEquals(List.of("DATA/*"),
                    makeCriterion(makeBasicResourceSelector(null, "b", "c", null), false).dataTopics());
            assertEquals(List.of("DATA/*"),
                    makeCriterion(makeBasicResourceSelector(null, null, "c", null), false).dataTopics());
        }

        @Test
        void testSingleLevelNoResource() {
            assertEquals(List.of("DATA/+/+/+/+"),
                    makeCriterion(makeBasicResourceSelector(null, null, null, null), true).dataTopics());
            assertEquals(List.of("DATA/a/+/+/+"),
                    makeCriterion(makeBasicResourceSelector("a", null, null, null), true).dataTopics());
            assertEquals(List.of("DATA/a/b/+/+"),
                    makeCriterion(makeBasicResourceSelector("a", "b", null, null), true).dataTopics());
            assertEquals(List.of("DATA/a/b/c/+"),
                    makeCriterion(makeBasicResourceSelector("a", "b", "c", null), true).dataTopics());
            assertEquals(List.of("DATA/a/+/c/+"),
                    makeCriterion(makeBasicResourceSelector("a", null, "c", null), true).dataTopics());
            assertEquals(List.of("DATA/+/b/c/+"),
                    makeCriterion(makeBasicResourceSelector(null, "b", "c", null), true).dataTopics());
            assertEquals(List.of("DATA/+/+/c/+"),
                    makeCriterion(makeBasicResourceSelector(null, null, "c", null), true).dataTopics());
        }
    }
}
