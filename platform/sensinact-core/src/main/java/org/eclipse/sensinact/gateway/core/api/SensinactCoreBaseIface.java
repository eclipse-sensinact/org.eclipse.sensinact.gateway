package org.eclipse.sensinact.gateway.core.api;

import org.json.JSONObject;

public interface SensinactCoreBaseIface {

    public String namespace();
    public String getProviders(String identifier, String filter);
    public String getServices(String identifier, final String serviceProviderId);
    public String getResources(String identifier, final String serviceProviderId, final String serviceId);
    public String getProvider(String identifier, final String serviceProviderId);
    public String get(String identifier, final String serviceProviderId, final String serviceId,
                          final String resourceId, final String attributeId);

}
