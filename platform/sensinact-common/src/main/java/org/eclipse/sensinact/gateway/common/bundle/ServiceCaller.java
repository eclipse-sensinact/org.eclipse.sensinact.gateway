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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
final class ServiceCaller
{
	private BundleContext context;
	private AtomicInteger count;
	private Map<String, List<ServiceReference<?>>> references;
	
	/**
	 * Constructor
	 * 
	 * @param context
	 */
	public ServiceCaller(BundleContext context)
	{
		this.context  = context;
		this.count = new AtomicInteger(0);
		this.references = new HashMap<String, List<ServiceReference<?>>>();
	}

	/**
	 * @param clazz
	 * @param filter
	 * @return
	 * @throws InvalidSyntaxException
	 */
	private <S> List<ServiceReference<?>> getServiceReferences(
		Class<S> clazz, String filter) throws InvalidSyntaxException
	{
		String classname = clazz.getCanonicalName();
		StringBuilder builder = new StringBuilder();
		
		if(filter != null)
		{
			builder.append("(&");
		}
		builder.append("(");
		builder.append("objectClass=");
		builder.append(classname);
		builder.append(")");
		
		if(filter != null)
		{
			if(!filter.startsWith("("))
			{
				builder.append("(");
			}
			builder.append(filter);
			if(!filter.endsWith(")"))
			{
				builder.append(")");
			}
			builder.append(")");
		}
		String completeFilter = builder.toString();
        
        List<ServiceReference<?>> referenceList = 
        		this.references.get(completeFilter);
        
        if(referenceList == null || referenceList.isEmpty())
        {
        	Collection<ServiceReference<S>> referenceCollection = 
        			 this.context.getServiceReferences(clazz,filter);
        	
        	referenceList = referenceCollection==null
        		?Collections.<ServiceReference<?>>emptyList()
        		:Collections.<ServiceReference<?>>unmodifiableList(
        		new ArrayList<ServiceReference<S>>(referenceCollection));
        	
        	this.references.put(classname, referenceList);	        	
        }
        return referenceList;
	}
	
    /**
     * @return
     */
    public int attach()
    {
    	return count.incrementAndGet();
    }

    /**
     * @return
     */
    public int release()
    {
    	return count.decrementAndGet();
    }

    /**
     * @throws IllegalStateException
     */
    private void checkContext() throws IllegalStateException
    {
        switch (this.context.getBundle().getState())
        {
            case Bundle.ACTIVE:
            case Bundle.STARTING:
            case Bundle.STOPPING:
            	break;
            default:throw new IllegalStateException();
        }
    }
    
    /**
     * @param serviceType
     * @param executor
     * @return
     * @throws Exception
     */
    public <S, R> R callService(Class<S> serviceType, 
    	Executable<S,R> executor) throws Exception
    {
        return this.callService(serviceType, null, executor);
    }

    /**
     * @param serviceType
     * @param filter
     * @param executor
     * @return
     * @throws Exception
     */
    public <S, R> R callService(Class<S> serviceType,
    	String filter, Executable<S,R> executor) throws Exception
    {
        R result = null; 
        S service = null;
        try
        {
            checkContext();
            
        } catch(IllegalStateException e)
        {
        	return result;
        }        
        List<ServiceReference<?>> serviceReferences = 
	        	this.getServiceReferences(serviceType, filter);
        
    	if(serviceReferences!= null && !serviceReferences.isEmpty())
    	{
    		Iterator<ServiceReference<?>> iterator = 
    				serviceReferences.iterator();
    		
    		while(iterator.hasNext())
    		{
    			ServiceReference<?> serviceReference = iterator.next();
    			if((service =  (S) this.context.getService(
    					serviceReference))!=null)
		    	{
    	    		try
    	    		{
    	    			result = executor.execute(service);	    	    			
    	    			break;
    	    			
    	    		}catch(Exception e)
    	    		{
    	    			throw e;
    	    			
    	    		} finally
    	    		{
		    			if(service != null)
		    			{
		    				this.context.ungetService(serviceReference);
		    			}
		    			service = null;
    	    		}
		    	}
    		}
    	}
    	return result;	    	
    }
    
    /**
     * @param serviceType
     * @param executor
     * @throws Exception
     */
    public <S> void callServices(Class<S> serviceType, 
    	Executable<S,Void> executor) throws Exception
    {
        this.callServices(serviceType, null, executor);
    }

    /**
     * @param serviceType
     * @param filter
     * @param executor
     * @throws Exception
     */
    public <S> void callServices(Class<S> serviceType, 
    		String filter, Executable<S,Void> executor) 
    				throws Exception
    {	        
        try
        {
            checkContext();
            
        } catch(IllegalStateException e)
        {
        	return;
        }     
        List<ServiceReference<?>> serviceReferences = 
	        	this.getServiceReferences(serviceType, filter);

    	if(serviceReferences!= null && !serviceReferences.isEmpty())
    	{
    		Iterator<ServiceReference<?>> iterator = serviceReferences.iterator();
    		S service = null;
    		
    		while(iterator.hasNext())
    		{
    			ServiceReference<?> serviceReference = iterator.next();
    			if((service =  (S) this.context.getService(
    					serviceReference))!=null)
		    	{
		    		try
		    		{
		    			executor.execute(service);
		    			
		    		}catch(Exception e)
		    		{
		    			continue;
		    			
		    		} finally
		    		{
		    			if(service != null)
		    			{
		    				this.context.ungetService(serviceReference);
		    			}
		    			service = null;
		    		}
		    	}
    		}
    	}
    }

    /**
     * @param serviceType
     * @param returnType
     * @param filter
     * @param executor
     * @return
     * @throws Exception
     */
    public <S, R> Collection<R> callServices(Class<S> serviceType, 
    	Class<R> returnType, String filter, Executable<S,R> executor) 
    			throws Exception
    {	
    	try
        {
            checkContext();
            
        } catch(IllegalStateException e)
        {
        	return Collections.<R>emptyList();
        }     
        Collection<R> collection = new ArrayList<R>();
        
        List<ServiceReference<?>> serviceReferences = 
	        	this.getServiceReferences(serviceType, filter);
    	
    	if(serviceReferences!= null && !serviceReferences.isEmpty())
    	{
    		Iterator<ServiceReference<?>> iterator = 
    				serviceReferences.iterator();
    		S service = null;
    		
    		while(iterator.hasNext())
    		{
    			ServiceReference<?> serviceReference = iterator.next();
    			if((service =  (S) this.context.getService(
    					serviceReference))!=null)
		    	{
		    		try
		    		{
		    			R result = executor.execute(service);
		    			collection.add(result);
		    			
		    		}catch(Exception e)
		    		{
		    			continue;
		    			
		    		} finally
		    		{
		    			if(service != null)
		    			{
		    				this.context.ungetService(serviceReference);
		    			}
		    			service = null;
		    		}
		    	}
    		}
    	}
    	return collection;
    }
}