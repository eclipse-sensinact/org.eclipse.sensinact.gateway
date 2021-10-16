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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.sensinact.gateway.core.ResourceConfig;
import org.eclipse.sensinact.gateway.generic.Task.CommandType;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.ChainedHttpTask;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpChainedTasks;
import org.eclipse.sensinact.gateway.sthbnd.http.task.JSONHttpChainedTasks;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ChainedHttpTaskDescription {

    public static ChainedHttpTaskDescription toDescription(ChainedHttpTask chainedHttpTask) {
    	ChainedHttpTaskDescription description = new ChainedHttpTaskDescription();
    	description.setChain(HttpChildTaskConfigurationDescription.toDescription(chainedHttpTask.chain()));
    	description.setChaining(chainedHttpTask.chaining());
    	description.setCommands(Arrays.asList(chainedHttpTask.commands()));
    	description.setConfiguration(HttpTaskConfigurationDescription.toDescription(chainedHttpTask.configuration()));
    	description.setProfile(chainedHttpTask.profile());
    	return description;
    }
    
	@JsonProperty(value="profile")
	private String profile;
	
	@JsonProperty(value="commands")
	private List<CommandType> commands;
	
	@JsonProperty(value="chaining")
	private Class<? extends HttpChainedTasks> chaining;

	@JsonProperty(value="configuration")
	private HttpTaskConfigurationDescription configuration;

	@JsonProperty(value="chain")
	private List<HttpChildTaskConfigurationDescription> chain;
	
	public ChainedHttpTaskDescription() {}
	
	public ChainedHttpTaskDescription(String profile,List<CommandType> commands,
		Class<? extends HttpChainedTasks> chaining, HttpTaskConfigurationDescription configuration,
		List<HttpChildTaskConfigurationDescription> chain) {
		this.profile = profile;
		if(commands != null)
			this.commands = Collections.unmodifiableList(commands);
		this.chaining = chaining;
		this.configuration = configuration;
		if(chain != null)
			this.chain = Collections.unmodifiableList(chain);
	}

	/**
	 * @return the profile
	 */
	public String getProfile() {
    	if(this.profile == null)
    		return ResourceConfig.ALL_PROFILES;
		return profile;
	}

	/**
	 * @param profile the profile to set
	 */
	public void setProfile(String profile) {
		this.profile = profile;
	}

	/**
	 * @return the commands
	 */
	public List<CommandType> getCommands() {
    	if(this.commands == null)
    		return Arrays.asList(CommandType.GET);
		return commands;
	}

	/**
	 * @param commands the commands to set
	 */
	public void setCommands(List<CommandType> commands) {
		if(commands != null)
			this.commands = Collections.unmodifiableList(commands);
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
}
