/*********************************************************************
* Copyright (c) 2026 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.sensorthings.sensing.rest.extra.endpoint;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mock.Strictness.LENIENT;

import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessProviderUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessResourceUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessServiceUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.usecase.impl.DtoMemoryCacheProvider;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IDtoMemoryCache;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.DatastreamsExtraUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.FeatureOfInterestExtraUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.IExtraUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.LocationsExtraUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.ObservationsExtraUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.ObservedPropertiesExtraUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.RefIdUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.SensorsExtraUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.ThingsExtraUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.usecase.impl.AccessProviderUseCaseProvider;
import org.eclipse.sensinact.sensorthings.sensing.rest.usecase.impl.AccessResourceUseCaseProvider;
import org.eclipse.sensinact.sensorthings.sensing.rest.usecase.impl.AccessServiceUseCaseProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.ws.rs.ext.Providers;

@ExtendWith(MockitoExtension.class)
class UseCaseTest {

    @Mock(strictness = LENIENT)
    Providers providers;
    @Mock
    DataUpdate dataUpdate;
    @Mock
    GatewayThread gatewayThread;

    private UseCaseProvider ucp;

    @SuppressWarnings("rawtypes")
    @BeforeEach
    void setup() {
        ucp = new UseCaseProvider();
        ucp.providers = providers;
        Mockito.when(providers.<IExtraUseCase>getContextResolver(
                Mockito.argThat(c -> c != null && IExtraUseCase.class.isAssignableFrom(c)), Mockito.any()))
                .thenReturn(ucp);

        // Dependencies
        Mockito.when(providers.<IDtoMemoryCache>getContextResolver(
                Mockito.argThat(c -> c != null && IDtoMemoryCache.class.isAssignableFrom(c)), Mockito.any()))
                .thenReturn(new DtoMemoryCacheProvider());
        Mockito.when(providers.<DataUpdate>getContextResolver(
                Mockito.argThat(c -> c != null && DataUpdate.class.isAssignableFrom(c)), Mockito.any()))
                .thenReturn(new DataUpdateProvider(dataUpdate));
        Mockito.when(providers.<IAccessProviderUseCase>getContextResolver(
                Mockito.argThat(c -> c != null && IAccessProviderUseCase.class.isAssignableFrom(c)), Mockito.any()))
                .thenReturn(new AccessProviderUseCaseProvider());
        Mockito.when(providers.<IAccessServiceUseCase>getContextResolver(
                Mockito.argThat(c -> c != null && IAccessServiceUseCase.class.isAssignableFrom(c)), Mockito.any()))
                .thenReturn(new AccessServiceUseCaseProvider());
        Mockito.when(providers.<IAccessResourceUseCase>getContextResolver(
                Mockito.argThat(c -> c != null && IAccessResourceUseCase.class.isAssignableFrom(c)), Mockito.any()))
                .thenReturn(new AccessResourceUseCaseProvider());
        Mockito.when(providers.<GatewayThread>getContextResolver(
                Mockito.argThat(c -> c != null && GatewayThread.class.isAssignableFrom(c)), Mockito.any()))
                .thenReturn(new GatewayThreadProvider(gatewayThread));

    }

    @ValueSource(classes = { DatastreamsExtraUseCase.class, FeatureOfInterestExtraUseCase.class,
            LocationsExtraUseCase.class, ObservationsExtraUseCase.class, ObservedPropertiesExtraUseCase.class,
            RefIdUseCase.class, SensorsExtraUseCase.class, ThingsExtraUseCase.class })
    @ParameterizedTest
    void testUseCaseCreation(Class<?> c) {
        assertNotNull(ucp.getContext(c));
    }
}
