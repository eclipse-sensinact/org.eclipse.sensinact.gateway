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

import java.util.List;
import java.util.Map;

/**
 * Extended {@link Task} dedicated to URI typed tasks
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface URITask extends Task {
    public static final Task.RequestType REQUEST_TYPE = Task.RequestType.URI;

    /**
     * Returns the payload of the frame command to send as
     * a bytes array
     *
     * @return the payload of the frame command to send as
     * a bytes array
     */
    Object getContent();

    /**
     * Returns the string formated URI of the targeted remote
     * service endpoint
     *
     * @return the string formated URI of the targeted remote
     * service endpoint
     */
    String getUri();

    /**
     * Returns a set options parameterizing the request to
     * build
     *
     * @return a set options parameterizing the request to
     * build
     */
    Map<String, List<String>> getOptions();
}
