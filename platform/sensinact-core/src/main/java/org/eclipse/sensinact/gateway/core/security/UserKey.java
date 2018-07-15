package org.eclipse.sensinact.gateway.core.security;

/**
 * A UserKey wrapped the String public key of a user
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class UserKey {
	private final String publicKey;

	/**
	 * @param publicKey
	 */
	public UserKey(String publicKey) {
		this.publicKey = publicKey;
	}

	/**
	 * @return
	 */
	public String getPublicKey() {
		return this.publicKey;
	}
}
