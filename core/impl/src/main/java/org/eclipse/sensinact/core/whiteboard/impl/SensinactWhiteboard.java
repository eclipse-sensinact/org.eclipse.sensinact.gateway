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
import static java.util.stream.Stream.empty;
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
import org.eclipse.sensinact.core.model.nexus.ModelNexus;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.core.whiteboard.WhiteboardAct;
import org.eclipse.sensinact.core.whiteboard.WhiteboardActDescription;
import org.eclipse.sensinact.core.whiteboard.WhiteboardGet;
import org.eclipse.sensinact.core.whiteboard.WhiteboardHandler;
import org.eclipse.sensinact.core.whiteboard.WhiteboardResourceDescription;
import org.eclipse.sensinact.core.whiteboard.WhiteboardSet;
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
    private final Map<RegistryKey, List<WhiteboardContext<WhiteboardAct<?>>>> actMethodRegistry = new ConcurrentHashMap<>();

    /**
     * Links a resource key to its GET methods
     */
    private final Map<RegistryKey, List<WhiteboardContext<WhiteboardGet<?>>>> getMethodRegistry = new ConcurrentHashMap<>();

    /**
     * Links a resource key to its SET methods
     */
    private final Map<RegistryKey, List<WhiteboardContext<WhiteboardSet<?>>>> setMethodRegistry = new ConcurrentHashMap<>();

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
     * Register a whiteboard handler service. The handler must provider either
     * {@link WhiteboardAct} or {@link WhiteboardGet} and/or {@link WhiteboardSet}
     *
     * @param handler The whiteboard handler service
     * @param props   The handler service properties
     */
    public void addWhiteboardHandler(WhiteboardHandler<?> handler, Map<String, Object> props) {
        final Long serviceId = (Long) props.get(Constants.SERVICE_ID);
        Set<String> providers = toSet(props.get("sensiNact.provider.name"));
        RegistryKey key = new RegistryKey((String) props.get("sensiNact.whiteboard.modelPackageUri"),
                (String) props.get("sensiNact.whiteboard.model"), (String) props.get("sensiNact.whiteboard.service"),
                (String) props.get("sensiNact.whiteboard.resource"));

        boolean isGet = handler instanceof WhiteboardGet;
        boolean isSet = handler instanceof WhiteboardSet;
        boolean isValue = isGet || isSet;
        boolean isAct = handler instanceof WhiteboardAct;
        if (!isAct && !isValue) {
            LOG.error("Whiteboard handler service {} for {} doesn't provider meaningful interfaces", serviceId, key);
            return;
        }

        final Boolean createResource = (Boolean) props.get("sensiNact.whiteboard.create");
        if (createResource != null && createResource.booleanValue()) {
            if (isAct && isValue) {
                LOG.error("Can't create a resource if its handler is both made for action and value resources");
                return;
            }

            if (isValue) {
                if (handler instanceof WhiteboardResourceDescription) {
                    WhiteboardResourceDescription<?> description = (WhiteboardResourceDescription<?>) handler;
                    makeResource(key, (type) -> type != ResourceType.ACTION, (b) -> {
                        ResourceBuilder<?, ?> builder = b.withType(description.getResourceType()).withGetter()
                                .withGetterCache(description.getCacheDuration()).withSetter();
                        builder.buildAll();
                    });
                } else {
                    LOG.error("Can't create resource as whiteboard handler is not a WhiteboardResourceDescription");
                    return;
                }
            } else if (isAct) {
                if (handler instanceof WhiteboardActDescription) {
                    WhiteboardActDescription<?> description = (WhiteboardActDescription<?>) handler;
                    makeResource(key, (type) -> type == ResourceType.ACTION,
                            (b) -> b.withType(description.getReturnType())
                                    .withAction(description.getNamedParameterTypes()).buildAll());
                } else {
                    LOG.error("Can't create action resource as whiteboard handler is not a WhiteboardActDescription");
                    return;
                }
            }
        }

        // Resource has been created, register the handler
        if (isAct) {
            final WhiteboardContext<WhiteboardAct<?>> ctx = new WhiteboardContext<>(serviceId,
                    (WhiteboardAct<?>) handler, providers);
            storeWhiteboardHandler(ctx, key, serviceIdToActMethods, actMethodRegistry);
        }

        if (isGet) {
            final WhiteboardContext<WhiteboardGet<?>> ctx = new WhiteboardContext<>(serviceId,
                    (WhiteboardGet<?>) handler, providers);
            storeWhiteboardHandler(ctx, key, serviceIdToGetMethods, getMethodRegistry);
        }

        if (isSet) {
            final WhiteboardContext<WhiteboardSet<?>> ctx = new WhiteboardContext<>(serviceId,
                    (WhiteboardSet<?>) handler, providers);
            storeWhiteboardHandler(ctx, key, serviceIdToSetMethods, setMethodRegistry);
        }
    }

    public void updatedWhiteboardHandler(WhiteboardHandler<?> handler, Map<String, Object> props) {
        Long serviceId = (Long) props.get(Constants.SERVICE_ID);
        Set<String> providers = toSet(props.get("sensiNact.provider.name"));

        updateServiceReferences("act", serviceId, providers, serviceIdToActMethods, actMethodRegistry);
        updateServiceReferences("get", serviceId, providers, serviceIdToGetMethods, getMethodRegistry);
        updateServiceReferences("set", serviceId, providers, serviceIdToSetMethods, setMethodRegistry);
    }

    public void removeWhiteboardHandler(WhiteboardHandler<?> handler, Map<String, Object> props) {
        final Long serviceId = (Long) props.get(Constants.SERVICE_ID);
        clearServiceReferences(serviceId, serviceIdToActMethods, actMethodRegistry);
        clearServiceReferences(serviceId, serviceIdToGetMethods, getMethodRegistry);
        clearServiceReferences(serviceId, serviceIdToSetMethods, setMethodRegistry);
    }

    /**
     * Stores a new handler to the given registries
     *
     * @param <T>            Handler type
     * @param ctx            Handler context
     * @param key            Registry key (path to resource)
     * @param keyRegistry    Service ID to registry keys map
     * @param methodRegistry Registry key to handlers contexts map
     */
    private <T extends WhiteboardHandler<?>> void storeWhiteboardHandler(WhiteboardContext<T> ctx, RegistryKey key,
            Map<Long, List<RegistryKey>> keyRegistry, Map<RegistryKey, List<WhiteboardContext<T>>> methodRegistry) {
        synchronized (keyRegistry) {
            keyRegistry.merge(ctx.serviceId, List.of(key), (k, v) -> concat(v.stream(), of(key)).collect(toList()));
        }

        synchronized (methodRegistry) {
            // FIXME: handle that better
            // Insert that handler as priority
            methodRegistry.merge(key, List.of(ctx), (k, v) -> concat(of(ctx), v.stream()).collect(toList()));
        }
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
    private <T extends WhiteboardHandler<?>> void updateServiceReferences(final String kind, final Long serviceId,
            final Set<String> providers, final Map<Long, List<RegistryKey>> serviceKeysHolder,
            final Map<RegistryKey, List<WhiteboardContext<T>>> methodRegistry) {
        for (RegistryKey key : serviceKeysHolder.getOrDefault(serviceId, List.of())) {
            methodRegistry.computeIfPresent(key, (x, v) -> {
                for (int i = 0; i < v.size(); i++) {
                    WhiteboardContext<T> context = v.get(i);
                    if (context.serviceId.equals(serviceId)) {
                        if (providers.equals(context.providers)
                                || (!providers.isEmpty() && !context.providers.isEmpty())) {
                            LOG.debug("The update to the whiteboard {} service {} did not change the resource {}", kind,
                                    serviceId, key);
                            return v;
                        } else {
                            LOG.debug(
                                    "The update to the whiteboard {} service {} changed the resource {} from providers {} to providers {}",
                                    kind, serviceId, key, context.providers, providers);
                            List<WhiteboardContext<T>> result = new ArrayList<>(v.size());
                            result.addAll(v.subList(0, i));
                            result.addAll(v.subList(i + 1, v.size()));
                            result.add(context);
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
    private <T extends WhiteboardHandler<?>> void clearServiceReferences(final Long serviceId,
            final Map<Long, List<RegistryKey>> serviceKeysHolder,
            final Map<RegistryKey, List<WhiteboardContext<T>>> methodRegistry) {
        final List<RegistryKey> keys = serviceKeysHolder.remove(serviceId);
        if (keys != null) {
            for (RegistryKey key : keys) {
                methodRegistry.computeIfPresent(key, (x, v) -> {
                    List<WhiteboardContext<T>> l = v.stream().filter(a -> !serviceId.equals(a.serviceId))
                            .collect(toList());
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
            WhiteboardContext<ActMethod> ctx = new WhiteboardContext<>(serviceId, am);
            if (actMethod.isAnnotationPresent(ACT.class)) {
                ACT act = actMethod.getAnnotation(ACT.class);
                processActMethod(ctx, act);
            }
            if (actMethod.isAnnotationPresent(ACTs.class)) {
                ACTs acts = actMethod.getAnnotation(ACTs.class);
                for (ACT act : acts.value()) {
                    processActMethod(ctx, act);
                }
            }
        }
    }

    static final class MethodHolder<A extends Annotation, RM extends AbstractResourceMethod> {
        final A annotation;
        final WhiteboardContext<RM> rcMethod;

        public MethodHolder(A annotation, WhiteboardContext<RM> rcMethod) {
            this.annotation = annotation;
            this.rcMethod = rcMethod;
        }
    }

    private void handlePullMethods(Long serviceId, Object service, Set<String> providers, List<Method> getMethods,
            List<Method> setMethods) {

        // List the resources that are GET only, SET only or both
        final Set<RegistryKey> resources = new LinkedHashSet<>();
        final Map<RegistryKey, List<MethodHolder<GET, GetMethod>>> listGetMethods = new HashMap<>();
        final Map<RegistryKey, List<MethodHolder<SET, SetMethod>>> listSetMethods = new HashMap<>();

        // Walk GET-annotated methods
        for (Method annotatedMethod : getMethods) {
            // We can find different behaviors for the handling of NullAction
            final Map<NullAction, GetMethod> cachedMethod = new HashMap<>();
            Stream<GET> getStream = Optional.ofNullable(annotatedMethod.getAnnotation(GET.class)).map(Stream::of)
                    .orElse(empty());

            Stream<GET> getsStream = Optional.ofNullable(annotatedMethod.getAnnotation(GETs.class)).map(GETs::value)
                    .map(Arrays::stream).orElse(empty());

            Stream.concat(getStream, getsStream).forEach(get -> {
                final RegistryKey key = new RegistryKey(get.modelPackageUri(), get.model(), get.service(),
                        get.resource());
                resources.add(key);

                final GetMethod getMethod = cachedMethod.computeIfAbsent(get.onNull(),
                        (onNull) -> new GetMethod(annotatedMethod, service, serviceId, providers, onNull));

                listGetMethods.computeIfAbsent(key, k -> new ArrayList<>())
                        .add(new MethodHolder<>(get, new WhiteboardContext<>(serviceId, getMethod, providers)));
            });
        }

        // Walk SET-annotated methods
        for (Method annotatedMethod : setMethods) {
            final SetMethod setMethod = new SetMethod(annotatedMethod, service, serviceId, providers);

            Stream<SET> setStream = Optional.ofNullable(annotatedMethod.getAnnotation(SET.class)).map(Stream::of)
                    .orElse(empty());

            Stream<SET> setsStream = Optional.ofNullable(annotatedMethod.getAnnotation(SETs.class)).map(SETs::value)
                    .map(Arrays::stream).orElse(empty());

            Stream.concat(setStream, setsStream).forEach(set -> {
                final RegistryKey key = new RegistryKey(set.modelPackageUri(), set.model(), set.service(),
                        set.resource());
                resources.add(key);

                final MethodHolder<SET, SetMethod> setHolder = new MethodHolder<>(set,
                        new WhiteboardContext<>(serviceId, setMethod, providers));
                listSetMethods.computeIfAbsent(key, k -> new ArrayList<MethodHolder<SET, SetMethod>>()).add(setHolder);
            });
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

    @SuppressWarnings("unchecked")
    private <M extends AbstractResourceMethod, T extends WhiteboardHandler<?>> void processAnnotatedMethod(
            final RegistryKey key, final Predicate<ResourceType> validateResourceType,
            final Consumer<ResourceBuilder<?, Object>> builderCaller, WhiteboardContext<M> ctx,
            Map<RegistryKey, List<WhiteboardContext<T>>> methodsRegistry,
            Map<Long, List<RegistryKey>> serviceIdRegistry) {

        serviceIdRegistry.merge(ctx.serviceId, List.of(key), (k, v) -> concat(v.stream(), of(key)).collect(toList()));

        final Class<M> comparableType = ctx.getType();

        methodsRegistry.merge(key, List.of((WhiteboardContext<T>) ctx), (k, v) -> {
            Stream<WhiteboardContext<T>> stream;
            if (ctx.handler.isCatchAll()) {
                // FIXME test kind of content first
                WhiteboardContext<T> previous = v.get(v.size() - 1);
                if (comparableType.isAssignableFrom(previous.handler.getClass())
                        && comparableType.cast(previous.handler).isCatchAll()) {
                    LOG.warn("There are two catch all services {} and {} defined for GET resource {}",
                            previous.serviceId, ctx.serviceId, key);
                }
                stream = concat(v.stream(), of((WhiteboardContext<T>) ctx));
            } else {
                if (v.stream().anyMatch(a -> comparableType.isAssignableFrom(a.handler.getClass())
                        && comparableType.cast(a.handler).overlaps(ctx.handler))) {
                    LOG.warn("There are overlapping services defined for GET resource {}: {}", key, v);
                }
                stream = concat(of((WhiteboardContext<T>) ctx), v.stream());
            }
            return stream.collect(toList());
        });

        makeResource(key, validateResourceType, builderCaller);
    }

    private void makeResource(final RegistryKey key, final Predicate<ResourceType> validateResourceType,
            final Consumer<ResourceBuilder<?, Object>> builderCaller) {
        gatewayThread.execute(new AbstractSensinactCommand<Void>() {
            @Override
            protected Promise<Void> call(SensinactDigitalTwin twin, SensinactModelManager modelMgr,
                    PromiseFactory promiseFactory) {
                ResourceBuilder<?, Object> builder = null;
                Resource resource = null;

                Model model = modelMgr.getModel(key.getModel());
                if (model == null) {
                    builder = modelMgr.createModel(key.getModelPackageUri(), key.getModel())
                            .withService(key.getService()).withResource(key.getResource());
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

    private void processActMethod(final WhiteboardContext<ActMethod> ctx, final ACT annotation) {
        RegistryKey key = new RegistryKey(annotation.modelPackageUri(), annotation.model(), annotation.service(),
                annotation.resource());
        processAnnotatedMethod(
                key, (type) -> type == ResourceType.ACTION, (b) -> b.withType(ctx.handler.getReturnType())
                        .withAction(ctx.handler.getNamedParameterTypes()).buildAll(),
                ctx, actMethodRegistry, serviceIdToActMethods);
    }

    private void processGetMethod(final RegistryKey key, final WhiteboardContext<GetMethod> ctx, final GET annotation,
            final boolean hasSet) {
        processAnnotatedMethod(key, (type) -> type != ResourceType.ACTION, (b) -> {
            ResourceBuilder<?, ?> builder = b.withType(getType(ctx.handler.getReturnType(), annotation.type()))
                    .withGetter()
                    .withGetterCache(Duration.of(annotation.cacheDuration(), annotation.cacheDurationUnit()));
            if (hasSet) {
                builder = builder.withSetter();
            }
            builder.buildAll();
        }, ctx, getMethodRegistry, serviceIdToGetMethods);
    }

    private void processSetMethod(final RegistryKey key, final WhiteboardContext<SetMethod> ctx, final SET annotation,
            final boolean hasGet) {
        processAnnotatedMethod(key, (type) -> type != ResourceType.ACTION, (b) -> {
            ResourceBuilder<?, ?> builder = b.withType(getType(ctx.handler.getReturnType(), annotation.type()))
                    .withSetter();
            if (hasGet) {
                builder = builder.withGetter();
            }
            builder.buildAll();
        }, ctx, setMethodRegistry, serviceIdToSetMethods);
    }

    public Promise<Object> act(String modelPackageUri, String model, String provider, String service, String resource,
            Map<String, Object> arguments) {
        RegistryKey key = new RegistryKey(modelPackageUri, model, service, resource);

        Optional<WhiteboardContext<WhiteboardAct<?>>> opt = actMethodRegistry.getOrDefault(key, List.of()).stream()
                .filter(a -> a.providers.isEmpty() || a.providers.contains(provider)).findFirst();
        if (opt.isEmpty()) {
            return promiseFactory.failed(new NoSuchElementException(String
                    .format("No suitable whiteboard handler for %s/%s/%s/%s", model, provider, service, resource)));
        }

        Deferred<Object> d = promiseFactory.deferred();
        final IMetricTimer overallTimer = metrics.withTimers("sensinact.whiteboard.act.request",
                "sensinact.whiteboard.act.request." + String.join(".", modelPackageUri, model, service, resource),
                "sensinact.whiteboard.act.request." + String.join(".", provider, service, resource));
        promiseFactory.executor().execute(() -> {
            try (final IMetricTimer timer = metrics.withTimers("sensinact.whiteboard.act.task",
                    "sensinact.whiteboard.act.task." + String.join(".", modelPackageUri, model, service, resource),
                    "sensinact.whiteboard.act.task." + String.join(".", provider, service, resource))) {
                Promise<?> result = opt.get().handler.act(promiseFactory, modelPackageUri, model, provider, service,
                        resource, arguments);
                if (result == null) {
                    d.fail(new NullPointerException(
                            String.format("Whiteboard action handler returned no promise for resource %s/%s/%s/%s",
                                    model, provider, service, resource)));
                } else {
                    d.resolveWith(result);
                }
            } catch (Exception e) {
                d.fail(e);
            }
        });
        return d.getPromise().onResolve(() -> overallTimer.close());
    }

    @SuppressWarnings("unchecked")
    public <T> Promise<TimedValue<T>> pullValue(String modelPackageUri, String model, String provider, String service,
            String resource, Class<T> type, TimedValue<T> cachedValue, Consumer<TimedValue<T>> gatewayUpdate) {
        // Find the handler method
        RegistryKey key = new RegistryKey(modelPackageUri, model, service, resource);

        Optional<WhiteboardContext<WhiteboardGet<?>>> opt = getMethodRegistry.getOrDefault(key, List.of()).stream()
                .filter(a -> a.providers.isEmpty() || a.providers.contains(provider)).findFirst();
        if (opt.isEmpty()) {
            return promiseFactory.failed(new NoSuchElementException(String
                    .format("No suitable whiteboard handler for %s/%s/%s/%s", model, provider, service, resource)));
        }

        // Coudln't find a better way to manage casting with generics
        return callGetMethod((WhiteboardContext<WhiteboardGet<T>>) (Object) opt.get(), key, modelPackageUri, model,
                provider, service, resource, type, cachedValue, gatewayUpdate);
    }

    @SuppressWarnings("unchecked")
    private <T> Promise<TimedValue<T>> callGetMethod(WhiteboardContext<WhiteboardGet<T>> handler, RegistryKey key,
            String modelPackageUri, String model, String provider, String service, String resource, Class<T> type,
            TimedValue<T> cachedValue, Consumer<TimedValue<T>> gatewayUpdate) {
        synchronized (concurrentGetHolder) {
            final Promise<TimedValue<?>> currentPromise = concurrentGetHolder.get(key);
            if (currentPromise != null) {
                // Call is already running: return its promise
                return currentPromise.map(tv -> (TimedValue<T>) tv);
            }

            final Deferred<TimedValue<T>> d = promiseFactory.deferred();

            final IMetricTimer overallTimer = metrics.withTimers("sensinact.whiteboard.pull.request",
                    "sensinact.whiteboard.pull.request." + String.join(".", modelPackageUri, model, service, resource),
                    "sensinact.whiteboard.pull.request." + String.join(".", provider, service, resource));
            promiseFactory.executor().execute(() -> {
                try (final IMetricTimer timer = metrics.withTimers("sensinact.whiteboard.pull.task",
                        "sensinact.whiteboard.pull.task." + String.join(".", modelPackageUri, model, service, resource),
                        "sensinact.whiteboard.pull.task." + String.join(".", provider, service, resource))) {
                    d.resolveWith(handler.handler.pullValue(promiseFactory, modelPackageUri, model, provider, service,
                            resource, type, cachedValue));
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
            return runOnGateway(promise, coCall, cachedValue);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> Promise<TimedValue<T>> pushValue(String modelPackageUri, String model, String provider, String service,
            String resource, Class<T> type, TimedValue<T> cachedValue, TimedValue<T> newValue,
            Consumer<TimedValue<T>> gatewayUpdate) {

        RegistryKey key = new RegistryKey(modelPackageUri, model, service, resource);

        Optional<WhiteboardContext<WhiteboardSet<?>>> opt = setMethodRegistry.getOrDefault(key, List.of()).stream()
                .filter(a -> a.providers.isEmpty() || a.providers.contains(provider)).findFirst();
        if (opt.isEmpty()) {
            return promiseFactory.failed(new NoSuchElementException(String
                    .format("No suitable whiteboard handler for %s/%s/%s/%s", model, provider, service, resource)));
        }

        final Deferred<TimedValue<T>> d = promiseFactory.deferred();
        // Coudln't find a better way to manage casting with generics
        final WhiteboardContext<WhiteboardSet<T>> setMethod = (WhiteboardContext<WhiteboardSet<T>>) (Object) opt.get();

        final IMetricTimer overallTimer = metrics.withTimers("sensinact.whiteboard.pull.request",
                "sensinact.whiteboard.pull.request." + String.join(".", modelPackageUri, model, service, resource),
                "sensinact.whiteboard.pull.request." + String.join(".", provider, service, resource));
        promiseFactory.executor().execute(() -> {
            try (final IMetricTimer timer = metrics.withTimers("sensinact.whiteboard.push.task",
                    "sensinact.whiteboard.push.task." + String.join(".", modelPackageUri, model, service, resource),
                    "sensinact.whiteboard.push.task." + String.join(".", provider, service, resource))) {
                d.resolveWith(setMethod.handler.pushValue(promiseFactory, modelPackageUri, model, provider, service,
                        resource, type, cachedValue, newValue));
            } catch (Exception e) {
                d.fail(e);
            }
        });

        final Promise<TimedValue<T>> promise = d.getPromise().onResolve(() -> overallTimer.close());
        return runOnGateway(promise, gatewayUpdate, cachedValue);
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
            final Consumer<TimedValue<T>> gatewayUpdate, final TimedValue<T> cachedValue) {
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
                                    return pf.resolved(value == null ? cachedValue : value);
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
