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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class MidOSGiTest implements BundleContextProvider
{

	protected static final String HTTP_ROOTURL = "http://localhost:8091/sensinact";
	protected static final String WS_ROOTURL = "/sensinact";
	
	private static final String AUTO_PROCESSOR = "org.apache.felix.main.AutoProcessor";
	private static final String FELIX_FRAMEWORK = "org.osgi.framework.launch.Framework";
	private static final String FELIX_FRAMEWORK_FACTORY = "org.apache.felix.framework.FrameworkFactory";

	private static final String BUNDLE = "org.osgi.framework.Bundle";

	private static final String BUNDLE_GET_CONTEXT = "getBundleContext";
	private static final String BUNDLE_STATE = "getState";

	private static final String FRAMEWORK_INIT = "init";
	private static final String FRAMEWORK_START = "start";
	private static final String FRAMEWORK_STOP = "stop";

	private static final String FRAMEWORK_FACTORY_INIT_FRAMEWORK = "newFramework";
	private static final Class<?>[] FRAMEWORK_FACTORY_INIT_FRAMEWORK_TYPES = 
			new Class<?>[] { Map.class };

    protected final FilterOSGiClassLoader classloader;
    private final ClassLoader current;
	private Object felix;
	private Class<?> frameworkClass;
	BundleContext context;

	public MidOSGiTest() throws MalformedURLException, IOException
	{
		String directoryName = "./target/felix/load";
		File theDir = new File(directoryName);

		// if the directory does not exist, create it
		if (!theDir.exists())
		{
		    //System.out.println("creating directory: " + directoryName);
		    try
		    {
		        theDir.mkdir();
		    } 
		    catch(SecurityException se){}
		}
		System.setProperty("java.security.policy", "src/test/resources/all.policy");
		
		File manifestFile = new File("./src/test/resources/MANIFEST.MF");		
		this.createDynamicBundle(manifestFile, theDir, new File[]{
				new File("./target/classes"), new File("./src/main/resources")
		});

		this.current = Thread.currentThread().getContextClassLoader();

		this.classloader = new FilterOSGiClassLoader(current, this, new URL[] {
				new URL("file:target/felix/bundle/sensinact-felix-security.jar"),
				new URL("file:target/felix/bundle/sensinact-utils.jar"),
				new URL("file:target/felix/bundle/sensinact-datastore-api.jar"),
				new URL("file:target/felix/bundle/sensinact-core.jar"),
				new URL("file:target/felix/bundle/sensinact-generic.jar"), 
				new URL("file:target/felix/bundle/http.jar"),
				new URL("file:target/felix/bundle/org.apache.felix.fileinstall.jar"),
				new URL("file:target/felix/bundle/org.osgi.compendium.jar"),
				new URL("file:target/felix/bundle/org.apache.felix.configadmin.jar"),
				new URL("file:target/felix/bundle/sensinact-northbound-access.jar"),
				new URL("file:target/felix/bundle/javax.servlet-api.jar"),
				new URL("file:target/felix/bundle/org.apache.felix.http.api.jar"),
				new URL("file:target/felix/bundle/org.apache.felix.http.jetty.jar"),
				new URL("file:target/felix/bundle/slider.jar"),
				new URL("file:target/felix/bundle/light.jar"),
				new URL("file:target/felix/load/dynamicBundle.jar")});
	}

	/**
	 * Creates a new <code>JarInputStream</code> and reads the optional
	 * manifest. If a manifest is present and verify is true, also attempts
	 * to verify the signatures if the JarInputStream is signed.
	 *
	 * @param in
	 *            the actual input stream
	 * @param verify
	 *            whether or not to verify the JarInputStream if it is
	 *            signed.
	 * @exception IOException
	 *                if an I/O error has occurred
	 */
	public void createDynamicBundle(
			File manifestFile,
			File destDirectory, 
			File... sourceDirectories) 
			throws IOException
	{
		if(!destDirectory.exists())
		{
			return;
		}		
		//Assume all necessary imports and variables are declared
        FileOutputStream fOut = new FileOutputStream(
        		new File(destDirectory, "dynamicBundle.jar"));

        Manifest manifest = new Manifest(
        		new FileInputStream(manifestFile));
        
        // Create a buffer for reading the files
        byte[] buf = new byte[1024];
        
        JarOutputStream jarOut = new JarOutputStream(fOut,manifest);
        FileInputStream reader = null;
        int index = 0;
        int length = sourceDirectories==null?0:sourceDirectories.length;
        
        for(;index < length; index++)
        {
        	File sourceDirectory = sourceDirectories[index];
        	if(!sourceDirectory.isDirectory())
        	{
        		continue;
        	}
	        FilesEnumerator enumerator = new FilesEnumerator(sourceDirectory);
	        while(enumerator.hasMoreElements())
	        {
	        	File file = enumerator.nextElement();
	            if (file.isFile())
	            {
	            	jarOut.putNextEntry(new ZipEntry(
	            	file.getAbsolutePath().substring(
	            	sourceDirectory.getAbsolutePath().length()+1)));
	        	
	            	reader = new FileInputStream(file);
	                int len;
	                while ((len = reader.read(buf)) > 0) 
	                {
	                	jarOut.write(buf, 0, len);
	                }
	                // Complete the entry
	                reader.close();
	                jarOut.closeEntry();
	            }
	        }
        }
        jarOut.close();
	}

	/** 
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.nthbnd.rest.BundleContextProvider#getBundleContext()
	 */
	@Override
	public BundleContext getBundleContext()
	{
		return this.context;
	}
	
	@After
	public void tearDown() throws Exception
	{
		frameworkClass.getDeclaredMethod(FRAMEWORK_STOP).invoke(felix);
		felix = null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes", "serial" })
	@Before
	public void init() throws Exception 
	{
		Thread.currentThread().setContextClassLoader(classloader);
		
		Map configuration = new HashMap();
		configuration.put("felix.cache.rootdir", "./target/felix");
		configuration.put("org.osgi.framework.storage", "felix-cache");
		configuration.put("org.osgi.framework.bootdelegation", "*");
		configuration.put("org.osgi.framework.system.packages.extra",
						"org.eclipse.sensinact.gateway.core.security.condperm,"
						+ "com.sun.net.httpserver," 
						+ "javax.net.ssl,"
						+ "javax.xml.parsers," 
						+ "javax.imageio," 
						+ "javax.management," 
						+ "javax.naming," 
						+ "javax.sql,"
						+ "javax.swing," 
						+ "javax.swing.border," 
						+ "javax.swing.event," 
						+ "javax.management.modelmbean,"
						+ "javax.management.remote," 
						+ "javax.security.auth," 
						+ "javax.security.cert,"
						+ "org.w3c.dom,"
						+ "org.xml.sax," 
						+ "org.xml.sax.helpers," 
						+ "sun.misc,"
						+ "sun.security.action");
		configuration.put("org.osgi.framework.storage.clean","onFirstInit");
		configuration.put("felix.cache.rootdir", "target/felix");
		configuration.put("felix.auto.deploy.action", "install,start");
		configuration.put("felix.log.level", "4");
		configuration.put("felix.fileinstall.dir", "./load");
		configuration.put("felix.fileinstall.bundles.new.start", "true");
		if (System.getSecurityManager() == null)
		{
			configuration.put("org.osgi.framework.security", "osgi");
		}
		configuration.put("felix.auto.start.1",
						  "file:target/felix/bundle/org.apache.felix.configadmin.jar "
						+ "file:target/felix/bundle/org.osgi.compendium.jar "
						+ "file:target/felix/bundle/sensinact-felix-security.jar "
						+ "file:target/felix/bundle/sensinact-utils.jar "
						+ "file:target/felix/bundle/sensinact-datastore-api.jar "
						+ "file:target/felix/bundle/org.apache.felix.fileinstall.jar ");
		configuration.put("felix.auto.start.2", "file:target/felix/bundle/sensinact-core.jar " 
						+ "file:target/felix/bundle/sensinact-generic.jar");
		configuration.put("felix.auto.start.3", 
				"file:target/felix/bundle/javax.servlet-api.jar "
				+ "file:target/felix/bundle/org.apache.felix.http.api.jar "
				+ "file:target/felix/bundle/org.apache.felix.http.jetty.jar " 
				+ "file:target/felix/bundle/http.jar "
				+ "file:target/felix/bundle/sensinact-northbound-access.jar");
		configuration.put("felix.auto.start.4", 
				"file:target/felix/bundle/slider.jar "
				+ "file:target/felix/bundle/light.jar ");
		configuration.put("org.osgi.framework.startlevel.beginning", "5");
		configuration.put("felix.startlevel.bundle", "5");
		configuration.put("org.osgi.service.http.port", "8091");
		configuration.put("org.apache.felix.http.jettyEnabled", "true");
		configuration.put("org.apache.felix.http.whiteboardEnabled", "true");
		configuration.put("org.eclipse.sensinact.simulated.gui.enabled", "false");
		configuration.put("felix.bootdelegation.classloaders", new HashMap() 
		{
			public Object get(Object key)
			{
				if (Bundle.class.isAssignableFrom(key.getClass()))
				{
					return classloader;
				}
				return super.get(key);
			}
		});
		
		Class<?> factoryClass = classloader.loadClass(FELIX_FRAMEWORK_FACTORY);
		Object factory = factoryClass.newInstance();

		felix = factoryClass.getDeclaredMethod(
				FRAMEWORK_FACTORY_INIT_FRAMEWORK, 
				FRAMEWORK_FACTORY_INIT_FRAMEWORK_TYPES)
				.invoke(factory, new Object[] { configuration });

		frameworkClass = classloader.loadClass(FELIX_FRAMEWORK);
		Class<?> bundleClass = classloader.loadClass(BUNDLE);

		frameworkClass.getDeclaredMethod(FRAMEWORK_INIT).invoke(felix);
		context = (BundleContext) bundleClass.getDeclaredMethod(BUNDLE_GET_CONTEXT
				).invoke(felix);

		Class<?> autoProcessorClass = classloader.loadClass(AUTO_PROCESSOR);

		autoProcessorClass.getDeclaredMethod("process", 
				new Class<?>[] { Map.class, BundleContext.class }).invoke(
						null, new Object[] { configuration, context });

		frameworkClass.getDeclaredMethod(FRAMEWORK_START).invoke(felix);

		Assert.assertTrue(bundleClass == Bundle.class);
		Assert.assertTrue(((Integer) bundleClass.getDeclaredMethod(BUNDLE_STATE
				).invoke(felix)) == Bundle.ACTIVE);

		Object httpService = context.getService(context.getServiceReferences(
						"org.apache.felix.http.api.ExtHttpService", null)[0]);

		Assert.assertTrue(httpService != null);
		
		Bundle bundle = context.installBundle("file:" + new File(
				"target/felix/load/dynamicBundle.jar").getAbsolutePath());
		bundle.start();

		Thread.currentThread().setContextClassLoader(current);
	}
	
}
