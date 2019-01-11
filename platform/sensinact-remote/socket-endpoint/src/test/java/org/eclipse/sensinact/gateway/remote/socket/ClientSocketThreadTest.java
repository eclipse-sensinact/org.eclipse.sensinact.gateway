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
package org.eclipse.sensinact.gateway.remote.socket;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:stephane.bergeon@cea.fr">Stéphane Bergeon</a>
 */
public class ClientSocketThreadTest {

    public ClientSocketThreadTest() {
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
    public void testUUIDtoTimeStamp() {
        final long timestamp = System.currentTimeMillis();
        try {
            ClientSocketThread instance = new ClientSocketThread(null, null, "localhost", 80,5000l);
            final String uuid = instance.generateUUID(timestamp);
            System.out.print("at " + timestamp);
            System.out.println(" generated uuid= " + uuid);
            long requestTime = instance.getRequestTime(uuid);
            System.out.println("recovered time stamp for request= " + requestTime);
            assertEquals("error in uuid generation / time stamp recovery", timestamp, requestTime);
        } catch (IOException ex) {
            Logger.getLogger(ClientSocketThreadTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("unexpected error in testUUIDtoTimeStamp");
        }
    }
}