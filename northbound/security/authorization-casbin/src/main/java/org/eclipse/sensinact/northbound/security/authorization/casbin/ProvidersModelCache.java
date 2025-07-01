/*********************************************************************
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Kentyou - initial implementation
 **********************************************************************/

package org.eclipse.sensinact.northbound.security.authorization.casbin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.sensinact.core.command.AbstractTwinCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.notification.LifecycleNotification;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.typedevent.TypedEventConstants;
import org.osgi.service.typedevent.TypedEventHandler;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

@Component(immediate = true, service = ProvidersModelCache.class)
public class ProvidersModelCache implements TypedEventHandler<LifecycleNotification> {

    /**
     * Representation of a provider model
     */
    public static record ModelDetails(
            /**
             * Model Package URI
             */
            String modelPackageUri,

            /**
             * Model name
             */
            String model) {
    }

    /**
     * sensiNact gateway thread
     */
    @Reference
    GatewayThread thread;

    /**
     * Provider model -&gt; Model details cache
     */
    final Map<String, ModelDetails> providerModel = Collections.synchronizedMap(new HashMap<>());

    /**
     * Typed Event Handler service registration
     */
    private ServiceRegistration<?> svcReg;

    /**
     * Component started
     */
    @Activate
    void activate(final BundleContext ctx) throws Exception {
        // Register to events before getting the list of models, to avoid losing some in
        // between the calls
        svcReg = ctx.registerService(TypedEventHandler.class, this,
                new Hashtable<>(Map.of(TypedEventConstants.TYPED_EVENT_TOPICS, new String[] { "LIFECYCLE/*" })));

        // Load current models
        providerModel.putAll(thread.execute(new AbstractTwinCommand<Map<String, ModelDetails>>() {
            @Override
            protected Promise<Map<String, ModelDetails>> call(SensinactDigitalTwin twin, PromiseFactory pf) {
                return pf.resolved(twin.getProviders().stream()
                        .map(p -> Map.entry(p.getName(), new ModelDetails(p.getModelPackageUri(), p.getModelName())))
                        .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())));
            }
        }).getValue());
    }

    /**
     * Component stopped
     */
    @Deactivate
    void deactivate() {
        if (svcReg != null) {
            svcReg.unregister();
            svcReg = null;
        }

        // Clear cache
        providerModel.clear();
    }

    /**
     * Returns the known model details for the given provider
     *
     * @param provider Provider name
     * @return Provider model detail, null if unknown
     */
    public ModelDetails getModel(final String provider) {
        return providerModel.get(provider);
    }

    @Override
    public void notify(String topic, LifecycleNotification event) {
        switch (event.status()) {
        case PROVIDER_CREATED:
            providerModel.put(event.provider(), new ModelDetails(event.modelPackageUri(), event.model()));
            break;

        case PROVIDER_DELETED:
            providerModel.remove(event.provider());
            break;

        default:
            // Ignore
        }
    }
}
