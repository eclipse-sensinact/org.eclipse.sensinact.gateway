package org.eclipse.sensinact.prototype.command;

import org.eclipse.sensinact.prototype.notification.NotificationAccumulator;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

public interface GatewayThread {

	public PromiseFactory getPromiseFactory();
	
	public <T> Promise<T> execute(AbstractSensinactCommand<T> command);

	public NotificationAccumulator createAccumulator();
	
	public static GatewayThread getGatewayThread() {
		Thread currentThread = Thread.currentThread();
		if(currentThread instanceof GatewayThread) {
			return (GatewayThread) currentThread;
		} else {
			throw new IllegalStateException("Not running on the gateway thread");
		}
	}
}
