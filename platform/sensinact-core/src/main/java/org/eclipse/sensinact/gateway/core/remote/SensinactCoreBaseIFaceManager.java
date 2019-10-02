package org.eclipse.sensinact.gateway.core.remote;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;

public interface SensinactCoreBaseIFaceManager {

	public static final String EMPTY_NAMESPACE = "#LOCAL#";
	public static final String FILTER_MAIN = "org.eclipse.sensinact.gateway.core.api.SensinactCoreBaseIface";
	public static final String REMOTE_NAMESPACE_PROPERTY = "org.eclipse.sensinact.remote.namespace";
	
	String namespace();
    
    void start(Mediator mediator);
    
    void stop();
}
