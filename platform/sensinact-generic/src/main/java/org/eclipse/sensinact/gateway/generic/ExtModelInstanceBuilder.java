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
package org.eclipse.sensinact.gateway.generic;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.ModelAlreadyRegisteredException;
import org.eclipse.sensinact.gateway.core.ModelInstanceBuilder;
import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.eclipse.sensinact.gateway.util.ReflectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows to build in a simple way an {@link ExtModelInstance}
 *
 * @param <C> the extended {@link ExtModelConfiguration} type in use
 * @param <I> the extended {@link ExtModelInstance} type in use
 */
public class ExtModelInstanceBuilder<P extends Packet,C extends ExtModelConfiguration<P>, I extends ExtModelInstance<C>> 
extends ModelInstanceBuilder<C,I> {
	
	private static final Logger LOG = LoggerFactory.getLogger(ExtModelInstanceBuilder.class);
	private Connector<? extends Packet> connector;

    /**
     * @param mediator
     * @param packetType the {@link Packet} type of the {@link Connector}
     *                   to which connect the {@link SensiNactResourceModel} to build
     */
    public ExtModelInstanceBuilder(Mediator mediator){
    	super(mediator);
    }

    /**
     * @param connector
     * @return
     */
    protected ExtModelInstanceBuilder<P,C,I> withConnector(Connector<P> connector) {
    	this.connector = connector;
    	return this;
    }
   
    /**
     * Creates and return a {@link SensiNactResourceModel}
     * instance with the specified properties. Optional arguments
     * apply to the {@link SensiNactResourceModelConfiguration}
     * initialization
     *
     * @return the new created {@link SensiNactResourceModel}
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
    public I build(String name, String profileId, C modelConfiguration) {
    	I instance = null;
        if (modelConfiguration != null) {
            super.buildAccessNode(modelConfiguration.getAccessTree(), name);
            Class<I> ci = modelConfiguration.<C,I>getModelInstanceType();

            if(ci != null) {
            	instance = ReflectUtils.<ExtModelInstance,I>getInstance(
            		ExtModelInstance.class, ci,  this.mediator, modelConfiguration, 
            		   name, profileId, this.connector);
            } else {
            	instance = (I) ReflectUtils.<ExtModelInstance>getInstance(
                	ExtModelInstance.class,new Object[] { this.mediator, modelConfiguration, 
                		name, profileId, this.connector});
            }
            try {
                super.register(instance);

            } catch (ModelAlreadyRegisteredException e) {
                LOG.error("Model instance '{}' already exists", name);
                instance = null;
            }
        }
        return instance;
    }
}
