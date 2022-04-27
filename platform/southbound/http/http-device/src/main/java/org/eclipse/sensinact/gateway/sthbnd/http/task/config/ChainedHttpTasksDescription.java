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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ChainedHttpTasksDescription {
	
	@JsonProperty(value="tasks")
	private List<ChainedHttpTaskDescription> tasks;

	@JsonProperty(value="recurrences")
	private List<RecurrentChainedHttpTaskDescription> recurrences;
	
	public ChainedHttpTasksDescription(){}		
	
	public ChainedHttpTasksDescription(List<ChainedHttpTaskDescription> tasks, 
			List<RecurrentChainedHttpTaskDescription> recurrences){	
		if(tasks != null)
			this.tasks = Collections.unmodifiableList(tasks);
		if(recurrences != null)
			this.recurrences = Collections.unmodifiableList(recurrences);
	}
	
	public void setTasks(List<ChainedHttpTaskDescription> tasks) {
		if(tasks != null)
			this.tasks = Collections.unmodifiableList(tasks);
	}
	
    public List<ChainedHttpTaskDescription> getTasks() {
    	if(this.tasks==null)
    		return Collections.<ChainedHttpTaskDescription>emptyList();
    	return this.tasks;
    }
	
	public void setRecurrences(List<RecurrentChainedHttpTaskDescription> recurrences) {
		if(recurrences != null)
			this.recurrences = Collections.unmodifiableList(recurrences);
	}
	
    public List<RecurrentChainedHttpTaskDescription> getRecurrences() {
    	if(this.recurrences == null)
    		return Collections.<RecurrentChainedHttpTaskDescription>emptyList();
    	return this.recurrences;
    }
}
