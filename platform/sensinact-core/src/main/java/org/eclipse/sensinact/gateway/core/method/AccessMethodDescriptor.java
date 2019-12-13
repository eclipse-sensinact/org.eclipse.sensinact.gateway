/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.core.method;

import java.util.Set;

import org.eclipse.sensinact.gateway.common.primitive.ElementsProxy;
import org.eclipse.sensinact.gateway.core.ModelElement;

/**
 * 
 * @author <a href="mailto:cmunilla@cmssi.fr">Christophe Munilla</a>
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
