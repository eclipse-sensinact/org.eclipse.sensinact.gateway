/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.app.manager.application.persistence;

import org.eclipse.sensinact.gateway.app.api.persistence.ApplicationPersistenceService;
import org.eclipse.sensinact.gateway.app.api.persistence.dao.Application;
import org.eclipse.sensinact.gateway.app.api.persistence.exception.ApplicationPersistenceException;
import org.eclipse.sensinact.gateway.app.api.persistence.listener.ApplicationAvailabilityListener;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.component.propertytypes.ServiceRanking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component(service = ApplicationPersistenceService.class,scope = ServiceScope.SINGLETON,configurationPolicy = ConfigurationPolicy.REQUIRE)
@ServiceRanking(500)
public class InMemoryApplicationPersistenceService implements ApplicationPersistenceService {
    private static Logger LOG = LoggerFactory.getLogger(InMemoryApplicationPersistenceService.class);
    private List<Application> applications = new ArrayList<Application>();
    private final Set<ApplicationAvailabilityListener> listeners = new HashSet<ApplicationAvailabilityListener>();

    
	@Activate
	public void activate() {
		notifyServiceAvailable();
	}

	private Application findApplication(String name) {
        for (Application application : applications) {
            if (application.getName().equals(name)) return application;
        }
        return null;
    }

    @Override
    public void persist(Application application) throws ApplicationPersistenceException {
        //Not implemented
        Application applicationStored = findApplication(application.getName());
        if (applicationStored == null) {
            applications.add(application);
            notifyInclusion(application);
        } else if (!application.getDiggest().equals(applicationStored.getDiggest())) {
            applications.remove(applicationStored);
            applications.add(application);
            notifyModification(application);
        }
    }

    @Override
    public void delete(String applicationName) throws ApplicationPersistenceException {
        notifyRemoval(findApplication(applicationName));
        applications.remove(findApplication(applicationName));
    }

    @Override
    public JSONObject fetch(String applicationName) throws ApplicationPersistenceException {
        return findApplication(applicationName).getContent();
    }

    @Override
    public Collection<Application> list() {
        return applications;
    }

    @Override
    public void registerServiceAvailabilityListener(ApplicationAvailabilityListener listenerClient) {
        synchronized (this.listeners) {
            this.listeners.add(listenerClient);
        }
    }

    @Override
    public void unregisterServiceAvailabilityListener(ApplicationAvailabilityListener listenerClient) {
        synchronized (this.listeners) {
            this.listeners.remove(listenerClient);
        }
    }

    private void notifyInclusion(Application application) {
    	if(application == null)
    		return;
        try {
            LOG.info("Notifying application '{}' inclusion ", application.getName());
            for (ApplicationAvailabilityListener list : new HashSet<ApplicationAvailabilityListener>(listeners)) {
                try {
                    synchronized (list) {
                        list.applicationFound(application.getName(), application.getContent().toString());
                    }
                } catch (Exception e) {
                    LOG.error("Failed to add application {} into the platform, is ApplicationManager running?", application.getName(), e);
                }
            }
        } catch (Exception e) {
            LOG.error("Failed to load application", e);
        }
    }

    private void notifyModification(Application application) {
    	if(application == null)
    		return;
        try {
            LOG.info("Notifying application '{}' modification ", application.getName());
            for (ApplicationAvailabilityListener list : new HashSet<ApplicationAvailabilityListener>(listeners)) {
                try {
                    synchronized (list) {
                        list.applicationChanged(application.getName(), application.getContent().toString());
                    }
                } catch (Exception e) {
                    LOG.error("Failed to add application {} into the platform, is ApplicationManager running?", application.getName(), e);
                }
            }
        } catch (Exception e) {
            LOG.error("Failed to load application", e);
        }
    }

    private void notifyRemoval(Application application) {
    	if(application == null)
    		return;
        try {
            LOG.info("Notifying application '{}' removal ", application.getName());
            for (ApplicationAvailabilityListener list : new HashSet<ApplicationAvailabilityListener>(listeners)) {
                try {
                    synchronized (list) {
                        list.applicationRemoved(application.getName());
                    }
                } catch (Exception e) {
                    LOG.error("Failed to add application {} into the platform, is ApplicationManager running?", application.getName(), e);
                }
            }
        } catch (Exception e) {
            LOG.error("Failed to load application", e);
        }
    }

    private void notifyServiceUnavailable() {
        LOG.debug("Memory Persistence service is going offline");
        for (ApplicationAvailabilityListener list : new HashSet<ApplicationAvailabilityListener>(listeners)) {
            try {
                list.serviceOffline();
            } catch (Exception e) {
                LOG.error("Memory Persistence service is going offline", e);
            }
        }
    }

    private void notifyServiceAvailable() {
        LOG.debug("Memory Persistence service is going online");
        for (ApplicationAvailabilityListener listener : new HashSet<ApplicationAvailabilityListener>(listeners)) {
            try {
                listener.serviceOnline();
            } catch (Exception e) {
                LOG.error("Memory Persistence service is going online", e);
            }
        }
    }
}
