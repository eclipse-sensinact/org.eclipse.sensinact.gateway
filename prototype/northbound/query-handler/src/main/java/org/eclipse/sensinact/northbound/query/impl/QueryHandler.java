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
package org.eclipse.sensinact.northbound.query.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.northbound.filters.api.FilterCommandHelper;
import org.eclipse.sensinact.northbound.filters.api.FilterException;
import org.eclipse.sensinact.northbound.filters.api.IFilterHandler;
import org.eclipse.sensinact.northbound.query.api.AbstractQueryDTO;
import org.eclipse.sensinact.northbound.query.api.AbstractResultDTO;
import org.eclipse.sensinact.northbound.query.api.EReadWriteMode;
import org.eclipse.sensinact.northbound.query.api.EResultType;
import org.eclipse.sensinact.northbound.query.api.IQueryHandler;
import org.eclipse.sensinact.northbound.query.dto.SensinactPath;
import org.eclipse.sensinact.northbound.query.dto.query.QueryActDTO;
import org.eclipse.sensinact.northbound.query.dto.query.QueryDescribeDTO;
import org.eclipse.sensinact.northbound.query.dto.query.QueryGetDTO;
import org.eclipse.sensinact.northbound.query.dto.query.QueryListDTO;
import org.eclipse.sensinact.northbound.query.dto.query.QuerySetDTO;
import org.eclipse.sensinact.northbound.query.dto.result.AccessMethodDTO;
import org.eclipse.sensinact.northbound.query.dto.result.AccessMethodParameterDTO;
import org.eclipse.sensinact.northbound.query.dto.result.CompleteProviderDescriptionDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ErrorResultDTO;
import org.eclipse.sensinact.northbound.query.dto.result.MetadataDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResponseDescribeProviderDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResponseDescribeResourceDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResponseDescribeServiceDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResponseGetDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResultActDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResultDescribeProvidersDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResultListProvidersDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResultListResourcesDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResultListServicesDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ShortResourceDescriptionDTO;
import org.eclipse.sensinact.northbound.query.dto.result.TypedResponse;
import org.eclipse.sensinact.prototype.ProviderDescription;
import org.eclipse.sensinact.prototype.ResourceDescription;
import org.eclipse.sensinact.prototype.ResourceShortDescription;
import org.eclipse.sensinact.prototype.SensiNactSession;
import org.eclipse.sensinact.prototype.ServiceDescription;
import org.eclipse.sensinact.prototype.command.GatewayThread;
import org.eclipse.sensinact.prototype.model.ResourceType;
import org.eclipse.sensinact.prototype.model.ValueType;
import org.eclipse.sensinact.prototype.snapshot.ICriterion;
import org.eclipse.sensinact.prototype.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.prototype.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.prototype.snapshot.ResourceValueFilter;
import org.eclipse.sensinact.prototype.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.prototype.twin.TimedValue;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the query handler
 */
@Component(service = IQueryHandler.class, immediate = true)
public class QueryHandler implements IQueryHandler {

    private static final Logger logger = LoggerFactory.getLogger(QueryHandler.class);

    /**
     * Default language to use for filter parsers
     */
    private static final String DEFAULT_FILTER_LANGUAGE = "ldap";

    /**
     * SensiNact gateway thread
     */
    @Reference
    GatewayThread gatewayThread;

    /**
     * Current filter handler
     */
    private AtomicReference<IFilterHandler> filterHandlerRef = new AtomicReference<>();

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    void setFilterHandler(final IFilterHandler filterHandler) {
        synchronized (filterHandlerRef) {
            filterHandlerRef.set(filterHandler);
        }
    }

    void unsetFilterHandler(final IFilterHandler filterHandler) {
        synchronized (filterHandlerRef) {
            filterHandlerRef.set(null);
        }
    }

    @Override
    public AbstractResultDTO handleQuery(final SensiNactSession userSession, final AbstractQueryDTO query) {

        AbstractResultDTO result;
        try {
            switch (query.operation) {
            case LIST:
                result = handleList(userSession, (QueryListDTO) query);
                break;

            case DESCRIBE:
                result = handleDescribe(userSession, (QueryDescribeDTO) query);
                break;

            case GET:
                result = handleGet(userSession, (QueryGetDTO) query);
                break;

            case SET:
                result = handleSet(userSession, (QuerySetDTO) query);
                break;

            case ACT:
                result = handleAct(userSession, (QueryActDTO) query);
                break;

            default:
                result = new ErrorResultDTO(501, "Operation not implemented");
                break;
            }
        } catch (Throwable t) {
            logger.error("Error handling query {} on {}: {}", query.operation, query.uri, t.getMessage(), t);
            result = new ErrorResultDTO(t);
        }

        if (result == null) {
            result = new ErrorResultDTO(204, "No content");
        }

        if (result.uri == null) {
            result.uri = query.uri.toUri();
        }

        if (query.requestId != null) {
            result.requestId = query.requestId;
        }

        return result;
    }

    /**
     * Root of list handling
     *
     * @param userSession Caller session
     * @param dto         Query description
     * @return Result DTO
     */
    private AbstractResultDTO handleList(final SensiNactSession userSession, final QueryListDTO dto) throws Exception {
        final SensinactPath path = dto.uri;
        if (path == null || path.isEmpty()) {
            // No path object: list providers
            return listProviders(userSession, dto);
        } else if (path.targetsSpecificProvider()) {
            // List the services of a provider
            return listServices(userSession, dto, path.provider);
        } else if (path.targetsSpecificService()) {
            // List the resources of a service
            return listResources(userSession, dto, path.provider, path.service);
        }

        return new ErrorResultDTO(405,
                "List operation can be applied on the root, a specific provider or a specific service.");
    }

    /**
     * Root of description handling
     *
     * @param userSession Caller session
     * @param dto         Query description
     * @return Result DTO
     */
    private AbstractResultDTO handleDescribe(final SensiNactSession userSession, final QueryDescribeDTO dto)
            throws Exception {
        final SensinactPath path = dto.uri;
        if (path == null || path.isEmpty()) {
            return describeProviders(userSession, dto);
        } else if (path.targetsSpecificProvider()) {
            return describeProvider(userSession, path.provider);
        } else if (path.targetsSpecificService()) {
            return describeService(userSession, path.provider, path.service);
        } else if (path.targetsSpecificResource()) {
            return describeResource(userSession, path.provider, path.service, path.resource);
        }

        // Not allowed
        return new ErrorResultDTO(405, "Describe only works on specific provider, service or resource");
    }

    /**
     * Handle GET queries
     *
     * @param userSession Caller session
     * @param dto         Query description
     * @return Result DTO
     */
    private AbstractResultDTO handleGet(final SensiNactSession userSession, final QueryGetDTO dto) {
        final SensinactPath path = dto.uri;
        if (path == null || (!path.targetsSpecificResource() && !path.targetsSpecificMetadata())) {
            return new ErrorResultDTO(405, "GET only works on specific resource or metadata");
        }

        if (path.targetsSpecificMetadata()) {
            return new ErrorResultDTO(501, "Not implemented");
        }

        final ResourceDescription rcDesc = userSession.describeResource(path.provider, path.service, path.resource);
        if (rcDesc == null) {
            return new ErrorResultDTO(404, "Resource not found");
        }

        final ResourceShortDescription rcShortDesc = userSession.describeResourceShort(path.provider, path.service,
                path.resource);

        final TypedResponse<ResponseGetDTO> result = new TypedResponse<>(EResultType.GET_RESPONSE);
        result.response = new ResponseGetDTO();
        result.response.name = rcDesc.resource;
        if (rcShortDesc.contentType != null) {
            result.response.type = rcShortDesc.contentType.getName();
        } else if (rcDesc.value != null) {
            result.response.type = rcDesc.value.getClass().getName();
        }

        if (rcDesc.timestamp != null) {
            result.statusCode = 200;
            result.response.value = rcDesc.value;
            result.response.timestamp = rcDesc.timestamp.toEpochMilli();
        } else {
            // No time stamp = no content
            result.statusCode = 204;
            result.error = "No value set";
            result.response.value = null;
            result.response.timestamp = Instant.now().toEpochMilli();
        }

        return result;
    }

    /**
     * Handles resource value set
     *
     * @param userSession Caller session
     * @param dto         Query description
     * @return Result DTO
     */
    private AbstractResultDTO handleSet(final SensiNactSession userSession, final QuerySetDTO dto) {
        final SensinactPath path = dto.uri;
        if (!path.targetsSpecificResource() && !path.targetsSpecificMetadata()) {
            return new ErrorResultDTO(405, "Can only set a resource or its metadata");
        }

        final TypedResponse<ResponseGetDTO> result = new TypedResponse<>(EResultType.SET_RESPONSE);
        final ResponseGetDTO response = new ResponseGetDTO();

        // Force the timestamp so that we are sure we are coherent with what we return
        final Instant timestamp = Instant.now();
        final Object newValue = dto.value;

        if (path.targetsSpecificResource()) {
            response.name = path.resource;
            userSession.setResourceValue(path.provider, path.service, path.resource, newValue, timestamp);
        } else {
            response.name = path.resource + "/" + path.metadata;
            userSession.setResourceMetadata(path.provider, path.service, path.resource, path.metadata, newValue);
        }

        if (path.targetsSpecificResource()) {
            final ResourceShortDescription rcShortDesc = userSession.describeResourceShort(path.provider, path.service,
                    path.resource);
            response.timestamp = timestamp.toEpochMilli();
            response.type = rcShortDesc.contentType.getName();
        } else {
            response.timestamp = timestamp.toEpochMilli();
            response.type = null;
        }
        response.value = newValue;

        result.response = response;
        result.statusCode = 200;
        return result;
    }

    /**
     * Handles a call to ACT
     *
     * @param userSession Caller session
     * @param dto         Query description
     * @return Result DTO
     */
    private AbstractResultDTO handleAct(final SensiNactSession userSession, final QueryActDTO dto) {
        final SensinactPath path = dto.uri;
        if (!path.targetsSpecificResource()) {
            return new ErrorResultDTO(405, "ACT can only be used on resources");
        }

        final ResultActDTO result = new ResultActDTO();
        result.statusCode = 200;
        result.response = userSession.actOnResource(path.provider, path.service, path.resource, dto.parameters);
        return result;
    }

    /**
     * Parses the given filter
     *
     * @param filter         Filter string
     * @param filterLanguage Filter language
     * @return Parsed filter
     * @throws StatusException Error parsing filter
     */
    private ICriterion parseFilter(final String filter, final String filterLanguage) throws StatusException {
        synchronized (filterHandlerRef) {
            IFilterHandler filterHandler = filterHandlerRef.get();
            if (filterHandler == null) {
                throw new StatusException(501, "No filter implementation available");
            }

            try {
                return filterHandler.parseFilter(filterLanguage != null ? filterLanguage : DEFAULT_FILTER_LANGUAGE,
                        filter);
            } catch (Throwable t) {
                throw new StatusException(500, "Error parsing filter: " + t.getMessage());
            }
        }
    }

    /**
     * Executes the given parser
     *
     * @param filter         Filter string
     * @param filterLanguage Filter language
     * @return Matching snapshot
     * @throws StatusException Error parsing or executing filter
     */
    private Collection<ProviderSnapshot> executeFilter(final String filter, final String filterLanguage)
            throws StatusException {
        final ICriterion parsedFilter = parseFilter(filter, filterLanguage);
        try {
            return FilterCommandHelper.executeFilter(gatewayThread, parsedFilter);
        } catch (FilterException e) {
            throw new StatusException(500, "Error executing filter: " + e.getMessage());
        }
    }

    /**
     * Lists the providers
     *
     * @param userSession Caller session
     * @param query       List query
     * @return Result DTO
     */
    private AbstractResultDTO listProviders(final SensiNactSession userSession, final QueryListDTO query)
            throws Exception {

        final ResultListProvidersDTO result = new ResultListProvidersDTO();
        if (query.filter != null && !query.filter.isBlank()) {
            // Use a filter
            final Collection<ProviderSnapshot> filteredSnapshot;
            try {
                filteredSnapshot = executeFilter(query.filter, query.filterLanguage);
            } catch (StatusException e) {
                return e.toErrorResult();
            }

            result.providers = filteredSnapshot.stream().map(p -> p.getName()).collect(Collectors.toList());
            result.statusCode = 200;
        } else {
            // Direct listing
            result.providers = userSession.listProviders().stream().map(provider -> provider.provider)
                    .collect(Collectors.toList());
            result.statusCode = 200;
        }
        return result;
    }

    /**
     * Lists the services of a provider
     *
     * @param userSession Caller session
     * @param query       List query
     * @param providerId  Provider ID
     * @return Result DTO
     */
    private AbstractResultDTO listServices(final SensiNactSession userSession, final QueryListDTO query,
            final String providerId) throws Exception {

        final ProviderDescription providerDescr = userSession.describeProvider(providerId);
        if (providerDescr == null) {
            return new ErrorResultDTO(404, "Unknown provider");
        }

        final ResultListServicesDTO result = new ResultListServicesDTO();
        if (query.filter != null && !query.filter.isBlank()) {
            // Use a filter
            final Collection<ProviderSnapshot> filteredSnapshot;
            final ICriterion parsedFilter;
            synchronized (filterHandlerRef) {
                parsedFilter = parseFilter(query.filter, query.filterLanguage);
            }

            final UpdatableCriterion updatedCriterion = new UpdatableCriterion(parsedFilter);
            updatedCriterion.addProviderFilter(p -> providerId.equals(p.getName()));
            try {
                filteredSnapshot = FilterCommandHelper.executeFilter(gatewayThread, updatedCriterion);
            } catch (final Throwable t) {
                return new ErrorResultDTO(500, "Error executing filter: " + t.getMessage());
            }

            if (filteredSnapshot.isEmpty()) {
                result.services = List.of();
            } else {
                final ProviderSnapshot provider = filteredSnapshot.iterator().next();
                final ResourceValueFilter resourceValueFilter = updatedCriterion.getResourceValueFilter();
                final List<ServiceSnapshot> services;
                if (resourceValueFilter != null) {
                    services = provider.getServices().stream()
                            .filter(s -> resourceValueFilter.test(provider, s.getResources()))
                            .collect(Collectors.toList());
                } else {
                    services = provider.getServices();
                }
                result.services = services.stream().map(s -> s.getName()).collect(Collectors.toList());
            }
            result.statusCode = 200;
        } else {
            // Direct listing
            result.services = providerDescr.services;
            result.statusCode = 200;
        }
        return result;
    }

    /**
     * Lists the services of a provider
     *
     * @param userSession Caller session
     * @param query       Query list DTO
     * @param providerId  Provider ID
     * @param serviceId   Service ID
     * @return Result DTO
     */
    private AbstractResultDTO listResources(final SensiNactSession userSession, final QueryListDTO query,
            final String providerId, final String serviceId) throws Exception {
        final ServiceDescription serviceDescr = userSession.describeService(providerId, serviceId);
        if (serviceDescr == null) {
            return new ErrorResultDTO(404, "Unknown service");
        }

        final ResultListResourcesDTO result = new ResultListResourcesDTO();

        if (query.filter != null && !query.filter.isBlank()) {
            // Use a filter
            final Collection<ProviderSnapshot> filteredSnapshot;
            final ICriterion parsedFilter;
            synchronized (filterHandlerRef) {
                parsedFilter = parseFilter(query.filter, query.filterLanguage);
            }

            final UpdatableCriterion updatedCriterion = new UpdatableCriterion(parsedFilter);
            updatedCriterion.addProviderFilter(p -> providerId.equals(p.getName()));
            try {
                filteredSnapshot = FilterCommandHelper.executeFilter(gatewayThread, updatedCriterion);
            } catch (final Throwable t) {
                return new ErrorResultDTO(500, "Error executing filter: " + t.getMessage());
            }

            if (filteredSnapshot.isEmpty()) {
                result.resources = List.of();
            } else {
                final ProviderSnapshot provider = filteredSnapshot.iterator().next();
                // The admin service is
                final Optional<ServiceSnapshot> service = provider.getServices().stream()
                        .filter(s -> serviceId.equals(s.getName())).findFirst();
                if (service.isEmpty()) {
                    result.resources = List.of();
                } else {
                    final ResourceValueFilter resourceValueFilter = updatedCriterion.getResourceValueFilter();
                    final List<ResourceSnapshot> resources;
                    if (resourceValueFilter != null) {
                        resources = service.get().getResources().stream()
                                .filter(r -> resourceValueFilter.test(provider, List.of(r)))
                                .collect(Collectors.toList());
                    } else {
                        resources = service.get().getResources();
                    }
                    result.resources = resources.stream().map(s -> s.getName()).collect(Collectors.toList());
                }
            }
            result.statusCode = 200;
        } else {
            // Direct listing
            result.resources = serviceDescr.resources;
            result.statusCode = 200;
        }
        return result;
    }

    /**
     * One-line to construct an access method parameter DTO
     *
     * @param name Parameter name
     * @param type Parameter type
     * @return The parameter DTO
     */
    private AccessMethodParameterDTO makeParam(final String name, final String type) {
        final AccessMethodParameterDTO parameterDTO = new AccessMethodParameterDTO();
        parameterDTO.name = name;
        parameterDTO.type = type;
        return parameterDTO;
    }

    /**
     * Generates the methods to access the given resource
     *
     * @param rcDesc      Resource description
     * @param rcShortDesc Short resource description
     * @return Access methods descriptions
     */
    private List<AccessMethodDTO> generateAccessMethodsDescriptions(final ResourceDescription rcDesc,
            final ResourceShortDescription rcShortDesc) {
        final List<AccessMethodDTO> methods = new ArrayList<>();
        if (rcShortDesc.resourceType == ResourceType.ACTION) {
            // Only an action is available
            final AccessMethodDTO actMethod = new AccessMethodDTO();
            actMethod.name = "ACT";

            final List<Entry<String, Class<?>>> actMethodArgumentsTypes = rcShortDesc.actMethodArgumentsTypes;
            final List<AccessMethodParameterDTO> actParams = new ArrayList<>(actMethodArgumentsTypes.size());
            for (final Entry<String, Class<?>> entry : actMethodArgumentsTypes) {
                final AccessMethodParameterDTO param = new AccessMethodParameterDTO();
                param.name = entry.getKey();
                param.type = entry.getValue().getName();
                actParams.add(param);
            }
            actMethod.parameters = actParams;
            methods.add(actMethod);
        } else {
            // GET is available
            final AccessMethodDTO getMethod = new AccessMethodDTO();
            getMethod.name = "GET";
            getMethod.parameters = List.of(makeParam("attributeName", "string"));
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
            unsubscriptionMethod.parameters = List.of(makeParam("subscriptionId", "string"));
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

    /**
     * Generates the list of resource metadata
     *
     * @param rcDesc Resource description
     * @return Description of resource metadta
     */
    private List<MetadataDTO> generateMetadataDescriptions(final ResourceDescription rcDesc) {
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

    /**
     * Extracts a resource value from a provider snapshot
     *
     * @param provider Provider snapshot
     * @param service  Service name
     * @param rcName   Resource name
     * @return Resource TimedValue (can be null)
     */
    private TimedValue<?> getResourceValue(final ProviderSnapshot provider, final String service, final String rcName) {
        for (final ServiceSnapshot svc : provider.getServices()) {
            if (service.equals(svc.getName())) {
                for (final ResourceSnapshot rc : svc.getResources()) {
                    if (rcName.equals(rc.getName())) {
                        return rc.getValue();
                    }
                }
                // Service found, but no resource with the right name: stop here
                break;
            }
        }
        return null;
    }

    /**
     * Describes all providers
     *
     * @param userSession Caller session
     * @param query       The description query
     * @return Result DTO
     */
    private AbstractResultDTO describeProviders(final SensiNactSession userSession, final QueryDescribeDTO query)
            throws Exception {

        final Collection<ProviderSnapshot> providers;
        if (query.filter != null && !query.filter.isBlank()) {
            // Use a filter
            try {
                providers = executeFilter(query.filter, query.filterLanguage);
            } catch (StatusException e) {
                return e.toErrorResult();
            }
        } else {
            // Direct listing
            providers = userSession.filteredSnapshot(null);
        }

        final ResultDescribeProvidersDTO result = new ResultDescribeProvidersDTO();
        result.statusCode = 200;
        result.providers = new ArrayList<>(providers.size());

        for (final ProviderSnapshot provider : providers) {
            final CompleteProviderDescriptionDTO providerDto = new CompleteProviderDescriptionDTO();
            providerDto.name = provider.getName();

            TimedValue<?> value = getResourceValue(provider, "admin", "icon");
            if (value != null) {
                providerDto.icon = (String) value.getValue();
            }

            value = getResourceValue(provider, "admin", "location");
            if (value != null) {
                providerDto.location = (GeoJsonObject) value.getValue();
            }

            providerDto.services = new ArrayList<>(provider.getServices().size());
            for (final ServiceSnapshot service : provider.getServices()) {
                final ResponseDescribeServiceDTO svcDescription = completeServiceDescription(userSession,
                        provider.getName(), service.getName());
                if (svcDescription != null) {
                    providerDto.services.add(svcDescription);
                }
            }

            result.providers.add(providerDto);
        }

        return result;
    }

    /**
     * Describes a provider
     *
     * @param userSession Caller session
     * @param providerId  The ID of provider to describe
     * @return Result DTO
     */
    private AbstractResultDTO describeProvider(final SensiNactSession userSession, final String providerId)
            throws Exception {

        final ProviderDescription provider = userSession.describeProvider(providerId);
        if (provider == null) {
            return new ErrorResultDTO(404, "Unknown provider");
        }

        final TypedResponse<ResponseDescribeProviderDTO> result = new TypedResponse<>(EResultType.DESCRIBE_PROVIDER);
        result.statusCode = 200;
        result.response = new ResponseDescribeProviderDTO();
        result.response.name = provider.provider;
        result.response.services = provider.services;
        return result;
    }

    /**
     * Generates the complete description of a service. This description contains
     * its name and the description of its resources.
     *
     * @param providerId Provider ID
     * @param svcName    Service name
     * @return Complete description of the service
     */
    private final ResponseDescribeServiceDTO completeServiceDescription(final SensiNactSession userSession,
            final String providerId, final String svcName) {
        final ServiceDescription serviceDescription = userSession.describeService(providerId, svcName);
        if (serviceDescription == null) {
            return null;
        }

        final ResponseDescribeServiceDTO svcDto = new ResponseDescribeServiceDTO();
        svcDto.name = serviceDescription.service;
        svcDto.resources = new ArrayList<>(serviceDescription.resources.size());

        for (final String rcName : serviceDescription.resources) {
            final ResourceShortDescription rcDescription = userSession.describeResourceShort(providerId, svcName,
                    rcName);

            final ShortResourceDescriptionDTO rcDto = new ShortResourceDescriptionDTO();
            svcDto.resources.add(rcDto);
            rcDto.name = rcName;
            rcDto.type = rcDescription.resourceType;
            if (rcDto.type != ResourceType.ACTION) {
                rcDto.rws = EReadWriteMode.fromValueType(rcDescription.valueType);
            }
        }

        return svcDto;
    }

    /**
     * Describes a service
     *
     * @param userSession Caller session
     * @param providerId  The ID of service provider
     * @param serviceId   The ID of service to describe
     * @return Result DTO
     */
    private AbstractResultDTO describeService(final SensiNactSession userSession, final String providerId,
            final String serviceId) throws Exception {

        final TypedResponse<ResponseDescribeServiceDTO> result = new TypedResponse<>(EResultType.DESCRIBE_SERVICE);
        result.statusCode = 200;
        result.response = completeServiceDescription(userSession, providerId, serviceId);
        if (result.response == null) {
            return new ErrorResultDTO(404, "Service not found");
        }
        return result;
    }

    /**
     * Describes a resource
     *
     * @param userSession Caller session
     * @param providerId  The ID of service provider
     * @param serviceId   The ID of resource service
     * @param resourceId  The ID of resource to describe
     * @return Result DTO
     */
    private AbstractResultDTO describeResource(final SensiNactSession userSession, final String providerId,
            final String serviceId, final String resourceId) throws Exception {

        final ResourceDescription rcDesc = userSession.describeResource(providerId, serviceId, resourceId);
        if (rcDesc == null) {
            return new ErrorResultDTO(404, "Resource not set");
        }

        final ResourceShortDescription rcShortdesc = userSession.describeResourceShort(providerId, serviceId,
                resourceId);

        final TypedResponse<ResponseDescribeResourceDTO> result = new TypedResponse<>(EResultType.DESCRIBE_RESOURCE);
        result.statusCode = 200;
        result.response = new ResponseDescribeResourceDTO();
        result.response.name = rcDesc.resource;
        result.response.type = rcShortdesc.resourceType;
        result.response.accessMethods = generateAccessMethodsDescriptions(rcDesc, rcShortdesc);
        result.response.attributes = generateMetadataDescriptions(rcDesc);
        return result;
    }
}
