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
package org.eclipse.sensinact.gateway.southbound.http.callback.api;

import java.io.Reader;
import java.util.List;
import java.util.Map;

public interface HttpCallback {

    void activate(String uri);

    void deactivate(String uri);

    void call(String uri, Map<String, List<String>> headers, Reader body);

}
