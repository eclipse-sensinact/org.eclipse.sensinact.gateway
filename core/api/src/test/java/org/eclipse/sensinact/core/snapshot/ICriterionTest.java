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
package org.eclipse.sensinact.core.snapshot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.gateway.geojson.LineString;
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.gateway.geojson.Polygon;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ICriterionTest {

    private <T> Predicate<T> match(Function<T, String> function, String value) {
        return value == null ? null : t -> value.equals(function.apply(t));
    }

    private ICriterion getTestCriterion(String provider, String service, String resource, String value, String location) {
        return new ICriterion() {

            @Override
            public Predicate<ServiceSnapshot> getServiceFilter() {
                return match(ServiceSnapshot::getName, service);
            }

            @Override
            public ResourceValueFilter getResourceValueFilter() {
                return value == null ? null : (a,b) -> value.equals(b.get(0).getValue().getValue());
            }

            @Override
            public Predicate<ResourceSnapshot> getResourceFilter() {
                return match(ResourceSnapshot::getName, resource);
            }

            @Override
            public Predicate<ProviderSnapshot> getProviderFilter() {
                return match(ProviderSnapshot::getName, provider);
            }

            @Override
            public Predicate<GeoJsonObject> getLocationFilter() {
                return match(g -> g.type.name(), location);
            }
        };
    }

    @Mock
    ProviderSnapshot ps;
    @Mock
    ServiceSnapshot ss;
    @Mock
    ResourceSnapshot rs;
    @Mock
    TimedValue<Object> tv;

    @Nested
    class NegationTests {

        public void nullTest(String value, boolean expected) {

            ICriterion criterion = getTestCriterion(null, null, null, null, null);
            ICriterion negated = criterion.negate();

            assertNull(criterion.getProviderFilter());
            assertNull(negated.getProviderFilter());
            assertNull(criterion.getServiceFilter());
            assertNull(negated.getServiceFilter());
            assertNull(criterion.getResourceFilter());
            assertNull(negated.getResourceFilter());
            assertNull(criterion.getLocationFilter());
            assertNull(negated.getLocationFilter());
            assertNull(criterion.getResourceValueFilter());
            assertNull(negated.getResourceValueFilter());
        }

        @ParameterizedTest
        @CsvSource(value = {"a,true", "b,false"})
        public void providerTest(String value, boolean expected) {

            Mockito.when(ps.getName()).thenReturn(value);

            ICriterion criterion = getTestCriterion("a", null, null, null, null);

            assertEquals(expected, criterion.getProviderFilter().test(ps));
            assertEquals(!expected, criterion.negate().getProviderFilter().test(ps));
        }

        @ParameterizedTest
        @CsvSource(value = {"a,true", "b,false"})
        public void serviceTest(String value, boolean expected) {

            Mockito.when(ss.getName()).thenReturn(value);

            ICriterion criterion = getTestCriterion(null, "a", null, null, null);

            assertEquals(expected, criterion.getServiceFilter().test(ss));
            assertEquals(!expected, criterion.negate().getServiceFilter().test(ss));
        }

        @ParameterizedTest
        @CsvSource(value = {"a,true", "b,false"})
        public void resourceTest(String value, boolean expected) {

            Mockito.when(rs.getName()).thenReturn(value);

            ICriterion criterion = getTestCriterion(null, null, "a", null, null);

            assertEquals(expected, criterion.getResourceFilter().test(rs));
            assertEquals(!expected, criterion.negate().getResourceFilter().test(rs));
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        @ParameterizedTest
        @CsvSource(value = {"a,true", "b,false"})
        public void valueTest(String value, boolean expected) {

            Mockito.when(rs.getValue()).thenReturn((TimedValue) tv);
            Mockito.when((Object) tv.getValue()).thenReturn(value);

            ICriterion criterion = getTestCriterion(null, null, null, "a", null);

            assertEquals(expected, criterion.getResourceValueFilter().test(ps, List.of(rs)));
            assertEquals(!expected, criterion.negate().getResourceValueFilter().test(ps, List.of(rs)));
        }

        @Test
        public void locationTest() {

            GeoJsonObject point = new Point();
            GeoJsonObject polygon = new Polygon();

            ICriterion criterion = getTestCriterion(null, null, null, null, "Point");

            assertTrue(criterion.getLocationFilter().test(point));
            assertFalse(criterion.negate().getLocationFilter().test(point));

            assertFalse(criterion.getLocationFilter().test(polygon));
            assertTrue(criterion.negate().getLocationFilter().test(polygon));
        }

        @ParameterizedTest
        @CsvSource(value = {"e,true", "f,false"})
        public void dataEventTest(String value, boolean expected) {
            ResourceDataNotification rdn = getNotification(value);

            ICriterion criterion = getTestCriterion(null, null, null, "e", null);

            assertEquals(expected, criterion.dataEventFilter().test(rdn));
            assertEquals(!expected, criterion.negate().dataEventFilter().test(rdn));
        }
    }

    private ResourceDataNotification getNotification(String value) {
        ResourceDataNotification rdn = new ResourceDataNotification();
        rdn.model = "a";
        rdn.provider = "b";
        rdn.service = "c";
        rdn.resource = "d";
        rdn.newValue = value;
        rdn.timestamp = Instant.now();
        rdn.metadata = Map.of("foo", "bar");
        return rdn;
    }

    @Nested
    class AndTests {

        public void nullTest(String value, boolean expected) {

            ICriterion criterionA = getTestCriterion(null, null, null, null, null);
            ICriterion criterionB = getTestCriterion("a", "b", "c", "d", "e");
            ICriterion criterionAB = criterionA.and(criterionB);
            ICriterion criterionBA = criterionB.and(criterionA);

            assertNull(criterionA.getProviderFilter());
            assertNotNull(criterionB.getProviderFilter());
            assertNotNull(criterionAB.getProviderFilter());
            assertNotNull(criterionBA.getProviderFilter());

            assertNull(criterionA.getServiceFilter());
            assertNotNull(criterionB.getServiceFilter());
            assertNotNull(criterionAB.getServiceFilter());
            assertNotNull(criterionBA.getServiceFilter());

            assertNull(criterionA.getResourceFilter());
            assertNotNull(criterionB.getResourceFilter());
            assertNotNull(criterionAB.getResourceFilter());
            assertNotNull(criterionBA.getResourceFilter());

            assertNull(criterionA.getLocationFilter());
            assertNotNull(criterionB.getLocationFilter());
            assertNotNull(criterionAB.getLocationFilter());
            assertNotNull(criterionBA.getLocationFilter());

            assertNull(criterionA.getResourceValueFilter());
            assertNotNull(criterionB.getResourceValueFilter());
            assertNotNull(criterionAB.getResourceValueFilter());
            assertNotNull(criterionBA.getResourceValueFilter());
        }

        @ParameterizedTest
        @CsvSource(value = {"a,true", "b,false"})
        public void providerTest(String value, boolean expected) {

            Mockito.when(ps.getName()).thenReturn(value);

            ICriterion criterionA = getTestCriterion("a", null, null, null, null);
            ICriterion nullCriterion = getTestCriterion(null, null, null, null, null);
            ICriterion criterionB = getTestCriterion("b", null, null, null, null);

            ICriterion andNull = criterionA.and(nullCriterion);
            ICriterion nullAnd = nullCriterion.and(criterionA);

            ICriterion criterionAB = criterionA.and(criterionB);
            ICriterion criterionBA = criterionB.and(criterionA);

            assertEquals(expected, andNull.getProviderFilter().test(ps));
            assertEquals(expected, nullAnd.getProviderFilter().test(ps));
            assertFalse(criterionAB.getProviderFilter().test(ps));
            assertFalse(criterionBA.getProviderFilter().test(ps));
        }

        @ParameterizedTest
        @CsvSource(value = {"a,true", "b,false"})
        public void serviceTest(String value, boolean expected) {

            Mockito.when(ss.getName()).thenReturn(value);

            ICriterion criterionA = getTestCriterion(null, "a", null, null, null);
            ICriterion nullCriterion = getTestCriterion(null, null, null, null, null);
            ICriterion criterionB = getTestCriterion(null, "b", null, null, null);

            ICriterion andNull = criterionA.and(nullCriterion);
            ICriterion nullAnd = nullCriterion.and(criterionA);

            ICriterion criterionAB = criterionA.and(criterionB);
            ICriterion criterionBA = criterionB.and(criterionA);

            assertNull(andNull.getServiceFilter());
            assertNull(nullAnd.getServiceFilter());
            assertTrue(criterionAB.getServiceFilter().test(ss));
            assertTrue(criterionBA.getServiceFilter().test(ss));
        }

        @ParameterizedTest
        @CsvSource(value = {"a,true", "b,false"})
        public void resourceTest(String value, boolean expected) {

            Mockito.when(rs.getName()).thenReturn(value);

            ICriterion criterionA = getTestCriterion(null, null, "a", null, null);
            ICriterion nullCriterion = getTestCriterion(null, null, null, null, null);
            ICriterion criterionB = getTestCriterion(null, null, "b", null, null);

            ICriterion andNull = criterionA.and(nullCriterion);
            ICriterion nullAnd = nullCriterion.and(criterionA);

            ICriterion criterionAB = criterionA.and(criterionB);
            ICriterion criterionBA = criterionB.and(criterionA);

            assertNull(andNull.getResourceFilter());
            assertNull(nullAnd.getResourceFilter());
            assertTrue(criterionAB.getResourceFilter().test(rs));
            assertTrue(criterionBA.getResourceFilter().test(rs));
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        @ParameterizedTest
        @CsvSource(value = {"a,true", "b,false"})
        public void valueTest(String value, boolean expected) {

            Mockito.when(rs.getValue()).thenReturn((TimedValue) tv);
            Mockito.when((Object) tv.getValue()).thenReturn(value);

            ICriterion criterionA = getTestCriterion(null, null, null, "a", null);
            ICriterion nullCriterion = getTestCriterion(null, null, null, null, null);
            ICriterion criterionB = getTestCriterion(null, null, null, "b", null);

            ICriterion andNull = criterionA.and(nullCriterion);
            ICriterion nullAnd = nullCriterion.and(criterionA);

            ICriterion criterionAB = criterionA.and(criterionB);
            ICriterion criterionBA = criterionB.and(criterionA);

            assertEquals(expected, andNull.getResourceValueFilter().test(ps, List.of(rs)));
            assertEquals(expected, nullAnd.getResourceValueFilter().test(ps, List.of(rs)));
            assertFalse(criterionAB.getResourceValueFilter().test(ps, List.of(rs)));
            assertFalse(criterionBA.getResourceValueFilter().test(ps, List.of(rs)));
        }

        @Test
        public void locationTest() {

            GeoJsonObject point = new Point();
            GeoJsonObject polygon = new Polygon();

            ICriterion criterionA = getTestCriterion(null, null, null, null, "Point");
            ICriterion nullCriterion = getTestCriterion(null, null, null, null, null);
            ICriterion criterionB = getTestCriterion(null, null, null, null, "Polygon");

            ICriterion andNull = criterionA.and(nullCriterion);
            ICriterion nullAnd = nullCriterion.and(criterionA);

            ICriterion criterionAB = criterionA.and(criterionB);
            ICriterion criterionBA = criterionB.and(criterionA);

            assertTrue(andNull.getLocationFilter().test(point));
            assertTrue(nullAnd.getLocationFilter().test(point));
            assertFalse(criterionAB.getLocationFilter().test(point));
            assertFalse(criterionBA.getLocationFilter().test(point));

            assertFalse(andNull.getLocationFilter().test(polygon));
            assertFalse(nullAnd.getLocationFilter().test(polygon));
            assertFalse(criterionAB.getLocationFilter().test(polygon));
            assertFalse(criterionBA.getLocationFilter().test(polygon));
        }

        @ParameterizedTest
        @CsvSource(value = {"e,true", "f,false"})
        public void dataEventTest(String value, boolean expected) {
            ResourceDataNotification rdn = getNotification(value);

            ICriterion criterionE = getTestCriterion(null, null, null, "e", null);
            ICriterion nullCriterion = getTestCriterion(null, null, null, null, null);
            ICriterion criterionF = getTestCriterion(null, null, null, "f", null);

            ICriterion andNull = criterionE.and(nullCriterion);
            ICriterion nullAnd = nullCriterion.and(criterionE);

            ICriterion criterionEF = criterionE.and(criterionF);
            ICriterion criterionFE = criterionF.and(criterionE);

            assertEquals(expected, andNull.dataEventFilter().test(rdn));
            assertEquals(expected, nullAnd.dataEventFilter().test(rdn));
            assertFalse(criterionEF.dataEventFilter().test(rdn));
            assertFalse(criterionFE.dataEventFilter().test(rdn));
        }
    }

    @Nested
    class OrTests {

        public void nullTest(String value, boolean expected) {

            ICriterion criterionA = getTestCriterion(null, null, null, null, null);
            ICriterion criterionB = getTestCriterion("a", "b", "c", "d", "e");
            ICriterion criterionAB = criterionA.or(criterionB);
            ICriterion criterionBA = criterionB.or(criterionA);

            assertNull(criterionA.getProviderFilter());
            assertNull(criterionB.getProviderFilter());
            assertNull(criterionAB.getProviderFilter());
            assertNull(criterionBA.getProviderFilter());

            assertNull(criterionA.getServiceFilter());
            assertNull(criterionB.getServiceFilter());
            assertNull(criterionAB.getServiceFilter());
            assertNull(criterionBA.getServiceFilter());

            assertNull(criterionA.getResourceFilter());
            assertNull(criterionB.getResourceFilter());
            assertNull(criterionAB.getResourceFilter());
            assertNull(criterionBA.getResourceFilter());

            assertNull(criterionA.getLocationFilter());
            assertNull(criterionB.getLocationFilter());
            assertNull(criterionAB.getLocationFilter());
            assertNull(criterionBA.getLocationFilter());

            assertNull(criterionA.getResourceValueFilter());
            assertNull(criterionB.getResourceValueFilter());
            assertNull(criterionAB.getResourceValueFilter());
            assertNull(criterionBA.getResourceValueFilter());
        }

        @ParameterizedTest
        @CsvSource(value = {"a,true", "b,true", "c,false"})
        public void providerTest(String value, boolean expected) {

            Mockito.when(ps.getName()).thenReturn(value);

            ICriterion criterionA = getTestCriterion("a", null, null, null, null);
            ICriterion criterionB = getTestCriterion("b", null, null, null, null);

            ICriterion criterionAB = criterionA.or(criterionB);
            ICriterion criterionBA = criterionB.or(criterionA);

            assertEquals(expected, criterionAB.getProviderFilter().test(ps));
            assertEquals(expected, criterionBA.getProviderFilter().test(ps));
        }

        @ParameterizedTest
        @CsvSource(value = {"a,true", "b,true", "c,false"})
        public void serviceTest(String value, boolean expected) {

            Mockito.when(ss.getName()).thenReturn(value);

            ICriterion criterionA = getTestCriterion(null, "a", null, null, null);
            ICriterion criterionB = getTestCriterion(null, "b", null, null, null);

            ICriterion criterionAB = criterionA.or(criterionB);
            ICriterion criterionBA = criterionB.or(criterionA);

            assertEquals(expected, criterionAB.getServiceFilter().test(ss));
            assertEquals(expected, criterionBA.getServiceFilter().test(ss));
        }

        @ParameterizedTest
        @CsvSource(value = {"a,true", "b,true", "c,false"})
        public void resourceTest(String value, boolean expected) {

            Mockito.when(rs.getName()).thenReturn(value);

            ICriterion criterionA = getTestCriterion(null, null, "a", null, null);
            ICriterion criterionB = getTestCriterion(null, null, "b", null, null);

            ICriterion criterionAB = criterionA.or(criterionB);
            ICriterion criterionBA = criterionB.or(criterionA);

            assertEquals(expected, criterionAB.getResourceFilter().test(rs));
            assertEquals(expected, criterionBA.getResourceFilter().test(rs));
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        @ParameterizedTest
        @CsvSource(value = {"a,true", "b,true", "c,false"})
        public void valueTest(String value, boolean expected) {

            Mockito.when(rs.getValue()).thenReturn((TimedValue) tv);
            Mockito.when((Object) tv.getValue()).thenReturn(value);

            ICriterion criterionA = getTestCriterion(null, null, null, "a", null);
            ICriterion criterionB = getTestCriterion(null, null, null, "b", null);

            ICriterion criterionAB = criterionA.or(criterionB);
            ICriterion criterionBA = criterionB.or(criterionA);

            assertEquals(expected, criterionAB.getResourceValueFilter().test(ps, List.of(rs)));
            assertEquals(expected, criterionBA.getResourceValueFilter().test(ps, List.of(rs)));
        }

        @Test
        public void locationTest() {

            GeoJsonObject point = new Point();
            GeoJsonObject polygon = new Polygon();
            GeoJsonObject linestring = new LineString();

            ICriterion criterionA = getTestCriterion(null, null, null, null, "Point");
            ICriterion criterionB = getTestCriterion(null, null, null, null, "Polygon");

            ICriterion criterionAB = criterionA.or(criterionB);
            ICriterion criterionBA = criterionB.or(criterionA);

            assertTrue(criterionAB.getLocationFilter().test(point));
            assertTrue(criterionBA.getLocationFilter().test(point));

            assertTrue(criterionAB.getLocationFilter().test(polygon));
            assertTrue(criterionBA.getLocationFilter().test(polygon));

            assertFalse(criterionAB.getLocationFilter().test(linestring));
            assertFalse(criterionBA.getLocationFilter().test(linestring));
        }

        @ParameterizedTest
        @CsvSource(value = {"e,true", "f,false"})
        public void dataEventTest(String value, boolean expected) {
            ResourceDataNotification rdn = getNotification(value);

            ICriterion criterionE = getTestCriterion(null, null, null, "e", null);
            ICriterion criterionF = getTestCriterion(null, null, null, "f", null);

            ICriterion criterionEF = criterionE.and(criterionF);
            ICriterion criterionFE = criterionF.and(criterionE);

            assertFalse(criterionEF.dataEventFilter().test(rdn));
            assertFalse(criterionFE.dataEventFilter().test(rdn));
        }
    }
}
