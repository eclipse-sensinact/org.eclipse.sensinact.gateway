/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.common.bundle;

import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

class MediatorManagedConfiguration implements ManagedService {
    //********************************************************************//
    //						NESTED DECLARATIONS			  			      //
    //********************************************************************//

    //********************************************************************//
    //						ABSTRACT DECLARATIONS						  //
    //********************************************************************//
    //********************************************************************//
    //						STATIC DECLARATIONS							  //
    //********************************************************************//
	
	private static final Logger LOG = LoggerFactory.getLogger(MediatorManagedConfiguration.class);
    public static final String MANAGED_SENSINACT_MODULE = "org.eclipse.sensinact.gateway.managed";
    //********************************************************************//
    //						INSTANCE DECLARATIONS						  //
    //********************************************************************//

    private Mediator mediator;
    private ServiceRegistration<ManagedService> registration;
    private List<ManagedConfigurationListener> listeners;
    private String pid;

    /**
     * Constructor
     *
     * @param mediator the {@link Mediator} allowing to interact
     *                 with the OSGi host environment
     * @param pid      the string identifier of the {@link ManagedService}
     *                 to be instantiated
     */
    MediatorManagedConfiguration(Mediator mediator, String pid) {
        this.pid = pid;
        this.mediator = mediator;
        this.listeners = new ArrayList<ManagedConfigurationListener>();
    }

    /**
     * Adds a {@link ManagedConfigurationListener} to be notified
     * when the properties of this {@link ManagedService} are
     * updated
     *
     * @param listener the {@link ManagedConfigurationListener} to add
     */
    public void addListener(ManagedConfigurationListener listener) {
        if (listener != null) {
            synchronized (this.listeners) {
                this.listeners.add(listener);
            }
        }
    }

    /**
     * Removes the {@link ManagedConfigurationListener} to be removed from
     * the list of those to be notified when the properties of this {@link
     * ManagedService} are updated
     *
     * @param listener the {@link ManagedConfigurationListener} to remove
     */
    public void deleteListener(ManagedConfigurationListener listener) {
        if (listener != null) {
            synchronized (this.listeners) {
                this.listeners.remove(listener);
            }
        }
    }

    /**
     * Returns the default configuration properties set
     *
     * @return the default set of configuration properties
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private Dictionary<String, Object> getDefaults() {
        Hashtable defaults = new Hashtable();
        defaults.put(Constants.SERVICE_PID, pid);
        return defaults;
    }

    /**
     * Registers this MediatorManagedService in the OSGi
     * host environment
     */
    public void register() {
        this.registration = mediator.getContext().<ManagedService>registerService(ManagedService.class, this, getDefaults());
    }

    /**
     * Unregisters this MediatorManagedService from the OSGi
     * host environment
     */
    public void unregister() {
        if (this.registration == null) {
            return;
        }
        try {
            this.registration.unregister();

        } catch (IllegalStateException e) {
            LOG.error(e.getMessage());
        }
    }

    @Override
    public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
        if (registration == null) {
            return;
        }
        Dictionary<String, Object> props = null;
        Dictionary<String, Object> dflt = this.getDefaults();

        if (properties == null) {
            props = dflt;

        } else {
            props = (Dictionary<String, Object>) properties;
            for (Enumeration<String> e = dflt.keys(); e.hasMoreElements(); ) {
                String key = e.nextElement();
                if (props.get(key) == null) props.put(key, dflt.get(key));
            }
        }
        synchronized (this.listeners) {
            try {
                registration.setProperties(props);

                Iterator<ManagedConfigurationListener> iterator = this.listeners.iterator();

                while (iterator.hasNext()) {
                    ManagedConfigurationListener listener = iterator.next();
                    listener.updated(props);
                }
            } catch (Exception e) {
                throw new ConfigurationException(null, e.getMessage(), e);
            }
        }
    }
}