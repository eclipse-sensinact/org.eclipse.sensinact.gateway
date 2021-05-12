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
/**
 *
 */
package org.eclipse.sensinact.gateway.generic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.ActionResource;
import org.eclipse.sensinact.gateway.core.ResourceConfig;
import org.eclipse.sensinact.gateway.core.ResourceConfigCatalog;
import org.eclipse.sensinact.gateway.core.ResourceDescriptor;
import org.eclipse.sensinact.gateway.generic.parser.XmlResourceConfigHandler;

/**
 *
 */
public class ExtResourceConfigCatalog implements ResourceConfigCatalog {
    /**
     * the service string identifiers are mapped to the
     * default associated {@link ResourceConfig}s
     */
    private Map<String, ExtResourceConfig> defaults;

    /**
     * the set of managed {@link ExtResourceConfig}
     */
    protected List<ExtResourceConfig> resourceConfigs;

    /**
     * Constructor
     *
     * @param mediator the {@link Mediator} allowing to interact with the OSGi host environment
     * @param configurationFile the URL of the XML description file of the handled resources
     */
    public ExtResourceConfigCatalog(XmlResourceConfigHandler handler) {
        this(handler, Collections.<String, String>emptyMap());
    }

    /**
     * Constructor
     *
     * @param mediator the {@link Mediator} allowing to interact with the OSGi host environment
     * @param configurationFile the URL of the XML description file of the handled resources
     * @param defaults mapping between service name and its default resource name
     */
    public ExtResourceConfigCatalog(XmlResourceConfigHandler handler, Map<String, String> defaults) {
        this.resourceConfigs = new ArrayList<ExtResourceConfig>();

        Map<String, ExtResourceConfig> tmpDefaults = new HashMap<String, ExtResourceConfig>();

        if (handler != null) {
            Iterator<ExtResourceConfig> resourceIterator = handler.getXmlResourceConfigs();

            while (resourceIterator.hasNext()) {
                ExtResourceConfig resourceConfig = resourceIterator.next();
                this.resourceConfigs.add(resourceConfig);
                if (defaults == null) 
                    continue;
                String name = resourceConfig.getName();
                Iterator<Map.Entry<String, String>> iterator = defaults.entrySet().iterator();

                while (iterator.hasNext()) {
                    Map.Entry<String, String> entry = iterator.next();
                    String serviceId = null;
                    String config = null;
                    if ((serviceId = entry.getKey()) == null || (config = entry.getValue()) == null || !config.equals(name)) 
                        continue;
                    tmpDefaults.put(serviceId, resourceConfig);
                }
            }
        }
        this.defaults = Collections.unmodifiableMap(tmpDefaults);
    }

    @Override
    public ExtResourceConfig getResourceConfig(ResourceDescriptor resourceConfigDescriptor) {
        if (resourceConfigDescriptor == null)
            return null;
        ExtResourceConfig resourceConfig = null;
        String serviceName = resourceConfigDescriptor.serviceName();
        String resourceName = resourceConfigDescriptor.resourceName();
        byte[] identifier = null;

        String profile = resourceConfigDescriptor.profile();
        
        if (ExtResourceDescriptor.class.isAssignableFrom(resourceConfigDescriptor.getClass())) 
            identifier = ((ExtResourceDescriptor) resourceConfigDescriptor).identifier();        
        int index = -1;
        if (resourceName != null) {
            Iterator<ExtResourceConfig> iterator = this.resourceConfigs.iterator();
            while (iterator.hasNext()) {
                resourceConfig = iterator.next();
                String rawName = serviceName!=null?resourceConfig.getRawName(serviceName):resourceConfig.getRawName();
                if (resourceName.equalsIgnoreCase(rawName) && resourceConfig.isProfiled(profile))
                    break;
                resourceConfig = null;
            }
        } else if (identifier != null) {
            Iterator<ExtResourceConfig> iterator = this.resourceConfigs.iterator();
            while (iterator.hasNext()) {
                resourceConfig = iterator.next();
                if (ExtModelConfiguration.compareBytesArrays(identifier, resourceConfig.getIdentifier()))
                    break;
                resourceConfig = null;
            }
        }
        return resourceConfig;
    }

    /**
     * Returns the array of {@link ExtResourceConfig} associated to the
     * service whose name is passed as parameter.
     *
     * @param serviceId the name of the service for which retrieve the
     *                  list of associated {@link ExtResourceConfig}s
     * @return the list of {@link ExtResourceConfig}s associated to the
     * specified service
     */
    public List<ExtResourceConfig> getResourceConfigs(String serviceId) {
        return this.getResourceConfigs(ResourceConfig.ALL_PROFILES, serviceId);
    }

    /**
     * Returns the array of {@link ExtResourceConfig} associated to the
     * service whose name is passed as parameter.
     *
     * @param serviceId the name of the service for which retrieve the
     *                  list of associated {@link ExtResourceConfig}s
     * @return the list of {@link ExtResourceConfig}s associated to the
     * specified service
     */
    public List<ExtResourceConfig> getResourceConfigs(String profile, String service) {
        String profileId = profile == null ? ResourceConfig.ALL_PROFILES : profile;
        String serviceId = service == null ? ResourceConfig.ALL_TARGETS : service;
        List<ExtResourceConfig> list = new ArrayList<ExtResourceConfig>();
        List<ExtResourceConfig> actionslist = new ArrayList<ExtResourceConfig>();

        Iterator<ExtResourceConfig> enumeration = this.resourceConfigs.iterator();
        while (enumeration.hasNext()) {
            ExtResourceConfig resourceConfig = enumeration.next();
            if (resourceConfig.isProfiled(profileId) && resourceConfig.isTargeted(serviceId)) {
                if (ActionResource.class.isAssignableFrom(resourceConfig.getTypeConfig().getResourceImplementedInterface()))
                    actionslist.add(resourceConfig);
                else
                    list.add(resourceConfig);
            }
        }
        list.addAll(actionslist);
        return list;
    }

    public ExtResourceConfig getDefaultResourceConfig(String serviceName) {
        return this.getDefaultResourceConfig(ResourceConfig.ALL_PROFILES, serviceName);
    }

    @Override
    public ExtResourceConfig getDefaultResourceConfig(String profile, String serviceName) {
        return this.defaults.get(serviceName);
    }
}
