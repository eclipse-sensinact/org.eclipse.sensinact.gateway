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
*   Data In Motion - initial API and implementation
**********************************************************************/
package org.eclipse.sensinact.gateway.southbound.history.spikes.postgres;

import java.sql.DriverManager;

import org.eclipse.sensinact.gateway.southbound.history.spikes.HistoryStorageContractTest;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.HistoryStorage;
import org.junit.jupiter.api.condition.EnabledIf;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@EnabledIf("dockerAvailable")
class PostgresUnifiedHistoryStorageTest extends HistoryStorageContractTest {

    static boolean dockerAvailable() {
        return DockerClientFactory.instance().isDockerAvailable();
    }

    @Container
    static final PostgreSQLContainer container = new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));

    @Override
    protected HistoryStorage createStorage() throws Exception {
        return new PostgresUnifiedHistoryStorage(DriverManager.getConnection(container.getJdbcUrl(),
                container.getUsername(), container.getPassword()), false);
    }
}
