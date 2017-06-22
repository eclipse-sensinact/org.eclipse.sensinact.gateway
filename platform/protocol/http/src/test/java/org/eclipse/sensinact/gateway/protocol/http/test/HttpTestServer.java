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
package org.eclipse.sensinact.gateway.protocol.http.test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import org.eclipse.sensinact.gateway.protocol.http.server.Server;
import org.eclipse.sensinact.gateway.util.ReflectUtils;

/**
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class HttpTestServer implements Runnable,CallbackCollection
{	
	private Server server;
	Map<Class<? extends Annotation>, List<Callback>> callbacks;
	

	public HttpTestServer(int port) throws Exception
	{
		this.callbacks = new HashMap<Class<? extends Annotation>, List<Callback>>();
		this.server = new Server(new HttpServerTestHandler(this));
		this.server.bind("127.0.0.1", port);
	}


	public void registerCallback(Object callback)
	{
		Map<Method, doGet> getMethods  = 
				ReflectUtils.getAnnotatedMethods(
				callback.getClass(), doGet.class);
		
		if(getMethods != null && getMethods.size()>0)
		{
			List<Callback> callbackList = this.callbacks.get(
					doGet.class);
			
			if(callbackList == null)
			{
				callbackList = new ArrayList<Callback>();
				this.callbacks.put(doGet.class, callbackList);
			}
			Iterator<Method> iterator = getMethods.keySet().iterator();
			while(iterator.hasNext())
			{
				callbackList.add(new Callback(callback, iterator.next()));
			}
		}
		Map<Method, doPost> postMethods  = 
				ReflectUtils.getAnnotatedMethods(
				callback.getClass(), doPost.class);

		if(postMethods != null && postMethods.size()>0)
		{
			List<Callback> callbackList = this.callbacks.get(
					doPost.class);
			
			if(callbackList == null)
			{
				callbackList = new ArrayList<Callback>();
				this.callbacks.put(doPost.class, callbackList);
			}
			Iterator<Method> iterator = postMethods.keySet().iterator();
			while(iterator.hasNext())
			{
				callbackList.add(new Callback(callback, iterator.next()));
			}
		}
	}

	public boolean isStarted() 
	{
		return this.running;
	}
	
	public void start() throws Exception 
	{
		this.server.start();
		Thread.sleep(2000);
	}
	
	public void stop() throws Exception 
	{
		this.server.stop();
		Thread.sleep(2000);
	}
	
	private boolean running = false;
	
	/** 
	 * @inheritDoc
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		running = true;
		try
		{
			this.start();
			
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		running = false;
	}

	/**
	 * @param class1
	 * @return
	 */
	public List<Callback> getdoGetCallbacks()
	{
		return this.callbacks.get(doGet.class);
	}
	
	/**
	 * @param class1
	 * @return
	 */
	public List<Callback> getdoPostCallbacks()
	{
		return this.callbacks.get(doPost.class);
	}
}
