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

package org.eclipse.sensinact.gateway.common.bundle;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;


import org.eclipse.sensinact.gateway.util.PropertyUtils;

/**
 * Abstract implementation of the {@link BundleActivator} interface
 */
public abstract class AbstractActivator<M extends Mediator> implements BundleActivator 
{
	/**
	 * Completes the starting process 
	 */
	public abstract void doStart() throws Exception;

	/**
	 * Completes the stopping process 
	 */
	public abstract void doStop() throws Exception;
	
	/**
	 * Creates and returns the specific {@link Mediator} 
	 * extended implementation instance
	 * 
	 * @param context
	 * 		the current {@link BundleContext}
	 * @return
	 * 		the specific {@link Mediator} extended 
	 * 		implementation instance
	 * 
	 * @ 
	 */
	public abstract M doInstantiate(BundleContext context) ;
	
	/**
	 * {@link Mediator} extended implementation instance
	 */
	protected M mediator;
	
	/**
	 * Specific properties define for the current 
	 * {@link org.osgi.framework.Bundle}
	 */
	protected Properties properties;
	
	/**
	 * @inheritDoc
	 * 
	 * @see org.osgi.framework.BundleActivator#
	 * start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception 
	{
		 Properties properties = new Properties();
		 
		 URL config  =  context.getBundle().getResource(
				 "sensiNact-conf.xml");
		 
		 if(config != null)
		 {
			 InputStream input = config.openStream();  
			 properties.loadFromXML(input);
			 input.close();
		 }
	     //initialize fields
	     this.properties = properties;	     
	     this.mediator = this.initMediator(context);
	     //complete starting process
	     this.doStart();
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.osgi.framework.BundleActivator#
	 * stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception 
	{
		doStop();
		if(this.mediator != null)
		{
			this.mediator.deactivate();			
		}
		this.mediator = null;
	}
	
	/**
	 * Returns the value object of the property whose key is
	 * passed as parameter
	 * 
	 * @param property
	 * 		the property key to return the value of
	 * @return
	 * 		the value object of the property for the 
	 * 		specified key
	 */
	protected Object getProperty(String property)
	{
		return PropertyUtils.getProperty(
				this.mediator.getContext(), 
				this.properties, 
				property);
	}
	
    /**
	 * Initializes and returns the {@link Mediator} of the
	 * current {@link Bundle}
	 * 
	 * @param context
	 * 		The current {@link BundleContext} 
	 * @return
	 * 		 the {@link Mediator}
	 * @ 
	 * @throws IOException 
	 */
	protected M initMediator(BundleContext context) 
			
	{
		 M mediator = doInstantiate(context);
		 
		 String logLevel = PropertyUtils.getProperty(
				 context, this.properties, "log.level");
		
		 if(logLevel != null)
		 {		
			 int level = LogExecutor.LOG_INFO;
			 try
			 {
				 level = Integer.parseInt(logLevel);
				 mediator.setLogLevel(level);
				 
			 } catch(NumberFormatException e)
			 {
				 level = LogExecutor.NO_LOG;
			 }
		 } 
		 if(this.properties != null)
		 {
			 Iterator<Entry<Object, Object>> iterator = 
					 this.properties.entrySet().iterator();
			 
			 while(iterator.hasNext())
			 {
				 Entry<Object, Object> entry = iterator.next();
				 if(String.class == entry.getKey().getClass())
				 {
					 mediator.setProperty((String) entry.getKey(), 
							 entry.getValue());
				 }
			 }
			 
		 }
		 return mediator;
	}

}
