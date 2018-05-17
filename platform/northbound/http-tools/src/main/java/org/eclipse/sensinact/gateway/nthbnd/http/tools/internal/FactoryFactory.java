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
package  org.eclipse.sensinact.gateway.nthbnd.http.tools.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.felix.http.api.ExtHttpService;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.nthbnd.http.callback.internal.CallbackFactory;
import org.eclipse.sensinact.gateway.nthbnd.http.forward.internal.ForwardingFactory;

/**
 * A FactoryFactory is in charge of creating {@link CallbackFactory}s and
 * {@link ForwardingFactory}s to be attached to registered {@link 
 * ExtHttpService}s
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class FactoryFactory 
{
	private Mediator mediator;
	private String appearingKey;
	private String disappearingKey;

	private Map<ExtHttpService, ForwardingFactory> forwarders;
	private Map<ExtHttpService, CallbackFactory> callbacks;
	
	/**
	 * Constructor
	 * 
	 * @param mediator the {@link Mediator} allowing the ForwardingIntallerFactory
	 * to be instantiated to interact with the OSGi host environment
	 */
	public FactoryFactory(Mediator mediator) 
	{
		this.mediator = mediator;
		this.callbacks = Collections.synchronizedMap(
				new HashMap<ExtHttpService,CallbackFactory>());
		this.forwarders = Collections.synchronizedMap(
				new HashMap<ExtHttpService,ForwardingFactory>());
	}

	/**
	 * Starts this ForwardingInstallerFActory and starts to observe the registration and 
	 * the unregistration of the {@link ExtHttpService}s
	 */
	public void start() throws Exception
	{
	    appearingKey = mediator.attachOnServiceAppearing(ExtHttpService.class, null, new Executable<ExtHttpService,Void>()
	    {
            /**
             * @inheritDoc
             *
             * @see org.eclipse.sensinact.gateway.common.execution.Executable#
             * execute(java.lang.Object)
             */
            public Void execute(ExtHttpService service)
            {	
            	CallbackFactory callback = new CallbackFactory(mediator, service);
	    		FactoryFactory.this.callbacks.put(service, callback);
	    		callback.start();
    	    
            	ForwardingFactory forwarder = new ForwardingFactory(mediator, service);
	    		FactoryFactory.this.forwarders.put(service, forwarder);
	    		forwarder.start();
                return null;        	    
            }
	    });
	    disappearingKey = mediator.attachOnServiceDisappearing(ExtHttpService.class, null, new Executable<ExtHttpService,Void>()
	    {
            /**
             * @inheritDoc
             *
             * @see org.eclipse.sensinact.gateway.common.execution.Executable#
             * execute(java.lang.Object)
             */
            public Void execute(ExtHttpService service)
            {	  
            	CallbackFactory callback = 
            		FactoryFactory.this.callbacks.remove(service);
	    		if(callback!=null)
	    		{
	    			callback.stop();
	    		}
            	ForwardingFactory forwarder = 
            		FactoryFactory.this.forwarders.remove(service);
	    		if(forwarder!=null)
	    		{
	    			forwarder.stop();
	    		}
                return null;
            }
	    });
	}

	/**
	 * Stops this ForwardingInstallerFactory and stops to observe the registration and 
	 * the unregistration of the {@link ExtHttpService}s
	 */
	public void stop() throws Exception
	{
	    mediator.detachOnServiceAppearing(ExtHttpService.class, null, appearingKey);
	    mediator.detachOnServiceDisappearing(ExtHttpService.class, null, disappearingKey);
	    synchronized(this.callbacks)
	    {
	    	Iterator<CallbackFactory> it = this.callbacks.values().iterator();
	    	while(it.hasNext())
	    	{
	    		it.next().stop();
	    		it.remove();
	    	}
	    }
    	synchronized(this.forwarders)
	    {
	    	Iterator<ForwardingFactory> it = this.forwarders.values().iterator();
	    	while(it.hasNext())
	    	{
	    		it.next().stop();
	    		it.remove();
	    	}
	    }
	}
}
