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
package org.eclipse.sensinact.core.emf.model;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.sensinact.prototype.model.Model;
import org.eclipse.sensinact.prototype.model.ModelBuilder;
import org.eclipse.sensinact.prototype.model.SensinactModelManager;

/**
 * The sensiNact interface used to create and discover the models using EMF
 */
public interface SensinactEMFModelManager extends SensinactModelManager {

    Model getModel(EClass eClass);

    ModelBuilder createModel(EClass eClass);

}
