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
package org.eclipse.sensinact.filters.resource.selector.impl.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.sensinact.core.command.AbstractTwinCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.push.dto.BulkGenericDto;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceValueFilter;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.filters.api.IFilterParser;
import org.eclipse.sensinact.filters.resource.selector.api.ResourceSelector;
import org.eclipse.sensinact.filters.resource.selector.api.ResourceSelectorFilterFactory;
import org.eclipse.sensinact.filters.resource.selector.api.Selection;
import org.eclipse.sensinact.filters.resource.selector.api.Selection.MatchType;
import org.eclipse.sensinact.filters.resource.selector.api.ValueSelection;
import org.eclipse.sensinact.filters.resource.selector.api.ResourceSelector.ProviderSelection;
import org.eclipse.sensinact.filters.resource.selector.api.ResourceSelector.ResourceSelection;
import org.eclipse.sensinact.filters.resource.selector.api.ValueSelection.CheckType;
import org.eclipse.sensinact.filters.resource.selector.api.ValueSelection.OperationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

public class ResourceSelectorIntegrationTest {

    @InjectService
    DataUpdate push;

    @InjectService
    GatewayThread thread;

    @InjectService
    ResourceSelectorFilterFactory filterFactory;

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

    private List<ProviderSnapshot> applyFilter(final ResourceSelector query) throws Exception {
        return applyFilter(filterFactory.parseResourceSelector(query));
    }

    private List<ProviderSnapshot> applyFilter(final ResourceSelector... query) throws Exception {
        return applyFilter(filterFactory.parseResourceSelector(Arrays.stream(query)));
    }

    private List<ProviderSnapshot> applyFilter(ICriterion parsedFilter)
            throws InvocationTargetException, InterruptedException {
        Collection<ProviderSnapshot> providers = thread
                .execute(new AbstractTwinCommand<Collection<ProviderSnapshot>>() {
                    @Override
                    protected Promise<Collection<ProviderSnapshot>> call(SensinactDigitalTwin model,
                            PromiseFactory pf) {
                        return pf.resolved(model.filteredSnapshot(null, parsedFilter.getProviderFilter(),
                                parsedFilter.getServiceFilter(), parsedFilter.getResourceFilter()));
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

    @Test
    void testResourceValue() throws Exception {

        ResourceSelector rs = makeBasicResourceSelector(null, null, "sensor", "temperature");
        // Exact value
        rs = updateValueTest(rs, makeValueSelection("10", null, OperationType.EQUALS));
        List<ProviderSnapshot> results = applyFilter(rs);
        assertEquals(1, results.size());
        assertEquals("Temp1", results.get(0).getName());

        // Greater equal
        rs = updateValueTest(rs, makeValueSelection("10", null, OperationType.GREATER_THAN_OR_EQUAL));
        results = applyFilter(rs);
        assertEquals(3, results.size());
        assertFindProviders(results, "Temp1", "Temp2", "Temp3");

        // Less equal
        rs = updateValueTest(rs, makeValueSelection("15", null, OperationType.LESS_THAN_OR_EQUAL));
        results = applyFilter(rs);
        assertEquals(2, results.size());
        assertFindProviders(results, "Temp1", "test");
    }

    @Test
    void testResourcePresence() throws Exception {
        ResourceSelector rs = makeBasicResourceSelector(null, null, "sensor", "O3");
        rs = updateValueTest(rs, makeValueSelection(null, null, OperationType.IS_SET));
        List<ProviderSnapshot> results = applyFilter(rs);
        assertEquals(3, results.size());
        assertFindProviders(results, "Detect1", "Detect2", "test");

        rs = makeBasicResourceSelector(null, null, null, "O3");
        rs = updateValueTest(rs, makeValueSelection(null, null, OperationType.IS_SET));
        results = applyFilter(rs);
        assertEquals(3, results.size());
        assertFindProviders(results, "Detect1", "Detect2", "test");

        rs = makeBasicResourceSelector(null, null, "sensor", "CO2");
        rs = updateValueTest(rs, makeValueSelection(null, null, OperationType.IS_SET));
        results = applyFilter(rs);
        assertEquals(1, results.size());
        assertEquals("Detect1", results.get(0).getName());
    }

    @Test
    void testResourceNotPresent() throws Exception {
        ResourceSelector rs = makeBasicResourceSelector("temperature", null, "sensor", "unit");
        rs = updateValueTest(rs, makeValueSelection(null, null, OperationType.IS_SET));
        List<ProviderSnapshot> results = applyFilter(rs);
        assertEquals(2, results.size());
        assertFindProviders(results, "Temp1", "Temp2");

        rs = updateValueTest(rs, makeValueSelection(null, null, OperationType.IS_SET, true));
        results = applyFilter(rs);
        assertEquals(1, results.size());
        assertEquals("Temp3", results.get(0).getName());
    }

    @Test
    void testProviderFilters() throws Exception {
        ResourceSelector rs = makeBasicResourceSelector(null, "Detect1", null, null);
        List<ProviderSnapshot> results = applyFilter(rs);
        assertEquals(1, results.size());
        assertEquals("Detect1", results.get(0).getName());

        results = applyFilter(rs, makeBasicResourceSelector(null, "test", null, null));
        assertEquals(2, results.size());
        assertFindProviders(results, "Detect1", "test");

        rs = makeBasicResourceSelector("test", "test", null, null);
        results = applyFilter(rs);
        assertEquals(1, results.size());
        assertEquals("test", results.get(0).getName());

        rs = makeBasicResourceSelector("gas", null, null, null);
        results = applyFilter(rs);
        assertEquals(2, results.size());
        assertFindProviders(results, "Detect1", "Detect2");

        rs = makeBasicResourceSelector("test", "Detect1", null, null);
        results = applyFilter(rs);
        assertEquals(0, results.size());
    }

    @Test
    void testComplex() throws Exception {
        ResourceSelector rs = makeBasicResourceSelector("gas", null, null, "CO");
        rs = updateValueTest(rs, makeValueSelection("1", null, OperationType.GREATER_THAN_OR_EQUAL));
        List<ProviderSnapshot> results = applyFilter(rs);
        assertEquals(2, results.size());
        assertFindProviders(results, "Detect1", "Detect2");

        ResourceSelector unitDegrees = makeBasicResourceSelector(null, null, "sensor", "unit");
        unitDegrees = updateValueTest(unitDegrees, makeValueSelection("°C", null, OperationType.EQUALS));
        ResourceSelector unitUnset = makeBasicResourceSelector(null, null, "sensor", "unit");
        unitUnset = updateValueTest(unitUnset, makeValueSelection("°C", null, OperationType.IS_SET, true));
        ResourceSelector tempGreater = makeBasicResourceSelector(null, null, "sensor", "temperature");
        tempGreater = updateValueTest(tempGreater, makeValueSelection("5", null, OperationType.GREATER_THAN_OR_EQUAL));

        ICriterion baseFilter = filterFactory.parseResourceSelector(tempGreater)
                .and(filterFactory.parseResourceSelector(Stream.of(unitUnset, unitDegrees)));
        results = applyFilter(baseFilter);
        assertEquals(2, results.size());
        assertFindProviders(results, "Temp1", "Temp3");

        ResourceSelector gasCO = makeBasicResourceSelector("gas", null, null, "CO");
        gasCO = updateValueTest(gasCO, makeValueSelection("1", null, OperationType.GREATER_THAN_OR_EQUAL));
        ICriterion fullFilter = filterFactory.parseResourceSelector(gasCO).or(baseFilter);
        results = applyFilter(fullFilter);
        assertEquals(4, results.size());
        assertFindProviders(results, "Detect1", "Detect2", "Temp1", "Temp3");

        // TODO - check this over as the LDAP tests seem to require incorrect behaviour
//        results = applyFilter(fullFilter.negate());
//        assertEquals(2, results.size());
//        assertFindProviders(results, "Temp2", "test");

        results = applyFilter(makeBasicResourceSelector("gas", null, null, null),
                makeBasicResourceSelector("temperature", null, null, null));
        assertEquals(5, results.size());
        assertFindProviders(results, "Temp1", "Temp2", "Temp3", "Detect1", "Detect2");

        results = applyFilter(makeBasicResourceSelector("gas", null, null, null),
                makeBasicResourceSelector("temperature", null, null, null),
                makeBasicResourceSelector("test", null, null, null));
        assertEquals(6, results.size());
        assertFindProviders(results, "Temp1", "Temp2", "Temp3", "Detect1", "Detect2", "test");
    }

    @Test
    void testNaming() throws Exception {
        ResourceSelector rs = makeBasicResourceSelector(null, null, "naming", "sensor-1");
        rs = updateValueTest(rs, makeValueSelection(null, null, OperationType.IS_SET));
        List<ProviderSnapshot> results = applyFilter(rs);
        assertEquals(1, results.size());
        assertFindProviders(results, "naming1");

        rs = makeBasicResourceSelector(null, null, "naming", "sensor_2");
        rs = updateValueTest(rs, makeValueSelection(null, null, OperationType.IS_SET));
        results = applyFilter(rs);
        assertEquals(1, results.size());
        assertFindProviders(results, "naming2");
    }

    /**
     * Test the resource selector as a Filter parser
     *
     * @param parser
     * @throws Exception
     */
    @Test
    void testResourceValueFromJsonString(
            @InjectService(filter = "(" + IFilterParser.SUPPORTED_FILTER_LANGUAGE + "="
                    + ResourceSelectorFilterFactory.RESOURCE_SELECTOR_FILTER + ")") IFilterParser parser)
            throws Exception {

        String json = """
                {
                    "service": {
                        "type": "EXACT",
                        "value": "sensor"
                    },
                    "resource": {
                        "type": "EXACT",
                        "value": "temperature"
                    },
                    "value": {
                        "value": "10",
                        "operation": "EQUALS"
                    }
                }
                """;

        ICriterion filter = parser.parseFilter(json);
        List<ProviderSnapshot> results = applyFilter(filter);
        assertEquals(1, results.size());
        assertEquals("Temp1", results.get(0).getName());

        json = """
                [
                    {
                        "service": {
                            "type": "EXACT",
                            "value": "sensor"
                        },
                        "resource": {
                            "type": "EXACT",
                            "value": "temperature"
                        },
                        "value": {
                            "value": "10",
                            "operation": "EQUALS"
                        }
                    },
                    {
                        "service": {
                            "type": "EXACT",
                            "value": "sensor"
                        },
                        "resource": {
                            "type": "EXACT",
                            "value": "temperature"
                        },
                        "value": {
                            "value": "20",
                            "operation": "EQUALS"
                        }
                    }
                ]
                """;

        filter = parser.parseFilter(json);
        results = applyFilter(filter).stream().sorted(Comparator.comparing(ProviderSnapshot::getName)).toList();
        assertEquals(2, results.size());
        assertEquals("Temp1", results.get(0).getName());
        assertEquals("Temp3", results.get(1).getName());
    }
}
