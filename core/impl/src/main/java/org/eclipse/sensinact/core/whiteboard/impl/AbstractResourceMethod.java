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
package org.eclipse.sensinact.core.whiteboard.impl;

import static java.util.stream.Collectors.toList;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import org.eclipse.sensinact.core.annotation.verb.ActParam;
import org.eclipse.sensinact.core.annotation.verb.UriParam;
import org.eclipse.sensinact.core.model.nexus.emf.EMFUtil;

/**
 * Share code between ACT and GET methods
 */
abstract class AbstractResourceMethod {

    /**
     * Invoked method
     */
    protected final Method method;

    /**
     * Bound instance
     */
    protected final Object instance;

    final Long serviceId;

    /**
     * Associated providers
     */
    final Set<String> providers;

    public AbstractResourceMethod(final Method method, final Object instance, final Long serviceId,
            final Set<String> providers) {
        super();
        this.method = method;
        this.instance = instance;
        this.serviceId = serviceId;
        this.providers = providers;
    }

    /**
     * Checks if the bound instance can be used for any provider
     */
    public boolean isCatchAll() {
        return providers.isEmpty();
    }

    /**
     * Checks if this method overlaps the providers of another
     */
    public boolean overlaps(AbstractResourceMethod otherMethod) {
        return (providers.isEmpty() && otherMethod.providers.isEmpty())
                || Collections.disjoint(providers, otherMethod.providers);
    }

    /**
     * Returns the list of parameters of the invoked method
     */
    public List<Entry<String, Class<?>>> getNamedParameterTypes() {
        return Arrays.stream(method.getParameters()).filter(p -> !p.isAnnotationPresent(UriParam.class)).map(
                p -> new AbstractMap.SimpleImmutableEntry<String, Class<?>>(getActionParameterName(p), p.getType()))
                .collect(toList());
    }

    private String getActionParameterName(Parameter p) {
        String name;
        if (p.isAnnotationPresent(ActParam.class)) {
            name = p.getAnnotation(ActParam.class).value();
        } else {
            name = p.getName();
        }
        return name;
    }

    public Class<?> getReturnType() {
        return method.getReturnType();
    }

    protected <A extends Annotation, E extends Enum<E>> Object invoke(String modelPackageUri, String model,
            String provider, String service, String resource, Map<Object, Object> params,
            Class<A> extraArgumentAnnotation, Function<A, E> argNameExtractor) throws Exception {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            final Parameter p = parameters[i];
            final UriParam param = p.getAnnotation(UriParam.class);
            if (param != null) {
                switch (param.value()) {
                case MODEL_PACKAGE_URI:
                    args[i] = modelPackageUri;
                    break;
                case MODEL:
                    args[i] = model;
                    break;
                case PROVIDER:
                    args[i] = provider;
                    break;
                case RESOURCE:
                    args[i] = resource;
                    break;
                case SERVICE:
                    args[i] = service;
                    break;
                case URI:
                    args[i] = String.format("%s/%s/%s/%s", model, provider, service, resource);
                    break;
                default:
                    throw new IllegalArgumentException(param.value().toString());
                }
            } else {
                if (extraArgumentAnnotation != null) {
                    final A extraAnnotation = p.getAnnotation(extraArgumentAnnotation);
                    Object key = argNameExtractor.apply(extraAnnotation);
                    args[i] = params.get(key);
                } else {
                    String name = getActionParameterName(p);
                    final Object o = params.get(name);
                    args[i] = o == null ? null
                            : p.getType().isInstance(o) ? o : EMFUtil.convertToTargetType(p.getType(), o);
                }
            }
        }
        return method.invoke(instance, args);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[serviceId=" + serviceId + ", providers=" + providers + "]";
    }
}
