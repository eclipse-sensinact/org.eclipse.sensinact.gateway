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
package org.eclipse.sensinact.gateway.southbound.device.factory.impl;

import static java.util.stream.Collectors.toMap;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.chrono.ChronoZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.sensinact.core.annotation.dto.NullAction;
import org.eclipse.sensinact.core.metrics.IMetricTimer;
import org.eclipse.sensinact.core.metrics.IMetricsManager;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.push.DataUpdateException;
import org.eclipse.sensinact.core.push.FailedUpdatesException;
import org.eclipse.sensinact.core.push.dto.BulkGenericDto;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.gateway.geojson.Coordinates;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.gateway.southbound.device.factory.Constants;
import org.eclipse.sensinact.gateway.southbound.device.factory.DeviceFactoryException;
import org.eclipse.sensinact.gateway.southbound.device.factory.IDeviceMappingHandler;
import org.eclipse.sensinact.gateway.southbound.device.factory.IDeviceMappingParser;
import org.eclipse.sensinact.gateway.southbound.device.factory.IDeviceMappingRecord;
import org.eclipse.sensinact.gateway.southbound.device.factory.IPlaceHolderKeys;
import org.eclipse.sensinact.gateway.southbound.device.factory.IResourceMapping;
import org.eclipse.sensinact.gateway.southbound.device.factory.InvalidResourcePathException;
import org.eclipse.sensinact.gateway.southbound.device.factory.LocaleUtils;
import org.eclipse.sensinact.gateway.southbound.device.factory.MissingParserException;
import org.eclipse.sensinact.gateway.southbound.device.factory.ParserException;
import org.eclipse.sensinact.gateway.southbound.device.factory.RecordPath;
import org.eclipse.sensinact.gateway.southbound.device.factory.ValueType;
import org.eclipse.sensinact.gateway.southbound.device.factory.VariableNotFoundException;
import org.eclipse.sensinact.gateway.southbound.device.factory.dto.DeviceMappingConfigurationDTO;
import org.eclipse.sensinact.gateway.southbound.device.factory.dto.DeviceMappingOptionsDTO;
import org.osgi.service.component.ComponentServiceObjects;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.util.promise.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Handles the common code of device factory
 */
@Component(immediate = true, service = IDeviceMappingHandler.class)
public class FactoryParserHandler implements IDeviceMappingHandler, IPlaceHolderKeys {

    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(FactoryParserHandler.class);

    /**
     * Keeps track of the state of the current record
     */
    private static class RecordState {
        /**
         * Place holder -&gt; record path
         */
        Map<String, IResourceMapping> placeholders;

        /**
         * Variable name -&gt; unresolved record path
         */
        Map<String, IResourceMapping> rawVariables;

        /**
         * Variable name -&gt; resolved record path
         */
        Map<String, String> variables;

        /**
         * SensiNact resource path -&gt; record path
         */
        List<ResourceRecordMapping> rcMappings;

        /**
         * SensiNact resource path -&gt; literal record path
         */
        List<ResourceLiteralMapping> rcLiterals;
    }

    /**
     * Available parsers
     */
    private final Map<String, List<ComponentServiceObjects<IDeviceMappingParser>>> parsers = new ConcurrentHashMap<>();

    /**
     * SensiNact update endpoint
     */
    @Reference
    DataUpdate dataUpdate;

    /**
     * SensiNact metrics gathering
     */
    @Reference
    IMetricsManager metrics;

    /**
     * JSON mapper
     */
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * New parser service registered
     */
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    void addParser(final ComponentServiceObjects<IDeviceMappingParser> parser, final Map<String, Object> properties) {
        parsers.merge((String) properties.get(IDeviceMappingParser.PARSER_ID), List.of(parser),
                (x, v) -> Stream.concat(Stream.of(parser), v.stream()).collect(Collectors.toList()));
    }

    /**
     * Parser service unregistered
     */
    void removeParser(final ComponentServiceObjects<IDeviceMappingParser> parser,
            final Map<String, Object> properties) {
        parsers.computeIfPresent((String) properties.get(IDeviceMappingParser.PARSER_ID), (x, v) -> {
            List<ComponentServiceObjects<IDeviceMappingParser>> list = v.stream()
                    .filter(c -> !c.getServiceReference().equals(parser.getServiceReference()))
                    .collect(Collectors.toList());
            return list.isEmpty() ? null : list;
        });
    }

    /**
     * Looks for the parser with the given ID
     */
    private ComponentServiceObjects<IDeviceMappingParser> findParser(final String parserId)
            throws MissingParserException {
        final List<ComponentServiceObjects<IDeviceMappingParser>> matchingParsers = parsers.get(parserId);
        if (matchingParsers == null || matchingParsers.isEmpty()) {
            throw new MissingParserException(String.format("No parser found with ID '%s'", parserId));
        }

        return matchingParsers.get(0);
    }

    @Override
    public void handle(final DeviceMappingConfigurationDTO configuration, final Map<String, String> context,
            final byte[] payload) throws DeviceFactoryException {

        // Check parser ID
        final String parserId = configuration.parser;
        if (parserId == null || parserId.isBlank()) {
            throw new MissingParserException("No parser ID given");
        }

        // Extract mapping information
        final RecordState globalState;
        try (IMetricTimer timer = metrics.withTimer("device.factory.mapping.setup.time")) {
            globalState = computeInitialState(configuration, context);
        }

        // Check if a provider is set
        if (globalState.placeholders.get(KEY_PROVIDER) == null) {
            throw new IllegalArgumentException("No provider mapping given");
        }

        final boolean logErrors = configuration.mappingOptions.logErrors;

        // Find it
        final ComponentServiceObjects<IDeviceMappingParser> cso = findParser(parserId);
        final IDeviceMappingParser parser = cso.getService();
        try {
            // Use it
            final List<? extends IDeviceMappingRecord> records;
            try (IMetricTimer timer = metrics.withTimer("device.factory.parse." + parserId + ".time")) {
                records = parser.parseRecords(payload, configuration.parserOptions, context);
            }

            if (records != null) {
                final BulkGenericDto bulk = new BulkGenericDto();
                bulk.dtos = new ArrayList<>();

                for (final IDeviceMappingRecord record : records) {
                    try {
                        try (IMetricTimer timer = metrics.withTimer("device.factory.record.mapping.time")) {
                            bulk.dtos.addAll(handleRecord(configuration, globalState, record));
                        }
                    } catch (InvalidResourcePathException | ParserException | VariableNotFoundException e) {
                        if (logErrors) {
                            logger.error("Error parsing record with parser {}: {}", parserId, e.getMessage(), e);
                        }
                    }
                }

                if (!bulk.dtos.isEmpty()) {
                    // Send all updates to the gateway thread at once
                    Promise<?> pushUpdate = dataUpdate.pushUpdate(bulk);
                    if (logErrors) {
                        pushUpdate = pushUpdate.onFailure((t) -> {
                            if (t instanceof FailedUpdatesException) {
                                for (DataUpdateException ex : ((FailedUpdatesException) t).getFailedUpdates()) {
                                    logger.error("Error updating digital twin of {}/{}/{} with parser {}: {}",
                                            ex.getProvider(), ex.getService(), ex.getResource(), parserId,
                                            ex.getMessage(), ex);
                                }
                            } else {
                                logger.error("Error updating digital twin with parser {}: {}", parserId, t.getMessage(),
                                        t);
                            }
                        });
                    }
                }
            } else if (logErrors) {
                logger.error("No record found by parser {}", parserId);
            }
        } catch (Exception e) {
            if (logErrors) {
                logger.error("Error parsing payload with parser {}", parserId, e);
            }
            throw e;
        } finally {
            cso.ungetService(parser);
        }
    }

    /**
     * Returns the resource value as a string
     *
     * @param record  Current record
     * @param mapping Resource mapping
     * @param options Device mapping options
     * @return The value as a string (can be null)
     */
    private String getFieldString(final IDeviceMappingRecord record, final IResourceMapping mapping,
            final DeviceMappingOptionsDTO options) {
        if (mapping.isLiteral()) {
            final Object value = ((ResourceLiteralMapping) mapping).getValue();
            return value != null ? String.valueOf(value) : null;
        } else {
            final RecordPath path = ((ResourceRecordMapping) mapping).getRecordPath();
            return record.getFieldString(path, options);
        }
    }

    /**
     * Returns the resource value as is
     *
     * @param record  Current record
     * @param mapping Resource mapping
     * @param options Device mapping options
     * @return The value as is (can be null)
     */
    private Object getFieldValue(final IDeviceMappingRecord record, final IResourceMapping mapping,
            final DeviceMappingOptionsDTO options) {
        if (mapping.isLiteral()) {
            return ((ResourceLiteralMapping) mapping).getTypedValue(options);
        } else {
            final RecordPath path = ((ResourceRecordMapping) mapping).getRecordPath();
            return record.getField(path, options);
        }
    }

    /**
     * Handles a record, i.e. an entry containing fields
     *
     * @param session       sensiNact session to use to update resources
     * @param configuration Mapping configuration (must contain the parser ID)
     * @param record        Record to read
     * @return True if resources have been updated
     * @throws InvalidResourcePathException Invalid mapping
     * @throws ParserException              Error parsing content
     * @throws VariableNotFoundException    Error resolving variables
     */
    private List<GenericDto> handleRecord(final DeviceMappingConfigurationDTO configuration,
            final RecordState globalState, final IDeviceMappingRecord record)
            throws InvalidResourcePathException, ParserException, VariableNotFoundException {

        final DeviceMappingOptionsDTO options = configuration.mappingOptions;
        final RecordState recordState = computeRecordState(configuration, globalState, record);

        // Extract the provider
        final String provider = getFieldString(record, recordState.placeholders.get(KEY_PROVIDER), options);
        if (provider == null || provider.isBlank()) {
            throw new ParserException("Empty provider field");
        }
        // Extract the modelPackageUri
        String modelPackageUri = null;
        if (recordState.placeholders.containsKey(KEY_MODEL_PACKAGE_URI)) {
            modelPackageUri = getFieldString(record, recordState.placeholders.get(KEY_MODEL_PACKAGE_URI), options);
        }

        // Extract the model
        final String model;
        if (recordState.placeholders.containsKey(KEY_MODEL)) {
            model = getFieldString(record, recordState.placeholders.get(KEY_MODEL), options);
            if (model == null || model.isBlank()) {
                throw new ParserException("Empty model field for " + provider);
            }
        } else {
            model = provider;
        }

        // Bulk update preparation
        final List<GenericDto> bulk = new ArrayList<>();

        // Compute the timestamp
        final Instant timestamp = computeTimestamp(provider, record, recordState.placeholders, configuration);

        // Get the friendly name
        final IResourceMapping nameKey = recordState.placeholders.get(KEY_NAME);
        if (nameKey != null) {
            final String name = getFieldString(record, nameKey, options);
            if (name != null) {
                bulk.add(makeDto(modelPackageUri, model, provider, "admin", "friendlyName", name, timestamp));
            }
        }

        // Compute location, if any
        final GeoJsonObject location;
        try {
            location = computeLocation(record, recordState.placeholders, configuration.mappingOptions);
            if (location != null) {
                bulk.add(makeDto(modelPackageUri, model, provider, "admin", "location", location, timestamp));
            }
        } catch (JsonProcessingException e) {
            throw new ParserException("Error parsing location of " + provider, e);
        }

        // Loop on resources
        final Map<String, Map<String, Map<String, Object>>> allMetadata = new HashMap<>();
        for (final ResourceRecordMapping rcMapping : recordState.rcMappings) {
            final String service = rcMapping.getService();
            final String rcName = rcMapping.getResource();

            try {
                final Object value = record.getField(rcMapping.getRecordPath(), options);
                if (value != Constants.IGNORE) {
                    final ValueType valueType = rcMapping.getRecordPath().getValueType();
                    if (rcMapping.isMetadata()) {
                        allMetadata.computeIfAbsent(service, k -> new HashMap<>())
                                .computeIfAbsent(rcName, k -> new HashMap<>()).put(rcMapping.getMetadata(), value);
                    } else {
                        bulk.add(makeDto(modelPackageUri, model, provider, service, rcName, value,
                                valueType.toJavaClass(), timestamp));
                    }
                }
            } catch (Exception e) {
                logger.warn("Error reading mapping for {}/{}/{}: {}", provider, service, rcName, e.getMessage());
            }
        }

        // Loop on literals
        for (final ResourceLiteralMapping rcLiteral : recordState.rcLiterals) {
            final String service = rcLiteral.getService();
            final String rcName = rcLiteral.getResource();
            try {
                final Object value = rcLiteral.getTypedValue(options);
                if (value != Constants.IGNORE) {
                    final ValueType valueType = rcLiteral.getValueType();
                    if (rcLiteral.isMetadata()) {
                        allMetadata.computeIfAbsent(service, k -> new HashMap<>())
                                .computeIfAbsent(rcName, k -> new HashMap<>()).put(rcLiteral.getMetadata(), value);
                    } else {
                        bulk.add(makeDto(modelPackageUri, model, provider, service, rcName, value,
                                valueType.toJavaClass(), timestamp));
                    }
                }
            } catch (Exception e) {
                logger.warn("Error reading mapping for {}/{}/{}: {}", provider, service, rcName, e.getMessage());
            }
        }

        // Remove null entries
        bulk.removeIf(Objects::isNull);

        // Set the null action to all DTOs
        bulk.stream().forEach(dto -> {
            dto.nullAction = options.nullAction;
        });

        // Add metadata updates afterwards, to avoid the null action to be overwritten
        for (Entry<String, Map<String, Map<String, Object>>> svcEntry : allMetadata.entrySet()) {
            final String svcName = svcEntry.getKey();
            for (Entry<String, Map<String, Object>> rcEntry : svcEntry.getValue().entrySet()) {
                bulk.add(makeMetadataDto(modelPackageUri, model, provider, svcName, rcEntry.getKey(),
                        rcEntry.getValue()));
            }
        }

        return bulk;
    }

    /**
     * Prepares a generic DTO from the given information
     */
    private GenericDto makeDto(final String modelPackageUri, final String model, final String provider,
            final String service, final String resource, final Object value, final Instant timestamp) {
        return makeDto(modelPackageUri, model, provider, service, resource, value, null, timestamp);
    }

    /**
     * Prepares a generic DTO from the given information
     */
    private GenericDto makeDto(final String modelPackageUri, final String model, final String provider,
            final String service, final String resource, final Object value, final Class<?> valueType,
            final Instant timestamp) {
        final GenericDto dto = new GenericDto();
        dto.modelPackageUri = modelPackageUri;
        dto.model = model;
        dto.provider = provider;
        dto.service = service;
        dto.resource = resource;
        dto.value = value;
        if (valueType != null) {
            dto.type = valueType;
        } else if (value != null) {
            dto.type = value.getClass();
        } else {
            logger.debug("Ignoring {}/{}/{}: null value without explicit type", provider, service, resource);
            return null;
        }
        if (timestamp != null) {
            dto.timestamp = timestamp;
        }
        return dto;
    }

    /**
     * Prepares a resource metadata update DTO
     */
    private GenericDto makeMetadataDto(final String modelPackageUri, final String model, final String provider,
            final String service, final String resource, final Map<String, Object> metadata) {
        final GenericDto dto = new GenericDto();
        dto.modelPackageUri = modelPackageUri;
        dto.model = model;
        dto.provider = provider;
        dto.service = service;
        dto.resource = resource;
        dto.metadata = metadata;
        dto.nullAction = NullAction.IGNORE;
        return dto;
    }

    /**
     * Computes the list of place holders, variables and resource mapping
     *
     * @param configuration Device mapping configuration
     * @param context
     * @return The global state
     * @throws InvalidResourcePathException Error parsing resource path
     */
    private RecordState computeInitialState(DeviceMappingConfigurationDTO configuration, Map<String, String> context)
            throws InvalidResourcePathException {
        final Map<String, IResourceMapping> placeholders = new HashMap<>();
        final Map<String, IResourceMapping> variablesMappings = new HashMap<>();
        final List<ResourceRecordMapping> rcMappings = new ArrayList<>();
        final List<ResourceLiteralMapping> rcLiterals = new ArrayList<>();

        final ResourceMappingHandler handler = new ResourceMappingHandler();

        for (Entry<String, Object> entry : configuration.mapping.entrySet()) {
            final String key = entry.getKey();
            final IResourceMapping mapping = handler.parseMapping(key, entry.getValue());
            if (key.startsWith("@")) {
                // Placeholder
                placeholders.put(key, mapping);
            } else if (key.startsWith("$") && !key.startsWith("${")) {
                // Variable definition
                if (VariableSolver.isValidKey(key)) {
                    variablesMappings.put(key, mapping);
                } else {
                    throw new InvalidResourcePathException(String.format("Invalid variable format: '%s'", key));
                }
            } else {
                // Mapping
                if (mapping.isLiteral()) {
                    rcLiterals.add((ResourceLiteralMapping) mapping);
                } else {
                    rcMappings.add((ResourceRecordMapping) mapping);
                }
            }
        }

        final RecordState state = new RecordState();
        state.placeholders = Map.copyOf(placeholders);
        state.rawVariables = Map.copyOf(variablesMappings);
        state.rcMappings = List.copyOf(rcMappings);
        state.rcLiterals = List.copyOf(rcLiterals);
        state.variables = Map.copyOf(
                context.entrySet().stream().collect(toMap(e -> "$context.".concat(e.getKey()), Entry::getValue)));
        return state;
    }

    /**
     * Computes the state associated to this mapping
     *
     * @param configuration Mapping configuration
     * @param record        Current record
     * @return The state of the current record
     * @throws InvalidResourcePathException Error parsing a resource or record path
     * @throws ParserException              Error parsing content
     * @throws VariableNotFoundException    Error resolving a variable
     */
    private RecordState computeRecordState(final DeviceMappingConfigurationDTO configuration,
            final RecordState initialState, final IDeviceMappingRecord record)
            throws InvalidResourcePathException, ParserException, VariableNotFoundException {

        final RecordState state = new RecordState();

        // Resolve variables
        state.variables = resolveVariables(configuration, record, initialState.variables, initialState.rawVariables);

        // Replace values
        state.placeholders = fillInVariables(initialState.placeholders, state.variables);

        state.rcMappings = new ArrayList<>(initialState.rcMappings.size());
        for (final ResourceRecordMapping rcMapping : initialState.rcMappings) {
            state.rcMappings.add((ResourceRecordMapping) rcMapping.fillInVariables(state.variables).ensureValidPath());
        }

        state.rcLiterals = new ArrayList<>(initialState.rcLiterals.size());
        for (final ResourceLiteralMapping rcMapping : initialState.rcLiterals) {
            state.rcLiterals.add((ResourceLiteralMapping) rcMapping.fillInVariables(state.variables).ensureValidPath());
        }
        return state;
    }

    /**
     * Fills in variables in the given map
     *
     * @param placeholders Map with keys and values that can contain variables
     * @param variables    Resolved variables
     * @return A new map with resolved placeholders
     * @throws InvalidResourcePathException Invalid resolved resource key
     * @throws VariableNotFoundException    Error resolving variables
     */
    private Map<String, IResourceMapping> fillInVariables(final Map<String, IResourceMapping> placeholders,
            final Map<String, String> variables) throws VariableNotFoundException, InvalidResourcePathException {

        final Map<String, IResourceMapping> newPlaceholders = new HashMap<>(placeholders.size());
        for (Entry<String, IResourceMapping> entry : placeholders.entrySet()) {
            // Update key
            final String newKey = VariableSolver.fillInVariables(entry.getKey(), variables);
            final IResourceMapping newValue = entry.getValue().fillInVariables(variables);
            newPlaceholders.put(newKey, newValue);
        }

        return newPlaceholders;
    }

    /**
     * Assigns a value to each variable or throws an exception
     *
     * @param configuration Mapping configuration
     * @param record        Current record
     * @param rawVariables  Definitions of variables
     * @return
     * @throws ParserException Error resolving variables
     */
    private Map<String, String> resolveVariables(final DeviceMappingConfigurationDTO configuration,
            final IDeviceMappingRecord record, final Map<String, String> initialVariables,
            final Map<String, IResourceMapping> rawVariables) throws ParserException {

        final DeviceMappingOptionsDTO options = configuration.mappingOptions;

        Set<String> previouslyRemaining = new HashSet<>();
        final Set<String> remainingVars = new HashSet<>(rawVariables.keySet());
        final Map<String, String> resolvedVars = new HashMap<>(initialVariables);

        while (!remainingVars.isEmpty()) {
            if (previouslyRemaining.equals(remainingVars)) {
                throw new ParserException("Can't resolve variables: " + remainingVars);
            }

            previouslyRemaining = Set.copyOf(remainingVars);
            final Set<String> resolved = new HashSet<>();
            for (final String varName : remainingVars) {
                final IResourceMapping mapping = rawVariables.get(varName);
                if (mapping.isLiteral()) {
                    final Object value = ((ResourceLiteralMapping) mapping).getValue();
                    if (value != null) {
                        resolvedVars.put(varName, String.valueOf(value));
                        resolved.add(varName);
                    }
                } else {
                    final String path = ((ResourceRecordMapping) mapping).getRecordPath().asString();
                    final Matcher matcher = VariableSolver.varUsePattern.matcher(path);
                    if (matcher.find()) {
                        String newPath = path;
                        boolean fullyResolved = true;
                        do {
                            final String innerVar = matcher.group(1);
                            final Object resolvedPath = resolvedVars.get("$" + innerVar);
                            if (resolvedPath != null) {
                                newPath = newPath.replace("${" + innerVar + "}", String.valueOf(resolvedPath));
                            } else {
                                // Not yet available
                                fullyResolved = false;
                                break;
                            }

                            if (fullyResolved) {
                                resolvedVars.put(varName, newPath);
                                resolved.add(varName);
                            }
                        } while (matcher.find());
                    } else {
                        // Direct field access
                        resolvedVars.put(varName, getFieldString(record, mapping, options));
                        resolved.add(varName);
                    }
                }
            }

            remainingVars.removeAll(resolved);
        }

        return resolvedVars;
    }

    /**
     * Looks for a location in the given record
     *
     * @param record       Record to read
     * @param placeholders Defined mapping placeholders
     * @param options      Mapping options
     * @return The parsed location as a GeoJSON string or null
     * @throws JsonProcessingException Error parsing GeoJSON
     */
    private GeoJsonObject computeLocation(final IDeviceMappingRecord record,
            final Map<String, IResourceMapping> placeholders, final DeviceMappingOptionsDTO options)
            throws JsonProcessingException {

        final IResourceMapping locationPath = placeholders.get(KEY_LOCATION);
        if (locationPath != null) {
            final Object locationValue = getFieldValue(record, locationPath, options);
            if (locationValue != null) {
                if (locationValue instanceof String) {
                    final String strLocation = (String) locationValue;
                    if (strLocation.startsWith("{")) {
                        // GeoJSON string
                        return mapper.readValue(strLocation, GeoJsonObject.class);
                    } else if (strLocation.contains(":")) {
                        // Short format location: lat:lon[:alt]
                        final String[] parts = strLocation.split(":");
                        if (parts.length >= 2) {
                            return makeLocation(parts[0], parts[1], parts.length >= 3 ? parts[2] : null,
                                    options.logErrors);
                        }
                    }
                } else if (locationValue instanceof Map) {
                    // Got a map, consider it a GeoJSON input
                    return mapper.convertValue(locationValue, GeoJsonObject.class);
                }
            }
        }

        // Fallback on fields
        final IResourceMapping latitudePath = placeholders.get(KEY_LATITUDE);
        final IResourceMapping longitudePath = placeholders.get(KEY_LONGITUDE);
        if (latitudePath == null || longitudePath == null) {
            // No lat/lon data
            return null;
        }

        final Object latitude = getFieldValue(record, latitudePath, options);
        final Object longitude = getFieldValue(record, longitudePath, options);
        Object altitude = null;

        final IResourceMapping altitudePath = placeholders.get(KEY_ALTITUDE);
        if (altitudePath != null) {
            altitude = getFieldValue(record, altitudePath, options);
        }

        return makeLocation(latitude, longitude, altitude, options.logErrors);
    }

    /**
     * Tries to obtain a float from the given value
     *
     * @param value A record value
     * @return A float or null
     */
    private Float toFloat(final Object value) {

        if (value == null) {
            return null;
        }

        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }

        try {
            return Float.valueOf(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Tries to obtain a long from the given value
     *
     * @param value A record value
     * @return A long or null
     */
    private Long toLong(final Object value) {

        if (value == null) {
            return null;
        }

        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        try {
            return Long.valueOf(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Tries to obtain an {@link Instant} from the given value
     *
     * @param provider Parsed provider
     * @param value    A record value
     * @return An instant or null
     */
    private Instant toInstant(final String provider, final Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Instant) {
            return (Instant) value;
        }

        if (value instanceof Number) {
            return convertTimestamp(provider, ((Number) value).longValue());
        }

        if (value instanceof ChronoZonedDateTime) {
            return ((ChronoZonedDateTime<?>) value).toInstant();
        }

        final Long timestamp = toLong(value);
        if (timestamp != null) {
            return convertTimestamp(provider, timestamp);
        }
        return null;
    }

    /**
     * Prepares a GeoJSON object for the point at the given location
     */
    private Point makeLocation(final Object latitude, final Object longitude, final Object altitude,
            final boolean logErrors) {
        Float lat = toFloat(latitude);
        Float lon = toFloat(longitude);
        Float alt = toFloat(altitude);

        if (lat == null || lon == null) {
            // Invalid location
            if (logErrors) {
                logger.debug("Invalid location: lat={} lon={} alt={}", lat, lon, alt);
            }
            return null;
        }

        final Point location = new Point();
        location.coordinates = new Coordinates();
        location.coordinates.longitude = lon;
        location.coordinates.latitude = lat;
        if (alt != null) {
            location.coordinates.elevation = alt;
        }
        return location;
    }

    /**
     * Parse the configured timezone, UTC by default
     *
     * @param dateTimezone Timezone from mapping configuration
     * @return Parsed timezone or UTC
     */
    private ZoneId getTimezone(final String dateTimezone) {
        if (dateTimezone == null || dateTimezone.isBlank()) {
            return ZoneOffset.UTC;
        } else {
            try {
                return ZoneId.of(dateTimezone);
            } catch (DateTimeException e) {
                logger.warn("Can't parse configured timezone '{}': {}", dateTimezone, e.getMessage());
                return ZoneOffset.UTC;
            }
        }
    }

    /**
     * Looks for a time value in the given record
     *
     * @param provider      Parsed provider
     * @param record        Record to read
     * @param placeholders  Defined mapping placeholders
     * @param configuration Mapping configuration
     * @return The parsed timestamp or the current time
     */
    private Instant computeTimestamp(final String provider, final IDeviceMappingRecord record,
            final Map<String, IResourceMapping> placeholders, final DeviceMappingConfigurationDTO configuration) {

        final DeviceMappingOptionsDTO options = configuration.mappingOptions;

        final IResourceMapping timestampPath = placeholders.get(KEY_TIMESTAMP);
        if (timestampPath != null) {
            final Instant timestamp = toInstant(provider, getFieldValue(record, timestampPath, options));
            if (timestamp != null) {
                return timestamp;
            }
        }

        final ZoneId timezone = getTimezone(configuration.mappingOptions.dateTimezone);
        final Locale locale = LocaleUtils.fromString(configuration.mappingOptions.formatDateTimeLocale);

        final IResourceMapping dateTimePath = placeholders.get(KEY_DATETIME);
        if (dateTimePath != null) {
            final String strDateTime = getFieldString(record, dateTimePath, options);
            if (strDateTime != null && !strDateTime.isBlank()) {
                final TemporalAccessor parsedDateTime = parseDateTime(strDateTime, configuration.mappingOptions,
                        locale);
                return extractDateTime(parsedDateTime, timezone);
            }
        }

        LocalDate date = null;
        final IResourceMapping datePath = placeholders.get(KEY_DATE);
        if (datePath != null) {
            final String strDate = getFieldString(record, datePath, options);
            if (strDate != null && !strDate.isBlank()) {
                date = parseDate(strDate, configuration.mappingOptions, locale);
            }
        }

        OffsetTime time = null;
        final IResourceMapping timePath = placeholders.get(KEY_TIME);
        if (timePath != null) {
            final String strTime = getFieldString(record, timePath, options);
            if (strTime != null && !strTime.isBlank()) {
                time = parseTime(strTime, date, timezone, configuration.mappingOptions, locale);
            }
        }

        if (date == null) {
            if (time == null) {
                return Instant.now();
            } else {
                return LocalDate.now().atTime(time).toInstant();
            }
        } else if (time == null) {
            return date.atStartOfDay(timezone).toInstant();
        } else {
            return time.atDate(date).toInstant();
        }
    }

    /**
     * Extract an {@link LocalDate} from the given parsed date
     */
    private LocalDate extractDate(final TemporalAccessor parsedDate) {
        int year;
        try {
            year = parsedDate.get(ChronoField.YEAR);
        } catch (Exception e) {
            year = parsedDate.get(ChronoField.YEAR_OF_ERA);
        }

        return LocalDate.of(year, parsedDate.get(ChronoField.MONTH_OF_YEAR), parsedDate.get(ChronoField.DAY_OF_MONTH));
    }

    /**
     * Extract an {@link OffsetTime} from the given parsed time
     *
     * @param parsedTime   Parsed input
     * @param expectedDate Date associated to the given time
     * @param timezone     Fallback timezone
     */
    private OffsetTime extractTime(final TemporalAccessor parsedTime, final LocalDate expectedDate,
            final ZoneId timezone) {
        final int hour = parsedTime.get(ChronoField.HOUR_OF_DAY);

        int minute;
        try {
            minute = parsedTime.get(ChronoField.MINUTE_OF_HOUR);
        } catch (Exception e) {
            minute = 0;
        }

        int second;
        try {
            second = parsedTime.get(ChronoField.SECOND_OF_MINUTE);
        } catch (Exception e) {
            second = 0;
        }

        int nanoOfSecond;
        try {
            nanoOfSecond = parsedTime.get(ChronoField.NANO_OF_SECOND);
        } catch (Exception e) {
            nanoOfSecond = 0;
        }

        ZoneOffset offset;
        try {
            offset = ZoneOffset.ofTotalSeconds(parsedTime.get(ChronoField.OFFSET_SECONDS));
        } catch (UnsupportedTemporalTypeException e) {
            if (expectedDate != null) {
                offset = timezone.getRules().getOffset(expectedDate.atTime(hour, minute, second, nanoOfSecond));
            } else {
                offset = timezone.getRules().getOffset(Instant.now());
            }
        }

        return OffsetTime.of(hour, minute, second, nanoOfSecond, offset);
    }

    /**
     * Parses a date string
     *
     * @param strDate Date string
     * @param options Device mapping options
     * @param locale  Configured locale
     * @return The parsed date
     */
    private LocalDate parseDate(final String strDate, final DeviceMappingOptionsDTO options, final Locale locale) {
        DateTimeFormatter format = DateTimeFormatter.ISO_LOCAL_DATE;
        if (options.formatDate != null && !options.formatDate.isBlank()) {
            format = DateTimeFormatter.ofPattern(options.formatDate);
        } else if (options.formatDateStyle != null && !options.formatDateStyle.isBlank()) {
            format = DateTimeFormatter.ofLocalizedDate(FormatStyle.valueOf(options.formatDateStyle.toUpperCase()));
        }

        if (locale != null) {
            format = format.withLocale(locale);
        }

        return extractDate(format.parse(strDate));
    }

    /**
     * Parses a time string
     *
     * @param strTime      Time string
     * @param expectedDate Expected date of the time (today if null)
     * @param timezone     Fallback timezone
     * @param options      Device mapping options
     * @param locale       Configured locale
     * @return The parsed time at the expected date or today
     */
    private OffsetTime parseTime(final String strTime, final LocalDate expectedDate, final ZoneId timezone,
            final DeviceMappingOptionsDTO options, final Locale locale) {
        DateTimeFormatter format = DateTimeFormatter.ISO_OFFSET_TIME;
        if (options.formatTime != null && !options.formatTime.isBlank()) {
            format = DateTimeFormatter.ofPattern(options.formatTime);
        } else if (options.formatTimeStyle != null && !options.formatTimeStyle.isBlank()) {
            format = DateTimeFormatter.ofLocalizedDate(FormatStyle.valueOf(options.formatTimeStyle.toUpperCase()));
        }

        if (locale != null) {
            format = format.withLocale(locale);
        }

        return extractTime(format.parse(strTime), expectedDate, timezone);
    }

    /**
     * Parses a date time according to mapping options
     *
     * @param strDateTime String value of the date time
     * @param options     Mapping options
     * @param locale      Configured locale
     * @return The parsed date time
     */
    private TemporalAccessor parseDateTime(final String strDateTime, final DeviceMappingOptionsDTO options,
            final Locale locale) {
        DateTimeFormatter format = DateTimeFormatter.ISO_DATE_TIME;

        final String formatDateTime = options.formatDateTime;
        if (formatDateTime != null && !formatDateTime.isBlank()) {
            format = DateTimeFormatter.ofPattern(formatDateTime);
        } else {
            String formatDateStyle = options.formatDateStyle;
            String formatTimeStyle = options.formatTimeStyle;

            if (formatTimeStyle == null || formatTimeStyle.isBlank()) {
                formatTimeStyle = formatDateStyle;
            } else if (formatDateStyle == null || formatDateStyle.isBlank()) {
                formatDateStyle = formatTimeStyle;
            }

            if (formatTimeStyle != null && !formatTimeStyle.isBlank()) {
                format = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.valueOf(formatDateStyle.toUpperCase()),
                        FormatStyle.valueOf(formatTimeStyle.toUpperCase()));
            }
        }

        if (locale != null) {
            format = format.withLocale(locale);
        }

        return format.parse(strDateTime);
    }

    /**
     * Extract date and time from a parsed temporal accessor
     *
     * @param parsedTime Parsed date time
     * @param timezone   Fallback timezone
     * @return The parsed date as an instant
     */
    private Instant extractDateTime(final TemporalAccessor parsedTime, final ZoneId timezone) {
        final LocalDate date = extractDate(parsedTime);
        final OffsetTime offsetTime = extractTime(parsedTime, date, timezone);
        final OffsetDateTime dateTime = OffsetDateTime.of(date, offsetTime.toLocalTime(), offsetTime.getOffset());
        return dateTime.toInstant();
    }

    /**
     * Converts a timestamp to an Instant
     *
     * @param provider  Parsed provider
     * @param timestamp Parsed timestamp
     * @return The instant at the timestamp
     */
    private Instant convertTimestamp(final String provider, final Long timestamp) {

        int log10ms = (int) Math.log10(System.currentTimeMillis());
        int timestampLog = (int) Math.log10(timestamp);

        if (timestampLog > (log10ms + 2)) {
            // Over 500 years in the future - guess nanos
            return Instant.EPOCH.plusNanos(timestamp);
        } else if (timestampLog < (log10ms - 1)) {
            // Over 45 years in the past, guess seconds
            return Instant.ofEpochSecond(timestamp);
        } else {
            return Instant.ofEpochMilli(timestamp);
        }
    }
}
