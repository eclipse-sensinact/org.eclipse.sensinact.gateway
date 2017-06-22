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

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

public final class FilterOSGiClassLoader extends ClassLoader
{
	private BundleContextProvider contextProvider;
	Map<String, Set<String>> filteredEntries;
	
	private String loadingClass;
	private String loadingResource;

	public FilterOSGiClassLoader(ClassLoader parent, 
			BundleContextProvider contextProvider, URL[] filtered) 
			throws IOException
	{
		super(parent);
		this.contextProvider = contextProvider;
		this.filteredEntries = new HashMap<String,Set<String>>();
		this.addFiltered(filtered);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.ClassLoader#loadClass(java.lang.String)
	 */
	@Override
	public Class<?> loadClass(String clazz) 
			throws ClassNotFoundException 
	{
		return this.loadClass(clazz, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.ClassLoader#loadClass(java.lang.String)
	 */
	@Override
	public Class<?> loadClass(String classname, boolean resolve) 
			throws ClassNotFoundException
	{
		//System.out.println(classname);
		Class<?> clazz = null;
		String bundleName = isAFilteredClass(classname);

		if ( bundleName != null)
		{
			//avoid loop
//			if(this.loadingClass != null)
//			{	
//				this.loadingClass = null;
//				throw new ClassNotFoundException(classname);
//			}	
//			this.loadingClass = classname;				
//			Bundle[] bundles  = this.contextProvider.getBundleContext().getBundles();
//			
//			int index = 0;
//			int length = bundles == null?0:bundles.length;
//			for(;index < length; index++)
//			{
//				final Bundle bundle = bundles[index];
//				if(bundleName.equals(bundle.getSymbolicName()))
//				{
//					try
//					{
//						ClassLoader loader = AccessController.doPrivileged(
//					    new PrivilegedAction<ClassLoader>()
//					    {
//					    	public ClassLoader run()
//					    	{
//					    		BundleWiring wiring = null;
//					    		//System.out.println(bundle.getSymbolicName());
//					    		//System.out.println((
//					    		wiring = bundle.adapt(BundleWiring.class);
//					    		//));
//					    		if(wiring != null)
//					    		{
//					    			return wiring.getClassLoader();
//					    		}
//					    		return FilterOSGiClassLoader.this.getParent();
//					    	}
//					    });
//						return loader.loadClass(classname);
//						
//					} finally
//					{
//						this.loadingClass = null;
//					}
//				}
//			}
			return null;
		}
		if(classname.startsWith("["))
		{
			clazz = Class.forName(classname);
		} else
		{	
			clazz = super.loadClass(classname, resolve);
		}
		return clazz;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.ClassLoader#findResource(java.lang.String)
	 */
	public URL getResource(String name) 
	{
		URL resource = findResource(name);
		return resource;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.ClassLoader#findResource(java.lang.String)
	 */
	public URL findResource(final String name) 
	{
		String bundleName = isAFilteredResource(name);
		if ( bundleName != null)
		{
			//avoid loop
//			if(this.loadingResource != null)
//			{
//				this.loadingResource = null;
//				return null;
//			}
//			this.loadingResource = name;
//			Bundle[] bundles  = this.contextProvider.getBundleContext().getBundles();
//			
//			int index = 0;
//			int length = bundles == null?0:bundles.length;
//			for(;index < length; index++)
//			{
//				final Bundle bundle = bundles[index];
//				if(bundleName.equals(bundle.getSymbolicName()))
//				{
//					URL url =  AccessController.doPrivileged(
//					    		new PrivilegedAction<URL>()
//					    {
//					    	public URL run()
//					    	{
//								return bundle.getResource(name);
//					    	}
//					    });
//				
//					return url;
//				}
//			}
//			this.loadingResource = null;
			return null;
		}
		return super.findResource(name);
	}

	protected void addFiltered(URL[] urls) throws IOException 
	{
		int index = 0;
		int length = urls == null ? 0 : urls.length;

		for (; index < length; index++) {
			this.addFiltered(urls[index]);
		}
	}

	public void addFiltered(URL url) throws IOException 
	{
		if (url == null) 
		{
			return;
		}
		JarEntry jarEntry = null;
		JarInputStream jarInputStream = 
				new JarInputStream(url.openStream());
		
		Manifest manifest = jarInputStream.getManifest();
		Attributes attributes = manifest.getMainAttributes();
		
		String bundleName = attributes.getValue("Bundle-SymbolicName");			
		Set<String> entrySet = new HashSet<String>();

		while ((jarEntry = jarInputStream.getNextJarEntry()) != null) 
		{
			if (jarEntry.isDirectory()) 
			{
				continue;
			}
			String excluded = jarEntry.getName();			
			entrySet.add(excluded.replace('\\', '/'));
		}
		this.filteredEntries.put(bundleName, entrySet);
	}

	protected String isAFilteredClass(String clazzname)
	{
		String classname = clazzname.replace('.', '/');
		classname = classname + ".class";
		Iterator<String> iterator = this.filteredEntries.keySet().iterator();
		while(iterator.hasNext())
		{
			String bundleName = iterator.next();
			if(this.filteredEntries.get(bundleName
					).contains(classname))
			{
				return bundleName;
			}
		}
		return null;
	}

	protected String isAFilteredResource(String resource)
	{
		String searchResource = resource.replace('\\','/');
		if(searchResource.startsWith("/"))
		{
			searchResource.substring(1);
		}
		Iterator<String> iterator = this.filteredEntries.keySet().iterator();
		while(iterator.hasNext())
		{
			String bundleName = iterator.next();
			if(this.filteredEntries.get(bundleName
					).contains(searchResource))
			{
				return bundleName;
			}
		}
		return null;
	}
}