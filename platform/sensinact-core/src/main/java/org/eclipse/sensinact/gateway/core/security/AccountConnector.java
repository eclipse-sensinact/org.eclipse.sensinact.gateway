/**
 * 
 */
package org.eclipse.sensinact.gateway.core.security;

/**
 * @author christophe
 *
 */
public interface AccountConnector {
	boolean handle(String accountType);

	void connect(UserUpdater userUpdater);
}
