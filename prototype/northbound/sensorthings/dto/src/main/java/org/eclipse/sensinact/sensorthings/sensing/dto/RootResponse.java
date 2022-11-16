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

public class RootResponse {

    public ServerSettings serverSettings;

    public List<NameUrl> value;

    public static class ServerSettings {
        public List<String> conformance;
    }

    public static class NameUrl {
        public String name;
        public String url;

        public static NameUrl create(String name, String url) {
            NameUrl nu = new NameUrl();
            nu.name = name;
            nu.url = url;
            return nu;
        }
    }

}
