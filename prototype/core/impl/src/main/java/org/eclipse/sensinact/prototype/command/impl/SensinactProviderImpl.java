/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.prototype.command.impl;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.sensinact.prototype.command.SensinactProvider;
import org.eclipse.sensinact.prototype.command.SensinactService;
import org.eclipse.sensinact.prototype.model.ServiceBuilder;

public class SensinactProviderImpl extends CommandScopedImpl implements SensinactProvider {

    private final String model;
    private final String name;

    public SensinactProviderImpl(AtomicBoolean active, String model, String name) {
        super(active);
        this.model = model;
        this.name = name;
    }

    @Override
    public ServiceBuilder createService(String service) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, SensinactService> getServices() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isExclusivelyOwned() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isAutoDelete() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getModelName() {
        return model;
    }

}
