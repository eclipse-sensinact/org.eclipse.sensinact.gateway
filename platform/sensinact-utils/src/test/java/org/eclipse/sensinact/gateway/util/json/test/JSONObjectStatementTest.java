/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.util.json.test;

import org.eclipse.sensinact.gateway.util.json.JSONObjectStatement;
import org.json.JSONObject;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

public class JSONObjectStatementTest {
    public static final JSONObject RESOLVED_OBJECT = new JSONObject("{\"contextResponses\":[{\"contextElement\":{\"attributes\": " + "[ { \"metadatas\": [ { \"name\": \"code\", \"type\": \"\", \"value\": \"deg\" } ]," + " \"name\": \"Latitud\", \"type\": \"urn:x-ogc:def:phenomenon:IDAS:1.0:latitude\", " + "\"value\": \"43.46436\" }, { \"metadatas\": [ { \"name\": \"code\", \"type\": \"\"," + " \"value\": \"deg\" } ], \"name\": \"Longitud\", \"type\": " + "\"urn:x-ogc:def:phenomenon:IDAS:1.0:longitude\", \"value\": \"-3.79663\" }," + " { \"name\": \"TimeInstant\", \"type\": \"urn:x-ogc:def:trs:IDAS:1.0:ISO8601\"," + " \"value\": \"2015-10-09T13:32:13.000000Z\" }, { \"metadatas\": [ " + "{ \"name\": \"code\", \"type\": \"\", \"value\": \"%\" } ], " + "\"name\": \"batteryCharge\", \"type\": " + "\"urn:x-ogc:def:phenomenon:IDAS:1.0:batteryCharge\", " + "\"value\": \"78\" }, { \"metadatas\": [ { \"name\":" + " \"code\", \"type\": \"\", \"value\": \"lm\" } ]," + " \"name\": \"luminousFlux\", \"type\": " + "\"urn:x-ogc:def:phenomenon:IDAS:1.0:luminousFlux\", " + "\"value\": \"7367.49\" }, { \"metadatas\": [ " + "{ \"name\": \"code\", \"type\": \"\", \"value\": \"Cel\" } ]," + " \"name\": \"temperature\", \"type\": \"urn:x-ogc:def:phenomenon:IDAS:1.0:temperature\"," + " \"value\": \"-24.58\" } ], \"id\": \"urn:x-iot:smartsantander:u7jcfa:fixed:t384\", " + "\"isPattern\": \"false\", \"type\": \"santander:device\" }, " + "\"statusCode\": { \"code\": 200, \"reasonPhrase\": \"OK\" } } ] }");

    public static final JSONObjectStatement STATEMENT_OBJECT = new JSONObjectStatement("{\"contextResponses\":[{\"contextElement\":{\"attributes\": " + "[ { \"metadatas\": [ { \"name\": \"code\", \"type\": \"\", \"value\": \"deg\" } ]," + " \"name\": \"Latitud\", \"type\": \"urn:x-ogc:def:phenomenon:IDAS:1.0:latitude\", " + "\"value\": \"43.46436\" }, { \"metadatas\": [ { \"name\": \"code\", \"type\": \"\"," + " \"value\": \"deg\" } ], \"name\": \"Longitud\", \"type\": " + "\"urn:x-ogc:def:phenomenon:IDAS:1.0:longitude\", \"value\": \"-3.79663\" }," + " { \"name\": \"TimeInstant\", \"type\": \"urn:x-ogc:def:trs:IDAS:1.0:ISO8601\"," + " \"value\": $(time_instant) }, { \"metadatas\": [ " + "{ \"name\": \"code\", \"type\": \"\", \"value\": \"%\" } ], " + "\"name\": \"batteryCharge\", \"type\": " + "\"urn:x-ogc:def:phenomenon:IDAS:1.0:batteryCharge\", " + "\"value\": $(battery) }, { \"metadatas\": [ { \"name\":" + " \"code\", \"type\": \"\", \"value\": \"lm\" } ]," + " \"name\": \"luminousFlux\", \"type\": " + "\"urn:x-ogc:def:phenomenon:IDAS:1.0:luminousFlux\", " + "\"value\": \"7367.49\" }, { \"metadatas\": [ " + "{ \"name\": \"code\", \"type\": \"\", \"value\": $(metadata) } ]," + " \"name\": \"temperature\", \"type\": \"urn:x-ogc:def:phenomenon:IDAS:1.0:temperature\"," + " \"value\": \"-24.58\" } ], \"id\": \"urn:x-iot:smartsantander:u7jcfa:fixed:t384\", " + "\"isPattern\": \"false\", \"type\": \"santander:device\" }, " + "\"statusCode\": { \"code\": $(status), \"reasonPhrase\": \"OK\" } } ] }");

    /**
     *
     */
    @Test
    public void testStatement() {
        String timeInstant = "2015-10-09T13:32:13.000000Z";
        String battery = "78";
        String metadata = "Cel";
        int status = 200;
        STATEMENT_OBJECT.apply("time_instant", timeInstant);
        STATEMENT_OBJECT.apply("battery", battery);
        STATEMENT_OBJECT.apply("metadata", metadata);
        STATEMENT_OBJECT.apply("status", status);

        JSONAssert.assertEquals(RESOLVED_OBJECT.toString(), STATEMENT_OBJECT.toString(), false);
    }
}
