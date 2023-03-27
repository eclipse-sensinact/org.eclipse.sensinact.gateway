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
package org.eclipse.sensinact.prototype.model.nexus.emf.compare;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sensinact.model.core.metadata.MetadataFactory;
import org.eclipse.sensinact.model.core.metadata.MetadataPackage;
import org.eclipse.sensinact.model.core.metadata.ResourceMetadata;
import org.eclipse.sensinact.model.core.provider.Admin;
import org.eclipse.sensinact.model.core.provider.FeatureCustomMetadata;
import org.eclipse.sensinact.model.core.provider.Metadata;
import org.eclipse.sensinact.model.core.provider.Provider;
import org.eclipse.sensinact.model.core.provider.ProviderPackage;
import org.eclipse.sensinact.model.core.provider.Service;
import org.eclipse.sensinact.prototype.model.nexus.emf.EMFUtil;
import org.eclipse.sensinact.prototype.notification.NotificationAccumulator;

/**
 * Helper to Compare EObjects using EMFCompare
 *
 * @author Juergen Albert
 * @since 22 Feb 2023
 */
public class EMFCompareUtil {

    public static void compareAndSet(Provider incomming, Provider original, NotificationAccumulator accumulator) {
        if (incomming == null || original == null) {
            return;
        }

        if (incomming.eClass() != original.eClass()) {
            throw new IllegalArgumentException(
                    String.format("The given incomming Provider %s is of type %s but should be of type %s",
                            incomming.toString(), incomming.eClass().getName(), original.eClass().getName()));
        }

        EClass eClass = incomming.eClass();

        // We can simply set all attributes at the Provider level without any checks as
        // they are out of the
        EMFUtil.streamAttributes(eClass).forEach(ea -> original.eSet(ea, incomming.eGet(ea)));
        // The same goes for any Reference that is not of type Service or is the linked
        // Provider Reference
        eClass.getEAllReferences().stream()
                // We don't want references from EObject and anything above
                .filter(er -> er.getEContainingClass().getEPackage() != EcorePackage.eINSTANCE)
                .filter(Predicate.not(ProviderPackage.Literals.PROVIDER__LINKED_PROVIDERS::equals))
                .filter(er -> !ProviderPackage.Literals.SERVICE.isSuperTypeOf(er.getEReferenceType())).forEach(er -> {
                    original.eSet(er,
                            er.isContainment() ? EcoreUtil.copy((EObject) incomming.eGet(er)) : incomming.eGet(er));
                });

        updateAdmin(incomming, original, accumulator);

        eClass.getEAllReferences().stream()
                // We don't want references from EObject and anything above
                .filter(er -> er.getEContainingClass().getEPackage() != EcorePackage.eINSTANCE)
                .filter(Predicate.not(ProviderPackage.Literals.PROVIDER__LINKED_PROVIDERS::equals))
                .filter(Predicate.not(ProviderPackage.Literals.PROVIDER__ADMIN::equals))
                .filter(er -> ProviderPackage.Literals.SERVICE.isSuperTypeOf(er.getEReferenceType())).forEach(er -> {
                    serviceUpdate(er, incomming, original, accumulator, Collections.emptyList());
                });
    }

    private static void updateAdmin(Provider incomming, Provider original, NotificationAccumulator accumulator) {
        Admin newService = incomming.getAdmin();
        if (newService != null) {
            serviceUpdate(ProviderPackage.Literals.PROVIDER__ADMIN, incomming, original, accumulator,
                    List.of(ProviderPackage.Literals.ADMIN__MODEL_URI));
        }
    }

    private static void serviceUpdate(EReference reference, Provider incomming, Provider original,
            NotificationAccumulator accumulator, List<EStructuralFeature> blackList) {
        Service newService = (Service) incomming.eGet(reference);
        Service oldService = (Service) original.eGet(reference);

        if (newService == null && oldService == null) {
            return;
        } else if (newService != null && oldService == null) {
            Service copy = EcoreUtil.copy(newService);
            original.eSet(reference, copy);
            notifyServiceAdd(original, copy, reference, accumulator);
        } else if (newService == null && oldService != null) {
            notifyServiceRemove(original, oldService, reference, accumulator);
            original.eUnset(reference);
        } else {
            mergeAndNotify(reference, newService, oldService, blackList, accumulator);
        }
    }

//    private static void mergeAndNotify(EReference reference, Service newService, Service originalService,
//            NotificationAccumulator accumulator) {
//        mergeAndNotify(reference, newService, originalService, Collections.emptyList(), accumulator);
//    }

    private static void mergeAndNotify(EReference reference, Service newService, Service originalService,
            List<EStructuralFeature> blackList, NotificationAccumulator accumulator) {

        if (newService.eClass() != originalService.eClass()) {
            throw new UnsupportedOperationException("Merging Services of different Tyoes is not supported yet");
        }

        // We can copy every Reference that might exist, as they are out of the scope
        // of any notifications
        originalService.eClass().getEAllReferences().stream()
                // We don't want references from EObject and anything above
                .filter(er -> er.getEContainingClass().getEPackage() != EcorePackage.eINSTANCE)
                .filter(Predicate.not(ProviderPackage.Literals.SERVICE__METADATA::equals))
                .filter(Predicate.not(blackList::contains)).forEach(er -> {
                    originalService.eSet(er,
                            er.isContainment() ? EcoreUtil.copy((EObject) newService.eGet(er)) : newService.eGet(er));
                });

        originalService.eClass().getEAllAttributes().stream()
                // We don't want references from EObject and anything above
                .filter(er -> er.getEContainingClass().getEPackage() != EcorePackage.eINSTANCE)
                .filter(Predicate.not(blackList::contains)).forEach(er -> {
                    notifyAttributeChange(er, newService, originalService, accumulator);
                });

    }

    // 1. Attribute is new; use their timestamp if present or now if not
    // 2. Attribute changed; no timestamp change in diff; update attribute, keep
    // timestamp
    // 3. Attribute changed; timestamp change to null; update with Timestamp Now
    // 4. Attribute changed; timestamp changed; update attribute and timestamp if
    // new is after old timetamp
    // 5. Attribute not changed, but timestamp updated: same as 4.
    private static void notifyAttributeChange(EAttribute resource, Service newService, Service originalService,
            NotificationAccumulator accumulator) {
        EObject container = originalService.eContainer();
        if (container instanceof Provider) {
            String modelName = EMFUtil.getModelName(container.eClass());
            String providerName = ((Provider) container).getId();
            String serviceName = originalService.eContainingFeature().getName();
            Metadata originalMetadata = originalService.getMetadata().get(resource);

            boolean isNew = !originalService.eIsSet(resource);

            Object oldValue = originalService.eGet(resource);
            Object newValue = newService.eGet(resource);

            Instant previousTimestamp = null;
            Instant newTimestamp = getNewTimestampFromMetadata(resource, newService);
            if (originalMetadata != null) {
                previousTimestamp = originalMetadata.getTimestamp();
            }

            if (Objects.equals(oldValue, newValue) && Objects.equals(previousTimestamp, newTimestamp)) {
                return;
            }
            // if we already have a timestamp value that is newer we do nothing
            if (previousTimestamp != null && newTimestamp != null && previousTimestamp.isAfter(newTimestamp)) {
                return;
            }

            // We needed to do the checks before, before we use any kind of default
            // Timestamp from a different Attribute or the current Timestamp
            if (newTimestamp == null) {
                newTimestamp = Instant.now();
            }
            if (isNew || (resource.getDefaultValue() != null && Objects.equals(oldValue, resource.getDefaultValue()))) {
                accumulator.addResource(modelName, providerName, serviceName, resource.getName());
            }

            Map<String, Object> oldMetaData = null;

            if (previousTimestamp != null && !previousTimestamp.equals(Instant.EPOCH)) {
                oldMetaData = extractMetadataMap(oldValue, originalMetadata);
            }

            Metadata updatedMetadata = updateMetadata(resource, originalMetadata, newService, originalService,
                    newTimestamp);

            originalService.eSet(resource, newValue);

            accumulator.resourceValueUpdate(modelName, providerName, serviceName, resource.getName(),
                    resource.getEAttributeType().getInstanceClass(), oldValue, newValue, newTimestamp);

            Map<String, Object> newMetaData = extractMetadataMap(newValue, updatedMetadata);

            accumulator.metadataValueUpdate(modelName, providerName, serviceName, resource.getName(), oldMetaData,
                    newMetaData, newTimestamp);

            if (newValue == null) {
                accumulator.removeResource(modelName, providerName, serviceName, resource.getName());
            }
        }

    }

    public static Map<String, Object> extractMetadataMap(Object value, Metadata updatedMetadata) {
        Map<String, Object> newMetaData = EMFUtil.toEObjectAttributesToMap(updatedMetadata, true,
                MetadataPackage.Literals.NEXUS_METADATA.getEStructuralFeatures(), null, null);
        updatedMetadata.getExtra().stream().forEach(cm -> newMetaData.put(cm.getName(), cm.getValue()));
        newMetaData.put("value", value);
        return newMetaData;
    }

    private static ResourceMetadata updateMetadata(EAttribute resource, Metadata originalMetadata, Service newService,
            Service originalService, Instant newTimestamp) {
        ResourceMetadata resourceMetadata = (ResourceMetadata) originalMetadata;
        if (resourceMetadata == null) {
            resourceMetadata = MetadataFactory.eINSTANCE.createResourceMetadata();
            originalService.getMetadata().put(resource, resourceMetadata);
            resourceMetadata.setOriginalName(resource.getName());
        }
        resourceMetadata.setTimestamp(newTimestamp);
        Metadata update = newService.getMetadata().get(resource);
        if (update != null && update.eIsSet(ProviderPackage.Literals.METADATA__EXTRA)) {
            updateAndNotifyExtraMetadata(update.getExtra(), resourceMetadata.getExtra(), newTimestamp);
        }

        return resourceMetadata;
    }

    private static void updateAndNotifyExtraMetadata(EList<FeatureCustomMetadata> extraNew,
            EList<FeatureCustomMetadata> extraOriginal, Instant newTimestamp) {
        if (extraNew.isEmpty() && extraOriginal.isEmpty()) {
            return;
        }
        List<FeatureCustomMetadata> toRemove = new ArrayList<>(extraOriginal);

        extraNew.forEach(fcm -> {
            FeatureCustomMetadata original = removeByName(fcm.getName(), toRemove);
            Instant timestamp = fcm.getTimestamp() == null ? newTimestamp : fcm.getTimestamp();
            if (original == null) {
                FeatureCustomMetadata copy = EcoreUtil.copy(fcm);
                if (copy.getTimestamp() == null) {
                    copy.setTimestamp(newTimestamp);
                }
                extraOriginal.add(copy);
            } else if (original.getTimestamp().plusMillis(1).isBefore(timestamp)) {
                original.setValue(fcm.getValue());
                original.getTimestamp();
            }
        });
        extraOriginal.removeAll(toRemove);
    }

    private static FeatureCustomMetadata removeByName(String name, List<FeatureCustomMetadata> compareList) {
        for (Iterator<FeatureCustomMetadata> iterator = compareList.iterator(); iterator.hasNext();) {
            FeatureCustomMetadata featureCustomMetadata = iterator.next();
            if (name.equals(featureCustomMetadata.getName())) {
                iterator.remove();
                return featureCustomMetadata;
            }
        }
        return null;
    }

    private static Instant getNewTimestampFromMetadata(EAttribute resource, Service service) {
        Metadata metadata = service.getMetadata().get(resource);
        if (metadata != null) {
            if (metadata.getTimestamp() != null) {
                return metadata.getTimestamp();
            }
        }
        return null;
    }

    private static void notifyServiceAdd(Provider container, Service service, EReference reference,
            NotificationAccumulator accumulator) {
        String model = EMFUtil.getModelName(container.eClass());
        String providerName = container.getId();
        String serviceName = reference.getName();

        accumulator.addService(model, providerName, serviceName);

        EMFUtil.streamAttributes(service.eClass())
                .filter(ea -> service.eIsSet(ea) || service.getMetadata().containsKey(ea)).forEach(ea -> {
                    checkMetadata(service, ea);
                    Metadata metadata = service.getMetadata().get(ea);
                    accumulator.addResource(model, providerName, serviceName, ea.getName());
                    accumulator.resourceValueUpdate(model, providerName, serviceName, ea.getName(),
                            ea.getEAttributeType().getInstanceClass(), null, service.eGet(ea), metadata.getTimestamp());
                    Map<String, Object> newMetaData = EMFUtil.toEObjectAttributesToMap(metadata, true,
                            MetadataPackage.Literals.NEXUS_METADATA.getEStructuralFeatures(), null, null);
                    newMetaData.put("value", service.eGet(ea));

                    accumulator.metadataValueUpdate(model, providerName, serviceName, ea.getName(), null, newMetaData,
                            metadata.getTimestamp());
                });
    }

    private static void notifyServiceRemove(Provider container, Service value, EReference reference,
            NotificationAccumulator accumulator) {
        String model = EMFUtil.getModelName(container.eClass());
        String providerName = container.getId();
        String serviceName = reference.getName();

        EMFUtil.streamAttributes(value.eClass()).filter(ea -> value.eIsSet(ea)).forEach(ea -> {
            accumulator.resourceValueUpdate(model, providerName, serviceName, ea.getName(),
                    ea.getEAttributeType().getInstanceClass(), null, null, Instant.now());
            accumulator.removeResource(model, providerName, serviceName, ea.getName());
        });
        accumulator.removeService(model, providerName, serviceName);
    }

    private static void checkMetadataRemove(Notification msg) {
        Service service = (Service) msg.getNotifier();
        EAttribute resource = (EAttribute) msg.getFeature();
        service.getMetadata().removeKey(resource);
    }

    protected static void checkMetadata(Service service, EAttribute attribute) {
        if (!service.getMetadata().containsKey(attribute)) {
            ResourceMetadata curMetadata = MetadataFactory.eINSTANCE.createResourceMetadata();
            curMetadata.setTimestamp(Instant.now());
            curMetadata.setOriginalName(attribute.getName());
            service.getMetadata().put(attribute, curMetadata);
        } else {
            Metadata metadata = service.getMetadata().get(attribute);
            if (metadata.eClass() == ProviderPackage.Literals.METADATA) {
                ResourceMetadata curMetadata = MetadataFactory.eINSTANCE.createResourceMetadata();
                curMetadata.setTimestamp(metadata.getTimestamp() == null ? Instant.now() : metadata.getTimestamp());
                curMetadata.setOriginalName(attribute.getName());
                metadata.getExtra().stream().map(EcoreUtil::copy).map(fcm -> {
                    if (fcm.getTimestamp() == null) {
                        fcm.setTimestamp(Instant.now());
                    }
                    return fcm;
                }).forEach(curMetadata.getExtra()::add);
                service.getMetadata().put(attribute, curMetadata);
            }
        }
    }

}
