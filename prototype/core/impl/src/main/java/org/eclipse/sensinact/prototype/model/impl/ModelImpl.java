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
package org.eclipse.sensinact.prototype.model.impl;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.sensinact.model.core.SensiNactPackage;
import org.eclipse.sensinact.prototype.command.impl.CommandScopedImpl;
import org.eclipse.sensinact.prototype.model.Model;
import org.eclipse.sensinact.prototype.model.Service;
import org.eclipse.sensinact.prototype.model.ServiceBuilder;
import org.eclipse.sensinact.prototype.model.nexus.impl.ModelNexus;
import org.eclipse.sensinact.prototype.notification.NotificationAccumulator;

public class ModelImpl extends CommandScopedImpl implements Model {

    private final String name;

    private final EClass eClass;

    private ModelNexus nexusImpl;

    private final NotificationAccumulator accumulator;

    public ModelImpl(AtomicBoolean active, String name, EClass eClass, ModelNexus nexusImpl,
            NotificationAccumulator accumulator) {
        super(active);
        this.name = name;
        this.eClass = eClass;
        this.nexusImpl = nexusImpl;
        this.accumulator = accumulator;
    }

    @Override
    public String getName() {
        checkValid();
        return name;
    }

    @Override
    public boolean isExclusivelyOwned() {
        checkValid();
        throw new RuntimeException("Not implemented");
    }

    @Override
    public boolean isAutoDelete() {
        checkValid();
        throw new RuntimeException("Not implemented");
    }

    @Override
    public ServiceBuilder<Service> createService(String service) {
        checkValid();
        return new ServiceBuilderImpl<>(active, null, this, service, nexusImpl, accumulator);
    }

    @Override
    public Map<String, ? extends Service> getServices() {
        checkValid();
        // Remember to add in the provider, which is in the parent type and
        // therefore doesn't show up in EStructuralFeatures
        return Stream
                .concat(Stream.of(SensiNactPackage.eINSTANCE.getProvider_Admin()),
                        eClass.getEStructuralFeatures().stream())
                .collect(Collectors.toMap(f -> f.getName(),
                        f -> new ServiceImpl(active, this, f, nexusImpl, accumulator)));
    }

}
