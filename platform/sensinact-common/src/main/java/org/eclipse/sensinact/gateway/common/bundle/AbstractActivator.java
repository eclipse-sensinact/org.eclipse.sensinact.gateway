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

import org.eclipse.sensinact.gateway.common.annotation.Property;
import org.eclipse.sensinact.gateway.util.PropertyUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.service.log.LogService;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;

/**
 * Abstract implementation of the {@link BundleActivator} interface
 */
public abstract class AbstractActivator<M extends Mediator> implements BundleActivator 
{
	public static final String DEFAULT_BUNDLE_PROPERTY_FILEDIR="felix.fileinstall.dir";

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

		this.mediator = this.initMediator(context);

		//initialize fields
		this.properties = new Properties();

		/** Integrate local bundle property **/
		final Dictionary<String,String> bundleProperties=loadBundleProperties(context);

		updateMediatorProperties(this.mediator,bundleProperties);

		injectPropertyFields(context);
		
		//complete starting process
		this.doStart(bundleProperties);
	}

	private void updateMediatorProperties(M mediator,Dictionary<String,String> bundleProperties){
		Enumeration<String> enumProperties=bundleProperties.keys();
		while(enumProperties.hasMoreElements())
		{
			final String key=enumProperties.nextElement();
			final String value=bundleProperties.get(key);
			try
			{
				//Update local properties
				this.properties.setProperty(key,value);
				//Update mediator property
				mediator.setProperty(key,value.toString());

			}catch(NullPointerException cpe)
			{
				this.mediator.log(LogService.LOG_WARNING,String.format(
						"Failed to set property/value for the local abstract activator properties"));
			}
		}
	}

	/**
	 * Completes the starting process, this alternative method 
	 * allows extentions to override this method and receive 
	 * properties declared in cfg file.
	 */
	public void doStart(Dictionary<String,String> properties) 
			throws Exception 
	{
		doStart();
	}
	
	private void injectPropertyFields(BundleContext context) throws Exception {

		this.mediator.debug("Starting introspection in bundle %s", context.getBundle().getSymbolicName());
		for(Field field:this.getClass().getDeclaredFields()){
			this.mediator.debug("Evaluating field %s", field.getName());
			for(Annotation propertyAnnotation:field.getAnnotations()){
				try {
					if(! (propertyAnnotation instanceof Property)) continue;
					Property propAn=(Property)propertyAnnotation;
					String propertyName=null;
					if(!propAn.name().equals("")){
						propertyName=propAn.name();
					}else {
						propertyName=field.getName();
					}
					Object propertyValue=this.properties.getProperty(propertyName);
					if(propertyValue!=null){
						this.mediator.info("Setting property '%s' from bundle symbolic '%s' on field '%s' to value '%s'", propertyName, context.getBundle().getSymbolicName(), field.getName(), propertyValue);
						field.set(this, propertyValue);
					}else if(propAn.defaultValue()!=null&&!propAn.defaultValue().trim().equals("")){
						String value = propAn.defaultValue();
						field.set(this, propAn.defaultValue());
						this.mediator.info("Setting property '%s' from bundle symbolic name '%s' on field '%s' to default value which is '%s'", propertyName, context.getBundle().getSymbolicName(), field.getName(),value);
					}else {
						this.mediator.error("Property %s from symbolic name %s is mandatory, bundle might not be configured correctly", propAn.name(), context.getBundle().getSymbolicName());
						throw new Exception(String.format("Property '%s' from bundle symbolic name '%s' is mandatory, bundle might not be configured correctly",propertyName,context.getBundle().getSymbolicName()).toString());
					}
				} catch (IllegalAccessException e) {
					this.mediator.error(String.format("The field '%s' required property injection in bundle symbolic name '%s', although it was not possible to assign the value to the field, make sure the access signature is 'public' ",field.getName(),context.getBundle().getSymbolicName()).toString());
					throw new Exception(e);
				}
			}
		}
	}

	private Dictionary<String,String> loadBundleProperties(BundleContext context) throws Exception {
		Properties bundleProperties=null;
		Hashtable<String, String> map = new Hashtable<String,String>();

		try{
			final String fileInstallDir=context.getProperty(DEFAULT_BUNDLE_PROPERTY_FILEDIR);

			mediator.info("Configuration directory %s",fileInstallDir);

			final String symbolicName=context.getBundle().getSymbolicName();
			mediator.info("Bundle symbolic name %s",symbolicName);

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
		mediator.init();
		return mediator;
	}

}
