package org.eclipse.sensinact.prototype.notification;

import java.util.Map;

/**
 * Lifecycle notifications are sent to indicate the creation or deletion of a provider/service/resource
 * 
 * Topic name is
 * 
 * LIFECYCLE/&lt;provider&gt;[/&lt;service&gt;[/&lt;resource&gt;]]
 */
public class LifecycleNotification extends AbstractResourceNotification {
	
	public Status status;
	
	public Object initialValue;
	
	public Map<String, Object> initialMetadata;
	
	public enum Status {
		/**
		 * Provider created,
		 * <ul> 
		 * <li>{@link LifecycleNotification#service} will be null</li>
		 * <li>{@link LifecycleNotification#resource} will be null</li>
		 * <li>{@link LifecycleNotification#initialMetadata} will be null</li>
		 * <li>{@link LifecycleNotification#initialValue} will be a List of String service names for initial services</li>
		 * </ul>
		 */
		PROVIDER_CREATED, 
		
		/**
		 * Provider deleted,
		 * <ul> 
		 * <li>{@link LifecycleNotification#service} will be null</li>
		 * <li>{@link LifecycleNotification#resource} will be null</li>
		 * <li>{@link LifecycleNotification#initialMetadata} will be null</li>
		 * <li>{@link LifecycleNotification#initialValue} will be null</li>
		 * </ul>
		 */
		PROVIDER_DELETED, 
		
		/**
		 * Service created,
		 * <ul> 
		 * <li>{@link LifecycleNotification#resource} will be null</li>
		 * <li>{@link LifecycleNotification#initialMetadata} will be null</li>
		 * <li>{@link LifecycleNotification#initialValue} will be a List of String service names for initial resources</li>
		 * </ul>
		 */
		SERVICE_CREATED, 
		
		/**
		 * Service deleted,
		 * <ul> 
		 * <li>{@link LifecycleNotification#resource} will be null</li>
		 * <li>{@link LifecycleNotification#initialMetadata} will be null</li>
		 * <li>{@link LifecycleNotification#initialValue} will be null</li>
		 * </ul>
		 */
		SERVICE_DELETED, 
		
		/**
		 * Resource created,
		 * <ul> 
		 * <li>{@link LifecycleNotification#initialMetadata} will be the initial metadata</li>
		 * <li>{@link LifecycleNotification#initialValue} will be the initial value</li>
		 * </ul>
		 */
		RESOURCE_CREATED, 
		
		/**
		 * Resource created,
		 * <ul> 
		 * <li>{@link LifecycleNotification#initialMetadata} will be the initial metadata</li>
		 * <li>{@link LifecycleNotification#initialValue} will be the initial value</li>
		 * </ul>
		 */
		RESOURCE_DELETED;
	}
}
