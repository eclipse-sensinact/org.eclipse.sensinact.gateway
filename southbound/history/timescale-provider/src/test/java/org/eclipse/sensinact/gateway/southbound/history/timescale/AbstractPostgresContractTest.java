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
package org.eclipse.sensinact.gateway.southbound.history.timescale;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Callable;

import org.eclipse.sensinact.gateway.southbound.history.core.contract.HistoryStorageContractTest;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoryStorage;
import org.eclipse.sensinact.gateway.southbound.history.timescale.TimescaleHistoryStorage.TxRunner;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Runs the storage contract against the unified schema. The transaction
 * seam commits after each operation on a plain JDBC connection; production
 * uses tx-control instead.
 */
abstract class AbstractPostgresContractTest extends HistoryStorageContractTest {

    protected abstract PostgreSQLContainer container();

    private Connection connection;

    @Override
    protected HistoryStorage createStorage() throws Exception {
        connection = DriverManager.getConnection(container().getJdbcUrl(), container().getUsername(),
                container().getPassword());
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP SCHEMA IF EXISTS sensinact CASCADE");
        }
        TxRunner committing = new TxRunner() {
            @Override
            public <T> T inTransaction(Callable<T> operation) {
                try {
                    T result = operation.call();
                    connection.commit();
                    return result;
                } catch (Exception e) {
                    try {
                        connection.rollback();
                    } catch (SQLException rollbackFailure) {
                        e.addSuppressed(rollbackFailure);
                    }
                    throw e instanceof RuntimeException re ? re : new IllegalStateException(e);
                }
            }
        };
        connection.setAutoCommit(false);
        TimescaleHistoryStorage storage = new TimescaleHistoryStorage(committing, () -> connection, 10_000);
        storage.initialize();
        return storage;
    }

    @Override
    protected void destroyStorage(HistoryStorage storage) throws Exception {
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }
}
