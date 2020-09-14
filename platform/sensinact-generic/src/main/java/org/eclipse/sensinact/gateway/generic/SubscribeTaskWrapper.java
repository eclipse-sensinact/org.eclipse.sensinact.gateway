/**
 * 
 */
package org.eclipse.sensinact.gateway.generic;

import org.eclipse.sensinact.gateway.common.execution.Executable;

/**
 *
 */
public interface SubscribeTaskWrapper extends TaskWrapper {
	
	/**
	 * {@link Executable} in charge of extracting the subscription identifier
	 * from the result Object of the wrapped {@link Task}
	 */
	Executable<Object, String> subscriptionIdExtractor();

	/**
	 * {@link Executable} in charge of extracting the subscriber identifier
	 * from the wrapped {@link Task}
	 */
	Executable<Task, String> subscriberIdExtractor();

	void setSubscriptionId(String subscriptionId);
	
	String getSubscriptionId();

	String getSubscriberId();
}
