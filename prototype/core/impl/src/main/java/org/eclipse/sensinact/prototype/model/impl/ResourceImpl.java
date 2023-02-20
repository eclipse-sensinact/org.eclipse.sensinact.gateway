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

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.ETypedElement;
import org.eclipse.sensinact.model.core.ResourceAttribute;
import org.eclipse.sensinact.prototype.command.impl.CommandScopedImpl;
import org.eclipse.sensinact.prototype.model.Resource;
import org.eclipse.sensinact.prototype.model.ResourceType;
import org.eclipse.sensinact.prototype.model.Service;
import org.eclipse.sensinact.prototype.model.ValueType;

public class ResourceImpl extends CommandScopedImpl implements Resource {

    private final Service service;
    private final ETypedElement feature;

    public ResourceImpl(AtomicBoolean active, Service service, ETypedElement feature) {
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

        return findResourceType(feature);
    }

    public static ResourceType findResourceType(ETypedElement feature) {
        // Check the metadata, Sensor if no info
        if (feature instanceof EOperation) {
            return ResourceType.ACTION;
        } else if (feature instanceof ResourceAttribute) {
            return ResourceType.valueOf(((ResourceAttribute) feature).getResourceType().getName());
        }
        return ResourceType.PROPERTY;
    }

    @Override
    public List<Map.Entry<String, Class<?>>> getArguments() {
        checkValid();
        if (getResourceType() != ResourceType.ACTION) {
            throw new IllegalArgumentException("This is not an action resource");
        }
        return findActionParameters((EOperation) feature);
    }

    public static List<Map.Entry<String, Class<?>>> findActionParameters(EOperation operation) {
        List<Map.Entry<String, Class<?>>> result = operation.getEParameters().stream()
                .map(ep -> new AbstractMap.SimpleImmutableEntry<String, Class<?>>(ep.getName(),
                        ep.getEType().getInstanceClass()))
                .collect(Collectors.toList());
        return result;
    }

    @Override
    public Service getService() {
        checkValid();
        return service;
    }

    public static ValueType findValueType(ETypedElement feature) {
        // Check the metadata, Sensor if no info
        if (feature instanceof ResourceAttribute) {
            return ValueType.valueOf(((ResourceAttribute) feature).getValueType().getName());
        }
        throw new UnsupportedOperationException("Handling of none Sensinact Atributes not implemented yet");
    }

}
