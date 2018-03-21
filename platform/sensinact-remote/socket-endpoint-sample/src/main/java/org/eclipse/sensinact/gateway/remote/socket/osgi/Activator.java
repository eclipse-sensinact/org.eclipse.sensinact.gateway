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

package org.eclipse.sensinact.gateway.remote.socket.osgi;

import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.remote.socket.sample.SocketEndpointManager;
import org.osgi.framework.BundleContext;

/**
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class Activator extends AbstractActivator<Mediator>
{
	//********************************************************************//
	//						NESTED DECLARATIONS			  			      //
	//********************************************************************//

	//********************************************************************//
	//						ABSTRACT DECLARATIONS						  //
	//********************************************************************//

	//********************************************************************//
	//						STATIC DECLARATIONS							  //
	//********************************************************************//

	//********************************************************************//
	//						INSTANCE DECLARATIONS						  //
	//********************************************************************//

	private SocketEndpointManager manager;

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.common.bundle.AbstractActivator#doStart()
	 */
	@Override
	public void doStart() throws Exception
	{		
		this.manager = new SocketEndpointManager(
				super.mediator);
		super.mediator.addListener(this.manager);
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.common.bundle.AbstractActivator#doStop()
	 */
	@Override
	public void doStop() throws Exception
	{
		super.mediator.deleteListener(this.manager);
		
		this.manager.stop();
		this.manager = null;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.common.bundle.AbstractActivator#doInstantiate(org.osgi.framework.BundleContext)
	 */
	@Override
	public Mediator doInstantiate(BundleContext context)
	{
		return new Mediator(context);
	}
}
