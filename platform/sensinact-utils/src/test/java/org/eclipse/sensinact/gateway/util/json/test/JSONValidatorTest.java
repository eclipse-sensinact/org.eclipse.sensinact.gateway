package org.eclipse.sensinact.gateway.util.json.test;

import org.eclipse.sensinact.gateway.util.json.JSONValidator;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class JSONValidatorTest {
    public static final String VALID_OBJECT = "{\"contextResponses\":[{\"contextElement\":{\"attributes\": " + "[ { \"metadatas\": [ { \"name\": \"code\", \"type\": \"\", \"value\": \"deg\" } ]," + " \"name\": \"Latitud\", \"type\": \"urn:x-ogc:def:phenomenon:IDAS:1.0:latitude\", " + "\"value\": \"43.46436\" }, { \"metadatas\": [ { \"name\": \"code\", \"type\": \"\"," + " \"value\": \"deg\" } ], \"name\": \"Longitud\", \"type\": " + "\"urn:x-ogc:def:phenomenon:IDAS:1.0:longitude\", \"value\": \"-3.79663\" }," + " { \"name\": \"TimeInstant\", \"type\": \"urn:x-ogc:def:trs:IDAS:1.0:ISO8601\"," + " \"value\": \"2015-10-09T13:32:13.000000Z\" }, { \"metadatas\": [ " + "{ \"name\": \"code\", \"type\": \"\", \"value\": \"%\" } ], " + "\"name\": \"batteryCharge\", \"type\": " + "\"urn:x-ogc:def:phenomenon:IDAS:1.0:batteryCharge\", " + "\"value\": \"78\" }, { \"metadatas\": [ { \"name\":" + " \"code\", \"type\": \"\", \"value\": \"lm\" } ]," + " \"name\": \"luminousFlux\", \"type\": " + "\"urn:x-ogc:def:phenomenon:IDAS:1.0:luminousFlux\", " + "\"value\": \"7367.49\" }, { \"metadatas\": [ " + "{ \"name\": \"code\", \"type\": \"\", \"value\": \"Cel\" } ]," + " \"name\": \"temperature\", \"type\": \"urn:x-ogc:def:phenomenon:IDAS:1.0:temperature\"," + " \"value\": \"-24.58\" } ], \"id\": \"urn:x-iot:smartsantander:u7jcfa:fixed:t384\", " + "\"isPattern\": \"false\", \"type\": \"santander:device\" }, " + "\"statusCode\": { \"code\": 200, \"reasonPhrase\": \"OK\" } } ] }";

    public static final String INVALID_OBJECT = "{\"contextResponses\":[{\"contextElement\":{\"attributes\": " + "[ { \"metadatas\": [ { \"name\": \"code\", \"type\": \"\", \"value\": \"deg\" } ]," + " \"name\": \"Latitud\", \"type\": \"urn:x-ogc:def:phenomenon:IDAS:1.0:latitude\", " + "\"value\": \"43.46436\" }, { \"metadatas\": [ { \"name\": \"code\", \"type\": \"\"," + " \"value\": \"deg\" } ], \"name\": \"Longitud\", \"type\": " + "\"urn:x-ogc:def:phenomenon:IDAS:1.0:longitude\", \"value\": \"-3.79663\" }," + " { \"name\": \"TimeInstant\", \"type\": \"urn:x-ogc:def:trs:IDAS:1.0:ISO8601\"," + " \"value\": \"2015-10-09T13:32:13.000000Z\" }, { \"metadatas\": [ " + "{ \"name\": \"code\", \"type\": \"\", \"value\": \"%\" } ], " + "\"name\": \"batteryCharge\", \"type\": " + "\"urn:x-ogc:def:phenomenon:IDAS:1.0:batteryCharge\", " + "\"value\": \"78\" }}, { \"metadatas\": [ { \"name\":" + " \"code\", \"type\": \"\", \"value\": \"lm\" } ]," + " \"name\": \"luminousFlux\", \"type\": " + "\"urn:x-ogc:def:phenomenon:IDAS:1.0:luminousFlux\", " + "\"value\": \"7367.49\" }, { \"metadatas\": [ " + "{ \"name\": \"code\", \"type\": \"\", \"value\": \"Cel\" } ]," + " \"name\": \"temperature\", \"type\": \"urn:x-ogc:def:phenomenon:IDAS:1.0:temperature\"," + " \"value\": \"-24.58\" } ], \"id\": \"urn:x-iot:smartsantander:u7jcfa:fixed:t384\", " + "\"isPattern\": \"false\", \"type\": \"santander:device\" }, " + "\"statusCode\": { \"code\": 200, \"reasonPhrase\": \"OK\" } } ] }";

    /**
     *
     */
    @Test
    public void testValidObjectValidated() {
        assertTrue(new JSONValidator(VALID_OBJECT).valid());
    }

    /**
     *
     */
    @Test
    public void testInvalidObjectInvalidated() {
        assertFalse(new JSONValidator(INVALID_OBJECT).valid());
    }
}
