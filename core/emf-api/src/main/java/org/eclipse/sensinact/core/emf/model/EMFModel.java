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
*   Data In Motion - initial implementation
**********************************************************************/
package org.eclipse.sensinact.core.emf.model;

import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.sensinact.core.model.Model;

/**
 * A model for a Provider
 */
public interface EMFModel extends Model {

    EClass getModelEClass();

    boolean isDynamic();

    Map<String, ? extends EMFService> getServices();

    EMFServiceBuilder<EMFService> createService(String service);

    EMFServiceBuilder<EMFService> createService(String service, String serviceModelName);

    EMFService createServiceWithEClass(String svc, EClass svcEClass);
}
