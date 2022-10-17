/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
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

public class FilterOSGiClassLoader extends ClassLoader {
    protected BundleContextProvider contextProvider;
    protected Map<String, Set<String>> filteredEntries;

    protected String loadingClass;
    protected String loadingResource;

    public FilterOSGiClassLoader(ClassLoader parent, BundleContextProvider contextProvider, URL[] filtered) throws IOException {
        super(parent);
        this.contextProvider = contextProvider;
        this.filteredEntries = new HashMap<String, Set<String>>();
        this.addFiltered(filtered);
    }

    public FilterOSGiClassLoader(ClassLoader parent, BundleContextProvider contextProvider, FilterOSGiClassLoader loader) throws IOException {
        super(parent);
        this.contextProvider = contextProvider;
        this.filteredEntries = new HashMap<String, Set<String>>();
        this.addFiltereds(loader);
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
        //System.out.println(classname);
        Class<?> clazz = null;
        String bundleName = isAFilteredClass(classname);
        if (bundleName != null) {
            return null;
        }
        if (classname.startsWith("[")) {
            clazz = Class.forName(classname);
        } else {
            clazz = super.loadClass(classname, resolve);
        }
        return clazz;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.ClassLoader#findResource(java.lang.String)
     */
    public URL getResource(String name) {
        URL resource = findResource(name);
        return resource;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.ClassLoader#findResource(java.lang.String)
     */
    public URL findResource(final String name) {
        String bundleName = isAFilteredResource(name);
        if (bundleName != null) {
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

    public void addFiltered(URL url) throws IOException {
        if (url == null) {
            return;
        }
        JarEntry jarEntry = null;
        JarInputStream jarInputStream = new JarInputStream(url.openStream());

        Manifest manifest = jarInputStream.getManifest();
        if(manifest == null) {
        	return;
        }
        Attributes attributes = manifest.getMainAttributes();

        String bundleName = attributes.getValue("Bundle-SymbolicName");
        Set<String> entrySet = new HashSet<String>();
        while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
            if (jarEntry.isDirectory()) {
                continue;
            }
            String excluded = jarEntry.getName();
            entrySet.add(excluded.replace('\\', '/'));
        }
        this.filteredEntries.put(bundleName, entrySet);
    }

    public void addFiltereds(FilterOSGiClassLoader filterClassLoader) throws IOException {
        this.filteredEntries.putAll(filterClassLoader.filteredEntries);
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
        String searchResource = resource.replace('\\', '/');
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