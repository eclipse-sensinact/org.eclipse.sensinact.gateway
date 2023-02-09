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
package org.eclipse.sensinact.prototype.model.impl;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.sensinact.prototype.command.impl.CommandScopedImpl;
import org.eclipse.sensinact.prototype.model.Resource;
import org.eclipse.sensinact.prototype.model.ResourceType;
import org.eclipse.sensinact.prototype.model.Service;
import org.eclipse.sensinact.prototype.model.ValueType;

public class ResourceImpl extends CommandScopedImpl implements Resource {

    private final Service service;
    private final EStructuralFeature feature;

    public ResourceImpl(AtomicBoolean active, Service service, EStructuralFeature feature) {
        super(active);
        this.service = service;
        this.feature = feature;
    }

    @Override
    public String getName() {
        checkValid();
        return feature.getName();
    }

    @Override
    public boolean isExclusivelyOwned() {
        checkValid();
        throw new RuntimeException("Not implemented");
    }

    @Override
    public boolean isAutoDelete() {
        checkValid();
        throw new RuntimeException("Not implemented");
    }

    @Override
    public Class<?> getType() {
        checkValid();
        return feature.getEType().getInstanceClass();
    }

    @Override
    public ValueType getValueType() {
        checkValid();
        throw new RuntimeException("Not implemented");
    }

    @Override
    public ResourceType getResourceType() {
        checkValid();
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<Class<?>> getArguments() {
        checkValid();
        throw new RuntimeException("Not implemented");
    }

    @Override
    public Service getService() {
        checkValid();
        return service;
    }

}
