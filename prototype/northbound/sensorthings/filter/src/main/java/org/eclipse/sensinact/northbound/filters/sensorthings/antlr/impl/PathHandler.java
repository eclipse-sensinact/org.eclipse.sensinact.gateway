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
package org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.sensinact.prototype.snapshot.ResourceSnapshot;

/**
 * @author thoma
 *
 */
public class PathHandler {

    private final String path;

    public PathHandler(final String path) {
        this.path = path;
        if (path.isEmpty()) {
            throw new InvalidResultTypeException("Got an empty path");
        }
    }

    Object handle(ResourceValueFilterInputHolder holder) {
        // TODO implement it correctly

        System.out.println("Solving " + path + " in " + holder);

        final String[] parts = path.split("/");
        if (parts.length == 1) {
            // Filter resources by name
            List<ResourceSnapshot> matching = holder.getResources().stream().filter(r -> path.equals(r.getName()))
                    .collect(Collectors.toList());
            if (matching.size() == 1) {
                // Direct catch
                System.out.println("Direct match " + path + " -> " + matching.get(0).getValue().getValue());
                return matching.get(0).getValue().getValue();
            } else if (matching.size() > 1) {
                System.err
                        .println(String.format("Got multiple matches for resource name: %s -> %s", path,
                                matching.stream().map(r -> String.format("%s/%s/%s",
                                        r.getService().getProvider().getName(), r.getService().getName(), r.getName()))
                                        .collect(Collectors.toList())));
                return matching.get(0).getValue().getValue();
            } else {
                // No match
                System.out.println("No match for " + path + " in " + holder);
                return null;
            }
        } else if ("id".equals(parts[parts.length - 1])) {
            System.out.println("Return ID -> " + holder.getProvider().getName());
            return holder.getProvider().getName();
        } else {
            // No handled
            System.err.println("Path not handled: " + path);
            return null;
        }
    }
}
