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
package org.eclipse.sensinact.sensorthings.sensing.dto.expand;

import org.eclipse.sensinact.sensorthings.sensing.dto.Id;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * record that define a link that can be set on creation/update to association 2
 * sensorthing entity
 */
public record RefId(@JsonProperty("@iot.id") Object id, @JsonProperty("@iot.selfLink") String selfLink) implements Id {

    public RefId(Object id) {
        this(id, null);
    }
}
