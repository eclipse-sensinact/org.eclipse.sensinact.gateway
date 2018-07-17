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
package org.eclipse.sensinact.gateway.datastore.sqlite.test;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;
import org.eclipse.sensinact.gateway.datastore.api.UnableToConnectToDataStoreException;
import org.eclipse.sensinact.gateway.datastore.api.UnableToFindDataStoreException;
import org.eclipse.sensinact.gateway.datastore.sqlite.internal.SQLiteDataStoreService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class TestDataBaseService {
    private static final String FAKE_DATABASE_PATH = "target/test-resources/fake.db";
    private static final String TEST_DATABASE_PATH = "target/test-resources/sample.db";

    private static final String MOCK_BUNDLE_NAME = "MockedBundle";
    private static final long MOCK_BUNDLE_ID = 1;
    private BundleContext context = null;
    private Bundle bundle = null;
    private ServiceReference reference = null;

    private SQLiteDataStoreService dataService;
    private Mediator mediator;

    @Before
    public void init() throws Exception {
        context = Mockito.mock(BundleContext.class);
        bundle = Mockito.mock(Bundle.class);
        reference = Mockito.mock(ServiceReference.class);

        Mockito.when(bundle.getSymbolicName()).thenReturn(MOCK_BUNDLE_NAME);
        Mockito.when(bundle.getBundleId()).thenReturn(MOCK_BUNDLE_ID);

        Mockito.when(reference.getBundle()).thenReturn(bundle);
        Mockito.when(context.getBundle()).thenReturn(bundle);

        mediator = createMediator();

        Mockito.when(mediator.getContext()).thenReturn(context);
        dataService = new SQLiteDataStoreService(mediator, TEST_DATABASE_PATH);
        assertNotNull(dataService);

    }

    @Test(expected = UnableToFindDataStoreException.class)
    public void testOpenConnectionFail() throws DataStoreException, UnableToConnectToDataStoreException, UnableToFindDataStoreException {
        dataService = new SQLiteDataStoreService(mediator, FAKE_DATABASE_PATH);
        fail("No Exception has been thrown");
    }

    @Test
    public void testDataServiceConsultationQuery() throws UnableToConnectToDataStoreException, UnableToFindDataStoreException, DataStoreException {
        JSONArray json = dataService.select("SELECT * FROM person WHERE person.id=1");
        assertEquals(json.getJSONObject(0).getInt("id"), 1);
        assertEquals(json.getJSONObject(0).getString("name"), "leo");
    }

    @Test
    public void testDataServiceDeletionQuery() throws UnableToConnectToDataStoreException, UnableToFindDataStoreException, DataStoreException, InvalidSyntaxException {
        int entries = dataService.delete("DELETE FROM person WHERE person.id=2");
        assertEquals(1, entries);
        entries = (int) dataService.insert("INSERT INTO person VALUES (2,'michel')");
    }

    @Test
    public void testDataServiceInsertionQuery() throws UnableToConnectToDataStoreException, UnableToFindDataStoreException, DataStoreException {
        dataService.delete("DELETE FROM person WHERE person.id=10");

        int entries = (int) dataService.insert("INSERT INTO person VALUES (10,'robert') ");
        JSONArray json = dataService.select("SELECT * FROM person WHERE person.id=10");
        assertEquals(json.getJSONObject(0).getInt("id"), 10);
        assertEquals(json.getJSONObject(0).getString("name"), "robert");
        entries = dataService.delete("DELETE FROM person WHERE person.id=10");
        assertEquals(1, entries);
    }

    @Test
    public void testDataServiceAutoIncInsertionQuery() throws UnableToConnectToDataStoreException, UnableToFindDataStoreException, DataStoreException {
        long previous = dataService.insert("INSERT INTO autoperson VALUES (NULL,'autorobert') ");
        long entry = dataService.insert("INSERT INTO autoperson VALUES (NULL,'autobernard') ");

        assertEquals(1, entry - previous);
        int count = dataService.delete("DELETE FROM autoperson WHERE autoperson.AUTOID=" + previous);
        count = dataService.delete("DELETE FROM autoperson WHERE autoperson.AUTOID=" + entry);
    }

    @Test
    public void testRecursive() throws DataStoreException {
        JSONObject object = dataService.select("WITH RECURSIVE t(n) AS ( VALUES (1)  UNION ALL  SELECT n+1 FROM t WHERE n < 100)" + " SELECT sum(n) AS TOTAL FROM t;").optJSONObject(0);

        assertEquals(5050, object.optInt("TOTAL"));
    }

    protected static Mediator createMediator() {
        Mediator mediator = Mockito.mock(Mediator.class);

        Mockito.doAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                String info = (String) invocation.getArguments()[0];
                System.out.println(info);
                return null;
            }

        }).when(mediator).info(Mockito.anyString());
        Mockito.doAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                String warn = (String) invocation.getArguments()[0];
                System.out.println(warn);
                return null;
            }

        }).when(mediator).warn(Mockito.anyString());
        Mockito.doAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                String debug = (String) invocation.getArguments()[0];
                System.out.println(debug);
                return null;
            }

        }).when(mediator).debug(Mockito.anyString());
        Mockito.doAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                String error = (String) invocation.getArguments()[0];
                System.out.println(error);
                return null;
            }

        }).when(mediator).error(Mockito.anyString());
        Mockito.doAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                String error = (String) invocation.getArguments()[0];
                Throwable exception = (Throwable) invocation.getArguments()[1];
                System.out.println(error);
                exception.printStackTrace();
                return null;
            }

        }).when(mediator).error(Mockito.anyString(), Mockito.any(Exception.class));

        return mediator;

    }
}
