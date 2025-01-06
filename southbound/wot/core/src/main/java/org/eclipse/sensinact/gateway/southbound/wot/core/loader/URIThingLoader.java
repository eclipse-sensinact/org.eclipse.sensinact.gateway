/*********************************************************************
* Copyright (c) 2024 Kentyou.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Thomas Calmant (Kentyou) - Initial contribution
**********************************************************************/

package org.eclipse.sensinact.gateway.southbound.wot.core.loader;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Objects;

import org.eclipse.sensinact.gateway.southbound.wot.api.Thing;
import org.eclipse.sensinact.gateway.southbound.wot.api.handlers.ThingManager;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

@Component(immediate = true, configurationPid = "sensinact.southbound.wot.loader.uri", configurationPolicy = ConfigurationPolicy.REQUIRE)
public class URIThingLoader {

    private static final Logger logger = LoggerFactory.getLogger(URIThingLoader.class);

    /**
     * Web of Thing manager
     */
    @Reference
    ThingManager manager;

    /**
     * Thread to read data
     */
    private Thread thread;

    /**
     * Name of the loaded provider
     */
    private volatile String providerName;

    @Activate
    void activate(final LoaderConfiguration loaderConfiguration) throws Exception {
        final URI uri;
        try {
            uri = URI.create(Objects.requireNonNull(loaderConfiguration.uri(), "No target URI given"));
        } catch (Exception e) {
            logger.error("Error parsing target URI", e);
            throw e;
        }

        // Convert to a URL
        final URL url;
        try {
            url = uri.toURL();
        } catch (MalformedURLException e) {
            logger.error("Error reading target URL {}", uri, e);
            throw e;
        }

        // Work in a separate thread
        thread = new Thread(() -> load(url));
        thread.setDaemon(false);
        thread.start();
    }

    @Deactivate
    void deactivate() {
        if (thread != null) {
            if (thread.isAlive()) {
                thread.interrupt();
                try {
                    thread.join(1000);
                } catch (InterruptedException e) {
                    logger.warn("Interrupted waiting for thread to stop");
                }
            }

            thread = null;
        }

        if (providerName != null) {
            try {
                manager.unregisterThing(providerName);
            } catch (InvocationTargetException | InterruptedException e) {
                logger.error("Error unregistering provider {}", providerName, e);
            }

            providerName = null;
        }
    }

    void load(final URL url) {
        try (InputStream inStream = url.openStream()) {
            final ObjectMapper mapper = JsonMapper.builder().build();
            final Thing thing = mapper.readValue(inStream, Thing.class);
            providerName = manager.registerThing(thing);
        } catch (IOException e) {
            logger.error("Error reading data from {}", url, e);
        }
    }
}
