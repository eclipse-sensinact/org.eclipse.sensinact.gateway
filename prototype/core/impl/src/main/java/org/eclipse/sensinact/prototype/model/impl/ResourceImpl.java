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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.sensinact.model.core.ModelMetadata;
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
        // Check the metadata, Sensor if no info
        return findResourceType(feature);
    }

    public static ResourceType findResourceType(EStructuralFeature feature) {
        return getModelMetadata(feature).map(ModelMetadata::getExtra)
                .flatMap(l -> l.stream().filter(f -> "resourceType".equals(f.getName())).findFirst())
                .map(f -> (ResourceType) f.getValue()).orElse(ResourceType.SENSOR);
    }

    @Override
    public List<Map.Entry<String, Class<?>>> getArguments() {
        checkValid();
        if (getResourceType() != ResourceType.ACTION) {
            throw new IllegalArgumentException("This is not an action resource");
        }
        return findActionParameters(feature);
    }

    @SuppressWarnings("unchecked")
    public static List<Map.Entry<String, Class<?>>> findActionParameters(EStructuralFeature feature) {
        return getModelMetadata(feature).map(ModelMetadata::getExtra)
                .flatMap(l -> l.stream().filter(f -> "parameters".equals(f.getName())).findFirst())
                .map(f -> (List<Map.Entry<String, Class<?>>>) f.getValue())
                .orElseThrow(() -> new IllegalArgumentException("No parameter data available"));
    }

    private static Optional<ModelMetadata> getModelMetadata(EStructuralFeature feature) {
        return Optional.ofNullable(feature.getEAnnotation("metadata")).flatMap(ann -> ann.eContents().stream()
                .filter(ModelMetadata.class::isInstance).map(ModelMetadata.class::cast).findFirst());
    }

    @Override
    public Service getService() {
        checkValid();
        return service;
    }

}
