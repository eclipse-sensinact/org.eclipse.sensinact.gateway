package org.eclipse.sensinact.gateway.core.api;

import org.eclipse.sensinact.gateway.core.AnonymousSession;
import org.json.JSONObject;

public interface Sensinact {

    /**
     * Returns this Core's String namespace. The namespace will be used to prefix
     * the identifiers of the service providers that are handled by this Core
     *
     * @return this Core's String namespace
     */
    public String namespace();
    public JSONObject getProvider(String identifier, final String serviceProviderId);
    public JSONObject getService(String identifier, final String serviceProviderId, final String serviceId);
    public String getProvidersLocal(String identifier, String filter);
    public AnonymousSession getAnonymousSession();
    public String getServices(String identifier, final String serviceProviderId);
    public String getResources(String identifier, final String serviceProviderId, final String serviceId);
    public String getAll(String identifier, final String filter,Boolean attacheNamespace);

}
