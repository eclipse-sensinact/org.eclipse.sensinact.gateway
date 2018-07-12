/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.generic.test.moke3;

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
