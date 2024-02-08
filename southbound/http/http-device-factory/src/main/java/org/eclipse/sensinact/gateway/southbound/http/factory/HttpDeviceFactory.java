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

import static java.util.stream.Collectors.toMap;

import java.lang.reflect.Array;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Authentication;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
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

        final HttpDeviceFactoryConfigurationTaskDTO[] oneShotTasks = loadTasks(mapper,
                HttpDeviceFactoryConfigurationTaskDTO.class, configuration.tasks_oneshot());
        if (oneShotTasks != null) {
            for (HttpDeviceFactoryConfigurationTaskDTO task : oneShotTasks) {
                runTask(new ParsedHttpTask(task));
            }
        }

        final HttpDeviceFactoryConfigurationPeriodicDTO[] periodicTasks = loadTasks(mapper,
                HttpDeviceFactoryConfigurationPeriodicDTO.class, configuration.tasks_periodic());
        if (periodicTasks != null) {
            for (HttpDeviceFactoryConfigurationPeriodicDTO task : periodicTasks) {
                runScheduledTask(new ParsedHttpPeriodicTask(task));
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
     * Parses a JSON array from the configuration.
     *
     * ConfigurationAdmin will give an array with a single string containing the
     * JSON array of tasks. Configurator will give an array of strings, each one
     * being the JSON object of a tasks.
     *
     * @param <T>      Expected configuration type
     * @param mapper   Object mapper
     * @param type     Class of expected configuration type
     * @param strTasks Configuration array
     * @return Array of parsed tasks or null
     * @throws Exception Error parsing configuration
     */
    @SuppressWarnings("unchecked")
    private <T> T[] loadTasks(final ObjectMapper mapper, final Class<T> type, final String[] strTasks)
            throws Exception {
        if (strTasks != null) {
            if (strTasks.length == 1 && strTasks[0].startsWith("[")) {
                // Single string of array
                return (T[]) mapper.readValue(strTasks[0], Array.newInstance(type, 0).getClass());
            } else {
                // Strings of objects
                final T[] result = (T[]) Array.newInstance(type, strTasks.length);
                int i = 0;
                for (String rawPeriodicTask : strTasks) {
                    result[i++] = mapper.readValue(rawPeriodicTask, type);
                }
                return result;
            }
        }
        return null;
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

                private final AtomicReference<Map<String, String>> headers = new AtomicReference<>(Map.of());

                @Override
                public void onHeaders(Response response) {
                    super.onHeaders(response);
                    headers.set(response.getHeaders().getFieldNamesCollection().stream()
                            .collect(toMap(Function.identity(), h -> response.getHeaders().get(h))));
                }

                @Override
                public void onFailure(final Response response, final Throwable failure) {
                    logger.error("Error {} requesting {}: {}", response.getStatus(), task.url, failure);
                }

                @Override
                public void onSuccess(final Response response) {
                    try {
                        mappingHandler.handle(task.mapping, headers.get(), getContent());
                    } catch (DeviceFactoryException e) {
                        logger.error("Error parsing input from {}", task.url, e);
                    }
                }

                @Override
                public void onComplete(final Result result) {
                    // According to the documentation, calling client.stop() would cause a dead lock
                    scheduledExecutor.submit(() -> LifeCycle.stop(client));
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
