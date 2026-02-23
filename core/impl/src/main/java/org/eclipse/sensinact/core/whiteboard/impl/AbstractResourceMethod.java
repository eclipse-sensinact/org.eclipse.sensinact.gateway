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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.time.temporal.Temporal;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.sensinact.core.annotation.verb.ActParam;
import org.eclipse.sensinact.core.annotation.verb.UriParam;
import org.eclipse.sensinact.core.model.nexus.emf.EMFUtil;
import org.eclipse.sensinact.core.twin.DefaultTimedValue;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.core.whiteboard.WhiteboardHandler;
import org.osgi.util.converter.Converters;
import org.osgi.util.promise.Promise;

/**
 * Share code between ACT and GET methods
 */
abstract class AbstractResourceMethod implements WhiteboardHandler {

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

    final Function<Object, Object>[] converters;

    public AbstractResourceMethod(final Method method, final Object instance, final Long serviceId,
            final Set<String> providers) {
        super();
        this.method = method;
        this.instance = instance;
        this.serviceId = serviceId;
        this.providers = providers;

        converters = validateArgsAndMakeConverters(method);
    }

    private Function<Object, Object>[] validateArgsAndMakeConverters(Method method) {
        Parameter[] parameters = method.getParameters();
        @SuppressWarnings("unchecked")
        Function<Object, Object>[] converters = new Function[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter p = parameters[i];
            if(p.isAnnotationPresent(UriParam.class)) {
                if(!p.getType().isAssignableFrom(String.class)) {
                    throw new IllegalArgumentException("The parameter " + p.getName() + " for method " +
                            method + " is annotated with UriParam but is not compatible with type String");
                }
                if(isAnnotatedParam(p)) {
                    throw new IllegalArgumentException("The parameter " + p.getName() + " for method " +
                            method + " is annotated with UriParam as well as another parameter annotation " +
                            Arrays.toString(p.getAnnotations()));
                }
                converters[i] = Function.identity();
            } else {
                validateArg(p);
                converters[i] = makeConverter(p.getParameterizedType());
            }
        }
        return converters;
    }

    private Function<Object, Object> makeConverter(Type type) {
        Function<Object, Object> converter;
        if(isTimedValue(type)) {
            Type tvType = getTypeParameter(type);
            if(tvType == null) {
                // TODO warn no conversion
                converter = Function.identity();
            } else {
                converter = o -> {
                    if(o instanceof TimedValue<?> tv) {
                        Object val = tv.getValue();
                        if(val == null) {
                            return o;
                        } else {
                            Object converted = EMFUtil.convertToTargetType(tvType, val);
                            return converted == val ? o : new DefaultTimedValue<>(converted, tv.getTimestamp());
                        }
                    } else {
                        return new DefaultTimedValue<>(EMFUtil.convertToTargetType(tvType, o));
                    }
                };
            }
        } else if(checkIfGenericTypeIs(type, Temporal.class)) {
            converter = o -> {
                if(o instanceof TimedValue<?> tv) {
                    return EMFUtil.convertToTargetType(type, tv.getTimestamp());
                } else {
                    return EMFUtil.convertToTargetType(type, o);
                }
            };
        } else {
            converter = o -> {
                if(o instanceof TimedValue<?> tv) {
                    return EMFUtil.convertToTargetType(type, tv.getValue());
                } else {
                    return EMFUtil.convertToTargetType(type, o);
                }
            };
        }
        return converter;
    }

    /**
     * Is this parameter annotated with a relevant param annotation,
     * excluding {@link UriParam}.
     * @param param
     * @return
     */
    protected abstract boolean isAnnotatedParam(Parameter param);

    /**
     * Validate a parameter
     * @param param
     */
    protected abstract void validateArg(Parameter param);

    protected boolean isPromise(Type t) {
        return checkIfGenericTypeIs(t, Promise.class);
    }

    protected boolean isTimedValue(Type t) {
        return checkIfGenericTypeIs(t, TimedValue.class);
    }

    private boolean checkIfGenericTypeIs(Type t, Class<?> raw) {
        if(t instanceof Class<?> c) {
            return raw.equals(c);
        } else if (t instanceof ParameterizedType pt) {
            return raw.equals(pt.getRawType());
        } else if (t instanceof WildcardType wt) {
            Type[] upperBounds = wt.getUpperBounds();
            switch(upperBounds.length) {
                case 0:
                    return false;
                case 1:
                    return checkIfGenericTypeIs(upperBounds[0], raw);
                default:
                    throw new IllegalArgumentException("The type " + wt + " has more than one parameter");
            }
        } else if (t instanceof TypeVariable<?> tv) {
            Type[] upperBounds = tv.getBounds();
            switch(upperBounds.length) {
            case 0:
                return false;
            case 1:
                return checkIfGenericTypeIs(upperBounds[0], raw);
            default:
                throw new IllegalArgumentException("The type " + tv + " has more than one parameter");
            }
        }
        return false;
    }

    protected Type getTypeParameter(Type t) {
        if(t instanceof Class<?>) {
            // Not parameterised
            return null;
        } else if (t instanceof ParameterizedType pt) {
            Type[] actualTypeArguments = pt.getActualTypeArguments();
            switch(actualTypeArguments.length) {
                case 0:
                    // Not parameterised
                    return null;
                case 1:
                    // Parameterised, might be wildcard or type variable so unwrap further
                    Type typeArgument = actualTypeArguments[0];
                    return typeArgument instanceof Class || typeArgument instanceof ParameterizedType ?
                                typeArgument : getTypeParameter(typeArgument);
                default:
                    throw new IllegalArgumentException("The type " + t + " has more than one parameter");
            }
        } else if (t instanceof WildcardType wt) {
            Type[] upperBounds = wt.getUpperBounds();
            switch(upperBounds.length) {
                case 0:
                    return null;
                case 1:
                    return upperBounds[0];
                default:
                    throw new IllegalArgumentException("The type " + wt + " has more than one parameter");
            }
        } else if (t instanceof TypeVariable<?> tv) {
            Type[] bounds = tv.getBounds();
            switch(bounds.length) {
            case 0:
                return null;
            case 1:
                return bounds[0];
            default:
                throw new IllegalArgumentException("The type " + tv + " has more than one parameter");
            }
        } else {
            return null;
        }
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

    private static final Set<Class<?>> COLLECTIONS = Set.of(Collection.class, Set.class, List.class);

    public Class<?> getResourceType() {
        return inferResourceType(method.getGenericReturnType());
    }

    protected Class<?> inferResourceType(final Type genericType) {
        if(isTimedValue(genericType) || isPromise(genericType)) {
            return inferResourceType(getTypeParameter(genericType));
        }

        final Class<?> toCheck;
        final Type genericTypeToCheck;

        if(genericType instanceof Class<?> c) {
            toCheck = c;
            genericTypeToCheck = c;
        } else if(genericType instanceof ParameterizedType pt) {
            toCheck = (Class<?>) pt.getRawType();
            genericTypeToCheck = genericType;
        } else {
            // Unable to find the parameter, default to single
            toCheck  = Object.class;
            genericTypeToCheck = Object.class;
        }

        Class<?> resourceType;
        if(toCheck.isArray()) {
            resourceType = toCheck.getComponentType();
        } else if(COLLECTIONS.contains(toCheck)) {
            Type collectionType = getTypeParameter(genericTypeToCheck);
            if(collectionType instanceof ParameterizedType pt) {
                resourceType = (Class<?>) pt.getRawType();
            } else if(collectionType instanceof Class<?> c){
                resourceType = c;
            } else {
                // Not fully reified
                resourceType = Object.class;
            }
        } else {
            resourceType = toCheck;
        }
        return resourceType;
    }

    public int getUpperBound() {
        return inferUpperBound(method.getGenericReturnType());
    }

    protected int inferUpperBound(final Type genericType) {
        final Class<?> toCheck;
        if(isTimedValue(genericType) || isPromise(genericType)) {
            return inferUpperBound(getTypeParameter(genericType));
        }

        if(genericType instanceof Class<?> c) {
            toCheck = c;
        } else if(genericType instanceof ParameterizedType pt) {
            toCheck = (Class<?>) pt.getRawType();
        } else {
            // Unable to find the parameter, default to single
            toCheck  = Object.class;
        }

        int bound;
        if(toCheck.isArray()) {
            bound = -1;
        } else if(Collection.class.isAssignableFrom(toCheck)) {
            bound = -1;
        } else {
            bound = 1;
        }
        return bound;
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
                if(p.getType() != String.class) {
                    throw new IllegalArgumentException("Parameter " + i + " of method " + method + " on " + method.getDeclaringClass() +
                            " annotated with UriParam " + param.value() + " is not of type String");
                }
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
                    args[i] = converters[i].apply(params.get(key));
                } else {
                    String name = getActionParameterName(p);
                    final Object o = params.get(name);
                    args[i] = converters[i].apply(o);
                }
            }
        }
        return method.invoke(instance, args);
    }

    protected Object convertValueIfNeeded(Object o, Class<?> resourceType) throws Exception {
        if(o == null) {
            return null;
        } else if (resourceType.isInstance(o)) {
            return o;
        } else if (o instanceof Collection &&
                ((Collection<?>)o).stream().allMatch(resourceType::isInstance)) {
            return o;
        } else if (o.getClass().isArray() &&
                resourceType.isAssignableFrom(o.getClass().getComponentType())) {
            if(o.getClass().getComponentType().isPrimitive()) {
                return Converters.standardConverter().convert(o).to(List.class);
            } else {
                return Stream.of((Object[]) o).toList();
            }
        } else {
            throw new Exception("Invalid result type: " + o);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[serviceId=" + serviceId + ", providers=" + providers + "]";
    }
}
