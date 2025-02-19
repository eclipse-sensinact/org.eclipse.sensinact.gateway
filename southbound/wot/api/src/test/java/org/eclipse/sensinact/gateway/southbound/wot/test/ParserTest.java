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

package org.eclipse.sensinact.gateway.southbound.wot.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.sensinact.gateway.southbound.wot.api.ActionAffordance;
import org.eclipse.sensinact.gateway.southbound.wot.api.EventAffordance;
import org.eclipse.sensinact.gateway.southbound.wot.api.PropertyAffordance;
import org.eclipse.sensinact.gateway.southbound.wot.api.Thing;
import org.eclipse.sensinact.gateway.southbound.wot.api.dataschema.ArraySchema;
import org.eclipse.sensinact.gateway.southbound.wot.api.dataschema.BooleanSchema;
import org.eclipse.sensinact.gateway.southbound.wot.api.dataschema.DataSchema;
import org.eclipse.sensinact.gateway.southbound.wot.api.dataschema.IntegerSchema;
import org.eclipse.sensinact.gateway.southbound.wot.api.dataschema.ObjectSchema;
import org.eclipse.sensinact.gateway.southbound.wot.api.dataschema.OneOfDataSchema;
import org.eclipse.sensinact.gateway.southbound.wot.api.dataschema.StringSchema;
import org.eclipse.sensinact.gateway.southbound.wot.api.dataschema.UnknownDataTypeSchema;
import org.eclipse.sensinact.gateway.southbound.wot.api.security.BasicSecurityScheme;
import org.eclipse.sensinact.gateway.southbound.wot.api.security.NoSecurityScheme;
import org.eclipse.sensinact.gateway.southbound.wot.api.security.SecurityScheme;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

public class ParserTest {

    /**
     * The object mapper
     */
    final ObjectMapper mapper = JsonMapper.builder().build();

    /**
     * Reads a file from the test folder
     *
     * @param filename File name
     * @return File content as string
     * @throws Exception Error reading file
     */
    String readFile(final String filename) throws Exception {
        try (InputStream inputStream = getClass().getResourceAsStream("/data/" + filename)) {
            if (inputStream == null) {
                throw new FileNotFoundException(filename);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    void testBasic() throws Exception {
        final String rawContent = readFile("basic-td.td.jsonld");
        final Thing thing = mapper.readValue(rawContent, Thing.class);

        assertEquals("https://www.w3.org/2022/wot/td/v1.1", thing.context.defaultNs);
        assertEquals("urn:uuid:0804d572-cce8-422a-bb7c-4412fcd56f06", thing.id);
        assertEquals("MyLampThing", thing.title);
        assertEquals("Thing Description for a Lamp thing", thing.description);

        assertEquals(1, thing.semanticType.size());
        assertEquals("Thing", thing.semanticType.get(0));

        assertEquals(1, thing.security.size());
        assertEquals("basic_sc", thing.security.get(0));

        assertEquals(1, thing.securityDefinitions.size());
        final SecurityScheme scheme = thing.securityDefinitions.get("basic_sc");
        assertInstanceOf(BasicSecurityScheme.class, scheme);
        assertEquals("header", ((BasicSecurityScheme) scheme).in);

        assertEquals(1, thing.properties.size());
        final PropertyAffordance prop = thing.properties.get("status");
        assertNotNull(prop);
        assertNotNull(prop.schema);
        assertEquals("string", prop.schema.type);
        assertEquals(1, prop.forms.size());
        assertEquals("https://mylamp.example.com/status", prop.forms.get(0).href);

        assertEquals(1, thing.actions.size());
        final ActionAffordance action = thing.actions.get("toggle");
        assertNotNull(action);
        assertEquals(1, action.forms.size());
        assertEquals("https://mylamp.example.com/toggle", action.forms.get(0).href);

        assertEquals(1, thing.events.size());
        final EventAffordance event = thing.events.get("overheating");
        assertNotNull(event);
        assertNotNull(event.data);
        assertEquals("string", event.data.type);
        assertEquals(1, event.forms.size());
        assertEquals("https://mylamp.example.com/oh", event.forms.get(0).href);
        assertEquals("longpoll", event.forms.get(0).subprotocol);
    }

    @Test
    void testSmartCoffeeMachine() throws Exception {
        final String rawContent = readFile("smart-coffee-machine.jsonld");
        final Thing thing = mapper.readValue(rawContent, Thing.class);

        assertEquals("https://www.w3.org/2019/wot/td/v1", thing.context.defaultNs);
        assertEquals("urn:dev:wot:example:coffee-machine", thing.id);
        assertEquals("Smart-Coffee-Machine", thing.title);
        assertEquals(
                "A smart coffee machine with a range of capabilities.\nA complementary tutorial is available at http://www.thingweb.io/smart-coffee-machine.html.",
                thing.description);

        assertNull(thing.semanticType);

        assertEquals(1, thing.security.size());
        assertEquals("nosec_sc", thing.security.get(0));
        assertEquals(1, thing.securityDefinitions.size());
        assertInstanceOf(NoSecurityScheme.class, thing.securityDefinitions.get("nosec_sc"));

        assertEquals(5, thing.properties.size());

        // Properties
        PropertyAffordance prop = thing.properties.get("allAvailableResources");
        assertNotNull(prop);
        assertEquals(
                "Current level of all available resources given as an integer percentage for each particular resource.\nThe data is obtained from the machine's sensors but can be set manually in case the sensors are broken.",
                prop.description);
        assertNull(prop.forms);
        assertNotNull(prop.schema);
        assertEquals("object", prop.schema.type);
        assertInstanceOf(ObjectSchema.class, prop.schema);
        ObjectSchema objSchema = (ObjectSchema) prop.schema;
        assertEquals(4, objSchema.properties.size());
        for (String key : List.of("water", "milk", "chocolate", "coffeeBeans")) {
            assertInstanceOf(IntegerSchema.class, objSchema.properties.get(key));
            IntegerSchema intSchema = (IntegerSchema) objSchema.properties.get(key);
            assertEquals("integer", intSchema.type);
            assertEquals(0, intSchema.minimum);
            assertEquals(100, intSchema.maximum);
        }

        prop = thing.properties.get("possibleDrinks");
        assertNotNull(prop);
        assertNotNull(prop.schema);
        assertEquals("The list of possible drinks in general. Doesn't depend on the available resources.",
                prop.description);
        assertEquals("array", prop.schema.type);
        assertInstanceOf(ArraySchema.class, prop.schema);
        assertEquals("string", ((ArraySchema) prop.schema).items.get(0).type);

        prop = thing.properties.get("servedCounter");
        assertNotNull(prop);
        assertNotNull(prop.schema);
        assertEquals("The total number of served beverages.", prop.description);
        assertEquals("integer", prop.schema.type);
        assertInstanceOf(IntegerSchema.class, prop.schema);
        assertEquals(0, ((IntegerSchema) prop.schema).minimum);

        prop = thing.properties.get("maintenanceNeeded");
        assertNotNull(prop);
        assertNotNull(prop.schema);
        assertEquals("boolean", prop.schema.type);
        assertInstanceOf(BooleanSchema.class, prop.schema);
        assertEquals(
                "Shows whether a maintenance is needed. The property is observable. Automatically set to True when the servedCounter property exceeds 1000.",
                prop.description);
        assertTrue(prop.observable);

        prop = thing.properties.get("schedules");
        assertNotNull(prop);
        assertEquals("The list of scheduled tasks.", prop.description);
        assertNull(prop.forms);
        assertNotNull(prop.schema);
        assertEquals("array", prop.schema.type);
        assertInstanceOf(ArraySchema.class, prop.schema);
        List<DataSchema> arrSchemaItems = ((ArraySchema) prop.schema).items;
        assertNotNull(arrSchemaItems);
        assertEquals(1, arrSchemaItems.size());
        assertInstanceOf(ObjectSchema.class, arrSchemaItems.get(0));
        objSchema = (ObjectSchema) arrSchemaItems.get(0);
        assertEquals(5, objSchema.properties.size());

        // ... Drink ID
        assertInstanceOf(StringSchema.class, objSchema.properties.get("drinkId"));
        assertEquals("Defines what drink to make, drinkId is one of possibleDrinks property values, e.g. latte.",
                objSchema.properties.get("drinkId").description);

        // ... Size
        assertInstanceOf(StringSchema.class, objSchema.properties.get("size"));
        DataSchema subSchema = objSchema.properties.get("size");
        assertEquals("Defines the size of a drink, s = small, m = medium, l = large.", subSchema.description);
        assertEquals(List.of("s", "m", "l"), subSchema.enumOfAllowedValues);

        // ... Quantity
        assertInstanceOf(IntegerSchema.class, objSchema.properties.get("quantity"));
        IntegerSchema intSchema = (IntegerSchema) objSchema.properties.get("quantity");
        assertEquals("integer", intSchema.type);
        assertEquals(1, intSchema.minimum);
        assertEquals(5, intSchema.maximum);

        // ... Time
        assertInstanceOf(StringSchema.class, objSchema.properties.get("time"));
        assertEquals("Defines the time of the scheduled task in 24h format, e.g. 10:00 or 21:00.",
                objSchema.properties.get("time").description);

        // ... Mode
        assertInstanceOf(StringSchema.class, objSchema.properties.get("mode"));
        subSchema = objSchema.properties.get("mode");
        assertEquals(
                "Defines the mode of the scheduled task, e.g. once or everyday. All the possible values are given in the enum field of this Thing Description.",
                subSchema.description);
        assertEquals(List.of("once", "everyday", "everyMo", "everyTu", "everyWe", "everyTh", "everyFr", "everySat",
                "everySun"), subSchema.enumOfAllowedValues);

        // Actions
        assertEquals(2, thing.actions.size());
        ActionAffordance action = thing.actions.get("makeDrink");
        assertNotNull(action);
        assertEquals(
                "Make a drink from available list of beverages. Accepts drink id, size and quantity as input.\nBrews one medium americano if no input is specified.",
                action.description);
        assertInstanceOf(ObjectSchema.class, action.input);
        objSchema = (ObjectSchema) action.input;
        // ... Drink ID
        assertInstanceOf(StringSchema.class, objSchema.properties.get("drinkId"));
        assertEquals("Defines what drink to make, drinkId is one of possibleDrinks property values, e.g. latte.",
                objSchema.properties.get("drinkId").description);

        // ... Size
        assertInstanceOf(StringSchema.class, objSchema.properties.get("size"));
        subSchema = objSchema.properties.get("size");
        assertEquals("Defines the size of a drink, s = small, m = medium, l = large.", subSchema.description);
        assertEquals(List.of("s", "m", "l"), subSchema.enumOfAllowedValues);

        // ... Quantity
        assertInstanceOf(IntegerSchema.class, objSchema.properties.get("quantity"));
        intSchema = (IntegerSchema) objSchema.properties.get("quantity");
        assertEquals("integer", intSchema.type);
        assertEquals(1, intSchema.minimum);
        assertEquals(5, intSchema.maximum);

        assertInstanceOf(ObjectSchema.class, action.output);
        objSchema = (ObjectSchema) action.output;
        assertEquals("Returns True/false and a message when all invoked promises are resolved (asynchronous).",
                objSchema.description);
        assertEquals(2, objSchema.properties.size());
        assertInstanceOf(BooleanSchema.class, objSchema.properties.get("result"));
        assertInstanceOf(StringSchema.class, objSchema.properties.get("message"));

        action = thing.actions.get("setSchedule");
        assertEquals(
                "Add a scheduled task to the schedules property. Accepts drink id, size, quantity, time and mode as body of a request.\nAssumes one medium americano if not specified, but time and mode are mandatory fields.",
                action.description);
        assertInstanceOf(ObjectSchema.class, action.input);
        objSchema = (ObjectSchema) action.input;
        assertEquals(List.of("time", "mode"), objSchema.required);

        assertInstanceOf(ObjectSchema.class, action.output);
        objSchema = (ObjectSchema) action.output;
        assertEquals("Returns True/false and a message when all invoked promises are resolved (asynchronous).",
                objSchema.description);
        assertEquals(2, objSchema.properties.size());
        assertInstanceOf(BooleanSchema.class, objSchema.properties.get("result"));
        assertInstanceOf(StringSchema.class, objSchema.properties.get("message"));

        // Events
        assertEquals(1, thing.events.size());
        final EventAffordance event = thing.events.get("outOfResource");
        assertNotNull(event);
        assertNotNull(event.data);
        assertEquals("string", event.data.type);
        assertEquals(
                "Out of resource event. Emitted when the available resource level is not sufficient for a desired drink.",
                event.description);
        assertNull(event.forms);
    }

    @Test
    void testDataSchemaResolution() throws Exception {
        // Deserialization
        final DataSchema ds = mapper.readValue(readFile("dataschema-test.jsonld"), DataSchema.class);
        assertInstanceOf(ObjectSchema.class, ds);
        final ObjectSchema objDs = (ObjectSchema) ds;
        assertNotNull(objDs.properties);
        assertEquals(4, objDs.properties.size());
        assertInstanceOf(IntegerSchema.class, objDs.properties.get("month"));
        assertInstanceOf(IntegerSchema.class, objDs.properties.get("year"));
        assertInstanceOf(StringSchema.class, objDs.properties.get("route_type"));

        assertInstanceOf(OneOfDataSchema.class, objDs.properties.get("realm_id"));
        final OneOfDataSchema oneOfDs = (OneOfDataSchema) objDs.properties.get("realm_id");
        assertNull(oneOfDs.type);
        assertNotNull(oneOfDs.oneOf);
        assertEquals(2, oneOfDs.oneOf.size());
        assertInstanceOf(StringSchema.class, oneOfDs.oneOf.get(0));
        assertInstanceOf(UnknownDataTypeSchema.class, oneOfDs.oneOf.get(1));
        final UnknownDataTypeSchema unknown = (UnknownDataTypeSchema) oneOfDs.oneOf.get(1);
        assertEquals("uuid", unknown.type);
        assertEquals(Map.of("extra", 123), unknown.extraProperties);

        // Serialization
        final String out = mapper.writeValueAsString(ds);
        assertEquals(6, Pattern.compile("(\"type\":)").matcher(out).results().count());
        assertFalse(out.toLowerCase().contains("jsonschema:"));

        // Round back
        final DataSchema reloaded = mapper.readValue(out, DataSchema.class);
        final ObjectSchema reloadedObjDs = (ObjectSchema) reloaded;
        assertNotNull(reloadedObjDs.properties);
        assertEquals(4, reloadedObjDs.properties.size());
        assertInstanceOf(IntegerSchema.class, reloadedObjDs.properties.get("month"));
        assertInstanceOf(IntegerSchema.class, reloadedObjDs.properties.get("year"));
        assertInstanceOf(StringSchema.class, reloadedObjDs.properties.get("route_type"));

        assertInstanceOf(OneOfDataSchema.class, reloadedObjDs.properties.get("realm_id"));
        final OneOfDataSchema reloadedOneOfDs = (OneOfDataSchema) reloadedObjDs.properties.get("realm_id");
        assertNull(reloadedOneOfDs.type);
        assertNotNull(reloadedOneOfDs.oneOf);
        assertEquals(2, reloadedOneOfDs.oneOf.size());
        assertInstanceOf(StringSchema.class, reloadedOneOfDs.oneOf.get(0));
        assertInstanceOf(UnknownDataTypeSchema.class, reloadedOneOfDs.oneOf.get(1));
        final UnknownDataTypeSchema reloadedUnknown = (UnknownDataTypeSchema) reloadedOneOfDs.oneOf.get(1);
        assertEquals("uuid", reloadedUnknown.type);
        assertEquals(Map.of("extra", 123), reloadedUnknown.extraProperties);
    }
}
