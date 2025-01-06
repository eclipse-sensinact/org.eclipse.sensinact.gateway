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

package org.eclipse.sensinact.gateway.southbound.wot.http.loader;

import java.net.URI;
import java.util.Objects;

import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.sensinact.gateway.southbound.wot.api.Thing;
import org.eclipse.sensinact.gateway.southbound.wot.api.handlers.ThingManager;
import org.eclipse.sensinact.gateway.southbound.wot.http.SharedHttpClient;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, configurationPid = "sensinact.southbound.wot.loader.http", configurationPolicy = ConfigurationPolicy.REQUIRE)
public class ThingHttpLoader {

    private static final Logger logger = LoggerFactory.getLogger(ThingHttpLoader.class);

    /**
     * Thing manager
     */
    @Reference
    ThingManager manager;

    /**
     * Shared HTTP client
     */
    @Reference
    SharedHttpClient http;

    /**
     * Name of the provider created from the loaded URL
     */
    String managedProviderName;

    @Activate
    void activate(final Configuration config) {
        final URI uri;
        try {
            uri = URI.create(Objects.requireNonNull(config.url(), "No Thing description URL given"));
        } catch (Exception e) {
            logger.error("No URI given");
            throw new IllegalArgumentException("No URI given", e);
        }

        final Request request = http.getClient().newRequest(uri);
        request.accept(MimeTypes.Type.APPLICATION_JSON.asString());
        request.send(new BufferingResponseListener() {

            @Override
            public void onComplete(final Result result) {
                if (result.isFailed()) {
                    logger.error("Failed querying URL {}", uri, result.getFailure());
                    return;
                }

                final Response response = result.getResponse();
                if (response.getStatus() >= 400) {
                    logger.error("Error querying URL {}. Got status {}", uri, response.getStatus());
                    return;
                }

                final Thing parsedThing;
                try {
                    parsedThing = http.getMapper().readValue(getContent(), Thing.class);
                } catch (Exception e) {
                    logger.error("Error parsing thing from URL {}", uri, e);
                    return;
                }

                managedProviderName = manager.registerThing(parsedThing);
                logger.debug("Loaded ThingDescription from {} as provider {}", uri, managedProviderName);
            }
        });
    }

    @Deactivate
    void deactivate() {
        if (managedProviderName != null) {
            try {
                manager.unregisterThing(managedProviderName);
            } catch (Exception e) {
                logger.error("Error unregistering provider {}", managedProviderName, e);
            }
        }
    }
}
