/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.gateway.southbound.http.factory;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Authentication;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BasicAuthentication;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.sensinact.gateway.southbound.device.factory.DeviceFactoryException;
import org.eclipse.sensinact.gateway.southbound.device.factory.IDeviceMappingHandler;
import org.eclipse.sensinact.gateway.southbound.http.factory.ParsedHttpTask.KeyValue;
import org.eclipse.sensinact.gateway.southbound.http.factory.config.HttpDeviceFactoryConfiguration;
import org.eclipse.sensinact.gateway.southbound.http.factory.config.HttpDeviceFactoryConfigurationPeriodicDTO;
import org.eclipse.sensinact.gateway.southbound.http.factory.config.HttpDeviceFactoryConfigurationTaskDTO;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * HTTP device factory
 */
@Component(service = {}, configurationPid = "sensinact.http.device.factory", configurationPolicy = ConfigurationPolicy.REQUIRE)
public class HttpDeviceFactory {
    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(HttpDeviceFactory.class);

    /**
     * Device mapping service
     */
    @Reference
    private IDeviceMappingHandler mappingHandler;

    /**
     * HTTP client
     */
    @Reference
    private SharedHttpClientResources rcSharer;

    /**
     * Scheduled executor
     */
    private ScheduledThreadPoolExecutor scheduledExecutor;

    /**
     * Component activated
     *
     * @throws Exception Error starting HTTP client
     */
    @Activate
    void activate(final HttpDeviceFactoryConfiguration configuration) throws Exception {
        scheduledExecutor = new ScheduledThreadPoolExecutor(1);

        final ObjectMapper mapper = new ObjectMapper();

        final String rawOneShotTasks = configuration.tasks_oneshot();
        if (rawOneShotTasks != null && !rawOneShotTasks.isBlank()) {
            final HttpDeviceFactoryConfigurationTaskDTO[] oneShotTasks = mapper.readValue(rawOneShotTasks,
                    HttpDeviceFactoryConfigurationTaskDTO[].class);
            for (final HttpDeviceFactoryConfigurationTaskDTO task : oneShotTasks) {
                runTask(new ParsedHttpTask(task));
            }
        }

        final String rawPeriodicTasks = configuration.tasks_periodic();
        if (rawPeriodicTasks != null) {
            final HttpDeviceFactoryConfigurationPeriodicDTO[] periodicTasks = mapper.readValue(rawPeriodicTasks,
                    HttpDeviceFactoryConfigurationPeriodicDTO[].class);
            for (final HttpDeviceFactoryConfigurationPeriodicDTO rawTask : periodicTasks) {
                runScheduledTask(new ParsedHttpPeriodicTask(rawTask));
            }
        }
    }

    /**
     * Component deactivated
     *
     * @throws Exception Error stopping HTTP client
     */
    @Deactivate
    void deactivate() throws Exception {
        scheduledExecutor.shutdownNow();
        scheduledExecutor = null;
    }

    /**
     * Runs an HTTP task
     *
     * @param task Task to run
     */
    private void runTask(final ParsedHttpTask task) {
        try {
            // Client is already configured
            final HttpClient client = rcSharer.newClient(task);
            client.start();

            // Prepare request
            final Request request = client.newRequest(task.url);
            request.method(task.method);
            request.headers((headers) -> {
                for (final KeyValue<String, String> header : task.getHeaders()) {
                    headers.add(header.key(), header.value());
                }
            });

            if (task.authUser != null) {
                Authentication.Result authn = new BasicAuthentication.BasicResult(null, task.authUser,
                        task.authPassword);
                authn.apply(request);
            }

            request.send(new BufferingResponseListener(task.getBufferSize()) {
                @Override
                public void onComplete(final Result result) {
                    try {
                        if (result.isSucceeded()) {
                            try {
                                mappingHandler.handle(task.mapping, getContent());
                            } catch (DeviceFactoryException e) {
                                logger.error("Error parsing input from {}", task.url, e);
                            }
                        } else {
                            logger.error("Error {} accessing {}", result.getResponse().getStatus(), task.url);
                        }
                    } finally {
                        // According to the documentation, calling client.stop() would cause a dead lock
                        new Thread(() -> LifeCycle.stop(client)).start();
                    }
                }
            });
        } catch (Exception ex) {
            logger.error("Error querying {}", task.url, ex);
        }
    }

    /**
     * Runs a periodic HTTP task
     *
     * @param task Task to run
     */
    private void runScheduledTask(final ParsedHttpPeriodicTask periodicTask) {
        runTask(periodicTask);
        if (scheduledExecutor != null) {
            scheduledExecutor.schedule(() -> runScheduledTask(periodicTask), periodicTask.period, TimeUnit.SECONDS);
        }
    }
}
