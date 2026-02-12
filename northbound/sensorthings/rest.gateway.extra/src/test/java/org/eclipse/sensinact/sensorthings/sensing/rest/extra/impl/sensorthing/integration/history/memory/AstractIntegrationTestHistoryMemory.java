package org.eclipse.sensinact.sensorthings.sensing.rest.extra.impl.sensorthing.integration.history.memory;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

import org.eclipse.sensinact.sensorthings.sensing.rest.extra.impl.sensorthing.integration.AbstractIntegrationTest;
import org.osgi.service.cm.Configuration;

public abstract class AstractIntegrationTestHistoryMemory extends AbstractIntegrationTest {

    @Override
    protected void updateConfigurationHistory(Configuration sensorthingsConfig) throws IOException {
        Hashtable<String, Object> newProps = new Hashtable<String, Object>();
        newProps.put("history.in.memory", true);

        Dictionary<String, Object> properties = sensorthingsConfig.getProperties();
        Enumeration<String> keys = properties.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            newProps.put(key, properties.get(key));
        }

        sensorthingsConfig.update(newProps);
    }
}
