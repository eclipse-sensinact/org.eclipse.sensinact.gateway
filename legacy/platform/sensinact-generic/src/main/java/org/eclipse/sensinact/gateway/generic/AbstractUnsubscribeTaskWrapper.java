/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.generic;

import org.eclipse.sensinact.gateway.core.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public abstract class AbstractUnsubscribeTaskWrapper implements UnsubscribeTaskWrapper {
	
	private static final Logger LOG = LoggerFactory.getLogger(AbstractUnsubscribeTaskWrapper.class);
	/**
	 * The wrapped subscribe access method {@link Task}
	 */
	protected Task task;

	/**
	 * The String identifier of the subscription to be undone
	 */
	protected String subscriptionId;

	protected ProtocolStackEndpoint<?> endpoint;

	/**
	 * Constructor
	 * 
	 * Do not use it !
	 */
	private AbstractUnsubscribeTaskWrapper() {
		throw new IllegalArgumentException();
	}
	
	/**
	 * Constructor
	 * 
	 * @param task the {@link Task} subscribe access method
	 */
	protected AbstractUnsubscribeTaskWrapper(Task task, ProtocolStackEndpoint<?> endpoint) {
		if(!CommandType.UNSUBSCRIBE.equals(task.getCommand()))
			throw new IllegalArgumentException("Unsubscribe Task expected");
		this.task = task;
		this.endpoint = endpoint;
	}

	public void setSubscriptionId(String subscriptionId) {
		this.subscriptionId = subscriptionId;
	}

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
		Object[] parameters = task.getParameters();
		if(parameters == null || parameters.length==0) 
			return new Object[] {subscriptionId};
		Object[] ps = new Object[parameters.length+1];
		System.arraycopy(parameters, 0, ps, 0, parameters.length);
		ps[parameters.length]=subscriptionId;
		return ps;
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
		task.setResult(result);
	}

	@Override
	public void setResult(Object result, long timestamp) {
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
		endpoint.setSubsciptionIdentifier(this);
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
