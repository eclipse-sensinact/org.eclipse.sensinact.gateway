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

class SecuredAccessImpl implements SecuredAccess {
	private Mediator mediator;

	private ServiceRegistration<AuthorizationService> authorizationRegistration;

	/**
	 * @param mediator
	 */
	public SecuredAccessImpl(Mediator mediator) {
		this.mediator = mediator;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.security.SecuredAccess#getAgentPublicKey(java.lang.String)
	 */
	@Override
	public String getAgentPublicKey(String bundleIdentifier) throws SecuredAccessException {
		return UserManager.ANONYMOUS_PKEY;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.security.SecuredAccess#getUserAccessTree(java.lang.String)
	 */
	@Override
	public AccessTree<? extends AccessNode> getUserAccessTree(String publicKey) throws SecuredAccessException {
		ImmutableAccessNode root = new ImmutableAccessNode(null, "/", false, null,
				AccessProfileOption.DEFAULT.getAccessProfile());
		ImmutableAccessTree accessTree = new ImmutableAccessTree(root);
		return accessTree;
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.security.SecuredAccess#
	 *      getApplicationPublicKey(java.lang.String)
	 */
	@Override
	public String getApplicationPublicKey(String privateKey) throws SecuredAccessException {
		return UserManager.ANONYMOUS_PKEY;
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.security.SecuredAccess#
	 *      getApplicationAccessTree(java.lang.String)
	 */
	@Override
	public AccessTree<? extends AccessNode> getApplicationAccessTree(String publicKey) throws SecuredAccessException {
		ImmutableAccessNode root = new ImmutableAccessNode(null, "/", false, null,
				AccessProfileOption.DEFAULT.getAccessProfile());
		ImmutableAccessTree accessTree = new ImmutableAccessTree(root);
		return accessTree;
	}

	/**
	 * @inheritDoc
	 *
	 * @see SecuredAccess# getAccessNode(java.lang.String, java.lang.String)
	 */
	@Override
	public MutableAccessTree<? extends MutableAccessNode> getAccessTree(String identifier)
			throws SecuredAccessException {
		MutableAccessTree<? extends MutableAccessNode> accessTree = new AccessTreeImpl<>(mediator)
				.withAccessProfile(AccessProfileOption.ALL_ANONYMOUS);
		return accessTree;
	}

	/**
	 * @inheritDoc
	 *
	 * @see SecuredAccess# buildAccessNodesHierarchy(java.lang.String,
	 *      java.lang.String, org.eclipse.sensinact.gateway.core.security.RootNode)
	 */
	@Override
	public void buildAccessNodesHierarchy(String identifier, String name,
			MutableAccessTree<? extends MutableAccessNode> tree) throws SecuredAccessException {
		// do nothing
	}

	/**
	 * @inheritDoc
	 *
	 * @see SecuredAccess#createAuthorizationService()
	 */
	@Override
	public void createAuthorizationService() {
		// do nothing
	}

	/**
	 * @inheritDoc
	 *
	 * @see SecuredAccess#close()
	 */
	@Override
	public void close() {
		if (mediator.isDebugLoggable()) {
			mediator.debug("closing sensiNact secured access");
		}
		if (this.authorizationRegistration != null) {
			try {
				this.authorizationRegistration.unregister();

			} catch (IllegalStateException e) {
				try {
					mediator.debug(e.getMessage());
				} catch (IllegalStateException ise) {
					// do nothing because it probably means
					// that the OSGi environment is closing
				}
			}
		}
	}
}