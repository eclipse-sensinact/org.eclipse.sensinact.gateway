package org.eclipse.sensinact.prototype;

import java.util.List;

public interface SensiNactSessionManager {
	
	SensiNactSession getDefaultSession(String userToken);
	
	SensiNactSession getSession(String sessionId);
	
	List<String> getSessionIds(String userToken);
	
	SensiNactSession createNewSession(String userToken);

}
