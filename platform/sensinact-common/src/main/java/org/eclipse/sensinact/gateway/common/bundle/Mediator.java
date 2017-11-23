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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.util.PropertyUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Mediator Pattern Architecture purpose
 */
public class Mediator
{
	public static final String DEFAULT_BUNDLE_PROPERTY_FILEDIR="felix.fileinstall.dir";
	
	public static final String SENSINACT_CONFIG_FILE = "sensiNact-conf.xml";
	
	/**
     * ThreadLocal {@link ServiceCaller} used to interact
     * with the OSGi host environment by calling and
     * registering services
     */
    public final ThreadLocal<ServiceCaller> CALLERS = 
    		new ThreadLocal<ServiceCaller>()
    {
    	/** 
    	 * @inheritedDoc
    	 * 
    	 * @see java.lang.ThreadLocal#initialValue()
    	 */
    	public ServiceCaller initialValue()
    	{
    		return new ServiceCaller(Mediator.this.getContext());
    	}
    };
			
	private int logLevel;		
	protected Properties properties;	
	protected BundleContext context;
	private Map<String, TrackerCustomizer<?>> customizers;
	private List<ServiceTracker<?,?>> trackers;
	private List<ServiceRegistration<?>> registrations;
	private MediatorManagedConfiguration configuration;
	
	/**
	 * Constructor
	 * 
	 * @param context
	 */
	public Mediator(BundleContext context)
	{
		this.logLevel = -1;
		this.context = context;
		this.properties = new Properties();
		this.customizers = new HashMap<String, TrackerCustomizer<?>>();
		this.trackers = new ArrayList<ServiceTracker<?,?>>();
		this.registrations = new ArrayList<ServiceRegistration<?>>();		
		try 
		{
			URL config  =  context.getBundle(
			 ).getResource(SENSINACT_CONFIG_FILE);
			
			if(config != null)
			{
				InputStream input;
				input = config.openStream();
				properties.loadFromXML(input);
				input.close();
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}		
		loadBundleProperties();
		
		String managed = (String) this.getProperty(
			MediatorManagedConfiguration.MANAGED_SENSINACT_MODULE);
			
		if(managed!=null && Boolean.parseBoolean(managed))
		{
			this.configuration = new MediatorManagedConfiguration(this, 
				context.getBundle().getSymbolicName().replace('-', '.'));			
			this.configuration.register();
		}
	}
	
	private void loadBundleProperties()
	{
		updateLogLevel("felix.log.level");
		
		final String fileInstallDir = context.getProperty(
				DEFAULT_BUNDLE_PROPERTY_FILEDIR);
		
		if(fileInstallDir == null)
		{
			return;
		}
		info("Configuration directory %s",fileInstallDir);

		final String symbolicName= context.getBundle().getSymbolicName();
		info("Bundle symbolic name %s", symbolicName);

		final String bundlePropertyFileName=String.format(
				"%s/%s.properties",fileInstallDir,symbolicName);
		final String bundlePropertyFileNameFallback=String.format(
				"%s/%s.property",fileInstallDir,symbolicName);

		Properties bundleProperties = new Properties();
		
		/**
		 * Looks for property files put into config directory
		 */
		try
		{
			bundleProperties.load(new FileInputStream(bundlePropertyFileName));
			warn("File %s loaded successfully",bundlePropertyFileName);
			logBundleProperties(symbolicName,bundlePropertyFileName,
					bundleProperties);
		
		} catch(IOException e)
		{
			warn("Failed to load bundle property file %s, trying %s.",
					bundlePropertyFileName, 
					bundlePropertyFileNameFallback);
			try
			{
				bundleProperties.load(new FileInputStream(bundlePropertyFileNameFallback));
				logBundleProperties(symbolicName, bundlePropertyFileNameFallback, 
					bundleProperties);
				
				warn("File %s loaded successfully",bundlePropertyFileNameFallback);
				
			} catch(IOException ex)
			{
				error("Failed to load bundle property file %s.",
						bundlePropertyFileNameFallback);
			}
		}
		for (Map.Entry<Object,Object> name: bundleProperties.entrySet()){
			this.properties.put(name.getKey().toString(), name.getValue().toString());
		}
		updateLogLevel("log.level");
	}
	
	private void updateLogLevel(String property)
	{
	    String logLevelStr = (String)getProperty(property);
	    try
	    {
	    	int logLevel = Integer.parseInt(logLevelStr);
	    	this.setLogLevel(logLevel);
	    	
	    } catch(Exception e)
	    {	
	    	if(logLevel < 0)
	    	{
	    		this.setLogLevel(LogExecutor.DEFAULT_LOG_LEVEL);
	    	}
	    }
	}

	private void logBundleProperties(String bundleName, 
			String propertyFile,Properties properties)
	{
		info("Loading properties for bundle %s located in %s", 
				bundleName, propertyFile);
		for(Map.Entry<Object,Object> entry:properties.entrySet())
		{
			info("%s:%s",entry.getKey(),entry.getValue());
		}
	}
	
	/**
	 * Adds a {@link ManagedConfigurationListener} to be notified
	 * when the properties of this this Mediator's
	 * {@link MediatorManagedConfiguration} are updated
	 * 
	 * @param listener the {@link ManagedConfigurationListener} to add
	 */
	public void addListener(ManagedConfigurationListener listener)
	{
		if(configuration != null)
		{
			this.configuration.addListener(listener);
		}
	}


	/**
	 * Removes the {@link ManagedConfigurationListener} to be removed from
	 * the list of those to be notified when the properties of this Mediator's
	 * {@link MediatorManagedConfiguration} are updated
	 * 
	 * @param listener the {@link ManagedConfigurationListener} to remove
	 */
	public void deleteListener(ManagedConfigurationListener listener)
	{
		if(configuration != null)
		{
			this.configuration.deleteListener(listener);
		}
	}

	/**
	 * Registers the service passed as parameter in the 
	 * OSGi host environment using the specified type and
	 * Dictionary
	 * 
	 * @param service the service to be registered
	 * @param serviceType the type of the service to be registered
	 * @param properties the set of properties associated to the 
	 * service registration
	 */
	public <S> void register(S service, Class<S> serviceType, 
			Dictionary properties)
	{
		synchronized(this.registrations)
		{
			this.registrations.add(
					this.context.registerService(serviceType, 
							service, properties));
		}
	}
	
    /**
     * @param serviceType
     * @param executor
     * @return
     */
    public <S, R> R callService(Class<S> serviceType, 
    	Executable<S,R> executor)
    {
        return this.callService(serviceType, null, executor);
    }

    /**
     * @param serviceType
     * @param filter
     * @param executor
     * @return
     */
    public <S, R> R callService(Class<S> serviceType,
    	String filter, Executable<S,R> executor) 
    {
    	ServiceCaller caller = CALLERS.get();
    	caller.attach();
    	try
    	{
    		return caller.callService(serviceType, filter, executor);
    		
    	} catch(Exception e)
    	{
    		this.error(e);
    		
    	} finally
    	{
    		if(caller.release() == 0)
    		{
    			CALLERS.remove();
    		}
    	} 	
    	return null;
    }
    
    /**
     * @param serviceType
     * @param executor
     */
    public <S> void callServices(Class<S> serviceType, 
    	Executable<S,Void> executor) 
    {
        this.callServices(serviceType, null, executor);
    }

    /**
     * @param serviceType
     * @param filter
     * @param executor
     */
    public <S> void callServices(Class<S> serviceType, 
    		String filter, Executable<S,Void> executor) 
    {	        
    	ServiceCaller caller = CALLERS.get();
    	caller.attach();
    	try
    	{
    		caller.callServices(serviceType, filter, executor);
    		
    	} catch(Exception e)
    	{
    		this.error(e);
    		
    	} finally
    	{
    		if(caller.release() == 0)
    		{
    			CALLERS.remove();
    		}
    	} 
    }

    /**
     * @param serviceType
     * @param returnType
     * @param filter
     * @param executor
     * @return
     */
    public <S, R> Collection<R> callServices(Class<S> serviceType, 
    		Class<R> returnType, String filter, 
    		Executable<S,R> executor)
    {	
    	ServiceCaller caller = CALLERS.get();
    	caller.attach();
    	try
    	{
    		return caller.callServices(
    				serviceType, returnType, 
    				filter, executor);
    		
    	} catch(Exception e)
    	{
    		this.error(e);
    		
    	} finally
    	{
    		if(caller.release() == 0)
    		{
    			CALLERS.remove();
    		}
    	} 
    	return Collections.<R>emptyList();
    }

	/**
	 * @param serviceType
	 * @param filter
	 * @param executors
	 */
	public <S> void onServiceAppearing(Class<S> serviceType, 
			String filter, List<Executable<S,Void>> executors)
	{
		String trackingFilter = createFilter(serviceType, filter);
		TrackerCustomizer<S> customizer = (TrackerCustomizer<S>) 
				this.customizers.get(trackingFilter);
		ServiceTracker<S,S> tracker = null;
		
		if(customizer == null)
		{
			customizer = new TrackerCustomizer<S>(this);
			this.customizers.put(trackingFilter, customizer);
			
			customizer.onAdding(executors);
			tracker = new ServiceTracker<S,S>(context, 
					serviceType.getCanonicalName(), customizer);
			
			synchronized(this.trackers)
			{
				trackers.add(tracker);
			}
			tracker.open();
		} else
		{
			customizer.onAdding(executors);
		}
	}

	/**
	 * @param serviceType
	 * @param filter
	 * @param executor
	 */
	public <S> void onServiceAppearing(Class<S> serviceType, 
			String filter, Executable<S,Void> executor)
	{
		this.onServiceAppearing(serviceType, filter, 
				Collections.singletonList(
				executor));
	}

	/**
	 * @param serviceType
	 * @param filter
	 * @param executors
	 */
	public <S> void onServiceDisappearing(Class<S> serviceType, 
			String filter, List<Executable<S,Void>> executors)
	{
		String trackingFilter = createFilter(serviceType, filter);
		TrackerCustomizer<S> customizer = (TrackerCustomizer<S>) 
				this.customizers.get(trackingFilter);
		ServiceTracker<S,S> tracker = null;
		
		if(customizer == null)
		{
			customizer = new TrackerCustomizer<S>(this);
			this.customizers.put(trackingFilter, customizer);
			customizer.onRemoving(executors);
			
			tracker = new ServiceTracker<S,S>(context, trackingFilter, 
					customizer);
			synchronized(this.trackers)
			{
				trackers.add(tracker);
			}
			tracker.open();
		} else
		{
			customizer.onRemoving(executors);
		}
	}

	/**
	 * @param serviceType
	 * @param filter
	 * @param executor
	 */
	public <S> void onServiceDisappearing(Class<S> serviceType, 
			String filter, Executable<S,Void> executor)
	{
		this.onServiceDisappearing(serviceType, filter,
				Collections.singletonList(executor));
	}

	/**
	 * @param serviceType
	 * @param filter
	 * @param executors
	 */
	public <S> void onServiceUpdating(Class<S> serviceType, 
			String filter, List<Executable<S,Void>> executors)
	{
		String trackingFilter = createFilter(serviceType, filter);
		
		TrackerCustomizer<S> customizer = (TrackerCustomizer<S>) 
				this.customizers.get(trackingFilter);
	
		ServiceTracker<S,S> tracker = null;
		
		if(customizer == null)
		{
			customizer = new TrackerCustomizer<S>(this);
			this.customizers.put(trackingFilter, customizer);
			
			customizer.onModifying(executors);
			tracker = new ServiceTracker<S,S>(context, trackingFilter, 
					customizer);
			synchronized(this.trackers)
			{
				trackers.add(tracker);
			}
			tracker.open();
		} else
		{
			customizer.onModifying(executors);
		}
	}	
	
	/**
	 * @param serviceType
	 * @param filter
	 * @param executor
	 */
	public <S> void onServiceUpdating(Class<S> serviceType, 
			String filter, Executable<S,Void> executor)
	{
		this.onServiceUpdating(serviceType, filter, 
				Collections.singletonList(executor));
	}	
	
	/**
	 * @param serviceType
	 * @param filter
	 * @return
	 */
	public String createFilter(Class<?> serviceType, String filter)
	{
		StringBuilder classBuilder = null;
		StringBuilder filterBuilder = null;
		
		if(serviceType != null)
		{
			classBuilder = new StringBuilder();
			classBuilder.append("(objectClass=");
			classBuilder.append(serviceType.getCanonicalName());
			classBuilder.append(")");
		}
		if(filter != null)
		{
			filterBuilder = new StringBuilder();
			
			if(classBuilder!=null)
			{
				filterBuilder.append("(&");
				filterBuilder.append(classBuilder.toString());
			}
			if(!filter.startsWith("("))
			{
				filterBuilder.append("(");
			}
			filterBuilder.append(filter);
			if(!filter.endsWith(")"))
			{
				filterBuilder.append(")");
			}
			if(classBuilder!=null)
			{
				filterBuilder.append(")");
			}
		} else
		{
			filterBuilder = classBuilder;
		}
		if(filterBuilder != null)
		{
			return filterBuilder.toString();
		}
		return null;
	}
	
    /**
     * Define the log level
     *  
     * @param logLevel
     * 		the log level
     */
    public void setLogLevel(int logLevel)
    {
    	this.logLevel = logLevel;

    	if(this.logLevel<LogExecutor.NO_LOG || this.logLevel>LogService.LOG_DEBUG)
		{
    		this.logLevel = LogExecutor.NO_LOG;
		}
    }
    
	/**
	 * the mediator is deactivated before bundle stopping
	 */
	public void deactivate()
	{
		synchronized(this.trackers)
		{
			int index = 0; 
			int length = trackers==null?0:trackers.size();
			for(;index < length;index++)
			{
				trackers.remove(0).close();
			}
		}
		synchronized(this.registrations)
		{
			int index = 0; 
			int length = this.registrations==null?0:this.registrations.size();
			for(;index < length;index++)
			{
				try
				{
					this.registrations.remove(0).unregister();
				} catch(IllegalStateException e)
				{}
			}				
		}
		this.customizers.clear();
		if(this.configuration != null)
		{
			this.configuration.unregister();
		}
	}
	
	/**
	 * Returns the associated {@link BundleContext}
	 * 
	 * @return
	 * 		 the associated {@link BundleContext}
	 */
	public BundleContext getContext()
	{
		return this.context;
	}

	/**
	 * @return
	 */
	public ClassLoader getClassLoader()
	{
		ClassLoader classloader = null;
		
		try
		{
			classloader =  this.getContext().getBundle(
			).adapt(BundleWiring.class).getClassLoader();
			
		}catch(NullPointerException e)
		{
			Enumeration<URL> entries = getContext().getBundle(
					).findEntries("/","*.class", true);
			
			if(entries!=null && entries.hasMoreElements())
			{
				String classname = entries.nextElement().getPath();
				int startIndex = 0;
				int endIndex = classname.length() - ".class".length();							
				startIndex+=classname.startsWith("/")?1:0;
				
				classname = classname.substring(startIndex, endIndex);					
				classname = classname.replace('/', '.');
				try 
				{
					Class<?> clazz = getContext().getBundle(
						).loadClass(classname);
					classloader = clazz.getClassLoader();
					
				} catch (ClassNotFoundException ex) 
				{
					this.error(ex);
				}
			}
		}
		if(classloader == null)
		{
			classloader = Thread.currentThread(
					).getContextClassLoader();
		}
		return classloader;
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
	public Object getProperty(final String property)
	{
		return PropertyUtils.getProperty(
				Mediator.this.context,
				Mediator.this.properties, 
				property);
	}
	
	/**
	 * Adds a property entry to this Mediator
	 * 
	 * @param property
	 * 		the property key to set the value of
	 * @param value
	 * 		the object value of the property
	 */
	public void setProperty(String property, Object value)
	{
		if(property == null)
		{
			return;
		}
		this.properties.put(property, value);
	}

	/**
     * Define whether or not a log message which log level is passed 
     * as parameter can be displayed according to the current log mode 
     * and log level
     * 
     * @param logLevel
     * 			the log level of the message to display
     * @return
     * 		true if the log level allow to display the message
     * 		false otherwise
     */
    public boolean isLoggable(int logLevel)
    {
    	return (this.logLevel>LogExecutor.NO_LOG && logLevel<=this.logLevel);
    }    
    
	/**
     * Returns true if the logger is configured 
     * to display error messages ; returns false
     * otherwise
     * 
     * @return
     * 		true if the logger is configured 
     * 		to display error messages ; <p/>false
     * 		otherwise
     */
    public boolean isErrorLoggable()
    {
    	return this.isLoggable(LogService.LOG_ERROR);
    }
    
    /**
     * Returns true if the logger is configured 
     * to display warning messages ; returns false
     * otherwise
     * 
     * @return
     * 		true if the logger is configured 
     * 		to display warning messages ; <p/>false
     * 		otherwise
     */
    public boolean isWarningLoggable()
    {
    	return this.isLoggable(LogService.LOG_WARNING);
    }
    
    /**
     * Returns true if the logger is configured 
     * to display info messages ; returns false
     * otherwise
     * 
     * @return
     * 		true if the logger is configured 
     * 		to display info messages ; <p/>false
     * 		otherwise
     */
    public boolean isInfoLoggable()
    {
    	return this.isLoggable(LogService.LOG_INFO);
    }
    
    /**
     * Returns true if the logger is configured 
     * to display debug messages ; returns false
     * otherwise
     * 
     * @return
     * 		true if the logger is configured 
     * 		to display debug messages ; <p/>false
     * 		otherwise
     */
    public boolean isDebugLoggable()
    {
    	return this.isLoggable(LogService.LOG_DEBUG);
    }
    
    /**
     * Display an information message through the LogService if
     * if exists
     * 
     * @param msg
     * 		the message to display
     */
    public void info(String msg, Object...variables)
    {
    	this.log(LogService.LOG_INFO, msg, variables);
    }

    /**
     * Display the execution stack trace of the 
     * {@link Throwable} object passed as parameter 
     * through the LogService if it exists.
     * 
     * @param thrown the {@link Throwable} to display the stacktrace of
     */
    public void error(Throwable thrown)
    {
    	this.log(LogService.LOG_ERROR, thrown, thrown.getMessage());
    }
    
    /**
     * Display an error message and the execution stack 
     * trace of the {@link Throwable} object passed as 
     * parameter through the LogService if it exists.
     * 
     * @param thrown the {@link Throwable} to display the stacktrace of
     * @param msg the message to display
     * @param variables the variable set of arguments parameterizing the message
     * to display
     */
    public void error(Throwable thrown, String msg, Object...variables)
    {
    	this.log(LogService.LOG_ERROR, thrown, msg, variables);
    }

    /**
     * Display an error message through the LogService if
     * if exists.
     * 
     * @param msg the message to display
     * @param variables the variable set of arguments parameterizing the message
     * to display
     */
    public void error(String msg, Object...variables)
    {
    	this.log(LogService.LOG_ERROR, msg, variables);
    }


    /**
     * Display an debug message through the LogService if
     * it exists.
     * 
     * @param msg the message to display
     * @param variables the variable set of arguments parameterizing the message
     * to display
     */
    public void debug(String msg, Object...variables)
    {
    	this.log(LogService.LOG_DEBUG, msg, variables);	
    }

    /**
     * Display a warning message through the LogService if
     * it exists.
     * 
     * @param msg the message to display
     * @param variables the variable set of arguments parameterizing the message
     * to display
     */
    public void warn(String msg, Object...variables)
    {
    	this.log(LogService.LOG_WARNING, msg, variables);
    }
 
    /**
     * Display an information message through the LogService if
     * if exists, and if the Log level is less or equals to the 
     * system's one 
     * 
     * @param level the Log level
     * @param msg the message to display
     * @param variables the variable set of arguments parameterizing the message
     * to display
     */
    protected void log(int level, String msg, Object...variables)
    {
    	this.log(level, null, msg, variables);
    }
    
    /**
     * Display an information message through the LogService if
     * if exists, and if the Log level is less or equals to the 
     * system's one 
     * 
     * @param level the Log level
     * @param throwable  the {@link Throwable} to display the stacktrace of
     * @param msg the message to display
     * @param variables the variable set of arguments parameterizing the message
     * to display
     */
    protected void log(int level, Throwable throwable, 
    		String msg, Object...variables)
    {
    	if(!this.isLoggable(level))
    	{
    		//do not process the message or search
    		//for the LogService if the message
    		//is not supposed to be displayed
    		return;
    	}
    	String message = null;
    	
    	//format the message
    	if(variables != null && variables.length > 0)
    	{    		
    		Object[] replacements = null;
    		
    		if(variables.length == 1 
    			&& variables[0]!=null
    			&& variables[0].getClass().isArray())
    		{
    			replacements = (Object[]) variables[0];
    			
    		} else
    		{
    			replacements = variables;
    		}
    		message = String.format(msg, replacements);
    		
    	} else
    	{
    		message = msg;
    	}
    	callService(LogService.class, new LogExecutor(level, message, throwable));
    }
}
