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
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.sensinact.core.annotation.dto.NullAction;
import org.eclipse.sensinact.core.annotation.verb.ACT;
import org.eclipse.sensinact.core.annotation.verb.ACT.ACTs;
import org.eclipse.sensinact.core.annotation.verb.GET;
import org.eclipse.sensinact.core.annotation.verb.GET.GETs;
import org.eclipse.sensinact.core.annotation.verb.SET;
import org.eclipse.sensinact.core.annotation.verb.SET.SETs;
import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.metrics.IMetricTimer;
import org.eclipse.sensinact.core.metrics.IMetricsManager;
import org.eclipse.sensinact.core.model.Model;
import org.eclipse.sensinact.core.model.Resource;
import org.eclipse.sensinact.core.model.ResourceBuilder;
import org.eclipse.sensinact.core.model.ResourceType;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.model.Service;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.core.model.nexus.ModelNexus;
import org.eclipse.sensinact.core.twin.impl.TimedValueImpl;
import org.osgi.framework.Constants;
import org.osgi.util.promise.Deferred;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SensinactWhiteboard {

    private static final Logger LOG = LoggerFactory.getLogger(ModelNexus.class);

    /**
     * Gateway thread, to execute model updates
     */
    private final GatewayThread gatewayThread;

    /**
     * Metrics manager
     */
    private final IMetricsManager metrics;

    /**
     * Links a service ID to its dynamic ACT resources
     */
    private final Map<Long, List<RegistryKey>> serviceIdToActMethods = new ConcurrentHashMap<>();

    /**
     * Links a service ID to its dynamic GET resources
     */
    private final Map<Long, List<RegistryKey>> serviceIdToGetMethods = new ConcurrentHashMap<>();

    /**
     * Links a service ID to its dynamic SET resources
     */
    private final Map<Long, List<RegistryKey>> serviceIdToSetMethods = new ConcurrentHashMap<>();

    /**
     * Links a resource key to its ACT methods
     */
    private final Map<RegistryKey, List<ActMethod>> actMethodRegistry = new ConcurrentHashMap<>();

    /**
     * Links a resource key to its GET methods
     */
    private final Map<RegistryKey, List<GetMethod>> getMethodRegistry = new ConcurrentHashMap<>();

    /**
     * Links a resource key to its SET methods
     */
    private final Map<RegistryKey, List<SetMethod>> setMethodRegistry = new ConcurrentHashMap<>();

    /**
     * The white board has its own promise factory with a thread pool. The dynamic
     * calls are executed there while the twin updates are done in the gateway
     * thread.
     */
    private final PromiseFactory promiseFactory = new PromiseFactory(
            Executors.newCachedThreadPool(r -> new Thread(r, "Eclipse sensiNact Whiteboard Worker")),
            Executors.newScheduledThreadPool(5, r -> new Thread(r, "Eclipse sensiNact Whiteboard Scheduler")));

    /**
     * Stores the promise that will be returned by a dynamic get. Allows to share
     * the same promise when multiple dynamic GET occur.
     */
    private final Map<RegistryKey, Promise<TimedValue<?>>> concurrentGetHolder = new ConcurrentHashMap<>();

    public SensinactWhiteboard(GatewayThread gatewayThread, IMetricsManager metrics) {
        this.gatewayThread = gatewayThread;
        this.metrics = metrics;
    }

    /**
     * A new white board service has been found
     *
     * @param service Service instance
     * @param props   Service properties
     */
    public void addWhiteboardService(Object service, Map<String, Object> props) {
        Long serviceId = (Long) props.get(Constants.SERVICE_ID);

        Set<String> providers = toSet(props.get("sensiNact.provider.name"));

        Class<?> clz = service.getClass();

        List<Method> actMethods = findMethods(clz, List.of(ACT.class, ACTs.class));
        handleActMethods(serviceId, service, providers, actMethods);

        List<Method> getMethods = findMethods(clz, List.of(GET.class, GETs.class));
        List<Method> setMethods = findMethods(clz, List.of(SET.class, SETs.class));
        handlePullMethods(serviceId, service, providers, getMethods, setMethods);
    }

    /**
     * Looks for the methods of the given class that has at least one of the given
     * annotations
     *
     * @param clz         Class to analyze
     * @param annotations Annotations looked for
     * @return Methods from the class that are annotated
     */
    private List<Method> findMethods(Class<?> clz, Collection<Class<? extends Annotation>> annotations) {
        return Stream
                .concat(Arrays.stream(clz.getMethods()),
                        Arrays.stream(clz.getInterfaces()).flatMap(c -> Arrays.stream(c.getMethods())))
                .filter(m -> annotations.stream().anyMatch(a -> m.isAnnotationPresent(a))).collect(Collectors.toList());
    }

    /**
     * Updates the references to the given service
     *
     * @param <T>               Kind of call handler
     * @param kind              Name of the kind of call handler (for logging)
     * @param serviceId         Updated service ID
     * @param providers         Set of providers handled by the service
     * @param serviceKeysHolder Registry associating the service ID to the list of
     *                          handled resources
     * @param methodRegistry    Registry associating handled resources to the
     *                          handling method
     */
    private <T extends AbstractResourceMethod> void updateServiceReferences(final String kind, final Long serviceId,
            final Set<String> providers, final Map<Long, List<RegistryKey>> serviceKeysHolder,
            final Map<RegistryKey, List<T>> methodRegistry) {
        for (RegistryKey key : serviceKeysHolder.getOrDefault(serviceId, List.of())) {
            methodRegistry.computeIfPresent(key, (x, v) -> {
                for (int i = 0; i < v.size(); i++) {
                    T actMethod = v.get(i);
                    if (actMethod.serviceId.equals(serviceId)) {
                        if (providers.equals(actMethod.providers)
                                || (!providers.isEmpty() && !actMethod.providers.isEmpty())) {
                            LOG.debug("The update to the whiteboard {} service {} did not change the resource {}", kind,
                                    serviceId, key);
                            return v;
                        } else {
                            LOG.debug(
                                    "The update to the whiteboard {} service {} changed the resource {} from providers {} to providers {}",
                                    kind, serviceId, key, actMethod.providers, providers);
                            List<T> result = new ArrayList<>(v.size());
                            result.addAll(v.subList(0, i));
                            result.addAll(v.subList(i + 1, v.size()));
                            result.add(actMethod);
                            return result.isEmpty() ? null : result;
                        }
                    }
                }
                LOG.warn("No match for the act method {} was found for service {}", key, serviceId);
                return v;
            });
        }
    }

    /**
     * A known white board service has been updated
     *
     * @param service Service instance
     * @param props   New service properties
     */
    public void updatedWhiteboardService(Object service, Map<String, Object> props) {
        Long serviceId = (Long) props.get(Constants.SERVICE_ID);
        Set<String> providers = toSet(props.get("sensiNact.provider.name"));

        updateServiceReferences("act", serviceId, providers, serviceIdToActMethods, actMethodRegistry);
        updateServiceReferences("get", serviceId, providers, serviceIdToGetMethods, getMethodRegistry);
        updateServiceReferences("set", serviceId, providers, serviceIdToSetMethods, setMethodRegistry);
    }

    /**
     * Cleans up references to the given service
     *
     * @param <T>               Kind of call handler
     * @param serviceId         ID of the removed service
     * @param serviceKeysHolder Registry associating the service ID to the list of
     *                          handled resources
     * @param methodRegistry    Registry associating handled resources to the
     *                          handling method
     */
    private <T extends AbstractResourceMethod> void clearServiceReferences(final Long serviceId,
            final Map<Long, List<RegistryKey>> serviceKeysHolder, final Map<RegistryKey, List<T>> methodRegistry) {
        final List<RegistryKey> keys = serviceKeysHolder.remove(serviceId);
        if (keys != null) {
            for (RegistryKey key : keys) {
                methodRegistry.computeIfPresent(key, (x, v) -> {
                    List<T> l = v.stream().filter(a -> !serviceId.equals(a.serviceId)).collect(toList());
                    if (l.equals(v)) {
                        return v;
                    }
                    return l.isEmpty() ? null : l;
                });
            }
        }
    }

    /**
     * A known white board service has been removed
     *
     * @param service Service instance
     * @param props   Last service properties
     */
    public void removeWhiteboardService(Object service, Map<String, Object> props) {
        final Long serviceId = (Long) props.get(Constants.SERVICE_ID);
        clearServiceReferences(serviceId, serviceIdToActMethods, actMethodRegistry);
        clearServiceReferences(serviceId, serviceIdToGetMethods, getMethodRegistry);
        clearServiceReferences(serviceId, serviceIdToSetMethods, setMethodRegistry);
    }

    private Set<String> toSet(Object object) {
        Set<String> set;
        if (object == null) {
            set = Collections.emptySet();
        } else if (object.getClass().isArray()) {
            int length = Array.getLength(object);
            set = new HashSet<>(length);
            for (int i = 0; i < length; i++) {
                set.addAll(toSet(Array.get(object, i)));
            }
            return set;
        } else if (object instanceof Collection) {
            set = ((Collection<?>) object).stream().flatMap(o -> toSet(o).stream()).collect(Collectors.toSet());
        } else {
            set = Collections.singleton(object.toString());
        }
        return set;
    }

    private void handleActMethods(Long serviceId, Object service, Set<String> providers, List<Method> actMethods) {
        for (Method actMethod : actMethods) {
            ActMethod am = new ActMethod(actMethod, service, serviceId, providers);
            if (actMethod.isAnnotationPresent(ACT.class)) {
                ACT act = actMethod.getAnnotation(ACT.class);
                processActMethod(am, act);
            }
            if (actMethod.isAnnotationPresent(ACTs.class)) {
                ACTs acts = actMethod.getAnnotation(ACTs.class);
                for (ACT act : acts.value()) {
                    processActMethod(am, act);
                }
            }
        }
    }

    private void handlePullMethods(Long serviceId, Object service, Set<String> providers, List<Method> getMethods,
            List<Method> setMethods) {
        // We can find different behaviors for the handling of NullAction
        final Map<NullAction, GetMethod> cachedMethod = new HashMap<>();

        final class MethodHolder<A extends Annotation, RM extends AbstractResourceMethod> {
            A annotation;
            RM rcMethod;
        }

        // List the resources that are GET only, SET only or both
        final Set<RegistryKey> resources = new LinkedHashSet<>();
        final Map<RegistryKey, List<MethodHolder<GET, GetMethod>>> listGetMethods = new HashMap<>();
        final Map<RegistryKey, List<MethodHolder<SET, SetMethod>>> listSetMethods = new HashMap<>();

        // Walk GET-annotated methods
        for (Method annotatedMethod : getMethods) {
            if (annotatedMethod.isAnnotationPresent(GET.class)) {
                final GET get = annotatedMethod.getAnnotation(GET.class);
                final RegistryKey key = new RegistryKey(get.model(), get.service(), get.resource());
                resources.add(key);

                final GetMethod getMethod = cachedMethod.computeIfAbsent(get.onNull(),
                        (onNull) -> new GetMethod(annotatedMethod, service, serviceId, providers, onNull));

                final MethodHolder<GET, GetMethod> getHolder = new MethodHolder<>();
                getHolder.annotation = get;
                getHolder.rcMethod = getMethod;
                listGetMethods.computeIfAbsent(key, k -> new ArrayList<MethodHolder<GET, GetMethod>>()).add(getHolder);
            }

            if (annotatedMethod.isAnnotationPresent(GETs.class)) {
                final GETs gets = annotatedMethod.getAnnotation(GETs.class);
                for (GET get : gets.value()) {
                    final RegistryKey key = new RegistryKey(get.model(), get.service(), get.resource());
                    resources.add(key);

                    final GetMethod getMethod = cachedMethod.computeIfAbsent(get.onNull(),
                            (onNull) -> new GetMethod(annotatedMethod, service, serviceId, providers, onNull));

                    final MethodHolder<GET, GetMethod> getHolder = new MethodHolder<>();
                    getHolder.annotation = get;
                    getHolder.rcMethod = getMethod;
                    listGetMethods.computeIfAbsent(key, k -> new ArrayList<MethodHolder<GET, GetMethod>>())
                            .add(getHolder);
                }
            }
        }

        // Walk SET-annotated methods
        for (Method annotatedMethod : setMethods) {
            final SetMethod setMethod = new SetMethod(annotatedMethod, service, serviceId, providers);

            if (annotatedMethod.isAnnotationPresent(SET.class)) {
                final SET set = annotatedMethod.getAnnotation(SET.class);
                final RegistryKey key = new RegistryKey(set.model(), set.service(), set.resource());
                resources.add(key);

                final MethodHolder<SET, SetMethod> setHolder = new MethodHolder<>();
                setHolder.annotation = set;
                setHolder.rcMethod = setMethod;
                listSetMethods.computeIfAbsent(key, k -> new ArrayList<MethodHolder<SET, SetMethod>>()).add(setHolder);
            }
            if (annotatedMethod.isAnnotationPresent(SETs.class)) {
                final SETs sets = annotatedMethod.getAnnotation(SETs.class);
                for (SET set : sets.value()) {
                    final RegistryKey key = new RegistryKey(set.model(), set.service(), set.resource());
                    resources.add(key);
                    final MethodHolder<SET, SetMethod> setHolder = new MethodHolder<>();
                    setHolder.annotation = set;
                    setHolder.rcMethod = setMethod;
                    listSetMethods.computeIfAbsent(key, k -> new ArrayList<MethodHolder<SET, SetMethod>>())
                            .add(setHolder);
                }
            }
        }

        for (final RegistryKey key : resources) {
            final List<MethodHolder<GET, GetMethod>> definedGetMethods = listGetMethods.get(key);
            final List<MethodHolder<SET, SetMethod>> definedSetMethods = listSetMethods.get(key);
            final boolean hasGet = definedGetMethods != null && !definedGetMethods.isEmpty();
            final boolean hasSet = definedSetMethods != null && !definedSetMethods.isEmpty();

            if (hasGet) {
                for (MethodHolder<GET, GetMethod> getHolder : definedGetMethods) {
                    processGetMethod(key, getHolder.rcMethod, getHolder.annotation, hasSet);
                }
            }

            if (hasSet) {
                for (MethodHolder<SET, SetMethod> setHolder : definedSetMethods) {
                    processSetMethod(key, setHolder.rcMethod, setHolder.annotation, hasGet);
                }
            }
        }
    }

    private <T extends AbstractResourceMethod> void processAnnotatedMethod(final RegistryKey key,
            final Predicate<ResourceType> validateResourceType,
            final Consumer<ResourceBuilder<?, Object>> builderCaller, T method,
            Map<RegistryKey, List<T>> methodsRegistry, Map<Long, List<RegistryKey>> serviceIdRegistry) {

        serviceIdRegistry.merge(method.serviceId, List.of(key),
                (k, v) -> concat(v.stream(), of(key)).collect(toList()));

        methodsRegistry.merge(key, List.of(method), (k, v) -> {
            Stream<T> stream;
            if (method.isCatchAll()) {
                T previous = v.get(v.size() - 1);
                if (previous.isCatchAll()) {
                    LOG.warn("There are two catch all services {} and {} defined for GET resource {}",
                            previous.serviceId, method.serviceId, key);
                }
                stream = concat(v.stream(), of(method));
            } else {
                if (v.stream().anyMatch(a -> a.overlaps(method))) {
                    LOG.warn("There are overlapping services defined for GET resource {}: {}", key, v);
                }
                stream = concat(of(method), v.stream());
            }
            return stream.collect(toList());
        });

        gatewayThread.execute(new AbstractSensinactCommand<Void>() {
            @Override
            protected Promise<Void> call(SensinactDigitalTwin twin, SensinactModelManager modelMgr,
                    PromiseFactory promiseFactory) {
                ResourceBuilder<?, Object> builder = null;
                Resource resource = null;

                Model model = modelMgr.getModel(key.getModel());
                if (model == null) {
                    builder = modelMgr.createModel(key.getModel()).withService(key.getService())
                            .withResource(key.getResource());
                } else {
                    Service service = model.getServices().get(key.getService());
                    if (service == null) {
                        builder = model.createService(key.getService()).withResource(key.getResource());
                    } else {
                        resource = service.getResources().get(key.getResource());
                        if (resource == null) {
                            builder = service.createResource(key.getResource());
                        }
                    }
                }

                if (builder != null) {
                    // Construct the resource
                    builderCaller.accept(builder);
                } else if (resource != null) {
                    // Resource exists, check if we can update it
                    ResourceType type = resource.getResourceType();
                    if (!validateResourceType.test(type)) {
                        LOG.error("The resource {} in service {} already exists for the model {} as type {}",
                                key.getResource(), key.getService(), key.getModel(), type);
                        return promiseFactory.failed(
                                new IllegalStateException("Updating resource of type " + type + " is not allowed"));
                    }
                }

                return promiseFactory.resolved(null);
            }
        });
    }

    private Class<?> getType(Class<?> methodReturnType, Class<?> givenType) {
        return givenType != null ? givenType : methodReturnType;
    }

    private void processActMethod(ActMethod method, ACT annotation) {
        RegistryKey key = new RegistryKey(annotation.model(), annotation.service(), annotation.resource());
        processAnnotatedMethod(key, (type) -> type == ResourceType.ACTION,
                (b) -> b.withType(method.getReturnType()).withAction(method.getNamedParameterTypes()).buildAll(),
                method, actMethodRegistry, serviceIdToActMethods);
    }

    private void processGetMethod(final RegistryKey key, final GetMethod method, final GET annotation,
            final boolean hasSet) {
        processAnnotatedMethod(key, (type) -> type != ResourceType.ACTION, (b) -> {
            ResourceBuilder<?, ?> builder = b.withType(getType(method.getReturnType(), annotation.type())).withGetter()
                    .withGetterCache(Duration.of(annotation.cacheDuration(), annotation.cacheDurationUnit()));
            if (hasSet) {
                builder = builder.withSetter();
            }
            builder.buildAll();
        }, method, getMethodRegistry, serviceIdToGetMethods);
    }

    private void processSetMethod(final RegistryKey key, final SetMethod method, final SET annotation,
            final boolean hasGet) {
        processAnnotatedMethod(key, (type) -> type != ResourceType.ACTION, (b) -> {
            ResourceBuilder<?, ?> builder = b.withType(getType(method.getReturnType(), annotation.type())).withSetter();
            if (hasGet) {
                builder = builder.withGetter();
            }
            builder.buildAll();
        }, method, setMethodRegistry, serviceIdToSetMethods);
    }

    public Promise<Object> act(String model, String provider, String service, String resource,
            Map<String, Object> arguments) {
        RegistryKey key = new RegistryKey(model, service, resource);

        Optional<ActMethod> opt = actMethodRegistry.getOrDefault(key, List.of()).stream()
                .filter(a -> a.providers.isEmpty() || a.providers.contains(provider)).findFirst();

        if (opt.isEmpty()) {
            return promiseFactory.failed(new NoSuchElementException(
                    String.format("No suitable provider for model %s, provider %s, service %s, resource %s", model,
                            provider, service, resource)));
        } else {
            Deferred<Object> d = promiseFactory.deferred();
            final IMetricTimer overallTimer = metrics.withTimers("sensinact.whiteboard.act.request",
                    "sensinact.whiteboard.act.request." + String.join(".", model, service, resource),
                    "sensinact.whiteboard.act.request." + String.join(".", provider, service, resource));
            promiseFactory.executor().execute(() -> {
                try (final IMetricTimer timer = metrics.withTimers("sensinact.whiteboard.act.task",
                        "sensinact.whiteboard.act.task." + String.join(".", model, service, resource),
                        "sensinact.whiteboard.act.task." + String.join(".", provider, service, resource))) {
                    Object o = opt.get().invoke(model, provider, service, resource, arguments);
                    if (o instanceof Promise) {
                        d.resolveWith((Promise<?>) o);
                    } else {
                        d.resolve(o);
                    }
                } catch (Exception e) {
                    d.fail(e);
                }
            });
            return d.getPromise().onResolve(() -> overallTimer.close());
        }
    }

    @SuppressWarnings("unchecked")
    public <T> Promise<TimedValue<T>> pullValue(String model, String provider, String service, String resource,
            Class<T> type, TimedValue<T> cachedValue, Consumer<TimedValue<T>> gatewayUpdate) {
        // Find the handler method
        RegistryKey key = new RegistryKey(model, service, resource);

        Optional<GetMethod> opt = getMethodRegistry.getOrDefault(key, List.of()).stream()
                .filter(a -> a.providers.isEmpty() || a.providers.contains(provider)).findFirst();

        if (opt.isEmpty()) {
            // No method
            return promiseFactory.failed(new NoSuchElementException(
                    String.format("No suitable provider for model %s, provider %s, service %s, resource %s", model,
                            provider, service, resource)));
        } else {
            // Get/Set the current execution for that GET
            synchronized (concurrentGetHolder) {
                final Promise<TimedValue<?>> currentPromise = concurrentGetHolder.get(key);
                if (currentPromise != null) {
                    // Call is already running: return its promise
                    return currentPromise.map(tv -> (TimedValue<T>) tv);
                }

                final Deferred<TimedValue<T>> d = promiseFactory.deferred();
                final GetMethod getMethod = opt.get();

                final IMetricTimer overallTimer = metrics.withTimers("sensinact.whiteboard.pull.request",
                        "sensinact.whiteboard.pull.request." + String.join(".", model, service, resource),
                        "sensinact.whiteboard.pull.request." + String.join(".", provider, service, resource));
                promiseFactory.executor().execute(() -> {
                    try (final IMetricTimer timer = metrics.withTimers("sensinact.whiteboard.pull.task",
                            "sensinact.whiteboard.pull.task." + String.join(".", model, service, resource),
                            "sensinact.whiteboard.pull.task." + String.join(".", provider, service, resource))) {
                        final Object result = getMethod.invoke(model, provider, service, resource, type, cachedValue);
                        if (result instanceof Promise) {
                            d.resolveWith((Promise<TimedValue<T>>) result);
                        } else if (result instanceof TimedValue) {
                            d.resolve((TimedValue<T>) result);
                        } else if (result == null) {
                            switch (getMethod.actionOnNull()) {
                            case IGNORE:
                                d.resolve(null);
                                break;

                            case UPDATE:
                                d.resolve(new TimedValueImpl<T>(null));
                                break;
                            }
                        } else if (type.isAssignableFrom(result.getClass())) {
                            d.resolve(new TimedValueImpl<T>(type.cast(result)));
                        } else {
                            d.fail(new Exception("Invalid result type: " + result.getClass()));
                        }
                    } catch (Exception e) {
                        d.fail(e);
                    }
                });

                Consumer<TimedValue<T>> coCall = (v) -> {
                    try {
                        if (gatewayUpdate != null) {
                            gatewayUpdate.accept(v);
                        }
                    } finally {
                        concurrentGetHolder.remove(key);
                    }
                };

                final Promise<TimedValue<T>> promise = d.getPromise().onResolve(() -> overallTimer.close());
                return runOnGateway(promise, coCall);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> Promise<TimedValue<T>> pushValue(String model, String provider, String service, String resource,
            Class<T> type, TimedValue<T> cachedValue, TimedValue<T> newValue, Consumer<TimedValue<T>> gatewayUpdate) {
        RegistryKey key = new RegistryKey(model, service, resource);

        Optional<SetMethod> opt = setMethodRegistry.getOrDefault(key, List.of()).stream()
                .filter(a -> a.providers.isEmpty() || a.providers.contains(provider)).findFirst();

        if (opt.isEmpty()) {
            return promiseFactory.failed(new NoSuchElementException(
                    String.format("No suitable provider for model %s, provider %s, service %s, resource %s", model,
                            provider, service, resource)));
        } else {
            final Deferred<TimedValue<T>> d = promiseFactory.deferred();
            final SetMethod setMethod = opt.get();

            final IMetricTimer overallTimer = metrics.withTimers("sensinact.whiteboard.pull.request",
                    "sensinact.whiteboard.pull.request." + String.join(".", model, service, resource),
                    "sensinact.whiteboard.pull.request." + String.join(".", provider, service, resource));
            promiseFactory.executor().execute(() -> {
                try (final IMetricTimer timer = metrics.withTimers("sensinact.whiteboard.push.task",
                        "sensinact.whiteboard.push.task." + String.join(".", model, service, resource),
                        "sensinact.whiteboard.push.task." + String.join(".", provider, service, resource))) {
                    final Object o = setMethod.invoke(model, provider, service, resource, type, cachedValue, newValue);
                    if (o instanceof Promise) {
                        d.resolveWith((Promise<TimedValue<T>>) o);
                    } else if (o instanceof TimedValue) {
                        d.resolve((TimedValue<T>) o);
                    } else if (o == null) {
                        d.resolve(null);
                    } else if (type.isAssignableFrom(o.getClass())) {
                        d.resolve(new TimedValueImpl<T>(type.cast(o)));
                    } else {
                        d.fail(new Exception("Invalid result type: " + o.getClass()));
                    }
                } catch (Exception e) {
                    d.fail(e);
                }
            });

            final Promise<TimedValue<T>> promise = d.getPromise().onResolve(() -> overallTimer.close());
            return runOnGateway(promise, gatewayUpdate);
        }
    }

    /**
     * Runs the given consumer in the gateway thread.
     *
     * If <code>gatewayUpdate</code> is null, the returned promise is returned by
     * the promise factory of this white board.
     *
     * @param <T>           Resource value type
     * @param promisedValue Resource value promise (should already be resolved)
     * @param gatewayUpdate Method to call in the gateway thread.
     * @return
     */
    private <T> Promise<TimedValue<T>> runOnGateway(final Promise<TimedValue<T>> promisedValue,
            final Consumer<TimedValue<T>> gatewayUpdate) {
        final PromiseFactory gatewayPromiseFactory = gatewayThread.getPromiseFactory();
        final Deferred<TimedValue<T>> deferred = gatewayPromiseFactory.deferred();
        if (gatewayUpdate == null) {
            // Return the promised value from the gateway thread
            deferred.resolveWith(promisedValue);
        } else {
            // We are supposed to be called when the promise is resolved, by get the value
            // outside the gateway thread anyway
            promisedValue.onResolve(() -> {
                try {
                    final Throwable t = promisedValue.getFailure();
                    if (t == null) {
                        final TimedValue<T> value = promisedValue.getValue();
                        deferred.resolveWith(gatewayThread.execute(new AbstractSensinactCommand<TimedValue<T>>() {
                            @Override
                            protected Promise<TimedValue<T>> call(SensinactDigitalTwin twin,
                                    SensinactModelManager modelMgr, PromiseFactory pf) {
                                try {
                                    gatewayUpdate.accept(value);
                                    return pf.resolved(value);
                                } catch (Exception e) {
                                    return pf.failed(e);
                                }
                            }
                        }));
                    } else {
                        deferred.fail(t);
                    }
                } catch (InterruptedException e) {
                    deferred.fail(e);
                } catch (InvocationTargetException e) {
                    deferred.fail(e.getCause());
                }
            });
        }
        return deferred.getPromise();
    }
}
