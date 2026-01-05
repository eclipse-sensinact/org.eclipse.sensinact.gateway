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
package org.eclipse.sensinact.sensorthings.sensing.rest.filters;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.sensinact.sensorthings.sensing.dto.Id;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.ExpansionSettings;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class ExpansionSettingsImpl implements ExpansionSettings {

    private final Map<String, ExpansionSettingsImpl> configuredExpansions = new HashMap<>();

    private final Map<String, Map<String, Object>> expandedValues = new HashMap<>();

    private ExpansionSettingsImpl() {}

    public ExpansionSettingsImpl(Stream<String> expansion) {
        expansion.forEach(this::addRequestedExpansion);
    }

    private void addRequestedExpansion(String expansion) {

        ExpansionSettingsImpl settings = this;

        for(String s : expansion.split("/")) {
            settings = settings.configuredExpansions.computeIfAbsent(s, x -> new ExpansionSettingsImpl());
        }
    }

    @Override
    public ExpansionSettings getExpansionSettings(String pathSegment) {
        return configuredExpansions.get(pathSegment);
    }

    @Override
    public boolean shouldExpand(String pathSegment, Id context) {
        return configuredExpansions.containsKey(pathSegment) &&
                Optional.ofNullable(expandedValues.get(pathSegment)).map(m -> m.get(toStringId(context))).isEmpty();
    }

    @Override
    public void addExpansion(String pathSegment, Id context, Object expansion) {
        if(!shouldExpand(pathSegment, context)) {
            throw new IllegalArgumentException("The path " + pathSegment + " is not expandable");
        }
        expandedValues.computeIfAbsent(pathSegment, x -> new HashMap<>()).put(toStringId(context), expansion);
    }

    private String toStringId(Id context) {
        return String.valueOf(context.id());
    }

    public boolean isExpanded(String pathSegment) {
        return expandedValues.containsKey(pathSegment);
    }

    public Object getExpanded(String pathSegment) {
        return expandedValues.get(pathSegment);
    }

    public boolean isEmpty() {
        return configuredExpansions.isEmpty();
    }

    private Map<String, Object> getExpansionsFor(JsonNode context) {

        if(context.hasNonNull("@iot.id")) {
            String id = context.get("@iot.id").asText();

            return expandedValues.entrySet().stream()
                    .filter(e -> e.getValue().containsKey(id))
                    .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().get(id)));
        } else {
            return Map.of();
        }
    }

    public JsonNode processExpansions(ObjectMapper mapper, Object object) {
        JsonNode node;
        if(object instanceof JsonNode) {
            node = (JsonNode) object;
        } else {
            node = mapper.valueToTree(object);
        }

        processExpansions(mapper, node);

        return node;
    }

    private void processExpansions(ObjectMapper mapper, JsonNode node) {

        if(node.getNodeType() == JsonNodeType.OBJECT) {
            ObjectNode on = (ObjectNode) node;

            if(isResultList(on)) {
                processExpansions(mapper, on.get("value"));
                return;
            }

            for(Entry<String, Object> e : getExpansionsFor(on).entrySet()) {
                String field = e.getKey();
                JsonNode expansion = configuredExpansions.get(field).processExpansions(mapper, e.getValue());
                String linkField = field + "@iot.navigationLink";
                JsonNode link = on.remove(linkField);
                if(isResultList(expansion)) {
                    JsonNode value = expansion.get("value");
                    JsonNode count = expansion.get("@iot.count");
                    if(count != null && count.isNumber()) {
                        on.set(field + "@iot.count", count);
                        if(value.isArray() && count.asInt() > value.size()) {
                            on.put(field + "@iot.nextLink", link.asText() + "$skip=" + value.size());
                        }
                    }
                    on.set(field, value);
                } else {
                    on.set(field, expansion);
                }
            }
        } else if (node.getNodeType() == JsonNodeType.ARRAY) {
            node.forEach(n -> processExpansions(mapper, n));
        }
    }

    private boolean isResultList(JsonNode json) {
        // Must have a null or array value
        if(json.has("value")) {
            JsonNode node = json.get("value");
            if(node.isArray() || node.isNull()) {
                // Only value, count and nextLink are permitted
                Set<String> permittedKeys = Set.of("value", "@iot.count", "@iot.nextLink");
                return node.properties().stream()
                    .noneMatch(e -> !permittedKeys.contains(e.getKey()));
            }
        }
        return false;
    }
}
