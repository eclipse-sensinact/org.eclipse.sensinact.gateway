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
import org.eclipse.sensinact.gateway.core.Session;
import org.eclipse.sensinact.gateway.core.security.Authentication;
import org.eclipse.sensinact.gateway.core.security.InvalidCredentialException;
import org.osgi.framework.BundleContext;

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
			Session session = null;
			try
			{
				if(this.authentication != null)
				{
                    session = core.getSession(authentication);
				}
				if(session == null)
				{
                    session = core.getAnonymousSession();
				}
			} catch (InvalidCredentialException e) {
				throw e;
            } catch (Exception e) {
				e.printStackTrace();
			}
			return session;
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
	 * @param authentication
	 * @return the session associated to the credentials
	 */
	public Session getSession(Authentication<?> authentication) throws InvalidCredentialException 
	{	
		return super.callService(Core.class, new SessionExecutor(authentication));
	}

	/**
	 * @return
	 */
	public Session getSession() throws InvalidCredentialException 
	{
		return getSession(null);
	}
}
