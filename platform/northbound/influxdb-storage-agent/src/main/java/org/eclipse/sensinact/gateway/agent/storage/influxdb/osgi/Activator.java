package org.eclipse.sensinact.gateway.agent.storage.influxdb.osgi;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.Core;
import org.eclipse.sensinact.gateway.agent.storage.generic.StorageAgent;
import org.eclipse.sensinact.gateway.agent.storage.influxdb.internal.InfluxStorageConnection;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractActivator<Mediator> {

    private String databaseURL;
    private String login;
    private String password;
    private StorageAgent handler;
    private String registration;

    @Override
    public void doStart() throws Exception {
        try {
            if (super.mediator.isDebugLoggable()) {
                super.mediator.debug("Starting storage agent.");
            }
            Properties prop = new Properties();
            try {
                prop.load(new FileInputStream("cfgs/influxdb.property"));
                //sslClientProperties.load(new FileInputStream("cfgs/sslproperties.property"));
                login = prop.getProperty("username");
                password = prop.getProperty("password");
                databaseURL = prop.getProperty("databaseURL");
            } catch (IOException ex) {
                mediator.error(ex);
            }
            this.handler = new StorageAgent(new InfluxStorageConnection(super.mediator, databaseURL, login, password));
            this.registration = mediator.callService(Core.class, new Executable<Core, String>() {
                @Override
                public String execute(Core service) throws Exception {
                    return service.registerAgent(mediator, Activator.this.handler, null);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doStop() throws Exception {
        if (super.mediator.isDebugLoggable()) {
            super.mediator.debug("Stopping storage agent.");
        }
        if (this.handler != null) {
            this.handler.stop();
        }
        this.registration = null;
        this.handler = null;
    }

    @Override
    public Mediator doInstantiate(BundleContext context) {
        return new Mediator(context);
    }
}
