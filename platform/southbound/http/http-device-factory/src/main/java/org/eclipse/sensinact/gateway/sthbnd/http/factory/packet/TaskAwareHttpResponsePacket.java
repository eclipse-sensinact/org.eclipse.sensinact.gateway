package org.eclipse.sensinact.gateway.sthbnd.http.factory.packet;

import org.eclipse.sensinact.gateway.sthbnd.http.HttpConnectionConfiguration;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpResponse;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpResponsePacket;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpTask;
import org.eclipse.sensinact.gateway.sthbnd.http.task.config.MappingDescription;

public class TaskAwareHttpResponsePacket extends HttpResponsePacket {

	private final MappingDescription[] mapping;
	
	public TaskAwareHttpResponsePacket(HttpResponse response) {
		super(response, false, false);
		
		HttpConnectionConfiguration<?, ?> configuration = response.getConfiguration();
		if(configuration instanceof HttpTask<?, ?>) {
			HttpTask<?, ?> httpTask = (HttpTask<?,?>)configuration;
			mapping = httpTask.getMapping();
		} else {
			mapping = new MappingDescription[0];
		}
	}

	public MappingDescription[] getMapping() {
		return mapping;
	}

}
