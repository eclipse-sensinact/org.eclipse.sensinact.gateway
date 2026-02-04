/*********************************************************************
* Copyright (c) 2026 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.gateway.test.testcontainers.postgres.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.abort;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.postgresql.Driver;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class PostgresContainerTest {

    @Test
    void startContainer() throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(DockerClientFactory.class.getClassLoader());
        try {
            try {
                DockerClientFactory.lazyClient().versionCmd().exec();
            } catch (Throwable t) {
                abort("No docker executable on the path, so tests will be skipped");
            }
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }

        try(PostgreSQLContainer container = new PostgreSQLContainer(
                DockerImageName.parse("postgres").withTag("15.15-alpine3.22"))) {
            container.start();
            Properties props = new Properties();
            props.put("user", container.getUsername());
            props.put("password", container.getPassword());
            try(Connection conn = new Driver().connect(container.getJdbcUrl(), props)) {
                ResultSet result = conn.createStatement().executeQuery("select 1");
                assertTrue(result.next());
                assertEquals(1, result.getInt(1));
            }
        }
    }
}

