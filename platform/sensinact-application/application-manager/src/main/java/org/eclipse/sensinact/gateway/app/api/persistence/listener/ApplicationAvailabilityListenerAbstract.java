package org.eclipse.sensinact.gateway.app.api.persistence.listener;

public abstract class ApplicationAvailabilityListenerAbstract implements ApplicationAvailabilityListener {

    @Override
    public void serviceOffline() {

    }

    @Override
    public void serviceOnline() {

    }

    @Override
    public void applicationFound(String applicationName, String content) {

    }

    @Override
    public void applicationRemoved(String applicationName) {

    }
}
