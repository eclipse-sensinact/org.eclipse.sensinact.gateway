/*********************************************************************
* Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.sensinact.core.annotation.dto.Provider;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.AbstractExtraUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.DatastreamsExtraUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.FeatureOfInterestExtraUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.HistoricalLocationExtraUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.IExtraUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.LocationsExtraUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.ObservationsExtraUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.ObservedPropertiesExtraUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.RefIdUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.SensorsExtraUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.ThingsExtraUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Providers;

/**
 * Provides access to {@link IExtraUseCase} services in a Jakarta REST
 * application.
 * <p>
 * The Context type is used to determine which implementation to return. It
 * should be one of:
 * <ul>
 * <li>The type of the service</li>
 * <li>The handled type (i.e. first type parameter) for the service</li>
 * </ul>
 */
@SuppressWarnings("rawtypes")
@Provider
public class UseCaseProvider implements ContextResolver<IExtraUseCase> {
    private static final Logger LOG = LoggerFactory.getLogger(SensorThingsExtraFeature.class);

    @Context
    protected Application application;

    @Context
    public Providers providers;

    private final List<Class<? extends AbstractExtraUseCase<?, ?>>> knownExtras = List.of(DatastreamsExtraUseCase.class,
            FeatureOfInterestExtraUseCase.class, LocationsExtraUseCase.class, ObservationsExtraUseCase.class,
            ObservedPropertiesExtraUseCase.class, SensorsExtraUseCase.class, ThingsExtraUseCase.class,
            HistoricalLocationExtraUseCase.class, RefIdUseCase.class);

    private final Map<Class<? extends AbstractExtraUseCase<?, ?>>, IExtraUseCase<?, ?>> useCases = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public IExtraUseCase<?, ?> getContext(Class<?> type) {
        Class<? extends AbstractExtraUseCase<?, ?>> cacheKey;
        if (!AbstractExtraUseCase.class.isAssignableFrom(type)) {
            cacheKey = knownExtras.stream().filter(c -> AbstractExtraUseCase.getUseCaseTypeParameter(c).equals(type))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException(
                            "Type " + type + " is not a suitable Extra Use Case type"));
        } else {
            cacheKey = (Class<? extends AbstractExtraUseCase<?, ?>>) type;
        }
        // check if we have a method ensureDepenednciesUseCase before compute this one
        createDepedenciesUseCase(cacheKey, providers);

        IExtraUseCase<?, ?> useCase = useCases.computeIfAbsent(cacheKey, c -> {
            try {
                return c.getConstructor(Providers.class, Application.class).newInstance(providers, application);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                LOG.error("Failed creating a Use Case Provider for type {}.", type, e);
                throw new InternalServerErrorException("Failed to make the Extra Use Case provider", e);
            }
        });
        return useCase;
    }

    /**
     * check if method to ensureDependenciesUseCase are present and call it
     *
     * @param cacheKey
     */
    private void createDepedenciesUseCase(Class<? extends AbstractExtraUseCase<?, ?>> cacheKey, Providers providers) {
        if (cacheKey.isAnnotationPresent(DependsOnUseCases.class)) {
            DependsOnUseCases depends = cacheKey.getAnnotation(DependsOnUseCases.class);
            for (Class<? extends AbstractExtraUseCase<?, ?>> dep : depends.value()) {
                getContext(dep); // appel récursif pour créer les dépendances
            }
        }
    }

}
