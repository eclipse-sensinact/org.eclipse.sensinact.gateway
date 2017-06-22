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
package org.eclipse.sensinact.gateway.protocol.http.client.mid;

import java.util.logging.Logger;

import org.eclipse.sensinact.gateway.protocol.http.client.Request;
import org.eclipse.sensinact.gateway.protocol.http.client.Response;
import org.eclipse.sensinact.gateway.util.stack.AbstractStackEngineHandler;
import org.eclipse.sensinact.gateway.util.stack.StackEngineHandler;


/**
 * Intermediate client to manage execution errors. A recurrent IntermediateClientRequest 
 * can be defined as well as, after an error being occurred, a specific reactivation
 * one. If nothing disallowed to use a recurrent IntermediateClientRequest with punctual ones, 
 * this class has not be defined for that use and the resulting behavior is difficult
 * to predict. For a recurrent IntermediateClientRequest, the execution is scheduled according 
 * to the defined initial delay.
 */
public class MidClient<RESPONSE extends Response, 
REQUEST extends Request<RESPONSE> & Reusable> extends 
AbstractStackEngineHandler<REQUEST> 
{
	private static Logger LOG = Logger.getLogger(MidClient.class.getName());
	
	public static final int DEFAULT_MAX_STACK_SIZE = 50;
	//initial wait of one minute
	public static final int DEFAULT_INITIAL_DELAY = 1000*60;
	 //maximum wait of four hours 
	public static final int DEFAULT_MAX_DELAY = 1000*3600*4;	

	/**
	 * the {@link Reusable} to execute first
	 * after an error occurred 
	 */
	protected REQUEST reactivation;
	/**
	 * the listener handling the response of an {@link Reusable}
	 * execution
	 */
	protected final MidClientListener<RESPONSE> listener;
	
	protected int initialDelay;
	protected int maxDelay;
	
	protected int currentDelay;	
	protected boolean reactivate;
	
	/**
	 * Constructor
	 */
	public MidClient( 
			MidClientListener<RESPONSE> listener)
	{
		this(listener, MidClient.DEFAULT_MAX_STACK_SIZE);
	}

	/**
	 * Constructor
	 */
	public MidClient(
			MidClientListener<RESPONSE> listener,
			int maxStackSize)
	{
		super();
		super.eventEngine.setMaxStackSize(maxStackSize<=0
				?DEFAULT_MAX_STACK_SIZE:maxStackSize);	
		
		this.listener = listener;
		
		this.currentDelay = -1;
		this.initialDelay =  -1;
		this.maxDelay = -1;
		this.reactivate = false;
	}

	/**
	 * Defines the first {@link Reusable} to be 
	 * executed by IntermediateClient after a previous execution
	 * error.
	 * 
	 *  @param reactivation
	 *  	the first {@link Reusable} to be 
	 * 		executed by IntermediateClient after an error.
	 */
	public void setReactivationRequest (REQUEST reactivation)
	{
		this.reactivation = reactivation;
	}

	/**
	 * Returns the maximum size of the Stack of {@link 
	 * Reusable}s of this IntermediateClient.
	 * 
	 *  @return
	 *  	the maximum size of the Stack of {@link 
	 *  	Reusable}s of this IntermediateClient
	 */
	public int getMaxStackSize()
	{
		return super.eventEngine.getMaxStackSize();
	}
	
	/**
	 * Defines the initial delay between two tries of an {@link 
	 * Reusable} execution when an error occurred
	 *  
	 *  @param intialDelay
	 *  	the initial delay between two tries of an {@link 
	 * 		Reusable} execution
	 */
	public void setInitialDelay(int initialDelay)
	{
		this.initialDelay = initialDelay;
	}	

	/**
	 * Returns the initial delay between two tries of an {@link 
	 * Reusable} execution when an error occurred. 
	 * After each happening error the delay between two tries is 
	 * multiplied by two until it has reached the maximum waiting 
	 * delay. 
	 *  
	 *  @return
	 *  	the initial delay between two tries of an {@link 
	 * 		Reusable} execution
	 */
	public int getInitialDelay()
	{
		if(this.initialDelay < 0)
		{
			return MidClient.DEFAULT_INITIAL_DELAY;
		}
		return this.initialDelay;
	}
	
	/**
	 * Defines the maximum delay between two tries of an {@link 
	 * Reusable} execution when an error occurred
	 *  
	 *  @param intialDelay
	 *  	the initial delay between two tries of an {@link 
	 * 		Reusable} execution
	 */
	public void setMaxDelay(int maxDelay)
	{
		this.maxDelay = maxDelay;
	}	

	/**
	 * Returns the maximum delay between two tries of an {@link 
	 * Reusable} execution when an error occurred
	 *  
	 *  @return
	 *  	the maximum delay between two tries of an {@link 
	 * 		Reusable} execution
	 */
	public int getMaxDelay()
	{
		if(this.maxDelay < 0)
		{
			return MidClient.DEFAULT_MAX_DELAY;
		}
		return this.maxDelay;
	}
	
	/**
	 * Returns the delay before the next try of {@link 
	 * Reusable} execution
	 *  
	 *  @return
	 *  	the delay before the next try of {@link 
	 * 		Reusable} execution
	 */
	public void updateDelay()
	{
		if(this.currentDelay < 0)
		{
			this.currentDelay = this.getInitialDelay();	
			
		} else if(currentDelay < this.getMaxDelay())
		{	
			int tmpDelay = this.currentDelay*2;
			this.currentDelay = tmpDelay>this.getMaxDelay()
					?this.getMaxDelay():tmpDelay;
		}
	}
	
	/**
	 * Returns the delay before the next try of {@link 
	 * Reusable} execution
	 *  
	 *  @return
	 *  	the delay before the next try of {@link 
	 * 		Reusable} execution
	 */
	public int getCurrentDelay()
	{
		return this.currentDelay;
	}
	
	/**
	 * The current wait delay fall back into its initial 
	 * value
	 */
    protected void resetDelay()
    {
    	this.currentDelay = -1;
    }
	
	/**
	 * 
	 * @param request
	 */
	public void addRequest(REQUEST request)
	{
		super.eventEngine.push(request);
	}

	/**
	 * @inheritDoc
	 *
	 * @see StackEngineHandler#doHandle(java.lang.Object)
	 */
    @Override
    public void doHandle(REQUEST element)
    {
    	//System.out.println("DO HANDLE ["+currentDelay+"] " + element);
    	RESPONSE response = null;
		try
		{
			if(reactivate && this.reactivation!=null)
			{
				this.reactivate = false;			
				response = this.reactivation.send();
				this.listener.respond(response);
			}			
			response = element.send();	
			if(response.getStatusCode() >= 400)
			{
				throw new HttpResponseException(
						response.getStatusCode(), 
						response.getContent(),
						response.getHeaders());
			}
			this.resetDelay();
		}
        catch (HttpResponseException e)
        {
        	if(handleHttpError(e))
        	{
	            handleError(e, element);
	            
        	} else
        	{
    			this.resetDelay();			
    			this.listener.respond(response);        		
        	}
        }
        catch (Exception e)
        {
        	handleError(e, element);
        	
        } finally 
        {
			this.listener.respond(response);
        }
    }
	
	/**
	 * @param e
	 * @return
	 */
	protected boolean handleHttpError(HttpResponseException e)
	{
		return true;
	}
	
    /**
     * @param element
     */ 
    private void handleError(Exception e, REQUEST element)
    {
    	//LOG.log(Level.SEVERE, e.getMessage(),e);
		this.updateDelay();		
		this.reactivate = true;
		super.eventEngine.locked(this.getCurrentDelay());
		if(element.isReusable())
		{
			super.eventEngine.push((REQUEST) element.copy());
		}
    }
}
