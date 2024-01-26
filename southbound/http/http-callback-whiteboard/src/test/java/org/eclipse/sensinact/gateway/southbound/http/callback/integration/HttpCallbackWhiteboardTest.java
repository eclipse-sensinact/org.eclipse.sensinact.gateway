/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.gateway.southbound.http.callback.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.sensinact.gateway.southbound.http.callback.api.HttpCallback;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.config.WithConfiguration;

@MockitoSettings
public class HttpCallbackWhiteboardTest {

    @Mock
    HttpCallback callback;

    @WithConfiguration(pid = "org.apache.felix.http", location = "?", properties = {
            @Property(key = "org.osgi.service.http.port", value = "8234"),
            @Property(key = "org.apache.felix.http.host", value = "127.0.0.1") })
    @WithConfiguration(pid = "sensinact.http.callback.whiteboard", location = "?", properties = {})
    @Test
    void basicWhiteboard(@InjectBundleContext BundleContext context) throws Exception {

        context.registerService(HttpCallback.class, callback, null);

        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);

        Mockito.verify(callback, Mockito.timeout(1000)).activate(stringCaptor.capture());

        String uri = stringCaptor.getValue();

        AtomicReference<String> message = new AtomicReference<>();
        Mockito.doAnswer(i -> {
            BufferedReader br = i.getArgument(2, BufferedReader.class);
            StringWriter sw = new StringWriter();
            br.transferTo(sw);
            message.compareAndSet(null, sw.toString());
            return null;
        }).when(callback).call(Mockito.eq(uri), Mockito.anyMap(), Mockito.any());

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder(URI.create(uri)).POST(BodyPublishers.ofString("Hello World"))
                .timeout(Duration.ofSeconds(2)).build();

        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

        assertEquals(204, response.statusCode());

        assertEquals("Hello World", message.get());

    }

    @WithConfiguration(pid = "org.apache.felix.http", location = "?", properties = {
            @Property(key = "org.osgi.service.http.port", value = "8235"),
            @Property(key = "org.apache.felix.http.host", value = "127.0.0.1") })
    @WithConfiguration(pid = "sensinact.http.callback.whiteboard", location = "?", properties = @Property(key = "base.uri", value = "http://foo.com/bar"))
    @Test
    void customBaseUri(@InjectBundleContext BundleContext context) throws Exception {

        context.registerService(HttpCallback.class, callback, null);

        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);

        Mockito.verify(callback, Mockito.timeout(1000)).activate(stringCaptor.capture());

        String uri = stringCaptor.getValue();

        assertTrue(uri.startsWith("http://foo.com/bar/"));

        String path = uri.substring("http://foo.com/bar/".length());

        AtomicReference<String> message = new AtomicReference<>();
        Mockito.doAnswer(i -> {
            BufferedReader br = i.getArgument(2, BufferedReader.class);
            StringWriter sw = new StringWriter();
            br.transferTo(sw);
            message.compareAndSet(null, sw.toString());
            return null;
        }).when(callback).call(Mockito.eq(uri), Mockito.anyMap(), Mockito.any());

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:8235/" + path))
                .POST(BodyPublishers.ofString("Hello World")).timeout(Duration.ofSeconds(2)).build();

        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

        assertEquals(204, response.statusCode());

        assertEquals("Hello World", message.get());

    }
}
