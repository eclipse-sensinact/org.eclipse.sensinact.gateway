/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.http.openweather.osgi;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;
import org.eclipse.sensinact.gateway.generic.ExtModelConfigurationBuilder;
import org.eclipse.sensinact.gateway.generic.InvalidProtocolStackException;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpPacket;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.ChainedHttpTasks;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.HttpChildTaskConfiguration;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.HttpTaskConfiguration;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.KeyValuePair;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.RecurrentChainedHttpTask;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.DefaultHttpChainedTaskProcessingContext;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.DefaultHttpChainedTaskProcessingContextFactory;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpActivator;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpChainedTaskProcessingContextFactory;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpTaskConfigurator;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpTaskProcessingContext;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.SimpleHttpProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpChainedTask;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpChainedTasks;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;
import org.osgi.annotation.bundle.Header;
import org.osgi.framework.Constants;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsonp.JSONPModule;

import jakarta.json.JsonArray;

@ChainedHttpTasks(recurrences = {
	@RecurrentChainedHttpTask(
		delay = 1000, 
		period = 1000 * 60 * 5, 
		configuration = @HttpTaskConfiguration(host = HttpChildTaskConfiguration.DEFAULT_HOST), 
		chain = {
			@HttpChildTaskConfiguration(
			    host = "api.openweathermap.org", 
			    path = "/data/2.5/weather", 
			    identifier = "weather", 
			    query = {
			    	@KeyValuePair(key = "units", value= "metric"),
			    	@KeyValuePair(key = "lat", value = "$(org.eclipse.sensinact.gateway.weather.latitude.@context[endpoint.id])"), 
					@KeyValuePair(key = "lon", value = "$(org.eclipse.sensinact.gateway.weather.longitude.@context[endpoint.id])"), 
					@KeyValuePair(key = "APPID", value = "$(openweather-token)")
			    }
			), 
			@HttpChildTaskConfiguration(
				host = "openweathermap.org", 
				path = "/img/w/@context[weather.icon].png", 
				identifier = "icon", 
				query = {
					@KeyValuePair(key = "APPID", value = "$(openweather-token)")
				}
			)
		}
	)
})
@Header(name = Constants.BUNDLE_ACTIVATOR, value = "${@class}")
public class Activator extends HttpActivator {
	
	private final ObjectMapper mapper = JsonMapper.builder()
    		.addModule(new JSONPModule(JsonProviderFactory.getProvider()))
    		.build();
	
    List<SimpleHttpProtocolStackEndpoint> endpoints;

    @Override
    public void doStart() throws Exception {
        super.mediator.setTaskProcessingContextHandler(this.getProcessingContextHandler());

        this.mediator.setTaskProcessingContextFactory(this.getTaskProcessingContextFactory());

        this.mediator.setChainedTaskProcessingContextFactory(this.getChainedTaskProcessingContextFactory());

        ExtModelConfiguration<? extends HttpPacket> configuration = 
        	ExtModelConfigurationBuilder.instance(mediator, getPacketType()
        ).withStartAtInitializationTime(isStartingAtInitializationTime()
        ).withServiceBuildPolicy(getServiceBuildPolicy()
        ).withResourceBuildPolicy(getResourceBuildPolicy()
        ).build(getResourceDescriptionFile(), getDefaults());

        endpoints = new ArrayList<SimpleHttpProtocolStackEndpoint>();

        String stationsList = (String) mediator.getProperty("org.eclipse.sensinact.gateway.weather.stations");

        String[] stations = stationsList.split(",");

        int index = 0;
        int length = stations == null ? 0 : stations.length;

        for (; index < length; index++) {
            String station = stations[index];

            SimpleHttpProtocolStackEndpoint endpoint = this.configureProtocolStackEndpoint();
            endpoint.setEndpointIdentifier(station);
            endpoint.connect(configuration);
        }
    }

    @Override
    public void doStop() throws Exception {
        while (!endpoints.isEmpty()) {
            endpoints.remove(0).stop();
        }
    }

    @Override
    public Class<? extends HttpPacket> getPacketType() {
        return HttpPacket.class;
    }

    @Override
    public HttpChainedTaskProcessingContextFactory getChainedTaskProcessingContextFactory() {
        return new DefaultHttpChainedTaskProcessingContextFactory(mediator) {
           
            @Override
            public <CHAINED extends HttpChainedTask<?>> HttpTaskProcessingContext newInstance(HttpTaskConfigurator httpTaskConfigurator, String endpointId, HttpChainedTasks<?, CHAINED> tasks, CHAINED task) {
                return new OpenWeatherTaskProcessingContext(Activator.this.mediator, httpTaskConfigurator, endpointId, tasks, task);
            }

        };
    }

    /**
     * Extended {@link DefaultHttpChainedTaskProcessingContext} dedicated
     * OpenWeather tasks processing context
     *
     * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
     */
    private class OpenWeatherTaskProcessingContext extends DefaultHttpChainedTaskProcessingContext {
        
        public <CHAINED extends HttpChainedTask<?>> OpenWeatherTaskProcessingContext(Mediator mediator, HttpTaskConfigurator httpTaskConfigurator, final String endpointId, final HttpChainedTasks<?, CHAINED> tasks, final CHAINED task) {
            super(mediator, httpTaskConfigurator, endpointId, tasks, task);
            super.properties.put("weather.icon", new Executable<Void, String>() {
                @Override
                public String execute(Void parameter) throws Exception {
                    Object intermediate = tasks.getIntermediateResult();
                    if (intermediate == null) {
                        return null;
                    }
                    String result = intermediate.toString();
                    String icon = mapper.readValue(result, JsonArray.class)
                    	.getJsonObject(0).getJsonObject("weather").getJsonArray("weather")
                    	.getJsonObject(0).getString("icon");
                    return icon;
                }
            });
        }

    }

    @Override
    protected void connect(ExtModelConfiguration configuration) throws InvalidProtocolStackException {
    }

}