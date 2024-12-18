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

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdNodeBasedDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

@JsonDeserialize(using = Namespaces.Deserializer.class)
@JsonSerialize(using = Namespaces.Serializer.class)
public class Namespaces {

    /**
     * Default namespace
     */
    public String defaultNs;

    /**
     * Prefixes definitions
     */
    public Map<String, String> prefixes;

    /**
     * Counts the number of declared namespaces (default + prefixes)
     */
    @JsonIgnore
    public int size() {
        return (defaultNs != null ? 1 : 0) + (prefixes == null ? 0 : prefixes.size());
    }

    @Override
    public String toString() {
        return String.format("Namespaces(defaultNs=%s, prefixes=%s)", defaultNs, prefixes);
    }

    @SuppressWarnings("serial")
    public static class Deserializer extends StdNodeBasedDeserializer<Namespaces> {
        public Deserializer() {
            super(Namespaces.class);
        }

        @Override
        public Namespaces convert(JsonNode root, DeserializationContext ctxt) throws IOException {
            if (root.isNull()) {
                return null;
            }

            Namespaces ns = new Namespaces();
            ns.prefixes = Map.of();

            if (root.isTextual()) {
                ns.defaultNs = root.textValue();
                ns.prefixes = Map.of();
            } else if (root.isObject()) {
                ns.defaultNs = null;
                ns.prefixes = StreamSupport.stream(Spliterators.spliteratorUnknownSize(root.fields(), 0), false)
                        .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().textValue()));
            } else if (root.isArray()) {
                var iter = root.iterator();
                while (iter.hasNext()) {
                    var node = iter.next();
                    if (node.isTextual()) {
                        if (ns.defaultNs != null) {
                            throw new JsonParseException("Multiple default namespaces");
                        } else {
                            ns.defaultNs = node.textValue();
                        }
                    } else if (node.isObject()) {
                        if (!ns.prefixes.isEmpty()) {
                            throw new JsonParseException("Multiple prefix definitions");
                        } else {
                            ns.prefixes = StreamSupport
                                    .stream(Spliterators.spliteratorUnknownSize(node.fields(), 0), false)
                                    .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().textValue()));
                        }
                    } else {
                        throw new JsonParseException("Invalid namespaces: " + root);
                    }
                }
            } else {
                throw new JsonParseException("Invalid namespaces: " + root);
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
        public void serialize(Namespaces value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            if (value == null || (value.prefixes == null && value.defaultNs == null)) {
                gen.writeNull();
                return;
            }

            if (value.prefixes == null || value.prefixes.isEmpty()) {
                gen.writeString(value.defaultNs);
                return;
            }

            if (value.defaultNs == null) {
                gen.writeObject(value.prefixes);
                return;
            }

            gen.writeStartArray();
            gen.writeString(value.defaultNs);
            gen.writeObject(value.prefixes);
            gen.writeEndArray();
        }
    }
}
