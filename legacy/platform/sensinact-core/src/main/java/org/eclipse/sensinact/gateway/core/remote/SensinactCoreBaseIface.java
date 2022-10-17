 package org.eclipse.sensinact.gateway.core.remote;

public interface SensinactCoreBaseIface {

    String namespace();
    
    String getAll(String identifier, String filter);
    
    String getProviders(String identifier, String filter);
    
    String getProvider(String identifier, String serviceProviderId);
    
    String getServices(String identifier, String serviceProviderId);
    
    String getService(String identifier, String serviceProviderId,String serviceId);
    
    String getResources(String identifier, String serviceProviderId, String serviceId);
    
    String getResource(String identifier, String serviceProviderId, String serviceId,String resourceId);
    
    String get(String identifier, String serviceProviderId, String serviceId,
        String resourceId, String attributeId);
    
    String set(String identifier,String serviceProviderId, String serviceId,
        String resourceId, String attributeId, String parameter);
    
    String act(String identifier, String serviceProviderId, String serviceId,
         String resourceId, String parameters);
    
    boolean isAccessible(String identifier, String serviceProviderId, String serviceId,
            String resourceId);

}
