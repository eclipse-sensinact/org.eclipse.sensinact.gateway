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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.eclipse.sensinact.northbound.rest.api.IRestNorthbound;
import org.eclipse.sensinact.northbound.rest.dto.AccessMethodCallParameterDTO;
import org.eclipse.sensinact.northbound.rest.dto.AccessMethodDTO;
import org.eclipse.sensinact.northbound.rest.dto.AccessMethodParameterDTO;
import org.eclipse.sensinact.northbound.rest.dto.CompleteProviderDescriptionDTO;
import org.eclipse.sensinact.northbound.rest.dto.CompleteResourceDescriptionDTO;
import org.eclipse.sensinact.northbound.rest.dto.CompleteServiceDescriptionDTO;
import org.eclipse.sensinact.northbound.rest.dto.EReadWriteMode;
import org.eclipse.sensinact.northbound.rest.dto.GetResponse;
import org.eclipse.sensinact.northbound.rest.dto.MetadataDTO;
import org.eclipse.sensinact.northbound.rest.dto.ProviderDescriptionDTO;
import org.eclipse.sensinact.northbound.rest.dto.ResultActResponse;
import org.eclipse.sensinact.northbound.rest.dto.ResultCompleteListDTO;
import org.eclipse.sensinact.northbound.rest.dto.ResultProvidersListDTO;
import org.eclipse.sensinact.northbound.rest.dto.ResultResourcesListDTO;
import org.eclipse.sensinact.northbound.rest.dto.ResultServicesListDTO;
import org.eclipse.sensinact.northbound.rest.dto.ResultTypedResponseDTO;
import org.eclipse.sensinact.northbound.rest.dto.ShortResourceDescriptionDTO;
import org.eclipse.sensinact.northbound.rest.dto.notification.ResourceDataNotificationDTO;
import org.eclipse.sensinact.northbound.rest.dto.notification.ResourceLifecycleNotificationDTO;
import org.eclipse.sensinact.prototype.ProviderDescription;
import org.eclipse.sensinact.prototype.ResourceDescription;
import org.eclipse.sensinact.prototype.ResourceShortDescription;
import org.eclipse.sensinact.prototype.SensiNactSession;
import org.eclipse.sensinact.prototype.ServiceDescription;
import org.eclipse.sensinact.prototype.model.ResourceType;
import org.eclipse.sensinact.prototype.model.ValueType;
import org.eclipse.sensinact.prototype.notification.ClientDataListener;
import org.eclipse.sensinact.prototype.notification.ClientLifecycleListener;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
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
     * Returns a user session
     */
    private SensiNactSession getSession() {
        return providers.getContextResolver(SensiNactSession.class, MediaType.WILDCARD_TYPE).getContext(null);
    }

    /**
     * Generates the complete description of a service. This description contains
     * its name and the description of its resources.
     *
     * @param providerId Provider ID
     * @param svcName    Service name
     * @return Complete description of the service
     */
    private final CompleteServiceDescriptionDTO completeServiceDescription(final SensiNactSession userSession,
            final String providerId, final String svcName) {
        final ServiceDescription serviceDescription = userSession.describeService(providerId, svcName);
        if (serviceDescription == null) {
            return null;
        }

        final CompleteServiceDescriptionDTO svcDto = new CompleteServiceDescriptionDTO();
        svcDto.name = serviceDescription.service;
        svcDto.resources = new ArrayList<>(serviceDescription.resources.size());

        for (final String rcName : serviceDescription.resources) {
            final ResourceShortDescription rcDescription = userSession.describeResourceShort(providerId, svcName,
                    rcName);

            final ShortResourceDescriptionDTO rcDto = new ShortResourceDescriptionDTO();
            svcDto.resources.add(rcDto);
            rcDto.name = rcName;
            rcDto.rws = EReadWriteMode.fromValueType(rcDescription.valueType);
            rcDto.type = rcDescription.resourceType;
        }

        return svcDto;
    }

    @Override
    public ResultCompleteListDTO describeProviders() {
        final SensiNactSession userSession = getSession();
        final ResultCompleteListDTO result = new ResultCompleteListDTO();
        result.uri = "/";
        result.type = "COMPLETE_LIST";
        try {
            final List<ProviderDescription> providers = userSession.listProviders();
            final List<CompleteProviderDescriptionDTO> descriptionsList = new ArrayList<>(providers.size());

            for (final ProviderDescription providerDescription : providers) {
                final CompleteProviderDescriptionDTO providerDto = new CompleteProviderDescriptionDTO();
                descriptionsList.add(providerDto);

                final String provider = providerDescription.provider;
                providerDto.name = provider;
                providerDto.services = new ArrayList<>(providerDescription.services.size());

                final String location = userSession.getResourceValue(provider, "admin", "location", String.class);
                if (location != null) {
                    providerDto.location = location;
                }

                final Object icon = userSession.getResourceValue(provider, "admin", "icon", Object.class);
                if (icon != null) {
                    providerDto.icon = String.valueOf(icon);
                }

                for (final String svcName : providerDescription.services) {
                    final CompleteServiceDescriptionDTO svcDescription = completeServiceDescription(userSession,
                            provider, svcName);
                    if (svcDescription != null) {
                        providerDto.services.add(svcDescription);
                    }
                }

                result.statusCode = 200;
                result.providers = descriptionsList;
            }
        } catch (Exception e) {
            result.statusCode = 500;
            final StringWriter stringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stringWriter));
            result.error = stringWriter.getBuffer().toString();
        }

        return result;
    }

    @Override
    public ResultProvidersListDTO listProviders() {
        final SensiNactSession userSession = getSession();

        final ResultProvidersListDTO result = new ResultProvidersListDTO();
        result.uri = "/";
        result.type = "PROVIDERS_LIST";
        try {
            result.providers = userSession.listProviders().stream().map(provider -> provider.provider)
                    .collect(Collectors.toList());
            result.statusCode = 200;
        } catch (Exception e) {
            result.statusCode = 500;
            final StringWriter stringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stringWriter));
            result.error = stringWriter.getBuffer().toString();
        }
        return result;
    }

    @Override
    public ResultTypedResponseDTO<ProviderDescriptionDTO> describeProvider(final String providerId) {
        final SensiNactSession userSession = getSession();
        final ResultTypedResponseDTO<ProviderDescriptionDTO> result = new ResultTypedResponseDTO<>();
        result.uri = String.join("/", "", providerId);
        result.type = "DESCRIBE_PROVIDER";
        try {
            final ProviderDescription provider = userSession.describeProvider(providerId);
            if (provider == null) {
                result.statusCode = 404;
                result.error = "Unknown provider";
            } else {
                final ProviderDescriptionDTO response = new ProviderDescriptionDTO();
                response.name = provider.provider;
                response.services = provider.services;

                result.statusCode = 200;
                result.response = response;
            }
        } catch (Exception e) {
            result.statusCode = 500;
            final StringWriter stringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stringWriter));
            result.error = stringWriter.getBuffer().toString();
        }
        return result;
    }

    @Override
    public ResultServicesListDTO listServices(final String providerId) {
        final SensiNactSession userSession = getSession();
        final ResultServicesListDTO result = new ResultServicesListDTO();
        result.uri = String.join("/", "", providerId);
        result.type = "SERVICES_LIST";

        try {
            final ProviderDescription provider = userSession.describeProvider(providerId);
            if (provider == null) {
                result.statusCode = 404;
                result.error = "Unknown provider";
            } else {
                result.services = provider.services;
                result.statusCode = 200;
            }
        } catch (Exception e) {
            result.statusCode = 500;
            final StringWriter stringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stringWriter));
            result.error = stringWriter.getBuffer().toString();
        }

        return result;
    }

    @Override
    public ResultTypedResponseDTO<CompleteServiceDescriptionDTO> describeService(final String providerId,
            final String serviceName) {
        final SensiNactSession userSession = getSession();
        final ResultTypedResponseDTO<CompleteServiceDescriptionDTO> result = new ResultTypedResponseDTO<>();
        result.uri = String.join("/", "", providerId, serviceName);
        result.type = "DESCRIBE_SERVICE";
        try {
            result.response = completeServiceDescription(userSession, providerId, serviceName);
            if (result.response != null) {
                result.statusCode = 200;
            } else {
                result.statusCode = 404;
                result.error = "Unknown service";
            }
        } catch (Exception e) {
            result.statusCode = 500;
            final StringWriter stringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stringWriter));
            result.error = stringWriter.getBuffer().toString();
        }

        return result;
    }

    @Override
    public ResultResourcesListDTO listResources(final String providerId, final String serviceName) {
        final SensiNactSession userSession = getSession();
        final ResultResourcesListDTO result = new ResultResourcesListDTO();
        result.uri = String.join("/", "", providerId, serviceName);
        result.type = "RESOURCES_LIST";

        try {
            final ServiceDescription service = userSession.describeService(providerId, serviceName);
            if (service == null) {
                result.statusCode = 404;
                result.error = "Unknown service";
            } else {
                result.resources = service.resources;
                result.statusCode = 200;
            }
        } catch (Exception e) {
            result.statusCode = 500;
            final StringWriter stringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stringWriter));
            result.error = stringWriter.getBuffer().toString();
        }

        return result;
    }

    private AccessMethodParameterDTO makeParam(final String name) {
        return makeParam(name, "string");
    }

    private AccessMethodParameterDTO makeParam(final String name, final String type) {
        final AccessMethodParameterDTO parameterDTO = new AccessMethodParameterDTO();
        parameterDTO.name = name;
        parameterDTO.type = type;
        return parameterDTO;
    }

    private List<AccessMethodDTO> computeMethods(final ResourceDescription rcDesc,
            final ResourceShortDescription rcShortDesc) {
        final List<AccessMethodDTO> methods = new ArrayList<>();
        if (rcShortDesc.resourceType == ResourceType.ACTION) {
            // Only an action is available
            final AccessMethodDTO actMethod = new AccessMethodDTO();
            actMethod.name = "ACT";

            final List<Class<?>> actMethodArgumentsTypes = rcShortDesc.actMethodArgumentsTypes;
            final List<AccessMethodParameterDTO> actParams = new ArrayList<>(actMethodArgumentsTypes.size());
            int idx = 0;
            for (final Class<?> argClass : actMethodArgumentsTypes) {
                final AccessMethodParameterDTO param = new AccessMethodParameterDTO();
                param.name = String.valueOf(idx);
                param.type = argClass.getName();
                actParams.add(param);
                idx++;
            }
            actMethod.parameters = actParams;
            methods.add(actMethod);
        } else {
            // GET is available
            final AccessMethodDTO getMethod = new AccessMethodDTO();
            getMethod.name = "GET";
            getMethod.parameters = List.of(makeParam("attributeName"));
            methods.add(getMethod);

            // Notifications are available
            final AccessMethodDTO subscriptionMethod = new AccessMethodDTO();
            subscriptionMethod.name = "SUBSCRIBE";
            subscriptionMethod.parameters = List.of(makeParam("topics", "array"),
                    makeParam("isDataListener", "boolean"), makeParam("isMetadataListener", "boolean"),
                    makeParam("isLifecycleListener", "boolean"), makeParam("isActionListener", "boolean"));
            methods.add(subscriptionMethod);

            final AccessMethodDTO unsubscriptionMethod = new AccessMethodDTO();
            unsubscriptionMethod.name = "UNSUBSCRIBE";
            unsubscriptionMethod.parameters = List.of(makeParam("subscriptionId"));
            methods.add(unsubscriptionMethod);

            if (rcShortDesc.valueType == ValueType.MODIFIABLE) {
                // SET is also available
                final AccessMethodDTO setMethod = new AccessMethodDTO();
                setMethod.name = "SET";
                setMethod.parameters = List.of(makeParam("value",
                        rcShortDesc.contentType != null ? rcShortDesc.contentType.getName() : Object.class.getName()));
                methods.add(setMethod);
            }
        }

        return methods;
    }

    private List<MetadataDTO> computeAttributes(final ResourceDescription rcDesc) {
        final Map<String, Object> metadataMap = rcDesc.metadata;
        final List<MetadataDTO> result;
        if (metadataMap != null) {
            result = new ArrayList<>(metadataMap.size());
            for (final Entry<String, Object> entry : metadataMap.entrySet()) {
                final Object value = entry.getValue();

                final MetadataDTO meta = new MetadataDTO();
                meta.name = entry.getKey();
                meta.value = value;
                if (value != null) {
                    meta.type = value.getClass().getName();
                }
            }
        } else {
            result = List.of();
        }
        return result;
    }

    @Override
    public ResultTypedResponseDTO<CompleteResourceDescriptionDTO> describeResource(final String providerId,
            final String serviceName, final String rcName) {
        final SensiNactSession userSession = getSession();
        final ResultTypedResponseDTO<CompleteResourceDescriptionDTO> result = new ResultTypedResponseDTO<>();
        result.uri = String.join("/", "", providerId, serviceName, rcName);
        result.type = "DESCRIBE_RESOURCE";

        try {
            final ResourceDescription rcDesc = userSession.describeResource(providerId, serviceName, rcName);
            if (rcDesc == null) {
                result.statusCode = 404;
                result.error = "Resource not set";
            } else {
                final ResourceShortDescription rcShortdesc = userSession.describeResourceShort(providerId, serviceName,
                        rcName);
                final CompleteResourceDescriptionDTO response = new CompleteResourceDescriptionDTO();
                response.name = rcDesc.resource;
                response.type = rcShortdesc.resourceType;
                response.accessMethods = computeMethods(rcDesc, rcShortdesc);
                response.attributes = computeAttributes(rcDesc);

                result.response = response;
                result.statusCode = 200;
            }
        } catch (Exception e) {
            result.statusCode = 500;
            final StringWriter stringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stringWriter));
            result.error = stringWriter.getBuffer().toString();
        }

        return result;
    }

    @Override
    public <T extends Object> ResultTypedResponseDTO<GetResponse<T>> resourceGet(final String providerId,
            final String serviceName, final String rcName) {
        final SensiNactSession userSession = getSession();
        final ResultTypedResponseDTO<GetResponse<T>> result = new ResultTypedResponseDTO<>();
        result.uri = String.join("/", "", providerId, serviceName, rcName);
        result.type = "GET_RESPONSE";

        try {
            final ResourceDescription rcDesc = userSession.describeResource(providerId, serviceName, rcName);
            if (rcDesc == null) {
                // No access to the resource
                result.error = "Unknown provider or service";
                result.statusCode = 404;
            } else {
                final ResourceShortDescription rcShortDesc = userSession.describeResourceShort(providerId, serviceName,
                        rcName);

                final GetResponse<T> response = new GetResponse<T>();
                response.name = rcDesc.resource;

                if (rcShortDesc.contentType != null) {
                    response.type = rcShortDesc.contentType.getName();
                } else if (response.value != null) {
                    response.type = response.value.getClass().getName();
                }

                if (rcDesc.timestamp != null) {
                    response.timestamp = rcDesc.timestamp.toEpochMilli();
                    result.response = response;
                    result.statusCode = 200;

                    // FIXME: find a way to avoid that unchecked casting
                    // Calling userSession.getResourceValue with generic type would not return the
                    // value timestamp
                    response.value = (T) rcDesc.value;
                } else {
                    // No time stamp = no content
                    response.timestamp = Instant.now().toEpochMilli();
                    response.value = null;
                    result.response = response;

                    result.error = "No value set";
                    result.statusCode = 204;
                }
            }
        } catch (Exception e) {
            result.statusCode = 500;
            final StringWriter stringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stringWriter));
            result.error = stringWriter.getBuffer().toString();
        }

        return result;
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
    public ResultTypedResponseDTO<GetResponse<?>> resourceSet(final String providerId, final String serviceName,
            final String rcName, final List<AccessMethodCallParameterDTO> parameters) {
        final SensiNactSession userSession = getSession();

        final ResultTypedResponseDTO<GetResponse<?>> result = new ResultTypedResponseDTO<>();
        result.uri = String.join("/", "", providerId, serviceName, rcName);
        result.type = "SET_RESPONSE";

        try {
            final Object newValue = extractSetValue(parameters);

            // Force the timestamp so that we are sure we are coherent with what we return
            final Instant timestamp = Instant.now();
            userSession.setResourceValue(providerId, serviceName, rcName, newValue, timestamp);

            final ResourceShortDescription rcShortDesc = userSession.describeResourceShort(providerId, serviceName,
                    rcName);

            final GetResponse<Object> response = new GetResponse<>();
            response.name = rcName;
            response.timestamp = timestamp.toEpochMilli();
            response.type = rcShortDesc.contentType.getName();
            response.value = newValue;

            result.response = response;
            result.statusCode = 200;
        } catch (Exception e) {
            result.statusCode = 500;
            final StringWriter stringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stringWriter));
            result.error = stringWriter.getBuffer().toString();
        }

        return result;
    }

    private Object[] extractActParams(final List<Class<?>> actMethodArgumentsTypes,
            final List<AccessMethodCallParameterDTO> parameters) {
        final Object[] params = new Object[actMethodArgumentsTypes.size()];

        int idx = 0;
        boolean allowIdx = true;
        for (final AccessMethodCallParameterDTO param : parameters) {
            int givenIdx = -1;
            try {
                givenIdx = Integer.parseInt(param.name);
                // Reject named arguments if one of name is indexed
                allowIdx = false;
            } catch (NumberFormatException e) {
                if (!allowIdx) {
                    throw new IllegalArgumentException("Cannot mix positional and named arguments");
                }

                givenIdx = idx + 1;
            }

            if (givenIdx < 0 || givenIdx >= params.length) {
                throw new IllegalArgumentException("Given argument index is out of bounds: " + givenIdx);
            }

            if (params[givenIdx] != null) {
                throw new IllegalArgumentException("Trying to overwrite given argument: " + givenIdx);
            }

            final Object paramValue = param.value;
            if (paramValue != null && !actMethodArgumentsTypes.get(givenIdx).isAssignableFrom(paramValue.getClass())) {
                throw new IllegalArgumentException("Invalid parameter type: " + paramValue.getClass());
            }

            params[givenIdx] = paramValue;
        }

        return params;
    }

    @Override
    public ResultActResponse<?> resourceAct(final String providerId, final String serviceName, final String rcName,
            final List<AccessMethodCallParameterDTO> parameters) {
        final SensiNactSession userSession = getSession();
        final ResultActResponse<Object> result = new ResultActResponse<>();

        result.uri = String.join("/", "", providerId, serviceName, rcName);
        result.type = "ACT_RESPONSE";

        try {
            final List<Class<?>> actMethodArgumentsTypes = userSession.describeResourceShort(providerId, serviceName,
                    rcName).actMethodArgumentsTypes;
            final Object[] params = extractActParams(actMethodArgumentsTypes, parameters);
            result.response = userSession.actOnResource(providerId, serviceName, rcName, params);
        } catch (Exception e) {
            result.statusCode = 500;
            final StringWriter stringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stringWriter));
            result.error = stringWriter.getBuffer().toString();
        }

        return result;
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
