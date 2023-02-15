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
package org.eclipse.sensinact.prototype.whiteboard.impl;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.sensinact.prototype.annotation.verb.ACT;
import org.eclipse.sensinact.prototype.annotation.verb.ACT.ACTs;
import org.eclipse.sensinact.prototype.command.AbstractSensinactCommand;
import org.eclipse.sensinact.prototype.command.GatewayThread;
import org.eclipse.sensinact.prototype.model.Model;
import org.eclipse.sensinact.prototype.model.Resource;
import org.eclipse.sensinact.prototype.model.ResourceBuilder;
import org.eclipse.sensinact.prototype.model.ResourceType;
import org.eclipse.sensinact.prototype.model.SensinactModelManager;
import org.eclipse.sensinact.prototype.model.Service;
import org.eclipse.sensinact.prototype.model.nexus.impl.ModelNexus;
import org.eclipse.sensinact.prototype.twin.SensinactDigitalTwin;
import org.osgi.framework.Constants;
import org.osgi.util.promise.Deferred;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SensinactWhiteboard {

    private static final Logger LOG = LoggerFactory.getLogger(ModelNexus.class);

    private final GatewayThread gatewayThread;

    private final Map<Long, List<RegistryKey>> serviceIdToActMethods = new ConcurrentHashMap<>();

    private final Map<RegistryKey, List<ActMethod>> actMethodRegistry = new ConcurrentHashMap<>();

    public SensinactWhiteboard(GatewayThread gatewayThread) {
        this.gatewayThread = gatewayThread;
    }

    public void addWhiteboardService(Object service, Map<String, Object> props) {
        Long serviceId = (Long) props.get(Constants.SERVICE_ID);

        Set<String> providers = toSet(props.get("sensiNact.provider.name"));

        Class<?> clz = service.getClass();

        List<Method> actMethods = Arrays.stream(clz.getMethods())
                .filter(m -> m.isAnnotationPresent(ACT.class) || m.isAnnotationPresent(ACTs.class))
                .collect(Collectors.toList());

        handleActMethods(serviceId, service, providers, actMethods);
    }

    public void updatedWhiteboardService(Object service, Map<String, Object> props) {
        Long serviceId = (Long) props.get(Constants.SERVICE_ID);

        Set<String> providers = toSet(props.get("sensiNact.provider.name"));

        for (RegistryKey key : serviceIdToActMethods.getOrDefault(serviceId, List.of())) {
            actMethodRegistry.computeIfPresent(key, (x, v) -> {
                for (int i = 0; i < v.size(); i++) {
                    ActMethod actMethod = v.get(i);
                    if (actMethod.serviceId.equals(serviceId)) {
                        if (providers.equals(actMethod.providers)
                                || (!providers.isEmpty() && !actMethod.providers.isEmpty())) {
                            LOG.debug(
                                    "The update to the whiteboard action service {} did not change the act resource {}",
                                    serviceId, key);
                            return v;
                        } else {
                            LOG.debug(
                                    "The update to the whiteboard action service {} changed the act resource {} from providers {} to providers {}",
                                    serviceId, key, actMethod.providers, providers);
                            List<ActMethod> result = new ArrayList<>(v.size());
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

    public void removeWhiteboardService(Object service, Map<String, Object> props) {
        Long serviceId = (Long) props.get(Constants.SERVICE_ID);

        List<RegistryKey> keys = serviceIdToActMethods.remove(serviceId);

        if (keys != null) {
            for (RegistryKey key : keys) {
                actMethodRegistry.computeIfPresent(key, (x, v) -> {
                    List<ActMethod> l = v.stream().filter(a -> !serviceId.equals(a.serviceId)).collect(toList());
                    if (l.equals(v)) {
                        LOG.warn("There were no act methods to remove for service {} and resource {}", serviceId, key);
                        return v;
                    }

                    return l.isEmpty() ? null : l;
                });
            }
        }
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

    private void processActMethod(ActMethod am, ACT act) {

        RegistryKey key = new RegistryKey(act.model(), act.service(), act.resource());

        serviceIdToActMethods.merge(am.serviceId, List.of(key),
                (k, v) -> concat(v.stream(), of(key)).collect(toList()));

        actMethodRegistry.merge(key, List.of(am), (k, v) -> {
            Stream<ActMethod> stream;
            if (am.isCatchAll()) {
                ActMethod previous = v.get(v.size() - 1);
                if (previous.isCatchAll()) {
                    LOG.warn("There are two catch all services {} and {} defined for act resource {}",
                            previous.serviceId, am.serviceId, key);
                }
                stream = concat(v.stream(), of(am));
            } else {
                if (v.stream().anyMatch(a -> a.overlaps(am))) {
                    LOG.warn("There are overlapping services defined for act resource {}: {}", key, v);
                }
                stream = concat(of(am), v.stream());
            }
            return stream.collect(toList());
        });
        gatewayThread.execute(new AbstractSensinactCommand<Void>() {
            @Override
            protected Promise<Void> call(SensinactDigitalTwin twin, SensinactModelManager modelMgr,
                    PromiseFactory promiseFactory) {
                ResourceBuilder<?, Object> builder = null;

                Model model = modelMgr.getModel(key.getModel());
                if (model == null) {
                    builder = modelMgr.createModel(key.getModel()).withService(key.getService())
                            .withResource(key.getResource());
                } else {
                    Service service = model.getServices().get(key.getService());
                    if (service == null) {
                        builder = model.createService(key.getService()).withResource(key.getResource());
                    } else {
                        Resource resource = service.getResources().get(key.getResource());
                        if (resource == null) {
                            builder = service.createResource(key.getResource());
                        } else {
                            ResourceType type = resource.getResourceType();
                            if (type != ResourceType.ACTION) {
                                LOG.error("The resource {} in service {} already exists for the model {} as type {}",
                                        key.getResource(), key.getService(), key.getModel(), type);
                            }
                        }
                    }
                }

                if (builder != null) {
                    builder.withType(am.getReturnType()).withAction(am.getNamedParameterTypes()).buildAll();
                }
                return null;
            }
        });
    }

    public Promise<Object> act(String model, String provider, String service, String resource,
            Map<String, Object> arguments) {
        RegistryKey key = new RegistryKey(model, service, resource);

        Optional<ActMethod> opt = actMethodRegistry.getOrDefault(key, List.of()).stream()
                .filter(a -> a.providers.isEmpty() || a.providers.contains(provider)).findFirst();

        PromiseFactory promiseFactory = gatewayThread.getPromiseFactory();
        if (opt.isEmpty()) {
            return promiseFactory.failed(new NoSuchElementException(
                    String.format("No suitable provider for model %s, provider %s, service %s, resource %s", model,
                            provider, service, resource)));
        } else {
            Deferred<Object> d = promiseFactory.deferred();
            promiseFactory.executor().execute(() -> {
                try {
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
            return d.getPromise();
        }
    }
}
