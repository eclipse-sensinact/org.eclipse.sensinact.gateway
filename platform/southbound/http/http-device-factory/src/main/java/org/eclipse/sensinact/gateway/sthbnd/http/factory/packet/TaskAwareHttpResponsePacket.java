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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.eclipse.sensinact.gateway.sthbnd.http.HttpConnectionConfiguration;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpResponse;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpResponsePacket;
import org.eclipse.sensinact.gateway.sthbnd.http.factory.endpoint.HttpMappingProtocolStackConnectorCustomizer;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpTask;
import org.eclipse.sensinact.gateway.sthbnd.http.task.config.MappingDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskAwareHttpResponsePacket extends HttpResponsePacket {

	private static final Logger LOG = LoggerFactory.getLogger(HttpMappingProtocolStackConnectorCustomizer.class);
	
	private final MappingDescription[] mapping;
	private final String rawContentType;
	private final Charset charset;
	
	public TaskAwareHttpResponsePacket(HttpResponse response) {
		super(response, false, false);
		
		HttpConnectionConfiguration<?, ?> configuration = response.getConfiguration();
		if(configuration instanceof HttpTask<?, ?>) {
			HttpTask<?, ?> httpTask = (HttpTask<?,?>)configuration;
			mapping = httpTask.getMapping();
		} else {
			mapping = new MappingDescription[0];
		}
		
		String contentType = getHeaderAsString("Content-Type");
		Charset charset = StandardCharsets.UTF_8;
		
		if(contentType != null) {
			int idx = contentType.indexOf(';');
			if(idx >=0) {
				int charsetStartIdx = contentType.indexOf("charset=", idx) + 8;
				if(charsetStartIdx >= 0) {
					charsetStartIdx += 8;
					int charsetStopIdx;
					if(contentType.charAt(charsetStartIdx) == '"') {
						charsetStartIdx += 1;
						charsetStopIdx = contentType.indexOf('"', charsetStartIdx);
					} else {
						charsetStopIdx = contentType.indexOf(':', charsetStartIdx);
					}
					if(charsetStopIdx < 0) {
						charsetStopIdx = contentType.length();
					}
					try {
						charset = Charset.forName(contentType.substring(charsetStartIdx, charsetStopIdx));
					} catch (Exception e) {
						LOG.error("Unable to determine the charset for content type {}. Using UTF-8", contentType, e);
					}
				}
				contentType = contentType.substring(0, idx);
			}
			if(LOG.isDebugEnabled()) {
				LOG.debug("Determined Content-Type to be {}", contentType);
			}
		}
		
		rawContentType = contentType == null || contentType.isEmpty() ? null : contentType.toLowerCase();
		this.charset = charset;
	}

	public MappingDescription[] getMapping() {
		return mapping;
	}

	
	public String getRawContentType() {
		return rawContentType;
	}
	
	public BufferedReader getReader() {
		InputStream is = getInputStream();
		if(is == null) {
			is = new ByteArrayInputStream(getBytes());
		}
		return new BufferedReader(new InputStreamReader(is, charset));
	}
}
