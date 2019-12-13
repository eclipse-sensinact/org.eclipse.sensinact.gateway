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
package org.eclipse.sensinact.gateway.app.manager.test;

import org.eclipse.sensinact.gateway.api.core.AttributeBuilder;
import org.eclipse.sensinact.gateway.api.core.DataResource;
import org.eclipse.sensinact.gateway.api.core.Resource;
import org.eclipse.sensinact.gateway.app.manager.osgi.AppServiceMediator;
import org.eclipse.sensinact.gateway.core.ResourceBuilder;
import org.eclipse.sensinact.gateway.core.ResourceConfig;
import org.eclipse.sensinact.gateway.core.ResourceDescriptor;
import org.eclipse.sensinact.gateway.core.ResourceImpl;
import org.eclipse.sensinact.gateway.core.TypeConfig;
import sun.misc.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class TestUtils {
    /**
     * Returns a new {@link ResourceBuilder} instance to create
     * a new {@link ResourceImpl} whose proxied type is the one
     * passed as parameter
     *
     * @param implementationInterface the extended {@link Resource} type that will be
     *                                used to create new {@link ResourceImpl} instance(s)
     * @return a new {@link ResourceBuilder} instance
     */
    static ResourceBuilder createResourceBuilder(AppServiceMediator mediator, ResourceDescriptor descriptor) {
        ResourceConfig resourceConfig = new ResourceConfig();
        TypeConfig typeConfig = new TypeConfig(descriptor.resourceType());
        if (descriptor.resourceImplementationType() != null) {
            typeConfig.setImplementationClass(descriptor.resourceImplementationType());
        }
        resourceConfig.setTypeConfig(typeConfig);
        if (descriptor.updatePolicy() != null) {
            resourceConfig.setUpdatePolicy(descriptor.updatePolicy());
        }
        ResourceBuilder builder = new ResourceBuilder(mediator, resourceConfig);
        if (descriptor.resourceName() != null) {
            builder.configureName(descriptor.resourceName());
        }
        if (descriptor.dataType() != null) {
            builder.configureType(descriptor.dataType());
        }
        if (descriptor.dataValue() != null) {
            builder.configureValue(descriptor.dataValue());
        }
        if (descriptor.modifiable() != null) {
            builder.configureRequirement(DataResource.VALUE, AttributeBuilder.Requirement.MODIFIABLE, descriptor.modifiable());
        }
        if (descriptor.hidden() != null) {
            builder.configureRequirement(DataResource.VALUE, AttributeBuilder.Requirement.HIDDEN, descriptor.hidden().booleanValue());
        }
        return builder;
    }

    /**
     * Returns a {@link String} from a file
     *
     * @param stream
     * @param encoding
     * @return
     * @throws IOException
     */
    public static String readFile(InputStream stream, Charset encoding) throws IOException {
        String output = new String(IOUtils.readFully(stream, stream.available(), true), encoding);
        stream.close();
        return output;
    }
}
