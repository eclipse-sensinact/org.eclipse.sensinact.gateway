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
package org.eclipse.sensinact.core.twin;

import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.core.command.CommandScoped;

/**
 * This is the digital twin of a single provider instance
 */
public interface SensinactProvider extends CommandScoped {

    /**
     * The provider name. Unique within the gateway
     *
     * @return
     */
    String getName();

    /**
     * The model name for this provider
     *
     * @return
     */
    String getModelName();

    /**
     * The digital twins for the services for this provider
     *
     * @return
     */
    Map<String, ? extends SensinactService> getServices();

    /**
     * Get the list of linked provider instances
     *
     * @return an immutable list of linked providers
     */
    List<? extends SensinactProvider> getLinkedProviders();

    /**
     * Add a linked provider instance
     *
     * @param provider
     */
    void addLinkedProvider(SensinactProvider provider);

    /**
     * Remove a linked provider instance
     *
     * @param provider
     */
    void removeLinkedProvider(SensinactProvider provider);

    /**
     * Delete this provider from the Digital Twin
     */
    void delete();
}
