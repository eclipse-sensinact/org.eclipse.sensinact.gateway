/**
 * 
 */
package org.eclipse.sensinact.gateway.generic;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public abstract class AbstractSubscribeTaskWrapper implements SubscribeTaskWrapper {
	
	private static final Logger LOG = LoggerFactory.getLogger(AbstractSubscribeTaskWrapper.class);
	/**
	 * The wrapped subscribe access method {@link Task}
	 */
	protected Task task;

	/**
	 * The String identifier of the subscription done by the wrapped {@link Task}
	 */
	protected String subscriptionId;

	/**
	 * the {@link Mediator} allowing the SubscribeTaskWrapper to be 
	 * instantiated to interact with the OSGi host environment  
	 */
	protected Mediator mediator;

	protected ProtocolStackEndpoint<?> endpoint;

	/**
	 * Constructor
	 * 
	 * Do not use it !
	 */
	private AbstractSubscribeTaskWrapper() {
		throw new IllegalArgumentException();
	}
	
	/**
	 * Constructor
	 * 
	 * @param task the {@link Task} subscribe access method
	 */
	protected AbstractSubscribeTaskWrapper(Mediator mediator, Task task, ProtocolStackEndpoint<?> endpoint) {
		if(!CommandType.SUBSCRIBE.equals(task.getCommand()))
			throw new IllegalArgumentException("Subscribe Task expected");
		this.task = task;
		this.mediator = mediator;
		this.endpoint = endpoint;
	}

	@Override
	public void setSubscriptionId(String subscriptionId) {
		this.subscriptionId = subscriptionId;
		this.endpoint.registerSubsciptionIdentifier(this);
	}
	
	@Override
	public String getSubscriptionId() {
		return this.subscriptionId;
	}

	@Override
	public String getTargetId() {
		try {
			return this.targetIdExtractor().execute(this.task);
		} catch (Exception e) {
			LOG.error("Error when extracting the subscriber identifier", e);
		}
		return null;
	}
	
	@Override
	public String getJSON() {
		return task.getJSON();
	}

	@Override
	public RequestType getRequestType() {
		return task.getRequestType();
	}

	@Override
	public CommandType getCommand() {
		return task.getCommand();
	}

	@Override
	public boolean isDirect() {
		return task.isDirect();
	}

	@Override
	public LifecycleStatus getLifecycleStatus() {
		return task.getLifecycleStatus();
	}

	@Override
	public String getTaskIdentifier() {
		return task.getTaskIdentifier();
	}

	@Override
	public void setTaskIdentifier(String taskIdentifier) {
		task.setTaskIdentifier(taskIdentifier);
	}

	@Override
	public ResourceConfig getResourceConfig() {
		return task.getResourceConfig();
	}

	@Override
	public Object[] getParameters() {
		return task.getParameters();
	}

	@Override
	public String getPath() {
		return task.getPath();
	}

	@Override
	public String getProfile() {
		return task.getProfile();
	}

	@Override
	public boolean isResultAvailable() {
		return task.isResultAvailable();
	}

	@Override
	public void setResult(Object result) {
		try {
			this.setSubscriptionId(this.subscriptionIdExtractor().execute(result));
		} catch (Exception e) {
			LOG.error("Error when extracting the subscription identifier", e);
		}
		task.setResult(result);
	}

	@Override
	public void setResult(Object result, long timestamp) {
		try {
			this.setSubscriptionId(this.subscriptionIdExtractor().execute(result));
		} catch (Exception e) {
			LOG.error("Error when extracting the subscription identifier", e);
		}
		task.setResult(result,timestamp);
	}

	@Override
	public long getTimestamp() {
		return task.getTimestamp();
	}

	@Override
	public Object getResult() {
		return task.getResult();
	}

	@Override
	public void execute() {
		((TaskImpl)this.task).setLifecycleStatus(LifecycleStatus.LAUNCHED);
        ((TaskImpl)this.task).launched = System.currentTimeMillis();
        ((TaskImpl)this.task).transmitter.send(this);
	}

	@Override
	public long getTimeout() {
		return task.getTimeout();
	}

	@Override
	public void setTimeout(long timeout) {
		task.setTimeout(timeout);
	}

	@Override
	public void abort(Object result) {
		task.abort(result);
	}

	@Override
	public void registerCallBack(TaskCallBack callback) {
		task.registerCallBack(callback);
	}
}
