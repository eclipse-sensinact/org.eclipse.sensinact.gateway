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

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.sensinact.gateway.core.security.Credentials;
import org.eclipse.sensinact.gateway.core.security.InvalidCredentialException;

/**
 * A NorthboundEndpoints allow to build a {@link NorthboundEndpoint}. 
 * Once assigned to a {@link NorthboundEndpoint} reference, the endpoint 
 * field is immutable
 *  
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public final class NorthboundEndpoints
{
	/**
	 * Provides a distinct NorthboundEndpoint by thread 
	 */
	public static final ThreadLocal<NorthboundEndpoints> ENDPOINTS = 
			new ThreadLocal<NorthboundEndpoints>()
	{
		/**
		 * @inheritDoc
		 *
		 * @see java.lang.ThreadLocal#initialValue()
		 */
		public NorthboundEndpoints initialValue()
		{
			return new NorthboundEndpoints();
		}
	};

	//for ThreadLocal management 
	private final AtomicInteger count;
	
	private NorthboundEndpoint endpoint;
	
	/**
	 * 
	 */
	private NorthboundEndpoints()
	{
		this.count = new AtomicInteger(0);
	}
	
	/**
	 * @return
	 */
	public int attach()
	{
		return count.incrementAndGet();
	}
	
	/**
	 * @return
	 */
	public int release()
	{
		return count.decrementAndGet();
	}
	
	/**
	 * @param login
	 * @param password
	 * @return
	 */
	public NorthboundEndpoint withCredentials(NorthboundMediator mediator, String login, String password)
			throws InvalidCredentialException
	{
		if(this.endpoint == null)
		{
			this.endpoint = new NorthboundEndpoint(mediator, 
					new Credentials(login, password));
		}
		return this.endpoint;
	}
	
	/**
	 * @param encoded
	 * @return
	 */
	public NorthboundEndpoint withCredentials(NorthboundMediator mediator, String encoded)
			throws InvalidCredentialException
	{
		if(this.endpoint == null)
		{
			this.endpoint = new NorthboundEndpoint(mediator, 
					new Credentials(encoded));
		}
		return this.endpoint;
	}

	/**
	 * @param mediator
	 * @return
	 */
	public NorthboundEndpoint withoutAuthentication(NorthboundMediator mediator)
			throws InvalidCredentialException
	{
		if(this.endpoint == null)
		{
			this.endpoint = new NorthboundEndpoint(mediator, null);
		}
		return this.endpoint;
	}

	/**
	 * @return
	 */
	public NorthboundEndpoint endpoint()
	{
		return this.endpoint;
	}
	
	
}