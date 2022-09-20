package org.eclipse.sensinact.prototype.command.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.eclipse.sensinact.prototype.command.AbstractSensinactCommand;
import org.eclipse.sensinact.prototype.command.SensinactModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.service.typedevent.TypedEventBus;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

@ExtendWith(MockitoExtension.class)
public class GatewayThreadImplTest {

	@Mock
	TypedEventBus eventBus;
	
	GatewayThreadImpl thread;
	
	@BeforeEach
	void setup() {
		thread = new GatewayThreadImpl();
		thread.typedEventBus = eventBus;
		thread.activate();
	}
	
	@AfterEach
	void teardown() {
		thread.deactivate();
	}
	
	@Test
	void testExecute() throws Exception {
		Semaphore sem = new Semaphore(0);
		AbstractSensinactCommand<Integer> command = new AbstractSensinactCommand<Integer>() {

			@Override
			protected Promise<Integer> call(SensinactModel model, PromiseFactory promiseFactory) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				return promiseFactory.resolved(5);
			}
		};
		
		Promise<Integer> result = thread.execute(command).onResolve(sem::release);
		
		assertFalse(result.isDone());
		assertTrue(sem.tryAcquire(200, TimeUnit.MILLISECONDS));
		
		assertEquals(5, result.getValue());
	}

}
