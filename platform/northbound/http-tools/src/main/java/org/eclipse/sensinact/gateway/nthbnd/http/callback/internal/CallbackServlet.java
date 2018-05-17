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
package org.eclipse.sensinact.gateway.nthbnd.http.callback.internal;

import java.io.IOException;

import javax.servlet.AsyncContext;
import javax.servlet.WriteListener;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.nthbnd.http.callback.CallbackContext;

/**
 * Callback servlet
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@SuppressWarnings("serial")
@WebServlet(asyncSupported=true)
public class CallbackServlet extends HttpServlet
{	
	public static enum CALLBACK_METHOD 
	{
		GET, POST, PUT, DELETE;
	}
	
	private Mediator mediator;
	private Executable<CallbackContext,Void> callbackProcessor;
	
	/**
	 * Constructor
	 * 
	 * @param mediator the {@link Mediator} allowing the CallbackServlet
	 * to be instantiated to interact with the OSGi host environment
	 * @param callbackProcessor the {@link Executable} allowing the 
	 * CallbackServlet to be instantiated to process the callback request
	 */
	public CallbackServlet(Mediator mediator, 
			Executable<CallbackContext, Void> callbackProcessor)
	{
		this.mediator = mediator;
		this.callbackProcessor = callbackProcessor;
	}

	private final void doCall(final CALLBACK_METHOD method, 
		HttpServletRequest request, HttpServletResponse response)
					throws IOException
	{
		if(response.isCommitted())
        {
        	return;
        } 
        final AsyncContext asyncContext;  
        if(request.isAsyncStarted())
        {
        	asyncContext = request.getAsyncContext();
        	
        } else
        {
        	asyncContext = request.startAsync(request,response);
        }        
        final CallbackContext context = new CallbackContext(mediator,
        		method.name(), asyncContext);
        
        response.getOutputStream().setWriteListener(new WriteListener()
        {
			@Override
			public void onWritePossible() throws IOException 
			{
				 HttpServletRequest request = 
				     (HttpServletRequest) asyncContext.getRequest();		        
				 HttpServletResponse response = 
				    (HttpServletResponse) asyncContext.getResponse();				 
			     try
			     {			    	
			    	 if(CallbackServlet.this.callbackProcessor != null)
			    	 {
			    		 CallbackServlet.this.callbackProcessor.execute(context);
			    	 }
				 } catch (Exception e) 
				 {
					e.printStackTrace();
					onError(e);
					response.sendError(520, "Internal server error");
					
				 }catch (Error e) 
				 {
					e.printStackTrace();
					
				 } finally
				 {
					if(request.isAsyncStarted())
					{
						asyncContext.complete();
					}
				 }
			}

			@Override
			public void onError(Throwable t) 
			{
				mediator.error(t);
			}
        	
        });
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see javax.servlet.http.HttpServlet#
	 * doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
    		throws IOException
	{
		doCall(CALLBACK_METHOD.GET, request,response);
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see javax.servlet.http.HttpServlet#
	 * doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
    		throws IOException
	{
		doCall(CALLBACK_METHOD.POST, request,response);
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see javax.servlet.http.HttpServlet#
	 * doPut(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
    public void doPut(HttpServletRequest request, HttpServletResponse response)
    		throws IOException
	{
		doCall(CALLBACK_METHOD.PUT, request,response);
	}

	/**
	 * @inheritDoc
	 *
	 * @see javax.servlet.http.HttpServlet#
	 * doDelete(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response)
    		throws IOException
	{
		doCall(CALLBACK_METHOD.DELETE, request,response);
	}
}
