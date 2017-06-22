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
package org.eclipse.sensinact.gateway.core.test;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.InvalidServiceProviderException;
import org.eclipse.sensinact.gateway.core.ModelConfiguration;
import org.eclipse.sensinact.gateway.core.ModelInstance;
import org.eclipse.sensinact.gateway.core.message.MessageHandler;

/**
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class MyModelInstance extends ModelInstance<ModelConfiguration>
{

	/**
	 * @param mediator
	 * @param modelConfiguration
	 * @param name
	 * @param profile
	 * @throws InvalidServiceProviderException
	 */
	public MyModelInstance(Mediator mediator,
	        ModelConfiguration modelConfiguration, String name, String profile)
	        throws InvalidServiceProviderException
	{
		super(mediator, modelConfiguration, name, profile);
	}
	
	public MessageHandler getHandler()
	{
		return super.messageHandler;
	}
	

}
