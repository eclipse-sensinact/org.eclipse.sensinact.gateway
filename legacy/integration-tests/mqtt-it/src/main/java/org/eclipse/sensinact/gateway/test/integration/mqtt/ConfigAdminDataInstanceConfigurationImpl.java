package org.eclipse.sensinact.gateway.test.integration.mqtt;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.IOException;
import java.util.Hashtable;

/**
 * This component configures SensiNact services (this configuration is mandatory in order to start all SensiNact required services)
 * Configuration is sent via ConfigAdmin
 */
@Component(immediate = true)
public class ConfigAdminDataInstanceConfigurationImpl implements ConfigAdminDataInstanceConfiguration{

    @Reference
    ConfigurationAdmin configurationAdmin;

    @Activate
    public void activated(){

        try {
            Configuration configSensinact=configurationAdmin.getConfiguration("sensinact","?");
            Hashtable<String, Object> props= new Hashtable<>();
            props.put( "namespace", "sna1" );
            props.put( "broker", "tcp://sensinact-cea.ddns.net:5269" );
            props.put( "broker.topic.prefix", "/" );
            configSensinact.update(props);
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            Configuration configSensinactMQTT=configurationAdmin.getConfiguration("mqtt.server","?");
            Hashtable<String, Object> props1= new Hashtable<>();
            props1.put( "port", "1883" );
            props1.put( "autoStart", "false" );
            configSensinactMQTT.update(props1);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public String who() {
        return "ConfigAdminDataInstanceConfigurationImpl";
    }

}