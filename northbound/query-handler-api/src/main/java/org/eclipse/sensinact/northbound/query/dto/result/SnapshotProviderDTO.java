/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.northbound.query.dto.result;

import java.util.List;
import java.util.Map;

public class SnapshotProviderDTO {

    /**
     * Provider ID
     */
    public String name;

    /**
     * Model Name
     */
    public String modelName;

    /**
     * List of services
     */
    public Map<String, SnapshotServiceDTO> services;

    /**
     * List of services
     */
    public List<SnapshotLinkedProviderDTO> linkedProviders;
}
