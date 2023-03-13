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
*   Data In Motion - initial API and implementation
**********************************************************************/
package org.eclipse.sensinact.prototype.model.nexus.emf.change;

import java.time.Instant;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.sensinact.model.core.Metadata;
import org.eclipse.sensinact.model.core.Provider;
import org.eclipse.sensinact.model.core.SensiNactPackage;
import org.eclipse.sensinact.model.core.Service;
import org.eclipse.sensinact.prototype.model.nexus.emf.EMFUtil;
import org.eclipse.sensinact.prototype.notification.NotificationAccumulator;

public class ProviderChangeAdapter extends AdapterImpl {

    private Supplier<NotificationAccumulator> accumulatorSupplier;

    public ProviderChangeAdapter(Supplier<NotificationAccumulator> accumulatorSupplier) {
        this.accumulatorSupplier = accumulatorSupplier;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.emf.common.notify.impl.AdapterImpl#notifyChanged(org.eclipse.emf.
     * common.notify.Notification)
     */
    @Override
    public void notifyChanged(Notification msg) {
        Object feature = msg.getFeature();
        if (feature instanceof EReference
                && SensiNactPackage.Literals.SERVICE.isSuperTypeOf(((EReference) feature).getEReferenceType())) {
            notifyReferenceChange(msg, accumulatorSupplier.get());
        }
    }

    private void notifyReferenceChange(Notification msg, NotificationAccumulator accumulator) {
        EReference reference = (EReference) msg.getFeature();
        Provider provider = (Provider) msg.getNotifier();
        if (msg.getEventType() == Notification.SET) {
            notifyServiceAdd(provider, (Service) msg.getNewValue(), reference, accumulator);
        } else if (msg.getEventType() == Notification.UNSET) {
            notifyServiceRemove(provider, (Service) msg.getOldValue(), reference, accumulator);
        }
    }

    private void notifyServiceAdd(Provider container, Service value, EReference reference,
            NotificationAccumulator accumulator) {
        String model = EMFUtil.getModelName(container.eClass());
        String providerName = container.getId();
        String serviceName = reference.getName();
        ServiceChangeAdapter serviceChangeAdapter = new ServiceChangeAdapter(accumulatorSupplier);
        value.eAdapters().add(serviceChangeAdapter);

        accumulator.addService(model, providerName, serviceName);

        EMFUtil.streamAttributes(value.eClass()).filter(ea -> value.eIsSet(ea)).forEach(ea -> {
            serviceChangeAdapter.checkMetadata(ea);
            Metadata metadata = value.getMetadata().get(ea);
            accumulator.addResource(model, providerName, serviceName, ea.getName());
            accumulator.resourceValueUpdate(model, providerName, serviceName, ea.getName(),
                    ea.getEAttributeType().getInstanceClass(), null, value.eGet(ea), metadata.getTimestamp());
            Map<String, Object> newMetaData = EMFUtil.toEObjectAttributesToMap(metadata, true,
                    EMFUtil.METADATA_PRIVATE_LIST, null, null);
            newMetaData.put("value", value.eGet(ea));

            accumulator.metadataValueUpdate(model, providerName, serviceName, ea.getName(), null, newMetaData,
                    metadata.getTimestamp());
        });
    }

    private void notifyServiceRemove(Provider container, Service value, EReference reference,
            NotificationAccumulator accumulator) {
        String model = EMFUtil.getModelName(container.eClass());
        String providerName = container.getId();
        String serviceName = reference.getName();

        value.eAdapters().removeAll(
                value.eAdapters().stream().filter(ServiceChangeAdapter.class::isInstance).collect(Collectors.toList()));

        EMFUtil.streamAttributes(value.eClass()).filter(ea -> value.eIsSet(ea)).forEach(ea -> {
            accumulator.resourceValueUpdate(model, providerName, serviceName, ea.getName(),
                    ea.getEAttributeType().getInstanceClass(), null, null, Instant.now());
            accumulator.removeResource(model, providerName, serviceName, ea.getName());
        });
        accumulator.removeService(model, providerName, serviceName);
    }

}
