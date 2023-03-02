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
*   Kentyou - initial API and implementation
**********************************************************************/
package org.eclipse.sensinact.prototype.whiteboard.impl;

import static java.util.stream.Collectors.toList;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.sensinact.prototype.annotation.verb.ActParam;
import org.eclipse.sensinact.prototype.annotation.verb.UriParam;
import org.eclipse.sensinact.prototype.model.nexus.emf.EMFUtil;

class ActMethod {
    private final Method method;
    private final Object instance;
    final Long serviceId;
    final Set<String> providers;

    public ActMethod(Method method, Object instance, Long serviceId, Set<String> providers) {
        super();
        this.method = method;
        this.instance = instance;
        this.serviceId = serviceId;
        this.providers = providers;
    }

    public boolean isCatchAll() {
        return providers.isEmpty();
    }

    public boolean overlaps(ActMethod actMethod) {
        return (providers.isEmpty() && actMethod.providers.isEmpty())
                || Collections.disjoint(providers, actMethod.providers);
    }

    public List<Entry<String, Class<?>>> getNamedParameterTypes() {
        return Arrays.stream(method.getParameters()).filter(p -> !p.isAnnotationPresent(UriParam.class)).map(
                p -> new AbstractMap.SimpleImmutableEntry<String, Class<?>>(getActionParameterName(p), p.getType()))
                .collect(toList());
    }

    private String getActionParameterName(Parameter p) {
        String name;
        if (p.isAnnotationPresent(ActParam.class)) {
            name = p.getAnnotation(ActParam.class).name();
        } else {
            name = p.getName();
        }
        return name;
    }

    public Class<?> getReturnType() {
        return method.getReturnType();
    }

    public Object invoke(String model, String provider, String service, String resource, Map<String, Object> params)
            throws Exception {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter p = parameters[i];
            UriParam param = p.getAnnotation(UriParam.class);
            if (param != null) {
                switch (param.value()) {
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
                String name = getActionParameterName(p);
                Object o = params.get(name);
                args[i] = o == null ? null
                        : p.getType().isInstance(o) ? o : EMFUtil.convertToTargetType(p.getType(), o);
            }
        }
        return method.invoke(instance, args);
    }

    @Override
    public String toString() {
        return "ActMethod [serviceId=" + serviceId + ", providers=" + providers + "]";
    }
}
