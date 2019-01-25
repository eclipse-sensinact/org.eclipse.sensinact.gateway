package org.eclipse.sensinact.gateway.device.openhab.common;

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
