/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
/**
 *
 */
package org.eclipse.sensinact.gateway.generic;

import org.eclipse.sensinact.gateway.common.primitive.Modifiable;
import org.eclipse.sensinact.gateway.core.Resource;
import org.eclipse.sensinact.gateway.core.Resource.UpdatePolicy;
import org.eclipse.sensinact.gateway.core.ResourceConfig;
import org.eclipse.sensinact.gateway.core.ResourceDescriptor;
import org.eclipse.sensinact.gateway.core.ResourceImpl;

/**
 *
 */
public class ExtResourceDescriptor extends ResourceDescriptor {
    private byte[] identifier;

    /**
     * Constructor
     */
    public ExtResourceDescriptor() {
        super();
        this.withResourceConfigType(ExtResourceConfig.class);
    }

    /**
     * @inheritDoc
     * @see ResourceDescriptor#withProfile(java.lang.String)
     */
    @Override
    public ExtResourceDescriptor withProfile(String profile) {
        super.withProfile(profile);
        return this;
    }

    /**
     * @inheritDoc
     * @see ResourceDescriptor#withServiceName(java.lang.String)
     */
    @Override
    public ExtResourceDescriptor withServiceName(String serviceName) {
        super.withServiceName(serviceName);
        return this;
    }

    /**
     * @inheritDoc
     * @see ResourceDescriptor#withResourceName(java.lang.String)
     */
    @Override
    public ExtResourceDescriptor withResourceName(String resourceName) {
        super.withResourceName(resourceName);
        return this;
    }

    /**
     * @inheritDoc
     * @see ResourceDescriptor#withResourceType(java.lang.Class)
     */
    @Override
    public ExtResourceDescriptor withResourceType(Class<? extends Resource> resourceType) {
        super.withResourceType(resourceType);
        return this;
    }

    /**
     * @inheritDoc
     * @see ResourceDescriptor#withDataType(java.lang.Class)
     */
    @Override
    public ExtResourceDescriptor withDataType(Class<?> dataType) {
        super.withDataType(dataType);
        return this;
    }

    /**
     * @inheritDoc
     * @see ResourceDescriptor#withDataValue(java.lang.Object)
     */
    @Override
    public ExtResourceDescriptor withDataValue(Object value) {
        super.withDataValue(value);
        return this;
    }

    /**
     * @inheritDoc
     * @see ResourceDescriptor#withUpdatePolicy(Resource.UpdatePolicy)
     */
    @Override
    public ExtResourceDescriptor withUpdatePolicy(UpdatePolicy updatePolicy) {
        super.withUpdatePolicy(updatePolicy);
        return this;
    }

    /**
     * @inheritDoc
     * @see ResourceDescriptor#withModifiable(Modifiable)
     */
    @Override
    public ExtResourceDescriptor withModifiable(Modifiable modifiable) {
        super.withModifiable(modifiable);
        return this;
    }

    /**
     * @inheritDoc
     * @see ResourceDescriptor#withHidden(boolean)
     */
    @Override
    public ExtResourceDescriptor withHidden(boolean hidden) {
        super.withHidden(hidden);
        return this;
    }

    /**
     * @inheritDoc
     * @see ResourceDescriptor#withResourceConfigType(java.lang.Class)
     */
    @Override
    public ExtResourceDescriptor withResourceConfigType(Class<? extends ResourceConfig> resourceConfigType) {
        super.withResourceConfigType(resourceConfigType);
        return this;
    }

    /**
     * @inheritDoc
     * @see ResourceDescriptor#withResourceImplementationType(java.lang.Class)
     */
    @Override
    public ExtResourceDescriptor withResourceImplementationType(Class<? extends ResourceImpl> resourceImplementationType) {
        super.withResourceImplementationType(resourceImplementationType);
        return this;
    }

    /**
     * @param identifier
     * @return
     */
    public ExtResourceDescriptor withIdentifier(byte[] identifier) {
        this.identifier = identifier;
        return this;
    }

    /**
     * @return
     */
    public byte[] identifier() {
        return this.identifier;
    }
}
