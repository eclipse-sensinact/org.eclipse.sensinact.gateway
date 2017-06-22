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
 * Getter {@link AccessMethod} 
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class DescribeMethod  extends AbstractAccessMethod
{
	/**
	 * Constructor
	 */
    public DescribeMethod(Mediator mediator, String uri, 
			AccessMethodExecutor preProcessingExecutor)
    {
	    super(mediator, uri, AccessMethod.Type.DESCRIBE, preProcessingExecutor);
    }

	/**
	 * @inheritDoc
	 *
	 * @see AbstractAccessMethod#
	 * createAccessMethodResult(java.lang.Object[])
	 */
    @Override
    protected DescribeResult createAccessMethodResult(Object[] parameters)
    {
	    return new DescribeResult(super.mediator, uri,parameters);
    }
}
