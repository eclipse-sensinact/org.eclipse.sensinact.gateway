/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.http.factory.endpoint;

import static com.fasterxml.jackson.core.StreamReadFeature.AUTO_CLOSE_SOURCE;

import java.text.SimpleDateFormat;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.DefaultConnectorCustomizer;
import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.generic.packet.PacketReader;
import org.eclipse.sensinact.gateway.sthbnd.http.factory.config.HttpMappingProtocolStackEndpointDescription;
import org.eclipse.sensinact.gateway.sthbnd.http.factory.packet.TaskAwareHttpResponsePacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonFactoryBuilder;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HttpMappingProtocolStackConnectorCustomizer extends DefaultConnectorCustomizer<TaskAwareHttpResponsePacket> {

	private static final Logger LOG = LoggerFactory.getLogger(HttpMappingProtocolStackConnectorCustomizer.class);
	
	private final SimpleDateFormat timestampFormat;
	private final String serviceProviderIdPattern;
	private final String overrideResponseContentType;
	private final char csvDelimiterChar;
	private final boolean csvTitles;
	private final String csvNumberLocale;
	private final Integer csvMaxLines;
	private final MappingJsonFactory factory;
	
	public HttpMappingProtocolStackConnectorCustomizer(Mediator mediator,
			@SuppressWarnings("rawtypes") ExtModelConfiguration ExtModelConfiguration,
			HttpMappingProtocolStackEndpointDescription config) {
		super(mediator, ExtModelConfiguration);
		this.timestampFormat = config.getTimestampPattern() == null ? null : new SimpleDateFormat(config.getTimestampPattern());
		this.serviceProviderIdPattern = config.getServiceProviderIdPattern();
		this.overrideResponseContentType = config.getOverrideResponseContentType();
		this.csvDelimiterChar = config.getCsvDelimiter();
		this.csvNumberLocale = config.getCsvNumberLocale();
		this.csvTitles = config.getCsvTitles();
		this.csvMaxLines = config.getCsvMaxRows();
		JsonFactory jsonFactory = new JsonFactoryBuilder().configure(AUTO_CLOSE_SOURCE, true)
				.build();
		factory = new MappingJsonFactory(jsonFactory, new ObjectMapper(jsonFactory));
	}

	@Override
	public PacketReader<TaskAwareHttpResponsePacket> newPacketReader(TaskAwareHttpResponsePacket packet) throws InvalidPacketException {
		
		
		String contentType;
		
		if(overrideResponseContentType != null) {
			contentType = overrideResponseContentType;
			if(LOG.isDebugEnabled()) {
				LOG.debug("The content type of the response is overridden to be {}", contentType);
			}
		} else {
			contentType = packet.getRawContentType();
			if(contentType == null) {
				LOG.error("No Content-Type header in the response, and no override content type set");
				throw new InvalidPacketException("Unable to determine Content-Type"); 
			}
		}
		
		contentType = contentType.toLowerCase();
		
		PacketReader<TaskAwareHttpResponsePacket> reader;
		switch(contentType) {
			case "application/json":
				reader = new JsonPacketReader(timestampFormat, serviceProviderIdPattern, factory);
				break;
			case "text/csv":
				reader = new CsvPacketReader(timestampFormat, serviceProviderIdPattern, csvDelimiterChar, csvTitles, 
						csvNumberLocale, csvMaxLines);
				break;
			default:
				if(contentType.endsWith("+json")) {
					reader = new JsonPacketReader(timestampFormat, serviceProviderIdPattern, factory);
				} else {
					LOG.error("Unable to parse responses of type {}", contentType);
					throw new InvalidPacketException("Unable to parse content type " + contentType);
				}
				break;
		}
		reader.load(packet);
		return reader;
	}
}
