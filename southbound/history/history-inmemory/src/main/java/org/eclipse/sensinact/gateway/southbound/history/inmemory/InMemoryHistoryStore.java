/*********************************************************************
* Copyright (c) 2026 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Data In Motion - initial API and implementation
**********************************************************************/
package org.eclipse.sensinact.gateway.southbound.history.inmemory;

import java.util.Hashtable;

import org.eclipse.sensinact.gateway.southbound.history.storage.HistoryStorage;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers an {@link InMemoryHistoryStorage} as a {@link HistoryStorage}
 * service; the history-core engine picks it up and exposes it as a
 * HistoryProvider named by the {@code provider} config property.
 */
@Component(service = {}, immediate = true, configurationPid = "sensinact.history.inmemory", configurationPolicy = ConfigurationPolicy.REQUIRE)
public class InMemoryHistoryStore {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryHistoryStore.class);

    public @interface Config {
        String provider() default "inmemory-history";

        int max_page_size() default 10_000;

        String[] include_resources() default { "{}" };

        String[] exclude_resources() default {};
    }

    private ServiceRegistration<HistoryStorage> registration;

    @Activate
    void start(BundleContext context, Config config) {
        Hashtable<String, Object> properties = new Hashtable<>();
        properties.put(HistoryStorage.PROP_NAME, config.provider());
        properties.put(HistoryStorage.PROP_INCLUDE, config.include_resources());
        if (config.exclude_resources().length > 0) {
            properties.put(HistoryStorage.PROP_EXCLUDE, config.exclude_resources());
        }
        registration = context.registerService(HistoryStorage.class,
                new InMemoryHistoryStorage(config.max_page_size()), properties);
        logger.debug("In-memory history storage {} registered", config.provider());
    }

    @Deactivate
    void stop() {
        if (registration != null) {
            registration.unregister();
            registration = null;
        }
    }
}
