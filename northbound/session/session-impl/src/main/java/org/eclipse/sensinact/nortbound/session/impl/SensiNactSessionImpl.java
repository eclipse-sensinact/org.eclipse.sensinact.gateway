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
package org.eclipse.sensinact.nortbound.session.impl;

import static java.util.stream.Collectors.toList;
import static org.eclipse.sensinact.northbound.security.api.AuthorizationEngine.Authorizer.PreAuth.DENY;
import static org.eclipse.sensinact.northbound.security.api.AuthorizationEngine.Authorizer.PreAuth.UNKNOWN;
import static org.eclipse.sensinact.northbound.security.api.AuthorizationEngine.PermissionLevel.ACT;
import static org.eclipse.sensinact.northbound.security.api.AuthorizationEngine.PermissionLevel.DESCRIBE;
import static org.eclipse.sensinact.northbound.security.api.AuthorizationEngine.PermissionLevel.READ;
import static org.eclipse.sensinact.northbound.security.api.AuthorizationEngine.PermissionLevel.UPDATE;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.sensinact.core.command.AbstractTwinCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.command.GetLevel;
import org.eclipse.sensinact.core.command.ResourceCommand;
import org.eclipse.sensinact.core.model.ResourceType;
import org.eclipse.sensinact.core.model.ValueType;
import org.eclipse.sensinact.core.notification.AbstractResourceNotification;
import org.eclipse.sensinact.core.notification.ClientActionListener;
import org.eclipse.sensinact.core.notification.ClientDataListener;
import org.eclipse.sensinact.core.notification.ClientLifecycleListener;
import org.eclipse.sensinact.core.notification.ClientMetadataListener;
import org.eclipse.sensinact.core.notification.LifecycleNotification;
import org.eclipse.sensinact.core.notification.ResourceActionNotification;
import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.core.notification.ResourceMetaDataNotification;
import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.core.twin.SensinactService;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.northbound.security.api.AuthorizationEngine.Authorizer;
import org.eclipse.sensinact.northbound.security.api.AuthorizationEngine.Authorizer.PreAuth;
import org.eclipse.sensinact.northbound.security.api.AuthorizationEngine.NotPermittedException;
import org.eclipse.sensinact.northbound.security.api.AuthorizationEngine.PermissionLevel;
import org.eclipse.sensinact.northbound.security.api.UserInfo;
import org.eclipse.sensinact.northbound.session.ProviderDescription;
import org.eclipse.sensinact.northbound.session.ResourceDescription;
import org.eclipse.sensinact.northbound.session.ResourceShortDescription;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.northbound.session.ServiceDescription;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SensiNactSessionImpl implements SensiNactSession {

    private static final Logger LOG = LoggerFactory.getLogger(SensiNactSessionImpl.class);

    private final Object lock = new Object();

    private final String sessionId = UUID.randomUUID().toString();

    private final Map<String, List<String>> listenerRegistrations = new HashMap<>();

    private final NavigableMap<String, List<SessionListenerRegistration>> listenersByWildcardTopic = new TreeMap<>();
    private final Map<String, List<SessionListenerRegistration>> listenersByTopic = new HashMap<>();

    private Instant expiry;

    private boolean expired;

    private final GatewayThread thread;

    private final UserInfo user;

    private final Authorizer authorizer;

    public SensiNactSessionImpl(final UserInfo user, final Authorizer authorizer, final GatewayThread thread) {
        this.user = user;
        this.authorizer = authorizer;
        this.thread = thread;
        expiry = Instant.now().plusSeconds(600);
    }

    @Override
    public Map<String, List<String>> activeListeners() {
        synchronized (lock) {
            return new HashMap<>(listenerRegistrations);
        }
    }

    @Override
    public String addListener(List<String> topics, ClientDataListener cdl, ClientMetadataListener cml,
            ClientLifecycleListener cll, ClientActionListener cal) {
        String subscriptionId = UUID.randomUUID().toString();

        synchronized (lock) {

            if (cdl != null) {
                addListenerTopic(topics, "DATA/", new SessionDataListener(subscriptionId, authorizer, cdl));
            }

            if (cml != null) {
                addListenerTopic(topics, "METADATA/", new SessionMetadataListener(subscriptionId, authorizer, cml));
            }

            if (cll != null) {
                addListenerTopic(topics, "LIFECYCLE/", new SessionLifecycleListener(subscriptionId, authorizer, cll));
            }

            if (cal != null) {
                addListenerTopic(topics, "ACTION/", new SessionActionListener(subscriptionId, authorizer, cal));
            }

            listenerRegistrations.put(subscriptionId, List.copyOf(topics));
        }

        return subscriptionId;
    }

    /**
     * Must be called holding {@link #lock}
     *
     * @param topics
     * @param prefix
     * @param reg
     */
    private void addListenerTopic(List<String> topics, String prefix, SessionListenerRegistration reg) {
        topics.stream().map(prefix::concat).forEach(s -> {
            if (s.endsWith("*")) {
                listenersByWildcardTopic.merge(s.substring(0, s.length() - 1), List.of(reg), this::mergeLists);
            } else {
                listenersByTopic.merge(s, List.of(reg), this::mergeLists);
            }
        });
    }

    private List<SessionListenerRegistration> mergeLists(List<SessionListenerRegistration> a,
            List<SessionListenerRegistration> b) {
        return Stream.of(a, b).flatMap(List::stream).collect(toList());
    }

    @Override
    public void removeListener(String id) {
        synchronized (lock) {
            List<String> topics = listenerRegistrations.remove(id);

            if (topics != null) {
                removeListener(id, "DATA/", topics);
                removeListener(id, "METADATA/", topics);
                removeListener(id, "LIFECYCLE/", topics);
                removeListener(id, "ACTION/", topics);
            }
        }
    }

    private void removeListener(String subscriptionId, String prefix, List<String> topics) {
        topics.stream().map(prefix::concat).forEach(s -> {
            if (s.endsWith("*")) {
                listenersByWildcardTopic.computeIfPresent(s.substring(0, s.length() - 1),
                        (topic, regs) -> this.removeSubscription(subscriptionId, regs));
            } else {
                listenersByTopic.computeIfPresent(s, (topic, regs) -> this.removeSubscription(subscriptionId, regs));
            }
        });
    }

    /**
     * Must be called holding {@link #lock}
     *
     * @param subscriptionId
     * @param regs
     */
    private List<SessionListenerRegistration> removeSubscription(String subscriptionId,
            List<SessionListenerRegistration> regs) {
        List<SessionListenerRegistration> list = regs.stream().filter(r -> !r.subscriptionId.equals(subscriptionId))
                .collect(toList());

        return list.isEmpty() ? null : list;
    }

    private <I, T> T executeGetCommand(Function<SensinactDigitalTwin, I> caller, Function<I, T> converter) {
        return executeGetCommand(caller, converter, null);
    }

    private <I, T> T executeGetCommand(Function<SensinactDigitalTwin, I> caller, Function<I, T> converter,
            T defaultValue) {
        return safeExecute(new AbstractTwinCommand<T>() {
            @Override
            protected Promise<T> call(SensinactDigitalTwin model, PromiseFactory pf) {
                try {
                    I modelValue = caller.apply(model);
                    T value;
                    if (modelValue != null) {
                        value = converter.apply(modelValue);
                    } else {
                        value = defaultValue;
                    }
                    return pf.resolved(value);
                } catch (Exception e) {
                    return pf.failed(e);
                }
            }
        });
    }

    private <T> T safeExecute(AbstractTwinCommand<T> command) {
        return safeGetValue(thread.execute(command));
    }

    private <T> T safeGetValue(Promise<T> promise) {
        try {
            Throwable t = promise.getFailure();
            if(t != null) {
                if(t instanceof RuntimeException) {
                    throw (RuntimeException) t;
                } else {
                    throw new RuntimeException(t);
                }
            }
            return promise.getValue();
        } catch (InvocationTargetException ite) {
            // Re-throw cause as a runtime exception
            Throwable cause = ite.getCause();
            if(cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else {
                throw new RuntimeException(cause);
            }
        } catch (InterruptedException e) {
            // Re-throw as a runtime exception
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T getResourceValue(String provider, String service, String resource, Class<T> clazz) {
        final TimedValue<T> tv = getResourceTimedValue(provider, service, resource, clazz);
        if (tv != null) {
            return tv.getValue();
        } else {
            return null;
        }
    }

    @Override
    public <T> T getResourceValue(String provider, String service, String resource, Class<T> clazz, GetLevel getLevel) {
        final TimedValue<T> tv = getResourceTimedValue(provider, service, resource, clazz, getLevel);
        if (tv != null) {
            return tv.getValue();
        } else {
            return null;
        }
    }

    @Override
    public <T> TimedValue<T> getResourceTimedValue(String provider, String service, String resource, Class<T> clazz) {
        return getResourceTimedValue(provider, service, resource, clazz, GetLevel.NORMAL);
    }

    @Override
    public <T> TimedValue<T> getResourceTimedValue(String provider, String service, String resource, Class<T> clazz,
            GetLevel getLevel) {
        return doGetResourceTimedValue(provider, service, resource, clazz, getLevel);
    }

    private <T> TimedValue<T> doGetResourceTimedValue(String provider, String service, String resource, Class<T> clazz,
            GetLevel getLevel) {
        return doResourceWork(provider, service, resource, sr -> sr.getValue(clazz, getLevel), READ, () -> String.format("The user %s does not have permission to read resource %s",
                    user.getUserId(), String.format("%s/%s/%s", provider, service, resource)));
    }

    @Override
    public void setResourceValue(String provider, String service, String resource, Object o) {
        setResourceValue(provider, service, resource, o, Instant.now());
    }

    @Override
    public void setResourceValue(String provider, String service, String resource, Object o, Instant instant) {
        doSetResourceValue(provider, service, resource, o, instant);
    }

    private void doSetResourceValue(String provider, String service, String resource, Object o, Instant instant) {
        doResourceWork(provider, service, resource, sr -> sr.setValue(o, instant), UPDATE, () -> String.format("The user %s does not have permission to set resource %s",
                user.getUserId(), String.format("%s/%s/%s", provider, service, resource)));
    }

    private <T> T doResourceWork(String provider, String service, String resource, Function<SensinactResource, Promise<T>> work,
            PermissionLevel permissionLevel, Supplier<String> authFailureMessage) {

        final PreAuth preAuth = authorizer.preAuthResource(permissionLevel, provider, service, resource);
        if(preAuth == DENY) {
            throw new NotPermittedException(authFailureMessage.get());
        }

        return safeExecute(new AbstractTwinCommand<T>() {
            @Override
            protected Promise<T> call(SensinactDigitalTwin model, PromiseFactory pf) {
                try {
                    final SensinactResource sensinactResource = model.getResource(provider, service, resource);
                    if (sensinactResource != null) {
                        if(preAuth == UNKNOWN) {
                            // Authorisation check
                            SensinactProvider sp = sensinactResource.getService().getProvider();
                            if(!authorizer.hasResourcePermission(permissionLevel, sp.getModelPackageUri(), sp.getModelName(), provider, service, resource)) {
                                return pf.failed(new NotPermittedException(authFailureMessage.get()));
                            }
                        }
                        if (sensinactResource.getResourceType() == ResourceType.ACTION) {
                            return pf.resolved(null);
                        }
                        return work.apply(sensinactResource);
                    } else {
                        if(preAuth == UNKNOWN) {
                            // Still do an authorization check to avoid leaking that a provider exists
                            SensinactProvider sp = model.getProvider(provider);
                            if(sp != null) {
                                if(!authorizer.hasResourcePermission(DESCRIBE, sp.getModelPackageUri(), sp.getModelName(), provider, service, resource)) {
                                    return pf.failed(new NotPermittedException(authFailureMessage.get()));
                                }
                            } else {
                                if(!authorizer.hasResourcePermission(DESCRIBE, null, null, provider, service, resource)) {
                                    return pf.failed(new NotPermittedException(authFailureMessage.get()));
                                }
                            }
                        }
                        return pf.resolved(null);
                    }
                } catch (Throwable t) {
                    return pf.failed(t);
                }
            }
        });
    }

    @Override
    public Map<String, Object> getResourceMetadata(String provider, String service, String resource) {
        return Map.copyOf(doResourceWork(provider, service, resource, SensinactResource::getMetadataValues, READ, () -> String.format("The user %s does not have permission to read metadata for resource %s",
                user.getUserId(), String.format("%s/%s/%s", provider, service, resource))));
    }

    @Override
    public void setResourceMetadata(String provider, String service, String resource, Map<String, Object> metadata) {
        final Instant timestamp = Instant.now();
        Function<SensinactResource, Promise<Object>> setMetadata = sr -> thread.getPromiseFactory()
                .all(metadata.entrySet().stream()
                        .map(e -> sr.setMetadataValue(e.getKey(), e.getValue(), timestamp))
                        .collect(Collectors.toList()))
                .map(x -> null);
        doResourceWork(provider, service, resource, setMetadata, UPDATE, () -> String.format("The user %s does not have permission to set metadata for resource %s",
                user.getUserId(), String.format("%s/%s/%s", provider, service, resource)));
    }

    @Override
    public TimedValue<Object> getResourceMetadataValue(String provider, String service, String resource,
            String metadata) {
        return doResourceWork(provider, service, resource, sr -> sr.getMetadataValue(metadata), READ, () -> String.format("The user %s does not have permission to read metadata for resource %s",
                user.getUserId(), String.format("%s/%s/%s", provider, service, resource)));
    }

    @Override
    public void setResourceMetadata(String provider, String service, String resource, String metadata, Object value) {
        final Instant timestamp = Instant.now();
        safeExecute(new ResourceCommand<Void>(provider, service, resource) {
            @Override
            protected Promise<Void> call(SensinactResource resource, PromiseFactory pf) {
                return resource.setMetadataValue(metadata, value, timestamp);
            }
        });
    }

    @Override
    public Object actOnResource(String provider, String service, String resource, Map<String, Object> parameters) {
        return safeExecute(new ResourceCommand<Object>(provider, service, resource) {
            @Override
            protected Promise<Object> call(SensinactResource resource, PromiseFactory pf) {
                try {
                    return resource.act(parameters);
                } catch (Throwable t) {
                    return pf.failed(t);
                }
            }
        });
    }

    @Override
    public ResourceDescription describeResource(String provider, String service, String resource) {
        final PreAuth preAuth = authorizer.preAuthResource(DESCRIBE, provider, service, resource);
        if(preAuth == DENY) {
            throw new NotPermittedException(String.format("The user %s does not have permission to describe resource %s",
                    user.getUserId(), String.format("%s/%s/%s", provider, service, resource)));
        }
        // Avoid ResourceCommand as we return null on missing resources, not fail
        return safeExecute(new AbstractTwinCommand<ResourceDescription>() {
            @Override
            protected Promise<ResourceDescription> call(SensinactDigitalTwin model, PromiseFactory pf) {
                try {
                    final SensinactResource sensinactResource = model.getResource(provider, service, resource);

                    if (sensinactResource != null) {
                        if(preAuth == UNKNOWN) {
                            SensinactProvider sp = sensinactResource.getService().getProvider();
                            if(!authorizer.hasResourcePermission(DESCRIBE, sp.getModelPackageUri(), sp.getModelName(), provider, service, resource)) {
                                throw new NotPermittedException(String.format("The user %s does not have permission to describe resource %s",
                                        user.getUserId(), String.format("%s/%s/%s", provider, service, resource)));
                            }
                        }

                        ResourceType resourceType = sensinactResource.getResourceType();

                        final ResourceDescription result = new ResourceDescription();
                        result.provider = provider;
                        result.service = service;
                        result.resource = resource;
                        result.contentType = sensinactResource.getType();
                        result.resourceType = resourceType;

                        final Promise<ResourceDescription> val;
                        switch (resourceType) {
                        case ACTION:
                            result.actMethodArgumentsTypes = sensinactResource.getArguments();
                            val = pf.resolved(result);
                            break;

                        default:
                            val = sensinactResource.getValue(Object.class, GetLevel.NORMAL).map(tv -> {
                                // Add the current value
                                result.valueType = ValueType.UPDATABLE;
                                result.value = tv.getValue();
                                result.timestamp = tv.getTimestamp();
                                return result;
                            });
                            break;
                        }

                        final Promise<Map<String, Object>> metadata = sensinactResource.getMetadataValues();
                        return metadata.flatMap(x -> val.thenAccept(r -> r.metadata = x));
                    } else {
                        if(preAuth == UNKNOWN) {
                            SensinactProvider sp = model.getProvider(provider);
                            if(sp != null) {
                                if(!authorizer.hasResourcePermission(DESCRIBE, sp.getModelPackageUri(), sp.getModelName(), provider, service, resource)) {
                                    throw new NotPermittedException(String.format("The user %s does not have permission to describe resource %s",
                                            user.getUserId(), String.format("%s/%s/%s", provider, service, resource)));
                                }
                            } else {
                                if(!authorizer.hasResourcePermission(DESCRIBE, null, null, provider, service, resource)) {
                                    throw new NotPermittedException(String.format("The user %s does not have permission to describe resource %s",
                                            user.getUserId(), String.format("%s/%s/%s", provider, service, resource)));
                                }
                            }
                        }
                        return pf.resolved(null);
                    }
                } catch (Throwable t) {
                    return pf.failed(t);
                }
            }
        });
    }

    @Override
    public ResourceShortDescription describeResourceShort(String provider, String service, String resource) {

        final PreAuth preAuth = authorizer.preAuthResource(DESCRIBE, provider, service, resource);
        if(preAuth == DENY) {
            throw new NotPermittedException(String.format("The user %s does not have permission to describe resource %s",
                    user.getUserId(), String.format("%s/%s/%s", provider, service, resource)));
        }

        return executeGetCommand((m) -> {
                SensinactResource sr = m.getResource(provider, service, resource);
                if(sr != null) {
                    if(preAuth == UNKNOWN) {
                        SensinactProvider sp = sr.getService().getProvider();
                        if(!authorizer.hasResourcePermission(DESCRIBE, sp.getModelPackageUri(), sp.getModelName(), provider, service, resource)) {
                            throw new NotPermittedException(String.format("The user %s does not have permission to describe resource %s",
                                    user.getUserId(), String.format("%s/%s/%s", provider, service, resource)));
                        }
                    }
                } else if(preAuth == UNKNOWN) {
                    SensinactProvider sp = m.getProvider(provider);
                    if(sp != null) {
                        if(!authorizer.hasResourcePermission(DESCRIBE, sp.getModelPackageUri(), sp.getModelName(), provider, service, resource)) {
                            throw new NotPermittedException(String.format("The user %s does not have permission to describe resource %s",
                                    user.getUserId(), String.format("%s/%s/%s", provider, service, resource)));
                        }
                    } else {
                        if(!authorizer.hasResourcePermission(DESCRIBE, null, null, provider, service, resource)) {
                            throw new NotPermittedException(String.format("The user %s does not have permission to describe resource %s",
                                    user.getUserId(), String.format("%s/%s/%s", provider, service, resource)));
                        }
                    }
                }
                return sr;
            }, (rc) -> {
            final ResourceShortDescription result = new ResourceShortDescription();
            result.contentType = rc.getType();
            result.name = rc.getName();
            result.resourceType = rc.getResourceType();
            if (result.resourceType == ResourceType.ACTION) {
                result.actMethodArgumentsTypes = rc.getArguments();
            } else {
                // TODO: get it from the description
                result.valueType = ValueType.UPDATABLE;
            }
            return result;
        });
    }

    @Override
    public ServiceDescription describeService(String provider, String service) {
        final PreAuth preAuth = authorizer.preAuthService(DESCRIBE, provider, service);
        if(preAuth == DENY) {
            throw new NotPermittedException(String.format("The user %s does not have permission to describe service %s",
                    user.getUserId(), String.format("%s/%s", provider, service)));
        }

        return executeGetCommand((m) -> {
            SensinactService ss = m.getService(provider, service);
            if(ss != null) {
                if(preAuth == UNKNOWN) {
                    SensinactProvider sp = ss.getProvider();
                    if(!authorizer.hasServicePermission(DESCRIBE, sp.getModelPackageUri(), sp.getModelName(), provider, service)) {
                        throw new NotPermittedException(String.format("The user %s does not have permission to describe service %s",
                                user.getUserId(), String.format("%s/%s", provider, service)));
                    }
                }
            } else if(preAuth == UNKNOWN) {
                SensinactProvider sp = m.getProvider(provider);
                if(sp != null) {
                    if(!authorizer.hasServicePermission(DESCRIBE, sp.getModelPackageUri(), sp.getModelName(), provider, service)) {
                        throw new NotPermittedException(String.format("The user %s does not have permission to describe service %s",
                                user.getUserId(), String.format("%s/%s", provider, service)));
                    }
                } else {
                    if(!authorizer.hasServicePermission(DESCRIBE, null, null, provider, service)) {
                        throw new NotPermittedException(String.format("The user %s does not have permission to describe service %s",
                                user.getUserId(), String.format("%s/%s", provider, service)));
                    }
                }
            }
            return ss;
        }, (snSvc) -> {
            SensinactProvider sp = snSvc.getProvider();
            final ServiceDescription description = new ServiceDescription();
            description.service = snSvc.getName();
            description.provider = snSvc.getProvider().getName();
            description.resources = List.copyOf(authorizer.visibleResources(sp.getModelPackageUri(), sp.getModelName(), provider, service,
                    snSvc.getResources().keySet()));
            return description;
        });
    }

    @Override
    public ProviderDescription describeProvider(String provider) {
        final PreAuth preAuth = authorizer.preAuthProvider(DESCRIBE, provider);
        if(preAuth == DENY) {
            throw new NotPermittedException(String.format("The user %s does not have permission to describe service %s",
                    user.getUserId(), String.format("%s", provider)));
        }

        return executeGetCommand((m) -> {
                SensinactProvider sp = m.getProvider(provider);
                if(preAuth == UNKNOWN) {
                    if(sp != null) {
                        if(!authorizer.hasProviderPermission(DESCRIBE, sp.getModelPackageUri(), sp.getModelName(), provider)) {
                            throw new NotPermittedException(String.format("The user %s does not have permission to describe provider %s",
                                    user.getUserId(), String.format("%s", provider)));
                        }
                    } else {
                        if(!authorizer.hasProviderPermission(DESCRIBE, null, null, provider)) {
                            throw new NotPermittedException(String.format("The user %s does not have permission to describe provider %s",
                                    user.getUserId(), String.format("%s", provider)));
                        }
                    }
                }
                return sp;
            }, (snProvider) -> {
                final ProviderDescription description = new ProviderDescription();
                description.provider = snProvider.getName();
                description.services = List.copyOf(authorizer.visibleServices(snProvider.getModelPackageUri(),
                        snProvider.getModelName(), provider, snProvider.getServices().keySet()));
                return description;
            });
    }

    @Override
    public List<ProviderDescription> listProviders() {
        return executeGetCommand((m) -> m.getProviders(), (providers) -> providers.stream()
                .filter(snProvider -> authorizer.hasProviderPermission(DESCRIBE, snProvider.getModelPackageUri(),
                        snProvider.getModelName(), snProvider.getName()))
                .map((snProvider) -> {
                    final ProviderDescription description = new ProviderDescription();
                    description.provider = snProvider.getName();
                    description.services = List.copyOf(authorizer.visibleServices(snProvider.getModelPackageUri(),
                            snProvider.getModelName(), snProvider.getName(), snProvider.getServices().keySet()));
                    return description;
                }).collect(Collectors.toList()), List.of());
    }

    @Override
    public List<ProviderSnapshot> filteredSnapshot(ICriterion filter) {
        Predicate<ServiceSnapshot> service = this::authorizeService;
        Predicate<ResourceSnapshot> resource = this::authorizeResource;

        if (filter == null) {
            return executeGetCommand((m) -> m.filteredSnapshot(null, ps -> authorizeProvider(ps, false),
                    service, resource), Function.identity());
        } else {
            Predicate<GeoJsonObject> location = filter.getLocationFilter();
            Predicate<ProviderSnapshot> provider = ps -> authorizeProvider(ps, location != null);
            Predicate<ProviderSnapshot> pf = filter.getProviderFilter();
            Predicate<ServiceSnapshot> sf = filter.getServiceFilter();
            Predicate<ResourceSnapshot> rf = filter.getResourceFilter();
            return executeGetCommand((m) -> m.filteredSnapshot(location, pf == null ? provider : provider.and(pf),
                    sf == null ? service : service.and(sf), rf == null ? resource : resource.and(rf)), Function.identity());
        }
    }

    private boolean authorizeProvider(ProviderSnapshot ps, boolean useLocation) {
        if(useLocation && !authorizer.hasResourcePermission(READ, ps.getModelPackageUri(), ps.getModelName(),
                ps.getName(), "admin", "location")) {
            return false;
        }

        return authorizer.hasProviderPermission(DESCRIBE, ps.getModelPackageUri(), ps.getModelName(), ps.getName());
    }

    private boolean authorizeService(ServiceSnapshot ss) {
        ProviderSnapshot ps = ss.getProvider();
        return authorizer.hasServicePermission(DESCRIBE, ps.getModelPackageUri(), ps.getModelName(), ps.getName(), ss.getName());
    }
    private boolean authorizeResource(ResourceSnapshot sr) {
        ServiceSnapshot ss = sr.getService();
        ProviderSnapshot ps = ss.getProvider();
        return authorizer.hasResourcePermission(DESCRIBE, ps.getModelPackageUri(), ps.getModelName(), ps.getName(),
                ss.getName(), sr.getName());
    }

    public void notify(String topic, AbstractResourceNotification event) {
        List<SessionListenerRegistration> toNotify;
        synchronized (lock) {
            if (!isExpired()) {
                toNotify = new ArrayList<>();
                toNotify.addAll(listenersByTopic.getOrDefault(topic, List.of()));
                for (Entry<String, List<SessionListenerRegistration>> e : listenersByWildcardTopic.headMap(topic, true)
                        .descendingMap().entrySet()) {
                    if (topic.startsWith(e.getKey())) {
                        toNotify.addAll(e.getValue());
                    } else {
                        break;
                    }
                }
            } else {
                toNotify = List.of();
            }
        }

        toNotify.forEach(s -> s.notify(topic, event));
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public boolean isExpired() {
        synchronized (lock) {
            if (!expired && !expiry.isAfter(Instant.now())) {
                expired = true;
            }
            return expired;
        }
    }

    @Override
    public Instant getExpiry() {
        synchronized (lock) {
            return isExpired() ? null : expiry;
        }
    }

    @Override
    public void extend(Duration duration) {
        if (duration.isNegative() || duration.isZero()) {
            throw new IllegalArgumentException("The extension period must be greater than zero");
        }
        synchronized (lock) {
            checkWithException();
            expiry = Instant.now().plus(duration);
        }
    }

    @Override
    public void expire() {
        synchronized (lock) {
            expired = true;
        }
    }

    private void checkWithException() {
        if (isExpired()) {
            throw new IllegalStateException("Session is expired");
        }
    }

    private static abstract class SessionListenerRegistration {

        private final String subscriptionId;
        protected final Authorizer authorizer;

        public SessionListenerRegistration(String subscriptionId, Authorizer authorizer) {
            this.subscriptionId = subscriptionId;
            this.authorizer = authorizer;
        }

        public abstract void notify(String topic, AbstractResourceNotification notification);

    }

    private static class SessionLifecycleListener extends SessionListenerRegistration {

        private final ClientLifecycleListener listener;

        public SessionLifecycleListener(String subscriptionId, Authorizer authorizer, ClientLifecycleListener listener) {
            super(subscriptionId, authorizer);
            this.listener = listener;
        }

        @Override
        public void notify(String topic, AbstractResourceNotification notification) {

            LifecycleNotification ln = (LifecycleNotification) notification;

            switch(ln.status()) {
                case PROVIDER_CREATED:
                case PROVIDER_DELETED:
                    if(!authorizer.hasProviderPermission(DESCRIBE, ln.modelPackageUri(), ln.model(), ln.provider())) {
                        return;
                    }
                    break;
                case RESOURCE_CREATED:
                case RESOURCE_DELETED:
                    if(!authorizer.hasResourcePermission(DESCRIBE, ln.modelPackageUri(), ln.model(), ln.provider(), ln.service(), ln.resource())) {
                        return;
                    }
                    break;
                case SERVICE_CREATED:
                case SERVICE_DELETED:
                    if(!authorizer.hasServicePermission(DESCRIBE, ln.modelPackageUri(), ln.model(), ln.provider(), ln.service())) {
                        return;
                    }
                    break;
                default:
                    LOG.warn("Unrecognised lifecycle status {}. Denying access to the notification", ln.status());
                    return;
            }
            listener.notify(topic, ln);
        }
    }

    private static class SessionMetadataListener extends SessionListenerRegistration {

        private final ClientMetadataListener listener;

        public SessionMetadataListener(String subscriptionId, Authorizer authorizer, ClientMetadataListener listener) {
            super(subscriptionId, authorizer);
            this.listener = listener;
        }

        @Override
        public void notify(String topic, AbstractResourceNotification notification) {
            if(!authorizer.hasResourcePermission(READ, notification.modelPackageUri(), notification.model(), notification.provider(), notification.service(), notification.resource())) {
                return;
            }

            listener.notify(topic, (ResourceMetaDataNotification) notification);
        }
    }

    private static class SessionDataListener extends SessionListenerRegistration {

        private final ClientDataListener listener;

        public SessionDataListener(String subscriptionId, Authorizer authorizer, ClientDataListener listener) {
            super(subscriptionId, authorizer);
            this.listener = listener;
        }

        @Override
        public void notify(String topic, AbstractResourceNotification notification) {
            if(!authorizer.hasResourcePermission(READ, notification.modelPackageUri(), notification.model(), notification.provider(), notification.service(), notification.resource())) {
                return;
            }

            listener.notify(topic, (ResourceDataNotification) notification);
        }
    }

    private static class SessionActionListener extends SessionListenerRegistration {

        private final ClientActionListener listener;

        public SessionActionListener(String subscriptionId, Authorizer authorizer, ClientActionListener listener) {
            super(subscriptionId, authorizer);
            this.listener = listener;
        }

        @Override
        public void notify(String topic, AbstractResourceNotification notification) {
            if(!authorizer.hasResourcePermission(ACT, notification.modelPackageUri(), notification.model(), notification.provider(), notification.service(), notification.resource())) {
                return;
            }
            listener.notify(topic, (ResourceActionNotification) notification);
        }
    }

    @Override
    public UserInfo getUserInfo() {
        return user;
    }
}
