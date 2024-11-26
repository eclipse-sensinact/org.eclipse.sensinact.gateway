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
package org.eclipse.sensinact.southbound.rules.impl.integration;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.push.dto.BulkGenericDto;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.southbound.rules.api.RuleDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class RulesWhiteboardIntegrationTest {

    @InjectService
    DataUpdate push;

    @InjectService
    GatewayThread thread;

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

    @Test
    void testWhiteboard(@InjectBundleContext BundleContext bc) throws Exception {

        RuleDefinition rule = Mockito.mock(RuleDefinition.class);

        ICriterion criterion = Mockito.mock(ICriterion.class, Mockito.CALLS_REAL_METHODS);

        Mockito.when(rule.getInputFilter()).thenReturn(criterion);

        Mockito.when(criterion.getProviderFilter()).thenReturn(p -> "Temp1".equals(p.getName()));

        bc.registerService(RuleDefinition.class, rule, new Hashtable<>(Map.of(Constants.SERVICE_ID, 5,
                RuleDefinition.RULE_NAME_PROPERTY, "test")));

        Mockito.verify(rule).getInputFilter();

        // Called once and not again
        Mockito.verify(rule, Mockito.after(100)).evaluate(Mockito.argThat(hasProviders("Temp1")), Mockito.notNull());
        Mockito.verifyNoMoreInteractions(rule);
        Mockito.clearInvocations(rule);

        push.pushUpdate(makeRc("temperature", "Temp1", "sensor", "temperature", 12)).getValue();

        // Called once and not again
        Mockito.verify(rule, Mockito.after(100)).evaluate(Mockito.argThat(hasProviders("Temp1")), Mockito.notNull());

        push.pushUpdate(makeRc("temperature", "Temp2", "sensor", "temperature", 42)).getValue();

        // Not called for other updates
        Mockito.verify(rule, Mockito.after(100)).evaluate(Mockito.anyList(), Mockito.any());
    }

    @Test
    void testWhiteboardNoPreExisting(@InjectBundleContext BundleContext bc) throws Exception {

        RuleDefinition rule = Mockito.mock(RuleDefinition.class);

        ICriterion criterion = Mockito.mock(ICriterion.class, Mockito.CALLS_REAL_METHODS);

        Mockito.when(rule.getInputFilter()).thenReturn(criterion);

        Mockito.when(criterion.getProviderFilter()).thenReturn(p -> "Temp5".equals(p.getName()));

        bc.registerService(RuleDefinition.class, rule, new Hashtable<>(Map.of(Constants.SERVICE_ID, 5,
                RuleDefinition.RULE_NAME_PROPERTY, "test")));

        Mockito.verify(rule).getInputFilter();

        // Called once and not again
        Mockito.verify(rule, Mockito.after(100)).evaluate(Mockito.eq(List.of()), Mockito.notNull());
        Mockito.verifyNoMoreInteractions(rule);
        Mockito.clearInvocations(rule);

        push.pushUpdate(makeRc("temperature", "Temp5", "sensor", "temperature", 12)).getValue();

        // Called once and not again
        Mockito.verify(rule, Mockito.after(100)).evaluate(Mockito.argThat(hasProviders("Temp5")), Mockito.notNull());

        push.pushUpdate(makeRc("temperature", "Temp2", "sensor", "temperature", 42)).getValue();

        // Not called for other updates
        Mockito.verify(rule, Mockito.after(100)).evaluate(Mockito.anyList(), Mockito.any());
    }

    private ArgumentMatcher<List<ProviderSnapshot>> hasProviders(String... names) {
        return new ArgumentMatcher<List<ProviderSnapshot>>() {

            @Override
            public boolean matches(List<ProviderSnapshot> argument) {
                if(names.length == 0) {
                    return argument.isEmpty();
                } else {
                    Set<String> nameSet = Arrays.stream(names).collect(Collectors.toSet());
                    Set<String> providerSet = argument.stream()
                            .map(ProviderSnapshot::getName)
                            .collect(Collectors.toSet());
                    return nameSet.equals(providerSet);
                }
            }

            @Override
            public String toString() {
                return "<Providers " + Arrays.toString(names) + ">";
            }
        };
    }
}
