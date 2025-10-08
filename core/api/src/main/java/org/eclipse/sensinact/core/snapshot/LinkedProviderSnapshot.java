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

import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;

public interface LinkedProviderSnapshot extends CommonProviderSnapshot {

    /**
     * Returns the friendly name the provider
     */
    String getFriendlyName();

    /**
     * Returns the description of the provider
     */
    String getDescription();

    /**
     * Returns the icon of the provider
     */
    String getIcon();

    /**
     * Returns the location of the provider
     */
    GeoJsonObject getLocation();

}
