/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.generic.uri;

import org.eclipse.sensinact.gateway.generic.Task;
import org.eclipse.sensinact.gateway.generic.TaskTranslator;

import java.util.List;
import java.util.Map;

/**
 * Service dedicated to data stream transmission
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface URITaskTranslator extends TaskTranslator {
    public static final Task.RequestType REQUEST_TYPE = Task.RequestType.URI;

    /**
     * Creates an appropriate request and sends it
     *
     * @param serviceProviderIdentifier the String identifier of the requirer {@link ServiceProvider}
     * @param path                      URI targeted by the request to create
     * @param content                   the request object content
     * @param options                   set of options applying on the request to create
     */
    void send(String serviceProviderIdentifier, String path, Object content, Map<String, List<String>> options);
}
