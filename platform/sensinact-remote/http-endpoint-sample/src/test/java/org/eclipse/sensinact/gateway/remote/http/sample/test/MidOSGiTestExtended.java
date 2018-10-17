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
package org.eclipse.sensinact.gateway.remote.http.sample.test;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.Core;
import org.eclipse.sensinact.gateway.core.DataResource;
import org.eclipse.sensinact.gateway.core.SensiNact;
import org.eclipse.sensinact.gateway.core.Session;
import org.eclipse.sensinact.gateway.core.message.AbstractMidCallback;
import org.eclipse.sensinact.gateway.core.message.MessageRegisterer;
import org.eclipse.sensinact.gateway.core.message.MidAgentCallback;
import org.eclipse.sensinact.gateway.core.message.MidCallbackException;
import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.eclipse.sensinact.gateway.core.message.SnaErrorMessageImpl;
import org.eclipse.sensinact.gateway.core.message.SnaFilter;
import org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessageImpl;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.core.message.SnaResponseMessage;
import org.eclipse.sensinact.gateway.core.message.SnaUpdateMessageImpl;
import org.eclipse.sensinact.gateway.simulated.slider.api.SliderSetterItf;
import org.eclipse.sensinact.gateway.test.MidOSGiTest;
import org.eclipse.sensinact.gateway.test.MidProxy;
import org.json.JSONArray;
import org.junit.Assert;
import org.junit.Before;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.AllPermission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class MidOSGiTestExtended extends MidOSGiTest {
    //********************************************************************//
    //						NESTED DECLARATIONS			  			      //
    //********************************************************************//
    class AgentCallback extends AbstractMidCallback implements MidAgentCallback {
        final String[] UNLISTENED = new String[]{"/sensiNact/system", "/AppManager/admin"};

        /**
         * Constructor
         *
         * @param identifier the {@link Mediator} that will be used
         *                   by the AbstractSnaAgentCallback to instantiate
         */
        protected AgentCallback() {
            super(false);
        }

        /**
         * @inheritDoc
         * @see MessageRegisterer#register(SnaMessage)
         */
        @Override
        public void doCallback(SnaMessage<?> message) throws MidCallbackException {
            if (message == null) {
                return;
            }
            String path = message.getPath();
            if (path == null) {
                return;
            }
            int index = 0;
            int length = UNLISTENED == null ? 0 : UNLISTENED.length;
            for (; index < length; index++) {
                String unlistened = UNLISTENED[index];
                if (unlistened == null) {
                    continue;
                }
                if (path.startsWith(unlistened)) {
                    return;
                }
            }
            try {
                treat(message);

            } catch (Exception e) {
                throw new MidCallbackException(e);
            }
        }

        @Override
        public void doHandle(SnaLifecycleMessageImpl message) {
        }

        @Override
        public void doHandle(SnaUpdateMessageImpl message) {
        }

        @Override
        public void doHandle(SnaErrorMessageImpl message) {
        }

        @Override
        public void doHandle(SnaResponseMessage<?, ?> message) {
        }

        private final void treat(SnaMessage<?> message) { 
            String json = message.getJSON();
            synchronized(MidOSGiTestExtended.this.stack) {
            	stack.push(json);
            }
        }
    }

    //********************************************************************//
    //						ABSTRACT DECLARATIONS						  //
    //********************************************************************//
    //********************************************************************//
    //						STATIC DECLARATIONS							  //
    //********************************************************************//
    //********************************************************************//
    //						INSTANCE DECLARATIONS						  //
    //********************************************************************//
    private int count;
    private File confDir;

    private final Stack<String> stack = new Stack<>();
    private Session s = null;

    /**
     * @throws Exception
     */
    public MidOSGiTestExtended(int count) throws Exception {
        super();
        this.count = count;
        super.cacheDir.delete();
        cacheDir = new File(felixDir, String.format("felix-cache%s", count));
        if (!cacheDir.exists()) {
            cacheDir.mkdir();
        }
        confDir = new File(felixDir, String.format("conf%s", count));
        if (!confDir.exists()) {
            confDir.mkdir();
        }
    }

    /**
     * @throws Exception
     */
    @Before
    @Override
    @SuppressWarnings({"unchecked", "rawtypes", "serial"})
    public void init() throws Exception {
        final Map configuration = new HashMap();
        if (System.getSecurityManager() == null) {
            configuration.put("org.osgi.framework.security", "osgi");
        }
        configuration.put("felix.cache.rootdir", felixDir.getPath());
        configuration.put("org.osgi.framework.storage", cacheDir.getName());
        configuration.put("org.osgi.framework.bootdelegation", "*");
        configuration.put("org.osgi.framework.system.packages.extra", "org.eclipse.sensinact.gateway.test," + "com.sun.net.httpserver," + "javax.activation," + "javax.net.ssl," + "javax.xml.parsers," + "javax.imageio," + "javax.management," + "javax.naming," + "javax.sql," + "javax.swing," + "javax.swing.border," + "javax.swing.event," + "javax.management.modelmbean," + "javax.management.remote," + "javax.security.auth," + "javax.security.cert," + "org.w3c.dom," + "org.xml.sax," + "org.xml.sax.helpers," + "sun.misc," + "javax.mail," + "javax.mail.internet," + "sun.security.action");
        configuration.put("org.osgi.framework.storage.clean", "onFirstInit");
        configuration.put("felix.auto.deploy.action", "install");
        configuration.put("felix.log.level", "4");
        configuration.put("felix.fileinstall.log.level", "3");
        configuration.put("felix.fileinstall.dir", confDir.getPath());
        configuration.put("felix.fileinstall.noInitialDelay", "true");
        configuration.put("felix.fileinstall.poll", "1000");
        configuration.put("felix.cm.dir", confDir.getPath());
        configuration.put("felix.fileinstall.bundles.new.start", "true");
        configuration.put("org.osgi.framework.startlevel.beginning", "5");
        configuration.put("felix.startlevel.bundle", "5");
        configuration.put("org.eclipse.sensinact.simulated.gui.enabled", "false");
        configuration.put("felix.bootdelegation.classloaders", new HashMap() {
            public Object get(Object key) {
                if (Bundle.class.isAssignableFrom(key.getClass())) {
                    if (MidOSGiTestExtended.this.isExcluded(((Bundle) key).getSymbolicName())) {
                        return null;
                    }
                    return classloader;
                }
                return super.get(key);
            }
        });

        configuration.put("org.eclipse.sensinact.gateway.test.codeBase", getAllowedCodeBase());
        configuration.put("org.apache.felix.http.jettyEnabled", "true");
        configuration.put("org.apache.felix.http.whiteboardEnabled", "true");
        configuration.put("org.apache.felix.http.debug", "true");
        configuration.put("org.osgi.service.http.port", Integer.toString((8093 + count)));
        configuration.put("org.eclipse.sensinact.gateway.namespace", "sna" + count);

        this.doInit(configuration);

        final Class<?> factoryClass = classloader.loadClass(FELIX_FRAMEWORK_FACTORY);
        final Class<?> bundleClass = classloader.loadClass(BUNDLE);
        final Class<?> autoProcessorClass = classloader.loadClass(AUTO_PROCESSOR);

        frameworkClass = classloader.loadClass(FELIX_FRAMEWORK);
        if (!new File(bundleDir, "dynamicBundle.jar").exists()) {
            File manifestFile = new File("./target/generated-test-sources/META-INF/MANIFEST.MF");
            this.createDynamicBundle(manifestFile, bundleDir, new File[]{new File("./target/classes")});
        }
        Object factory = factoryClass.newInstance();

        felix = factoryClass.getDeclaredMethod(FRAMEWORK_FACTORY_INIT_FRAMEWORK, FRAMEWORK_FACTORY_INIT_FRAMEWORK_TYPES).invoke(factory, new Object[]{configuration});
        frameworkClass.getDeclaredMethod(FRAMEWORK_INIT).invoke(felix);

        context = (BundleContext) bundleClass.getDeclaredMethod(BUNDLE_GET_CONTEXT).invoke(felix);
        autoProcessorClass.getDeclaredMethod("process", new Class<?>[]{Map.class, BundleContext.class}).invoke(null, new Object[]{configuration, context});
        frameworkClass.getDeclaredMethod(FRAMEWORK_START).invoke(felix);

        Assert.assertTrue(bundleClass == Bundle.class);
        Assert.assertTrue(((Integer) bundleClass.getDeclaredMethod(BUNDLE_STATE).invoke(felix)) == Bundle.ACTIVE);

        //the following is needed, to avoid AccessControllerException
        //TODO: find the elegant (more appropriate) way of doing this
        try {
            Method m = felix.getClass().getDeclaredMethod("getSecurityProvider");
            m.setAccessible(true);
            Object s = m.invoke(felix);

            Field f = s.getClass().getDeclaredField("m_pai");
            f.setAccessible(true);
            Object p = f.get(s);

            Class<?> pic = p.getClass().getClassLoader().loadClass("org.osgi.service.permissionadmin.PermissionInfo");

            m = p.getClass().getDeclaredMethod("setPermissions", new Class<?>[]{String.class, Array.newInstance(pic, 0).getClass()});
            Object a = Array.newInstance(pic, 1);
            Object pi = pic.getConstructor(new Class<?>[]{String.class, String.class, String.class}).newInstance(new Object[]{AllPermission.class.getName(), "", ""});
            Array.set(a, 0, pi);
            m.invoke(p, new Object[]{"file:target/felix/bundle/org.apache.felix.fileinstall.jar", a});
            m.invoke(p, new Object[]{"file:target/felix/bundle/org.apache.felix.configadmin.jar", a});

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.test.MidOSGiTest#doInit(java.util.Map)
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void doInit(Map configuration) {
        configuration.put("felix.auto.start.1", 
        		"file:target/felix/bundle/org.osgi.compendium.jar "
        		+ "file:target/felix/bundle/org.apache.felix.configadmin.jar " 
        		+ "file:target/felix/bundle/org.apache.felix.framework.security.jar ");
        configuration.put("felix.auto.install.2", 
        		"file:target/felix/bundle/sensinact-utils.jar "
        		+ "file:target/felix/bundle/sensinact-common.jar " 
        		+ "file:target/felix/bundle/sensinact-datastore-api.jar " 
        		+ "file:target/felix/bundle/sensinact-framework-extension.jar " 
        		+ "file:target/felix/bundle/sensinact-security-none.jar " 
        		+ "file:target/felix/bundle/sensinact-generic.jar ");
        configuration.put("felix.auto.start.2", 
        		"file:target/felix/bundle/sensinact-test-configuration.jar " 
        		+ "file:target/felix/bundle/org.apache.felix.fileinstall.jar " 
        		+ "file:target/felix/bundle/sensinact-signature-validator.jar " 
        		+ "file:target/felix/bundle/sensinact-core.jar ");
        configuration.put("felix.auto.start.3", 
        		"file:target/felix/bundle/javax.servlet-api.jar " 
        		+ "file:target/felix/bundle/org.apache.felix.http.api.jar " 
        		+ "file:target/felix/bundle/org.apache.felix.http.jetty.jar " 
        		+ "file:target/felix/bundle/http.jar " 
        		+ "file:target/felix/bundle/sensinact-northbound-access.jar " 
        		+ "file:target/felix/bundle/rest-access.jar " 
        		+ "file:target/felix/bundle/http-tools.jar " 
        		+ "file:target/felix/bundle/http-endpoint.jar ");
        configuration.put("felix.auto.start.4", 
        		"file:target/felix/bundle/slider.jar " 
        		+ "file:target/felix/bundle/light.jar " 
        		+ "file:target/felix/bundle/dynamicBundle.jar ");

        configuration.put("org.eclipse.sensinact.gateway.security.jks.filename", "target/felix/bundle/keystore.jks");
        configuration.put("org.eclipse.sensinact.gateway.security.jks.password", "sensiNact_team");
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.test.MidOSGiTest#isExcluded(java.lang.String)
     */
    @Override
    protected boolean isExcluded(String filename) {
        if ("org.apache.felix.framework.security.jar".equals(filename)) {
            return true;
        }
        return false;
    }

    public void moveSlider(int value) throws ClassNotFoundException, IOException {
        MidProxy<SliderSetterItf> sliderProxy = new MidProxy<SliderSetterItf>(classloader, this, SliderSetterItf.class);
        SliderSetterItf slider = sliderProxy.buildProxy();
        slider.move(value);
    }

    public String namespace() throws ClassNotFoundException, IOException {
        MidProxy<Core> mid = new MidProxy<Core>(classloader, this, Core.class);
        Core core = mid.buildProxy();
        return core.namespace();
    }

    public String get(String provider, String service, String resource) throws ClassNotFoundException, IOException {
        MidProxy<Core> mid = new MidProxy<Core>(classloader, this, Core.class);

        Core core = mid.buildProxy();
        Session s = core.getAnonymousSession();
        MidProxy<Session> mids = (MidProxy<Session>) Proxy.getInvocationHandler(s);
        try {
            Object o = mids.toOSGi(Session.class.getDeclaredMethod("get", new Class<?>[]{String.class, String.class, String.class, String.class}), new Object[]{provider, service, resource, DataResource.VALUE});
            Object j = o.getClass().getDeclaredMethod("getJSON").invoke(o);
            System.out.println(j);
            return (String) j;

        } catch (Throwable e) {
            //e.printStackTrace();
        }
        return null;
    }

    public String set(String provider, String service, String resource, Object value) throws Exception {
        MidProxy<Core> mid = new MidProxy<Core>(classloader, this, Core.class);
        Core core = mid.buildProxy();
        Session s = core.getAnonymousSession();
        MidProxy<Session> mids = (MidProxy<Session>) Proxy.getInvocationHandler(s);
        try {
            Object o = mids.toOSGi(Session.class.getDeclaredMethod("set", new Class<?>[]{String.class, String.class, String.class, String.class, Object.class}), new Object[]{provider, service, resource, DataResource.VALUE, value});
            Object j = o.getClass().getDeclaredMethod("getJSON").invoke(o);
            System.out.println(j);
            return (String) j;

        } catch (Throwable e) {
            //e.printStackTrace();
        }
        return null;
    }

    public String act(String provider, String service, String resource, Object... args) throws Exception {
        MidProxy<Core> mid = new MidProxy<Core>(classloader, this, Core.class);
        Core core = mid.buildProxy();
        Session s = core.getAnonymousSession();
        MidProxy<Session> mids = (MidProxy<Session>) Proxy.getInvocationHandler(s);
        try {
            Object o = mids.toOSGi(Session.class.getDeclaredMethod("act", new Class<?>[]{String.class, String.class, String.class, Object[].class}), new Object[]{provider, service, resource, args});
            Object j = o.getClass().getDeclaredMethod("getJSON").invoke(o);
            System.out.println(j);
            return (String) j;

        } catch (Throwable e) {
            //e.printStackTrace();
        }
        return null;
    }

    public String subscribe(String provider, String service, String resource, Recipient recipient) throws Exception {
        MidProxy<Core> mid = new MidProxy<Core>(classloader, this, Core.class);
        Core core = mid.buildProxy();
        Session s = core.getAnonymousSession();
        MidProxy<Session> mids = (MidProxy<Session>) Proxy.getInvocationHandler(s);
        try {
            Object o = mids.toOSGi(Session.class.getDeclaredMethod("subscribe", new Class<?>[]{String.class, String.class, String.class, Recipient.class, JSONArray.class}), new Object[]{provider, service, resource, recipient, null});
            Object j = o.getClass().getDeclaredMethod("getJSON").invoke(o);
            System.out.println(j);
            return (String) j;

        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public String unsubscribe(String provider, String service, String resource, String subscriptionId) throws ClassNotFoundException, IOException {
        MidProxy<Core> mid = new MidProxy<Core>(classloader, this, Core.class);
        Core core = mid.buildProxy();
        Session s = core.getAnonymousSession();
        MidProxy<Session> mids = (MidProxy<Session>) Proxy.getInvocationHandler(s);
        try {
            Object o = mids.toOSGi(Session.class.getDeclaredMethod("unsubscribe", new Class<?>[]{String.class, String.class, String.class, String.class}), new Object[]{provider, service, resource, subscriptionId});
            Object j = o.getClass().getDeclaredMethod("getJSON").invoke(o);
            System.out.println(j);
            return (String) j;

        } catch (Throwable e) {
            //e.printStackTrace();
        }
        return null;
    }

    public String providers() throws Throwable {
        MidProxy<Core> mid = new MidProxy<Core>(classloader, this, Core.class);
        Core core = mid.buildProxy();
        Session s = core.getAnonymousSession();

        MidProxy<Session> mids = (MidProxy<Session>) Proxy.getInvocationHandler(s);

        Method m = Session.class.getMethod("getProviders");
        Object o = mids.toOSGi(m, null);
        Object j = o.getClass().getDeclaredMethod("getJSON").invoke(o);
        return (String) j;
    }

    public void registerAgent() throws Throwable {
        MidProxy<Core> mid = new MidProxy<Core>(classloader, this, Core.class);
        Core core = mid.buildProxy();
        s = core.getAnonymousSession();

        MidProxy<Session> mids = (MidProxy<Session>) Proxy.getInvocationHandler(s);
        Object o = mids.toOSGi(Session.class.getMethod("registerSessionAgent", new Class<?>[]{MidAgentCallback.class, SnaFilter.class}), new Object[]{new AgentCallback(), null});
        Object j = o.getClass().getDeclaredMethod("getJSON").invoke(o);
    }

    public void cleanAgent() throws Throwable {
    	synchronized(this.stack) {
        	this.stack.clear();
        }
    }
    
    public void stop() throws Throwable {
        MidProxy<Core> mid = new MidProxy<Core>(classloader, this, Core.class);
        Core core = mid.buildProxy();
        mid.toOSGi(SensiNact.class.getDeclaredMethod("unregisterEndpoint", String.class), new Object[]{"sna2"});
        mid.toOSGi(SensiNact.class.getDeclaredMethod("unregisterEndpoint", String.class), new Object[]{"sna1"});
        cleanAgent();
    }

    public List<String> listAgentMessages() {
        List<String> messages = new ArrayList<>();
        synchronized(this.stack) {
        	messages.addAll(this.stack);
        }
        return Collections.unmodifiableList(messages);
    }
}
