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

package org.eclipse.sensinact.gateway.southbound.wot.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.deser.std.StdNodeBasedDeserializer;
import tools.jackson.databind.ser.std.StdSerializer;

@JsonDeserialize(using = Namespaces.Deserializer.class)
@JsonSerialize(using = Namespaces.Serializer.class)
public class Namespaces {

    /**
     * Default namespace (first string entry)
     */
    public String defaultNs;

    /**
     * Prefixes definitions
     */
    public Map<String, String> prefixes;

    /**
     * Other contexts definition
     */
    public List<String> contexts;

    /**
     * Counts the number of declared namespaces (default + prefixes)
     */
    @JsonIgnore
    public int size() {
        return (defaultNs != null ? 1 : 0) + (prefixes == null ? 0 : prefixes.size());
    }

    @Override
    public String toString() {
        return String.format("Namespaces(defaultNs=%s, prefixes=%s, contexts=%s)", defaultNs, prefixes, contexts);
    }

    @SuppressWarnings("serial")
    public static class Deserializer extends StdNodeBasedDeserializer<Namespaces> {
        public Deserializer() {
            super(Namespaces.class);
        }

        @Override
        public Namespaces convert(JsonNode root, DeserializationContext ctxt) throws JacksonException {
            if (root.isNull()) {
                return null;
            }

            Namespaces ns = new Namespaces();
            ns.prefixes = Map.of();

            if (root.isString()) {
                ns.defaultNs = root.stringValue();
                ns.prefixes = Map.of();
                ns.contexts = List.of();
            } else if (root.isObject()) {
                ns.defaultNs = null;
                ns.prefixes = root.propertyStream().collect(Collectors.toMap(Entry::getKey, e -> e.getValue().stringValue()));
                ns.contexts = List.of();
            } else if (root.isArray()) {
                ns.defaultNs = null;
                final Map<String, String> prefixes = new HashMap<>();
                final List<String> contexts = new ArrayList<>();

                var iter = root.iterator();
                int idx = 0;
                while (iter.hasNext()) {
                    var node = iter.next();
                    if (node.isString()) {
                        if (idx == 0) {
                            // First entry is a URI: use it as default
                            ns.defaultNs = node.stringValue();
                        } else {
                            // Other entries are context
                            contexts.add(node.stringValue());
                        }
                    } else if (node.isObject()) {
                        prefixes.putAll(node.propertyStream().collect(Collectors.toMap(Entry::getKey, e -> e.getValue().stringValue())));
                    } else {
                        throw new RuntimeException("Invalid namespaces: " + root);
                    }
                    idx++;
                }

                // Convert to unmodifiable list
                ns.prefixes = Collections.unmodifiableMap(prefixes);
                ns.contexts = Collections.unmodifiableList(contexts);
            } else {
                throw new RuntimeException("Invalid namespaces: " + root);
            }
            return ns;
        }
    }

    @SuppressWarnings("serial")
    public static class Serializer extends StdSerializer<Namespaces> {
        public Serializer() {
            super(Namespaces.class);
        }

        @Override
        public void serialize(Namespaces value, JsonGenerator gen, SerializationContext provider) throws JacksonException {
            if (value == null) {
                gen.writeNull();
                return;
            }

            final boolean hasPrefixes = value.prefixes != null && !value.prefixes.isEmpty();
            final boolean hasContexts = value.contexts != null && !value.contexts.isEmpty();

            if (!hasPrefixes && !hasContexts) {
                gen.writeString(value.defaultNs);
                return;
            }

            if (value.defaultNs == null && !hasContexts) {
                gen.writePOJO(value.prefixes);
                return;
            }

            gen.writeStartArray();
            gen.writeString(value.defaultNs);
            if (value.prefixes != null && !value.prefixes.isEmpty()) {
                gen.writePOJO(value.prefixes);
            }
            if (value.contexts != null && !value.contexts.isEmpty()) {
                for (String ns : value.contexts) {
                    gen.writeString(ns);
                }
            }
            gen.writeEndArray();
        }
    }
}
