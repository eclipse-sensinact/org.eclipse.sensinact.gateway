package org.eclipse.sensinact.prototype.command;

import static org.eclipse.sensinact.prototype.command.GatewayThread.getGatewayThread;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.eclipse.sensinact.prototype.notification.NotificationAccumulator;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

public abstract class AbstractSensinactCommand<T> {

	private NotificationAccumulator accumulator;
	
	private final AtomicBoolean canRun = new AtomicBoolean(true);
	
	protected AbstractSensinactCommand() {
	}
	
	protected AbstractSensinactCommand(NotificationAccumulator accumulator) {
		this.accumulator = accumulator;
	}
	
	public final Promise<T> call(SensinactModel model) throws Exception {
		GatewayThread gateway = getGatewayThread();
		if(canRun.getAndSet(false)) {
			PromiseFactory promiseFactory = gateway.getPromiseFactory();
			return call(model, promiseFactory)
					.onResolve(getAccumulator()::completeAndSend);
		} else {
			throw new IllegalStateException("Commands can only be executed once");
		}
	}

	public NotificationAccumulator getAccumulator() {
		if(accumulator == null) {
			accumulator = getGatewayThread().createAccumulator();
		}
		return accumulator;
	}
	
	protected abstract Promise<T> call(SensinactModel model, PromiseFactory promiseFactory);
	
	protected static <R> Promise<R> safeCall(AbstractSensinactCommand<R> command, 
			SensinactModel model, PromiseFactory pf) {
		try {
			return command.call(model, pf);
		} catch (Exception e) {
			return pf.failed(e);
		}
	}
	
	protected static <R> Promise<R> safeCall(Supplier<Promise<R>> supplier, PromiseFactory pf) {
		try {
			return supplier.get();
		} catch (Exception e) {
			return pf.failed(e);
		}
	}
}
