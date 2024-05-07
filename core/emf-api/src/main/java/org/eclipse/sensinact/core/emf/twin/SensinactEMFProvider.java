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

import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.model.core.provider.Provider;
import org.osgi.util.promise.Promise;

/**
 * This is the digital twin of a single provider instance, including EMF access
 */
public interface SensinactEMFProvider extends SensinactProvider {

    /**
     * Get the list of linked provider instances
     *
     * @return an immutable list of linked providers
     */
    List<? extends SensinactEMFProvider> getLinkedProviders();

    /**
     * Updates a complete Provider
     *
     * @return
     */
    Promise<Void> update(Provider newVersion);

    /**
     * The digital twins for the services for this provider
     *
     * @return
     */
    Map<String, ? extends SensinactEMFService> getServices();

    /**
     * The digital twins for the service with the given name for this provider A new
     * Instance will be create if non exists.
     *
     * @return
     */
    SensinactEMFService getOrCreateService(String name, EClass serviceEClass);
}
