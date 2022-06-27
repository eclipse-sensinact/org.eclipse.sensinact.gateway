package notification;

import java.util.List;

import org.eclipse.sensinact.prototype.SensiNactSession;
import org.eclipse.sensinact.prototype.notification.ResourceDataNotification;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.typedevent.propertytypes.EventTopics;

/**
 * Notified for all data events visible to the client
 */
@Component
@EventTopics("DATA/*")
public class _02_ClientNotification {

	// This is probably not how we should retrieve the session in real life!
	@Reference
	SensiNactSession session;
	
	
	@Activate
	void start() {
		// We ask for * but only events visible to the user will be seen
		session.addListener(List.of("*"), this::notify, null, null, null);
	}
	
	
	private void notify(String topic, ResourceDataNotification event) {
		// TODO Auto-generated method stub
		
	}

}
