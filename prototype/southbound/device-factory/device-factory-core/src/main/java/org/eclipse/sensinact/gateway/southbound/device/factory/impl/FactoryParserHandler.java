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
package org.eclipse.sensinact.gateway.southbound.device.factory.impl;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;

import org.eclipse.sensinact.gateway.geojson.Coordinates;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.gateway.southbound.device.factory.DeviceFactoryException;
import org.eclipse.sensinact.gateway.southbound.device.factory.IDeviceMappingHandler;
import org.eclipse.sensinact.gateway.southbound.device.factory.IDeviceMappingParser;
import org.eclipse.sensinact.gateway.southbound.device.factory.IDeviceMappingRecord;
import org.eclipse.sensinact.gateway.southbound.device.factory.InvalidResourcePathException;
import org.eclipse.sensinact.gateway.southbound.device.factory.MissingParserException;
import org.eclipse.sensinact.gateway.southbound.device.factory.ParserException;
import org.eclipse.sensinact.gateway.southbound.device.factory.RecordPath;
import org.eclipse.sensinact.gateway.southbound.device.factory.ResourceMapping;
import org.eclipse.sensinact.gateway.southbound.device.factory.VariableNotFoundException;
import org.eclipse.sensinact.gateway.southbound.device.factory.VariableSolver;
import org.eclipse.sensinact.gateway.southbound.device.factory.dto.DeviceMappingConfigurationDTO;
import org.eclipse.sensinact.prototype.PrototypePush;
import org.eclipse.sensinact.prototype.generic.dto.BulkGenericDto;
import org.eclipse.sensinact.prototype.generic.dto.GenericDto;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.promise.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Handles the common code of device factory
 */
@Component(immediate = true, service = IDeviceMappingHandler.class)
public class FactoryParserHandler implements IDeviceMappingHandler {

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
        Map<String, RecordPath> placeholders;

        /**
         * Variable name -&gt; unresolved record path
         */
        Map<String, RecordPath> rawVariables;

        /**
         * Variable name -&gt; resolved record path
         */
        Map<String, String> variables;

        /**
         * SensiNact resource path -&gt; record path
         */
        List<ResourceMapping> rcMappings;
    }

    /**
     * SensiNact update endpoint
     */
    @Reference
    private PrototypePush prototypePush;

    /**
     * Bundle context
     */
    private BundleContext context;

    /**
     * JSON mapper
     */
    private ObjectMapper mapper = new ObjectMapper();

    /**
     * Component activated
     */
    @Activate
    void activate(ComponentContext context) {
        this.context = context.getBundleContext();
    }

    /**
     * Component deactivated
     */
    @Deactivate
    void deactivate() {
        this.context = null;
    }

    /**
     * Looks for the service reference of the parser with the given ID
     */
    private ServiceReference<IDeviceMappingParser> findParser(final String parserId) throws MissingParserException {
        final Collection<ServiceReference<IDeviceMappingParser>> svcRefs;
        try {
            svcRefs = context.getServiceReferences(IDeviceMappingParser.class,
                    String.format("(%s=%s)", IDeviceMappingParser.PARSER_ID, parserId));
        } catch (InvalidSyntaxException e) {
            throw new MissingParserException(String.format("Invalid parser ID '%s': %s'", parserId, e.getMessage()));
        }

        if (svcRefs == null || svcRefs.isEmpty()) {
            throw new MissingParserException(String.format("Parser ID '%s' is missing", parserId));
        }

        return svcRefs.stream().sorted(Comparator.naturalOrder()).findFirst().get();
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
        if (globalState.placeholders.get("@provider") == null) {
            throw new IllegalArgumentException("No provider mapping given");
        }

        // Find it
        final ServiceReference<IDeviceMappingParser> svcRef = findParser(parserId);
        final IDeviceMappingParser parser = context.getService(svcRef);
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
            context.ungetService(svcRef);
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

        final RecordState recordState = computeRecordState(configuration, globalState, record);

        // Extract the provider
        final String provider = record.getFieldString(recordState.placeholders.get("@provider"));
        if (provider == null || provider.isBlank()) {
            logger.error("Empty provider field.");
            return null;
        }

        // Bulk update preparation
        final BulkGenericDto bulk = new BulkGenericDto();
        bulk.dtos = new ArrayList<>();

        // Compute the timestamp
        final Instant timestamp = computeTimestamp(record, recordState.placeholders, configuration);

        // Get the friendly name
        final RecordPath nameKey = recordState.placeholders.get("@name");
        if (nameKey != null) {
            final String name = record.getFieldString(nameKey);
            if (name != null) {
                bulk.dtos.add(makeDto(provider, "admin", "friendlyName", name, timestamp));
            }
        }

        // Compute location, if any
        final GeoJsonObject location;
        try {
            location = computeLocation(record, recordState.placeholders);
            if (location != null) {
                bulk.dtos.add(makeDto(provider, "admin", "location", mapper.writeValueAsString(location), timestamp));
            }
        } catch (JsonProcessingException e) {
            throw new ParserException("Error parsing location", e);
        }

        // Loop on resources
        for (final ResourceMapping rcMapping : recordState.rcMappings) {
            final String service = rcMapping.getService();
            final String rcName = rcMapping.getResource();
            final Object value = record.getField(rcMapping.getRecordPath());
            if (rcMapping.isMetadata()) {
                logger.warn("Metadata update not supported.");
            } else {
                bulk.dtos.add(makeDto(provider, service, rcName, value, timestamp));
            }
        }

        // Push update
        return prototypePush.pushUpdate(bulk);
    }

    /**
     * Prepares a generic DTO from the given information
     */
    private GenericDto makeDto(final String provider, final String service, final String resource, final Object value,
            Instant timestamp) {
        final GenericDto dto = new GenericDto();
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
        final Map<String, RecordPath> placeholders = new HashMap<>();
        final Map<String, RecordPath> variablesMappings = new HashMap<>();
        final List<ResourceMapping> rcMappings = new ArrayList<>();

        for (Entry<String, Object> entry : configuration.mapping.entrySet()) {
            final String key = entry.getKey();
            final RecordPath path = new RecordPath(entry.getValue());
            if (key.startsWith("@")) {
                // Placeholder
                placeholders.put(key, new RecordPath(path));
            } else if (key.startsWith("$")) {
                // Variable
                variablesMappings.put(key, new RecordPath(path));
            } else {
                // Mapping
                rcMappings.add(new ResourceMapping(key, path));
            }
        }

        final RecordState state = new RecordState();
        state.placeholders = Map.copyOf(placeholders);
        state.rawVariables = Map.copyOf(variablesMappings);
        state.rcMappings = List.copyOf(rcMappings);
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
        for (final ResourceMapping rcMapping : initialState.rcMappings) {
            state.rcMappings.add(rcMapping.fillInVariables(state.variables));
        }

        return state;
    }

    /**
     * Fills in variables in the given map
     *
     * @param placeholders Map with keys and values that can contain variables
     * @param variables    Resolved variables
     * @return A new map with resolved placeholders
     * @throws VariableNotFoundException Error resolving variables
     */
    private Map<String, RecordPath> fillInVariables(final Map<String, RecordPath> placeholders,
            final Map<String, String> variables) throws VariableNotFoundException {

        final Map<String, RecordPath> newPlaceholders = new HashMap<>(placeholders.size());
        for (Entry<String, RecordPath> entry : placeholders.entrySet()) {
            // Update key
            final String newKey = VariableSolver.fillInVariables(entry.getKey(), variables);
            // Update path
            RecordPath newValue = entry.getValue().fillInVariables(variables);
            newPlaceholders.put(newKey, newValue);
        }

        return newPlaceholders;
    }

    /**
     * Assigns a value to each variable or throws an exception
     *
     * @param configuration     Mapping configuration
     * @param record            Current record
     * @param variablesMappings Definitions of variables
     * @return
     * @throws ParserException Error resolving variables
     */
    private Map<String, String> resolveVariables(final DeviceMappingConfigurationDTO configuration,
            final IDeviceMappingRecord record, final Map<String, RecordPath> variablesMappings) throws ParserException {

        Set<String> previouslyRemaining = new HashSet<>();
        final Set<String> remainingVars = new HashSet<>(variablesMappings.keySet());
        final Map<String, String> resolvedVars = new HashMap<>();

        while (!remainingVars.isEmpty()) {
            if (previouslyRemaining.equals(remainingVars)) {
                throw new ParserException("Can't resolve variables: " + remainingVars);
            }

            previouslyRemaining = Set.copyOf(remainingVars);
            final Set<String> resolved = new HashSet<>();
            for (final String varName : remainingVars) {
                final String path = String.valueOf(variablesMappings.get(varName));
                final Matcher matcher = VariableSolver.varPattern.matcher(path);
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
                    resolvedVars.put(varName, (String) record.getField(new RecordPath(path)));
                    resolved.add(varName);
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
     * @return The parsed location as a GeoJSON string or null
     * @throws JsonProcessingException Error parsing GeoJSON
     */
    private GeoJsonObject computeLocation(final IDeviceMappingRecord record, final Map<String, RecordPath> placeholders)
            throws JsonProcessingException {
        final RecordPath locationPath = placeholders.get("@location");
        if (locationPath != null) {
            final Object locationValue = record.getField(locationPath);
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
        final RecordPath latitudePath = placeholders.get("@latitude");
        final RecordPath longitudePath = placeholders.get("@longitude");
        if (latitudePath == null || longitudePath == null) {
            // No lat/lon data
            return null;
        }

        final Object latitude = record.getField(latitudePath);
        final Object longitude = record.getField(longitudePath);
        Object altitude = null;

        final RecordPath altitudePath = placeholders.get("@altitude");
        if (altitudePath != null) {
            altitude = record.getField(altitudePath);
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
     * Looks for a time value in the given record
     *
     * @param record        Record to read
     * @param placeholders  Defined mapping placeholders
     * @param configuration Mapping configuration
     * @return The parsed timestamp or the current time
     */
    private Instant computeTimestamp(IDeviceMappingRecord record, Map<String, RecordPath> placeholders,
            DeviceMappingConfigurationDTO configuration) {

        final RecordPath timestampPath = placeholders.get("@timestamp");
        if (timestampPath != null) {
            final Long timestamp = toLong(record.getField(timestampPath));
            if (timestamp != null) {
                final Instant parsed = convertTimestamp(timestamp);
                if (parsed != null) {
                    return parsed;
                }
            }
        }

        final RecordPath dateTimePath = placeholders.get("@datetime");
        if (dateTimePath != null) {
            final String strDateTime = record.getFieldString(dateTimePath);
            if (strDateTime != null && !strDateTime.isBlank()) {
                return parseDateTime(strDateTime, configuration.mappingOptions.formatDateTime);
            }
        }

        LocalDate date = null;
        final RecordPath datePath = placeholders.get("@date");
        if (datePath != null) {
            final String strDate = record.getFieldString(datePath);
            if (strDate != null && !strDate.isBlank()) {
                date = parseDate(strDate, configuration.mappingOptions.formatDate);
            }
        }

        OffsetTime time = null;
        final RecordPath timePath = placeholders.get("@time");
        if (timePath != null) {
            final String strTime = record.getFieldString(timePath);
            if (strTime != null && !strTime.isBlank()) {
                time = parseTime(strTime, configuration.mappingOptions.formatTime);
            }
        }

        if (date == null) {
            if (time == null) {
                return Instant.now();
            } else {
                return LocalDate.now().atTime(time).toInstant();
            }
        } else if (time == null) {
            return date.atStartOfDay(ZoneOffset.UTC).toInstant();
        } else {
            return date.atTime(time).toInstant();
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
     */
    private OffsetTime extractTime(final TemporalAccessor parsedTime) {
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
        } catch (Exception e) {
            offset = ZoneOffset.UTC;
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
     * @param strTime    Date string
     * @param formatDate Custom parsing format (can be null)
     * @return The parsed date
     */
    private OffsetTime parseTime(String strTime, String formatDate) {
        DateTimeFormatter format = DateTimeFormatter.ISO_OFFSET_TIME;
        if (formatDate != null && !formatDate.isBlank()) {
            format = DateTimeFormatter.ofPattern(formatDate);
        }

        return extractTime(format.parse(strTime));
    }

    /**
     * Parses a date/time string
     *
     * @param strDateTime    Date/time string
     * @param formatDateTime Custom parsing format (can be null)
     * @return The parsed date as an instant
     */
    private Instant parseDateTime(String strDateTime, String formatDateTime) {
        DateTimeFormatter format = DateTimeFormatter.ISO_DATE_TIME;
        if (formatDateTime != null && !formatDateTime.isBlank()) {
            format = DateTimeFormatter.ofPattern(formatDateTime);
        }

        final TemporalAccessor parsedTime = format.parse(strDateTime);
        final LocalDate date = extractDate(parsedTime);
        final OffsetTime offsetTime = extractTime(parsedTime);
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