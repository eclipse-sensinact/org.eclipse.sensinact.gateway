/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/

package org.eclipse.sensinact.gateway.datastore.sqlite.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.eclipse.sensinact.gateway.core.security.SecurityDataStoreService;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;
import org.eclipse.sensinact.gateway.datastore.api.UnableToConnectToDataStoreException;
import org.eclipse.sensinact.gateway.datastore.api.UnableToFindDataStoreException;
import org.eclipse.sensinact.gateway.datastore.sqlite.SQLiteDataStoreService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.dictionary.Dictionaries;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

@RequireConfigurationAdmin
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
public class TestDataBaseService {


	@InjectBundleContext BundleContext bc;
	@InjectService(cardinality = 1, timeout = 500)
	ConfigurationAdmin ca;
	@InjectService(cardinality = 0,service = SecurityDataStoreService.class)
	ServiceAware<SecurityDataStoreService> dataServiceAware;

	@Test
	public void testOpenConnectionFail() throws IOException, InterruptedException {

		Assertions.assertThat(dataServiceAware.getServices()).isEmpty();

		Configuration cfg = ca.getConfiguration(SQLiteDataStoreService.PID, "?");
		cfg.update(Dictionaries.dictionaryOf("database", FAKE_DATABASE_PATH()));

		Thread.sleep(1000);
		Assertions.assertThat(dataServiceAware.getServices()).isEmpty();
		cfg.delete();
	}

	private String FAKE_DATABASE_PATH() {
		return bc.getProperty("org.eclipse.sensinact.datastore.folder")+"/fake.db";
	}
	private String TEST_DATABASE_PATH() {
		return bc.getProperty("org.eclipse.sensinact.datastore.folder")+"/sample.db";
	}

	@Test
	public void testDataServiceConsultationQuery() throws DataStoreException, IOException, InterruptedException {
		Configuration cfg = ca.getConfiguration(SQLiteDataStoreService.PID, "?");
		cfg.update(Dictionaries.dictionaryOf("database", TEST_DATABASE_PATH()));

		JSONArray json = dataServiceAware.waitForService(1000).select("SELECT * FROM person WHERE person.id=1");
		assertEquals(json.getJSONObject(0).getInt("id"), 1);
		assertEquals(json.getJSONObject(0).getString("name"), "leo");
		cfg.delete();
	}

	@Test
	public void testDataServiceDeletionQuery()
			throws UnableToConnectToDataStoreException, UnableToFindDataStoreException, DataStoreException,
			InvalidSyntaxException, IOException, InterruptedException {

		Configuration cfg = ca.getConfiguration(SQLiteDataStoreService.PID, "?");
		cfg.update(Dictionaries.dictionaryOf("database", TEST_DATABASE_PATH()));

		int entries = dataServiceAware.waitForService(1000).delete("DELETE FROM person WHERE person.id=2");
		assertEquals(1, entries);
		entries = (int) dataServiceAware.getService().insert("INSERT INTO person VALUES (2,'michel')");
	}

	@Test
	public void testDataServiceInsertionQuery() throws UnableToConnectToDataStoreException,
			UnableToFindDataStoreException, DataStoreException, IOException, InterruptedException {
		Configuration cfg = ca.getConfiguration(SQLiteDataStoreService.PID, "?");
		cfg.update(Dictionaries.dictionaryOf("database", TEST_DATABASE_PATH()));

		dataServiceAware.waitForService(1000).delete("DELETE FROM person WHERE person.id=10");

		int entries = (int) dataServiceAware.getService().insert("INSERT INTO person VALUES (10,'robert') ");
		JSONArray json = dataServiceAware.getService().select("SELECT * FROM person WHERE person.id=10");
		assertEquals(json.getJSONObject(0).getInt("id"), 10);
		assertEquals(json.getJSONObject(0).getString("name"), "robert");
		entries = dataServiceAware.getService().delete("DELETE FROM person WHERE person.id=10");
		assertEquals(1, entries);
		cfg.delete();
	}

	@Test
	public void testDataServiceAutoIncInsertionQuery() throws UnableToConnectToDataStoreException,
			UnableToFindDataStoreException, DataStoreException, IOException, InterruptedException {

		Configuration cfg = ca.getConfiguration(SQLiteDataStoreService.PID, "?");
		cfg.update(Dictionaries.dictionaryOf("database", TEST_DATABASE_PATH()));

		long previous = dataServiceAware.waitForService(1000)
				.insert("INSERT INTO autoperson VALUES (NULL,'autorobert') ");
		long entry = dataServiceAware.getService().insert("INSERT INTO autoperson VALUES (NULL,'autobernard') ");

		assertEquals(1, entry - previous);
		int count = dataServiceAware.getService().delete("DELETE FROM autoperson WHERE autoperson.AUTOID=" + previous);
		count = dataServiceAware.getService().delete("DELETE FROM autoperson WHERE autoperson.AUTOID=" + entry);
		cfg.delete();
	}

	@Test
	public void testRecursive() throws DataStoreException, IOException, InterruptedException {

		Configuration cfg = ca.getConfiguration(SQLiteDataStoreService.PID, "?");
		cfg.update(Dictionaries.dictionaryOf("database", TEST_DATABASE_PATH()));

		JSONObject object = dataServiceAware.waitForService(1000)
				.select("WITH RECURSIVE t(n) AS ( VALUES (1)  UNION ALL  SELECT n+1 FROM t WHERE n < 100)"
						+ " SELECT sum(n) AS TOTAL FROM t;")
				.optJSONObject(0);

		assertEquals(5050, object.optInt("TOTAL"));
		cfg.delete();
	}

}
