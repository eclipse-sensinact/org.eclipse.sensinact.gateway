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
package org.eclipse.sensinact.gateway.generic.parser;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.xml.sax.Attributes;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Command definition wrapping its type and its bytes array identifier
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@XmlAttributes({@XmlAttribute(attribute = "identifier", field = "serviceProviderIdentifier"), @XmlAttribute(attribute = "profile", field = "serviceProviderProfile")})
public final class DeviceDefinition extends XmlDefinition {
    protected LinkedList<String> services;
    protected Map<String, String> metadata;
    protected String serviceProviderIdentifier;
    private String serviceProviderProfile;

    /**
     * Constructor
     *
     * @param mediator the {@link Mediator} allowing the DeviceDefinition 
     * to be instantiated to interact with the OSGi host environment
     * @param atts     the {@link Attributes} data structure of the "device" 
     * XML node 
     */
    public DeviceDefinition(Mediator mediator, Attributes atts) {
        super(mediator, atts);
        this.services = new LinkedList<String>();
        this.metadata = new HashMap<String, String>();

        int length = atts == null ? 0 : atts.getLength();
        int index = 0;

        for (; index < length; index++) {
            String key = atts.getQName(index);
            String value = atts.getValue(key);
            this.metadata.put(key, value);
        }
    }

    /**
     * Sets the string identifier of this {@link Device} definition
     *
     * @param serviceProviderIdentifier the string identifier of this {@link Device} definition
     */
    public void setServiceProviderIdentifier(String serviceProviderIdentifier) {
        this.serviceProviderIdentifier = serviceProviderIdentifier;
    }

    /**
     * Returns the string identifier of this {@link Device} definition
     *
     * @return the string identifier of this {@link Device} definition
     */
    public String getServiceProviderIdentifier() {
        return this.serviceProviderIdentifier;
    }

    /**
     * @return
     */
    public void setServiceProviderProfile(String serviceProviderProfile) {
        this.serviceProviderProfile = serviceProviderProfile;
    }

    /**
     * @return
     */
    public String getServiceProviderProfile() {
        return this.serviceProviderProfile;
    }

    /**
     * Adds the service name passed as parameter
     * to this device definition
     *
     * @param service the service name to add
     */
    public void addService(String service) {
        if (service != null) {
            this.services.add(service);
        }
    }

    /**
     * Returns the array of predefined services'name
     *
     * @return the array of predefined services'name
     */
    public String[] getServices() {
        return this.services.toArray(new String[this.services.size()]);
    }

    /**
     * Returns the map of metadata associated to
     * the device
     *
     * @return the map of metadata
     */
    public Map<String, String> getMetadata() {
        return Collections.unmodifiableMap(this.metadata);
    }
}
