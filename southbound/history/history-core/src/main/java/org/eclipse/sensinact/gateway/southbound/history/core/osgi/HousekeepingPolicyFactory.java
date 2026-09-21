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
package org.eclipse.sensinact.gateway.southbound.history.core.osgi;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.southbound.history.core.housekeeping.HousekeepingPolicy;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * One retention policy per {@code sensinact.history.housekeeping~<instance>}
 * configuration. Invalid policies (no retention bound at all) are rejected at
 * activation and never scheduled.
 */
@Component(service = {}, immediate = true, configurationPid = HousekeepingPolicyFactory.PID, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class HousekeepingPolicyFactory {

    public static final String PID = "sensinact.history.housekeeping";

    private static final Logger logger = LoggerFactory.getLogger(HousekeepingPolicyFactory.class);

    public @interface Config {
        String name() default "";

        String[] target() default {};

        String retention_period() default "";

        long keep_count() default -1;

        long max_delete() default -1;

        String schedule_period() default "PT24H";
    }

    private ServiceRegistration<HousekeepingPolicy> registration;

    @Activate
    void start(BundleContext context, Config config, Map<String, Object> rawConfig) {
        String name = config.name().isBlank() ? String.valueOf(rawConfig.get("service.pid")) : config.name();

        Duration retention = config.retention_period().isBlank() ? null : Duration.parse(config.retention_period());
        Long keepCount = config.keep_count() < 0 ? null : config.keep_count();
        Long maxDelete = config.max_delete() < 0 ? null : config.max_delete();
        Duration schedule = Duration.parse(config.schedule_period());

        HousekeepingPolicy policy = new HousekeepingPolicy(name, List.of(config.target()), retention, keepCount,
                maxDelete, schedule);
        registration = context.registerService(HousekeepingPolicy.class, policy, null);
        logger.info("Housekeeping policy {} registered (retention: {}, keep: {}, cap: {}, every {})", name,
                retention, keepCount, maxDelete, schedule);
    }

    @Deactivate
    void stop() {
        if (registration != null) {
            registration.unregister();
            registration = null;
        }
    }
}
