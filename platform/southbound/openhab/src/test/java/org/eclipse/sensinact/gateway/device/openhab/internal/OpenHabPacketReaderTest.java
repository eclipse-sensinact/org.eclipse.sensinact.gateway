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
package org.eclipse.sensinact.gateway.device.openhab.internal;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Stéphane Bergeon <stephane.bergeon@cea.fr
 */
public class OpenHabPacketReaderTest {
    
    public OpenHabPacketReaderTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testParseOpenhabPath_zwave_device_07150a2a_node21_alarm_general() {
        testParseOpenhabPath("zwave_device_07150a2a_node21_alarm_general");
    }

    @Test
    public void testParseOpenhabPath_Testeur_3_zwave_device_1e6c7d23_node10_sensor_temperature() {
        testParseOpenhabPath("Testeur_3:zwave_device_1e6c7d23_node10_sensor_temperature");
    }

    @Test
    public void testParseOpenhabPath_zwave_device_07150a2a_node16_config_decimal_param61() {
        testParseOpenhabPath("zwave_device_07150a2a_node16_config_decimal_param61", "decimal_param61");
    }
    
    @Test
    public void testCreateProvider_Testeur_3_zwave_device_1e6c7d23_node12_sensor_door() {
        testCreateProvider("Testeur_3:zwave_device_1e6c7d23_node12_sensor_door");
    }

    @Test
    public void testCreateProvider_zwave_device_07150a2a_node21_alarm_general() {
        testCreateProvider("zwave_device_07150a2a_node21_alarm_general");
    }

    private String[]  testParseOpenhabPath(final String openhabDeviceId) {
        return testParseOpenhabPath(openhabDeviceId, null);
    }

    private String[] testParseOpenhabPath(final String openhabDeviceId, final String expectedResourceId) {
        String[] parseOpenhabPath = null;
        try {
            parseOpenhabPath = OpenHabPacketReader.parseOpenhabPath(openhabDeviceId);
            assertNotNull("unexpected null path", parseOpenhabPath);
            assertEquals("unexpected path length", 4, parseOpenhabPath.length);
            for (int i = 0; i < 4; i ++) {
                assertNotNull("unexpected null element", parseOpenhabPath[i]);
            }
            System.out.println("Provider=" + OpenHabPacketReader.OPENHAB_ZWAVE_PROVIDER_ID_FORMAT.format(parseOpenhabPath));
            System.out.println("Service =" + parseOpenhabPath[2]);
            System.out.println("Resource=" + parseOpenhabPath[3]);
            if (expectedResourceId != null) {
                assertEquals("unexpected resource id", expectedResourceId, parseOpenhabPath[3]);
            }
        } catch (Exception ex) {
            fail("unexpected error:" + ex.getMessage());
        }
        return parseOpenhabPath;
    }
    
    private void testCreateProvider(final String openhabDeviceId) {
        String[] parsedOpenhabPath = testParseOpenhabPath(openhabDeviceId);
        assertNotNull("unexpected null path", parsedOpenhabPath);
        Object createdProvider = OpenHabPacketReader.createProvider(parsedOpenhabPath);
        assertNotNull("unexpected null provider", createdProvider);
        System.out.println("provider= " + createdProvider.toString());
    }

}
