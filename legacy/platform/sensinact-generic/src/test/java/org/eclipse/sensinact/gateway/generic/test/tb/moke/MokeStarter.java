/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.generic.test.tb.moke;

import org.eclipse.sensinact.gateway.generic.Connector;
import org.eclipse.sensinact.gateway.generic.ExtModelInstance;
import org.eclipse.sensinact.gateway.test.StarterService;

public class MokeStarter implements StarterService {
    private Connector<MokePacket> mokeSnaProcessor;

    public MokeStarter(Connector<MokePacket> mokeSnaProcessor) {
        this.mokeSnaProcessor = mokeSnaProcessor;
    }

    @Override
    public void start(String serviceProvider) {
        ExtModelInstance<?> instance = this.mokeSnaProcessor.getModelInstance(serviceProvider);
        instance.getRootElement().start();
    }
}
