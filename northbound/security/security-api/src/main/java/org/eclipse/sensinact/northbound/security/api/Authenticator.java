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
package org.eclipse.sensinact.northbound.security.api;

public interface Authenticator {

    public UserInfo authenticate(String user, String credential);

    public String getRealm();

    public Scheme getScheme();

    public static enum Scheme {
        USER_PASSWORD("Basic"), TOKEN("Bearer");

        private final String httpScheme;

        private Scheme(String string) {
            this.httpScheme = string;
        }

        public String getHttpScheme() {
            return httpScheme;
        }
    }
}
