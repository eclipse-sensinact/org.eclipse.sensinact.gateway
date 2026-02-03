/*********************************************************************
* Copyright (c) 2025 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.sensorthings.sensing.rest.extra.endpoint;

import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsApplicationSelect;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsExtension;

import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;

/**
 * Registers the SensorThings Extra implementation with the main SensorThings
 * application.
 */
@JakartarsApplicationSelect("(osgi.jakartars.name=sensorthings)")
@JakartarsExtension
@Component
public class SensorThingsExtraFeature implements Feature {

    private final DataUpdate dataUpdate;
    private final GatewayThread gatewayThread;

    @Activate
    public SensorThingsExtraFeature(@Reference DataUpdate dataUpdate, @Reference GatewayThread gatewayThread) {
        this.dataUpdate = dataUpdate;
        this.gatewayThread = gatewayThread;
    }

    @Override
    public boolean configure(FeatureContext context) {
        context.register(new DataUpdateProvider(dataUpdate));
        context.register(new GatewayThreadProvider(this.gatewayThread));
        context.register(ExtraDelegateProvider.class);
        context.register(UseCaseProvider.class);
        return true;
    }

}
