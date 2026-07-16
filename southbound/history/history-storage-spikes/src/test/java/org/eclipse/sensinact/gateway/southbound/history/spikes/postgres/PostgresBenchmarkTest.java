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
import java.util.Properties;

import org.eclipse.sensinact.gateway.southbound.history.spikes.StorageBenchmark;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Run with: mvn test -pl southbound/history/history-storage-spikes
 * -Dtest=*BenchmarkTest -Dspike.benchmark=true
 */
@Testcontainers
@EnabledIfSystemProperty(named = "spike.benchmark", matches = "true")
class PostgresBenchmarkTest {

    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));

    @Container
    static final PostgreSQLContainer timescale = new PostgreSQLContainer(
            DockerImageName.parse("timescale/timescaledb:latest-pg16").asCompatibleSubstituteFor("postgres"));

    private static PostgresUnifiedHistoryStorage connect(PostgreSQLContainer container, boolean timescaleMode)
            throws Exception {
        Properties props = new Properties();
        props.setProperty("user", container.getUsername());
        props.setProperty("password", container.getPassword());
        props.setProperty("reWriteBatchedInserts", "true");
        return new PostgresUnifiedHistoryStorage(DriverManager.getConnection(container.getJdbcUrl(), props),
                timescaleMode);
    }

    @Test
    void benchmarkPlainPostgres() throws Exception {
        try (PostgresUnifiedHistoryStorage storage = connect(postgres, false)) {
            StorageBenchmark.run(storage, "postgres16-date_bin");
        }
    }

    @Test
    void benchmarkTimescale() throws Exception {
        try (PostgresUnifiedHistoryStorage storage = connect(timescale, true)) {
            StorageBenchmark.run(storage, "timescale-pg16-time_bucket");
        }
    }
}
