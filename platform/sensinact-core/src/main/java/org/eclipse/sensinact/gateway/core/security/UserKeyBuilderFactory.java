/**
 * 
 */
package org.eclipse.sensinact.gateway.core.security;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;

/**
 * Authentication service factory
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface UserKeyBuilderFactory<A, C extends Authentication<A>, S extends UserKeyBuilder<A,C>> {
	/**
	 * Returns the type registered into the OSGi host environment's registry as
	 * {@link UserKeyBuilder} service by this factory
	 * 
	 * @return the registered {@link UserKeyBuilder} service type
	 */
	Class<S> getType();

	/**
	 * Creates and registers a new {@link UserKeyBuilder} service into the
	 * OSGi registry
	 * 
	 * @param mediator the {@link Mediator} allowing to interact with the OSGi host
	 * environment
	 * 
	 * @throws SecuredAccessException
	 */
	void newInstance(Mediator mediator) throws SecuredAccessException;
}
