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
public class HttpTasksDescription {

	@JsonProperty(value="tasks")
	private List<SimpleHttpTaskDescription> tasks;

	@JsonProperty(value="recurrences")
	private List<RecurrentHttpTaskDescription> recurrences;
	
	public HttpTasksDescription() {}	

	public HttpTasksDescription(List<SimpleHttpTaskDescription> tasks, List<RecurrentHttpTaskDescription> recurrences) {
		if(tasks != null)
			this.tasks = Collections.unmodifiableList(tasks);		
		if(recurrences != null)
			this.recurrences = Collections.unmodifiableList(recurrences);
	}

	/**
	 * @return the tasks
	 */
	public List<SimpleHttpTaskDescription> getTasks() {
		if(this.tasks == null)
			return Collections.<SimpleHttpTaskDescription>emptyList();
		return tasks;
	}

	/**
	 * @param tasks the tasks to set
	 */
	public void setTasks(List<SimpleHttpTaskDescription> tasks) {
		if(tasks != null)
			this.tasks = Collections.unmodifiableList(tasks);
	}

	/**
	 * @return the recurrences
	 */
	public List<RecurrentHttpTaskDescription> getRecurrences() {
		if(this.recurrences == null)
			return Collections.<RecurrentHttpTaskDescription>emptyList();
		return recurrences;
	}

	/**
	 * @param recurrences the recurrences to set
	 */
	public void setRecurrences(List<RecurrentHttpTaskDescription> recurrences) {
		if(recurrences != null)
			this.recurrences = Collections.unmodifiableList(recurrences);
	}	
	
}
