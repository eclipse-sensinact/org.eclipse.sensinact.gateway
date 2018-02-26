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

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.security.Authentication;
import org.eclipse.sensinact.gateway.core.security.AuthenticationToken;
import org.eclipse.sensinact.gateway.core.security.Credentials;
import org.eclipse.sensinact.gateway.core.security.InvalidCredentialException;
import org.eclipse.sensinact.gateway.util.CastUtils;

/**
 * A collection {@link NorthboundEndpoint}s with a limited lifetime,
 * but 'reactivable'
 *  
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public final class NorthboundEndpoints
{	
	/**
	 * Defines the lifetime end of an {@link NorthboundEndpoint}
	 */
	private final class Term
	{
		Term previous;
		Term next;
		final String identifier;
		final AtomicLong timeout;

		Term(String identifier)
		{
			this.identifier = identifier;
			this.timeout = new AtomicLong(0);
		}
		
		final void reactivate()
		{
			this.timeout.set(System.currentTimeMillis() + 
					NorthboundEndpoints.this.lifetime);
		}
		
		final boolean expired()
		{
			return System.currentTimeMillis() > this.timeout.get();
		}
	}

	/**
	 * Handles the list of created {@link NorthboundEndpoint} according
	 * to their lifetime 
	 */
	private final class Schedule implements Runnable
	{
		private volatile boolean running;
		private Map<String, Term> map = null;
		private Term head = null;
		private Term queue = null;
		
		Schedule()
		{
			this.map = new HashMap<String, Term>();
			this.running = false;
		}
		
		void stop()
		{
			this.running = false;
		}
		
		Term getTerm(String sessionIdentifier)
		{
			Term term = null;
			synchronized(NorthboundEndpoints.this.lock)
			{
				term = this.map.get(sessionIdentifier);				
			}
			return term;
		}

		boolean update(String sessionIdentifier)
		{
			Term term = this.remove(sessionIdentifier);
			if(term != null)
			{
				term.reactivate();
				this.add(term);
				return true;
			}
			return false;
		}
		
		Term remove(String sessionIdentifier)
		{
			synchronized(NorthboundEndpoints.this.lock)
			{
				Term term = this.map.remove(sessionIdentifier);
				
				if(term == null)
				{
					return term;
				}
				if(term == this.head)
				{
					this.head=term.next;
				}
				if(term == this.queue)
				{
					this.queue=term.previous;
				}
				if(term.previous != null)
				{
					term.previous.next = term.next;
				}
				if(term.next != null)
				{
					term.next.previous = term.previous;
				}
				term.next=null;
				term.previous=null;
				return term;
			}
		}
		
		Term add(Term term)
		{
			Term old = null;
			if(term ==null || term.identifier==null)
			{
				return old;
			}
			synchronized(NorthboundEndpoints.this.lock)
			{
				term.previous = this.queue;
				if(this.queue != null)
				{
					this.queue.next= term;
					
				} else 
				{
					this.head=term;
				}
			    this.queue = term;
			    old = this.map.put(term.identifier, term);
			}
			return old;
		}
		
		
		/**
		 * @inheritDoc
		 *
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run()
		{
			this.running = true;			
			while(this.running)
			{
				synchronized(NorthboundEndpoints.this.lock)
				{
					while(this.head!= null && this.head.expired())
					{
						this.map.remove(this.head.identifier);
						this.head=this.head.next;
						if(this.head!=null)
						{
						   this.head.previous=null;
						}
					}
				}				
				try
				{
					Thread.sleep(5000);
					
				} catch(InterruptedException e)
				{
					Thread.interrupted();
					this.running = false;
					continue;
				}
			}
			this.map.clear();
			Term n = head;
			Term t = null;
			while(n!=null)
			{
				t = n.next;
				n.previous= null;
				n.next = null;
				n = t;
				t = null;
			}
			this.head = null;
			this.queue = null;			
		}		
	}
	public static final String NORTHBOUND_ENDPOINT_LIFETIME = 
			"org.eclipse.sensinact.ntbnd.endpoint.lifetime";
	
	private static final long DEFAULT_LIFETIME = 5 * 60 * 1000;
	
	private final Object lock = new Object();
	
	private Map<Term,NorthboundEndpoint> endpoints;
	private NorthboundMediator mediator;
	private Schedule schedule;
	private final long lifetime;
	
	/**
	 * Constructor
	 * 
	 * @param mediator the {@link Mediator} allowing the 
	 * NorthboundEndpoints to be instantiated to interact 
	 * with the OSGi host environment
	 */
	public NorthboundEndpoints(NorthboundMediator mediator)
	{		
		Object lifetimeProp = mediator.getProperty(NORTHBOUND_ENDPOINT_LIFETIME);
		Long l = null;
		if(lifetimeProp == null || (l = CastUtils.castPrimitive(
			Long.class, lifetimeProp))==null || l.longValue() < 1000)
		{
			this.lifetime = DEFAULT_LIFETIME;
			
		} else
		{
			this.lifetime = l.longValue();
		}
		this.endpoints = new WeakHashMap<Term,NorthboundEndpoint>();
		this.schedule = new Schedule();	
		this.mediator = mediator;
		new Thread(this.schedule).start();
	}

	/**
	 * Returns the {@link NorthboundEndpoint} whose {@link 
	 * Session} will be created using the authentication material 
	 * wrapped by the {@link Authentication} instance passed as 
	 * parameter. Returns an anonymous {@link Session} if the 
	 * {@link Authentication} argument is null
	 * 
	 * @param authentication the {@link Authentication} wrapping 
	 * the authentication material
	 * 
	 * @return the {@link NorthboundEndpoint} for the specified
	 * authentication material
	 * 
	 * @throws InvalidCredentialException 
	 */
	public NorthboundEndpoint getEndpoint(Authentication<?> authentication)
		throws InvalidCredentialException
	{
		if(authentication == null)
		{
			return getEndpoint();
		}
		if(AuthenticationToken.class.isAssignableFrom(
				authentication.getClass()))
		{
			return getEndpoint((AuthenticationToken) 
					authentication);
		}
		if(Credentials.class.isAssignableFrom(
				authentication.getClass()))
		{
			return getEndpoint((Credentials)authentication);
		}
		return null;
	}

	/**
	 * Returns the {@link NorthboundEndpoint} for an anonymous 
	 * access
	 * 
	 * @return the {@link NorthboundEndpoint} for an anonymous 
	 * access
	 * 
	 * @throws InvalidCredentialException 
	 */
	public NorthboundEndpoint getEndpoint() 
			throws InvalidCredentialException
	{
		NorthboundEndpoint endpoint = new NorthboundEndpoint(mediator,null);
		Term term = new Term(endpoint.getSessionToken());
		term.reactivate();
		this.endpoints.put(term,endpoint);		
		this.schedule.add(term);
		return endpoint;
	}

	/**
	 * Returns the {@link NorthboundEndpoint} whose {@link 
	 * Session} will be created using the authentication material 
	 * wrapped by the {@link Credentials} instance passed as 
	 * parameter. Returns an anonymous {@link Session} if the 
	 * {@link Credentials} argument is null
	 * 
	 * @param credentials the {@link Credentials} wrapping the
	 * authentication material
	 * 
	 * @return the {@link NorthboundEndpoint} for the specified
	 * authentication material
	 * 
	 * @throws InvalidCredentialException 
	 */
	public NorthboundEndpoint getEndpoint(Credentials credentials)
			throws InvalidCredentialException
	{
		if(credentials == null)
		{
			return getEndpoint();
		}
		NorthboundEndpoint endpoint = new NorthboundEndpoint(mediator, 
					credentials);
		Term term = new Term(endpoint.getSessionToken());
		term.reactivate();
		this.endpoints.put(term,endpoint);		
		this.schedule.add(term);
		return endpoint;
	}


	/**
	 * Returns the already existing {@link NorthboundEndpoint} 
	 * associated to the {@link Session} whose String identifier 
	 * is  wrapped by the {@link AuthenticationToken} instance 
	 * passed as parameter. The lifetime of the {@link 
	 * NorthboundEndpoint} is 'reactivated' before it is returned.
	 * if the {@link AuthenticationToken} argument is null or the 
	 * {@link NorthboundEnpoint} does not already exist the method
	 * returns null.
	 * 
	 * @param token the {@link AuthenticationToken} wrapping the
	 * String identifier of the {@link Session}
	 * 
	 * @return the {@link NorthboundEndpoint} for the specified
	 * authentication material
	 * 
	 * @throws InvalidCredentialException 
	 */
	public NorthboundEndpoint getEndpoint(AuthenticationToken token) 
		throws InvalidCredentialException
	{
		if(token == null)
		{
			return null;
		}
		String sessionIdentifier = token.getAuthenticationMaterial();
		if(this.schedule.update(sessionIdentifier))
		{
			Term t = this.schedule.getTerm(sessionIdentifier);
			return this.endpoints.get(t);
		}
		return null;
	}
	
	/**
	 * Closes this {@link NorthboundEndpoint}s collection
	 */
	public void close()
	{
		this.schedule.stop();
	}
}