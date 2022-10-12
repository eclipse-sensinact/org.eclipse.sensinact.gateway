/*********************************************************************
* Copyright (c) 2022 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.sensorthings.sensing.dto;

import java.util.List;

public class RootResponse {

    public List<NameUrl> value;
    
    public static class NameUrl {
        public String name;
        public String url;
    }
    
}
