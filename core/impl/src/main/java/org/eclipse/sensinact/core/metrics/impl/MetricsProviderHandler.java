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
*   Thomas Calmant (Kentyou) - initial implementation
**********************************************************************/
package org.eclipse.sensinact.core.metrics.impl;

import java.util.Hashtable;

import org.eclipse.sensinact.core.metrics.IMetricsListener;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.push.dto.BulkGenericDto;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

@Component(immediate = true, service = {}, configurationPid = MetricsManager.PID, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class MetricsProviderHandler implements IMetricsListener {

    @Reference
    private DataUpdate updater;

    /**
     * Enable pushing DTOs to the gateway
     */
    private boolean enabled;

    /**
     * Registration of the optional registration metric
     */
    private ServiceRegistration<IMetricsListener> svcReg;

    @Activate
    void activate(BundleContext ctx, MetricsConfiguration config) {
        enabled = config.enabled() && config.provider_enabled();
        if (enabled) {
            svcReg = ctx.registerService(IMetricsListener.class, this, new Hashtable<>());
        } else {
            svcReg = null;
        }
    }

    @Deactivate
    void deactivate() {
        enabled = false;
        if (svcReg != null) {
            svcReg.unregister();
            svcReg = null;
        }
    }

    @Override
    public void onMetricsReport(BulkGenericDto dto) {
        if (enabled) {
            updater.pushUpdate(dto);
        }
    }
}
