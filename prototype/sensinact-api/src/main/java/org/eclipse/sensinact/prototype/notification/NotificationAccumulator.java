package org.eclipse.sensinact.prototype.notification;

import java.time.Instant;
import java.util.Map;

public interface NotificationAccumulator {

	/**
	 * Called to add a provider. If the latest event for this provider is:
	 * <ul>
	 *  <li>CREATED: then the events are collapsed</li> 
	 *  <li>DELETED: then an added event is added next</li> 
	 * </ul>
	 * @param name the provider name
	 * @throws IllegalStateException if this accumulator has been completed with {@link #completeAndSend()}
	 */
	void addProvider(String name);

	/**
	 * Called to remove a provider. If the latest event for this provider is:
	 * <ul>
	 *  <li>CREATED: then the created event is removed and no new event added</li> 
	 *  <li>DELETED: then the events are collapsed</li> 
	 * </ul>
	 * @param name the provider name
	 * @throws IllegalStateException if this accumulator has been completed with {@link #completeAndSend()}
	 */
	void removeProvider(String name);

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
	void addService(String provider, String name);

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
	void removeService(String provider, String name);

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
	void addResource(String provider, String service, String name);

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
	void removeResource(String provider, String service, String name);

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
	void metadataValueUpdate(String provider, String service, String resource, Map<String, Object> oldValues,
			Map<String, Object> newValues, Instant timestamp);

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
	void resourceValueUpdate(String provider, String service, String resource, Object oldValue, Object newValue,
			Instant timestamp);

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
	void resourceAction(String provider, String service, String resource, Instant timestamp);

	void completeAndSend();

}