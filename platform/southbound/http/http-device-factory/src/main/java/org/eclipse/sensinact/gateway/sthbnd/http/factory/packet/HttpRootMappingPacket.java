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

import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.eclipse.sensinact.gateway.sthbnd.http.task.config.RootMappingDescription;
import org.eclipse.sensinact.gateway.util.json.JSONParser;
import org.eclipse.sensinact.gateway.util.json.JSONParser.Evaluation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extended {@link HttpResponsePacket} allowing to map json
 * data structures to sensiNact's inner data model
 */
public class HttpRootMappingPacket extends HttpMappingPacket<RootMappingDescription>{
	
	static final Logger LOG = LoggerFactory.getLogger(HttpRootMappingPacket.class);

	protected int pos = 0;
	protected String json= null;
	
	/**
	 * Constructor
	 * 
	 * @param response the {@link HttpResponse} processed
	 * by the HttpMappingPacket to be initialized
	 */
	public HttpRootMappingPacket(HttpResponse response) {
		super(response, false, true);
		byte[] bytes = super.getBytes();
		if(bytes != null && bytes.length > 0)
			this.json = new String(bytes);
	}	
	
	/**
	 * Defines the {@link MappingDescription}s for this HttpMappingPacket
	 * 
	 * @param mappings the {@link MappingDescription}s applying
	 */
	public void doSetMapping(RootMappingDescription[] mappings) {
		for(int i = 0; i < mappings.length; i++) {					
			RootMappingDescription rmd = mappings[i];;
			Map<String,String> tmp = rmd.getMapping();
			this.modelMapping.putAll(tmp);
			this.jsonMapping.put(String.format("root_%s",i), tmp.keySet().stream().collect(Collectors.toList()));
		}
	}	
	
	protected void initParsing() {
		getEvent();
	}	

	protected Map<String,String> getEvent () {
		int iteration = 0;
		Iterator<List<String>> it = jsonMapping.values().iterator();
		List<String> list = null;
		while(pos < jsonMapping.size() && iteration < pos) {
			it.next();
			iteration+=1;
		}
		list = it.next();
		if(list == null) {
			this.resultMapping = null;				
		} else {
			JSONParser parser = null;
			StringReader reader = null;
			try {
				reader = new StringReader(json);
				parser = new JSONParser(reader);
			} catch (Exception e) {
				LOG.error(e.getMessage(),e);
				return null;
			}			
			List<Evaluation> evaluations = null;
			try {
				evaluations = parser.parse(list);
				this.resultMapping = evaluations.stream().<Map<String,String>>collect(
					HashMap::new,
					(m,e)-> {m.put(e.path,e.result);},
					Map::putAll);
			}catch(Exception e) {
				LOG.error(e.getMessage(),e);
			} finally {
				parser.close();
				parser = null;
			}
		}
		pos++;
		this.serviceProviderMapping = null;
		if(this.resultMapping != null) 	
			this.serviceProviderMapping = reverseModelMapping(PROVIDER_PATTERN);
		return this.resultMapping;		
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
