package org.sensinact.mqtt.server.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.sensinact.mqtt.server.MQTTServerService;
import org.sensinact.mqtt.server.impl.MQTTServerImpl;

import java.util.Hashtable;


public class Activator implements BundleActivator {

	private MQTTServerService service;
    private ServiceRegistration sr;

    public void start(BundleContext bundleContext) throws Exception {

		service=new MQTTServerImpl(bundleContext);
		sr=bundleContext.registerService(MQTTServerService.class.getName(),service,new Hashtable<String,String>());

    }

    public void stop(BundleContext bundleContext) throws Exception {
        sr.unregister();
        service.stopServer();
    }

}
