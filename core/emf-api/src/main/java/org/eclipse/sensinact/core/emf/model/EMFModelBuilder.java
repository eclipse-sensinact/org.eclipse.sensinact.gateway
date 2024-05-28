/*********************************************************************
* Copyright (c) 2024 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Data In Motion - initial implementation
**********************************************************************/
package org.eclipse.sensinact.core.emf.model;

import java.time.Instant;

import org.eclipse.sensinact.core.model.ModelBuilder;

/**
 * A builder for programmatically registering emf models
 */
public interface EMFModelBuilder extends ModelBuilder {

    EMFModelBuilder exclusivelyOwned(boolean exclusive);

    EMFModelBuilder withAutoDeletion(boolean autoDelete);

    EMFModelBuilder withCreationTime(Instant creationTime);

    EMFModel build();
}
