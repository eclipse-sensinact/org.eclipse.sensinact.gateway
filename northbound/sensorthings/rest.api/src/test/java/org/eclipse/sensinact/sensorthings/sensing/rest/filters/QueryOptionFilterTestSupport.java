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
*   Data In Motion - initial API and implementation
**********************************************************************/
package org.eclipse.sensinact.sensorthings.sensing.rest.filters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Self;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import tools.jackson.databind.ObjectMapper;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.Response.StatusType;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Providers;
import jakarta.ws.rs.ext.RuntimeDelegate;

/**
 * Mocked jakarta-rs contexts backed by real property and query-parameter maps,
 * shared by the query-option filter tests.
 */
abstract class QueryOptionFilterTestSupport {

    public record TestEntity(String selfLink, String id, Integer rank) implements Self {
    }

    protected ContainerRequestContext requestContext;
    protected ContainerResponseContext responseContext;
    protected UriInfo uriInfo;
    protected MultivaluedMap<String, String> queryParameters;

    private final Map<String, Object> requestProperties = new HashMap<>();
    private Object responseEntity;

    /**
     * The jakarta.ws.rs-api Response builder requires a RuntimeDelegate
     * implementation; provide a minimal recording stub so plain unit tests can
     * assert aborted responses.
     */
    @BeforeAll
    static void installRuntimeDelegateStub() {
        RuntimeDelegate delegate = mock(RuntimeDelegate.class);
        when(delegate.createResponseBuilder()).thenAnswer(unused -> recordingResponseBuilder());
        RuntimeDelegate.setInstance(delegate);
    }

    private static ResponseBuilder recordingResponseBuilder() {
        AtomicInteger status = new AtomicInteger();
        AtomicReference<Object> entity = new AtomicReference<>();
        ResponseBuilder builder = mock(ResponseBuilder.class, Mockito.RETURNS_SELF);
        doAnswer(invocation -> {
            status.set(invocation.getArgument(0));
            return invocation.getMock();
        }).when(builder).status(any(int.class));
        doAnswer(invocation -> {
            status.set(invocation.<StatusType> getArgument(0).getStatusCode());
            return invocation.getMock();
        }).when(builder).status(any(StatusType.class));
        doAnswer(invocation -> {
            entity.set(invocation.getArgument(0));
            return invocation.getMock();
        }).when(builder).entity(any());
        when(builder.build()).thenAnswer(unused -> {
            Response response = mock(Response.class);
            when(response.getStatus()).thenReturn(status.get());
            when(response.getStatusInfo()).thenReturn(Response.Status.fromStatusCode(status.get()));
            when(response.getEntity()).thenAnswer(u -> entity.get());
            return response;
        });
        return builder;
    }

    @BeforeEach
    void setUpContexts() {
        queryParameters = new MultivaluedHashMap<>();
        uriInfo = mock(UriInfo.class);
        when(uriInfo.getQueryParameters()).thenReturn(queryParameters);

        requestContext = mock(ContainerRequestContext.class);
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        doAnswer(invocation -> requestProperties.put(invocation.getArgument(0), invocation.getArgument(1)))
                .when(requestContext).setProperty(anyString(), any());
        when(requestContext.getProperty(anyString()))
                .thenAnswer(invocation -> requestProperties.get(invocation.getArgument(0)));

        responseContext = mock(ContainerResponseContext.class);
        when(responseContext.getEntity()).thenAnswer(invocation -> responseEntity);
        doAnswer(invocation -> responseEntity = invocation.getArgument(0)).when(responseContext).setEntity(any());
    }

    protected void setRequestProperty(String name, Object value) {
        requestProperties.put(name, value);
    }

    protected void setResponseEntity(Object entity) {
        responseEntity = entity;
    }

    protected Object responseEntity() {
        return responseEntity;
    }

    @SuppressWarnings("unchecked")
    protected ResultList<TestEntity> responseResultList() {
        return (ResultList<TestEntity>) responseEntity;
    }

    protected Response abortedResponse() {
        ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
        verify(requestContext).abortWith(captor.capture());
        return captor.getValue();
    }

    protected void assertAbortedWithBadRequest() {
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), abortedResponse().getStatus());
    }

    protected static List<TestEntity> entities(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> new TestEntity("https://example.org/TestEntities(" + i + ")", "entity-" + i, i))
                .toList();
    }

    protected static Providers providersWith(ObjectMapper mapper) {
        @SuppressWarnings("unchecked")
        ContextResolver<ObjectMapper> resolver = mock(ContextResolver.class);
        when(resolver.getContext(any())).thenReturn(mapper);
        Providers providers = mock(Providers.class);
        doReturn(resolver).when(providers).getContextResolver(any(), any());
        return providers;
    }
}
