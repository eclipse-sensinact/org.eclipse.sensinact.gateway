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
package org.eclipse.sensinact.gateway.generic.test.tb.moke3;

import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.SensiNactResourceModelConfiguration.BuildPolicy;
import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;
import org.eclipse.sensinact.gateway.generic.ExtModelConfigurationBuilder;
import org.eclipse.sensinact.gateway.generic.ExtModelInstance;
import org.eclipse.sensinact.gateway.test.ProcessorService;
import org.eclipse.sensinact.gateway.test.StarterService;
import org.osgi.annotation.bundle.Header;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;

import java.util.Collections;

@Header(name = Constants.BUNDLE_ACTIVATOR, value = "${@class}")
public class Activator extends AbstractActivator<Mediator> {
    private ExtModelConfiguration manager;
    private MokeStack endpoint;
    private ServiceRegistration processorRegistration;
    private ServiceRegistration starterRegistration;

    @Override
    public void doStart() throws Exception {
    	
    	this.manager = new ExtModelConfigurationBuilder(super.mediator, 
    			ExtModelConfiguration.class, 
    			ExtModelInstance.class, 
    			MokePacket.class
    		).withDesynchronization(false
    		).withStartAtInitializationTime(false
    		).withServiceBuildPolicy(BuildPolicy.BUILD_ON_DESCRIPTION.getPolicy()
    		).withResourceBuildPolicy(BuildPolicy.BUILD_COMPLETE_ON_DESCRIPTION.getPolicy()
    		).build("genova-resource.xml", Collections.<String, String>emptyMap());

        this.endpoint = new MokeStack();
        this.endpoint.connect(this.manager);

        MokeProcessor processor = new MokeProcessor(super.mediator, this.endpoint.getConnector());

        this.processorRegistration = super.mediator.getContext().registerService(ProcessorService.class, processor, null);
        MokeStarter starter = new MokeStarter(this.endpoint.getConnector());

        this.starterRegistration = super.mediator.getContext().registerService(StarterService.class, starter, null);

    }

    @Override
    public void doStop() throws Exception {
        if (this.starterRegistration != null) {
            try {
                this.starterRegistration.unregister();

            } catch (IllegalStateException e) {
            }
            this.starterRegistration = null;
        }
        if (this.processorRegistration != null) {
            try {
                this.processorRegistration.unregister();

            } catch (IllegalStateException e) {
            }
            this.processorRegistration = null;
        }
    }

    @Override
    public Mediator doInstantiate(BundleContext context) {
        return new Mediator(context);
    }
}
