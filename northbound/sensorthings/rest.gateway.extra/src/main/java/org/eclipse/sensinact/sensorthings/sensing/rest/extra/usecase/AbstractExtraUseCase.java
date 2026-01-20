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
package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import java.lang.reflect.ParameterizedType;

import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.Id;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import com.fasterxml.jackson.core.JsonProcessingException;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Providers;

/**
 * abstract use case
 *
 * @param <M>
 * @param <S>
 */
public abstract class AbstractExtraUseCase<M extends Id, S> implements IExtraUseCase<M, S> {

    private Class<M> type;

    public AbstractExtraUseCase() {
        this.type = internalGetUseCaseTypeParameter(getClass());
    }

    public static Class<?> getUseCaseTypeParameter(Class<? extends AbstractExtraUseCase<?, ?>> c) {
        return internalGetUseCaseTypeParameter(c);
    }

    protected ExpandedObservation getExpandedObservationFromService(ExtraUseCaseRequest<?> request,
            ServiceSnapshot serviceDatastream) {
        String obsStr = DtoMapperSimple.getResourceField(serviceDatastream, "lastObservation", String.class);
        return parseObservation(request, obsStr);
    }

    protected ExpandedObservation parseObservation(ExtraUseCaseRequest<?> request, String obsStr) {
        ExpandedObservation existingObservation;
        try {
            existingObservation = request.mapper().readValue(obsStr, ExpandedObservation.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return existingObservation;
    }

    /**
     * Get the type parameter
     *
     * @param superclass
     */
    @SuppressWarnings("unchecked")
    private static <T> Class<T> internalGetUseCaseTypeParameter(Class<?> clazz) {
        if (AbstractExtraUseCase.class.equals(clazz) || !AbstractExtraUseCase.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("Not a suitable class to check");
        }
        var superclass = clazz.getGenericSuperclass();
        return (Class<T>) ((ParameterizedType) superclass).getActualTypeArguments()[0];
    }

    /**
     * get generic type (dto) for use case
     */
    public Class<M> getType() {
        return type;
    }

    /**
     * Quickly resolve a context object where the context type is unimportant
     *
     * @param <T>
     * @param providers
     * @param type
     * @return
     */
    protected <T> T resolve(Providers providers, Class<T> type) {
        return resolve(providers, type, type);
    }

    /**
     * Resolve a context object of the given type for the supplied context
     *
     * @param <T>
     * @param providers
     * @param type
     * @param contextType
     * @return
     */
    protected <T> T resolve(Providers providers, Class<T> type, Class<?> contextType) {
        ContextResolver<T> resolver = providers.getContextResolver(type, MediaType.WILDCARD_TYPE);
        if (resolver == null) {
            throw new WebApplicationException("Unable to resolve a provider for " + type);
        } else {
            return resolver.getContext(contextType);
        }
    }

    /**
     * Quickly resolve a Use Case provider of the supplied type
     *
     * @param <T>
     * @param providers
     * @param type
     * @return
     */
    @SuppressWarnings("unchecked")
    protected <T extends AbstractExtraUseCase<?, ?>> T resolveUseCase(Providers providers, Class<T> type) {
        return (T) resolve(providers, IExtraUseCase.class, type);
    }
}
