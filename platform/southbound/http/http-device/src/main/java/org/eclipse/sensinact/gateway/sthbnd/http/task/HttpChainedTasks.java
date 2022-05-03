/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.http.task;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.sensinact.gateway.core.ResourceConfig;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.generic.TaskTranslator;
import org.eclipse.sensinact.gateway.protocol.http.client.Request;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpPacket;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.sthbnd.http.SimpleHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public abstract class HttpChainedTasks<REQUEST extends Request<SimpleHttpResponse>, 
HTTP_CHAINED_TASK extends HttpChainedTask<REQUEST>> extends HttpTaskImpl<SimpleHttpResponse, REQUEST> {
	
	private static final Logger LOG = LoggerFactory.getLogger(HttpChainedTasks.class);
    /**
     * @param type
     * @param path
     * @param resourceConfig
     * @param parameters
     * @return
     */
    public abstract HTTP_CHAINED_TASK createChainedTask(String identifier, CommandType type, String path, String profileId, ResourceConfig resourceConfig, Object[] parameters);

    /**
     * @param type
     * @param path
     * @param resourceConfig
     * @param parameters
     * @return
     */
    protected abstract byte[] getResultBytes();

    /**
     * the {@link Deque} of the chained tasks of this
     * HttpChainedTasks
     */
    protected Deque<HTTP_CHAINED_TASK> chain;

    /**
     * The intermediate result object of the chained
     * tasks execution
     */
    protected Object intermediateResult;

    /**
     * @param mediator
     * @param transmitter
     * @param path
     * @param resourceConfig
     * @param parameters
     */
    public HttpChainedTasks(CommandType command, TaskTranslator transmitter, Class<REQUEST> requestType, String path, String profileId, ResourceConfig resourceConfig, Object[] parameters) {
        super(command, transmitter, requestType, path, profileId, resourceConfig, parameters);
        this.chain = new LinkedList<HTTP_CHAINED_TASK>();
    }

    /**
     * @inheritDoc
     * @see HttpTask#execute()
     */
    @Override
    public void execute() {
        Iterator<HTTP_CHAINED_TASK> iterator = this.chain.iterator();

        HTTP_CHAINED_TASK task = null;

        while (iterator.hasNext()) {
            task = iterator.next();
            long wait = task.getTimeout();
            try {
                task.execute(this.getIntermediateResult());
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error(e.getMessage(), e);
            }
            task.execute();
            while (!task.isResultAvailable() && wait > 0) {
                try {
                    Thread.sleep(150);
                    wait -= 150;
                } catch (InterruptedException e) {
                    Thread.interrupted();
                    LOG.error(e.getMessage(), e);
                    break;
                }
            }
            if (!task.isResultAvailable()) {
                task.abort(AccessMethod.EMPTY);
            }
            this.intermediateResult = task.getResult();
        }
        if (task != null) {
            this.intermediateResult = task.getResult();

        } else {
            super.abort(AccessMethod.EMPTY);
            return;
        }
        byte[] bytes = this.getResultBytes();

        HttpPacket packet = null;
        Class<? extends HttpPacket> packetType = (this.packetType != null) ? this.packetType : HttpPacket.class;
        try {
            packet = packetType.getConstructor(Map.class, byte[].class).newInstance(this.getHeaders(), bytes);

            ((HttpProtocolStackEndpoint) transmitter).process(packet);

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * @param type
     * @param path
     * @param resourceConfig
     * @param parameters
     * @param linker
     * @return
     */
    public HTTP_CHAINED_TASK addChainedTask(String identifier) {
        return this.addChainedTask(this.createChainedTask(identifier, this.getCommand(), super.getPath(), super.getProfile(), super.getResourceConfig(), super.getParameters()));
    }

    /**
     * @param type
     * @param path
     * @param resourceConfig
     * @param parameters
     * @param linker
     * @return
     */
    public HTTP_CHAINED_TASK addChainedTask(HTTP_CHAINED_TASK task) {
        task.addHeaders(this.getHeaders());
        if (this.chain.add(task)) {
            return task;
        }
        return null;
    }

    /**
     * @inheritDoc
     * @see HttpTask#isDirect()
     */
    @Override
    public boolean isDirect() {
        return false;
    }

    /**
     * Returns the intermediate result object of the
     * chained tasks execution
     *
     * @return the intermediate result object of the
     * chained tasks execution
     */
    public Object getIntermediateResult() {
        return this.intermediateResult;
    }
}
