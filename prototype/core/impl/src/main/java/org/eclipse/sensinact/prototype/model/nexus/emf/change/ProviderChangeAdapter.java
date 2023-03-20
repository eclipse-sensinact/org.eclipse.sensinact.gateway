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
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.sensinact.model.core.provider.Metadata;
import org.eclipse.sensinact.model.core.provider.Provider;
import org.eclipse.sensinact.model.core.provider.ProviderPackage;
import org.eclipse.sensinact.model.core.provider.Service;
import org.eclipse.sensinact.prototype.model.Resource;
import org.eclipse.sensinact.prototype.model.nexus.emf.EMFUtil;
import org.eclipse.sensinact.prototype.notification.NotificationAccumulator;

/**
 * This Adapter Records Changes to a Provider in general and provides
 * notifications via the current {@link NotificationAccumulator}. Changes will
 * be recoreded first and send out after fireNotifications is called.
 *
 * When the Adapter is added the following will happen:
 * <li>1. All defined {@link EReference}s of the return type {@link Service}
 * will be iterated and all {@link EAttribute}s that have a default value will
 * be announced with the given last update timestamp. For all {@link Service}s
 * containing such Resources an instance will be created and for the Resources a
 * Metadata will be created with the given timestamp.</li>
 * <li>New Resources will be announced as resources will be set.</li>
 * <li>Removel will be announced, only if they are marked as unsetable and when
 * their value will become null. If the value is set back to its default, it
 * will not be seen as unset, contrary to how EMF sees this here.</li>
 *
 * Notifications for Resource updates will be created under the following
 * conditions:
 * <li>First and formost, only after fireNotifications is called!</li>
 * <li>When an update to an actual value happens.</li>
 * <li>When a timestamp for a resource changes.</li>
 *
 * {@link Service} and the {@link Resource} itself have a timestamp. When either
 * one changes all contained and set Resources will send out and update.
 *
 * Notification collection:
 *
 * Until fireNotications is called, changes can happen in a random order.
 * Besides the above mentioned criteria it is importent, that the timestamp of
 * the last and current change and the old and new values (if present) are
 * reflected.
 *
 * TODO: Metadata Updates???
 *
 * @author Juergen Albert
 * @since 23 Mar 2023
 */
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
                && ProviderPackage.Literals.SERVICE.isSuperTypeOf(((EReference) feature).getEReferenceType())) {
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
