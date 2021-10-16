/**
 * 
 */
package org.eclipse.sensinact.gateway.sthbnd.http.task.config;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 *
 */
public class HttpProtocolStackEndpointTasksDescription {

	@JsonProperty(value="standalone")
	private HttpTasksDescription standalone;

	@JsonProperty(value="chained")
	private ChainedHttpTasksDescription chained;

	public HttpProtocolStackEndpointTasksDescription() {
	}
	
	public HttpProtocolStackEndpointTasksDescription(HttpTasksDescription standalone, ChainedHttpTasksDescription chained) {
		this.standalone = standalone ;
		this.chained = chained;
	}

	/**
	 * @return the standalone
	 */
	public HttpTasksDescription getStandalone() {
		return standalone;
	}

	/**
	 * @param standalone the standalone to set
	 */
	public void setStandalone(HttpTasksDescription standalone) {
		this.standalone = standalone;
	}

	/**
	 * @return the chained
	 */
	public ChainedHttpTasksDescription getChained() {
		return chained;
	}

	/**
	 * @param chained the chained to set
	 */
	public void setChained(ChainedHttpTasksDescription chained) {
		this.chained = chained;
	}

}
