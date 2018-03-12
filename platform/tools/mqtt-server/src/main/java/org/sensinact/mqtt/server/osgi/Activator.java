package org.sensinact.mqtt.server.osgi;

import org.eclipse.sensinact.gateway.common.annotation.Property;
import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.wiring.BundleWiring;
import org.sensinact.mqtt.server.MQTTException;
import org.sensinact.mqtt.server.MQTTServerService;
import org.sensinact.mqtt.server.impl.MQTTServerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;


public class Activator extends AbstractActivator {

    @Property
    private Boolean autoStart;

    @Property(defaultValue = "1883")
    private Integer port;

    private Logger LOG= LoggerFactory.getLogger(Activator.class);
	private MQTTServerService service;
    private ServiceRegistration sr;

    public void doStart() throws Exception {

        BundleContext bundleContext=super.mediator.getContext();

        service=new MQTTServerImpl(bundleContext);

		if(autoStart){
            LOG.debug("Start MQTT Service autoStart enabled.");
		    try {
                LOG.info("Start MQTT Service on port {}",port);
                service.startService(port.toString());
                LOG.info("MQTT Service on port {} started.",port);
            }catch(MQTTException e){
                LOG.warn("Failed to start MQTT Service on port {}",port);
            }

        }

        sr=bundleContext.registerService(MQTTServerService.class.getName(),service,new Hashtable<String,String>());

    }

    public void doStop() throws Exception {
        sr.unregister();
        service.stopServer();
    }

    @Override
    public Mediator doInstantiate(BundleContext context) {
        return new Mediator(context);
    }

}
