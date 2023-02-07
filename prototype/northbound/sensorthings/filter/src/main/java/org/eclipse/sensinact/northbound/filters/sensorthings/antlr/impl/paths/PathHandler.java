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
package org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.paths;

import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.ResourceValueFilterInputHolder;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.UnsupportedRuleException;

/**
 * @author thoma
 *
 */
public class PathHandler {

    private final String path;

    public PathHandler(final String path) {
        this.path = path;
    }

    public Object handle(final ResourceValueFilterInputHolder holder) {
        // TODO implement it correctly
        switch (holder.getContext()) {
        case THINGS:
            return new ThingPathHandler(holder.getProvider(), holder.getResources()).handle(path);

        case OBSERVATIONS:
            return new ObservationPathHandler(holder.getProvider(), holder.getResource()).handle(path);

        default:
            throw new UnsupportedRuleException("Path of " + holder.getContext() + " is not yet supported");
        }
    }
}
