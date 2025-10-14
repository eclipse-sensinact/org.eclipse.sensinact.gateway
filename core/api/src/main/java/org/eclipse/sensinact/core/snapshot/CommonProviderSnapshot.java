/*********************************************************************
* Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package org.eclipse.sensinact.core.snapshot;

/**
 * Common behaviours between {@link ProviderSnapshot} and
 * {@link LinkedProviderSnapshot}
 */
public interface CommonProviderSnapshot extends Snapshot {

    /**
     * Returns the package URI of the model of the provider
     */
    String getModelPackageUri();

    /**
     * Returns the name of the model of the provider
     */
    String getModelName();

}
