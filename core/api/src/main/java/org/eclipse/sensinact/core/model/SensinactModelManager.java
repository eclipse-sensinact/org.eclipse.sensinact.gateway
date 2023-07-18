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

import java.io.InputStream;
import java.util.Map;

import org.eclipse.sensinact.core.command.CommandScoped;

/**
 * The sensiNact interface used to create and discover the models
 */
public interface SensinactModelManager extends CommandScoped {

    ModelBuilder createModel(String model);

    Model getModel(String model);

    void deleteModel(String model);

    void registerModel(String model);

    void registerModel(InputStream model);

    Map<String, Model> getModels();

}
