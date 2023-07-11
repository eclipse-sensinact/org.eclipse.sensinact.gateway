/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.prototype.metrics.impl;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.sensinact.core.metrics.IMetricsListener;
import org.eclipse.sensinact.core.push.PrototypePush;
import org.eclipse.sensinact.core.push.dto.BulkGenericDto;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pushes metrics to a sensiNact provider
 */
@Component(immediate = true, service = {
        IMetricsListener.class }, configurationPid = MetricsManager.PID, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class MetricsPusher implements IMetricsListener {

    private static final Logger logger = LoggerFactory.getLogger(MetricsPusher.class);

    /**
     * Pusher for Generic DTOs
     */
    @Reference
    private PrototypePush push;

    /**
     * Activation flag
     */
    private boolean isActive;

    @Activate
    void activate(final MetricsConfiguration config) {
        update(config);
    }

    @Modified
    void update(final MetricsConfiguration config) {
        isActive = config.enabled() && config.provider_enabled();
        if (isActive) {
            logger.debug("Metrics provider is active as {}", config.provider_name());
        }
    }

    @Deactivate
    void deactivate() {
        isActive = false;
    }

    @Override
    public void onMetricsReport(final BulkGenericDto dtos) {
        if (isActive) {
            try {
                push.pushUpdate(dtos).getValue();
            } catch (InvocationTargetException | InterruptedException e) {
                logger.error("Error updating SensiNact metrics provider: {}", e.getMessage(), e);
            }
        }
    }
}
