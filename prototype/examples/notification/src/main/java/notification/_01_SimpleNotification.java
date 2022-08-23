package notification;

import org.eclipse.sensinact.prototype.notification.ResourceDataNotification;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.typedevent.TypedEventHandler;
import org.osgi.service.typedevent.propertytypes.EventTopics;

/**
 * Notified for all data events
 */
@Component
@EventTopics("DATA/*")
public class _01_SimpleNotification implements TypedEventHandler<ResourceDataNotification> {

	@Override
	public void notify(String topic, ResourceDataNotification event) {
		// TODO Auto-generated method stub
		
	}

}
