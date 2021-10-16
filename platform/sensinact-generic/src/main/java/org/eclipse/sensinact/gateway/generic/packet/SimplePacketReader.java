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
package org.eclipse.sensinact.gateway.generic.packet;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.primitive.Name;
import org.eclipse.sensinact.gateway.generic.Task.CommandType;
import org.eclipse.sensinact.gateway.generic.TaskManager;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class SimplePacketReader<P extends Packet> extends AbstractPacketReader<P> {
    protected String profileId;
    protected String serviceProviderId;
    protected String serviceId;
    protected String resourceId;
    protected String attributeId;
    protected String metadataId;
    protected long timestamp;
    protected Object data;

    protected boolean isHelloMessage;
    protected boolean isGoodbyeMessage;

    protected CommandType command;
    protected Mediator mediator;

    /**
     * Constructor
     *
     * @param mediator the {@link Mediator} used to interact with
     *                 the host OSGi environment
     */
    protected SimplePacketReader(Mediator mediator) {
        this.mediator = mediator;
    }

    /**
     * @param profileId
     */
    protected void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    /**
     * @param serviceProviderId
     */
    protected void setServiceProviderId(String serviceProviderId) {
        this.serviceProviderId = serviceProviderId;
    }

    /**
     * Returns the {@link Service}'s string identifier
     * targeted by the {@link PayloadServiceFragment} to create
     *
     * @param serviceId the targeted {@link Service}'s identifier
     */
    protected void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    /**
     * Returns the {@link Resource}'s string identifier
     * targeted by the {@link PayloadServiceFragment} to create
     *
     * @param resourceId the targeted {@link Resource}'s identifier
     */
    protected void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    /**
     * Defines the {@link Attribute}'s string identifier of
     * the {@link PayloadResourceFragment} to create
     *
     * @param attributeId the {@link Attribute}'s string identifier of
     *                    the {@link PayloadResourceFragment} to create
     */
    protected void setAttributeId(String attributeId) {
        this.attributeId = attributeId;
    }

    /**
     * Defines the {@link Metadata}'s string identifier of
     * the {@link PayloadResourceFragment} to create
     *
     * @param attributeId the {@link Metadata}'s string identifier of
     *                    the {@link PayloadResourceFragment} to create
     */
    protected void setMetadataId(String metadataId) {
        this.metadataId = metadataId;
    }

    /**
     * Defines the data object value of the {@link
     * PayloadResourceFragment} to create
     *
     * @param data the data object to set to the
     *             {@link PayloadResourceFragment} to create
     */
    protected void setData(Object data) {
        this.data = data;
    }

    /**
     * Defines the data object value of the {@link
     * PayloadResourceFragment} to create
     *
     * @param data the data object to set to the
     *             {@link PayloadResourceFragment} to create
     */
    protected void setCommand(CommandType command) {
        this.command = command;
    }

    /**
     * Defines whether the SubPacket to create is an "Hello" message,
     * meaning that the targeted {@link ServiceProvider} is connecting
     * to the network or not
     *
     * @param isHelloMessage <ul>
     *                       <li>true if this SubPacket is an "Hello" message</li>
     *                       <li>false otherwise</li>
     *                       </ul>
     */
    public void isHelloMessage(boolean isHelloMessage) {
        this.isHelloMessage = isHelloMessage;
    }

    /**
     * Defines whether the SubPacket to create is a "Goodbye" message,
     * meaning that the targeted {@link ServiceProvider} is disconnecting
     * from the network or not
     *
     * @param isGoobyeMessage <ul>
     *                        <li>true if this SubPacket is an "Goodbye" message</li>
     *                        <li>false otherwise</li>
     *                        </ul>
     */
    public void isGoodbyeMessage(boolean isGoodbyeMessage) {
        this.isGoodbyeMessage = isGoodbyeMessage;
    }

    /**
     * Defines the timestamp associated to the parsed
     * {@link Packet}
     *
     * @param the parsed {@link Packet}'s timestamp
     */
    public long setTimestamp(long timestamp) {
        return this.timestamp = timestamp;
    }
    
    /**
     * Specifies that the packet reader reached the end of the
     * packet
     */
    protected void configureEOF() {
    	reset();
    	super.setSubPacket(PayloadFragment.EOF_FRAGMENT);
    }

    /**
     * Creates the SubPacket, PayloadFragment and PayloadAttributeFragment
     */
    protected void configure() {
        boolean isNewPayloadFragment = false;
        PayloadFragmentImpl subPacket = null;
        
        if (this.serviceProviderId == null) {
        	this.configureEOF();
            return;
        }
        subPacket = newSubPacket();
        subPacket.setProfileId(this.profileId);
        subPacket.setServiceProviderIdentifier(this.serviceProviderId);
        subPacket.isGoodbyeMessage(this.isGoodbyeMessage);
        subPacket.isHelloMessage(this.isHelloMessage);
        
        PayloadServiceFragmentImpl payloadFragment = null;
        StringBuilder builder = new StringBuilder();
        if (this.command != null) {
            builder.append(this.command.name());
            if (this.serviceId != null) 
                builder.append(TaskManager.IDENTIFIER_SEP_CHAR);
        }
        if (this.serviceId != null) {
            builder.append(this.serviceId);
            if (this.resourceId != null)
                builder.append(TaskManager.IDENTIFIER_SEP_CHAR);
        }
        if (this.resourceId != null) 
            builder.append(this.resourceId);
        int index = -1;
        String name = builder.toString();
        
        if (name.length() > 0) {
            if ((index = subPacket.payloadFragments.indexOf(new Name<PayloadServiceFragment>(name))) != -1)
                payloadFragment = (PayloadServiceFragmentImpl) subPacket.payloadFragments.get(index);
            else {
                payloadFragment = newPayloadFragment();
                payloadFragment.setCommand(this.command);
                payloadFragment.setServiceId(this.serviceId);
                payloadFragment.setResourceId(this.resourceId);
                isNewPayloadFragment = true;
            }
        }
        if (payloadFragment != null) {
            if (this.attributeId != null || this.data != null) {
                PayloadResourceFragmentImpl payloadAttributeFragment = newPayloadAttributeFragment(this.attributeId, this.metadataId, this.data);
                payloadAttributeFragment.setTimestamp(this.timestamp);
                payloadFragment.addPayloadAttributeFragment(payloadAttributeFragment);
            }
            if (isNewPayloadFragment) 
                subPacket.addPayloadFragment(payloadFragment);
        }
        reset();
        super.setSubPacket(subPacket);
    }

    @Override
    public void reset() {
        resetFields();
        super.subPacket = null;
    }

    /**
     * Resets the fields of this {@link PacketReader}
     */
    private final void resetFields() {
        this.isGoodbyeMessage = false;
        this.isHelloMessage = false;
        this.command = null;
        this.serviceProviderId = null;
        this.serviceId = null;
        this.resourceId = null;
        this.attributeId = null;
        this.metadataId = null;
        this.timestamp = -1;
        this.data = null;
    }

    /**
     * Creates and returns a new {@link PayloadFragment} instance
     *
     * @return a new {@link PayloadFragment} instance
     */
    protected PayloadFragmentImpl newSubPacket() {
        return new PayloadFragmentImpl(this.mediator);
    }

    /**
     * Creates and returns a new {@link PayloadServiceFragment}
     * instance
     *
     * @return a new {@link PayloadServiceFragment} instance
     */
    protected PayloadServiceFragmentImpl newPayloadFragment() {
        return new PayloadServiceFragmentImpl(this.mediator);
    }

    /**
     * Creates and returns a new {@link PayloadResourceFragment}
     * instance
     *
     * @param attributeId the attribute identifier of the {@link
     *                    PayloadResourceFragment} to create
     * @param metadataId  the metadata identifier of the {@link
     *                    PayloadResourceFragment} to create
     * @param data        the data object of the {@link
     *                    PayloadResourceFragment} to create
     * @return a new {@link PayloadServiceFragment} instance
     */
    private PayloadResourceFragmentImpl newPayloadAttributeFragment(String attributeId, String metadataId, Object data) {
        return new PayloadResourceFragmentImpl(attributeId, metadataId, data);
    }
}
