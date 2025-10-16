/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.core.model;

import java.time.Instant;

/**
 * A builder for programmatically registering models
 */
public interface ModelBuilder {

    ModelBuilder exclusivelyOwned(boolean exclusive);

    ModelBuilder withAutoDeletion(boolean autoDelete);

    ModelBuilder withCreationTime(Instant creationTime);

    ServiceBuilder<ModelBuilder> withService(String name);

    ServiceBuilder<ModelBuilder> withService(String name, String serviceModelName);

    Model build();
}
