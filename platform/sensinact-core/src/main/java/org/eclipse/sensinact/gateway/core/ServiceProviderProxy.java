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

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.util.UriUtils;

/**
 * {@link ServiceProvider} proxy
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ServiceProviderProxy extends ModelElementProxy
{
    /**
     * @param mediator
     * @param name
     */
    public ServiceProviderProxy(Mediator mediator, String name) 
    {
    	super(mediator, ServiceProvider.class, 
    		UriUtils.getUri(new String[]{name}));
    }

	/**
	 * @inheritDoc
	 *
     * @see SensiNactResourceModelElementProxy#
     * getAccessMethod(AccessMethod.Type)
     */
    @Override
	public AccessMethod getAccessMethod(String name)
    {
	    return null;
    }
}
