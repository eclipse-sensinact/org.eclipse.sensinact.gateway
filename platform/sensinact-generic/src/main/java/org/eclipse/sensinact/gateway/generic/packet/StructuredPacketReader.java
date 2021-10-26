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
import org.eclipse.sensinact.gateway.generic.packet.annotation.AttributeID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.CommandID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.Data;
import org.eclipse.sensinact.gateway.generic.packet.annotation.GoodbyeMessage;
import org.eclipse.sensinact.gateway.generic.packet.annotation.HelloMessage;
import org.eclipse.sensinact.gateway.generic.packet.annotation.Iteration;
import org.eclipse.sensinact.gateway.generic.packet.annotation.MetadataID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ProfileID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ResourceID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ServiceID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ServiceProviderID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @param <P>
 */
public class StructuredPacketReader<P extends Packet> extends SimplePacketReader<P> {
	private static final Logger LOG = LoggerFactory.getLogger(PacketReader.class);
	
    private static final List<String> ANNOTATIONS = Arrays.asList(
    		AttributeID.class.getCanonicalName(), 
    		CommandID.class.getCanonicalName(), 
    		Data.class.getCanonicalName(), 
    		Iteration.class.getCanonicalName(), 
    		MetadataID.class.getCanonicalName(), 
    		ProfileID.class.getCanonicalName(), 
    		ResourceID.class.getCanonicalName(), 
    		ServiceID.class.getCanonicalName(), 
    		ServiceProviderID.class.getCanonicalName(), 
    		Timestamp.class.getCanonicalName(), 
    		HelloMessage.class.getCanonicalName(), 
    		GoodbyeMessage.class.getCanonicalName());

    private PojoPacketWrapper<P> wrapper = null;
    private Iterator<SubPacket> iterator = null;

    /**
     * @param mediator the {@link Mediator} allowing to
     *                 interact with the OSGi host environment
     */
    public StructuredPacketReader(Mediator mediator) {
        super(mediator);
    }

    /**
     * Constructor
     *
     * @param mediator   the {@link Mediator} allowing to
     *                   interact with the OSGi host environment
     * @param packetType the {@link Packet} type to handle
     * @throws InvalidPacketTypeException
     */
    public StructuredPacketReader(Mediator mediator, Class<P> packetType) throws InvalidPacketTypeException {
        this(mediator);
        boolean isAnnotated = false;

        Field[] fields = packetType.getDeclaredFields();
        int fieldsLength = fields == null ? 0 : fields.length;

        Method[] methods = packetType.getDeclaredMethods();
        int methodsLength = methods == null ? 0 : methods.length;

        AnnotatedElement[] elements = new AnnotatedElement[(fieldsLength) + (methodsLength)];

        if (fieldsLength > 0) {
            System.arraycopy(fields, 0, elements, 0, fieldsLength);
        }
        if (methodsLength > 0) {
            System.arraycopy(methods, 0, elements, fieldsLength, methodsLength);
        }
        int index = 0;
        int length = elements == null ? 0 : elements.length;
        PojoPacketWrapper<P> wrapper = new PojoPacketWrapper<P>(mediator);
        for (; index < length; index++) {
        	Annotation[] annotations = null;
        	try {
        		annotations = elements[index].getAnnotations();
        	} catch(Exception e) {
        		e.printStackTrace();
        	}        	
            int annotationIndex = 0;
            int annotationLength = annotations == null ? 0 : annotations.length;

            for (; annotationIndex < annotationLength; annotationIndex++) {
                Class<? extends Annotation> annotationType = annotations[annotationIndex].annotationType();

                int ind = ANNOTATIONS.indexOf(annotationType.getCanonicalName());
                if (ind < 0) {
                    continue;
                }
                String annotationName = annotationType.getSimpleName();
                annotationName = new StringBuilder().append(annotationName.substring(0, 1).toLowerCase()
                	).append(annotationName.substring(1)).append("Annotated").toString();
                
                try {
                    Field annotationField = PojoPacketWrapper.class.getDeclaredField(annotationName);
                    annotationField.setAccessible(true);
                    annotationField.set(wrapper, elements[index]);
                    isAnnotated = true;
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                    throw new InvalidPacketTypeException(e);
                }
            }
        }
        if (!isAnnotated) {
            throw new InvalidPacketTypeException();
        }
        this.wrapper = wrapper;
    }

    @Override
    public void load(P packet) throws InvalidPacketException {
        super.reset();
    	StructuredPacket structuredPacket = null;
        if (StructuredPacket.class.isAssignableFrom(packet.getClass()))
            structuredPacket = (StructuredPacket) packet;
        else if (this.wrapper != null) {
            wrapper.wrap(packet);
            structuredPacket = wrapper;
        }
        if (structuredPacket == null)
            return;
        this.iterator = structuredPacket.iterator();
    }
    
	@Override
	public void parse() throws InvalidPacketException {
        if (iterator == null)
            return;
        if(!iterator.hasNext()) {
        	this.iterator = null;
        	super.configureEOF();
        	return;
        }
        super.reset();
        
        SubPacket subPacket = iterator.next();
        super.isGoodbyeMessage(subPacket.isGoodbyeMessage());
        super.isHelloMessage(subPacket.isHelloMessage());
        super.setCommand(subPacket.getCommand());
        super.setProfileId(subPacket.getProfileId());
        super.setServiceProviderId(subPacket.getServiceProviderId());
        super.setServiceId(subPacket.getServiceId());
        super.setResourceId(subPacket.getResourceId());
        super.setAttributeId(subPacket.getAttributeId());
        super.setMetadataId(subPacket.getMetadataId());
        super.setTimestamp(subPacket.getTimestamp());
        super.setData(subPacket.getData());
        super.configure();
    }
}
