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

import org.eclipse.sensinact.core.model.ResourceType;
import org.eclipse.sensinact.northbound.query.api.EReadWriteMode;

public class ShortResourceDescriptionDTO {

    /**
     * Resource name
     */
    public String name;

    /**
     * Read/write mode
     */
    public EReadWriteMode rws;

    /**
     * Resource type
     */
    public ResourceType type;
}
