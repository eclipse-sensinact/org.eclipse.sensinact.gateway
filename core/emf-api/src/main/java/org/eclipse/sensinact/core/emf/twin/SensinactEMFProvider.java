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
    @Override
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
    @Override
    Map<String, ? extends SensinactEMFService> getServices();

    /**
     * The digital twins for the service with the given name for this provider A new
     * Instance will be create if non exists.
     * <p>
     * {@code serviceEClass} is honoured when it is a sub type of the EClass the
     * service has, which is how a service of this provider gains resources its
     * declaration does not list. An existing instance is retyped to it, keeping every
     * value and all the metadata it already carries; the resources the sub type adds
     * read as unset until they are given a value. The EClass the service already has,
     * and any super type of it, change nothing, so a caller that passes the declared
     * service type never downgrades a service that was retyped before.
     *
     * @param name          the service name
     * @param serviceEClass the EClass the service should have, may be {@code null} to
     *                      take the declared one
     * @return the digital twin of the service
     * @throws IllegalArgumentException if {@code serviceEClass} is neither a sub type
     *                                  nor a super type of the EClass the service has,
     *                                  or if this provider neither declares
     *                                  {@code name} nor is a
     *                                  {@link org.eclipse.sensinact.model.core.provider.DynamicProvider}
     */
    SensinactEMFService getOrCreateService(String name, EClass serviceEClass);

    /**
     * {@return a copy of the underlying provider}
     */
    Provider getEMFProvider();

    /**
     * {@return a copy of the underlying provider}
     *
     * @param returnType Expected result type
     * @throws ClassCastException Incompatible return type
     */
    <T extends Provider> T getEMFProvider(Class<T> returnType);
}
