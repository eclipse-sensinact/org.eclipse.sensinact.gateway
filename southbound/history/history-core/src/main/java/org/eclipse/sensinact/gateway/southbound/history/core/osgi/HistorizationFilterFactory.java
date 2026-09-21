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

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.filters.resource.selector.api.ResourceSelector;
import org.eclipse.sensinact.filters.resource.selector.api.ResourceSelectorFilterFactory;
import org.eclipse.sensinact.gateway.southbound.history.core.filter.ChangeCondition;
import org.eclipse.sensinact.gateway.southbound.history.core.filter.ChangeCondition.Mode;
import org.eclipse.sensinact.gateway.southbound.history.core.filter.ConfiguredIngestFilter;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoryIngestFilter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * One historization filter per {@code sensinact.history.filter~<instance>}
 * configuration: declarative include/exclude resource selectors, optional
 * provider targeting and an optional change condition (on-change/deadband
 * with heartbeat). Registered as a {@link HistoryIngestFilter} whiteboard
 * service; configuration changes restart the component and thus re-register
 * the filter — no backend restart involved.
 */
@Component(service = {}, immediate = true, configurationPid = HistorizationFilterFactory.PID, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class HistorizationFilterFactory {

    public static final String PID = "sensinact.history.filter";

    private static final Logger logger = LoggerFactory.getLogger(HistorizationFilterFactory.class);

    public @interface Config {
        String name() default "";

        String[] target() default {};

        String[] include_resources() default { "{}" };

        String[] exclude_resources() default {};

        String change_mode() default "all";

        String change_threshold() default "";

        String change_threshold_percent() default "";

        String change_max_interval() default "";
    }

    @Reference
    ResourceSelectorFilterFactory filterFactory;

    private final ObjectMapper mapper = new ObjectMapper();

    private ServiceRegistration<HistoryIngestFilter> registration;

    @Activate
    void start(BundleContext context, Config config, Map<String, Object> rawConfig) {
        String name = config.name().isBlank() ? String.valueOf(rawConfig.get("service.pid")) : config.name();

        ICriterion include = parse(config.include_resources());
        ICriterion exclude = config.exclude_resources().length == 0 ? null : parse(config.exclude_resources());
        ChangeCondition changeCondition = changeCondition(config, name);

        ConfiguredIngestFilter filter = new ConfiguredIngestFilter(name, List.of(config.target()), include, exclude,
                changeCondition);
        registration = context.registerService(HistoryIngestFilter.class, filter, null);
        logger.info("Historization filter {} registered (targets: {})", name,
                config.target().length == 0 ? "all" : String.join(",", config.target()));
    }

    @Deactivate
    void stop() {
        if (registration != null) {
            registration.unregister();
            registration = null;
        }
    }

    private ICriterion parse(String[] selectors) {
        return filterFactory.parseResourceSelector(Arrays.stream(selectors).map(this::selectorFromJson));
    }

    private ResourceSelector selectorFromJson(String json) {
        try {
            return mapper.readValue(json, ResourceSelector.class);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Unable to read resource selector " + json, e);
        }
    }

    private static ChangeCondition changeCondition(Config config, String name) {
        Mode mode = switch (config.change_mode().toLowerCase(Locale.ROOT)) {
        case "all" -> Mode.ALL;
        case "on-change" -> Mode.ON_CHANGE;
        case "deadband" -> Mode.DEADBAND;
        default -> throw new IllegalArgumentException(
                "Filter " + name + ": unknown change.mode " + config.change_mode());
        };
        BigDecimal threshold = config.change_threshold().isBlank() ? null
                : new BigDecimal(config.change_threshold());
        BigDecimal thresholdPercent = config.change_threshold_percent().isBlank() ? null
                : new BigDecimal(config.change_threshold_percent());
        Duration maxInterval = config.change_max_interval().isBlank() ? null
                : Duration.parse(config.change_max_interval());
        return new ChangeCondition(mode, threshold, thresholdPercent, maxInterval);
    }
}
