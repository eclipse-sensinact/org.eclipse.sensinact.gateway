package org.eclipse.sensinact.prototype.notification.impl;

import static java.util.Collections.emptyMap;
import static org.eclipse.sensinact.prototype.notification.LifecycleNotification.Status.PROVIDER_CREATED;
import static org.eclipse.sensinact.prototype.notification.LifecycleNotification.Status.PROVIDER_DELETED;
import static org.eclipse.sensinact.prototype.notification.LifecycleNotification.Status.RESOURCE_CREATED;
import static org.eclipse.sensinact.prototype.notification.LifecycleNotification.Status.RESOURCE_DELETED;
import static org.eclipse.sensinact.prototype.notification.LifecycleNotification.Status.SERVICE_CREATED;
import static org.eclipse.sensinact.prototype.notification.LifecycleNotification.Status.SERVICE_DELETED;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.sensinact.prototype.notification.AbstractResourceNotification;
import org.eclipse.sensinact.prototype.notification.LifecycleNotification;
import org.eclipse.sensinact.prototype.notification.NotificationAccumulator;
import org.eclipse.sensinact.prototype.notification.LifecycleNotification.Status;
import org.eclipse.sensinact.prototype.notification.ResourceActionNotification;
import org.eclipse.sensinact.prototype.notification.ResourceDataNotification;
import org.eclipse.sensinact.prototype.notification.ResourceMetaDataNotification;
import org.osgi.service.typedevent.TypedEventBus;


/**
 * This class is responsible for managing batches of update notifications.
 * No notifications are sent until the batch is completed
 * 
 * If multiple events occur for the same target then the events will be collapsed to "debounce"
 * the notifications
 * 
 * This type is not thread safe and must not be used concurrently.
 */
public class NotificationAccumulatorImpl implements NotificationAccumulator {
	
	private final TypedEventBus eventBus;
	
	private final SortedMap<NotificationKey, List<AbstractResourceNotification>> notifications = new TreeMap<>();

	private boolean complete = false;
	
	public NotificationAccumulatorImpl(TypedEventBus eventBus) {
		this.eventBus = eventBus;
	}

	/**
	 * Called to add a provider. If the latest event for this provider is:
	 * <ul>
	 *  <li>CREATED: then the events are collapsed</li> 
	 *  <li>DELETED: then an added event is added next</li> 
	 * </ul>
	 * @param name the provider name
	 * @throws IllegalStateException if this accumulator has been completed with {@link #completeAndSend()}
	 */
	@Override
	public void addProvider(String name) {
		doLifecycleMerge(PROVIDER_CREATED, name, null, null, null, null, false);
	}

	/**
	 * Called to remove a provider. If the latest event for this provider is:
	 * <ul>
	 *  <li>CREATED: then the created event is removed and no new event added</li> 
	 *  <li>DELETED: then the events are collapsed</li> 
	 * </ul>
	 * @param name the provider name
	 * @throws IllegalStateException if this accumulator has been completed with {@link #completeAndSend()}
	 */
	@Override
	public void removeProvider(String name) {
		doLifecycleMerge(PROVIDER_DELETED, name, null, null, null, null, true);
	}

	/**
	 * Called to add a service. If the latest event for this service is:
	 * <ul>
	 *  <li>CREATED: then the events are collapsed</li> 
	 *  <li>DELETED: then an added event is added next</li> 
	 * </ul>
	 * 
	 * @param provider the provider name
	 * @param name the service name
	 * @throws IllegalStateException if this accumulator has been completed with {@link #completeAndSend()}
	 */
	@Override
	public void addService(String provider, String name) {
		doLifecycleMerge(SERVICE_CREATED, provider, name, null, null, null, false);
	}

	/**
	 * Called to remove a service. If the latest event for this service is:
	 * <ul>
	 *  <li>CREATED: then the created event is removed and no new event added</li> 
	 *  <li>DELETED: then the events are collapsed</li> 
	 * </ul>
	 * @param provider the provider name
	 * @param name the service name
	 * @throws IllegalStateException if this accumulator has been completed with {@link #completeAndSend()}
	 */
	@Override
	public void removeService(String provider, String name) {
		doLifecycleMerge(SERVICE_DELETED, provider, name, null, null, null, true);
	}

	/**
	 * Called to add a resource. If the latest event for this resource is:
	 * <ul>
	 *  <li>CREATED: then the events are collapsed</li> 
	 *  <li>DELETED: then an added event is added next</li> 
	 * </ul>
	 * 
	 * @param provider the provider name
	 * @param service the service name
	 * @param name the resource name
	 * @throws IllegalStateException if this accumulator has been completed with {@link #completeAndSend()}
	 */
	@Override
	public void addResource(String provider, String service, String name) {
		doLifecycleMerge(RESOURCE_CREATED, provider, service, name, null, null, false);
	}

	/**
	 * Called to remove a resource. If the latest event for this resource is:
	 * <ul>
	 *  <li>CREATED: then the created event is removed and no new event added</li> 
	 *  <li>DELETED: then the events are collapsed</li> 
	 * </ul>
	 * @param provider the provider name
	 * @param service the service name
	 * @param name the resource name
	 * @throws IllegalStateException if this accumulator has been completed with {@link #completeAndSend()}
	 */
	@Override
	public void removeResource(String provider, String service, String name) {
		doLifecycleMerge(RESOURCE_DELETED, provider, service, name, null, null, true);
	}

	private void doLifecycleMerge(Status status, String provider, String service, String resource, 
				Object initialValue, Map<String, Object> initialMetadata, boolean isDelete) {
		check();
		notifications.compute(
				new NotificationKey(provider, service, resource, LifecycleNotification.class), 
				(a,b) -> {
					LifecycleNotification ln = createLifecycleNotification(status, provider, service, resource, initialValue, initialMetadata);
					if (b != null) {
						// Check the status of the last entry
						Status s = ((LifecycleNotification) b.get(b.size() - 1)).status;
						if(s == status) {
							// Simply replace the final entry with the update
							return b.size() == 2 ? List.of(b.get(0), ln) : List.of(ln);
						} else if(isDelete) {
							// A create/delete is nothing, A delete/create/delete is a delete
							return b.size() == 1 ? null : List.of(ln);
						} else {
							// Must be a delete/create, as we can't have a create/delete/create
							return List.of(b.get(0), ln);
						}
					}
					return List.of(ln);
				});
	}

	private LifecycleNotification createLifecycleNotification(Status status, String provider, String service, String resource,
			Object initialValue, Map<String, Object> initialMetadata) {
		LifecycleNotification ln = new LifecycleNotification();
		ln.provider = provider;
		ln.service = service;
		ln.resource = resource;
		ln.status = status;
		ln.initialValue = initialValue;
		ln.initialMetadata = initialMetadata;
		return ln;
	}

	/**
	 * Called to update metadata - provides the complete snapshot of metadata before and after
	 * 
	 * @param provider the provider name
	 * @param service the service name
	 * @param resource the resource name
	 * @param oldValues the metadata values before the update
	 * @param newValues the metadata values after the update
	 * @param timestamp the latest timestamp of the metadata after the update
	 * 
	 * @throws IllegalArgumentException if the timestamp is older than the latest known metadata update
	 * @throws IllegalStateException if this accumulator has been completed with {@link #completeAndSend()}
	 */
	@Override
	public void metadataValueUpdate(String provider, String service, String resource, 
			Map<String,Object> oldValues, Map<String,Object> newValues, Instant timestamp) {
		check();
		
		final Map<String,Object> nonNullOldValues = oldValues == null ? emptyMap() : oldValues;
		final Map<String,Object> nonNullNewValues = newValues == null ? emptyMap() : newValues;
		Objects.requireNonNull(timestamp);
		
		notifications.compute(
				new NotificationKey(provider, service, resource, ResourceMetaDataNotification.class), 
				(a,b) -> {
					Map<String,Object> oldValuesToUse;
					Map<String,Object> newValuesToUse;
					Instant timestampToUse;
					if(b != null) {
						ResourceMetaDataNotification previous = (ResourceMetaDataNotification)b.get(0);
						oldValuesToUse = previous.oldValues;
						if(previous.timestamp.isAfter(timestamp)) {
							throw new IllegalArgumentException("Received metadata updates out of temporal order");
						} else {
							newValuesToUse = nonNullNewValues;
							timestampToUse = timestamp;
						}
					} else {
						oldValuesToUse = nonNullOldValues;
						newValuesToUse = nonNullNewValues;
						timestampToUse = timestamp;
					}
					return List.of(createResourceMetaDataNotification(provider, service, resource, 
							oldValuesToUse, newValuesToUse, timestampToUse));
				});
	}
	
	private ResourceMetaDataNotification createResourceMetaDataNotification(String provider, String service, String resource,
			Map<String,Object> oldValues, Map<String,Object> newValues, Instant timestamp) {
		ResourceMetaDataNotification rn = new ResourceMetaDataNotification();
		rn.provider = provider;
		rn.service = service;
		rn.resource = resource;
		rn.oldValues = oldValues;
		rn.newValues = newValues;
		rn.timestamp = timestamp;
		return rn;
	}
	
	/**
	 * Called to update a resource value. If multiple updates occur they will be collapsed into
	 * single events
	 * 
	 * @param provider the provider name
	 * @param service the service name
	 * @param resource the resource name
	 * @param oldValue the value before the update
	 * @param newValue the value after the update
	 * @param timestamp the latest timestamp of the value after the update
	 * 
	 * @throws IllegalArgumentException if the timestamp is older than the latest known metadata update
	 * @throws IllegalStateException if this accumulator has been completed with {@link #completeAndSend()}
	 */
	@Override
	public void resourceValueUpdate(String provider, String service, String resource, Object oldValue, 
			Object newValue, Instant timestamp) {
		check();
		Objects.requireNonNull(timestamp);
		notifications.compute(
				new NotificationKey(provider, service, resource, ResourceDataNotification.class), 
				(a,b) -> {
					Object oldValueToUse;
					if(b != null) {
						ResourceDataNotification previous = (ResourceDataNotification)b.get(0);
						if(previous.timestamp.isAfter(timestamp)) {
							throw new IllegalArgumentException("Received resource value updates out of temporal order");
						}
						oldValueToUse = previous.oldValue;
					} else {
						oldValueToUse = oldValue;
					}
					return List.of(createResourceDataNotification(provider, service, resource, 
							oldValueToUse, newValue, timestamp));
				});
	}
	
	private ResourceDataNotification createResourceDataNotification(String provider, String service, String resource,
			Object oldValue, Object newValue, Instant timestamp) {
		ResourceDataNotification rn = new ResourceDataNotification();
		rn.provider = provider;
		rn.service = service;
		rn.resource = resource;
		rn.oldValue = oldValue;
		rn.newValue = newValue;
		rn.timestamp = timestamp;
		return rn;
	}

	/**
	 * Called to notify of a resource action - if multiple actions occur they will be 
	 * sorted into timestamp order
	 * 
	 * @param provider the provider name
	 * @param service the service name
	 * @param resource the resource name
	 * @param oldValue the value before the update
	 * @param newValue the value after the update
	 * @param timestamp the latest timestamp of the value after the update
	 * 
	 * @throws IllegalStateException if this accumulator has been completed with {@link #completeAndSend()}
	 */
	@Override
	public void resourceAction(String provider, String service, String resource, Instant timestamp) {
		check();
		Objects.requireNonNull(timestamp);
		notifications.compute(
				new NotificationKey(provider, service, resource, ResourceActionNotification.class), 
				(a,b) -> {
					ResourceActionNotification ran = createResourceActionNotification(provider, service, resource, timestamp);
					if(b != null) {
						return Stream.concat(
								b.stream(), 
								Stream.of(ran)
							)
							.map(ResourceActionNotification.class::cast)
							.sorted((i,j) -> i.timestamp.compareTo(j.timestamp))
							.collect(Collectors.toList());
					}
					return List.of(ran);
				});
	}
	
	private ResourceActionNotification createResourceActionNotification(String provider, String service, String resource,
			Instant timestamp) {
		ResourceActionNotification rn = new ResourceActionNotification();
		rn.provider = provider;
		rn.service = service;
		rn.resource = resource;
		rn.timestamp = timestamp;
		return rn;
	}
	
	@Override
	public void completeAndSend() {
		check();
		complete = true;

		notifications.values().stream().flatMap(List::stream).forEach(n -> eventBus.deliver(n.getTopic(), n));
	}

	private void check() {
		if (complete) {
			throw new IllegalStateException("The accumulator is already complete");
		}
	}
}

