/*********************************************************************
* Copyright (c) 2024 Kentyou.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Thomas Calmant (Kentyou) - Initial contribution
**********************************************************************/

package org.eclipse.sensinact.gateway.southbound.wot.core;

import static org.eclipse.sensinact.gateway.southbound.wot.api.constants.Utils.classFromType;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.AbstractTwinCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.model.Model;
import org.eclipse.sensinact.core.model.Resource;
import org.eclipse.sensinact.core.model.ResourceType;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.model.Service;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.core.twin.SensinactService;
import org.eclipse.sensinact.gateway.geojson.Coordinates;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.gateway.southbound.wot.api.ActionAffordance;
import org.eclipse.sensinact.gateway.southbound.wot.api.EventAffordance;
import org.eclipse.sensinact.gateway.southbound.wot.api.InteractionAffordance;
import org.eclipse.sensinact.gateway.southbound.wot.api.Namespaces;
import org.eclipse.sensinact.gateway.southbound.wot.api.PropertyAffordance;
import org.eclipse.sensinact.gateway.southbound.wot.api.Thing;
import org.eclipse.sensinact.gateway.southbound.wot.api.constants.Utils;
import org.eclipse.sensinact.gateway.southbound.wot.api.constants.WoTConstants;
import org.eclipse.sensinact.gateway.southbound.wot.api.dataschema.DataSchema;
import org.eclipse.sensinact.gateway.southbound.wot.api.dataschema.ObjectSchema;
import org.eclipse.sensinact.gateway.southbound.wot.api.handlers.SensinactThingDescriptor;
import org.eclipse.sensinact.gateway.southbound.wot.api.handlers.ThingListener;
import org.eclipse.sensinact.gateway.southbound.wot.api.handlers.ThingManager;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the appearance / disappearance of web of thing objects
 */
@Component(immediate = true, configurationPid = "sensinact.southbound.wot", configurationPolicy = ConfigurationPolicy.OPTIONAL)
public class WebOfThingManager implements ThingManager {

    private static final Logger logger = LoggerFactory.getLogger(WebOfThingManager.class);

    /**
     * SensiNact gateway thread
     */
    @Reference
    GatewayThread gatewayThread;

    /**
     * Thing handlers
     */
    private final List<ThingListener> listeners = new ArrayList<>();

    /**
     * List of providers created by this manager
     */
    private final Map<String, SensinactThingDescriptor> managedThings = new HashMap<>();

    /**
     * If true, try to the Thing @type field as model name
     */
    boolean useTypeAsModelName;

    /**
     * Prefix to add to model names
     */
    String modelPrefix;

    /**
     * If true, prefer "id" instead of "title" as provider name
     */
    boolean useIdAsProviderName;

    /**
     * Prefix to add to all loaded providers
     */
    String providerPrefix;

    /**
     * Component activated
     */
    @Activate
    void activate(final Configuration config) {
        useTypeAsModelName = config.model_useType();
        modelPrefix = config.model_prefix();
        useIdAsProviderName = config.naming_useId();
        providerPrefix = config.naming_prefix();
    }

    @Override
    public String registerThing(final Thing thing) {
        // Look for the name
        final String[] testedFields;
        if (useIdAsProviderName) {
            // Prefer the thing ID as provider name
            testedFields = new String[] { thing.id, thing.title };
        } else {
            // Prefer thing title
            testedFields = new String[] { thing.title, thing.id };
        }

        String rawBaseName = null;
        for (final String testedField : testedFields) {
            if (testedField != null && !testedField.isBlank()) {
                rawBaseName = testedField.trim();
                break;
            }
        }

        if (rawBaseName == null) {
            logger.warn("No title or ID found in thing description. Abandon.");
            throw new IllegalArgumentException("Given Thing doesn't have an ID nor a title");
        }

        // Clean up the name
        final String thingName = rawBaseName.replaceAll("[^-_A-Za-z0-9 ]", "_");

        final String providerId;
        if (providerPrefix != null) {
            providerId = providerPrefix + thingName;
        } else {
            providerId = thingName;
        }

        final String defaultModelName;
        if (modelPrefix != null) {
            defaultModelName = modelPrefix + thingName;
        } else {
            defaultModelName = Utils.makeWoTSanitizedName(thingName);
        }

        final SensinactThingDescriptor descriptor = new SensinactThingDescriptor();
        descriptor.modelPackageUri = WoTConstants.MODEL_PACKAGE_URI;
        descriptor.modelName = defaultModelName;
        descriptor.providerId = providerId;
        descriptor.thing = thing;

        if (useTypeAsModelName && thing.semanticType != null && thing.semanticType.size() == 1) {
            final String semanticType = expandFieldName(thing.semanticType.get(0), thing.context);
            logger.debug("Semantic type: {} - context: {} -> {}", thing.semanticType.get(0), thing.context,
                    semanticType);
            if (semanticType == null || semanticType.isBlank()) {
                logger.warn("Can't use Thing semantic type as model name: {}", thing.semanticType);
            } else {
                descriptor.modelName = Utils.makeWoTSanitizedName(semanticType);
                logger.debug("Using @type entry as model name for {}: {}", descriptor.providerId, descriptor.modelName);
            }
        }

        // Create the provider
        createThingModelAndProvider(descriptor);

        // At this point, we can consider we manage this description
        managedThings.put(descriptor.providerId, descriptor);
        return descriptor.providerId;
    }

    @Override
    public boolean unregisterThing(final String providerName) throws InterruptedException, InvocationTargetException {
        if (managedThings.remove(providerName) != null) {
            try {
                // Known provider
                return gatewayThread.execute(new AbstractTwinCommand<Boolean>() {
                    @Override
                    protected Promise<Boolean> call(final SensinactDigitalTwin twin, final PromiseFactory pf) {
                        try {
                            SensinactProvider provider = twin.getProvider(providerName);
                            if (provider != null) {
                                provider.delete();
                                return pf.resolved(true);
                            }
                            return pf.resolved(false);
                        } catch (final Exception e) {
                            logger.error("Error deleting provider {}", providerName, e);
                            return pf.failed(e);
                        }
                    }
                }).getValue();
            } catch (final InterruptedException | InvocationTargetException e) {
                logger.error("Error unregistering thing {}", providerName, e);
                throw e;
            }
        } else {
            logger.debug("Trying to remove unmanaged thing {}", providerName);
            return false;
        }
    }

    /**
     * Creates a provider and its model based on the given description. Fails if the
     * provider already exists with a different model.
     *
     * @param descriptor Description of the thing
     * @throws IllegalStateException A provider with the same name but a different
     *                               model already exists
     */
    void createThingModelAndProvider(final SensinactThingDescriptor descriptor) throws IllegalStateException {

        final Thing thing = descriptor.thing;
        final String providerName = (thing.title != null && !thing.title.isBlank()) ? thing.title
                : descriptor.providerId;
        final GeoJsonObject location = lookupLocation(thing);

        // Register the model
        try {
            gatewayThread.execute(new AbstractSensinactCommand<Void>() {
                @Override
                protected Promise<Void> call(final SensinactDigitalTwin twin, final SensinactModelManager modelMgr,
                        final PromiseFactory pf) {
                    try {
                        // Check if the provider already exists
                        SensinactProvider provider = twin.getProvider(providerName);
                        if (provider == null) {
                            // If not, set it all up
                            logger.debug("Setting up model {} for thing {}", descriptor.modelName, thing.id);

                            Model model = modelMgr.getModel(descriptor.modelPackageUri, descriptor.modelName);
                            if (model == null) {
                                model = modelMgr.createModel(descriptor.modelPackageUri, descriptor.modelName).build();
                            }

                            Service wotService = model.getServices().get(WoTConstants.WOT_SERVICE);
                            if (wotService == null) {
                                wotService = model.createService(WoTConstants.WOT_SERVICE).build();
                            }

                            Service wotEventService = model.getServices().get(WoTConstants.EVENT_SERVICE);
                            if (wotEventService == null) {
                                wotEventService = model.createService(WoTConstants.EVENT_SERVICE).build();
                            }

                            try {
                                if (thing.actions != null) {
                                    for (final Entry<String, ActionAffordance> entry : thing.actions.entrySet()) {
                                        buildAction(entry.getKey(), entry.getValue(), wotService);
                                    }
                                }

                                if (thing.properties != null) {
                                    for (final Entry<String, PropertyAffordance> entry : thing.properties.entrySet()) {
                                        buildProperty(entry.getKey(), entry.getValue(), wotService);
                                    }
                                }

                                if (thing.events != null) {
                                    for (final Entry<String, EventAffordance> entry : thing.events.entrySet()) {
                                        buildEvent(entry.getKey(), entry.getValue(), wotEventService);
                                    }
                                }
                            } catch (final Exception e) {
                                return pf.failed(e);
                            }

                            // Create the provider
                            provider = twin.createProvider(model.getPackageUri(), model.getName(),
                                    descriptor.providerId);
                        }

                        // Have a fixed timestamp for all resources
                        final Instant timestamp = Instant.now();

                        // Set friendly name
                        final Map<String, ? extends SensinactResource> adminRc = provider.getServices().get("admin")
                                .getResources();
                        adminRc.get("friendlyName").setValue(providerName, timestamp);

                        if (location != null) {
                            // Set geo-location
                            adminRc.get("location").setValue(location, timestamp);
                        }

                        final SensinactService snaWotService = provider.getServices().get(WoTConstants.WOT_SERVICE);
                        if (snaWotService == null) {
                            logger.error("Provider {} doesn't have a WoT service.", providerName);
                            return pf.failed(new IllegalStateException("WoT service not created"));
                        }

                        final SensinactService snaWotEventService = provider.getServices()
                                .get(WoTConstants.EVENT_SERVICE);
                        if (snaWotEventService == null) {
                            logger.error("Provider {} doesn't have a WoT event service.", providerName);
                            return pf.failed(new IllegalStateException("WoT event service not created"));
                        }

                        // Add metadata to resources
                        for (final Entry<String, ActionAffordance> entry : thing.actions.entrySet()) {
                            try {
                                final SensinactResource rc = snaWotService.getResources().get(entry.getKey());
                                if (rc != null) {
                                    addActionMetadata(pf, rc, entry.getValue(), timestamp);
                                }
                            } catch (final Exception e) {
                                logger.error("Error adding metadata to {}/{}/{}", providerName,
                                        WoTConstants.WOT_SERVICE, entry.getKey(), e);
                            }
                        }

                        for (final Entry<String, PropertyAffordance> entry : thing.properties.entrySet()) {
                            try {
                                final SensinactResource rc = snaWotService.getResources().get(entry.getKey());
                                if (rc != null) {
                                    addPropertyMetadata(pf, rc, entry.getValue(), timestamp);
                                }
                            } catch (final Exception e) {
                                logger.error("Error adding metadata to {}/{}/{}", providerName,
                                        WoTConstants.WOT_SERVICE, entry.getKey(), e);
                            }
                        }

                        for (final Entry<String, EventAffordance> entry : thing.events.entrySet()) {
                            try {
                                final SensinactResource rc = snaWotEventService.getResources().get(entry.getKey());
                                if (rc != null) {
                                    addEventMetadata(pf, rc, entry.getValue(), timestamp);
                                }
                            } catch (final Exception e) {
                                logger.error("Error adding metadata to {}/{}/{}", providerName,
                                        WoTConstants.EVENT_SERVICE, entry.getKey(), e);
                            }
                        }

                        logger.info("Created model and provider {} for thing {}", providerName, thing.id);
                        return pf.resolved(null);
                    } catch (Exception e) {
                        logger.error("Something went wrong creating provider {}", providerName, e);
                        return pf.failed(e);
                    }
                }
            }).getValue();
        } catch (final InterruptedException e) {
            logger.error("Interrupted while creating model for thing {}", thing.id, e);
            return;
        } catch (final InvocationTargetException e) {
            logger.error("Exception while creating model for thing {}", thing.id, e);
            return;
        }
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    void addListener(final ThingListener listener) {
        final List<SensinactThingDescriptor> descriptors = List.copyOf(managedThings.values());
        for (final SensinactThingDescriptor descriptor : descriptors) {
            try {
                logger.debug("Notifying {} of {}", listener, descriptor.thing.id);
                listener.thingRegistered(descriptor);
            } catch (final Exception e) {
                logger.error("Error notifying listener", e);
            }
        }
        listeners.add(listener);
    }

    void removeListener(final ThingListener listener) {
        listeners.remove(listener);
    }

    /**
     * Adds metadata specific to interaction affordance
     *
     * @param interaction Interaction to read
     * @param metadata    Metadata to fill in
     */
    protected void addInteractionMetadata(final InteractionAffordance interaction, final Map<String, Object> metadata) {
        metadata.put("description", interaction.description);
    }

    /**
     * Sets metadata of a resource
     *
     * @param pf        Promise factory
     * @param rc        Resource to apply metadata to
     * @param metadata  Metadata to apply
     * @param timestamp Common timestamp
     * @throws InvocationTargetException Error applying metadata
     * @throws InterruptedException      Interrupted while applying metadata
     */
    protected void applyResourceMetadata(final PromiseFactory pf, final SensinactResource rc,
            final Map<String, Object> metadata, final Instant timestamp)
            throws InvocationTargetException, InterruptedException {
        final List<Promise<Void>> entries = metadata.entrySet().stream().filter(e -> e.getValue() != null)
                .map(e -> rc.setMetadataValue(e.getKey(), e.getValue(), timestamp)).collect(Collectors.toList());
        if (!entries.isEmpty()) {
            pf.all(entries).getValue();
        }
    }

    /**
     * Sets metadata of an action
     *
     * @param pf        Promise factory
     * @param rc        Resource to apply metadata to
     * @param action    Action description
     * @param timestamp Common timestamp
     * @throws InvocationTargetException Error applying metadata
     * @throws InterruptedException      Interrupted while applying metadata
     */
    protected void addActionMetadata(final PromiseFactory pf, final SensinactResource rc, final ActionAffordance action,
            final Instant timestamp) throws InvocationTargetException, InterruptedException {
        final Map<String, Object> metadata = new HashMap<>();
        addInteractionMetadata(action, metadata);
        applyResourceMetadata(pf, rc, metadata, timestamp);
    }

    /**
     * Sets metadata of a property
     *
     * @param pf        Promise factory
     * @param rc        Resource to apply metadata to
     * @param property  Property description
     * @param timestamp Common timestamp
     * @throws InvocationTargetException Error applying metadata
     * @throws InterruptedException      Interrupted while applying metadata
     */
    protected void addPropertyMetadata(final PromiseFactory pf, final SensinactResource rc,
            final PropertyAffordance property, final Instant timestamp)
            throws InvocationTargetException, InterruptedException {
        final Map<String, Object> metadata = new HashMap<>();
        addInteractionMetadata(property, metadata);
        if (property.schema != null) {
            metadata.put("unit", property.schema.unit);
            metadata.put("format", property.schema.format);
        }
        applyResourceMetadata(pf, rc, metadata, timestamp);
    }

    /**
     * Sets metadata of an event
     *
     * @param pf        Promise factory
     * @param rc        Resource to apply metadata to
     * @param event     Event description
     * @param timestamp Common timestamp
     * @throws InvocationTargetException Error applying metadata
     * @throws InterruptedException      Interrupted while applying metadata
     */
    protected void addEventMetadata(final PromiseFactory pf, final SensinactResource rc, final EventAffordance event,
            final Instant timestamp) throws InvocationTargetException, InterruptedException {
        final Map<String, Object> metadata = new HashMap<>();
        addInteractionMetadata(event, metadata);
        applyResourceMetadata(pf, rc, metadata, timestamp);
    }

    List<ParameterDescription> makeParameters(final DataSchema schema) {
        if (schema == null || schema.type == null) {
            logger.warn("Trying to read parameters from an empty data schema");
            return List.of();
        }

        switch (schema.type) {
        case "string":
            return List.of(new ParameterDescription(WoTConstants.DEFAULT_ARG_NAME, String.class, schema));

        case "boolean":
            return List.of(new ParameterDescription(WoTConstants.DEFAULT_ARG_NAME, Boolean.class, schema));

        case "number":
            return List.of(new ParameterDescription(WoTConstants.DEFAULT_ARG_NAME, Double.class, schema));

        case "integer":
            return List.of(new ParameterDescription(WoTConstants.DEFAULT_ARG_NAME, Long.class, schema));

        case "object": {
            return ((ObjectSchema) schema).properties.entrySet().stream()
                    .map(e -> new ParameterDescription(e.getKey(), classFromType(e.getValue().type), e.getValue()))
                    .collect(Collectors.toList());
        }

        default:
            logger.warn("Unknown data schema type: {}", schema.type);
            return List.of();
        }
    }

    /**
     * Adds an action resource in the given service from an action affordance
     *
     * @param key     Action name
     * @param action  Action description
     * @param service SensiNact model service
     * @throws Exception Error building action
     */
    private void buildAction(final String key, final ActionAffordance action, final Service service) throws Exception {
        final List<ParameterDescription> paramsDescription = makeParameters(action.input);
        final List<Entry<String, Class<?>>> params = paramsDescription.stream().map(ParameterDescription::toEntry)
                .collect(Collectors.toList());
        final Resource resource = service.getResources().get(key);
        if (resource == null) {
            service.createResource(key).withResourceType(ResourceType.ACTION).withType(classFromType(action.output))
                    .withAction(params).withDefaultMetadata(paramsDescription.stream()
                            .collect(Collectors.toMap(p -> p.name, ParameterDescription::toMetadata)))
                    .build();
        } else if (resource.getResourceType() != ResourceType.ACTION) {
            logger.error("Trying to convert a non-action resource to an action ({})", key);
            throw new Exception("Trying to convert a value resource to an action");
        } else {
            // Check result type and parameters
            if (!resource.getType().equals(classFromType(action.output.type))) {
                logger.warn("Action {} result type mismatch: {} vs. {}", key, resource.getType().getSimpleName(),
                        classFromType(action.output.type).getSimpleName());
            }

            if (!params.equals(resource.getArguments())) {
                logger.warn("Action {} parameters changed", key);
            }
        }
    }

    /**
     * Adds a property resource in the given service from an action affordance
     *
     * @param key     Property name
     * @param action  Property description
     * @param service SensiNact model service
     * @throws Exception Error building resource
     */
    private void buildProperty(final String key, final PropertyAffordance property, final Service service)
            throws Exception {
        final Resource resource = service.getResources().get(key);
        if (resource == null) {
            service.createResource(key).withResourceType(ResourceType.SENSOR)
                    .withType(classFromType(property.schema.type)).withGetter().withSetter().build();
        } else if (resource.getResourceType() == ResourceType.ACTION) {
            logger.error("Trying to convert an action resource to a property ({})", key);
            throw new Exception("Trying to convert an action to a value resource");
        } else {
            // Check result type and parameters
            if (!resource.getType().equals(classFromType(property.schema.type))) {
                logger.warn("Resource {} result type mismatch: {} vs. {}", key, resource.getType().getSimpleName(),
                        classFromType(property.schema.type).getSimpleName());
            }
        }
    }

    /**
     * Adds a resource to mimic events in the given service from an event affordance
     *
     * @param key     Event name
     * @param action  Event description
     * @param service SensiNact model service
     * @throws Exception Error building resource
     */
    private void buildEvent(final String key, final EventAffordance event, final Service service) throws Exception {
        final Resource resource = service.getResources().get(key);
        if (resource == null) {
            service.createResource(key).withResourceType(ResourceType.SENSOR).withType(classFromType(event.data.type))
                    .build();
        } else if (resource.getResourceType() == ResourceType.ACTION) {
            logger.error("Trying to convert an action resource to an event ({})", key);
            throw new Exception("Trying to convert an action to a value resource");
        } else {
            // Check result type and parameters
            if (!resource.getType().equals(classFromType(event.data.type))) {
                logger.warn("Resource {} result type mismatch: {} vs. {}", key, resource.getType().getSimpleName(),
                        classFromType(event.data.type).getSimpleName());
            }
        }
    }

    /**
     * Shortens the given field name based on namespaces
     *
     * @param name Field name
     * @param ns   Thing context
     * @return Shortened field name, or the given name
     */
    String shortenFieldName(final String name, final Namespaces ns) {
        if (name == null) {
            return null;
        }

        if (name.isBlank()) {
            return name;
        }

        if (ns == null) {
            return null;
        }

        if (ns.defaultNs != null && name.startsWith(ns.defaultNs)) {
            return name.substring(ns.defaultNs.length());
        }

        return ns.prefixes.entrySet().stream().filter(e -> name.startsWith(e.getValue()))
                .map(e -> String.format("%s:%s", e.getKey(), name.substring(e.getValue().length()))).findFirst()
                .orElse(name);
    }

    /**
     * Expands the given field name based on namespaces
     *
     * @param name Field name
     * @param ns   Thing context
     * @return Expanded field name, or the given name
     */
    String expandFieldName(final String name, final Namespaces ns) {
        if (name == null) {
            return null;
        }

        if (name.isBlank()) {
            return name;
        }

        if (name.contains("://")) {
            // Already a URL
            return name;
        }

        if (ns == null) {
            return null;
        }

        if (ns.defaultNs != null && !name.contains(":")) {
            return ns.defaultNs + ":" + name;
        }

        return ns.prefixes.entrySet().stream().filter(e -> name.startsWith(e.getKey() + ":"))
                .map(e -> e.getValue() + name.substring(e.getKey().length() + 1)).findFirst().orElse(name);
    }

    /**
     * Looks up the given field name or its shorten form in the given map
     *
     * @param properties List of thing additional properties
     * @param fieldName  Name of the field to look up
     * @param ns         Thing context
     * @return The value of the field, or null
     */
    Object lookupGeoField(final Map<String, Object> properties, final String fieldName, final Namespaces ns) {
        if (properties.containsKey(fieldName)) {
            return properties.get(fieldName);
        }
        return properties.get(shortenFieldName(fieldName, ns));
    }

    /**
     * Tries to convert the given object to a double. Returns {@link Double#NaN} in
     * case of an error.
     *
     * @param value Input value
     * @return The double representation of the given object, or {@link Double#NaN}
     *         in case of an error
     */
    Double toDouble(final Object value) {
        if (value == null) {
            return Double.NaN;
        }

        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        if (value instanceof CharSequence) {
            return Double.parseDouble(value.toString());
        }

        return Double.NaN;
    }

    /**
     * Lookup the geolocation of a thing
     *
     * See
     * https://www.w3.org/TR/wot-thing-description/#semantic-annotations-example-geoloc
     * for more details
     *
     * @param thing Parsed thing
     * @return A location or null if not available
     */
    GeoJsonObject lookupLocation(final Thing thing) {
        final Object latitude, longitude, altitude;

        final Map<String, Object> extraProps = thing.getAdditionalProperties();
        latitude = lookupGeoField(extraProps, GeoNamespaces.W3_GEO_WGS84_LATITUDE, thing.context);
        if (latitude == null) {
            // No latitude: no location
            return null;
        }

        longitude = lookupGeoField(extraProps, GeoNamespaces.W3_GEO_WGS84_LONGITUDE, thing.context);
        if (longitude == null) {
            // No longitude: no location
            return null;
        }

        // Altitude is optional
        altitude = lookupGeoField(extraProps, GeoNamespaces.W3_GEO_WGS84_ALTITUDE, thing.context);

        final Point point = new Point();
        point.coordinates = new Coordinates();
        point.coordinates.latitude = toDouble(latitude);
        point.coordinates.longitude = toDouble(longitude);
        point.coordinates.elevation = toDouble(altitude);
        return point;
    }
}
