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
package org.eclipse.sensinact.gateway.core.security.test;

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

import org.osgi.framework.Bundle;

final class FilterOSGiClassLoader extends ClassLoader {
	private BundleContextProvider contextProvider;
	Map<String, Set<String>> filteredEntries;

	private String loadingClass;
	private String loadingResource;

	public FilterOSGiClassLoader(BundleContextProvider contextProvider, URL[] urls) throws IOException {
		this(Thread.currentThread().getContextClassLoader(), contextProvider, urls);
	}

	public FilterOSGiClassLoader(ClassLoader parent, BundleContextProvider contextProvider, URL[] urls)
			throws IOException {
		super(parent);
		this.contextProvider = contextProvider;
		this.filteredEntries = new HashMap<String, Set<String>>();
		this.addFiltered(urls);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.ClassLoader#loadClass(java.lang.String)
	 */
	@Override
	public Class<?> loadClass(String clazz) throws ClassNotFoundException {
		return this.loadClass(clazz, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.ClassLoader#loadClass(java.lang.String)
	 */
	@Override
	public Class<?> loadClass(String classname, boolean resolve) throws ClassNotFoundException {
		String bundleName = isAFilteredClass(classname);

		if (bundleName != null) {
			// avoid loop
			if (this.loadingClass != null) {
				this.loadingClass = null;
				throw new ClassNotFoundException(classname);
			}
			Class<?> clazz = null;
			this.loadingClass = classname;

			if (Thread.currentThread().getContextClassLoader() == this) {
				Bundle[] bundles = this.contextProvider.getBundleContext().getBundles();
				int index = 0;
				int length = bundles == null ? 0 : bundles.length;
				for (; index < length; index++) {
					if (bundleName.equals(bundles[index].getSymbolicName())) {
						try {
							clazz = bundles[index].loadClass(classname);
						} catch (ClassNotFoundException e) {
						}
						break;
					}
				}
			}
			this.loadingClass = null;
			if (clazz != null) {
				return clazz;
			}
			throw new ClassNotFoundException(classname);
		}
		return super.loadClass(classname, resolve);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.ClassLoader#findResource(java.lang.String)
	 */
	public URL getResource(String name) {
		return findResource(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.ClassLoader#findResource(java.lang.String)
	 */
	protected URL findResource(String name) {
		String bundleName = isAFilteredResource(name);
		if (bundleName != null) {
			// avoid loop
			if (this.loadingResource != null) {
				this.loadingResource = null;
				return null;
			}
			this.loadingResource = name;

			if (Thread.currentThread().getContextClassLoader() == this) {
				Bundle[] bundles = this.contextProvider.getBundleContext().getBundles();
				int index = 0;
				int length = bundles == null ? 0 : bundles.length;
				for (; index < length; index++) {
					if (bundleName.equals(bundles[index].getSymbolicName())) {
						return bundles[index].getResource(name);
					}
				}
			}
			this.loadingResource = null;
			return null;
		}
		return super.findResource(name);
	}

	protected void addFiltered(URL[] urls) throws IOException {
		int index = 0;
		int length = urls == null ? 0 : urls.length;

		for (; index < length; index++) {
			this.addFiltered(urls[index]);
		}
	}

	protected void addFiltered(URL url) throws IOException {
		if (url == null) {
			return;
		}
		JarEntry jarEntry = null;
		JarInputStream jarInputStream = new JarInputStream(url.openStream());

		Manifest manifest = jarInputStream.getManifest();
		Attributes attributes = manifest.getMainAttributes();

		String bundleName = attributes.getValue("Bundle-SymbolicName");
		Set<String> entrySet = new HashSet<String>();

		while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
			if (jarEntry.isDirectory()) {
				continue;
			}
			String excluded = jarEntry.getName();
			entrySet.add(excluded);
		}
		this.filteredEntries.put(bundleName, entrySet);
	}

	protected String isAFilteredClass(String clazzname) {
		String classname = clazzname.replace('.', '/');
		classname = classname + ".class";
		Iterator<String> iterator = this.filteredEntries.keySet().iterator();
		while (iterator.hasNext()) {
			String bundleName = iterator.next();
			if (this.filteredEntries.get(bundleName).contains(classname)) {
				return bundleName;
			}
		}
		return null;
	}

	protected String isAFilteredResource(String resource) {
		String searchResource = resource;
		if (searchResource.startsWith("/")) {
			searchResource.substring(1);
		}
		Iterator<String> iterator = this.filteredEntries.keySet().iterator();
		while (iterator.hasNext()) {
			String bundleName = iterator.next();
			if (this.filteredEntries.get(bundleName).contains(searchResource)) {
				return bundleName;
			}
		}
		return null;
	}
}