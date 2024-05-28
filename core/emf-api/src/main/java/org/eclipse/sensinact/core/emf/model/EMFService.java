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

import org.eclipse.emf.ecore.EClass;
import org.eclipse.sensinact.core.model.Service;

/**
 * The model for a Service
 */
public interface EMFService extends Service {

    EMFModel getModel();

    EClass getServiceEClass();

}
