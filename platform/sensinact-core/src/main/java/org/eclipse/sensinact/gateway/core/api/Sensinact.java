package org.eclipse.sensinact.gateway.core.api;

public interface Sensinact {

    /**
     * Returns this Core's String namespace. The namespace will be used to prefix
     * the identifiers of the service providers that are handled by this Core
     *
     * @return this Core's String namespace
     */
    public String namespace();
    public String getProvidersLocal(String identifier, String filter);

}
