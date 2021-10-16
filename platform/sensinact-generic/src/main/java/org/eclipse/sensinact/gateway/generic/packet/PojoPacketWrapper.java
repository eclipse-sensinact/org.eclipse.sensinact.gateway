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
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.generic.Task;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;

public class PojoPacketWrapper<P extends Packet> implements StructuredPacket, SubPacket {

    private final class PojoPacketExecutable<T> implements Executable<P, T> {
        private Field annotated;

        PojoPacketExecutable(String annotated) {
            try {
                this.annotated = PojoPacketWrapper.class.getDeclaredField(annotated);

            } catch (Exception e) {
                PojoPacketWrapper.this.mediator.error(e);
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public T execute(P packet) throws Exception {
            annotated.setAccessible(true);
            Object value = annotated.get(PojoPacketWrapper.this);

            if (value != null) {
                if (Method.class.isAssignableFrom(value.getClass())) {
                    ((Method) value).setAccessible(true);
                    return (T) ((Method) value).invoke(packet);

                } else if (Field.class.isAssignableFrom(value.getClass())) {
                    ((Field) value).setAccessible(true);
                    return (T) ((Field) value).get(packet);
                }
            }
            return (T) null;
        }
    }

    private P packet;
    private Mediator mediator;
    protected AnnotatedElement attributeIDAnnotated = null;
    protected AnnotatedElement commandIDAnnotated = null;
    protected AnnotatedElement dataAnnotated = null;
    protected AnnotatedElement iterationAnnotated = null;
    protected AnnotatedElement metadataIDAnnotated = null;
    protected AnnotatedElement profileIDAnnotated = null;
    protected AnnotatedElement resourceIDAnnotated = null;
    protected AnnotatedElement serviceIDAnnotated = null;
    protected AnnotatedElement serviceProviderIDAnnotated = null;
    protected AnnotatedElement timestampAnnotated = null;
    protected AnnotatedElement helloMessageAnnotated = null;
    protected AnnotatedElement goodbyeMessageAnnotated = null;

    /**
     * {@link Executable} invoking the method annotated
     * by the @{@link Iteration} annotation
     */
    private final PojoPacketExecutable<Boolean> iteration = new PojoPacketExecutable<Boolean>("iterationAnnotated");

    /**
     * {@link Executable} invoking the @{@link CommandID} annotated
     * element
     */
    private final PojoPacketExecutable<Task.CommandType> commandExtractor = new PojoPacketExecutable<Task.CommandType>("commandIDAnnotated");

    /**
     * {@link Executable} invoking the @{@link GoodbyeMessage} annotated
     * element
     */
    private final PojoPacketExecutable<Boolean> isGoodByeMessageExtractor = new PojoPacketExecutable<Boolean>("goodbyeMessageAnnotated");

    /**
     * {@link Executable} invoking the @{@link HelloMessage} annotated
     * element
     */
    private final PojoPacketExecutable<Boolean> isHelloMessageExtractor = new PojoPacketExecutable<Boolean>("helloMessageAnnotated");
    /**
     * {@link Executable} invoking the @{@link ProfileID} annotated
     * element
     */
    private final PojoPacketExecutable<String> profileIdExtractor = new PojoPacketExecutable<String>("profileIDAnnotated");

    /**
     * {@link Executable} invoking the @{@link ServiceProviderID} annotated
     * element
     */
    private final PojoPacketExecutable<String> serviceProviderIdExtractor = new PojoPacketExecutable<String>("serviceProviderIDAnnotated");
    /**
     * {@link Executable} invoking the @{@link ServiceID} annotated
     * element
     */
    private final PojoPacketExecutable<String> serviceIdExtractor = new PojoPacketExecutable<String>("serviceIDAnnotated");
    /**
     * {@link Executable} invoking the @{@link ResourceID} annotated
     * element
     */
    private final PojoPacketExecutable<String> resourceIdExtractor = new PojoPacketExecutable<String>("resourceIDAnnotated");

    /**
     * {@link Executable} invoking the @{@link AttributeID} annotated
     * element
     */
    private final PojoPacketExecutable<String> attributeIdExtractor = new PojoPacketExecutable<String>("attributeIDAnnotated");
    /**
     * {@link Executable} invoking the @{@link MetadataID} annotated
     * element
     */
    private final PojoPacketExecutable<String> metadataIdExtractor = new PojoPacketExecutable<String>("metadataIDAnnotated");
    /**
     * {@link Executable} invoking the @{@link Timestamp} annotated
     * element
     */
    private final PojoPacketExecutable<Long> timestampExtractor = new PojoPacketExecutable<Long>("timestampAnnotated");
    /**
     * {@link Executable} invoking the @{@link Data} annotated
     * element
     */
    private final PojoPacketExecutable<Object> dataExtractor = new PojoPacketExecutable<Object>("dataAnnotated");

    /**
     * Constructor
     *
     * @param mediator the {@link Mediator} allowing to interact
     *                 with the OSGi host environment
     * @throws InvalidPacketException
     */
    public PojoPacketWrapper(Mediator mediator) {
        this.mediator = mediator;
    }

    /**
     * Wraps the extended <code>&lt;P&gt;</code> typed {@link Packet}
     * passed as parameter
     *
     * @param packet the extended <code>&lt;P&gt;</code> typed {@link Packet}
     *               to wrap
     */
    public void wrap(P packet) {
        this.packet = packet;
    }

    @Override
    public byte[] getBytes() {
        return this.packet.getBytes();
    }

    @Override
    public Iterator<SubPacket> iterator() {
        return new Iterator<SubPacket>() {
            boolean last = false;

            @Override
            public boolean hasNext() {
                return !last;
            }

            @Override
            public SubPacket next() {
                try {
                    last = iteration.execute(packet);
                } catch (Exception e) {
                    last = true;
                }
                return PojoPacketWrapper.this;
            }

            @Override
            public void remove() {
            }
        };
    }

    @Override
    public Task.CommandType getCommand() {
        try {
            return this.commandExtractor.execute(packet);

        } catch (Exception e) {
        	if(mediator.isDebugLoggable())
        		mediator.debug(e.getMessage());
        }
        return null;
    }

    @Override
    public boolean isHelloMessage() {
        try {
           Boolean hello = this.isHelloMessageExtractor.execute(packet);
           if(hello == null)
        	   return false;
           return hello.booleanValue();
        } catch (Exception e) {
        	if(mediator.isDebugLoggable())
        		mediator.debug(e.getMessage());
        }
        return false;
    }

    @Override
    public boolean isGoodbyeMessage() {
        try {
            Boolean goodbye = this.isGoodByeMessageExtractor.execute(packet);
            if(goodbye == null)
            	return false;
            return goodbye.booleanValue();
        } catch (Exception e) {
        	if(mediator.isDebugLoggable())
        		mediator.debug(e.getMessage());
        }
        return false;
    }

    @Override
    public String getProfileId() {
        try {
            return this.profileIdExtractor.execute(packet);

        } catch (Exception e) {
        	if(mediator.isDebugLoggable())
        		mediator.debug(e.getMessage());
        }
        return null;
    }

    @Override
    public String getServiceProviderId() {
        try {
            return this.serviceProviderIdExtractor.execute(packet);

        } catch (Exception e) {
        	if(mediator.isDebugLoggable())
        		mediator.debug(e.getMessage());
        }
        return null;
    }

    @Override
    public String getServiceId() {
        try {
            return this.serviceIdExtractor.execute(packet);

        } catch (Exception e) {
        	if(mediator.isDebugLoggable())
        		mediator.debug(e.getMessage());
        }
        return null;
    }

    @Override
    public String getResourceId() {
        try {
            return this.resourceIdExtractor.execute(packet);

        } catch (Exception e) {
        	if(mediator.isDebugLoggable())
        		mediator.debug(e.getMessage());
        }
        return null;
    }

    @Override
    public String getAttributeId() {
        try {
            return this.attributeIdExtractor.execute(packet);

        } catch (Exception e) {
        	if(mediator.isDebugLoggable())
        		mediator.debug(e.getMessage());
        }
        return null;
    }

    @Override
    public String getMetadataId() {
        try {
            return this.metadataIdExtractor.execute(packet);

        } catch (Exception e) {
        	if(mediator.isDebugLoggable())
        		mediator.debug(e.getMessage());
        }
        return null;
    }

    @Override
    public Object getData() {
        try {
            return this.dataExtractor.execute(packet);

        } catch (Exception e) {
        	if(mediator.isDebugLoggable())
        		mediator.debug(e.getMessage());
        }
        return null;
    }

    @Override
    public long getTimestamp() {
        try {
            Long l = this.timestampExtractor.execute(packet);
            if(l == null)
            	return -1;
            return l.longValue();
        } catch (Exception e) {
        	if(mediator.isDebugLoggable())
        		mediator.debug(e.getMessage());
        }
        return -1;
    }
}
