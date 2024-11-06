/*********************************************************************
* Copyright (c) 2024 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.filters.ldap.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.sensinact.core.command.AbstractTwinCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.push.dto.BulkGenericDto;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceValueFilter;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.filters.ldap.LdapParserTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

public class LdapFilterTest {

    @InjectService
    DataUpdate push;

    @InjectService
    GatewayThread thread;

    private GenericDto makeRc(final String model, final String provider, final String service, final String resource,
            final Object value) {
        return makeRc(null, model, provider, service, resource, value);
    }

    private GenericDto makeRc(final String packageUri, final String model, final String provider, final String service,
            final String resource, final Object value) {
        GenericDto dto = new GenericDto();
        dto.modelPackageUri = packageUri;
        dto.model = model;
        dto.provider = provider;
        dto.service = service;
        dto.resource = resource;
        dto.value = value;
        dto.type = value.getClass();
        dto.timestamp = Instant.now();
        return dto;
    }

    @BeforeEach
    void setup() throws Exception {
        BulkGenericDto dtos = new BulkGenericDto();
        dtos.dtos = new ArrayList<>();
        dtos.dtos.add(makeRc("temperature", "Temp1", "sensor", "temperature", 10));
        dtos.dtos.add(makeRc("temperature", "Temp1", "sensor", "unit", "°C"));
        dtos.dtos.add(makeRc("temperature", "Temp2", "sensor", "temperature", 40));
        dtos.dtos.add(makeRc("temperature", "Temp2", "sensor", "unit", "°F"));
        dtos.dtos.add(makeRc("temperature", "Temp3", "sensor", "temperature", 20));
        dtos.dtos.add(makeRc("gas", "Detect1", "sensor", "CO2", 1));
        dtos.dtos.add(makeRc("gas", "Detect1", "sensor", "CO", 2));
        dtos.dtos.add(makeRc("gas", "Detect1", "sensor", "O3", 2.5));
        dtos.dtos.add(makeRc("gas", "Detect2", "sensor", "CO", 3));
        dtos.dtos.add(makeRc("gas", "Detect2", "sensor", "O3", 4));
        dtos.dtos.add(makeRc("test", "test", "sensor", "temperature", 4));
        dtos.dtos.add(makeRc("test", "test", "sensor", "O3", 4));
        dtos.dtos.add(makeRc("https://eclipse.org/sensinact/ldap/test", "naming1", "naming", "sensor-1", 0));
        dtos.dtos.add(makeRc("https://eclipse.org/sensinact/ldap/test", "naming2", "naming", "sensor_2", 0));
        push.pushUpdate(dtos).getValue();
    }

    private List<ProviderSnapshot> applyFilter(final String query) throws Exception {
        ICriterion parsedFilter = LdapParserTest.parse(query);
        Collection<ProviderSnapshot> providers = thread
                .execute(new AbstractTwinCommand<Collection<ProviderSnapshot>>() {
                    protected Promise<Collection<ProviderSnapshot>> call(SensinactDigitalTwin model,
                            PromiseFactory pf) {
                        return pf.resolved(model.filteredSnapshot(null, parsedFilter.getProviderFilter(), null, null));
                    }

                ;
                }).getValue();

        if (parsedFilter.getResourceValueFilter() != null) {
            final ResourceValueFilter rcFilter = parsedFilter.getResourceValueFilter();
            return providers
                    .stream().filter(p -> rcFilter.test(p, p.getServices().stream()
                            .flatMap(s -> s.getResources().stream()).collect(Collectors.toList())))
                    .collect(Collectors.toList());
        } else {
            return List.copyOf(providers);
        }
    }

    private void assertFindProviders(final Collection<ProviderSnapshot> entries, final String... expected) {
        List<String> foundProviders = entries.stream().map(ProviderSnapshot::getName).collect(Collectors.toList());
        for (String name : expected) {
            if (!foundProviders.contains(name)) {
                fail(name + " not found in " + foundProviders);
            }
        }
    }

    @Test
    void testResourceValue() throws Exception {
        // Exact value
        List<ProviderSnapshot> results = applyFilter("(sensor.temperature=10)");
        assertEquals(1, results.size());
        assertEquals("Temp1", results.get(0).getName());

        // Greater equal
        results = applyFilter("(sensor.temperature>=10)");
        assertEquals(3, results.size());
        assertFindProviders(results, "Temp1", "Temp2", "Temp3");

        // Less equal
        results = applyFilter("(sensor.temperature<=15)");
        assertEquals(2, results.size());
        assertFindProviders(results, "Temp1", "test");
    }

    @Test
    void testResourcePresence() throws Exception {
        List<ProviderSnapshot> results = applyFilter("(sensor.O3=*)");
        assertEquals(3, results.size());
        assertFindProviders(results, "Detect1", "Detect2", "test");

        results = applyFilter("(*.O3=*)");
        assertEquals(3, results.size());
        assertFindProviders(results, "Detect1", "Detect2", "test");

        results = applyFilter("(sensor.CO2=*)");
        assertEquals(1, results.size());
        assertEquals("Detect1", results.get(0).getName());
    }

    @Test
    void testResourceNotPresent() throws Exception {
        List<ProviderSnapshot> results = applyFilter("(&(MODEL=temperature)(sensor.unit=*))");
        assertEquals(2, results.size());
        assertFindProviders(results, "Temp1", "Temp2");

        results = applyFilter("(&(MODEL=temperature)(!(sensor.unit=*)))");
        assertEquals(1, results.size());
        assertEquals("Temp3", results.get(0).getName());
    }

    @Test
    void testProviderFilters() throws Exception {
        List<ProviderSnapshot> results = applyFilter("(PROVIDER=Detect1)");
        assertEquals(1, results.size());
        assertEquals("Detect1", results.get(0).getName());

        results = applyFilter("(|(PROVIDER=Detect1)(PROVIDER=test))");
        assertEquals(2, results.size());
        assertFindProviders(results, "Detect1", "test");

        results = applyFilter("(&(MODEL=test)(PROVIDER=test))");
        assertEquals(1, results.size());
        assertEquals("test", results.get(0).getName());

        results = applyFilter("(MODEL=gas)");
        assertEquals(2, results.size());
        assertFindProviders(results, "Detect1", "Detect2");

        results = applyFilter("(&(MODEL=test)(PROVIDER=Detect1))");
        assertEquals(0, results.size());
    }

    @Test
    void testComplex() throws Exception {
        List<ProviderSnapshot> results = applyFilter("(&(MODEL=gas)(*.CO>=1))");
        assertEquals(2, results.size());
        assertFindProviders(results, "Detect1", "Detect2");

        results = applyFilter("(&(sensor.temperature>=5)(|(!(sensor.unit=*))(sensor.unit=°C)))");
        assertEquals(2, results.size());
        assertFindProviders(results, "Temp1", "Temp3");

        results = applyFilter(
                "(|(&(MODEL=gas)(*.CO>=1))(&(sensor.temperature>=5)(|(!(sensor.unit=*))(sensor.unit=°C))))");
        assertEquals(4, results.size());
        assertFindProviders(results, "Detect1", "Detect2", "Temp1", "Temp3");

        results = applyFilter(
                "(!(|(&(MODEL=gas)(*.CO>=1))(&(sensor.temperature>=5)(|(!(sensor.unit=*))(sensor.unit=°C)))))");
        assertEquals(2, results.size());
        assertFindProviders(results, "Temp2", "test");

        results = applyFilter(
                "(!(|(&(MODEL=gas)(*.CO>=1))(&(sensor.temperature>=5)(|(!(sensor.unit=*))(sensor.unit=°C)))))");
        assertEquals(2, results.size());
        assertFindProviders(results, "Temp2", "test");

        results = applyFilter("(|(MODEL=gas)(MODEL=temperature))");
        assertEquals(5, results.size());
        assertFindProviders(results, "Temp1", "Temp2", "Temp3", "Detect1", "Detect2");

        results = applyFilter("(|(MODEL=gas)(MODEL=temperature)(PROVIDER=test))");
        assertEquals(6, results.size());
        assertFindProviders(results, "Temp1", "Temp2", "Temp3", "Detect1", "Detect2", "test");
    }

    @Test
    void testNaming() throws Exception {
        List<ProviderSnapshot> results = applyFilter("(naming.sensor-1=*)");
        assertEquals(1, results.size());
        assertFindProviders(results, "naming1");

        results = applyFilter("(naming.sensor_2=*)");
        assertEquals(1, results.size());
        assertFindProviders(results, "naming2");
    }
}
