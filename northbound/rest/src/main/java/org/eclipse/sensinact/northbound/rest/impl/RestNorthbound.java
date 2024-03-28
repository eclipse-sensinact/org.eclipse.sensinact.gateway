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
package org.eclipse.sensinact.northbound.rest.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.sensinact.core.notification.ClientDataListener;
import org.eclipse.sensinact.core.notification.ClientLifecycleListener;
import org.eclipse.sensinact.northbound.query.api.AbstractQueryDTO;
import org.eclipse.sensinact.northbound.query.api.AbstractResultDTO;
import org.eclipse.sensinact.northbound.query.api.IQueryHandler;
import org.eclipse.sensinact.northbound.query.dto.SensinactPath;
import org.eclipse.sensinact.northbound.query.dto.notification.ResourceDataNotificationDTO;
import org.eclipse.sensinact.northbound.query.dto.notification.ResourceLifecycleNotificationDTO;
import org.eclipse.sensinact.northbound.query.dto.query.AccessMethodCallParameterDTO;
import org.eclipse.sensinact.northbound.query.dto.query.QueryActDTO;
import org.eclipse.sensinact.northbound.query.dto.query.QueryDescribeDTO;
import org.eclipse.sensinact.northbound.query.dto.query.QueryGetDTO;
import org.eclipse.sensinact.northbound.query.dto.query.QueryListDTO;
import org.eclipse.sensinact.northbound.query.dto.query.QuerySetDTO;
import org.eclipse.sensinact.northbound.query.dto.query.WrappedAccessMethodCallParametersDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ErrorResultDTO;
import org.eclipse.sensinact.northbound.rest.api.IRestNorthbound;
import org.eclipse.sensinact.northbound.session.ResourceShortDescription;
import org.eclipse.sensinact.northbound.session.SensiNactSession;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Providers;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;

public class RestNorthbound implements IRestNorthbound {

    /**
     * Server-sent events handling
     */
    @Context
    Sse sse;

    /**
     * Access to custom context resolvers
     */
    @Context
    Providers providers;

    /**
     * URI info
     */
    @Context
    UriInfo uriInfo;

    /**
     * Returns a user session
     */
    private SensiNactSession getSession() {
        return providers.getContextResolver(SensiNactSession.class, MediaType.WILDCARD_TYPE).getContext(null);
    }

    /**
     * Returns the query handler
     */
    private IQueryHandler getQueryHandler() {
        return providers.getContextResolver(IQueryHandler.class, MediaType.WILDCARD_TYPE).getContext(null);
    }

    /**
     * Runs the given query
     *
     * @param query Query to forwards to the handler
     * @return
     */
    private AbstractResultDTO handleQuery(final AbstractQueryDTO query) {
        return getQueryHandler().handleQuery(getSession(), query);
    }

    /**
     * Injects the query filter in a description query
     *
     * @param query Query to update
     */
    private void injectFilter(final QueryDescribeDTO query) {
        final MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters(true);
        query.filter = queryParameters.getFirst("filter");
        if (query.filter != null) {
            if (queryParameters.containsKey("filterLanguage")) {
                query.filterLanguage = queryParameters.getFirst("filterLanguage");
            } else {
                query.filterLanguage = "ldap";
            }
        } else if ((query.filter = queryParameters.getFirst("ldap")) != null) {
            // LDAP-only legacy attribute
            query.filterLanguage = "ldap";
        }
    }

    /**
     * Injects the query filter in a list query
     *
     * @param query Query to update
     */
    private void injectFilter(final QueryListDTO query) {
        final MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters(true);
        query.filter = queryParameters.getFirst("filter");
        if (queryParameters.containsKey("filterLanguage")) {
            query.filterLanguage = queryParameters.getFirst("filterLanguage");
        } else {
            query.filterLanguage = "ldap";
        }
    }

    @Override
    public AbstractResultDTO describeProviders() {
        final QueryDescribeDTO query = new QueryDescribeDTO();
        query.attrs = uriInfo.getQueryParameters().getOrDefault("attrs", List.of());
        query.uri = new SensinactPath();
        injectFilter(query);
        return handleQuery(query);
    }

    @Override
    public AbstractResultDTO listProviders() {
        final QueryListDTO query = new QueryListDTO();
        query.uri = new SensinactPath();
        injectFilter(query);
        return handleQuery(query);
    }

    @Override
    public AbstractResultDTO describeProvider(final String providerId) {
        final QueryDescribeDTO query = new QueryDescribeDTO();
        query.uri = new SensinactPath(providerId);
        injectFilter(query);
        return handleQuery(query);
    }

    @Override
    public AbstractResultDTO listServices(final String providerId) {
        final QueryListDTO query = new QueryListDTO();
        query.uri = new SensinactPath(providerId);
        injectFilter(query);
        return handleQuery(query);
    }

    @Override
    public AbstractResultDTO describeService(final String providerId, final String serviceName) {
        final QueryDescribeDTO query = new QueryDescribeDTO();
        query.uri = new SensinactPath(providerId, serviceName);
        injectFilter(query);
        return handleQuery(query);
    }

    @Override
    public AbstractResultDTO listResources(final String providerId, final String serviceName) {
        final QueryListDTO query = new QueryListDTO();
        query.uri = new SensinactPath(providerId, serviceName);
        injectFilter(query);
        return handleQuery(query);
    }

    @Override
    public AbstractResultDTO describeResource(final String providerId, final String serviceName, final String rcName) {
        final QueryDescribeDTO query = new QueryDescribeDTO();
        query.uri = new SensinactPath(providerId, serviceName, rcName);
        injectFilter(query);
        return handleQuery(query);
    }

    @Override
    public AbstractResultDTO resourceGet(final String providerId, final String serviceName, final String rcName) {
        final QueryGetDTO query = new QueryGetDTO();
        query.uri = new SensinactPath(providerId, serviceName, rcName);
        return handleQuery(query);
    }

    /**
     * Extract the new value to set to a resource according to the given arguments
     *
     * @param parameters Given parameters
     * @return The new value
     * @throws IllegalArgumentException Invalid argument value or number of
     *                                  arguments
     */
    private Object extractSetValue(final List<AccessMethodCallParameterDTO> parameters) {
        if (parameters == null || parameters.isEmpty() || parameters.size() > 2) {
            throw new IllegalArgumentException("Only 1 or 2 arguments are accepted");
        }

        Object newValue = null;
        if (parameters.size() == 2) {
            boolean gotValue = false;
            for (final AccessMethodCallParameterDTO param : parameters) {
                if ("attributeName".equals(param.name)) {
                    if (!"value".equals(param.value)) {
                        throw new IllegalArgumentException("Only the resource value can be set");
                    }
                } else if ("value".equals(param.name)) {
                    newValue = param.value;
                    gotValue = true;
                } else {
                    throw new IllegalArgumentException("Unsupported argument: " + param.name);
                }
            }

            if (!gotValue) {
                throw new IllegalArgumentException("No value given");
            }
        } else {
            // Value only
            newValue = parameters.get(0).value;
        }

        return newValue;
    }

    @Override
    public AbstractResultDTO resourceSet(final String providerId, final String serviceName, final String rcName,
            final WrappedAccessMethodCallParametersDTO parameters) {

        final QuerySetDTO query = new QuerySetDTO();
        query.uri = new SensinactPath(providerId, serviceName, rcName);
        query.value = extractSetValue(parameters.parameters);
        return handleQuery(query);
    }

    private Map<String, Object> extractActParams(final List<Entry<String, Class<?>>> actMethodArgumentsTypes,
            final List<AccessMethodCallParameterDTO> parameters) {
        final Map<String, Object> params = new HashMap<>();

        boolean named = false;
        boolean indexed = false;
        for (final AccessMethodCallParameterDTO param : parameters) {
            String name;
            try {
                int givenIdx = Integer.parseInt(param.name);
                // Reject named arguments if one of name is indexed
                if (named) {
                    throw new IllegalArgumentException("Cannot mix positional and named arguments");
                }
                indexed = true;
                if (givenIdx < 0 || givenIdx >= actMethodArgumentsTypes.size()) {
                    throw new IllegalArgumentException("Given argument index is out of bounds: " + givenIdx);
                }
                name = actMethodArgumentsTypes.get(givenIdx).getKey();
            } catch (NumberFormatException e) {
                if (indexed) {
                    throw new IllegalArgumentException("Cannot mix positional and named arguments");
                }
                name = param.name;
            }

            if (params.containsKey(name)) {
                throw new IllegalArgumentException("Trying to overwrite given argument: " + name);
            }
            params.put(param.name, param.value);
        }

        return params;
    }

    @Override
    public AbstractResultDTO resourceAct(final String providerId, final String serviceName, final String rcName,
            final WrappedAccessMethodCallParametersDTO parameters) {

        final ResourceShortDescription rcDesc = getSession().describeResourceShort(providerId, serviceName, rcName);
        if (rcDesc == null) {
            // Unknown resource
            final ErrorResultDTO error = new ErrorResultDTO();
            error.statusCode = 404;
            error.error = "Resource not found";
            error.uri = String.join("/", providerId, serviceName, rcName);
            return error;
        }

        final List<Entry<String, Class<?>>> actMethodArgumentsTypes = rcDesc.actMethodArgumentsTypes;

        final QueryActDTO query = new QueryActDTO();
        query.uri = new SensinactPath(providerId, serviceName, rcName);
        query.parameters = extractActParams(actMethodArgumentsTypes, parameters.parameters);
        return handleQuery(query);
    }

    @Override
    public void watchResource(String providerId, String serviceName, String rcName, SseEventSink eventSink) {
        final SensiNactSession session = getSession();
        final AtomicReference<String> listenerId = new AtomicReference<>();

        final ClientDataListener cdl = (t, e) -> {
            if (eventSink.isClosed()) {
                // Event sink is already closed: remove listener
                session.removeListener(listenerId.get());
                return;
            }

            eventSink.send(sse.newEventBuilder().name("data").mediaType(MediaType.APPLICATION_JSON_TYPE)
                    .data(new ResourceDataNotificationDTO(e)).build());
        };

        final ClientLifecycleListener cll = (t, e) -> {
            if (eventSink.isClosed()) {
                // Event sink is already closed: remove listener
                session.removeListener(listenerId.get());
                return;
            }
            eventSink.send(sse.newEventBuilder().name("lifecycle").mediaType(MediaType.APPLICATION_JSON_TYPE)
                    .data(new ResourceLifecycleNotificationDTO(e)).build());
        };

        // Register the listener
        final String topic = String.join("/", providerId, serviceName, rcName);
        listenerId.set(session.addListener(List.of(topic), cdl, null, cll, null));
    }
}
