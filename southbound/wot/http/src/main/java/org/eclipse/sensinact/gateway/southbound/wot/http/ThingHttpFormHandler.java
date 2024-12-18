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

package org.eclipse.sensinact.gateway.southbound.wot.http;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.whiteboard.WhiteboardConstants;
import org.eclipse.sensinact.core.whiteboard.WhiteboardHandler;
import org.eclipse.sensinact.gateway.southbound.wot.api.Form;
import org.eclipse.sensinact.gateway.southbound.wot.api.InteractionAffordance;
import org.eclipse.sensinact.gateway.southbound.wot.api.Thing;
import org.eclipse.sensinact.gateway.southbound.wot.api.handlers.SensinactThingDescriptor;
import org.eclipse.sensinact.gateway.southbound.wot.api.handlers.ThingListener;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;

@Component(immediate = true, service = ThingListener.class)
public class ThingHttpFormHandler implements ThingListener {

    private final static Logger logger = LoggerFactory.getLogger(ThingHttpFormHandler.class);

    /**
     * sensiNact gateway thread
     */
    @Reference
    GatewayThread gatewayThread;

    /**
     * Shared HTTP client
     */
    @Reference
    SharedHttpClient http;

    /**
     * Bundle context
     */
    BundleContext ctx;

    /**
     * Map of registered whiteboard handlers: Thing ID -&gt; handler
     */
    private final Map<String, ServiceRegistration<WhiteboardHandler>> whiteboardsRegistrations = new HashMap<>();

    /**
     * Main/default configuration
     */
    private WhiteboardHandlerConfiguration mainConfiguration;

    /**
     * Configuration per form URL prefix
     */
    private Map<String, WhiteboardHandlerConfiguration> perUrlConfiguration;

    @Activate
    void activate(final FormHandlerConfiguration configuration, final BundleContext ctx) throws Exception {
        this.ctx = ctx;

        mainConfiguration = new WhiteboardHandlerConfiguration();
        mainConfiguration.argumentsKey = configuration.argumentsKey();
        mainConfiguration.useArgumentsKeyOnEmptyArgs = configuration.useArgumentsKeyOnEmptyArgs();
        mainConfiguration.propertyKey = configuration.propertyKey();
        mainConfiguration.timestampKey = configuration.timestampKey();

        try {
            if (configuration.url_configuration() != null && !configuration.url_configuration().isBlank()) {
                perUrlConfiguration = http.mapper.readValue(configuration.url_configuration(),
                        new TypeReference<Map<String, WhiteboardHandlerConfiguration>>() {
                        });
            } else {
                perUrlConfiguration = Map.of();
            }
        } catch (Exception e) {
            logger.error("Error parsing configuration", e);
            throw e;
        }
    }

    @Deactivate
    void deactivate() throws Exception {
        whiteboardsRegistrations.values().forEach(ServiceRegistration::unregister);
        whiteboardsRegistrations.clear();
        ctx = null;
        perUrlConfiguration = null;
        mainConfiguration = null;
    }

    /**
     * Checks if there is at least 1 HTTP form in the given list
     *
     * @param forms Properties or actions forms
     * @return True if a least 1 HTTP form was found
     */
    private boolean checkHttpForm(InteractionAffordance affordance) {
        if (affordance == null) {
            return false;
        }

        List<Form> forms = affordance.forms;
        if (forms == null || forms.isEmpty()) {
            return false;
        }

        for (Form form : forms) {
            if (form.href != null && form.href.toLowerCase().startsWith("http")) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void thingRegistered(SensinactThingDescriptor descriptor) {

        // Check if the thing has HTTP support
        Thing thing = descriptor.thing;
        boolean hasHttp = false;
        if (thing.properties != null && !thing.properties.isEmpty()) {
            hasHttp = thing.properties.values().stream().anyMatch(this::checkHttpForm);
        }

        if (!hasHttp && thing.actions != null && !thing.actions.isEmpty()) {
            hasHttp = thing.actions.values().stream().anyMatch(this::checkHttpForm);
        }

        if (!hasHttp) {
            logger.warn("Ignoring Thing {} as it doesn't have any HTTP action nor property", thing.id);
            return;
        }

        // Register the HTTP handler
        logger.debug("Creating HTTP handler for {}", thing.id);
        ThingHttpWhiteboardHandler handler = new ThingHttpWhiteboardHandler(thing, http.getClient(), mainConfiguration,
                perUrlConfiguration);
        ctx.registerService(WhiteboardHandler.class, handler,
                new Hashtable<>(Map.of(WhiteboardConstants.PROP_MODEL_PACKAGE_URI, descriptor.modelPackageUri,
                        WhiteboardConstants.PROP_MODEL, descriptor.modelName, WhiteboardConstants.PROP_PROVIDERS,
                        descriptor.providerId)));
    }

    @Override
    public void thingUnregistered(SensinactThingDescriptor descriptor) {
        logger.debug("Thing {} has disappeared", descriptor.thing.id);

        // Unregister the model and provider based on thing ID
        ServiceRegistration<WhiteboardHandler> svcReg = whiteboardsRegistrations.remove(descriptor.thing.id);
        if (svcReg != null) {
            svcReg.unregister();
        }
    }
}
