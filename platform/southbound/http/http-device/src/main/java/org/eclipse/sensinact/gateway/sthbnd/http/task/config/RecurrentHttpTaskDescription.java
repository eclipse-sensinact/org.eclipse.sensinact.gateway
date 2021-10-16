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
package org.eclipse.sensinact.gateway.sthbnd.http.task.config;

import org.eclipse.sensinact.gateway.generic.Task.CommandType;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.RecurrentHttpTask;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class RecurrentHttpTaskDescription {

   public static RecurrentHttpTaskDescription toDescription(RecurrentHttpTask recurrentHttpTask) {
    	RecurrentHttpTaskDescription description = new RecurrentHttpTaskDescription();
    	description.setCommand(recurrentHttpTask.command());
    	description.setConfiguration(HttpTaskConfigurationDescription.toDescription(recurrentHttpTask.recurrence()));
    	description.setDelay(recurrentHttpTask.delay());
    	description.setPeriod(recurrentHttpTask.period());
    	description.setTimeout(recurrentHttpTask.timeout());
    	return description;
    }
    
    
	static final long DEFAULT_DELAY = 1000;
	static final long DEFAULT_PERIOD = 60*1000;
	static final long DEFAULT_TIMEOUT = -1;

	@JsonProperty(value="command")
	private CommandType command;
	
	@JsonProperty(value="configuration")
	private HttpTaskConfigurationDescription configuration;

	@JsonProperty(value="period")
	private long period;
	
	@JsonProperty(value="delay")
	private long delay;
	
	@JsonProperty(value="timeout")
	private long timeout;

	public RecurrentHttpTaskDescription() {}
	
	public RecurrentHttpTaskDescription(CommandType command, HttpTaskConfigurationDescription configuration, 
		long period, long delay, long timeout) {
		this.command = command;
		this.configuration = configuration;
		this.period = period;
		this.delay = delay;
		this.timeout = timeout;
	}

	/**
	 * @return the commands
	 */
	public CommandType getCommand() {
    	if(this.command == null)
    		return CommandType.GET;
		return command;
	}

	/**
	 * @param commands the commands to set
	 */
	public void setCommand(CommandType command) {
		this.command = command;
	}

	/**
	 * @return the configuration
	 */
	public HttpTaskConfigurationDescription getConfiguration() {
		return configuration;
	}

	/**
	 * @param configuration the configuration to set
	 */
	public void setConfiguration(HttpTaskConfigurationDescription configuration) {
		this.configuration = configuration;
	}

	/**
	 * @return the period
	 */
	public long getPeriod() {
		if(period <= 0)
			return DEFAULT_PERIOD;
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
		if(delay <= 0)
			return DEFAULT_DELAY;
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
		if(timeout <= 0)
			return DEFAULT_TIMEOUT;
		return timeout;
	}

	/**
	 * @param timeout the timeout to set
	 */
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}
}
