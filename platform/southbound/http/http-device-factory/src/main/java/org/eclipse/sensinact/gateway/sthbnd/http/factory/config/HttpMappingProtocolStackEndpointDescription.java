/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.http.factory.config;

import java.util.Collections;
import java.util.Map;

import org.eclipse.sensinact.gateway.common.primitive.Modifiable;
import org.eclipse.sensinact.gateway.core.SensiNactResourceModelConfiguration.BuildPolicy;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpPacket;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpProtocolStackEndpoint;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * HttpProtocolStackEndpointDescription gathers the minimal set of properties allowing to
 * configure an {@link HttpProtocolStackEndpoint}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HttpMappingProtocolStackEndpointDescription {

	@JsonProperty(value="startAtInitializationTime")
	private boolean startAtInitializationTime;

	private boolean startAtInitializationTimeSet;

	@JsonProperty(value="packetTypeName")
	private String packetTypeName;

	@JsonProperty(value="modifiable")
	private String modifiable;
	
	@JsonProperty(value="defaults")
	private Map<String,String> defaults;

	@JsonProperty(value="resourceBuildPolicy")
	private String[] resourceBuildPolicy;

	@JsonProperty(value="serviceBuildPolicy")
	private String[] serviceBuildPolicy;

	@JsonProperty(value="observed")
	private String[] observed;

	@JsonProperty(value="serviceProviderIdPattern")
	private String serviceProviderIdPattern;

	@JsonProperty(value="timestampPattern")
	private String timestampPattern;

	@JsonProperty(value="overrideResponseContentType")
	private String overrideResponseContentType;

	@JsonProperty(value="csvTitles")
	private boolean csvTitles;

	@JsonProperty(value="csvDelimiter")
	private Character csvDelimiter;
	
	@JsonProperty(value="csvNumberLocale")
	private String csvNumberLocale;

	@JsonProperty(value="csvMaxRows")
	private Integer csvMaxRows;
	
	public HttpMappingProtocolStackEndpointDescription() {}
	
	public HttpMappingProtocolStackEndpointDescription(
			boolean startAtInitializationTime,
			String packetTypeName, 
			String endpointTypeName, 
			String modifiable,
			Map<String,String> defaults,
			String[] resourceBuildPolicy, 
			String[] serviceBuildPolicy,
			String serviceProviderIdPattern,
			String timestampPattern,
			String defaultResponseContentType,
			boolean csvTitles) {
		this.startAtInitializationTime = startAtInitializationTime;
		this.startAtInitializationTimeSet = true;
		this.packetTypeName = packetTypeName;
		this.modifiable = modifiable;
		this.defaults = defaults;
		this.resourceBuildPolicy = resourceBuildPolicy;
		this.serviceBuildPolicy = serviceBuildPolicy;
		this.serviceProviderIdPattern = serviceProviderIdPattern;
		this.timestampPattern = timestampPattern;
		this.overrideResponseContentType = defaultResponseContentType;
		this.csvTitles = csvTitles;
	}

	/**
	 * @return the startAtInitializationTime
	 */
	public boolean isStartAtInitializationTime() {
		if(!this.startAtInitializationTimeSet)
			return true;
		return startAtInitializationTime;
	}

	/**
	 * @param startAtInitializationTime the startAtInitializationTime to set
	 */
	public void setStartAtInitializationTime(boolean startAtInitializationTime) {
		this.startAtInitializationTime = startAtInitializationTime;
		this.startAtInitializationTimeSet = true;
	}

	/**
	 * @return the packetTypeName
	 */
	public String getPacketTypeName() {
		if(this.packetTypeName == null)
			return HttpPacket.class.getCanonicalName();
		return this.packetTypeName;
	}

	/**
	 * @param packetTypeName the packetTypeName to set
	 */
	public void setPacketTypeName(String packetTypeName) {
		this.packetTypeName = packetTypeName;
	}

	/**
	 * @return the modifiable
	 */
	public String getModifiable() {
		if(this.modifiable == null) 
			return Modifiable.MODIFIABLE.name();
		return modifiable;
	}

	/**
	 * @param modifiable the modifiable to set
	 */
	public void setModifiable(String modifiable) {
		this.modifiable = modifiable;
	}

	/**
	 * @return the defaults
	 */
	public Map<String,String> getDefaults() {
		if(this.defaults == null)
			return Collections.emptyMap();
		return defaults;
	}

	/**
	 * @param defaults the defaults to set
	 */
	public void setDefaults(Map<String,String> defaults) {
		this.defaults = defaults;
	}

	/**
	 * @return the resourceBuildPolicy
	 */
	public String[] getResourceBuildPolicy() {
		if(this.resourceBuildPolicy == null)
	         return new String[] { BuildPolicy.BUILD_COMPLETE_ON_DESCRIPTION.name()};
		return resourceBuildPolicy;
	}

	/**
	 * @param resourceBuildPolicy the resourceBuildPolicy to set
	 */
	public void setResourceBuildPolicy(String[] resourceBuildPolicy) {
		this.resourceBuildPolicy = resourceBuildPolicy;
	}

	/**
	 * @return the serviceBuildPolicy
	 */
	public String[] getServiceBuildPolicy() {
		if(this.serviceBuildPolicy == null)
	         return new String[] { BuildPolicy.BUILD_APPEARING_ON_DESCRIPTION.name(), 
	        	BuildPolicy.BUILD_COMPLETE_ON_DESCRIPTION.name()};
		return serviceBuildPolicy;
	}

	/**
	 * @param serviceBuildPolicy the serviceBuildPolicy to set
	 */
	public void setServiceBuildPolicy(String[] serviceBuildPolicy) {
		this.serviceBuildPolicy = serviceBuildPolicy;
	}
	
	/**
	 * @return the serviceBuildPolicy
	 */
	public String[] getObserved() {
		if(this.observed == null)
			return new String[0];
		return observed;
	}
	
	/**
	 * @param serviceBuildPolicy the serviceBuildPolicy to set
	 */
	public void setObserved(String[] observed) {
		this.observed = observed;
	}

	/**
	 * @return the serviceProviderIdPattern
	 */
	public String getServiceProviderIdPattern() {
		return serviceProviderIdPattern;
	}

	/**
	 * @param serviceProviderIdPattern the serviceProviderIdPattern to set
	 */
	public void setServiceProviderIdPattern(String serviceProviderIdPattern) {
		this.serviceProviderIdPattern = serviceProviderIdPattern;
	}

	/**
	 * @return the timestampPattern
	 */
	public String getTimestampPattern() {
		return timestampPattern;
	}

	/**
	 * @param timestampPattern the timestampPattern to set
	 */
	public void setTimestampPattern(String timestampPattern) {
		this.timestampPattern = timestampPattern;
	}
	
	/**
	 * @return the defaultResponseContentType
	 */
	public String getOverrideResponseContentType() {
		return overrideResponseContentType;
	}
	
	/**
	 * @param defaultResponseContentType the defaultResponseContentType to set
	 */
	public void setOverrideResponseContentType(String overrideResponseContentType) {
		this.overrideResponseContentType = overrideResponseContentType;
	}

	/**
	 * @return the csvTitles
	 */
	public boolean getCsvTitles() {
		return csvTitles;
	}

	/**
	 * @param csvTitles the csvTitles to set
	 */
	public void setCsvTitles(boolean csvTitles) {
		this.csvTitles = csvTitles;
	}

	/**
	 * @return the csvDelimiter
	 */
	public char getCsvDelimiter() {
		return csvDelimiter == null ? ',' : csvDelimiter.charValue();
	}
	
	/**
	 * @param csvTitles the csvTitles to set
	 */
	public void setCsvDelimiter(Character csvDelimiter) {
		this.csvDelimiter = csvDelimiter;
	}

	/**
	 * @return the csvLocale
	 */
	public String getCsvNumberLocale() {
		return csvNumberLocale;
	}
	
	/**
	 * @param csvTitles the csvTitles to set
	 */
	public void setCsvNumberLocale(String csvNumberLocale) {
		this.csvNumberLocale = csvNumberLocale;
	}

	/**
	 * @return the csvLocale
	 */
	public Integer getCsvMaxRows() {
		return csvMaxRows;
	}
	
	/**
	 * @param csvTitles the csvTitles to set
	 */
	public void setCsvMaxRows(Integer csvMaxRows) {
		this.csvMaxRows = csvMaxRows;
	}
}
