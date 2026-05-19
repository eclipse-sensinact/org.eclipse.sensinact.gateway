/*********************************************************************
* Copyright (c) 2025 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.filters.resource.selector.jackson;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.sensinact.filters.resource.selector.api.CompactResourceSelector;
import org.eclipse.sensinact.filters.resource.selector.api.ResourceSelector;
import org.eclipse.sensinact.filters.resource.selector.api.ResourceSelector.ProviderSelection;
import org.eclipse.sensinact.filters.resource.selector.api.ResourceSelector.ResourceSelection;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdNodeBasedDeserializer;
import tools.jackson.databind.exc.InvalidFormatException;
import tools.jackson.databind.node.JsonNodeType;

public class ResourceSelectorDeserializer extends StdNodeBasedDeserializer<ResourceSelector> {

    public ResourceSelectorDeserializer() {
        super(ResourceSelector.class);
    }

    private static final Set<Object> FULL_KEYS = Set.of("providers", "resources");

    @Override
    public ResourceSelector convert(JsonNode root, DeserializationContext ctxt) throws JacksonException {

        if(root.getNodeType() != JsonNodeType.OBJECT) {
            ctxt.reportBadCoercion(this, ResourceSelector.class, root, "Resource Selector must be a JSON object but was %s", root.getNodeType());
            // Not used as the previous line throws an exception
            return null;
        }

        ResourceSelector rs;
        if(root.has("providers")) {
            // This is a full selector
            root.propertyStream().filter(e -> !FULL_KEYS.contains(e.getKey()))
                    .forEach(p -> {
                        try (JsonParser jp = p.getValue().traverse(ctxt)) {
                            ctxt.handleUnknownProperty(jp, this, root, p.getKey());
                        }
                    });

            List<ProviderSelection> providers = getSelectionList(ctxt, root, "providers", ProviderSelection.class);
            List<ResourceSelection> resources = getSelectionList(ctxt, root, "resources", ResourceSelection.class);

            rs = new ResourceSelector(providers, resources);
        } else {
            // This is a compact selector
            rs = ctxt.readTreeAsValue(root, CompactResourceSelector.class).toResourceSelector();
        }
        return rs;
    }

    private <T> List<T> getSelectionList(DeserializationContext ctxt, JsonNode root, String key, Class<T> type)
            throws JacksonException, InvalidFormatException {
        JsonNode child = root.get(key);
        if(child == null) {
            return List.of();
        } else {
            return switch(child.getNodeType()) {
            case NULL: yield List.of();
            case OBJECT: yield List.of(ctxt.readTreeAsValue(child, type));
            case ARRAY:
                List<T> selection = new ArrayList<>(child.size());
                for(JsonNode n : child) {
                    selection.add(ctxt.readTreeAsValue(n, type));
                }
                yield List.copyOf(selection);
            default:
                ctxt.reportPropertyInputMismatch(type, key, "The %s property of a ResourceSelector must be an Object, Array or null; the deserializer encountered %s", key, child.getNodeType());
                yield List.of();
            };
        }
    }
}
