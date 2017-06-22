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
package org.eclipse.sensinact.gateway.core;

import java.lang.reflect.InvocationHandler;

import org.eclipse.sensinact.gateway.common.primitive.ElementsProxy;
import org.eclipse.sensinact.gateway.common.primitive.Nameable;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.method.AccessMethod.Type;

/**
 * A proxy of an {@link SensiNactResourceModelElement}
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface SensiNactResourceModelElementProxy<P extends Nameable>
extends InvocationHandler, ElementsProxy<P>
{
	/**
	 * Returns the {@link AccessMethod} of this SensiNactResourceModelElementProxy
	 * whose {@link AccessMethod.Type} is passed as parameter
	 * 
	 * @param type the {@link AccessMethod.Type} of the {@link AccessMethod}
	 * to be returned
	 * @return the method of this SensiNactResourceModelElementProxy with 
	 * the specified type
	 */
    AccessMethod getAccessMethod(Type type);
}
