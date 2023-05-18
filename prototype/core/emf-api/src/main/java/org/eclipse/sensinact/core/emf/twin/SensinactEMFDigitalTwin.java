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
package org.eclipse.sensinact.core.emf.twin;

import java.time.Instant;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.sensinact.prototype.twin.SensinactDigitalTwin;

/**
 * The {@link SensinactEMFDigitalTwin} provides access to the in-memory digital
 * twin including the EMF model for the data
 */
public interface SensinactEMFDigitalTwin extends SensinactDigitalTwin {

    /**
     * List all the providers in the runtime
     *
     * @return
     */
    List<? extends SensinactEMFProvider> getProviders();

    /**
     * List all providers for the named model
     *
     * @param model
     * @return
     */
    List<? extends SensinactEMFProvider> getProviders(String model);

    /**
     * Get a provider by name
     *
     * @param providerName
     * @return
     */
    SensinactEMFProvider getProvider(String providerName);

    /**
     * Get a provider by name and model
     *
     * @param model
     * @param providerName
     * @return
     */
    SensinactEMFProvider getProvider(String model, String providerName);

    /**
     * Create a provider instance for the named model
     *
     * @param model
     * @param providerName
     * @return
     */
    SensinactEMFProvider createProvider(String model, String providerName);

    /**
     * Get a provider by name and its concrete model class
     *
     * @param model
     * @param providerName
     * @return
     */
    SensinactEMFProvider getProvider(EClass model, String id);

    /**
     * Create a provider instance for the named model
     *
     * @param model
     * @param providerName
     * @return
     */
    SensinactEMFProvider createProvider(String model, String providerName, Instant created);

}
