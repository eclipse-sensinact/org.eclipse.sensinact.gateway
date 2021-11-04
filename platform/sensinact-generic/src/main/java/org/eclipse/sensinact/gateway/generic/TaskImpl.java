/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.generic;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.ResourceConfig;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.util.JSONUtils;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A generic {@link Task} implementation
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class TaskImpl implements Task {
	
	private static final Logger LOG = LoggerFactory.getLogger(TaskImpl.class);
    protected final Object lock = new Object();

    protected final ResourceConfig resourceConfig;
    protected final Object[] parameters;
    protected final TaskTranslator transmitter;
    protected final String path;
    protected final String profileId;
    protected String taskIdentifier;
    protected Object result;
    protected long launched;
    protected long executed;

    protected long timestamp;

    protected final AtomicBoolean available;
    protected final AtomicLong timeout;

    protected LifecycleStatus status;
    private Deque<TaskCallBack> callbacks;
    protected CommandType command;

    /**
     * Constructor
     *
     * @param mediator       the associated {@link Mediator}
     * @param transmitter    the {@link TaskTranslator} in charge of sending the requests
     *                       based on the task to instantiate
     * @param path           String path of the {@link SnaObject} which has created the task
     *                       to instantiate
     * @param resourceConfig the {@link ResourceConfig} mapped to the {@link ExtResourceImpl}
     *                       on which the task applies
     * @param parameters     the objects array parameterizing the call
     */
    public TaskImpl(CommandType command, TaskTranslator transmitter, String path, String profileId, ResourceConfig resourceConfig, Object[] parameters) {
        this.available = new AtomicBoolean(false);
        this.transmitter = transmitter;
        this.path = path;
        this.profileId = profileId;
        this.resourceConfig = resourceConfig;
        this.parameters = parameters;
        this.command = command;

        String serviceProviderId = null;
        String service = null;
        String resource = null;
        String attribute = null;

        String[] pathElements = UriUtils.getUriElements(path);
        switch (pathElements.length) {
            case 4:
                attribute = pathElements[3];
            case 3:
                resource = pathElements[2];
            case 2:
                service = pathElements[1];
            case 1:
                serviceProviderId = pathElements[0];
            default:
                break;
        }
        this.timeout = new AtomicLong(Task.DEFAULT_TIMEOUT);
        this.status = LifecycleStatus.INITIALIZED;
        this.callbacks = new LinkedList<TaskCallBack>();
        if (serviceProviderId != null) {
            StringBuilder buffer = new StringBuilder();
            buffer.append(serviceProviderId);
            buffer.append(TaskManager.IDENTIFIER_SEP_CHAR);
            buffer.append(this.getCommand().name());

            if (service != null) {
                buffer.append(TaskManager.IDENTIFIER_SEP_CHAR);
                buffer.append(service);
            }
            if (resource != null) {
                buffer.append(TaskManager.IDENTIFIER_SEP_CHAR);
                buffer.append(resource);

            }
            if (attribute != null) {
                buffer.append(TaskManager.IDENTIFIER_SEP_CHAR);
                buffer.append(attribute);
            }
            this.setTaskIdentifier(buffer.toString());
        }
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.generic.Task#getCommand()
     */
    @Override
    public CommandType getCommand() {
        return this.command;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.generic.Task#isDirect()
     */
    @Override
    public boolean isDirect() {
        return false;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.generic.Task#getResourceConfig()
     */
    @Override
    public ResourceConfig getResourceConfig() {
        return this.resourceConfig;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.generic.Task#getParameters()
     */
    @Override
    public Object[] getParameters() {
        return this.parameters;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.generic.Task#getPath()
     */
    @Override
    public String getPath() {
        return this.path;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.generic.Task#getProfile()
     */
    @Override
    public String getProfile() {
        return this.profileId;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.generic.Task#getLifecycleStatus()
     */
    @Override
    public LifecycleStatus getLifecycleStatus() {
        LifecycleStatus status = null;
        synchronized (this.status) {
            status = this.status;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(new StringBuilder("Task status ").append(status).append("[").append(System.currentTimeMillis()).append("]").toString());
        }
        return status;
    }

    /**
     * Defines the current life cycle status value
     *
     * @param status the current {@link LifecycleStatus} to set
     */
    protected void setLifecycleStatus(LifecycleStatus status) {
        synchronized (this.status) {
            this.status = status;
        }
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.generic.Task#setTaskIdentifier(java.lang.String)
     */
    public void setTaskIdentifier(String taskIdentifier) {
        this.taskIdentifier = taskIdentifier;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.generic.Task#getTaskIdentifier()
     */
    public String getTaskIdentifier() {
        return this.taskIdentifier;
    }

    /**
     * @InheriDoc
     * @see org.eclipse.sensinact.gateway.generic.Task#setResult(java.lang.Object)
     */
    @Override
    public void setResult(Object result) {
        this.setResult(result, System.currentTimeMillis());
    }

    /**
     * @InheriDoc
     * @see org.eclipse.sensinact.gateway.generic.Task#setResult(java.lang.Object, long)
     */
    @Override
    public void setResult(Object result, long timestamp) {
        if (isResultAvailable()) {
            if (LOG.isWarnEnabled()) 
                LOG.warn(new StringBuilder("result already set [").append(this).append("][current: ").append((this.result == AccessMethod.EMPTY ? "EMPTY" : this.result)).append("][new = ").append((result == AccessMethod.EMPTY ? "EMPTY" : result + "]")).toString());
            return;
        }
        this.result = result;
        this.timestamp = timestamp;
        this.setLifecycleStatus(LifecycleStatus.EXECUTED);
        this.executed = System.currentTimeMillis();

        synchronized (this.lock) {
            this.available.set(true);
        }
        while (!this.callbacks.isEmpty()) {
            callbacks.pop().callback(this);
        }
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.generic.Task#abort(java.lang.Object)
     */
    public void abort(Object result) {
        this.result = result;
        this.setLifecycleStatus(LifecycleStatus.ABORDED);
        this.executed = System.currentTimeMillis();

        synchronized (this.lock) {
            this.available.set(true);
        }
        while (!this.callbacks.isEmpty()) {
            callbacks.pop().callback(this);
        }
    }

    /**
     * @InheriDoc
     * @see org.eclipse.sensinact.gateway.generic.Task#getResult()
     */
    @Override
    public Object getResult() {
        if (!isResultAvailable()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("result is not available");
            }
            return null;
        }
        return this.result;
    }

    /**
     * @InheriDoc
     * @see org.eclipse.sensinact.gateway.generic.Task#getTimestamp()
     */
    @Override
    public long getTimestamp() {
        if (!isResultAvailable()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("result is not available");
            }
            return -1;
        }
        return this.timestamp;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.generic.Task#isResultAvailable()
     */
    @Override
    public boolean isResultAvailable() {
        boolean available;
        synchronized (this.lock) {
            available = this.available.get();
        }
        return available;
    }

    /**
     * Returns the delay of execution of this
     * task
     *
     * @return the delay of execution of this
     * task
     */
    public long getExecutionDelay() {
        if (getLifecycleStatus() != LifecycleStatus.EXECUTED) {
            return -1;
        }
        return this.executed - this.launched;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.generic.Task#getTimeout()
     */
    public long getTimeout() {
        return this.timeout.longValue();
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.generic.Task#setTimeout(long)
     */
    public void setTimeout(long timeout) {
        this.timeout.set(timeout);
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.generic.Task#registerCallBack(org.eclipse.sensinact.gateway.generic.TaskCallBack)
     */
    public void registerCallBack(TaskCallBack callback) {
        if (callback == null) {
            return;
        }
        synchronized (this.lock) {
            if (this.available.get()) {
                callback.callback(this);

            } else {
                this.callbacks.offer(callback);
            }
        }
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.generic.Task#execute()
     */
    @Override
    public void execute() {
        this.setLifecycleStatus(LifecycleStatus.LAUNCHED);
        this.launched = System.currentTimeMillis();
        this.transmitter.send(this);
    }

    /**
     * @InheritDoc
     * @see org.eclipse.sensinact.gateway.common.primitive.JSONable#getJSON()
     */
    public String getJSON() {
        if (!this.isResultAvailable()) {
            return JSONUtils.EMPTY;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(JSONUtils.OPEN_BRACE);
        builder.append(JSONUtils.QUOTE);
        builder.append("uri");
        builder.append(JSONUtils.QUOTE);
        builder.append(JSONUtils.COLON);
        builder.append(JSONUtils.QUOTE);
        builder.append(this.path);
        builder.append(JSONUtils.QUOTE);
        builder.append(JSONUtils.COMMA);
        builder.append(JSONUtils.QUOTE);
        builder.append("task");
        builder.append(JSONUtils.QUOTE);
        builder.append(JSONUtils.COLON);
        builder.append(JSONUtils.QUOTE);
        builder.append(this.getCommand().name());
        builder.append(JSONUtils.QUOTE);
        builder.append(JSONUtils.COMMA);
        builder.append(JSONUtils.QUOTE);
        builder.append("start");
        builder.append(JSONUtils.QUOTE);
        builder.append(JSONUtils.COLON);
        builder.append(this.launched);
        builder.append(JSONUtils.COMMA);
        builder.append(JSONUtils.QUOTE);
        builder.append("end");
        builder.append(JSONUtils.QUOTE);
        builder.append(JSONUtils.COLON);
        builder.append(this.executed);
        builder.append(JSONUtils.COMMA);
        builder.append(JSONUtils.QUOTE);
        builder.append("status");
        builder.append(JSONUtils.QUOTE);
        builder.append(JSONUtils.COLON);
        builder.append(JSONUtils.QUOTE);
        builder.append(this.status.name());
        builder.append(JSONUtils.QUOTE);
        if (!LifecycleStatus.ABORDED.equals(this.status)) {
            builder.append(JSONUtils.COMMA);
            builder.append(JSONUtils.QUOTE);
            builder.append("result");
            builder.append(JSONUtils.QUOTE);
            builder.append(JSONUtils.COLON);
            builder.append(JSONUtils.toJSONFormat(this.result));
        }
        builder.append(JSONUtils.CLOSE_BRACE);
        return builder.toString();
    }
}
