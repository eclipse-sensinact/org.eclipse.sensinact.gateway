/*
 * Copyright (c) 2021 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.sthbnd.http.factory.packet;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.eclipse.sensinact.gateway.generic.packet.annotation.AttributeID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.Data;
import org.eclipse.sensinact.gateway.generic.packet.annotation.Iteration;
import org.eclipse.sensinact.gateway.generic.packet.annotation.MetadataID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ResourceID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ServiceID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ServiceProviderID;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpResponse;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpResponsePacket;
import org.eclipse.sensinact.gateway.sthbnd.http.task.config.MappingDescription;
import org.eclipse.sensinact.gateway.sthbnd.http.task.config.NestedMappingDescription;
import org.eclipse.sensinact.gateway.util.json.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extended {@link HttpResponsePacket} allowing to map json
 * data structures to sensiNact's inner data model
 */
public class HttpNestedMappingPacket  extends HttpMappingPacket<NestedMappingDescription> {
	
	static final Logger LOG = LoggerFactory.getLogger(HttpNestedMappingPacket.class);
			
	protected JSONParser parser = null;	
	protected JSONParserListener listener = null;
	protected ExecutorService worker = null;

	/**
	 * Constructor
	 * 
	 * @param response the {@link HttpResponse} processed
	 * by the HttpMappingPacket to be initialized
	 */
	public HttpNestedMappingPacket(HttpResponse response) {
		super(response, false, false);
		this.worker = Executors.newSingleThreadExecutor(r -> new Thread(r, "Http Mapping Packet Worker Thread"));
	}	
	
	/**
	 * Defines the {@link MappingDescription}s for this HttpMappingPacket
	 * 
	 * @param mappings the {@link MappingDescription}s applying
	 */
	public void doSetMapping(NestedMappingDescription[] mappings) {
		for(int i = 0; i < mappings.length; i++) {
			NestedMappingDescription nmd = mappings[i];	
			Map<String,String> tmp = nmd.getMapping();
			super.modelMapping.putAll(tmp);
			super.jsonMapping.put(nmd.getPath(), tmp.keySet().stream().collect(Collectors.toList()));
		}
	}	

	protected void initParsing() {
		try {
			InputStreamReader reader = new InputStreamReader(super.getInputStream());
			this.parser = new JSONParser(reader);
		} catch (Exception e) {
			LOG.error(e.getMessage(),e);
			return;
		}
		this.listener = new JSONParserListener();
		final List<String> paths = this.jsonMapping.keySet().stream(
			).<List<String>>collect(ArrayList::new, 
			(l,s)->{l.add(s);},
			List::addAll);
		this.worker.submit(new Runnable() {
			@Override
			public void run() {
				try {
					parser.parse(paths, HttpNestedMappingPacket.this.listener);
				} catch(Exception e) {					
					LOG.error(e.getMessage(), e);
					HttpNestedMappingPacket.this.listener.handle(JSONParser.END_OF_PARSING);					
				}
			}			
		});
		this.resultMapping = getEvent();
	}

	protected Map<String,String> getEvent () {
		this.serviceProviderMapping = null;
		Map<String,String> event = this.listener.getEvent(this.jsonMapping);
		while(event == null && !this.listener.isEndOfParsing()) {
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				Thread.interrupted();
			}
			event = this.listener.getEvent(this.jsonMapping);
		}
		if(this.listener.isEndOfParsing()) {
			this.worker.shutdownNow();
			this.worker = null;
			this.parser.close();
			this.parser = null;
		}
		this.resultMapping = event;
		this.listener.countDown();
		if(this.resultMapping != null) 	
			this.serviceProviderMapping = reverseModelMapping(PROVIDER_PATTERN);
		return event;
	}

	@Iteration
	public boolean wasLast() {	
		return super.wasLast();
	}

	@ServiceProviderID
	public String getServiceProviderId() {
		return super.getServiceProviderId();
	}
	
	@ServiceID
	public String getServiceId() {
		return super.getServiceId();
	}
	
	@ResourceID
	public String getResourceId() {
		return super.getResourceId();
	}

	@AttributeID
	public String getAttributeId() {
		return super.getAttributeId();
	}

	@MetadataID
	public String getMetadataId() {
		return super.getMetadataId();
	}

	@Data
	public Object getData() {
		return super.getData();
	}

}
