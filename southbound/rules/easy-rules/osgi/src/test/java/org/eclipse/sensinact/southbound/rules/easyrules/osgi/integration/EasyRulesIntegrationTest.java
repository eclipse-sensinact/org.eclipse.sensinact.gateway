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
package org.eclipse.sensinact.southbound.rules.easyrules.osgi.integration;

import java.lang.reflect.InvocationTargetException;
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
import org.eclipse.sensinact.filters.resource.selector.api.CompactResourceSelector;
import org.eclipse.sensinact.filters.resource.selector.api.ResourceSelector;
import org.eclipse.sensinact.filters.resource.selector.api.ResourceSelectorFilterFactory;
import org.eclipse.sensinact.filters.resource.selector.api.Selection;
import org.eclipse.sensinact.filters.resource.selector.api.ValueSelection;
import org.eclipse.sensinact.filters.resource.selector.api.ValueSelection.CheckType;
import org.eclipse.sensinact.filters.resource.selector.api.ValueSelection.OperationType;
import org.eclipse.sensinact.southbound.rules.api.ResourceUpdater;
import org.eclipse.sensinact.southbound.rules.api.ResourceUpdater.BatchUpdate;
import org.eclipse.sensinact.southbound.rules.easyrules.EasyRulesHelper;
import org.jeasy.rules.api.Rules;
import org.jeasy.rules.api.RulesEngine;
import org.jeasy.rules.core.DefaultRulesEngine;
import org.jeasy.rules.core.RuleBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

@ExtendWith(MockitoExtension.class)
public class EasyRulesIntegrationTest {

    @InjectService
    DataUpdate push;

    @InjectService
    GatewayThread thread;

    @InjectService
    ResourceSelectorFilterFactory filterFactory;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    ResourceUpdater updater;

    RulesEngine re = new DefaultRulesEngine();

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

    private List<ProviderSnapshot> applyFilter(ICriterion parsedFilter)
            throws InvocationTargetException, InterruptedException {
        Collection<ProviderSnapshot> providers = thread
                .execute(new AbstractTwinCommand<Collection<ProviderSnapshot>>() {
                    protected Promise<Collection<ProviderSnapshot>> call(SensinactDigitalTwin model,
                            PromiseFactory pf) {
                        return pf.resolved(model.filteredSnapshot(null, parsedFilter.getProviderFilter(), parsedFilter.getServiceFilter(), parsedFilter.getResourceFilter()));
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

    private ResourceSelector makeBasicResourceSelector(String model, String provider, String service, String resource,
            ValueSelection vs) {
        return new CompactResourceSelector(null,
                model == null ? null : makeExactSelection(model),
                provider == null ? null :makeExactSelection(provider),
                service == null ? null : makeExactSelection(service),
                resource == null ? null : makeExactSelection(resource),
                        List.of(vs), List.of()).toResourceSelector();
    }

    private Selection makeExactSelection(String name) {
        return new Selection(name, null, false);
    }

    private ValueSelection makeValueSelection(String value, CheckType check, OperationType operation) {
        return new ValueSelection(value, operation, false, check);
    }

    @Test
    void testProvidersFound() throws Exception {

        Rules rules = new Rules(new RuleBuilder()
                .name("test")
                .description("Test rule")
                .when(f -> !((Collection<?>)f.get("$providers")).isEmpty())
                .then(f -> {
                    BatchUpdate bu = ((ResourceUpdater)f.get("$updater")).updateBatch();
                    ((Collection<?>)f.get("$providers")).forEach(
                            s -> bu.updateResource(String.valueOf(s), "alert", "temperature", "high"));
                    bu.completeBatch();
                })
                .build());

        ResourceSelector rs = makeBasicResourceSelector(null, null, "sensor", "temperature",
                makeValueSelection("10", null, OperationType.GREATER_THAN_OR_EQUAL));
        // "Temp1", "Temp2", "Temp3"
        re.fire(rules, EasyRulesHelper.toFacts(applyFilter(rs), updater));

        Mockito.verify(updater).updateBatch();
        Mockito.verify(updater.updateBatch()).updateResource("Temp1", "alert", "temperature", "high");
        Mockito.verify(updater.updateBatch()).updateResource("Temp3", "alert", "temperature", "high");
        Mockito.verify(updater.updateBatch()).updateResource("Temp3", "alert", "temperature", "high");
        Mockito.verify(updater.updateBatch()).completeBatch();
    }
}
