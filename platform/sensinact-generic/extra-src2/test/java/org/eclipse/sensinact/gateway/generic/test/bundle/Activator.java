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
package org.eclipse.sensinact.gateway.generic.test.bundle;

import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.SensiNactResourceModelConfiguration.BuildPolicy;
import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;
import org.eclipse.sensinact.gateway.generic.ExtModelInstance;
import org.eclipse.sensinact.gateway.generic.ExtModelInstanceBuilder;
import org.eclipse.sensinact.gateway.generic.local.LocalProtocolStackEndpoint;
import org.osgi.framework.BundleContext;

import java.util.Collections;

/**
 *
 */
public class Activator<C extends ExtModelConfiguration, I extends ExtModelInstance<C>> extends AbstractActivator<Mediator> {
    ExtModelConfiguration manager = null;
    LocalProtocolStackEndpoint<GenericTestPacket> connector = null;

    /**
     * @inheritDoc
     * @see AbstractActivator#doStart()
     */
    @SuppressWarnings({"unchecked"})
    @Override
    public void doStart() throws Exception {
        this.manager = new ExtModelInstanceBuilder(super.mediator, GenericTestPacket.class).withDesynchronization(false).withStartAtInitializationTime(true).withResourceBuildPolicy(BuildPolicy.BUILD_COMPLETE_ON_DESCRIPTION.getPolicy()).<ExtModelConfiguration>buildConfiguration("test-resource.xml", Collections.<String, String>emptyMap());
        this.connector = new LocalProtocolStackEndpoint<GenericTestPacket>(mediator);
        this.connector.connect(manager);
    }

    /**
     * @inheritDoc
     * @see AbstractActivator#doStop()
     */
    @Override
    public void doStop() throws Exception {
        connector.stop();
    }

    /**
     * @inheritDoc
     * @see AbstractActivator#doInstantiate(org.osgi.framework.BundleContext)
     */
    @Override
    public Mediator doInstantiate(BundleContext context) {
        return new Mediator(context);
    }
}
