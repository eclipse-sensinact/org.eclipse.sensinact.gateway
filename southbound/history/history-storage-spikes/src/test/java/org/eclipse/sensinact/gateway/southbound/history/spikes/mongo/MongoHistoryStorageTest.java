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
package org.eclipse.sensinact.gateway.southbound.history.spikes.mongo;

import org.eclipse.sensinact.gateway.southbound.history.spikes.HistoryStorageContractTest;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.HistoryStorage;
import org.junit.jupiter.api.condition.EnabledIf;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import com.mongodb.client.MongoClients;

@Testcontainers
@EnabledIf("dockerAvailable")
class MongoHistoryStorageTest extends HistoryStorageContractTest {

    static boolean dockerAvailable() {
        return DockerClientFactory.instance().isDockerAvailable();
    }

    @Container
    static final MongoDBContainer container = new MongoDBContainer(DockerImageName.parse("mongo:7.0"));

    @Override
    protected HistoryStorage createStorage() {
        return new MongoHistoryStorage(MongoClients.create(container.getConnectionString()), "sensinact_spike");
    }
}
