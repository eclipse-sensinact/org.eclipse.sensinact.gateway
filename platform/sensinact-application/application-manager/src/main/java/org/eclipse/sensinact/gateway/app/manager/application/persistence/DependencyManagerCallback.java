package org.eclipse.sensinact.gateway.app.manager.application.persistence;

/**
 * Callback used to indicate if the all the dependencies all a given application were all satisfied
 */
public interface DependencyManagerCallback {

    /**
     * All Dependencies are satisfied
     * @param applicationName
     */
    void ready(String applicationName);

    /**
     * Some Dependencies are NOT satisfied
     * @param applicationName
     */
    void unready(String applicationName);

}
