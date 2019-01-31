/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.generic.model;

public class Resource {

	private final Service service;
	private final String resourceId;
	private Object value;

	public Resource(Service service, String name) {
		this(service, name, "");
	}

	public Resource(Service service, String resourceId, Object value) {
		this.service = service;
		this.resourceId = resourceId;
		this.value = value;
	}

	public Service getService() {
		return service;
	}
	
	public Provider getProvider() {
		return service.getProvider();
	}
	
	public String getId() {
		return resourceId;
	}

	public Object getValue() {
		return value;
	}

	public boolean setValue(Object newValue) {
		if (value == null) {
			if (newValue == null) {
				return false;
			} else {
				value = newValue;
				return true;
			}
		} else {
			if (value.equals(newValue)) {
				return false;
			} else {
				value = newValue;
				return true;
			}
		}
	}
	
	@Override
	public String toString() {
		return "value=" + value;
	}
}
