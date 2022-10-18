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

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.sensinact.prototype.command.SensinactProvider;
import org.eclipse.sensinact.prototype.command.SensinactResource;
import org.eclipse.sensinact.prototype.command.SensinactService;
import org.eclipse.sensinact.prototype.model.ResourceType;
import org.eclipse.sensinact.prototype.model.ValueType;
import org.eclipse.sensinact.prototype.model.nexus.impl.NexusImpl;
import org.eclipse.sensinact.prototype.notification.NotificationAccumulator;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

public class SensinactResourceImpl extends CommandScopedImpl implements SensinactResource {

    private final String name;
    private final SensinactService service;
    private final Class<?> type;
    private final NexusImpl nexusImpl;
    private final PromiseFactory promiseFactory;

    public SensinactResourceImpl(AtomicBoolean active, SensinactService service, String name, Class<?> type,
            NotificationAccumulator accumulator, NexusImpl nexusImpl, PromiseFactory promiseFactory) {
        super(active);
        this.service = service;
        this.name = name;
        this.type = type;
        this.nexusImpl = nexusImpl;
        this.promiseFactory = promiseFactory;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public ValueType getValueType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResourceType getResourceType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Class<?>> getArguments() {
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
    public Promise<Void> setValue(Object value, Instant timestamp) {
        checkValid();

        SensinactService service = getService();
        SensinactProvider provider = service.getProvider();

        nexusImpl.handleDataUpdate(provider.getModelName(), provider.getName(), service.getName(), getName(), getType(),
                value, timestamp);
        return promiseFactory.resolved(null);
    }

    @Override
    public Promise<Object> getValue() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SensinactService getService() {
        return service;
    }

    @Override
    public Promise<Void> setMetadataValue(String name, Object value, Instant timestamp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Promise<Object> getMetadataValue(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Promise<Map<String, Object>> getMetadataValues() {
        // TODO Auto-generated method stub
        return null;
    }

}
