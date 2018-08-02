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
package org.eclipse.sensinact.gateway.generic;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.primitive.Name;
import org.eclipse.sensinact.gateway.core.InvalidServiceProviderException;
import org.eclipse.sensinact.gateway.core.ServiceProvider;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.eclipse.sensinact.gateway.generic.packet.PacketReader;
import org.eclipse.sensinact.gateway.generic.packet.PayloadFragment;
import org.eclipse.sensinact.gateway.generic.packet.TaskIdValuePair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Manages IO between a {@link ProtocolStackEndpoint} and
 * a set of {@link ServiceProvider}s
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class Connector<P extends Packet> extends TaskManager {
    /**
     * map of managed {@link ExtModelInstance}s
     */
    protected final List<ExtModelInstance<?>> instances;

    /**
     * The global XML formated sensiNact resource model
     * configuration
     */
    protected final ExtModelConfiguration ExtModelConfiguration;

    /**
     * the {@link ConnectorCustomizer} handling the {@link PacketReader}
     * initialization
     */
    protected ConnectorCustomizer<P> customizer;

    /**
     * Initial lock status
     */
    protected boolean locked;

    /**
     * Constructor
     *
     * @param context The associated {@link BundleContext}
     * @param locked  Defines the initial lock state of the
     *                {@link TokenEventProvider} to instantiate
     */
    public Connector(Mediator mediator, ProtocolStackEndpoint<?> endpoint, ExtModelConfiguration ExtModelConfiguration, ConnectorCustomizer<P> customizer) {
        super(mediator, endpoint, ExtModelConfiguration.isLockedAtInitializationTime(), ExtModelConfiguration.isDesynchronized());

        this.ExtModelConfiguration = ExtModelConfiguration;
        this.locked = ExtModelConfiguration.isLockedAtInitializationTime();
        this.instances = new ArrayList<ExtModelInstance<?>>();

        this.customizer = customizer;
        this.configureCustomizer();
    }

    /**
     * Constructor
     *
     * @param context The associated {@link BundleContext}
     * @param locked  Defines the initial lock state of the
     *                {@link TokenEventProvider} to instantiate
     */
    public Connector(Mediator mediator, ProtocolStackEndpoint<?> endpoint, ExtModelConfiguration ExtModelConfiguration) {
        this(mediator, endpoint, ExtModelConfiguration, null);
    }

    /**
     * Configures this Connector's ConnectorCustomiser according
     * to initial configuration properties
     *
     * @param packetType this Connector's Packet type
     * @throws InvalidPacketTypeException
     */
    protected void configureCustomizer() {
        try {
            if (this.customizer == null) {
                this.customizer = new DefaultConnectorCustomizer<P>(mediator, this.ExtModelConfiguration);
            }
        } catch (Exception e) {
            mediator.error(e);
        }
    }

    /**
     * @throws InvalidServiceProviderException
     * @inheritDoc
     * @see PacketReader#process(Packet)
     */
    public void process(P packet) throws InvalidPacketException {
        if (!this.customizer.preProcessing(packet)) {
            if (super.mediator.isDebugLoggable()) {
                super.mediator.debug("Do not process the received packet : exiting");
            }
            return;
        }
        PacketReader<P> reader = this.customizer.newPacketReader(packet);

        if (reader == null) {
            throw new InvalidPacketException("Unable to create an appropriate reader");
        }
        Iterator<TaskIdValuePair> iterator = reader.getTaskIdValuePairs();

        while (iterator.hasNext()) {
            TaskIdValuePair taskIdValuePair = iterator.next();
            String taskIdentifier = taskIdValuePair.taskIdentifier;
            // No need to process if the protocol allows
            // to identify the response according to the
            // initial Task object
            List<Task> tasks = super.remove(taskIdentifier);
            Iterator<Task> taskIterator = tasks.iterator();

            boolean treated = false;

            while (taskIterator.hasNext()) {
                Task task = taskIterator.next();
                if (task != null && !task.isResultAvailable()) {
                    task.setResult(taskIdValuePair.getValue(), taskIdValuePair.getTimestamp());
                    treated = true;
                }
            }
            if (treated) {
                reader.treated(taskIdentifier);
            }
        }
        Iterator<PayloadFragment> subPacketIterator = reader.iterator();
        while (subPacketIterator.hasNext()) {
            PayloadFragment subPacket = subPacketIterator.next();
            String serviceProviderName = subPacket.getServiceProviderIdentifier();

            if (serviceProviderName == null) {
                if (this.mediator.isDebugLoggable()) {
                    this.mediator.debug("Unable to identify the targeted service provider");
                }
                continue;
            }
            int index = -1;
            ExtModelInstance<?> instance = null;
            ExtServiceProviderImpl serviceProvider = null;

            if ((index = this.instances.indexOf(new Name<ExtModelInstance<?>>(serviceProviderName))) > -1) {
                instance = this.instances.get(index);
            }
            if (subPacket.isGoodByeMessage()) {
                this.processGoodbye(instance);
                if (index > -1) {
                    this.instances.remove(index);
                }
                continue;
            }
            if (instance == null) {
                try {
                    instance = this.addModelInstance(subPacket.getProfileId(), serviceProviderName);
                    if (instance == null) {
                        continue;
                    }
                    super.mediator.debug("Service provider discovered : %s", serviceProviderName);
                } catch (InvalidServiceProviderException e) {
                    throw new InvalidPacketException(e);
                }
            }
            serviceProvider = instance.getRootElement();
            if (subPacket.isHelloMessage()) {
                this.processHello(serviceProvider);
            }
            serviceProvider.process(subPacket);
            this.customizer.postProcessing(serviceProvider, reader);
        }
    }

    /**
     * Processes an 'hello' message sent by the {@link ServiceProvider}
     * passed as parameter
     *
     * @param serviceProvider the {@link ServiceProvider} joining the network
     */
    protected void processHello(ExtServiceProviderImpl serviceProvider) {
        if (ServiceProvider.LifecycleStatus.INACTIVE.equals(serviceProvider.getStatus())) {
            if (super.mediator.isDebugLoggable()) {
                super.mediator.debug(new StringBuilder().append("Service provider ").append(serviceProvider.getName()).append("activated").toString());
            }
            serviceProvider.start();
        }
    }

    /**
     * Processes a 'goodbye' message sent by the {@link ServiceProvider}
     * passed as parameter
     *
     * @param serviceProvider the {@link ServiceProvider} leaving the network
     */
    protected void processGoodbye(final ExtModelInstance<?> instance) {
        if (instance == null) {
            if (super.mediator.isDebugLoggable()) {
                super.mediator.debug("An unknown model instance is leaving the network");
            }
            return;
        }
        if (super.mediator.isInfoLoggable()) {
            super.mediator.info(new StringBuilder().append("Service provider '").append(instance.getName()).append("' is leaving the network").toString());
        }
        instance.unregister();
    }

    /**
     * Creates, adds and returns a new {@link ExtServiceProviderImpl}
     * instance with the identifier passed as parameter
     *
     * @param serviceProviderName
     * @param serviceProviderName a String key identifier of the
     *                            {@link ExtServiceProviderImpl} to instantiate
     * @param services            the array of
     * @return a new {@link ExtServiceProviderImpl} instance
     * @throws InvalidServiceProviderException
     */
    protected ExtModelInstance<?> addModelInstance(String profileId, final String serviceProviderName) throws InvalidServiceProviderException {
        @SuppressWarnings({"unchecked", "rawtypes"}) ExtModelInstance<?> instance = new ExtModelInstanceBuilder(this.mediator, this.ExtModelConfiguration.getPacketType()).withConnector(this).withConfiguration(this.ExtModelConfiguration).<ExtModelConfiguration, ExtModelInstance>build(serviceProviderName, profileId);
        if (instance != null) {
            this.instances.add(instance);
        }
        return instance;
    }

    /**
     * Returns the {@link ExtModelInstance} whose name
     * is passed as parameter
     *
     * @param instanceName the name of the {@link ExtModelInstance}
     *                     to return
     * @return the {@link ExtModelInstance} with the specified
     * name
     */
    public ExtModelInstance<?> getModelInstance(String instanceName) {
        int index = this.instances.indexOf(new Name<ExtModelInstance<?>>(instanceName));

        if (index < 0) {
            return null;
        }
        return this.instances.get(index);
    }

    /**
     * Stops this factory, the created {@link TokenEventProvider} and all
     * {@link ExtServiceProviderImpl} instances
     */
    public void stop() {
        super.stop();
        if (this.instances == null || this.instances.size() == 0) {
            return;
        }
        synchronized (this.instances) {
            int length = this.instances.size();
            for (; length > 0; ) {
                try {
                    this.instances.remove(0).unregister();

                } catch (IllegalStateException e) {
                }
                length = this.instances.size();
            }
            this.instances.clear();
        }
    }
}
