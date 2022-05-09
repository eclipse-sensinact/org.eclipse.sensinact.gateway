/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.http.task.config;

import java.util.Collections;
import java.util.List;

import org.eclipse.sensinact.gateway.generic.Task.CommandType;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.RecurrentChainedHttpTask;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpChainedTasks;
import org.eclipse.sensinact.gateway.sthbnd.http.task.JSONHttpChainedTasks;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class RecurrentChainedHttpTaskDescription {

    public static RecurrentChainedHttpTaskDescription toDescription(RecurrentChainedHttpTask recurrentChainedHttpTask) {
    	RecurrentChainedHttpTaskDescription description = new RecurrentChainedHttpTaskDescription();
    	description.setChain(HttpChildTaskConfigurationDescription.toDescription(recurrentChainedHttpTask.chain()));
    	description.setChaining(recurrentChainedHttpTask.chaining());
    	description.setCommand(recurrentChainedHttpTask.command());
    	description.setConfiguration(HttpTaskConfigurationDescription.toDescription(recurrentChainedHttpTask.configuration()));
    	description.setDelay(recurrentChainedHttpTask.delay());
    	description.setPeriod(recurrentChainedHttpTask.period());
    	description.setTimeout(recurrentChainedHttpTask.timeout());
		return description;
    }
    
	@JsonProperty(value="chaining")
	private Class<? extends HttpChainedTasks> chaining;

	@JsonProperty(value="chain")
	private List<HttpChildTaskConfigurationDescription> chain;

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
	
	public RecurrentChainedHttpTaskDescription() {}
	
	public RecurrentChainedHttpTaskDescription(CommandType command, 
		Class<? extends HttpChainedTasks> chaining, HttpTaskConfigurationDescription configuration,
		List<HttpChildTaskConfigurationDescription> chain,
		long period, long delay, long timeout) {
		this.command = command;
		this.chaining = chaining;
		this.configuration = configuration;
		if(chain != null)
			this.chain = Collections.unmodifiableList(chain);
		this.period = period;
		this.delay = delay;
		this.timeout = timeout;
	}
	
	/**
	 * @return the chaining
	 */
	public Class<? extends HttpChainedTasks> getChaining() {
    	if(this.chaining == null)
    		return JSONHttpChainedTasks.class;
		return chaining;
	}

	/**
	 * @param chaining the chaining to set
	 */
	public void setChaining(Class<? extends HttpChainedTasks> chaining) {
		this.chaining = chaining;
	}

	/**
	 * @return the chain
	 */
	public List<HttpChildTaskConfigurationDescription> getChain() {
		if(this.chain == null)
			return Collections.<HttpChildTaskConfigurationDescription>emptyList();
		return chain;
	}

	/**
	 * @param chain the chain to set
	 */
	public void setChain(List<HttpChildTaskConfigurationDescription> chain) {
		if(chain != null)
			this.chain = Collections.unmodifiableList(chain);
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
			return RecurrentHttpTaskDescription.DEFAULT_PERIOD;
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
			return RecurrentHttpTaskDescription.DEFAULT_DELAY;
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
			return RecurrentHttpTaskDescription.DEFAULT_TIMEOUT;
		return timeout;
	}

	/**
	 * @param timeout the timeout to set
	 */
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}
}
