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

import org.eclipse.sensinact.gateway.southbound.wot.api.dataschema.DataSchema;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * A property instance is both an InteractionAffordance and a DataSchema
 */
@JsonDeserialize(using = PropertyAffordance.Deserializer.class)
public class PropertyAffordance extends InteractionAffordance {

    public boolean observable = false;

    @JsonIgnore
    public DataSchema schema;

    @JsonAnyGetter
    public Map<String, Object> schemaProperties() {
        return new ObjectMapper().convertValue(schema, new TypeReference<Map<String, Object>>() {
        });
    }

    @SuppressWarnings("serial")
    public static class Deserializer extends StdDeserializer<PropertyAffordance> {

        public Deserializer() {
            super(PropertyAffordance.class);
        }

        @Override
        public PropertyAffordance deserialize(JsonParser jp, DeserializationContext ctxt)
                throws IOException, JacksonException {
            ObjectCodec oc = jp.getCodec();
            JsonNode node = oc.readTree(jp);

            // Parse interaction affordance fields
            InteractionAffordance ia = oc.readValue(node.traverse(), InteractionAffordance.class);
            PropertyAffordance pa = new PropertyAffordance();
            pa.semanticType = ia.semanticType;
            pa.title = ia.title;
            pa.description = ia.description;
            pa.forms = ia.forms;

            // Specific to PropertyAffordance
            JsonNode valueNode = node.get("observable");
            pa.observable = valueNode != null ? valueNode.asBoolean() : false;

            pa.schema = oc.readValue(node.traverse(), DataSchema.class);
            return pa;
        }
    }
}
