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

    /**
     * Creates a service handle over the given {@link EClass} for a service name this
     * model does <em>not</em> declare.
     * <p>
     * The handle carries no change to the model itself, so it cannot retype a service
     * the model already declares - for an inherited declaration such as the admin
     * service there is nothing it could change. To give the service of one provider
     * an EClass that adds resources, use
     * {@link org.eclipse.sensinact.core.emf.twin.SensinactEMFProvider#getOrCreateService(String, EClass)}
     * with a sub type of the declared service type.
     *
     * @param svc       the service name
     * @param svcEClass the EClass of the service
     * @return a handle over {@code svcEClass}
     * @throws IllegalArgumentException if this model declares {@code svc} with a
     *                                  different EClass, or declares it as something
     *                                  that is not a service
     */
    EMFService createServiceWithEClass(String svc, EClass svcEClass);
}
