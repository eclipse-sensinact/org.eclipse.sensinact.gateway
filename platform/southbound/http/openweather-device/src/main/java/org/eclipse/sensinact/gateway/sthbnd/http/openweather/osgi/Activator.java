/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.sthbnd.http.openweather.osgi;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;
import org.eclipse.sensinact.gateway.generic.ExtModelInstanceBuilder;
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
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

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
public class Activator extends HttpActivator {
    List<SimpleHttpProtocolStackEndpoint> endpoints;

    /**
     * @inheritDoc
     * @see AbstractActivator#doStart()
     */
    @Override
    public void doStart() throws Exception {
        super.mediator.setTaskProcessingContextHandler(this.getProcessingContextHandler());

        this.mediator.setTaskProcessingContextFactory(this.getTaskProcessingContextFactory());

        this.mediator.setChainedTaskProcessingContextFactory(this.getChainedTaskProcessingContextFactory());

        ExtModelConfiguration configuration = new ExtModelInstanceBuilder(mediator, getPacketType()).withStartAtInitializationTime(isStartingAtInitializationTime()).withServiceBuildPolicy(getServiceBuildPolicy()).withResourceBuildPolicy(getResourceBuildPolicy()).buildConfiguration(getResourceDescriptionFile(), getDefaults());

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

    /**
     * @inheritDoc
     * @see AbstractActivator#doStop()
     */
    @Override
    public void doStop() throws Exception {
        while (!endpoints.isEmpty()) {
            endpoints.remove(0).stop();
        }
    }

    /**
     * @inheritDoc
     * @see HttpActivator#getPacketType()
     */
    public Class<? extends HttpPacket> getPacketType() {
        return HttpPacket.class;
    }

    /**
     * @inheritDoc
     * @see HttpActivator#
     * getChainedTaskProcessingContextFactory()
     */
    public HttpChainedTaskProcessingContextFactory getChainedTaskProcessingContextFactory() {
        return new DefaultHttpChainedTaskProcessingContextFactory(mediator) {
            /**
             * @inheritDoc
             *
             * @see HttpChainedTaskProcessingContextFactory#newInstance(HttpChainedTasks, HttpChainedTask)
             */
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
        /**
         * @param mediator
         * @param task
         */
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
                    String icon = new JSONArray(result).optJSONObject(0).optJSONObject("weather").optJSONArray("weather").optJSONObject(0).getString("icon");
                    return icon;
                }
            });
        }

    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpActivator#connect(org.eclipse.sensinact.gateway.generic.ExtModelConfiguration)
     */
    protected void connect(ExtModelConfiguration configuration) throws InvalidProtocolStackException {
    }

}