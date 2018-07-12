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
package org.eclipse.sensinact.gateway.sthbnd.http.free.internal;

import org.eclipse.sensinact.gateway.common.bundle.ManagedConfigurationListener;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpMediator;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.SimpleHttpProtocolStackEndpoint;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class FreeSmsProtocolStackEndpoint extends SimpleHttpProtocolStackEndpoint implements ManagedConfigurationListener {
    //********************************************************************//
    //						NESTED DECLARATIONS			  			      //
    //********************************************************************//

    /**
     * Free Mobile SMS service configuration
     */
    public class FreeSmsResourceConfig {
        private String user;
        private String pass;

        public void setUser(String user) {
            this.user = user;
        }

        public void setPass(String pass) {
            this.pass = pass;
        }

        public String getUser() {
            return user;
        }

        public String getPass() {
            return pass;
        }
    }

    //********************************************************************//
    //						ABSTRACT DECLARATIONS						  //
    //********************************************************************//
    //********************************************************************//
    //						STATIC DECLARATIONS							  //
    //********************************************************************//
    protected static final String USER_KEY = "user";

    protected static final String PASS_KEY = "pass";

    //********************************************************************//
    //						INSTANCE DECLARATIONS						  //
    //********************************************************************//
    /**
     * the registered Free mobile SMS services
     */
    private Map<String, FreeSmsResourceConfig> configuredResources;

    /**
     * @param mediator
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public FreeSmsProtocolStackEndpoint(HttpMediator mediator) throws ParserConfigurationException, SAXException, IOException {
        super(mediator);
        this.configuredResources = new HashMap<String, FreeSmsResourceConfig>();
        super.mediator.addListener(this);
    }

    /**
     * @inheritDoc
     * @see ManagedConfigurationListener#
     * updated(java.util.Dictionary)
     */
    @Override
    public void updated(Dictionary<String, ?> properties) {
        Map<String, FreeSmsResourceConfig> propertiesResources = new HashMap<String, FreeSmsResourceConfig>();

        Dictionary<String, ?> props = properties == null ? new Hashtable<String, Object>() : properties;
        Enumeration<String> enumeration = props.keys();
        Stack<String> toBeAdded = new Stack<String>();

        while (enumeration.hasMoreElements()) {
            String propertyKey = enumeration.nextElement();
            String[] propertyKeyParts = propertyKey.split("#");

            if (propertyKeyParts.length == 2) {
                FreeSmsResourceConfig fsrc = propertiesResources.get(propertyKeyParts[0]);

                if (fsrc == null) {
                    fsrc = new FreeSmsResourceConfig();
                    propertiesResources.put(propertyKeyParts[0], fsrc);
                    if (!configuredResources.containsKey(propertyKeyParts[0])) {
                        toBeAdded.push(propertyKeyParts[0]);
                    }
                }
                if (USER_KEY.equals(propertyKeyParts[1])) {
                    fsrc.setUser((String) props.get(propertyKey));
                    continue;

                } else if (PASS_KEY.equals(propertyKeyParts[1])) {
                    fsrc.setPass((String) props.get(propertyKey));
                    continue;
                }
            }
            super.mediator.warn(String.format("unrecognized property '%s' \n '<name>#(user|pass)' key format expected ", propertyKey));
        }

        Iterator<Map.Entry<String, FreeSmsResourceConfig>> iterator = configuredResources.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, FreeSmsResourceConfig> entry = iterator.next();
            if (!propertiesResources.containsKey(entry.getKey())) {
                try {
                    super.process(new FreeSmsPacket(entry.getKey(), null, null, false));
                    iterator.remove();
                } catch (InvalidPacketException e) {
                    mediator.error(e);
                }
            }
        }
        while (!toBeAdded.isEmpty()) {
            String key = toBeAdded.pop();
            FreeSmsResourceConfig fsrc = propertiesResources.get(key);

            String user = null;
            String pass = null;

            if (fsrc == null || (user = fsrc.getUser()) == null || (pass = fsrc.getPass()) == null) {
                mediator.warn(String.format("%s : user and pass properties are needed", key));
                continue;
            }
            try {
                configuredResources.put(key, fsrc);
                super.process(new FreeSmsPacket(key, user, pass, true));
            } catch (InvalidPacketException e) {
                super.mediator.error(e);
            }
        }
    }

    /**
     * Returns the FreeSmsResourceConfig data structure mapped to the
     * service provider name passed as parameter
     *
     * @param serviceProvider the service provider name for which to return
     *                        the associated FreeSmsResourceConfig data structure
     * @return the FreeSmsResourceConfig data structure for the specified
     * service provider
     */
    public FreeSmsResourceConfig getFreeSmsResourceConfig(String serviceProvider) {
        return this.configuredResources.get(serviceProvider);
    }
}
