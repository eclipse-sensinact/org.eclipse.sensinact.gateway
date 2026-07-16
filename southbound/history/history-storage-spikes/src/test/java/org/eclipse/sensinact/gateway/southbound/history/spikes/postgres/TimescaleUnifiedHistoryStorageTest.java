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

/**
 * Same unified schema on the Apache-2 TimescaleDB image (deliberately not
 * timescaledb-ha, which is Timescale License) with hypertable + time_bucket.
 */
@Testcontainers
@EnabledIf("dockerAvailable")
class TimescaleUnifiedHistoryStorageTest extends HistoryStorageContractTest {

    static boolean dockerAvailable() {
        return DockerClientFactory.instance().isDockerAvailable();
    }

    @Container
    static final PostgreSQLContainer container = new PostgreSQLContainer(
            DockerImageName.parse("timescale/timescaledb:latest-pg16").asCompatibleSubstituteFor("postgres"));

    @Override
    protected HistoryStorage createStorage() throws Exception {
        return new PostgresUnifiedHistoryStorage(DriverManager.getConnection(container.getJdbcUrl(),
                container.getUsername(), container.getPassword()), true);
    }
}
