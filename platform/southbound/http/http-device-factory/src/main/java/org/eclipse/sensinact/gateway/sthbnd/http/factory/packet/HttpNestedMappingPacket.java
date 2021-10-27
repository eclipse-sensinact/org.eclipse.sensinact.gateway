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

import java.io.File;
import java.io.FileReader;
//import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.sensinact.gateway.generic.packet.annotation.AttributeID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.Data;
import org.eclipse.sensinact.gateway.generic.packet.annotation.Iteration;
import org.eclipse.sensinact.gateway.generic.packet.annotation.MetadataID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ResourceID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ServiceID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ServiceProviderID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.Timestamp;
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
	
	private static final boolean SAVE_CONTENT_INTO_TEMPORARY_FILE = true;
	
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
		super(response, SAVE_CONTENT_INTO_TEMPORARY_FILE, false);
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
			super.jsonMapping.put(nmd.getPath(), tmp.keySet().stream().collect(
					ArrayList::new,
					(l,s)->{
						if(!s.startsWith("$concat("))
							l.add(s);
					}, 
					List::addAll));
		}
	}	

	protected void initParsing() {
		try {
			FileReader reader = new FileReader(new File(super.savedContent));
			//InputStreamReader reader = new InputStreamReader(super.getInputStream());
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
	}

	protected Map<String,String> getEvent () {
		super.serviceProviderMapping = null;
		Map<String,String> event = null;
		while(true) {
			event = this.listener.getEvent(super.jsonMapping);
			this.listener.countDown();
			if(event!=null || this.listener.isEndOfParsing())
				break;
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				Thread.interrupted();
			}
		}
		if(this.listener.isEndOfParsing()) {
			this.worker.shutdownNow();
			this.worker = null;
			this.parser.close();
			this.parser = null;
			if(super.savedContent!=null)
				new File(super.savedContent).delete();
		}
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

	@Timestamp
	public long getTimestamp() {
		return super.getTimestamp();
	}
}
