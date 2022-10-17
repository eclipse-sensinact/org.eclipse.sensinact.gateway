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

import java.util.Set;

import org.eclipse.sensinact.gateway.common.primitive.ElementsProxy;
import org.eclipse.sensinact.gateway.core.ModelElement;

/**
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface AccessMethodDescriptor<E extends ElementsProxy<?>> {
	/**
	 * Defines on which Type this AccessMethodDescriptor applies
	 * 
	 * @return the Type on which this AccessMethodDescriptor applies
	 */
	Class<E> getTargetType();

	/**
	 * Returns the Set of {@link Signature}s wrapped by this AccessMethodDescriptor
	 * 
	 * @return this AccessMethodDescriptor's Set of {@link Signature}
	 */
	Set<Signature> getSignatures();

	/**
	 * Defines whether this AccessMethodDescriptor is valid for the
	 * {@link ModelElement} passed as parameter, depending on the specific
	 * constraints applying on this AccessMethodDescriptor
	 * 
	 * @param element
	 * @return
	 */
	boolean valid(ModelElement<?, ?, ?, ?, E> element);
}
