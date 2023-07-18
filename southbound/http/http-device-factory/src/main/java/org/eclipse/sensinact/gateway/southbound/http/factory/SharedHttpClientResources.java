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

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.dynamic.HttpClientTransportDynamic;
import org.eclipse.jetty.io.ClientConnector;
import org.eclipse.jetty.util.SocketAddressResolver;
import org.eclipse.jetty.util.SocketAddressResolver.Async;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ScheduledExecutorScheduler;
import org.eclipse.jetty.util.thread.Scheduler;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * Shared HTTP client
 */
@Component(immediate = true, service = SharedHttpClientResources.class)
public class SharedHttpClientResources {

    private QueuedThreadPool sharedPool;
    private Scheduler sharedScheduler;
    private Async sharedAddressResolver;

    @Activate
    void activate() throws Exception {
        final String baseName = "sensinact-http-device-factory";
        sharedPool = new QueuedThreadPool(32);
        sharedPool.setName(baseName + "-pool");
        sharedPool.start();

        sharedScheduler = new ScheduledExecutorScheduler(baseName + "-scheduler", false);
        sharedScheduler.start();

        sharedAddressResolver = new SocketAddressResolver.Async(sharedPool, sharedScheduler, 15000);
    }

    @Deactivate
    void deactivate() throws Exception {
        sharedScheduler.stop();
        sharedScheduler = null;
        sharedPool.stop();
        sharedPool = null;
        sharedAddressResolver = null;
    }

    /**
     * Returns a new HTTP client using shared resources and configured according to
     * the given task. The client is not started when returned.
     */
    public HttpClient newClient(final ParsedHttpTask task) {
        // SSL configuration
        final SslContextFactory.Client sslContextFactory = new SslContextFactory.Client(task.ignoreSslErrors);
        if (task.keystorePath != null) {
            sslContextFactory.setKeyStorePath(task.keystorePath);
            if (task.keystorePassword != null) {
                sslContextFactory.setKeyStorePassword(task.keystorePassword);
            }
        }

        if (task.trustStorePath != null) {
            sslContextFactory.setTrustStorePath(task.trustStorePath);
            if (task.trustStorePassword != null) {
                sslContextFactory.setTrustStorePassword(task.trustStorePassword);
            }
        }

        // Setup the connector using shared resources
        final ClientConnector clientConnector = new ClientConnector();
        clientConnector.setExecutor(sharedPool);
        clientConnector.setScheduler(sharedScheduler);
        clientConnector.setSslContextFactory(sslContextFactory);
        clientConnector.setConnectTimeout(Duration.of(task.timeout, ChronoUnit.SECONDS));

        // Construct the client with shared address resolver
        final HttpClient client = new HttpClient(new HttpClientTransportDynamic(clientConnector));
        client.setSocketAddressResolver(sharedAddressResolver);

        // HTTP behavior
        client.setFollowRedirects(task.followHttpRedirect);
        return client;
    }
}
