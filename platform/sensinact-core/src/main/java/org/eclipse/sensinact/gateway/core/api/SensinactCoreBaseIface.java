package org.eclipse.sensinact.gateway.core.api;

public interface SensinactCoreBaseIface {

    public String namespace();
    public String getProviders(String identifier, String filter);

}
