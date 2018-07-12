package org.eclipse.sensinact.gateway.app.manager.application.persistence;

import org.eclipse.sensinact.gateway.app.api.persistence.ApplicationPersistenceService;
import org.eclipse.sensinact.gateway.app.api.persistence.dao.Application;
import org.eclipse.sensinact.gateway.app.api.persistence.exception.ApplicationPersistenceException;
import org.eclipse.sensinact.gateway.app.api.persistence.listener.ApplicationAvailabilityListener;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SNAPersistApplicationInMemory implements ApplicationPersistenceService {
    private static Logger LOG = LoggerFactory.getLogger(SNAPersistApplicationInMemory.class);
    private List<Application> applicationList = new ArrayList<Application>();
    private final Set<ApplicationAvailabilityListener> listener = new HashSet<ApplicationAvailabilityListener>();

    private Application findApplication(String name) {
        for (Application application : applicationList) {
            if (application.getName().equals(name)) return application;
        }
        return null;
    }

    @Override
    public void persist(Application application) throws ApplicationPersistenceException {
        //Not implemented
        Application applicationStored = findApplication(application.getName());
        if (applicationStored == null) {
            applicationList.add(application);
            notifyInclusion(application);
        } else if (!application.getDiggest().equals(applicationStored.getDiggest())) {
            applicationList.remove(applicationStored);
            applicationList.add(application);
            notifyModification(application);
        }
    }

    @Override
    public void delete(String applicationName) throws ApplicationPersistenceException {
        notifyRemoval(findApplication(applicationName));
        applicationList.remove(findApplication(applicationName));
    }

    @Override
    public JSONObject fetch(String applicationName) throws ApplicationPersistenceException {
        return findApplication(applicationName).getContent();
    }

    @Override
    public Collection<Application> list() {
        return applicationList;
    }

    @Override
    public void registerServiceAvailabilityListener(ApplicationAvailabilityListener listenerClient) {
        synchronized (this.listener) {
            this.listener.add(listenerClient);
        }
    }

    @Override
    public void unregisterServiceAvailabilityListener(ApplicationAvailabilityListener listenerClient) {
        synchronized (this.listener) {
            this.listener.remove(listenerClient);
        }
    }

    private void notifyInclusion(Application application) {
        try {
            LOG.info("Notifying application '{}' deployment ", application.getName());
            for (ApplicationAvailabilityListener list : new HashSet<ApplicationAvailabilityListener>(listener)) {
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
        try {
            LOG.info("Notifying application '{}' deployment ", application.getName());
            for (ApplicationAvailabilityListener list : new HashSet<ApplicationAvailabilityListener>(listener)) {
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
        try {
            LOG.info("Notifying application '{}' deployment ", application.getName());
            for (ApplicationAvailabilityListener list : new HashSet<ApplicationAvailabilityListener>(listener)) {
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
        for (ApplicationAvailabilityListener list : new HashSet<ApplicationAvailabilityListener>(listener)) {
            try {
                list.serviceOffline();
            } catch (Exception e) {
                LOG.error("Memory Persistence service is going offline", e);
            }
        }
    }

    private void notifyServiceAvailable() {
        LOG.debug("Memory Persistence service is going online");
        for (ApplicationAvailabilityListener list : new HashSet<ApplicationAvailabilityListener>(listener)) {
            try {
                list.serviceOnline();
            } catch (Exception e) {
                LOG.error("Memory Persistence service is going online", e);
            }
        }
    }

    @Override
    public void run() {
        notifyServiceAvailable();
    }
}
