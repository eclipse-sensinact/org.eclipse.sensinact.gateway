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
/**
 * 
 */
package org.eclipse.sensinact.gateway.datastore.sqlite;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.sensinact.gateway.util.IOUtils;

/**
 * @author christophe
 *
 */
public final class SQLiteDataStoreFactory {

	private static final Logger LOG = Logger.getLogger(SQLiteDataStoreFactory.class.getName());
	private File dbFile;
	private File script;

	/**
	 * @throws Exception
	 * 
	 */
	public SQLiteDataStoreFactory() throws Exception {
		this(null, null);
	}

	/**
	 * @throws Exception
	 * 
	 */
	public SQLiteDataStoreFactory(File dbFile) throws Exception {
		this(dbFile, null);
	}

	/**
	 * @throws Exception
	 * 
	 */
	public SQLiteDataStoreFactory(File dbFile, File script) throws Exception {
		try {
			Class.forName("org.sqlite.JDBC");

		} catch (ClassNotFoundException e) {
			LOG.log(Level.SEVERE, e.getMessage(), e);
			return;
		}
		if (dbFile == null || (!(dbFile.getParentFile().getAbsoluteFile()).exists())) {
			return;
		}
		this.dbFile = dbFile;
		this.script = script;

	}

	public void newInstance() throws Exception {
		if (this.dbFile == null) {
			return;
		}
		Connection connection = null;
		try {
			String connectionString = new StringBuilder("jdbc:sqlite:").append(dbFile.getAbsolutePath()).toString();

			connection = DriverManager.getConnection(connectionString);

			connection.setAutoCommit(true);

		} catch (SQLException e) {
			LOG.log(Level.SEVERE, e.getMessage(), e);
			return;
		}
		String[] sqlCommands = this.parse(script);
		this.populate(connection, sqlCommands);
	}

	private void populate(Connection connection, String[] sqlCommands) {
		int index = 0;
		int length = sqlCommands == null ? 0 : sqlCommands.length;

		for (; index < length; index++) {
			executeStatement(connection, sqlCommands[index]);
		}
	}

	/**
	 * @param executor
	 * @return
	 */
	protected void executeStatement(Connection connection, String query) {
		Statement statement = null;
		try {
			// System.out.println("------------------------------------");
			// System.out.println(query);
			statement = connection.createStatement();
			statement.setQueryTimeout(30);
			statement.executeUpdate(query);
			// System.out.println("DONE");
			// System.out.println("------------------------------------");

		} catch (Exception e) {
			// System.out.println("ERROR");
			// System.out.println("------------------------------------");
			LOG.log(Level.SEVERE, e.getMessage(), e);

		} finally {
			if (statement != null) {
				try {
					statement.close();

				} catch (SQLException e) {
					LOG.log(Level.SEVERE, "Error while closing statement", e);
				}
			}
		}
	}

	private String[] parse(File script) throws Exception {
		if (script == null || (!script.exists())) {
			return new String[0];
		}
		byte[] scriptBytesArray = IOUtils.read(new FileInputStream(script), true);

		List<String> sqlCommands = new ArrayList<String>();
		sqlCommands.add(new String(scriptBytesArray));

		// StringTokenizer tokenizer = new StringTokenizer(
		// new String(scriptBytesArray), ";");
		//
		// while(tokenizer.hasMoreTokens())
		// {
		// sqlCommands.add(tokenizer.nextToken());
		// }
		return sqlCommands.toArray(new String[sqlCommands.size()]);
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		if (args == null || args.length < 1) {
			LOG.log(Level.SEVERE, "database file and sql script paths expected");
			return;
		}
		File dbFile = new File(args[0]).getAbsoluteFile();
		File scriptFile = null;

		if (args.length > 1) {
			scriptFile = new File(args[1]).getAbsoluteFile();
		}
		SQLiteDataStoreFactory factory = new SQLiteDataStoreFactory(dbFile, scriptFile);
		factory.newInstance();
	}

}
