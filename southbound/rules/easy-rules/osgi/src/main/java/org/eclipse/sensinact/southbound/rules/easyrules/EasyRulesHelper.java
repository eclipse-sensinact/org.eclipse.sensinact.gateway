/*********************************************************************
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Kentyou - initial implementation
 **********************************************************************/
package org.eclipse.sensinact.southbound.rules.easyrules;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.southbound.rules.api.ResourceUpdater;
import org.jeasy.rules.api.Facts;

public class EasyRulesHelper {

    private EasyRulesHelper() { }

    public static Facts toFacts(List<ProviderSnapshot> snapshots, ResourceUpdater updater) {
        Facts facts = new Facts();
        facts.put("$updater", updater);
        facts.put("$providers", snapshots.stream().map(ProviderSnapshot::getName).collect(toList()));

        Map<String, Object> data = snapshots.stream()
                .collect(toMap(ProviderSnapshot::getName, EasyRulesHelper::toProviderData));
        facts.put("$data", data);
        return facts;
    }

    private static Map<String, Object> toProviderData(ProviderSnapshot p) {
        final Map<String, Object> result = new HashMap<String, Object>();
        final List<String> services = new ArrayList<String>();

        for(ServiceSnapshot s : p.getServices()) {
            String name = s.getName();
            services.add(name);
            result.put(name, toServiceData(s));
        };
        result.put("$services", List.copyOf(services));

        return Collections.unmodifiableMap(result);
    }

    private static Map<String, Object> toServiceData(ServiceSnapshot s) {
        final Map<String, Object> result = new HashMap<String, Object>();
        final List<String> resources = new ArrayList<String>();

        for(ResourceSnapshot r : s.getResources()) {
            String name = r.getName();
            resources.add(name);
            result.put(name, toResourceData(r));
        };
        result.put("$resources", List.copyOf(resources));

        return Collections.unmodifiableMap(result);
    }

    private static Map<String, Object> toResourceData(ResourceSnapshot r) {
        final Map<String, Object> result = new HashMap<String, Object>();
        result.putAll(r.getMetadata());

        if(r.isSet()) {
            TimedValue<?> tv = r.getValue();
            result.put("$set", Boolean.TRUE);
            result.put("$value", tv.getValue());
            result.put("$timestamp", tv.getTimestamp());
        } else {
            result.put("$set", Boolean.FALSE);
        }

        return Collections.unmodifiableMap(result);
    }
}
