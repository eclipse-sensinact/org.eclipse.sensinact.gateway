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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.ETypedElement;
import org.eclipse.sensinact.model.core.metadata.MetadataFactory;
import org.eclipse.sensinact.model.core.metadata.NexusMetadata;
import org.eclipse.sensinact.model.core.metadata.ResourceMetadata;
import org.eclipse.sensinact.model.core.provider.Metadata;
import org.eclipse.sensinact.model.core.provider.Provider;
import org.eclipse.sensinact.model.core.provider.ProviderPackage;
import org.eclipse.sensinact.model.core.provider.Service;
import org.eclipse.sensinact.model.core.provider.impl.FeatureMetadataImpl;
import org.eclipse.sensinact.prototype.model.nexus.emf.EMFUtil;
import org.eclipse.sensinact.prototype.notification.NotificationAccumulator;

import com.google.common.base.Objects;

public class ServiceChangeAdapter extends AdapterImpl {

    private Supplier<NotificationAccumulator> accumulatorSupplier;

    /**
     * Keeps a List of which Metadata has been updated with a current Timestamp. It
     * is used to determine if a value change should be marked as now and in the end
     * to determine if notifications have to be send for resources, where only the
     * timestamp has changed.
     */
    List<ETypedElement> metadataAlreadyTouched = new ArrayList<>();

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
        } else if (msg.getFeature() == ProviderPackage.Literals.SERVICE__METADATA
                && msg.getEventType() == Notification.ADD) {
            FeatureMetadataImpl newData = ((FeatureMetadataImpl) msg.getNewValue());
            ETypedElement element = newData.getKey();
            Metadata meta = newData.getValue();
            Service service = getService();
            if (!(meta instanceof NexusMetadata)) {
                NexusMetadata realMetadata;
                if (element instanceof EAttribute) {
                    realMetadata = MetadataFactory.eINSTANCE.createResourceMetadata();
                } else {
                    realMetadata = MetadataFactory.eINSTANCE.createActionMetadata();
                }
                service.getMetadata().removeKey(element);
                service.getMetadata().put(element, realMetadata);
            } else if (meta.eAdapters().stream().filter(MetadataChangeAdapter.class::isInstance).findFirst()
                    .isEmpty()) {
                meta.eAdapters().add(new MetadataChangeAdapter(this, accumulatorSupplier));
            }
        } else if (msg.getFeature() == ProviderPackage.Literals.SERVICE__METADATA
                && msg.getEventType() == Notification.REMOVE) {
            Metadata meta = ((FeatureMetadataImpl) msg.getOldValue()).getValue();
            meta.eAdapters().stream().filter(MetadataChangeAdapter.class::isInstance).findFirst()
                    .ifPresent(getService().eAdapters()::remove);
        }
    }

    private void checkMetadataRemove(Notification msg) {
        Service service = getService();
        EAttribute resource = (EAttribute) msg.getFeature();
        service.getMetadata().removeKey(resource);
    }

    protected void checkMetadata(Notification msg) {
        EAttribute attribute = (EAttribute) msg.getFeature();
        checkMetadata(attribute);
    }

    protected void checkMetadata(EAttribute attribute) {
        if (!getService().getMetadata().containsKey(attribute)) {
            ResourceMetadata curMetadata = MetadataFactory.eINSTANCE.createResourceMetadata();
            curMetadata.setTimestamp(Instant.now());
            curMetadata.setOriginalName(attribute.getName());
            getService().getMetadata().put(attribute, curMetadata);
        }
    }

    private Service getService() {
        Notifier target = getTarget();
        if (target instanceof Service) {
            return (Service) target;
        }
        throw new RuntimeException(
                "The ServiceChangeAdapter must be attached to a Service or one of its Metadata Objects");
    }

    private void notifyAttributeChange(Notification msg, NotificationAccumulator accumulator) {
        EAttribute resource = (EAttribute) msg.getFeature();
        Object oldValue = msg.getOldValue();
        Object newValue = msg.getNewValue();
        Service service = getService();
        EObject container = service.eContainer();
        if (container instanceof Provider) {
            String modelName = EMFUtil.getModelName(container.eClass());
            String providerName = ((Provider) container).getId();
            String serviceName = service.eContainingFeature().getName();
            Metadata resourceMetadata = getService().getMetadata().get(resource);

            if (resourceMetadata == null) {
                throw new NullPointerException(
                        "No Metadata Found for " + resource.getName() + " there is some serious Programming Error!");
            }

            if (!metadataAlreadyTouched.remove(resource)) {
                // Nobody has updated the Metadata yet and set a new one. Thus we will do it
                resourceMetadata.setTimestamp(Instant.now());
            }

            Instant timestamp = resourceMetadata.getTimestamp();

            if (msg.getEventType() == Notification.SET && Objects.equal(oldValue, resource.getDefaultValue())) {
                accumulator.addResource(modelName, providerName, serviceName, resource.getName());
            }

            Map<String, Object> oldMetaData = null;
            Instant previousTimestamp = getPreviousTimestamp(resourceMetadata);
            if (previousTimestamp != null && !previousTimestamp.equals(Instant.EPOCH)) {
                oldMetaData = EMFUtil.toEObjectAttributesToMap(resourceMetadata, true, EMFUtil.METADATA_PRIVATE_LIST,
                        ProviderPackage.Literals.FEATURE_CUSTOM_METADATA__TIMESTAMP, previousTimestamp);
                oldMetaData.put("value", oldValue);
            }

            accumulator.resourceValueUpdate(modelName, providerName, serviceName, resource.getName(),
                    resource.getEAttributeType().getInstanceClass(), oldValue, newValue,
                    timestamp == null ? Instant.now() : timestamp);

            Map<String, Object> newMetaData = EMFUtil.toEObjectAttributesToMap(resourceMetadata, true,
                    EMFUtil.METADATA_PRIVATE_LIST, null, null);
            newMetaData.put("value", newValue);

            accumulator.metadataValueUpdate(modelName, providerName, serviceName, resource.getName(), oldMetaData,
                    newMetaData, timestamp);

            if (msg.getEventType() == Notification.UNSET) {
                accumulator.removeResource(modelName, providerName, serviceName, resource.getName());
            }
        }
    }

    private Instant getPreviousTimestamp(Metadata metadata) {
        if (metadata == null || !(metadata instanceof NexusMetadata)) {
            return null;
        }
        return ((NexusMetadata) metadata).getPreviousTimestamp();
    }

    protected void metadataAlreadyTouched(ETypedElement key) {
        metadataAlreadyTouched.add(key);
    }

}
