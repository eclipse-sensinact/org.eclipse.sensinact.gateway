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

package org.eclipse.sensinact.gateway.util.stack;

/**
 * Abstract implementation of a {@link StackEnginetHandler}
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class AbstractStackEngineHandler<E> 
implements StackEngineHandler<E>
{
	/**
	 * the {@link StackEngine} stacking the <code>&lt;E&gt;</code>
	 * typed elements
	 */
	protected final StackEngine<E, StackEngineHandler<E>> eventEngine;
	
	protected final Thread stackEngineThread;

	/**
	 * Constructor
	 */
	//TODO : allow restart by defining a separated start method
	public AbstractStackEngineHandler() 
	{
		//instantiate the engine
		this.eventEngine = new StackEngine<E, StackEngineHandler<E>>(this);
		//start the engine
	    this.stackEngineThread = new Thread(eventEngine);
	    this.stackEngineThread.start();
	}

	/**
	 * Stops 
	 */
	public void stop()
	{
		//stop the engine
		this.eventEngine.stop();
	}

	/**
	 * Stops 
	 * @throws InterruptedException 
	 */
	public void close()
	{
		//wait for the stack emptiness for stopping
		this.eventEngine.closeWhenEmpty();
		try
		{
			this.stackEngineThread.join();
			
		} catch (InterruptedException e)
		{
			this.stackEngineThread.interrupt();
		}
	}
}
