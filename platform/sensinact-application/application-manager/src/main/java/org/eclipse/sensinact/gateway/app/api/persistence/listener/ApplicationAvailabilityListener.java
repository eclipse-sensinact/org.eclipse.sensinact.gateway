package org.eclipse.sensinact.gateway.app.api.persistence.listener;

public interface ApplicationAvailabilityListener {
    void serviceOffline();

    void serviceOnline();

    void applicationFound(String applicationName, String content);

    void applicationChanged(String applicationName, String content);

    void applicationRemoved(String applicationName);
}
