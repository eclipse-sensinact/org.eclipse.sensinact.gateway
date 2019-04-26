import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Properties;

@Component(immediate = true)
public class ConfigAdminDataInstanceConfiguration {

    @Reference
    ConfigurationAdmin configurationAdmin;

    @Activate
    public void activated(){
        System.out.println("****** Component activated *****");
        try {
            Configuration configSensinact=configurationAdmin.getConfiguration("sensinact","?");
            Properties props = new Properties();
            props.put( "namespace", "sna1" );
            props.put( "broker", "tcp://sensinact-cea.ddns.net:5269" );
            props.put( "broker.topic.prefix", "/unittests/" );
            configSensinact.setBundleLocation("?");
            configSensinact.update((Dictionary) props);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}