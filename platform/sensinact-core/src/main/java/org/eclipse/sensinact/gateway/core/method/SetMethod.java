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

import org.eclipse.sensinact.gateway.common.bundle.Mediator;

/**
 * Setter {@link AccessMethod} 
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class SetMethod  extends AbstractAccessMethod
{
	/**
	 * Constructor
	 */
    public SetMethod(Mediator mediator, String uri, 
			AccessMethodExecutor preProcessingExecutor)
    {
	    super(mediator, uri, AccessMethod.Type.SET, preProcessingExecutor);
    }

	/**
	 * @inheritDoc
	 *
	 * @see AbstractAccessMethod#
	 * createAccessMethodResult(java.lang.Object[])
	 */
    @Override
    protected SetResult createAccessMethodResult(Object[] parameters)
    {
    	return new SetResult(super.mediator, uri,parameters);
    }
}
