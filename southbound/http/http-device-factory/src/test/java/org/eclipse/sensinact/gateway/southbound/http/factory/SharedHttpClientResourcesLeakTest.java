/*********************************************************************
* Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.management.BufferPoolMXBean;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.sensinact.gateway.southbound.device.factory.dto.DeviceMappingConfigurationDTO;
import org.eclipse.sensinact.gateway.southbound.http.factory.config.HttpDeviceFactoryConfigurationTaskDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.management.HotSpotDiagnosticMXBean;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Checks that polling with a new client per request, as {@link HttpDeviceFactory}
 * does, does not retain direct memory.
 *
 * Set the system property <code>leak.hprof.dir</code> to also write a heap dump
 * before and after the requests.
 */
public class SharedHttpClientResourcesLeakTest {

    private static final int REQUESTS = 2000;

    /** One leaked network buffer per request would exceed this by far */
    private static final long MAX_GROWTH = 4 * 1024 * 1024;

    private Server server;
    private SharedHttpClientResources resources;
    private ExecutorService stopper;

    @BeforeEach
    void start() throws Exception {
        server = new Server(0);
        server.setHandler(new AbstractHandler() {
            @Override
            public void handle(String target, Request baseRequest, HttpServletRequest request,
                    HttpServletResponse response) {
                baseRequest.setHandled(true);
                response.setStatus(200);
                try {
                    response.getOutputStream().write("id,value\nsdv,1\n".getBytes(StandardCharsets.UTF_8));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        server.start();

        resources = new SharedHttpClientResources();
        resources.activate();
        stopper = Executors.newSingleThreadExecutor();
    }

    @AfterEach
    void stop() throws Exception {
        stopper.shutdownNow();
        resources.deactivate();
        server.stop();
    }

    @Test
    void directMemoryIsReleased() throws Exception {
        final HttpDeviceFactoryConfigurationTaskDTO dto = new HttpDeviceFactoryConfigurationTaskDTO();
        dto.url = "http://localhost:" + ((ServerConnector) server.getConnectors()[0]).getLocalPort() + "/sdv.csv";
        dto.mapping = new DeviceMappingConfigurationDTO();
        final ParsedHttpTask task = new ParsedHttpTask(dto);

        // Warm up the shared pool threads and the server buffers
        poll(task, 50);
        final long before = directMemory();
        dump("before");

        poll(task, REQUESTS);
        final long after = directMemory();
        dump("after");

        final long growth = after - before;
        System.out.printf("Direct memory: %d bytes before, %d bytes after %d requests (%+d bytes, %.1f bytes/request)%n",
                before, after, REQUESTS, growth, (double) growth / REQUESTS);
        assertTrue(growth < MAX_GROWTH, "Direct memory grew by " + growth + " bytes over " + REQUESTS + " requests");
    }

    /**
     * Same client life cycle as {@link HttpDeviceFactory}: one client per
     * request, stopped from another thread once the request completes.
     */
    private void poll(final ParsedHttpTask task, final int count) throws Exception {
        for (int i = 0; i < count; i++) {
            final CountDownLatch done = new CountDownLatch(1);
            final HttpClient client = resources.newClient(task);
            client.start();
            client.newRequest(task.url).send(new BufferingResponseListener(task.getBufferSize()) {
                @Override
                public void onComplete(Result result) {
                    stopper.submit(() -> {
                        LifeCycle.stop(client);
                        done.countDown();
                    });
                }
            });
            assertTrue(done.await(10, TimeUnit.SECONDS), "Request " + i + " did not complete");
        }
    }

    private static long directMemory() throws InterruptedException {
        // Direct buffers are only released once their owner is collected
        for (int i = 0; i < 3; i++) {
            System.gc();
            Thread.sleep(100);
        }
        return ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class).stream()
                .filter(p -> "direct".equals(p.getName())).mapToLong(BufferPoolMXBean::getMemoryUsed).sum();
    }

    private static void dump(final String name) throws Exception {
        final String dir = System.getProperty("leak.hprof.dir");
        if (dir == null) {
            return;
        }
        final Path file = Path.of(dir, "http-device-factory-" + name + ".hprof");
        Files.createDirectories(file.getParent());
        Files.deleteIfExists(file);
        ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class).dumpHeap(file.toString(), true);
    }
}
