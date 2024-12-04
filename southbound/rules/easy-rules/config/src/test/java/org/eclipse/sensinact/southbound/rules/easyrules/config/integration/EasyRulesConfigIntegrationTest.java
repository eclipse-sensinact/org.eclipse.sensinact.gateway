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
package org.eclipse.sensinact.southbound.rules.easyrules.config.integration;

import static org.junit.jupiter.api.Assertions.fail;

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
import org.eclipse.sensinact.southbound.rules.api.ResourceUpdater;
import org.eclipse.sensinact.southbound.rules.api.RuleDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class EasyRulesConfigIntegrationTest {

    @InjectService
    DataUpdate push;

    @InjectService
    GatewayThread thread;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    ResourceUpdater updater;

    ObjectMapper mapper = new ObjectMapper();

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

    private void assertFindProviders(final Collection<ProviderSnapshot> entries, final String... expected) {
        List<String> foundProviders = entries.stream().map(ProviderSnapshot::getName).collect(Collectors.toList());
        for (String name : expected) {
            if (!foundProviders.contains(name)) {
                fail(name + " not found in " + foundProviders);
            }
        }
    }

    @Test
    @WithFactoryConfiguration(factoryPid = "sensinact.rules.easyrules",
        properties = {
                @Property(key = "name", value = "test"),
                @Property(key = "resource.selectors", value = "{"
                        + "\"service\": { \"value\":\"sensor\",\"type\":\"EXACT\" },"
                        + "\"resource\": { \"value\":\"temperature\",\"type\":\"EXACT\" },"
                        + "\"value\": { \"value\":\"10\",\"operation\":\"GREATER_THAN_OR_EQUAL\"}}"),
                @Property(key = "rule.definitions", value = "{"
                        + "\"name\":\"test\","
                        + "\"description\":\"Test Rule\","
                        + "\"condition\":\"not $providers.isEmpty()\","
                        + "\"action\":\"const b = $updater.updateBatch(); for(p : $providers) { b.updateResource(p, 'alert', 'temperature', 'high'); } b.completeBatch();\""
                        + "}")
        })
    void testProvidersFound(@InjectService(filter = "(name=test)") RuleDefinition def) throws Exception {

        List<ProviderSnapshot> snapshots = applyFilter(def.getInputFilter());
        assertFindProviders(snapshots, "Temp1", "Temp2", "Temp3");

        def.evaluate(snapshots, updater);

        Mockito.verify(updater).updateBatch();
        Mockito.verify(updater.updateBatch()).updateResource("Temp1", "alert", "temperature", "high");
        Mockito.verify(updater.updateBatch()).updateResource("Temp3", "alert", "temperature", "high");
        Mockito.verify(updater.updateBatch()).updateResource("Temp3", "alert", "temperature", "high");
        Mockito.verify(updater.updateBatch()).completeBatch();
    }

    @Test
    @WithFactoryConfiguration(factoryPid = "sensinact.rules.easyrules",
    properties = {
            @Property(key = "name", value = "test"),
            @Property(key = "resource.selectors", value = "{"
                    + "\"service\": { \"value\":\"sensor\",\"type\":\"EXACT\" },"
                    + "\"resource\": { \"value\":\"O3\",\"type\":\"EXACT\" }}"),
            @Property(key = "rule.definitions", value = "{"
                    + "\"name\":\"test\","
                    + "\"description\":\"Test Rule\","
                    + "\"condition\":\"true\","
                    + "\"action\":\"let sum = 0.0d; for(p : $providers) { var v = $data[p].get('sensor').get('O3').get('$value'); sum = sum + v; } if ( size($providers) > 0 ) { sum = sum / size($providers); } $updater.updateResource('test-stats', 'avg', 'O3', sum);\""
                    + "}")
    })
    void testAveraging(@InjectService(filter = "(name=test)") RuleDefinition def) throws Exception {

        List<ProviderSnapshot> snapshots = applyFilter(def.getInputFilter());
        assertFindProviders(snapshots, "Detect1", "Detect2", "test");

        def.evaluate(snapshots, updater);

        Mockito.verify(updater).updateResource("test-stats", "avg", "O3", 10.5d / 3);
    }
}
