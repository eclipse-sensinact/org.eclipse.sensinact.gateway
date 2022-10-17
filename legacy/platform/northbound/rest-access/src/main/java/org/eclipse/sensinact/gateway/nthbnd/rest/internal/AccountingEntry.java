/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.nthbnd.rest.internal;

public class AccountingEntry {
    public final String timestamp;
    public final String client;
    public final String uri;
    public final String method;
    public final String status;

    public AccountingEntry(String timestamp, String client, String uri, String method, String status) {
        this.timestamp = timestamp;
        this.client = client;
        this.uri = uri;
        this.method = method;
        this.status = status;
    }
}
