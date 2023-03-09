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
import java.util.function.Supplier;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.sensinact.model.core.Metadata;
import org.eclipse.sensinact.model.core.SensiNactPackage;
import org.eclipse.sensinact.prototype.notification.NotificationAccumulator;

public class MetadataChangeAdapter extends AdapterImpl {

    private Supplier<NotificationAccumulator> accumulatorSupplier;

    private Instant previousTimestamp = null;

    public MetadataChangeAdapter(Supplier<NotificationAccumulator> accumulatorSupplier) {
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

        if (msg.getFeature() == SensiNactPackage.Literals.METADATA__ORIGINAL_NAME) {
            return;
        } else if (msg.getFeature() == SensiNactPackage.Literals.TIMESTAMPED__TIMESTAMP) {
            previousTimestamp = (Instant) msg.getOldValue();
        } else {
            notifyUpdate(msg, accumulatorSupplier.get());
        }

    }

    public Instant getPreviousTimestamp() {
        return previousTimestamp;
    }

    private Metadata getMetaData() {
        return (Metadata) getTarget();
    }

    private void notifyUpdate(Notification msg, NotificationAccumulator accumulator) {
//        Metadata metadata = getMetaData();
//        Service service = (Service) metadata.eContainer();
//        ETypedElement resource = service.getMetadata().stream().filter(e -> e.getValue() == getMetaData())
//                .map(Entry::getKey).findFirst().get();
//        Object oldValue = msg.getOldValue();
//        EObject provider = service.eContainer();
//        if (provider instanceof Provider) {
//            String modelName = EMFUtil.getModelName(provider.eClass());
//            String providerName = ((Provider) provider).getId();
//            String serviceName = service.eContainingFeature().getName();
//
//            Map<String, Object> oldMetaData = null;
//            oldMetaData = EMFUtil.toEObjectAttributesToMap(metadata, true,
//                    List.of(SensiNactPackage.Literals.METADATA__ORIGINAL_NAME,
//                            SensiNactPackage.Literals.METADATA__LOCKED),
//                    (EStructuralFeature) msg.getFeature(), oldValue);
//
//            Map<String, Object> newMetaData = EMFUtil.toEObjectAttributesToMap(metadata, true, List
//                    .of(SensiNactPackage.Literals.METADATA__ORIGINAL_NAME, SensiNactPackage.Literals.METADATA__LOCKED),
//                    null, null);
//
//            accumulator.metadataValueUpdate(modelName, providerName, serviceName, resource.getName(), oldMetaData,
//                    newMetaData, metadata.getTimestamp());
//        }
    }
}
