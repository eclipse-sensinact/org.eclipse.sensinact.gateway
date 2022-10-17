/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.http.smpl;

import org.eclipse.sensinact.gateway.generic.Task.CommandType;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpTask;
import org.eclipse.sensinact.gateway.sthbnd.http.task.config.HttpTaskConfigurationDescription;

class RecurrentTaskConfigurator extends SimpleTaskConfigurator 
implements RecurrentHttpTaskConfigurator {
    private long period = 1000 * 60;
    private long delay = 1000;
    private long timeout = -1;
    private Class<? extends HttpTask> taskType;

    public RecurrentTaskConfigurator(SimpleHttpProtocolStackEndpoint endpoint, CommandType command, 
    	HttpTaskUrlConfigurator urlBuilder, Class<? extends HttpTask> taskType, long period, long delay, 
    	long timeout, HttpTaskConfigurationDescription annotation) {
        super(endpoint, null, command, urlBuilder, annotation);
        this.taskType = taskType;
        this.period = period;
        this.delay = delay;
        this.timeout = timeout;
    }

    /**
     * @return the period
     */
    public long getPeriod() {
        return period;
    }

    /**
     * @param period the period to set
     */
    public void setPeriod(long period) {
        this.period = period;
    }

    /**
     * @return the delay
     */
    public long getDelay() {
        return delay;
    }

    /**
     * @param delay the delay to set
     */
    public void setDelay(long delay) {
        this.delay = delay;
    }

    /**
     * @return the timeout
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     * @param timeout the timeout to set
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /**
     * @return the taskType
     */
    public Class<? extends HttpTask> getTaskType() {
        return taskType;
    }

    /**
     * @param taskType the taskType to set
     */
    public void setTaskType(Class<? extends HttpTask> taskType) {
        this.taskType = taskType;
    }

}