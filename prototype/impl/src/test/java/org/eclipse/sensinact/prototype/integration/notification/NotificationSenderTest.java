package org.eclipse.sensinact.prototype.integration.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.sensinact.prototype.PrototypePush;
import org.eclipse.sensinact.prototype.SensiNactSession;
import org.eclipse.sensinact.prototype.SensiNactSessionManager;
import org.eclipse.sensinact.prototype.generic.dto.GenericDto;
import org.eclipse.sensinact.prototype.notification.ResourceDataNotification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.service.ServiceExtension;

@ExtendWith(ServiceExtension.class)
class SubscribeTest {

	private static final String USER = "user";
	
	
	private static final String MODEL = "model";
	private static final String PROVIDER = "provider";
	private static final String PROVIDER_TOPIC = PROVIDER + "/*";
	private static final String SERVICE = "service";
	private static final String RESOURCE = "resource";
	private static final Integer VALUE = 42;
	private static final Integer VALUE_2 = 84;
	
	@InjectService
	SensiNactSessionManager sessionManager;
	
	@InjectService
	PrototypePush push;
	
	@AfterEach
	void stop() {
		SensiNactSession session = sessionManager.getDefaultSession(USER);
		session.activeListeners().keySet().forEach(session::removeListener);
	}
	
	/**
	 * Show that data updates result in events received by subscribers
	 * @throws Exception
	 */
	@Test
	void basicSubscribe() throws Exception {
		
		BlockingQueue<ResourceDataNotification> queue = new ArrayBlockingQueue<>(32);
		
		SensiNactSession session = sessionManager.getDefaultSession(USER);
		session.addListener(List.of(PROVIDER_TOPIC), (t,e) -> queue.offer(e), null, null, null);
		
		
		assertNull(queue.poll(500, TimeUnit.MILLISECONDS));
		
		GenericDto dto = new GenericDto();
		dto.model = MODEL;
		dto.provider = PROVIDER;
		dto.service = SERVICE;
		dto.resource = RESOURCE;
		dto.value = VALUE;
		dto.type = Integer.class;
		
		push.pushUpdate(dto);
		
		ResourceDataNotification notification = queue.poll(1, TimeUnit.SECONDS);
		
		assertNotNull(notification);
		
		assertEquals(PROVIDER, notification.provider);
		assertEquals(SERVICE, notification.service);
		assertEquals(RESOURCE, notification.resource);
		assertEquals(null, notification.oldValue);
		assertEquals(VALUE, notification.newValue);
		
		assertNull(queue.poll(500, TimeUnit.MILLISECONDS));
		
		dto.value = VALUE_2;
		
		push.pushUpdate(dto);
		
		notification = queue.poll(1, TimeUnit.SECONDS);
		
		assertNotNull(notification);
		
		assertEquals(PROVIDER, notification.provider);
		assertEquals(SERVICE, notification.service);
		assertEquals(RESOURCE, notification.resource);
		assertEquals(VALUE, notification.oldValue);
		assertEquals(VALUE_2, notification.newValue);
		
		assertNull(queue.poll(500, TimeUnit.MILLISECONDS));
		
	}
}
