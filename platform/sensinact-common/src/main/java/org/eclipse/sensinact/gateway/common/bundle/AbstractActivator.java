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

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;
import org.eclipse.sensinact.gateway.util.PropertyUtils;

/**
 * Abstract implementation of the {@link BundleActivator} interface
 */
public abstract class AbstractActivator<M extends Mediator> implements BundleActivator 
{
	public static final String SENSINACT_CONFIG_FILE = "sensiNact-conf.xml";
	public static final String DEFAULT_BUNDLE_PROPERTY_FILEDIR="felix.fileinstall.dir";

	/**
	 * Completes the starting process, this alternative method allows extentions to override this method and receive properties declared in cfg file.
	 */
	public void doStart(Dictionary<String,String> properties) throws Exception {
		doStart();
	}

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
				 SENSINACT_CONFIG_FILE);
		 
		 if(config != null)
		 {
			 InputStream input = config.openStream();  
			 properties.loadFromXML(input);
			 input.close();
		 }
	     //initialize fields
	     this.properties = properties;
	     this.mediator = this.initMediator(context);

		/** Integrate local bundle property **/
		final Dictionary<String,String> bundleProperties=loadBundleProperties(context);
		Enumeration<String> enumProperties=bundleProperties.keys();
		while(enumProperties.hasMoreElements()){
			final String key=enumProperties.nextElement();
			final String value=bundleProperties.get(key);

			try {
				this.properties.setProperty(key,value);
			}catch(NullPointerException cpe){
				cpe.printStackTrace();
				this.mediator.log(LogService.LOG_WARNING,String.format("Failed to set property/value for the local abstract activator properties"));
			}



		}

		//complete starting process
		 this.doStart(bundleProperties);
	}

	private Dictionary<String,String> loadBundleProperties(BundleContext context)
	{
		Properties bundleProperties=null;
		Hashtable<String, String> map = new Hashtable<String,String>();

		try{
			final String fileInstallDir=context.getProperty(DEFAULT_BUNDLE_PROPERTY_FILEDIR);

			mediator.info("Configuration directory %s",fileInstallDir);

			final String symbolicName=context.getBundle().getSymbolicName();
			mediator.info("Bundle symbolic name %s");

			bundleProperties=new Properties();

			final String bundlePropertyFileName=String.format(
					"%s/%s.properties",fileInstallDir,symbolicName);
			final String bundlePropertyFileNameFallback=String.format(
					"%s/%s.property",fileInstallDir,symbolicName);

			/**
			 * Looks for property files put into config directory
			 */
			try
			{
				bundleProperties.load(new FileInputStream(bundlePropertyFileName));
				mediator.warn("File %s loaded successfully",bundlePropertyFileName);
				logBundleProperties(symbolicName,bundlePropertyFileName,bundleProperties);
			
			}catch(FileNotFoundException e)
			{
				mediator.warn("Failed to load bundle property file %s, trying %s.",
						bundlePropertyFileName, 
						bundlePropertyFileNameFallback);
				
				bundleProperties.load(new FileInputStream(bundlePropertyFileNameFallback));
				logBundleProperties(symbolicName, bundlePropertyFileNameFallback, 
						bundleProperties);
				mediator.warn("File %s loaded successfully",bundlePropertyFileNameFallback);
			}

			for (Map.Entry<Object,Object> name: bundleProperties.entrySet()){
				map.put(name.getKey().toString(), name.getValue().toString());
			}
		}catch(Exception e){
			bundleProperties=null;
		}
		return map;
	}

	private void logBundleProperties(String bundleName, 
			String propertyFile,Properties properties)
	{
		mediator.info("Loading properties for bundle %s located in %s", 
				bundleName, propertyFile);
		for(Map.Entry<Object,Object> entry:properties.entrySet())
		{
			mediator.info("%s:%s",entry.getKey(),entry.getValue());
		}

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
