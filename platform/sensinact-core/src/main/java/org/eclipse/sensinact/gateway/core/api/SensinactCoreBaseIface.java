package org.eclipse.sensinact.gateway.core.api;

import org.eclipse.sensinact.gateway.common.constraint.Constraint;
import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.json.JSONObject;

import java.util.Set;

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
    public String subscribe(String provider, String service, String resource,Recipient recipient, Set<Constraint> conditions, String policy);
    public String set(String requestId, final String serviceProviderId, final String serviceId,
                      final String resourceId, final String attributeId, final String parameter);
    public String act(String requestId, final String serviceProviderId, final String serviceId,
                      final String resourceId, final String parameters);

}
