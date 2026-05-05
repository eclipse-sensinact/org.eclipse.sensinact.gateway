/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.northbound.session.impl;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.eclipse.sensinact.core.authorization.PermissionLevel.DESCRIBE;
import static org.eclipse.sensinact.core.authorization.PermissionLevel.READ;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.sensinact.core.authorization.Authorizer;
import org.eclipse.sensinact.core.notification.LifecycleNotification;
import org.eclipse.sensinact.core.notification.LifecycleNotification.Status;
import org.eclipse.sensinact.core.notification.ResourceActionNotification;
import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.core.notification.ResourceMetaDataNotification;
import org.eclipse.sensinact.core.notification.ResourceNotification;
import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.LinkedProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.DefaultTimedValue;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.northbound.session.SnapshotUpdate;
import org.eclipse.sensinact.northbound.session.snapshot.ImmutableProviderSnapshot;
import org.eclipse.sensinact.northbound.session.snapshot.ImmutableResourceSnapshot;
import org.eclipse.sensinact.northbound.session.snapshot.ImmutableServiceSnapshot;
import org.osgi.framework.BundleContext;
import org.osgi.service.typedevent.TypedEventHandler;
import org.osgi.util.promise.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SensinactSessionSnapshotListener extends AbstractSensinactSessionEventManager
        implements TypedEventHandler<ResourceNotification> {

    private static enum State { OUTDATED, UP_TO_DATE, SKIP }

    private static final Logger LOG = LoggerFactory.getLogger(SensinactSessionSnapshotListener.class);

    private final ICriterion filter;
    private final Consumer<SnapshotUpdate> snapshotUpdate;
    private final Supplier<Promise<List<ProviderSnapshot>>> updateRequest;

    private final List<String> registeredTopics;

    private final Object lock = new Object();
    private Deque<Function<Map<String, ProviderSnapshot>, State>> pendingChecks = new LinkedList<>();
    private boolean firstUpdateSent;
    private boolean updateInProgress;
    private Deque<Map<String, ProviderSnapshot>> snapshots = new LinkedList<Map<String,ProviderSnapshot>>();
    /**
     * Mutual exclusion when delivering updates
     */
    private final Semaphore notificationSemaphore = new Semaphore(1);


    public SensinactSessionSnapshotListener(BundleContext context, String sessionId,
            String subscriptionId, List<String> topics, Authorizer authorizer,
            ICriterion filter, Consumer<SnapshotUpdate> snapshotUpdate,
            Supplier<Promise<List<ProviderSnapshot>>> updateRequest) {
        super(sessionId, subscriptionId, topics, authorizer);

        this.filter = filter;
        this.snapshotUpdate = snapshotUpdate;
        this.updateRequest = updateRequest;

        registeredTopics = getFullTopicList(topics.isEmpty() ? filter.dataTopics()
                : topics.stream().map("DATA/"::concat).toList());

        synchronized (lock) {
            // First update is against zero
            firstUpdateSent = false;
            snapshots.addLast(Map.of());
        }
        // Register first so we never miss an update
        register(context);
        // Ensure that we always do an initial update
        tryUpdate();
    }

    public List<String> getRegisteredTopics() {
        return registeredTopics;
    }

    private List<String> getFullTopicList(List<String> dataTopics) {
        Stream<String> dataAndMetadata = dataTopics.stream()
                .flatMap(s -> Stream.of(s, metadataTopic(s)));

        return Stream.concat(dataAndMetadata, lifecycleTopics(dataTopics))
                .toList();
    }

    private String metadataTopic(String s) {
        return "META".concat(s);
    }

    /**
     * We assume that we're interested in all lifecycle events for
     * all providers that are referenced
     * @param dataTopics
     * @return
     */
    private Stream<String> lifecycleTopics(List<String> dataTopics) {
        Map<String, Set<String>> modelsAndProviders = new HashMap<>();
        boolean fullWildcard = false;
        for(String dataTopic : dataTopics) {
            String[] split = dataTopic.split("/");
            if(split.length < 2) {
                LOG.warn("Unexpected data topic format {}", dataTopic);
            } else if (split.length == 2) {
                if("*".equals(split[1])) {
                    fullWildcard = true;
                    break;
                }
            } else {
                modelsAndProviders.merge(split[1], Set.of(split[2]),
                        (a,b) -> Stream.concat(a.stream(), b.stream()).collect(Collectors.toSet()));
            }
        }

        if(fullWildcard) {
            return Stream.of("LIFECYCLE/*");
        }

        Function<Entry<String, Set<String>>, Stream<String>> mapper = e -> {
            String model = e.getKey();
            Set<String> providers = e.getValue();
            if(providers.contains("*")) {
                return Stream.of(String.format("LIFECYCLE/%s/*", model));
            } else {
                return providers.stream().map(p -> String.format("LIFECYCLE/%s/%s/*", model, p));
            }
        };

        return modelsAndProviders.entrySet().stream()
                .flatMap(mapper);
    }

    private void checkUpdate(Function<Map<String, ProviderSnapshot>, State> check) {
        Map<String, ProviderSnapshot> ps;
        synchronized (lock) {
            if(updateInProgress) {
                pendingChecks.addFirst(check);
                return;
            }
            ps = snapshots.peekLast();
        }
        if(check.apply(ps) == State.OUTDATED) {
            tryUpdate();
        }
    }

    private void tryUpdate() {
        boolean doUpdate;
        synchronized (lock) {
            if(updateInProgress) {
                doUpdate = false;
            } else {
                doUpdate = true;
                updateInProgress = true;
            }
        }
        if(doUpdate) {
            updateRequest.get()
                .onSuccess(this::successfulUpdate)
                .onFailure(t -> LOG.error("Failed to update the snapshot", t));
        }
    }

    private void successfulUpdate(List<ProviderSnapshot> newSnapshot) {
        Map<String, ProviderSnapshot> snapshotMap = newSnapshot.stream()
                .collect(toUnmodifiableMap(ProviderSnapshot::getName, identity()));
        Deque<Function<Map<String, ProviderSnapshot>,State>> queuedChecks;
        synchronized (lock) {
            snapshots.addLast(snapshotMap);
            updateInProgress = false;
            queuedChecks = pendingChecks;
            pendingChecks = new LinkedList<>();
        }
        // Notify the listener
        notifyListener();
        // Check to see if we need to do more updates
        checks: for (Function<Map<String, ProviderSnapshot>, State> check : queuedChecks) {
            State state = check.apply(snapshotMap);
            switch (state) {
            // If oudated trigger an update and then stop checking
            case OUTDATED:
                tryUpdate();
                break checks;
            // If up to date then all subsequent checks must also be up to date
            case UP_TO_DATE: break checks;
            // If "skip" i.e. unknown then keep checking
            case SKIP: continue checks;
            default:
                LOG.error("Unexpected check state {}", state);
            }
        }
    }

    private void notifyListener() {
        try {
            notificationSemaphore.acquire();
        } catch (InterruptedException e) {
            LOG.warn("Interrupted while waiting to deliver snapshot updates. This update will be skipped.", e);
            return;
        }
        try {
            boolean firstUpdateSent;
            Map<String, ProviderSnapshot> previous;
            Map<String, ProviderSnapshot> next;
            synchronized (lock) {
                firstUpdateSent = this.firstUpdateSent;
                this.firstUpdateSent = true;
                previous = snapshots.removeFirst();
                next = snapshots.peekFirst();
            }

            Map<String, ProviderSnapshot> arriving = next.entrySet().stream()
                    .filter(e -> !previous.containsKey(e.getKey()))
                    .collect(toUnmodifiableMap(Entry::getKey, Entry::getValue));
            Set<String> departing = previous.keySet().stream()
                    .filter(s -> !next.containsKey(s))
                    .collect(toUnmodifiableSet());

            Map<String, ProviderSnapshot> modified = next.entrySet().stream()
                    .filter(e -> previous.containsKey(e.getKey()))
                    .filter(e -> isUpdated(previous.get(e.getKey()), e.getValue()))
                    .collect(toUnmodifiableMap(Entry::getKey, Entry::getValue));

            if(firstUpdateSent && arriving.isEmpty() && modified.isEmpty() && departing.isEmpty()) {
                LOG.debug("Skipping empty update for subscription {} in session {}", subscriptionId, sessionId);
            } else {
                snapshotUpdate.accept(new SnapshotUpdate(arriving, modified, departing));
            }
        } finally {
            notificationSemaphore.release();
        }
    }

    private static final List<Function<LinkedProviderSnapshot, Object>> LINKED_PROVIDER_FIELDS = List.of(
            LinkedProviderSnapshot::getModelPackageUri, LinkedProviderSnapshot::getModelName,
            LinkedProviderSnapshot::getName, LinkedProviderSnapshot::getFriendlyName,
            LinkedProviderSnapshot::getDescription, LinkedProviderSnapshot::getLocation,
            LinkedProviderSnapshot::getIcon);
    private static final List<Function<ResourceSnapshot, Object>> RESOURCE_FIELDS = List.of(
            ResourceSnapshot::getName, ResourceSnapshot::getValue, ResourceSnapshot::getMetadata,
            ResourceSnapshot::getResourceType, ResourceSnapshot::getType, ResourceSnapshot::getValueType,
            ResourceSnapshot::getArguments);

    private boolean isUpdated(ProviderSnapshot previous, ProviderSnapshot next) {
        if(!(Objects.equals(previous.getModelPackageUri(), next.getModelPackageUri()) &&
                Objects.equals(previous.getModelName(), next.getModelName()))) {
            return true;
        }

        List<ServiceSnapshot> previousServices = previous.getServices();
        List<ServiceSnapshot> nextServices = next.getServices();
        if(previousServices.size() != nextServices.size()) {
            return true;
        }

        for(int i = 0; i < previousServices.size(); i++) {
            ServiceSnapshot ps = previousServices.get(i);
            ServiceSnapshot ns = nextServices.get(i);
            if(!Objects.equals(ps.getName(), ns.getName())) {
                return true;
            }
            List<ResourceSnapshot> previousResources = ps.getResources();
            List<ResourceSnapshot> nextResources = ns.getResources();
            if(previousResources.size() != nextResources.size()) {
                return true;
            }
            for(int j = 0; j < previousResources.size(); j++) {
                ResourceSnapshot pr = previousResources.get(j);
                ResourceSnapshot nr = nextResources.get(j);
                if(!RESOURCE_FIELDS.stream().allMatch(f -> Objects.equals(f.apply(pr), f.apply(nr)))) {
                    return true;
                }
            }
        }

        // Linked Providers
        List<LinkedProviderSnapshot> previousLinks = previous.getLinkedProviders();
        List<LinkedProviderSnapshot> nextLinks = next.getLinkedProviders();
        if(previousLinks.size() != nextLinks.size()) {
            return true;
        }

        for(int i = 0; i < previousLinks.size(); i++) {
            LinkedProviderSnapshot pl = previousLinks.get(i);
            LinkedProviderSnapshot nl = nextLinks.get(i);
            if(!LINKED_PROVIDER_FIELDS.stream().allMatch(f -> Objects.equals(f.apply(pl), f.apply(nl)))) {
                return true;
            }
        }

        return false;
    }

    private boolean containsService(Map<String, ProviderSnapshot> snapshots, ResourceNotification rn) {
        return Optional.ofNullable(snapshots.get(rn.provider()))
                .map(ps -> ps.getService(rn.service()))
                .isPresent();
    }

    private boolean containsResource(Map<String, ProviderSnapshot> snapshots, ResourceNotification rn) {
        return Optional.ofNullable(snapshots.get(rn.provider()))
                .map(ps -> ps.getResource(rn.service(), rn.resource()))
                .isPresent();
    }

    protected void notifyLifecycle(String topic, LifecycleNotification ln) {

        switch(ln.status()) {
            case PROVIDER_CREATED:
            case PROVIDER_DELETED:
                if(!authorizer.hasProviderPermission(DESCRIBE, ln.modelPackageUri(), ln.model(), ln.provider())) {
                    // We are not allowed to see this change, so don't bother checking for an update
                    return;
                } else {
                    if(ln.status() == Status.PROVIDER_CREATED) {
                        // A new provider may be interesting
                        checkUpdate(m -> m.containsKey(ln.provider()) ? State.UP_TO_DATE : State.OUTDATED);
                    } else {
                        checkUpdate(m -> m.containsKey(ln.provider()) ? State.OUTDATED : State.UP_TO_DATE);
                    }
                }
                break;
            case RESOURCE_CREATED:
            case RESOURCE_DELETED:
                if(!authorizer.hasResourcePermission(DESCRIBE, ln.modelPackageUri(), ln.model(), ln.provider(), ln.service(), ln.resource())) {
                    // We are not allowed to see this change, so don't bother checking for an update
                    return;
                } else {
                    if(ln.status() == Status.RESOURCE_CREATED) {
                        // A new resource may be interesting
                        checkUpdate(m -> containsResource(m, ln) ? State.UP_TO_DATE : State.OUTDATED);
                    } else {
                        checkUpdate(m -> containsResource(m, ln) ? State.OUTDATED : State.UP_TO_DATE);
                    }
                }
                break;
            case SERVICE_CREATED:
            case SERVICE_DELETED:
                // A new service may be interesting
                if(!authorizer.hasServicePermission(DESCRIBE, ln.modelPackageUri(), ln.model(), ln.provider(), ln.service())) {
                    // We are not allowed to see this change, so don't bother checking for an update
                    return;
                } else {
                    if(ln.status() == Status.RESOURCE_CREATED) {
                        // A new service may be interesting
                        checkUpdate(m -> containsService(m, ln) ? State.UP_TO_DATE : State.OUTDATED);
                    } else {
                        checkUpdate(m -> containsService(m, ln) ? State.OUTDATED : State.UP_TO_DATE);
                    }
                }
                break;
            default:
                LOG.warn("Unrecognised lifecycle status {}. Denying access to the notification", ln.status());
                return;
        }
    }

    protected void notifyData(String topic, ResourceDataNotification rdn) {
        if(!authorizer.hasResourcePermission(READ, rdn.modelPackageUri(), rdn.model(), rdn.provider(), rdn.service(), rdn.resource())) {
            return;
        }
        checkUpdate(m -> Optional.ofNullable(m.get(rdn.provider()))
                .map(ps -> ps.getResource(rdn.service(), rdn.resource()))
                .map(rs -> checkUpdate(rs.getValue(), new DefaultTimedValue<>(rdn.newValue(), rdn.timestamp())))
                .orElseGet(() -> checkResourceIsWanted(rdn)));
    }

    private State checkUpdate(TimedValue<?> snapshot, TimedValue<?> event) {
        if(snapshot.isEmpty() || snapshot.getTimestamp().isBefore(event.getTimestamp())) {
            return State.OUTDATED;
        }
        if(snapshot.getTimestamp().isAfter(event.getTimestamp())) {
            return State.UP_TO_DATE;
        }

        // The two timestamps are equal. If the values are equal then skip
        if(Objects.equals(snapshot.getValue(), event.getValue())) {
            return State.SKIP;
        }
        // If we get here then we assume that we're outdated for safety
        return State.OUTDATED;
    }

    /**
     * Test whether the filter is interested in the resource. If yes then return OUTDATED to indicate
     * that the snapshot should update, otherwise return SKIP to indicate no interest.
     * @param rdn
     * @return
     */
    private State checkResourceIsWanted(ResourceDataNotification rdn) {
        ImmutableProviderSnapshot ips = new ImmutableProviderSnapshot(rdn.modelPackageUri(), rdn.model(),
                rdn.provider(), rdn.timestamp(), List.of(new ImmutableServiceSnapshot(null, rdn.service(),
                        List.of(new ImmutableResourceSnapshot(rdn.resource())))), List.of());

        boolean wanted = filter.getProviderFilter().test(ips) &&
        filter.getServiceFilter().test(ips.getService(rdn.service())) &&
        filter.getResourceFilter().test(ips.getResource(rdn.service(), rdn.resource()));
        return wanted == true ? State.OUTDATED : State.SKIP;
    }

    protected void notifyMetdata(String topic, ResourceMetaDataNotification rmn) {
        if(!authorizer.hasResourcePermission(READ, rmn.modelPackageUri(), rmn.model(), rmn.provider(), rmn.service(), rmn.resource())) {
            return;
        }
        checkUpdate(m -> Optional.ofNullable(m.get(rmn.provider()))
                .map(ps -> ps.getResource(rmn.service(), rmn.resource()))
                .map(rs -> rs.getMetadata().equals(rmn.newValues())? State.SKIP : State.OUTDATED)
                .orElse(State.SKIP));
    }

    protected void notifyAction(String topic, ResourceActionNotification notification) {
        // This is currently a no-op
    }
}
