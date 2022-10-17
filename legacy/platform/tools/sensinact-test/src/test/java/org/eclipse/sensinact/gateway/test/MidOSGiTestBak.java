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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import org.eclipse.sensinact.gateway.util.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public abstract class MidOSGiTestBak implements BundleContextProvider {
    /**
     * Completes the initialization configuration Map
     *
     * @param configuration the initialization
     *                      configuration Map to be completed
     */
    protected abstract void doInit(Map<String, Serializable> configuration);

    /**
     * Returns true if the file whose name is passed as
     * parameter is excluded from this MidOSGiTest's classpath;
     * returns false otherwise
     *
     * @param name the name of the file to exclude or not
     * @return true if the specified file is excluded from
     * this MidOSGiTest's classpath; false otherwise
     */
    protected abstract boolean isExcluded(String name);

    static {
        try {
            java.security.Policy.setPolicy(new TestPolicy());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected static final String AUTO_PROCESSOR = "org.apache.felix.main.AutoProcessor";
    protected static final String FELIX_FRAMEWORK = "org.osgi.framework.launch.Framework";
    protected static final String FELIX_FRAMEWORK_FACTORY = "org.apache.felix.framework.FrameworkFactory";
    protected static final String BUNDLE = "org.osgi.framework.Bundle";
    protected static final String BUNDLE_GET_CONTEXT = "getBundleContext";
    protected static final String BUNDLE_STATE = "getState";
    protected static final String FRAMEWORK_INIT = "init";
    protected static final String FRAMEWORK_START = "start";
    protected static final String FRAMEWORK_STOP = "stop";
    protected static final String FRAMEWORK_FACTORY_INIT_FRAMEWORK = "newFramework";
    protected static final Class<?>[] FRAMEWORK_FACTORY_INIT_FRAMEWORK_TYPES = new Class<?>[]{Map.class};
    protected static final String FRAMEWORK_WAIT_FOR_STOP = "waitForStop";
    protected static final Class<?>[] FRAMEWORK_WAIT_FOR_STOP_TYPES = new Class<?>[]{long.class};

    protected static final long WAIT_FOR_STOP_TIMEOUT = 60000;

    protected final FilterOSGiClassLoader classloader;
    protected final ClassLoader current;
    protected Object felix;
    protected Class<?> frameworkClass;
    protected BundleContext context;
    protected String policy = null;

    protected File felixDir = null;
    protected File cacheDir = null;
    protected File bundleDir = null;
    protected File loadDir = null;

    public MidOSGiTestBak() throws Exception {
        String directoryName = "target/felix";
        felixDir = new File(directoryName);
        bundleDir = new File(felixDir, "bundle");
        if (!bundleDir.exists()) {
            bundleDir.mkdir();
        }
        loadDir = new File(felixDir, "load");
        if (!loadDir.exists()) {
            loadDir.mkdir();
        }
        cacheDir = new File(felixDir, "felix-cache");
        if (!cacheDir.exists()) {
            cacheDir.mkdir();
        }
        File confDir = new File(felixDir, "conf");
        if (!confDir.exists()) {
            confDir.mkdir();
        }
        this.current = Thread.currentThread().getContextClassLoader();
        FilesEnumerator enumerator = new FilesEnumerator(bundleDir);

        List<URL> urls = new ArrayList<URL>();

        while (enumerator.hasMoreElements()) {
            File file = enumerator.nextElement();
            if (file.getName().endsWith(".jar") && !this.isExcluded(file.getName())) {
                urls.add(file.toURI().toURL());
            }
        }
        this.classloader = new FilterOSGiClassLoader(current, this, urls.toArray(new URL[0]));
    }

    /**
     * Creates a new <code>JarInputStream</code> and reads the optional
     * manifest. If a manifest is present and verify is true, also attempts
     * to verify the signatures if the JarInputStream is signed.
     *
     * @param manifestFile
     * @param destDirectory
     * @param sourceDirectories
     * @throws IOException if an I/O error has occurred
     */
    public void createDynamicBundle(File manifestFile, File destDirectory, File... sourceDirectories) throws IOException {
        if (!destDirectory.exists()) {
            return;
        }
        File dynamicBundleFile = new File(destDirectory, "dynamicBundle.jar");
    	//Assume all necessary imports and variables are declared
        FileOutputStream fOut = new FileOutputStream(dynamicBundleFile);
        Manifest manifest = null;
        try {
            manifest = new Manifest(new FileInputStream(manifestFile));
        } catch(FileNotFoundException e) {
        	e.printStackTrace();
        	try {
        		fOut.close();
        	}catch(IOException ex) {}
        	return;
        }    
    	// Create a buffer for reading the files
        byte[] buf = new byte[1024];

		try (JarOutputStream jarOut = new JarOutputStream(fOut, manifest);) {
			FileInputStream reader = null;
			int index = 0;
			int length = sourceDirectories == null ? 0 : sourceDirectories.length;

			for (; index < length; index++) {
				File sourceDirectory = sourceDirectories[index];
				FilesEnumerator enumerator = new FilesEnumerator(sourceDirectory);
				while (enumerator.hasMoreElements()) {
					File file = enumerator.nextElement();
					if (file.isFile()) {
						String entryName = null;
						if (sourceDirectory.isDirectory()) {
							entryName = file.getAbsolutePath()
									.substring(sourceDirectory.getAbsolutePath().length() + 1);

						} else {
							entryName = file.getName();
						}
						entryName = entryName.replace(File.separatorChar, '/');
						jarOut.putNextEntry(new ZipEntry(entryName));

						reader = new FileInputStream(file);
						int len;
						while ((len = reader.read(buf)) > 0) {
							jarOut.write(buf, 0, len);
						}
						// Complete the entry
						reader.close();
						jarOut.closeEntry();
					}
				}
			}
			jarOut.close();
			this.classloader.addFiltered(new File(destDirectory, "dynamicBundle.jar").toURI().toURL());
		}
    }

    /**
     * @param url
     */
    public Bundle installDynamicBundle(final URL url) {
        try {
        	int length = loadDir.listFiles().length;
        	String fileName = String.format("test%s.jar",length);
            File testFile = new File(loadDir, fileName);
            URL testFileURL = testFile.toURI().toURL();
            FileOutputStream output = new FileOutputStream(new File(loadDir,fileName));
            byte[] testJar = IOUtils.read(url.openStream(), true);
            IOUtils.write(testJar, output);
            this.classloader.addFiltered(testFileURL);
            return getBundleContext().installBundle(testFileURL.toExternalForm());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @inheritDoc
     * @see BundleContextProvider#getBundleContext()
     */
    @Override
    public BundleContext getBundleContext() {
        return this.context;
    }

    /**
     * @throws Exception
     */
    @AfterAll
    public void tearDown() throws Exception {
        long start = System.currentTimeMillis();
        frameworkClass.getDeclaredMethod(FRAMEWORK_STOP).invoke(felix);
        frameworkClass.getDeclaredMethod(FRAMEWORK_WAIT_FOR_STOP, FRAMEWORK_WAIT_FOR_STOP_TYPES).invoke(felix, new Object[]{WAIT_FOR_STOP_TIMEOUT});
        felix = null;
    	File prefix = new File("target/felix");
    	Stack<String> stack = new Stack<String>();
    	for(String s:prefix.list()) {
    		if(s.equals("bundle") || s.equals("conf")) {
    			continue;
    		}
    		stack.push(s);
    	}
    	while(true) {
    		String current = stack.peek();
    		File f = new File(prefix,current);
    		String[] list = f.list();
    		if(list!=null && list.length>0) {
	        	for(String s:list) {
	        		stack.push(current + "/" + s);
	        	}
    		} else {
    			f.delete();
    			stack.pop();
    		}
    		if(stack.isEmpty()){
    			break;
    		}	        	
    	}
    	new File(prefix,"load").mkdir();
    	System.out.println("Felix stopped [" + (System.currentTimeMillis() - start) + "]");
    	//let the environment finalize
        Thread.sleep(5000);
    }

    /**
     * @throws Exception
     */
    @SuppressWarnings({"unchecked", "rawtypes", "serial"})
    @BeforeAll
    public void init() throws Exception {
        final Map configuration = new HashMap();
        if (System.getSecurityManager() == null) {
            configuration.put("org.osgi.framework.security", "osgi");
        }
        configuration.put("felix.cache.rootdir", felixDir.getPath());
        configuration.put("org.osgi.framework.storage", "felix-cache");
        configuration.put("org.osgi.framework.bootdelegation", "*");
        configuration.put("org.osgi.framework.system.packages.extra", "org.eclipse.sensinact.gateway.test,org.slf4j," + "com.sun.net.httpserver," + "javax.activation," + "javax.net.ssl," + "javax.xml.parsers," + "javax.imageio," + "javax.management," + "javax.naming," + "javax.sql," + "javax.swing," + "javax.swing.border," + "javax.swing.event," + "javax.management.modelmbean," + "javax.management.remote," + "javax.security.auth," + "javax.security.cert," + "org.w3c.dom," + "org.xml.sax," + "org.xml.sax.helpers," + "sun.misc," + "javax.mail," + "javax.mail.internet," + "sun.security.action");
        configuration.put("org.osgi.framework.storage.clean", "onFirstInit");
        configuration.put("felix.auto.deploy.action", "install");
        configuration.put("felix.log.level", "4");
        configuration.put("felix.fileinstall.log.level", "4");
        configuration.put("felix.fileinstall.dir", loadDir.getPath());
        configuration.put("felix.fileinstall.noInitialDelay", "true");
        configuration.put("felix.fileinstall.poll", "1000");
        configuration.put("felix.fileinstall.bundles.new.start", "true");
        configuration.put("org.osgi.framework.startlevel.beginning", "5");
        configuration.put("felix.startlevel.bundle", "5");
        configuration.put("org.eclipse.sensinact.simulated.gui.enabled", "false");
        configuration.put("felix.bootdelegation.classloaders", new HashMap() {
            public Object get(Object key) {
                if (Bundle.class.isAssignableFrom(key.getClass())) {
                    if (MidOSGiTestBak.this.isExcluded(((Bundle) key).getSymbolicName())) {
                        return null;
                    }
                    return classloader;
                }
                return super.get(key);
            }
        });
        configuration.put("org.eclipse.sensinact.gateway.test.codeBase", getAllowedCodeBase());
        this.doInit(configuration);

        final Class<?> factoryClass = classloader.loadClass(FELIX_FRAMEWORK_FACTORY);
        final Class<?> bundleClass = classloader.loadClass(BUNDLE);
        final Class<?> autoProcessorClass = classloader.loadClass(AUTO_PROCESSOR);

        frameworkClass = classloader.loadClass(FELIX_FRAMEWORK);

        File manifestFile = new File("./target/generated-test-sources/META-INF/MANIFEST.MF");
        this.createDynamicBundle(manifestFile, bundleDir, new File[]{new File("./target/classes")});

        Object factory = factoryClass.newInstance();

        felix = factoryClass.getDeclaredMethod(FRAMEWORK_FACTORY_INIT_FRAMEWORK, FRAMEWORK_FACTORY_INIT_FRAMEWORK_TYPES).invoke(factory, new Object[]{configuration});
        frameworkClass.getDeclaredMethod(FRAMEWORK_INIT).invoke(felix);

        context = (BundleContext) bundleClass.getDeclaredMethod(BUNDLE_GET_CONTEXT).invoke(felix);
        autoProcessorClass.getDeclaredMethod("process", new Class<?>[]{Map.class, BundleContext.class}).invoke(null, new Object[]{configuration, context});
        frameworkClass.getDeclaredMethod(FRAMEWORK_START).invoke(felix);

        Assertions.assertTrue(bundleClass == Bundle.class);
        Assertions.assertTrue(((Integer) bundleClass.getDeclaredMethod(BUNDLE_STATE).invoke(felix)) == Bundle.ACTIVE);
        //let the environment initialize
        Thread.sleep(5000);
    }

    /**
     * @return
     */
    protected String getAllowedCodeBase() {
        String m2 = this.getMavenRepository();
        String path = "file:".concat(m2.concat("/*"));
        path = path.concat(",http://felix.extensions:9/");

        String testPath = new File("target/test-classes").getAbsolutePath();
        path = path.concat(String.format(",file:%s%s", testPath.startsWith("/") ? "" : "/", testPath));
        path = path.concat("/*");
        return path;
    }

    /**
     * @return
     */
    protected String getMavenRepository() {
        String m2 = System.getenv().get("M2_REPO");
        if (m2 == null) {
            m2 = System.getProperty("user.home");
            m2 = m2.concat(".m2/repository");
        }
        if (!m2.startsWith("/")) {
            m2 = "/".concat(m2);
        }
        return m2;
    }
}
