package org.eclipse.sensinact.prototype.command.impl;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.sensinact.prototype.command.AbstractSensinactCommand;
import org.eclipse.sensinact.prototype.command.GatewayThread;
import org.eclipse.sensinact.prototype.notification.NotificationAccumulator;
import org.eclipse.sensinact.prototype.notification.impl.NotificationAccumulatorImpl;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.typedevent.TypedEventBus;
import org.osgi.util.promise.Deferred;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

//TODO decide if this is the right level to be a component
@Component
public class GatewayThreadImpl extends Thread implements GatewayThread {

	@Reference
	TypedEventBus typedEventBus;
	
	// TODO decide if we should just use an infinite queue
	private final BlockingQueue<WorkItem<?>> work = new ArrayBlockingQueue<>(4096);
	
	private final AtomicBoolean run = new AtomicBoolean(true);
	
	private final PromiseFactory promiseFactory = new PromiseFactory(null, null);
	
	@Activate
	void activate() {
		start();
	}
	
	@Deactivate
	void deactivate() {
		run.set(false);
		interrupt();
		try {
			join(500);
		} catch (InterruptedException e) {
			// Just keep going and reset our interrupt status
			Thread.currentThread().interrupt();
		}
	}
	
	@Override
	public PromiseFactory getPromiseFactory() {
		return promiseFactory;
	}

	@Override
	public NotificationAccumulator createAccumulator() {
		return new NotificationAccumulatorImpl(typedEventBus);
	}

	@Override
	public <T> Promise<T> execute(AbstractSensinactCommand<T> command) {
		Deferred<T> d = getPromiseFactory().deferred();
		work.add(new WorkItem<>(d, command));
		return d.getPromise();
	}

	@Override
	public void run() {
		while(run.get()) {
			try {
				work.take().doWork();
			} catch (InterruptedException e) {
				continue;
			}
		}
	}
	
	private static class WorkItem<T> {
		private final Deferred<T> d;
		private final AbstractSensinactCommand<T> command;
		
		public WorkItem(Deferred<T> d, AbstractSensinactCommand<T> command) {
			this.d = d;
			this.command = command;
		}

		void doWork() {
			try {
				SensinactModelImpl modelImpl = new SensinactModelImpl(command.getAccumulator());
				Promise<T> promise;
				try {
					promise = command.call(modelImpl);
				} finally {
					modelImpl.invalidate();
				}
				d.resolveWith(promise);
			} catch (Exception e) {
				d.fail(e);
			}
		}
	}
}
