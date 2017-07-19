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
package org.eclipse.sensinact.gateway.core.message;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class AbstractSnaAgentCallback implements SnaAgentCallback
{
    private static final String[] UNLISTENED = new String[]{
    	"/sensiNact/system","/AppManager/admin"
    };

	/**
	 * The string formated location of service providers
	 * that have already been processed by this {@link 
	 * SnaAgentCallback}
	 */
	private Map<String, String> locations;

	/**
	 * Defines how many messages are currently processed
	 */
	protected int used;

	/**
	 * Constructor
	 * 
	 * @param mediator
	 * 		the {@link Mediator} that will be used 
	 * 		by the AbstractSnaAgentCallback to instantiate
	 */
	protected AbstractSnaAgentCallback()
	{
		this.locations = new HashMap<String,String>();
		this.used = 0;
	}
	
	/**
	 * Returns the String location for the service provider
	 * whose path is passed as parameter 
	 * 
	 * @param path
	 * 		the path  of the service provider for which to
	 * 		retrieve the string location
	 * @return
	 * 		the String location for the specified path
	 */
    protected String getLocation(String serviceProvider)
    {
		synchronized(this.locations)
		{
			return this.locations.get(serviceProvider);
		}
    }
    
    /**
	 * Sets the String location for the service provider
	 * whose path is passed as parameter 
	 * 
	 * @param path
	 * 		the path  of the service provider for which to
	 * 		set the string location
	 * @param location
	 * 		the string location to set
	 */
    protected void setLocation(String serviceProvider, String location)
    {
		synchronized(this.locations)
		{
			this.locations.put(serviceProvider, location);	
		}
    }
	
    /**
     * @return
     */
    protected int used()
    {
    	return used;
    }
    
	/**
	 * @inheritDoc
     *
     * @see MessageRegisterer#
     * register(SnaMessage)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void register(SnaMessage message)
    {
		used++;    	
    	if(message == null)
		{
			used--;
			return;
		}	
		String path = message.getPath();
		if(path == null)
		{
			used--;
		    return;
		}	
		int index = 0;
		int length = UNLISTENED==null?0:UNLISTENED.length;
		for(;index < length; index++)
		{			
			String unlistened = UNLISTENED[index];
			if(unlistened == null)
			{
				continue;
			}
	    	if(path.startsWith(unlistened))
	    	{
				used--;
	    		return;
	    	}
		}
		switch(((SnaMessageSubType)message.getType()
				).getSnaMessageType())
		{
			case ERROR:
				this.doHandle((SnaErrorMessageImpl) message);
				break;
			case LIFECYCLE:
				this.doHandle((SnaLifecycleMessageImpl) message);
				break;
			case RESPONSE:
				this.doHandle((SnaResponseMessage) message);
				break;
			case UPDATE:
				this.doHandle((SnaUpdateMessageImpl) message);
				break;	
			default:;
		}
		used--;
    }    
}