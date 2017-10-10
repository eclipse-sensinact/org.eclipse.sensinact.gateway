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
package org.eclipse.sensinact.gateway.nthbnd.endpoint;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.Core;
import org.eclipse.sensinact.gateway.core.security.Authentication;
import org.eclipse.sensinact.gateway.core.security.Session;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class NorthboundMediator extends Mediator
{
	//********************************************************************//
	//						NESTED DECLARATIONS			  			      //
	//********************************************************************//

	private static final class SessionExecutor
	implements Executable<Core, Session>
	{
		private Authentication<?> authentication;

		SessionExecutor(Authentication<?> authentication)
		{
			this.authentication = authentication;
		}
		
		/**
		 * @inheritDoc
		 *
		 * @see Executable#execute(java.lang.Object)
		 */
		@Override
		public Session execute(Core core) throws Exception
		{
			Session s= null;
			if(this.authentication != null)
			{ 
				s=core.getSession(authentication);
			}
			if(s==null)
			{
				s= core.getAnonymousSession();
			}
			return s;
		 }
	}
	
	//********************************************************************//
	//						ABSTRACT DECLARATIONS						  //
	//********************************************************************//

	//********************************************************************//
	//						STATIC DECLARATIONS							  //
	//********************************************************************//

	//********************************************************************//
	//						INSTANCE DECLARATIONS						  //
	//********************************************************************//

	/**
	 * @param context
	 */
	public NorthboundMediator(BundleContext context)
	{
		super(context);
	}
	
	/**
	 * @param login
	 * @param password
	 * @return
	 */
	public Session getSession(Authentication<?> authentication)
	{
		ServiceReference<Core> reference = 
				super.getContext().getServiceReference(
						Core.class);
		Core core = null;
		
		if(reference != null && (core = super.getContext(
				).getService(reference))!=null)
		{
			try
			{
				return new SessionExecutor(authentication
					).execute(core);
			}	
			catch (Exception e)
			{
				super.error(e);
				
			} finally
			{
				 super.getContext().ungetService(reference);
			}
		}
		return null;
	}

	/**
	 * @return
	 */
	public Session getSession()
	{
		return getSession(null);
	}
}
