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
package org.eclipse.sensinact.gateway.test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.wiring.BundleWiring;

public class MidProxy<T> implements InvocationHandler 
{
	private BundleContextProvider contextProvider;
	private Class<?> serviceType;
	
	private Object contextualizedInstance;
	private FilterOSGiClassLoader classloader;

	public MidProxy(
			FilterOSGiClassLoader classloader, 
			BundleContextProvider contextProvider, 
			Class<T> serviceType)
	{
		this.contextProvider = contextProvider;	
		this.classloader = classloader;
		this.serviceType = serviceType;
		if(this.serviceType == null)
		{
			throw new NullPointerException("Proxy type needed");
		}
	}
	
	@SuppressWarnings("unchecked")
	public T buildProxy() throws ClassNotFoundException
	{
		String classname = this.serviceType.getCanonicalName();
		Class<?> contextualizedClazz = this.loadClass(classname);
		
		ServiceReference reference = null;
		
		if(contextualizedClazz != null && (reference  =
			this.contextProvider.getBundleContext().getServiceReference(
				contextualizedClazz))!=null && (this.contextualizedInstance = 
					this.contextProvider.getBundleContext(
					).getService(reference))!=null)
		{
			return (T) Proxy.newProxyInstance( 
				Thread.currentThread().getContextClassLoader(),  
				new Class<?>[]{serviceType}, this);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public T buildProxy(Object contextualizedInstance) 
			throws ClassNotFoundException
	{
		Class<?>[] serviceTypes = null;
		
		String classname = this.serviceType.getCanonicalName();			
		Class<?> contextualizedClazz = this.loadClass(classname);
		
		if(contextualizedClazz == null ||
				!contextualizedClazz.isAssignableFrom(
						contextualizedInstance.getClass()))
		{
			return null;
		}
		serviceTypes = new Class<?>[]{serviceType};
		
		this.contextualizedInstance = contextualizedInstance;
		return (T) Proxy.newProxyInstance( 
				Thread.currentThread().getContextClassLoader(),  
				serviceTypes, this);
	}

	/**
	 * @param instanceType
	 * @param parameterTypes
	 * @param objects
	 * @return
	 * @throws ClassNotFoundException 
	 */
	public <C extends T> T buildProxy(String instanceType,
			Class<?>[] parameterTypes, Object[] objects) 
			throws ClassNotFoundException
	{
		String classname = this.serviceType.getCanonicalName();
		
		Class<?> contextualizedClazz = this.loadClass(classname);
		Class<?> contextualizedImplementation = this.loadClass(
				instanceType);
		
		if(contextualizedImplementation!= null && contextualizedClazz != null
				&& contextualizedClazz.isAssignableFrom(
						contextualizedImplementation))
		{
			try
			{
				this.contextualizedInstance = 
						contextualizedImplementation.getDeclaredConstructor(
						parameterTypes).newInstance(objects);
				
				return (T) Proxy.newProxyInstance( 
				Thread.currentThread().getContextClassLoader(),  
				new Class<?>[]{serviceType}, this);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return null;
	}

	private Class<?> loadClass(String classname) throws ClassNotFoundException
	{
		Class<?> contextualizedClazz = null;		
		String bundleName = classloader.isAFilteredClass(classname);

		if(bundleName != null 
				&& Thread.currentThread().getContextClassLoader()
				!=classloader)
		{
			Bundle[] bundles  = this.contextProvider.getBundleContext(
					).getBundles();
			int index = 0;
			int length = bundles == null?0:bundles.length;
			for(;index < length; index++)
			{
				if(bundleName.equals(bundles[index].getSymbolicName()))
				{
					try
					{
						BundleWiring wiring = bundles[index].adapt(
								BundleWiring.class);
						
						contextualizedClazz = wiring.getClassLoader().loadClass(
								classname);
						
					} catch(ClassNotFoundException e) {}
					break;
				}
			}
		} else
		{
			contextualizedClazz = classloader.loadClass(classname);
		}
		return contextualizedClazz;
	}
	
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) 
			throws Throwable
	{
		String methodName = method.getName();
		Class<?> clazz = method.getDeclaringClass();
		Class<?> returnedType = method.getReturnType();
		
		Class<?>[] parameterTypes = method.getParameterTypes();
		Class<?>[] contextualizedParameterTypes =
				new Class<?>[parameterTypes.length];
		
		int index = 0;
		int length = parameterTypes.length;
		
		for(;index < length; index++)
		{
			if(parameterTypes[index].isPrimitive())
			{
				contextualizedParameterTypes[index] = parameterTypes[index];
				
			} else
			{
				contextualizedParameterTypes[index] = 
				this.loadClass(parameterTypes[index].isArray()?
				"[L"+parameterTypes[index].getComponentType().getCanonicalName()+";":
					parameterTypes[index].getCanonicalName());
			}
			//TODO:handle MidProxy object parameters
			if(contextualizedParameterTypes[index] == null ||
				(contextualizedParameterTypes[index] != parameterTypes[index]
					&& !parameterTypes[index].isAssignableFrom(
							args[index].getClass())))
			{
				throw new IllegalArgumentException("Invalid parameter Types ");
			}
		}		
		Class<?> contextualizedClazz = this.loadClass(clazz.getCanonicalName());
		
		Method contextualizedMethod = contextualizedClazz.getMethod(
				methodName, contextualizedParameterTypes);
		
		Object result = null;
		try 
		{
			if(args == null)
			{
			  result = contextualizedMethod.invoke(
				this.contextualizedInstance);
			  
			} else
			{
			  result = contextualizedMethod.invoke(
					this.contextualizedInstance,args);
			}
		} catch (InvocationTargetException ite)
		{
			  throw ite.getCause();
		}		
		if(result != null)
		{		
			Class<?> contextualizedReturnedType = null;	
			try
			{
				contextualizedReturnedType = this.loadClass(
				returnedType.isArray()?
				"[L"+returnedType.getComponentType().getCanonicalName()+";":
				returnedType.getCanonicalName());
				
				if(contextualizedReturnedType != returnedType 
						&& returnedType.isInterface())
				{
					MidProxy returnedProxy = new MidProxy(classloader, 
						contextProvider, returnedType);
					result = returnedProxy.buildProxy(result);
				}
			} catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public Object invoke(Method method, Class<?> returnedType, Object[] args) 
			throws Throwable
	{
		if(this.contextualizedInstance == null)
		{
			return null;
		}
		Class<?> clazz = method.getDeclaringClass();
		
		Class<?>[] parameterTypes = method.getParameterTypes();
		Class<?>[] contextualizedParameterTypes =
				new Class<?>[parameterTypes.length];
		
		int index = 0;
		int length = parameterTypes.length;
		
		for(;index < length; index++)
		{
			contextualizedParameterTypes[index] = 
					this.loadClass(parameterTypes[index].isArray()?
					"[L"+parameterTypes[index].getComponentType().getCanonicalName()+";":
						parameterTypes[index].getCanonicalName());
			
			//TODO:handle MidProxy object parameters
			if(contextualizedParameterTypes[index] == null || 
					contextualizedParameterTypes[index] != parameterTypes[index])
			{
				throw new IllegalArgumentException("Invalid parameter Types ");
			}
		}		
		Class<?> contextualizedClazz = this.loadClass(
				clazz.getCanonicalName());
		
		Method contextualizedMethod = contextualizedClazz.getMethod(
				method.getName(),contextualizedParameterTypes);
		
		Object result = null;
		try 
		{
			if(args == null)
			{
			  result = contextualizedMethod.invoke(
				this.contextualizedInstance);
			  
			} else
			{
			  result = contextualizedMethod.invoke(
					this.contextualizedInstance,args);
			}
			  
		} catch (InvocationTargetException ite)
		{
			  throw ite.getCause();
		}
		if(result != null)
		{		
			Class<?> contextualizedReturnedType = null;	
			try
			{
				contextualizedReturnedType = this.loadClass(
				returnedType.isArray()?
				"[L"+returnedType.getComponentType().getCanonicalName()+";":
				returnedType.getCanonicalName());
				
				if(contextualizedReturnedType != returnedType 
						&& returnedType.isInterface())
				{
					MidProxy returnedProxy = new MidProxy(classloader, 
						contextProvider, returnedType);
					result = returnedProxy.buildProxy(result);
				}
			} catch(Exception e)
			{}
		}
		return result;
	}

	/**
	 * @return
	 */
	public Object getContextualizedInstance()
	{
		return this.contextualizedInstance;
	}

	/**
	 * @return
	 */
	public Class<?> getContextualizedType()
	{
		String classname = this.serviceType.getCanonicalName();
		Class<?> contextualizedClazz = null;
		try
		{
			contextualizedClazz = this.loadClass(classname);
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		return contextualizedClazz;
	}
}
