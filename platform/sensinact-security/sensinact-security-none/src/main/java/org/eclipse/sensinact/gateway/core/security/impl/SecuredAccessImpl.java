/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.core.security.impl;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.security.AccessNode;
import org.eclipse.sensinact.gateway.core.security.AccessProfileOption;
import org.eclipse.sensinact.gateway.core.security.AccessTree;
import org.eclipse.sensinact.gateway.core.security.AccessTreeImpl;
import org.eclipse.sensinact.gateway.core.security.AuthorizationService;
import org.eclipse.sensinact.gateway.core.security.ImmutableAccessNode;
import org.eclipse.sensinact.gateway.core.security.ImmutableAccessTree;
import org.eclipse.sensinact.gateway.core.security.MutableAccessNode;
import org.eclipse.sensinact.gateway.core.security.MutableAccessTree;
import org.eclipse.sensinact.gateway.core.security.SecuredAccess;
import org.eclipse.sensinact.gateway.core.security.SecuredAccessException;
import org.eclipse.sensinact.gateway.core.security.UserManager;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SecuredAccessImpl implements SecuredAccess {
	
	private static final Logger LOG = LoggerFactory.getLogger(SecuredAccessImpl.class);
	
	private Mediator mediator;

	private ServiceRegistration<AuthorizationService> authorizationRegistration;

	/**
	 * Constructor
	 * 
	 * @param mediator the {@link Mediator} allowing the {@link SecuredAccess} service 
	 * to be instantiated to interact with the OSGi host environment
	 */
	public SecuredAccessImpl(Mediator mediator) {
		this.mediator = mediator;
	}

	@Override
	public String getAgentPublicKey(String bundleIdentifier) throws SecuredAccessException {
		return UserManager.ANONYMOUS_PKEY;
	}

	@Override
	public AccessTree<? extends AccessNode> getUserAccessTree(String publicKey) throws SecuredAccessException {
		ImmutableAccessNode root = new ImmutableAccessNode(null, "/", false, null,
				AccessProfileOption.DEFAULT.getAccessProfile());
		ImmutableAccessTree accessTree = new ImmutableAccessTree(root);
		return accessTree;
	}

	@Override
	public String getApplicationPublicKey(String privateKey) throws SecuredAccessException {
		return UserManager.ANONYMOUS_PKEY;
	}

	@Override
	public AccessTree<? extends AccessNode> getApplicationAccessTree(String publicKey) throws SecuredAccessException {
		ImmutableAccessNode root = new ImmutableAccessNode(null, "/", false, null,
				AccessProfileOption.DEFAULT.getAccessProfile());
		ImmutableAccessTree accessTree = new ImmutableAccessTree(root);
		return accessTree;
	}

	@Override
	public MutableAccessTree<? extends MutableAccessNode> getAccessTree(String identifier)
			throws SecuredAccessException {
		MutableAccessTree<? extends MutableAccessNode> accessTree = new AccessTreeImpl<>(mediator)
				.withAccessProfile(AccessProfileOption.ALL_ANONYMOUS);
		return accessTree;
	}
	
	@Override
	public void buildAccessNodesHierarchy(String identifier, String name,
			MutableAccessTree<? extends MutableAccessNode> tree) throws SecuredAccessException {
		// do nothing
	}

	@Override
	public void createAuthorizationService() {
		// do nothing
	}

	@Override
	public void close() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("closing sensiNact secured access");
		}
		if (this.authorizationRegistration != null) {
			try {
				this.authorizationRegistration.unregister();

			} catch (IllegalStateException e) {
				try {
					LOG.debug(e.getMessage());
				} catch (IllegalStateException ise) {
					// do nothing because it probably means
					// that the OSGi environment is closing
				}
			}
		}
	}
}