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
package org.eclipse.sensinact.northbound.filters.api;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.sensinact.prototype.command.AbstractTwinCommand;
import org.eclipse.sensinact.prototype.command.GatewayThread;
import org.eclipse.sensinact.prototype.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.prototype.twin.SensinactDigitalTwin;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

/**
 * Helper that holds the command to run in the gateway thread
 */
public class FilterCommandHelper {

    public static Collection<ProviderSnapshot> executeFilter(GatewayThread thread, ICriterion criterion)
            throws FilterException {
        Collection<ProviderSnapshot> providers;
        try {
            providers = thread.execute(new AbstractTwinCommand<Collection<ProviderSnapshot>>() {
                protected Promise<Collection<ProviderSnapshot>> call(SensinactDigitalTwin model, PromiseFactory pf) {
                    return pf.resolved(model.filteredSnapshot(null, criterion.getProviderFilter(), null, null));
                }
            }).getValue();
        } catch (InterruptedException e) {
            throw new FilterException("Interrupted creating snapshot", e);
        } catch (InvocationTargetException e) {
            throw new FilterException("Error creating snapshot", e.getCause());
        }

        if (criterion.getResourceValueFilter() != null) {
            final ResourceValueFilter rcFilter = criterion.getResourceValueFilter();
            return providers
                    .stream().filter(p -> rcFilter.test(p, p.getServices().stream()
                            .flatMap(s -> s.getResources().stream()).collect(Collectors.toList())))
                    .collect(Collectors.toList());
        } else {
            return List.copyOf(providers);
        }
    }
}
