package org.eclipse.sensinact.gateway.core.remote;

public interface SensinactCoreBaseIface {

    public String namespace();
    public String getAll(String identifier, final String filter);
    public String getProviders(String identifier, String filter);
    public String getProvider(String identifier, final String serviceProviderId);
    public String getServices(String identifier, final String serviceProviderId);
    public String getService(String identifier, final String serviceProviderId,final String serviceId);
    public String getResources(String identifier, final String serviceProviderId, final String serviceId);
    public String getResource(String identifier, final String serviceProviderId, final String serviceId,final String resourceId);
    public String get(String identifier, final String serviceProviderId, final String serviceId,
                          final String resourceId, final String attributeId);
    public String set(String identifier,final String serviceProviderId, final String serviceId,
                      final String resourceId, final String attributeId, final String parameter);
    public String act(String identifier, final String serviceProviderId, final String serviceId,
                      final String resourceId, final String parameters);

}
