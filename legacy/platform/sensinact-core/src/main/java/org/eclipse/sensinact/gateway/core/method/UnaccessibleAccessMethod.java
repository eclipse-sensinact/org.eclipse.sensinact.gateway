/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core.method;

import java.util.Collections;
import java.util.Set;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.ErrorHandler;
import org.eclipse.sensinact.gateway.common.primitive.Describable;
import org.eclipse.sensinact.gateway.common.primitive.Nameable;
import org.eclipse.sensinact.gateway.common.primitive.PathElement;

/**
 * An unaccessible {@link AccessMethod} implementation
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class UnaccessibleAccessMethod implements AccessMethod<String, AccessMethodResponse<String>> {
	private final AccessMethod.Type method;
	private final String uri;

	/**
	 * Mediator used to interact with the OSGi host environment
	 */
	protected final Mediator mediator;

	/**
	 * @param uri
	 * @param method
	 */
	public UnaccessibleAccessMethod(Mediator mediator, String uri, AccessMethod.Type method) {
		this.mediator = mediator;
		this.method = method;
		this.uri = uri;
	}

	/**
	 * @inheritDoc
	 *
	 * @see Describable#getDescription()
	 */
	@Override
	public AccessMethodDescription getDescription() {
		return new AccessMethodDescription(this);
	}

	/**
	 * @inheritDoc
	 *
	 * @see Nameable#getName()
	 */
	@Override
	public String getName() {
		return this.method.name();
	}

	/**
	 * @inheritDoc
	 *
	 * @see AccessMethod#getType()
	 */
	@Override
	public Type getType() {
		return this.method;
	}

	/**
	 * @inheritDoc
	 *
	 * @see AccessMethod#invoke(java.lang.Object[])
	 */
	@Override
	public AccessMethodResponse<String> invoke(Object[] parameters) {
		return AccessMethodResponse.<String, AccessMethodResponse<String>>error(this.mediator, uri, this.method,
				AccessMethodResponse.FORBIDDEN_ERROR_CODE, "Unaccessible method", null);
	}

	/**
	 * @inheritDoc
	 *
	 * @see AccessMethod#getSignatures()
	 */
	@Override
	public Set<Signature> getSignatures() {
		return Collections.<Signature>emptySet();
	}

	/**
	 * @inheritDoc
	 *
	 * @see AccessMethod#size()
	 */
	@Override
	public int size() {
		return 0;
	}

	/**
	 * @inheritDoc
	 *
	 * @see AccessMethod#getErrorHandler()
	 */
	@Override
	public ErrorHandler getErrorHandler() {
		return null;
	}

	/**
	 * @inheritDoc
	 * 
	 * @see PathElement#getPath()
	 */
	@Override
	public String getPath() {
		return this.uri;
	}

}
