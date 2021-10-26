/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */

package org.eclipse.sensinact.gateway.core.security.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.eclipse.sensinact.gateway.core.InvalidServiceProviderException;
import org.eclipse.sensinact.gateway.core.security.dao.AuthenticatedDAO;
import org.eclipse.sensinact.gateway.core.security.dao.BundleDAO;
import org.eclipse.sensinact.gateway.core.security.dao.DAOException;
import org.eclipse.sensinact.gateway.core.security.dao.ObjectAccessDAO;
import org.eclipse.sensinact.gateway.core.security.dao.ObjectDAO;
import org.eclipse.sensinact.gateway.core.security.dao.UserDAO;
import org.eclipse.sensinact.gateway.core.security.entity.AuthenticatedEntity;
import org.eclipse.sensinact.gateway.core.security.entity.BundleEntity;
import org.eclipse.sensinact.gateway.core.security.entity.ObjectAccessEntity;
import org.eclipse.sensinact.gateway.core.security.entity.ObjectEntity;
import org.eclipse.sensinact.gateway.core.security.entity.UserEntity;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreService;
import org.eclipse.sensinact.gateway.datastore.api.UnableToConnectToDataStoreException;
import org.eclipse.sensinact.gateway.datastore.api.UnableToFindDataStoreException;
import org.eclipse.sensinact.gateway.datastore.sqlite.SQLiteDataStoreService;
import org.eclipse.sensinact.gateway.datastore.sqlite.SQLiteDataStoreService.SQLiteConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

/**
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class TestDAO {
	// ********************************************************************//
	// NESTED DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// ABSTRACT DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// STATIC DECLARATIONS //
	// ********************************************************************//

	private static final String LOG_FILTER = "(" + Constants.OBJECTCLASS + "=" + LogService.class.getCanonicalName()
			+ ")";

	private static final String DATA_STORE_FILTER = "(" + Constants.OBJECTCLASS + "="
			+ DataStoreService.class.getCanonicalName() + ")";

	private static final String MOCK_BUNDLE_NAME = "MockedBundle";
	private static final long MOCK_BUNDLE_ID = 1;

	// ********************************************************************//
	// INSTANCE DECLARATIONS //
	// ********************************************************************//

	private final Filter filterDataStore = Mockito.mock(Filter.class);

	private final BundleContext context = Mockito.mock(BundleContext.class);
	private final Bundle bundle = Mockito.mock(Bundle.class);

	private final ServiceReference referenceDataStoreService = Mockito.mock(ServiceReference.class);

	private SQLiteDataStoreService dataStoreService;

	private File tempDB;

	@BeforeEach
	public void init() throws InvalidServiceProviderException, UnableToFindDataStoreException,
			UnableToConnectToDataStoreException, InvalidSyntaxException, IOException {
		Filter filter = Mockito.mock(Filter.class);
		Mockito.when(filter.toString()).thenReturn(LOG_FILTER);

		Mockito.when(context.createFilter(LOG_FILTER)).thenReturn(filter);
		Mockito.when(context.getServiceReferences((String) Mockito.eq(null), Mockito.eq(LOG_FILTER))).thenReturn(null);
		Mockito.when(context.getServiceReference(LOG_FILTER)).thenReturn(null);

		Mockito.when(context.createFilter(DATA_STORE_FILTER)).thenReturn(filterDataStore);
		Mockito.when(filterDataStore.toString()).thenReturn(DATA_STORE_FILTER);

		Mockito.when(context.getServiceReferences(Mockito.anyString(), Mockito.anyString()))
				.then(new Answer<ServiceReference[]>() {
					@Override
					public ServiceReference[] answer(InvocationOnMock invocation) throws Throwable {
						Object[] arguments = invocation.getArguments();
						if (arguments == null || arguments.length != 2) {
							return null;
						}
						if (arguments[0] != null && arguments[0].equals(DataStoreService.class.getCanonicalName())) {
							return new ServiceReference[] { referenceDataStoreService };

						}
						return null;
					}
				});
		Mockito.when(context.getServiceReferences(Mockito.any(Class.class), Mockito.anyString()))
				.then(new Answer<Collection<ServiceReference>>() {
					@Override
					public Collection<ServiceReference> answer(InvocationOnMock invocation) throws Throwable {
						Object[] arguments = invocation.getArguments();
						if (arguments == null || arguments.length != 2) {
							return null;
						}
						if (arguments[0] != null && arguments[0].equals(DataStoreService.class)) {
							return Collections.singleton(referenceDataStoreService);
						}
						return Collections.<ServiceReference>emptyList();
					}
				});
		Mockito.when(context.getService(Mockito.any(ServiceReference.class))).then(new Answer<Object>() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				Object[] arguments = invocation.getArguments();
				if (arguments == null || arguments.length != 1) {
					return null;
				} else if (arguments[0] == referenceDataStoreService) {
					return dataStoreService;
				}
				return null;
			}
		});

		Mockito.when(context.getBundle()).thenReturn(bundle);
		Mockito.when(bundle.getSymbolicName()).thenReturn(MOCK_BUNDLE_NAME);
		Mockito.when(bundle.getBundleId()).thenReturn(MOCK_BUNDLE_ID);
		Mockito.when(bundle.getState()).thenReturn(Bundle.ACTIVE);

		Mockito.when(bundle.getResource(Mockito.anyString())).thenAnswer(new Answer<URL>() {
			@Override
			public URL answer(InvocationOnMock invocation) throws Throwable {
				Object[] arguments = invocation.getArguments();
				if (arguments == null || arguments.length != 1) {
					return null;
				} else if (arguments[0].equals("script/getObjectFromPath.sql")) {
					return new File("src/main/resources/script/getObjectFromPath.sql").getAbsoluteFile().toURI()
							.toURL();
				} else if (arguments[0].equals("script/getMethodAccessibilities.sql")) {
					return new File("src/main/resources/script/getMethodAccessibilities.sql").getAbsoluteFile().toURI()
							.toURL();
				}
				return null;
			}
		});
	
		tempDB = File.createTempFile("test", ".sqlite");
		tempDB.deleteOnExit();
		
		String dbPath = FrameworkUtil.getBundle(getClass()).getBundleContext().getProperty("sqlitedb");
		Path copied = tempDB.toPath();
	    Path originalPath = Paths.get(dbPath);
	    System.err.println("orignal - " + originalPath.toFile().getAbsolutePath());
	    Files.copy(originalPath, copied, StandardCopyOption.REPLACE_EXISTING);
	    SQLiteConfig sqlLiteConfig = Mockito.mock(SQLiteDataStoreService.SQLiteConfig.class);
		Mockito.when(sqlLiteConfig.database()).thenReturn(tempDB.getAbsolutePath());
		dataStoreService = new SQLiteDataStoreService();
		dataStoreService.start(sqlLiteConfig);
	}
	

	@AfterEach
	public void after(){
		tempDB.delete();
	}

	@Test
	public void testSelectUserDAO() throws DAOException, DataStoreException {
		UserDAO userDAO = new UserDAO(dataStoreService);
		List<UserEntity> userEntities = userDAO.select();
		int index = 0;

		String[][] users = new String[][] {
				new String[] { "anonymous", "ANONYMOUS", "294de3557d9d0b3d2d8a1e6aab028cf", "anonymous" },
				new String[] { "christophe.munilla@cea.fr", "cea", "ac0dad7d2c39119f9d7d76d34303ec85",
						"73c5f1a1e7b4a75c2b5fabafca2cf51e6b0e7426" },
				new String[] { "fake@cea.fr", "fake", "144c9defac04969c7bfad8efaa8ea194",
						"f92fe92b61e018be14a88ab84f2859c35d832316" },
				new String[] { "fake2@cea.fr", "fake2", "503f7fada600da935e2851a1c7326084",
						"31e63e9c4a319bf313b8a6d454798e09b3e7344a" } };
		for (UserEntity userEntity : userEntities) {
			assertTrue(index == (int) userEntity.getIdentifier());
			String[] user = users[index++];
			assertTrue(userEntity.getAccount().equals(user[0]));
			assertTrue(userEntity.getLogin().equals(user[1]));
			assertTrue(userEntity.getPassword().equals(user[2]));
			assertTrue(userEntity.getPublicKey().equals(user[3]));
		}
	}

	@Test
	@Disabled
	public void testSelectObjectDAO() throws DAOException, DataStoreException {
		int index = 0;
		ObjectDAO objectDAO = new ObjectDAO(dataStoreService);

		Object[][] objects = new Object[][] { new Object[] { 6, 5, "/slider/cursor/position", 3, "position", 1 },
				new Object[] { 4, 3, "/slider/[^/]+/location", 3, "location", 1 },
				new Object[] { 5, 2, "/slider/cursor", 3, "cursor", 1 } };
		String[] paths = new String[] { "/slider/cursor/position", "/slider/cursor/location", "/slider/cursor/fake" };
		for (; index < 3; index++) {
			String path = paths[index];
			Object[] object = objects[index];

			List<ObjectEntity> objectEntities = objectDAO.find(path);
			ObjectEntity objectEntity = objectEntities.get(0);
			assertEquals(((Integer) object[0]).longValue(), objectEntity.getIdentifier());
			assertEquals(((Integer) object[1]).longValue(), objectEntity.getParent());
			assertEquals(object[2], objectEntity.getPath());
			assertEquals(((Integer) object[3]).longValue(), objectEntity.getBundleEntity());
			assertEquals(object[4], objectEntity.getName());
			assertEquals(((Integer) object[5]).longValue(), objectEntity.getObjectProfileEntity());
		}
	}

	@Test
	public void testSelectExactObjectDAO() throws DAOException, DataStoreException {
		ObjectDAO objectDAO = new ObjectDAO(dataStoreService);
		String[] paths = new String[] { "/slider/cursor/position", "/slider/cursor/location", "/slider/cursor/fake" };
		String[] resultingPaths = new String[] { "/slider/cursor/position", "/slider/[^/]+/location"
				// this last one will not be used
				, "/slider/cursor" };
		int index = 0;
		for (; index < 3; index++) {
			String path = paths[index];
			String resultingPath = resultingPaths[index];

			List<ObjectEntity> objectEntities = objectDAO.find(path, true);
			ObjectEntity objectEntity = objectEntities.isEmpty() ? null : objectEntities.get(0);

			switch (index) {
			case 0:
				;
			case 1:
				;
				assertTrue(objectEntity.getPath().equals(resultingPath));
				break;
			case 2:
				assertTrue(objectEntity == null);
				break;
			}
		}
	}

	@Test
	public void testSelectObjectChildren() throws DAOException, DataStoreException {
		ObjectDAO objectDAO = new ObjectDAO(dataStoreService);
		List<ObjectEntity> list = objectDAO.findChildren(2l);

		assertTrue(list.size() == 2);
		assertTrue(list.get(0).getIdentifier() == 3l);
		assertTrue(list.get(1).getIdentifier() == 5l);
	}

	@Test
	public void testFakePredefinedSelectStatement() throws DAOException, DataStoreException, MalformedURLException {

		URL url=FrameworkUtil.getBundle(TestDAO.class).getResource("script/fake.sql");
		ObjectDAO objectDAO = new ObjectDAO(dataStoreService,url);
		List<ObjectEntity> list = objectDAO.select("getObjectFromPath", "/slider/cursor/position");
		assertEquals(15, list.size());
	}

	@Test
	public void testImmutableCreate() throws DAOException {
		ObjectAccessDAO objectAccessDAO = new ObjectAccessDAO(dataStoreService);
		ObjectAccessEntity entity = new ObjectAccessEntity("FAKE", 12);
		
		assertThrows(DAOException.class,()->{
			
		objectAccessDAO.create(entity);
		});
	}

	@Test
	public void testImmutableDelete() throws DAOException, DataStoreException {
		ObjectAccessDAO objectAccessDAO = new ObjectAccessDAO(dataStoreService);
		ObjectAccessEntity entity = objectAccessDAO.find(1l);
		assertThrows(DAOException.class,()->{

		objectAccessDAO.delete(entity);
		});	}

	@Test
	public void testImmutableUpdate() throws DAOException, DataStoreException {
		ObjectAccessDAO objectAccessDAO = new ObjectAccessDAO(dataStoreService);
		ObjectAccessEntity entity = objectAccessDAO.find(1l);
		entity.setLevel(10);
		assertThrows(DAOException.class,()->{
		objectAccessDAO.update(entity);
		});	}

	@Test
	public void testImmutableSelect() throws DAOException, DataStoreException {
		ObjectAccessDAO objectAccessDAO = new ObjectAccessDAO(dataStoreService);
		ObjectAccessEntity entity = objectAccessDAO.find(1l);
		assertTrue(1l == entity.getIdentifier());
		assertTrue("Anonymous".equals(entity.getName()));
		assertTrue(1 == entity.getLevel());
	}

	@Test
	public void testMutable() throws DAOException, DataStoreException {
		ObjectDAO objectDAO = new ObjectDAO(dataStoreService);
		ObjectEntity entity = new ObjectEntity(2l, 2l, "admin", 0, 1, 2, null);
		objectDAO.create(entity);

		long identifier = entity.getIdentifier();
		assertTrue(identifier > 0l);

		// BE CAREFUL THE PATH HAS CHANGED
		// USING THE PREVIOUSLY DEFINED ENTITY MAY
		// OVERWRITE THE CALCULATED PATH TO NULL

		// TODO : avoid the use of the Constructor but use a static
		// method instead returning the entity created in the datastore
		entity = objectDAO.select(entity.getKeys());

		assertTrue(identifier == entity.getIdentifier());
		assertTrue("admin".equals(entity.getName()));

		entity.setName("administration");
		objectDAO.update(entity);
		entity = objectDAO.select(entity.getKeys());

		assertTrue(identifier == entity.getIdentifier());
		assertTrue("administration".equals(entity.getName()));

		assertTrue("/slider/administration".equals(entity.getPath()));
		objectDAO.delete(entity);
	}

	@Test
	public void testDeleteParentObject() throws DAOException, DataStoreException {
		ObjectDAO objectDAO = new ObjectDAO(dataStoreService);

		ObjectEntity entity = new ObjectEntity(2l, 1l, "fake", 0, 0, 0, null);
		objectDAO.create(entity);

		final long identifier = entity.getIdentifier();
		assertTrue(identifier > 0l);

		ObjectEntity newEntity = new ObjectEntity(2l, 2l, "admin", 0, 1, identifier, null);
		objectDAO.create(newEntity);

		final long newIdentifier = newEntity.getIdentifier();
		assertTrue(newIdentifier > 0l);

		ObjectEntity otherEntity = new ObjectEntity(2l, 1l, "service", 0, 1, identifier, null);
		objectDAO.create(otherEntity);

		final long otherIdentifier = otherEntity.getIdentifier();
		assertTrue(otherIdentifier > 0l);

		ObjectEntity lastEntity = new ObjectEntity(2l, 1l, "resource", 0, 1, otherIdentifier, null);
		objectDAO.create(lastEntity);

		final long lastIdentifier = lastEntity.getIdentifier();
		assertTrue(lastIdentifier > 0l);

		objectDAO.delete(entity);
		newEntity = objectDAO.select(newEntity.getKeys());

		assertNull(newEntity);

		AuthenticatedDAO authenticatedDAO = new AuthenticatedDAO(dataStoreService);
		List<AuthenticatedEntity> entities = authenticatedDAO.select(new HashMap<String, Object>() {
			{
				this.put("OID", lastIdentifier);
			}
		});
		assertTrue(entities.isEmpty());
		entities = authenticatedDAO.select(new HashMap<String, Object>() {
			{
				this.put("OID", otherIdentifier);
			}
		});
		assertTrue(entities.isEmpty());
		entities = authenticatedDAO.select(new HashMap<String, Object>() {
			{
				this.put("OID", newIdentifier);
			}
		});
		assertTrue(entities.isEmpty());
		entities = authenticatedDAO.select(new HashMap<String, Object>() {
			{
				this.put("OID", identifier);
			}
		});
		assertTrue(entities.isEmpty());
	}

	@Test
	public void testDeleteBundle() throws DAOException, DataStoreException {
		BundleDAO bundleDAO = new BundleDAO( dataStoreService);
		BundleEntity bundleEntity = new BundleEntity("fake-bundle", "fake-bundle", 0, 1);
		bundleDAO.create(bundleEntity);

		ObjectDAO objectDAO = new ObjectDAO(dataStoreService);

		ObjectEntity entity = new ObjectEntity(bundleEntity.getIdentifier(), 1l, "fake", 0, 0, 0, null);
		objectDAO.create(entity);

		final long identifier = entity.getIdentifier();
		assertTrue(identifier > 0l);

		ObjectEntity newEntity = new ObjectEntity(2l, 2l, "admin", 0, 1, identifier, null);
		objectDAO.create(newEntity);

		final long newIdentifier = newEntity.getIdentifier();
		assertTrue(newIdentifier > 0l);

		ObjectEntity otherEntity = new ObjectEntity(2l, 1l, "service", 0, 1, identifier, null);
		objectDAO.create(otherEntity);

		final long otherIdentifier = otherEntity.getIdentifier();
		assertTrue(otherIdentifier > 0l);

		ObjectEntity lastEntity = new ObjectEntity(2l, 1l, "resource", 0, 1, otherIdentifier, null);
		objectDAO.create(lastEntity);

		final long lastIdentifier = lastEntity.getIdentifier();
		assertTrue(lastIdentifier > 0l);

		bundleDAO.delete(bundleEntity);

		AuthenticatedDAO authenticatedDAO = new AuthenticatedDAO(dataStoreService);
		List<AuthenticatedEntity> entities = authenticatedDAO.select(new HashMap<String, Object>() {
			{
				this.put("OID", lastIdentifier);
			}
		});
		assertTrue(entities.isEmpty());
		entities = authenticatedDAO.select(new HashMap<String, Object>() {
			{
				this.put("OID", otherIdentifier);
			}
		});
		assertTrue(entities.isEmpty());
		entities = authenticatedDAO.select(new HashMap<String, Object>() {
			{
				this.put("OID", newIdentifier);
			}
		});
		assertTrue(entities.isEmpty());
		entities = authenticatedDAO.select(new HashMap<String, Object>() {
			{
				this.put("OID", identifier);
			}
		});
		assertTrue(entities.isEmpty());
	}
}
