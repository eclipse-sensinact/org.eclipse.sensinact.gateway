/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.generic.test.tb.moke4;

import java.util.Collections;

import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.SensiNactResourceModelConfiguration.BuildPolicy;
import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;
import org.eclipse.sensinact.gateway.generic.ExtModelConfigurationBuilder;
import org.eclipse.sensinact.gateway.generic.ExtModelInstance;
import org.eclipse.sensinact.gateway.test.ProcessorService;
import org.osgi.annotation.bundle.Header;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;

@Header(name = Constants.BUNDLE_ACTIVATOR, value = "${@class}")
public class Activator extends AbstractActivator<Mediator> {
    private ExtModelConfiguration manager;
    private MokeStack endpoint;
    private ServiceRegistration<ProcessorService> processorRegistration;
    private ServiceRegistration<?> starterRegistration;

    @Override
    public void doStart() throws Exception {
    	
    	this.manager = new ExtModelConfigurationBuilder(super.mediator, 
    			ExtModelConfiguration.class, 
    			ExtModelInstance.class, 
    			MokePacket.class
    		).withDesynchronization(false
    		).withStartAtInitializationTime(true
    		).withServiceBuildPolicy(BuildPolicy.BUILD_COMPLETE_ON_DESCRIPTION.getPolicy()
    		).withResourceBuildPolicy(BuildPolicy.BUILD_COMPLETE_ON_DESCRIPTION.getPolicy()
    		).build("genova-resource_0.xml", Collections.<String, String>emptyMap());

        this.endpoint = new MokeStack(mediator);
        this.endpoint.connect(this.manager);
        MokeProcessor processor = new MokeProcessor(this.endpoint.getConnector());
        this.processorRegistration = super.mediator.getContext().registerService(ProcessorService.class, processor, null);

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
