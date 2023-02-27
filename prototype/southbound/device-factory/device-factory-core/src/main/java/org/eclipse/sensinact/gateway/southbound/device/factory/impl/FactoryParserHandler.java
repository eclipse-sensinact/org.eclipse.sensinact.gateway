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

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.sensinact.gateway.geojson.Coordinates;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.gateway.southbound.device.factory.DeviceFactoryException;
import org.eclipse.sensinact.gateway.southbound.device.factory.IDeviceMappingHandler;
import org.eclipse.sensinact.gateway.southbound.device.factory.IDeviceMappingParser;
import org.eclipse.sensinact.gateway.southbound.device.factory.IDeviceMappingRecord;
import org.eclipse.sensinact.gateway.southbound.device.factory.IPlaceHolderKeys;
import org.eclipse.sensinact.gateway.southbound.device.factory.IResourceMapping;
import org.eclipse.sensinact.gateway.southbound.device.factory.InvalidResourcePathException;
import org.eclipse.sensinact.gateway.southbound.device.factory.MissingParserException;
import org.eclipse.sensinact.gateway.southbound.device.factory.NamingUtils;
import org.eclipse.sensinact.gateway.southbound.device.factory.ParserException;
import org.eclipse.sensinact.gateway.southbound.device.factory.RecordPath;
import org.eclipse.sensinact.gateway.southbound.device.factory.ResourceLiteralMapping;
import org.eclipse.sensinact.gateway.southbound.device.factory.ResourceMappingHandler;
import org.eclipse.sensinact.gateway.southbound.device.factory.ResourceRecordMapping;
import org.eclipse.sensinact.gateway.southbound.device.factory.VariableNotFoundException;
import org.eclipse.sensinact.gateway.southbound.device.factory.VariableSolver;
import org.eclipse.sensinact.gateway.southbound.device.factory.dto.DeviceMappingConfigurationDTO;
import org.eclipse.sensinact.gateway.southbound.device.factory.dto.DeviceMappingOptionsDTO;
import org.eclipse.sensinact.prototype.PrototypePush;
import org.eclipse.sensinact.prototype.generic.dto.BulkGenericDto;
import org.eclipse.sensinact.prototype.generic.dto.GenericDto;
import org.osgi.service.component.ComponentServiceObjects;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.Promises;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
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
    PrototypePush prototypePush;

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
    public void handle(final DeviceMappingConfigurationDTO configuration, final byte[] payload)
            throws DeviceFactoryException {

        // Check parser ID
        final String parserId = configuration.parser;
        if (parserId == null || parserId.isBlank()) {
            throw new MissingParserException(String.format("No parser ID given", parserId));
        }

        // Extract mapping information
        final RecordState globalState = computeInitialState(configuration);

        // Check if a provider is set
        if (globalState.placeholders.get(KEY_PROVIDER) == null) {
            throw new IllegalArgumentException("No provider mapping given");
        }

        // Find it
        final ComponentServiceObjects<IDeviceMappingParser> cso = findParser(parserId);
        final IDeviceMappingParser parser = cso.getService();
        try {
            // Use it
            for (final IDeviceMappingRecord record : parser.parseRecords(payload, configuration.parserOptions)) {
                try {
                    handleRecord(configuration, globalState, record)
                            .onFailure((t) -> logger.error("Error updating resource: {}", t.getMessage(), t));
                } catch (JsonProcessingException | InvalidResourcePathException | ParserException
                        | VariableNotFoundException e) {
                    logger.error("Error parsing record: {}", e.getMessage(), e);
                }
            }
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
    private Promise<?> handleRecord(final DeviceMappingConfigurationDTO configuration, final RecordState globalState,
            final IDeviceMappingRecord record) throws InvalidResourcePathException, ParserException,
            VariableNotFoundException, JsonMappingException, JsonProcessingException {

        final DeviceMappingOptionsDTO options = configuration.mappingOptions;
        final RecordState recordState = computeRecordState(configuration, globalState, record);

        // Extract the provider
        final String rawProvider = getFieldString(record, recordState.placeholders.get(KEY_PROVIDER), options);
        if (rawProvider == null || rawProvider.isBlank()) {
            return Promises.failed(new IllegalArgumentException("Empty provider field."));
        }
        final String provider = NamingUtils.sanitizeName(rawProvider, false);

        // Extract the model
        final String model;
        if (recordState.placeholders.containsKey(KEY_MODEL)) {
            final String rawModel = getFieldString(record, recordState.placeholders.get(KEY_MODEL), options);
            if (rawModel == null || rawModel.isBlank()) {
                return Promises.failed(new IllegalArgumentException("Empty model field."));
            } else {
                model = NamingUtils.sanitizeName(rawModel, false);
            }
        } else {
            model = provider;
        }

        // Bulk update preparation
        final BulkGenericDto bulk = new BulkGenericDto();
        bulk.dtos = new ArrayList<>();

        // Compute the timestamp
        final Instant timestamp = computeTimestamp(record, recordState.placeholders, configuration);

        // Get the friendly name
        final IResourceMapping nameKey = recordState.placeholders.get(KEY_NAME);
        if (nameKey != null) {
            final String name = getFieldString(record, nameKey, options);
            if (name != null) {
                bulk.dtos.add(makeDto(model, provider, "admin", "friendlyName", name, timestamp));
            }
        }

        // Compute location, if any
        final GeoJsonObject location;
        try {
            location = computeLocation(record, recordState.placeholders, configuration.mappingOptions);
            if (location != null) {
                bulk.dtos.add(makeDto(model, provider, "admin", "location", location, timestamp));
            }
        } catch (JsonProcessingException e) {
            throw new ParserException("Error parsing location", e);
        }

        // Loop on resources
        for (final ResourceRecordMapping rcMapping : recordState.rcMappings) {
            final String service = rcMapping.getService();
            final String rcName = rcMapping.getResource();
            try {
                final Object value = record.getField(rcMapping.getRecordPath(), options);
                if (rcMapping.isMetadata()) {
                    logger.warn("Metadata update not supported.");
                } else {
                    bulk.dtos.add(makeDto(model, provider, service, rcName, value, timestamp));
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
                if (rcLiteral.isMetadata()) {
                    logger.warn("Metadata update not supported.");
                } else {
                    bulk.dtos.add(makeDto(model, provider, service, rcName, value, timestamp));
                }
            } catch (Exception e) {
                logger.warn("Error reading mapping for {}/{}/{}: {}", provider, service, rcName, e.getMessage());
            }
        }

        // Push update
        return prototypePush.pushUpdate(bulk);
    }

    /**
     * Prepares a generic DTO from the given information
     */
    private GenericDto makeDto(final String model, final String provider, final String service, final String resource,
            final Object value, Instant timestamp) {
        final GenericDto dto = new GenericDto();
        dto.model = model;
        dto.provider = provider;
        dto.service = service;
        dto.resource = resource;
        dto.value = value;
        if (value != null) {
            dto.type = value.getClass();
        }
        if (timestamp != null) {
            dto.timestamp = timestamp;
        }
        return dto;
    }

    /**
     * Computes the list of place holders, variables and resource mapping
     *
     * @param configuration Device mapping configuration
     * @return The global state
     * @throws InvalidResourcePathException Error parsing resource path
     */
    private RecordState computeInitialState(DeviceMappingConfigurationDTO configuration)
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
            } else if (key.startsWith("$")) {
                // Variable
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
        state.variables = resolveVariables(configuration, record, initialState.rawVariables);

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
            final IDeviceMappingRecord record, final Map<String, IResourceMapping> rawVariables)
            throws ParserException {

        final DeviceMappingOptionsDTO options = configuration.mappingOptions;

        Set<String> previouslyRemaining = new HashSet<>();
        final Set<String> remainingVars = new HashSet<>(rawVariables.keySet());
        final Map<String, String> resolvedVars = new HashMap<>();

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
                            return makeLocation(parts[0], parts[1], parts.length >= 3 ? parts[2] : null);
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

        return makeLocation(latitude, longitude, altitude);
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
     * Prepares a GeoJSON object for the point at the given location
     */
    private Point makeLocation(final Object latitude, final Object longitude, final Object altitude) {
        Float lat = toFloat(latitude);
        Float lon = toFloat(longitude);
        Float alt = toFloat(altitude);

        if (lat == null || lon == null) {
            // Invalid location
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
     * @param record        Record to read
     * @param placeholders  Defined mapping placeholders
     * @param configuration Mapping configuration
     * @return The parsed timestamp or the current time
     */
    private Instant computeTimestamp(final IDeviceMappingRecord record,
            final Map<String, IResourceMapping> placeholders, final DeviceMappingConfigurationDTO configuration) {

        final DeviceMappingOptionsDTO options = configuration.mappingOptions;

        final IResourceMapping timestampPath = placeholders.get(KEY_TIMESTAMP);
        if (timestampPath != null) {
            final Long timestamp = toLong(getFieldValue(record, timestampPath, options));
            if (timestamp != null) {
                final Instant parsed = convertTimestamp(timestamp);
                if (parsed != null) {
                    return parsed;
                }
            }
        }

        final ZoneId timezone = getTimezone(configuration.mappingOptions.dateTimezone);

        final IResourceMapping dateTimePath = placeholders.get(KEY_DATETIME);
        if (dateTimePath != null) {
            final String strDateTime = getFieldString(record, dateTimePath, options);
            if (strDateTime != null && !strDateTime.isBlank()) {
                return parseDateTime(strDateTime, timezone, configuration.mappingOptions.formatDateTime);
            }
        }

        LocalDate date = null;
        final IResourceMapping datePath = placeholders.get(KEY_DATE);
        if (datePath != null) {
            final String strDate = getFieldString(record, datePath, options);
            if (strDate != null && !strDate.isBlank()) {
                date = parseDate(strDate, configuration.mappingOptions.formatDate);
            }
        }

        OffsetTime time = null;
        final IResourceMapping timePath = placeholders.get(KEY_TIME);
        if (timePath != null) {
            final String strTime = getFieldString(record, timePath, options);
            if (strTime != null && !strTime.isBlank()) {
                time = parseTime(strTime, date, timezone, configuration.mappingOptions.formatTime);
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

        ZoneOffset offset;
        try {
            offset = ZoneOffset.ofTotalSeconds(parsedTime.get(ChronoField.OFFSET_SECONDS));
        } catch (UnsupportedTemporalTypeException e) {
            if (expectedDate != null) {
                offset = timezone.getRules().getOffset(expectedDate.atTime(hour, minute, second));
            } else {
                offset = timezone.getRules().getOffset(Instant.now());
            }
        }

        return OffsetTime.of(hour, minute, second, 0, offset);
    }

    /**
     * Parses a date string
     *
     * @param strDate    Date string
     * @param formatDate Custom parsing format (can be null)
     * @return The parsed date
     */
    private LocalDate parseDate(String strDate, String formatDate) {
        DateTimeFormatter format = DateTimeFormatter.ISO_LOCAL_DATE;
        if (formatDate != null && !formatDate.isBlank()) {
            format = DateTimeFormatter.ofPattern(formatDate);
        }

        return extractDate(format.parse(strDate));
    }

    /**
     * Parses a time string
     *
     * @param strTime    Time string
     * @param timezone   Fallback timezone
     * @param formatTime Custom parsing format (can be null)
     * @return The parsed date
     */
    private OffsetTime parseTime(String strTime, LocalDate expectedDate, ZoneId timezone, String formatTime) {
        DateTimeFormatter format = DateTimeFormatter.ISO_OFFSET_TIME;
        if (formatTime != null && !formatTime.isBlank()) {
            format = DateTimeFormatter.ofPattern(formatTime);
        }

        return extractTime(format.parse(strTime), expectedDate, timezone);
    }

    /**
     * Parses a date/time string
     *
     * @param strDateTime    Date/time string
     * @param timezone       Fallback timezone
     * @param formatDateTime Custom parsing format (can be null)
     * @return The parsed date as an instant
     */
    private Instant parseDateTime(String strDateTime, ZoneId timezone, String formatDateTime) {
        DateTimeFormatter format = DateTimeFormatter.ISO_DATE_TIME;
        if (formatDateTime != null && !formatDateTime.isBlank()) {
            format = DateTimeFormatter.ofPattern(formatDateTime);
        }

        final TemporalAccessor parsedTime = format.parse(strDateTime);
        final LocalDate date = extractDate(parsedTime);
        final OffsetTime offsetTime = extractTime(parsedTime, date, timezone);
        final OffsetDateTime dateTime = OffsetDateTime.of(date, offsetTime.toLocalTime(), offsetTime.getOffset());
        return dateTime.toInstant();
    }

    /**
     * Converts a timestamp to an Instant
     *
     * @param timestamp Parsed timestamp
     * @return The instant at the timestamp
     */
    private Instant convertTimestamp(Long timestamp) {

        int currentLogMs = (int) Math.log10(System.currentTimeMillis());
        int currentLogNs = (int) Math.log10(System.nanoTime());
        int timestampLog = (int) Math.log10(timestamp);

        if (timestampLog == currentLogMs) {
            return Instant.ofEpochMilli(timestamp);
        } else if (timestampLog == currentLogMs - 3) {
            return Instant.ofEpochSecond(timestamp);
        } else if (timestampLog == currentLogNs) {
            return Instant.EPOCH.plusNanos(timestamp);
        } else {
            logger.warn("Can't determine timestamp unit %d (%d digits)", timestamp, timestampLog);
        }

        return Instant.now();
    }
}
