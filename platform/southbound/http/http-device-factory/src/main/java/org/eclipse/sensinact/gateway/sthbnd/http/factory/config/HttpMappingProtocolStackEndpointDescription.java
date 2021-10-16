/**
 * 
 */
package org.eclipse.sensinact.gateway.sthbnd.http.factory.config;

import java.util.Collections;
import java.util.Map;

import org.eclipse.sensinact.gateway.common.primitive.Modifiable;
import org.eclipse.sensinact.gateway.core.SensiNactResourceModelConfiguration.BuildPolicy;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpPacket;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.sthbnd.http.factory.endpoint.HttpMappingProtocolStackEndpoint;

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

	@JsonProperty(value="endpointTypeName")
	private String endpointTypeName;

	@JsonProperty(value="modifiable")
	private String modifiable;
	
	@JsonProperty(value="defaults")
	private Map<String,String> defaults;

	@JsonProperty(value="resourceBuildPolicy")
	private String[] resourceBuildPolicy;

	@JsonProperty(value="serviceBuildPolicy")
	private String[] serviceBuildPolicy;
	
	public HttpMappingProtocolStackEndpointDescription() {}
	
	public HttpMappingProtocolStackEndpointDescription(
			boolean startAtInitializationTime,
			String packetTypeName, 
			String endpointTypeName, 
			String modifiable,
			Map<String,String> defaults,
			String[] resourceBuildPolicy, 
			String[] serviceBuildPolicy) {
		this.startAtInitializationTime = startAtInitializationTime;
		this.startAtInitializationTimeSet = true;
		this.packetTypeName = packetTypeName;
		this.endpointTypeName = endpointTypeName;
		this.modifiable = modifiable;
		this.defaults = defaults;
		this.resourceBuildPolicy = resourceBuildPolicy;
		this.serviceBuildPolicy = serviceBuildPolicy;
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
	 * @return the endpointTypeName
	 */
	public String getEndpointTypeName() {
		if(this.endpointTypeName == null)
			return HttpMappingProtocolStackEndpoint.class.getCanonicalName();
		return endpointTypeName;
	}

	/**
	 * @param endpointTypeName the endpointTypeName to set
	 */
	public void setEndpointTypeName(String endpointTypeName) {
		this.endpointTypeName = endpointTypeName;
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
}
