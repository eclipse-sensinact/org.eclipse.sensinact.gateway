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
package org.eclipse.sensinact.sensorthings.sensing.dto;

import java.util.List;

public record RootResponse(ServerSettings serverSettings, List<NameUrl> value) {

    public RootResponse {
        if(value != null) {
            value = List.copyOf(value);
        }
    }

    public record ServerSettings (List<String> conformance) {
        public ServerSettings {
            if(conformance != null) {
                conformance = List.copyOf(conformance);
            }
        }
    }

    public record NameUrl(String name, String url) {
    }
}
