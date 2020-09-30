package ${package}.app.service;

import org.eclipse.sensinact.gateway.core.message.SnaMessage;

/**
 * A WebAppRelay is in charge of relaying received {@link SnaMessage}
 */
public interface WebAppRelay {

	/**
	 * Relays the received {@link SnaMessage}
	 * 
	 * @param message the {@link SnaMessage} to be relayed
	 */
	void relay(SnaMessage<?> message);
}
