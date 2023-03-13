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
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.sensinact.model.core.Metadata;
import org.eclipse.sensinact.model.core.Provider;
import org.eclipse.sensinact.model.core.ResourceMetadata;
import org.eclipse.sensinact.model.core.SensiNactFactory;
import org.eclipse.sensinact.model.core.SensiNactPackage;
import org.eclipse.sensinact.model.core.Service;
import org.eclipse.sensinact.model.core.impl.FeatureMetadataImpl;
import org.eclipse.sensinact.prototype.model.nexus.emf.EMFUtil;
import org.eclipse.sensinact.prototype.notification.NotificationAccumulator;

import com.google.common.base.Objects;

public class ServiceChangeAdapter extends AdapterImpl {

    private Supplier<NotificationAccumulator> accumulatorSupplier;

    public ServiceChangeAdapter(Supplier<NotificationAccumulator> accumulatorSupplier) {
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
        if (msg.getFeature() instanceof EAttribute) {
            if (msg.getEventType() == Notification.SET) {
                checkMetadata(msg);
            } else if (msg.getEventType() == Notification.UNSET) {
                checkMetadataRemove(msg);
            }
            notifyAttributeChange(msg, accumulatorSupplier.get());
        } else if (msg.getFeature() == SensiNactPackage.Literals.SERVICE__METADATA
                && msg.getEventType() == Notification.ADD) {
            ((FeatureMetadataImpl) msg.getNewValue()).getValue().eAdapters()
                    .add(new MetadataChangeAdapter(accumulatorSupplier));
        }
    }

    /**
     * @param msg
     */
    private void checkMetadataRemove(Notification msg) {
        Service service = getService();
        EAttribute resource = (EAttribute) msg.getFeature();
        service.getMetadata().removeKey(resource);
    }

    /**
     * @param msg
     */
    private void checkMetadata(Notification msg) {
        checkMetadata((EAttribute) msg.getFeature());
    }

    public void checkMetadata(EAttribute resource) {
        Service service = getService();
        if (!service.getMetadata().containsKey(resource)) {
            ResourceMetadata metadata = SensiNactFactory.eINSTANCE.createResourceMetadata();
            metadata.setTimestamp(Instant.now());
            metadata.setOriginalName(resource.getName());
            service.getMetadata().put(resource, metadata);
        } else {
            ResourceMetadata metadata = service.getMetadata().get(resource);
            if (!alreadyUpdated(metadata)) {
                metadata.setTimestamp(Instant.now());
            }
        }
    }

    private Service getService() {
        return (Service) getTarget();
    }

    private void notifyAttributeChange(Notification msg, NotificationAccumulator accumulator) {
        Service service = getService();
        EAttribute resource = (EAttribute) msg.getFeature();
        Object oldValue = msg.getOldValue();
        EObject container = service.eContainer();
        if (container instanceof Provider) {
            String modelName = EMFUtil.getModelName(container.eClass());
            String providerName = ((Provider) container).getId();
            String serviceName = service.eContainingFeature().getName();
            if (msg.getEventType() == Notification.SET && Objects.equal(oldValue, resource.getDefaultValue())) {
                accumulator.addResource(modelName, providerName, serviceName, resource.getName());
            }
            Metadata metadata = service.getMetadata().get(resource);

            Instant timestamp = metadata == null ? Instant.now() : metadata.getTimestamp();

            Map<String, Object> oldMetaData = null;
            Instant previousTimestamp = getPreviousTimestamp(metadata);
            if (previousTimestamp != null && !previousTimestamp.equals(Instant.EPOCH)) {
                oldMetaData = EMFUtil.toEObjectAttributesToMap(metadata, true, EMFUtil.METADATA_PRIVATE_LIST,
                        SensiNactPackage.Literals.FEATURE_CUSTOM_METADATA__TIMESTAMP, previousTimestamp);
                oldMetaData.put("value", oldValue);
            }

            accumulator.resourceValueUpdate(modelName, providerName, serviceName, resource.getName(),
                    resource.getEAttributeType().getInstanceClass(), oldValue, msg.getNewValue(),
                    timestamp == null ? Instant.now() : timestamp);

            Map<String, Object> newMetaData = EMFUtil.toEObjectAttributesToMap(metadata, true, List
                    .of(SensiNactPackage.Literals.METADATA__ORIGINAL_NAME, SensiNactPackage.Literals.METADATA__LOCKED),
                    null, null);
            newMetaData.put("value", msg.getNewValue());

            accumulator.metadataValueUpdate(modelName, providerName, serviceName, resource.getName(), oldMetaData,
                    newMetaData, timestamp);

            if (msg.getEventType() == Notification.UNSET) {
                accumulator.removeResource(modelName, providerName, serviceName, resource.getName());
            }
        }
    }

    private Instant getPreviousTimestamp(Metadata metadata) {
        if (metadata == null) {
            return null;
        }

        return metadata.eAdapters().stream().filter(MetadataChangeAdapter.class::isInstance)
                .map(MetadataChangeAdapter.class::cast).findFirst().map(MetadataChangeAdapter::getPreviousTimestamp)
                .orElseGet(() -> null);
    }

    private boolean alreadyUpdated(Metadata metadata) {
        if (metadata == null) {
            return Boolean.FALSE;
        }

        return metadata.eAdapters().stream().filter(MetadataChangeAdapter.class::isInstance)
                .map(MetadataChangeAdapter.class::cast).findFirst().map(MetadataChangeAdapter::alreadyUpdated)
                .orElseGet(() -> Boolean.FALSE);
    }
}
