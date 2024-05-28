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
package org.eclipse.sensinact.core.model.nexus.emf.compare;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.ETypedElement;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sensinact.core.model.nexus.emf.EMFUtil;
import org.eclipse.sensinact.core.notification.NotificationAccumulator;
import org.eclipse.sensinact.model.core.metadata.MetadataFactory;
import org.eclipse.sensinact.model.core.metadata.MetadataPackage;
import org.eclipse.sensinact.model.core.metadata.ResourceMetadata;
import org.eclipse.sensinact.model.core.provider.Admin;
import org.eclipse.sensinact.model.core.provider.DynamicProvider;
import org.eclipse.sensinact.model.core.provider.FeatureCustomMetadata;
import org.eclipse.sensinact.model.core.provider.Metadata;
import org.eclipse.sensinact.model.core.provider.Provider;
import org.eclipse.sensinact.model.core.provider.ProviderPackage;
import org.eclipse.sensinact.model.core.provider.Service;
import org.eclipse.sensinact.model.core.provider.impl.FeatureCustomMetadataImpl;
import org.eclipse.sensinact.model.core.provider.impl.ServiceMapImpl;

/**
 * Helper to Compare EObjects
 *
 * @author Juergen Albert
 * @since 22 Feb 2023
 */
public class EMFCompareUtil {

    public static void compareAndSet(Provider incmming, Provider original, NotificationAccumulator accumulator) {
        if (incmming == null || original == null) {
            return;
        }

        if (incmming.eClass() != original.eClass()) {
            throw new IllegalArgumentException(
                    String.format("The given incomming Provider %s is of type %s but should be of type %s",
                            incmming.toString(), incmming.eClass().getName(), original.eClass().getName()));
        }

        EClass eClass = incmming.eClass();

        // We can simply set all attributes at the Provider level without any checks as
        // they are out of the
        EMFUtil.streamAttributes(eClass).forEach(ea -> original.eSet(ea, incmming.eGet(ea)));
        // The same goes for any Reference that is not of type Service or is the linked
        // Provider Reference or the map of Services
        eClass.getEAllReferences().stream()
                // We don't want references from EObject and anything above
                .filter(er -> er.getEContainingClass().getEPackage() != EcorePackage.eINSTANCE)
                .filter(Predicate.not(ProviderPackage.Literals.PROVIDER__LINKED_PROVIDERS::equals))
                .filter(er -> !ProviderPackage.Literals.SERVICE.isSuperTypeOf(er.getEReferenceType()))
                .filter(Predicate.not(ProviderPackage.Literals.DYNAMIC_PROVIDER__SERVICES::equals)).forEach(er -> {
                    original.eSet(er,
                            er.isContainment() ? EcoreUtil.copy((EObject) incmming.eGet(er)) : incmming.eGet(er));
                });

        updateAdmin(incmming, original, accumulator);

        eClass.getEAllReferences().stream()
                // We don't want references from EObject and anything above
                .filter(er -> er.getEContainingClass().getEPackage() != EcorePackage.eINSTANCE)
                .filter(Predicate.not(ProviderPackage.Literals.PROVIDER__LINKED_PROVIDERS::equals))
                .filter(Predicate.not(ProviderPackage.Literals.PROVIDER__ADMIN::equals))
                .filter(Predicate.not(ProviderPackage.Literals.DYNAMIC_PROVIDER__SERVICES::equals))
                .filter(er -> ProviderPackage.Literals.SERVICE.isSuperTypeOf(er.getEReferenceType())).forEach(er -> {
                    serviceUpdate(er, incmming, original, accumulator, Collections.emptyList());
                });
        if (incmming instanceof DynamicProvider) {
            servicesMapUpdate((DynamicProvider) incmming, (DynamicProvider) original, accumulator,
                    Collections.emptyList());
        }
    }

    private static void updateAdmin(Provider incomming, Provider original, NotificationAccumulator accumulator) {
        Admin newService = incomming.getAdmin();
        if (newService != null) {
            serviceUpdate(ProviderPackage.Literals.PROVIDER__ADMIN, incomming, original, accumulator,
                    List.of(ProviderPackage.Literals.ADMIN__MODEL, ProviderPackage.Literals.ADMIN__MODEL_PACKAGE_URI));
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
            notifyServiceAdd(original, copy, reference.getName(), accumulator);
        } else if (newService == null && oldService != null) {
            notifyServiceRemove(original, oldService, reference.getName(), accumulator);
            original.eUnset(reference);
        } else {
            if (newService.eClass() != oldService.eClass()) {
                if (oldService.eClass().isSuperTypeOf(newService.eClass())) {
                    oldService = copyOldService(oldService, newService.eClass());
                    original.eSet(reference, oldService);
                } else {
                    throw new RuntimeException("Merging Services of different Types is not possible."
                            + newService.eClass().getName() + " must be a subtype of " + oldService.eClass().getName());
                }
            }
            mergeAndNotify(reference.getName(), newService, oldService, blackList, accumulator);
        }
    }

    private static void servicesMapUpdate(DynamicProvider incomming, DynamicProvider original,
            NotificationAccumulator accumulator, List<EStructuralFeature> blackList) {

        List<String> toDelete = new ArrayList<>(original.getServices().keySet());
        List<String> toAdd = new ArrayList<>(incomming.getServices().keySet());
        List<String> toUpdate = new ArrayList<>();

        for (Iterator<String> iterator = toAdd.iterator(); iterator.hasNext();) {
            String serviceName = iterator.next();
            if (toDelete.remove(serviceName)) {
                iterator.remove();
                toUpdate.add(serviceName);
            }
        }

        for (String serviceName : toAdd) {
            Service copy = EcoreUtil.copy(incomming.getServices().get(serviceName));
            original.getServices().put(serviceName, copy);
            notifyServiceAdd(original, copy, serviceName, accumulator);
        }
        for (String serviceName : toDelete) {
            Service oldService = original.getServices().removeKey(serviceName);
            notifyServiceRemove(original, oldService, serviceName, accumulator);
        }
        for (String serviceName : toUpdate) {
            Service newService = incomming.getServices().get(serviceName);
            Service oldService = original.getServices().get(serviceName);
            if (newService.eClass() != oldService.eClass()) {
                if (oldService.eClass().isSuperTypeOf(newService.eClass())) {
                    oldService = copyOldService(oldService, newService.eClass());
                    original.getServices().put(serviceName, oldService);
                } else {
                    throw new RuntimeException("Merging Services of different Types is not possible."
                            + newService.eClass().getName() + " must be a subtype of " + oldService.eClass().getName());
                }
            }
            mergeAndNotify(serviceName, newService, oldService, blackList, accumulator);
        }
    }

    /**
     * Copies the given Service in a new {@link EObject} created from the given
     * {@link EClass}
     *
     * @param oldService the service top copy from
     * @param eClass     the EClass. Must be a subtype of the oldServices
     *                   {@link EClass}
     * @return the copied service
     */
    private static Service copyOldService(Service oldService, EClass eClass) {
        Service eObject = (Service) EcoreUtil.create(eClass);
        oldService.eClass().getEAllStructuralFeatures().forEach(e -> eObject.eSet(e, oldService.eGet(e)));
        return eObject;
    }

//    private static void mergeAndNotify(EReference reference, Service newService, Service originalService,
//            NotificationAccumulator accumulator) {
//        mergeAndNotify(reference, newService, originalService, Collections.emptyList(), accumulator);
//    }

    private static void mergeAndNotify(String serviceName, Service newService, Service originalService,
            List<EStructuralFeature> blackList, NotificationAccumulator accumulator) {

        if (newService.eClass() != originalService.eClass()) {
            throw new UnsupportedOperationException("Merging Services of different Tyoes is not supported yet");
        }

        // we can simply copy all non containments, as they are out of scope for
        // notifications
        originalService.eClass().getEAllReferences().stream()
                // We don't want references from EObject and anything above
                .filter(er -> er.getEContainingClass().getEPackage() != EcorePackage.eINSTANCE)
                .filter(Predicate.not(ProviderPackage.Literals.SERVICE__METADATA::equals))
                .filter(Predicate.not(EReference::isContainment)).filter(Predicate.not(blackList::contains))
                .forEach(er -> {
                    originalService.eSet(er, newService.eGet(er));
                });

        originalService.eClass().getEAllStructuralFeatures().stream()
                // We don't want references from EObject and anything above
                .filter(er -> er.getEContainingClass().getEPackage() != EcorePackage.eINSTANCE)
                .filter(Predicate.not(blackList::contains))
                .filter(Predicate.not(ProviderPackage.Literals.SERVICE__METADATA::equals))
                .filter(f -> f instanceof EReference ? ((EReference) f).isContainment() : true).forEach(er -> {
                    notifyResourceChange(er, serviceName, newService, originalService, accumulator);
                });

    }

    // 1. Attribute is new; use their timestamp if present or now if not
    // 2. Attribute changed; no timestamp change in diff; update attribute, keep
    // timestamp
    // 3. Attribute changed; timestamp change to null; update with Timestamp Now
    // 4. Attribute changed; timestamp changed; update attribute and timestamp if
    // new is after old timetamp
    // 5. Attribute not changed, but timestamp updated: same as 4.
    @SuppressWarnings("unchecked")
    private static void notifyResourceChange(EStructuralFeature resource, String serviceName, Service newService,
            Service originalService, NotificationAccumulator accumulator) {
        EObject container = originalService.eContainer();
        if (container instanceof ServiceMapImpl) {
            container = container.eContainer();
        }
        if (container instanceof Provider) {
            String packageUri = container.eClass().getEPackage().getNsURI();
            String modelName = EMFUtil.getModelName(container.eClass());
            String providerName = ((Provider) container).getId();
            Metadata originalMetadata = originalService.getMetadata().get(resource);

            boolean isNew = !originalService.eIsSet(resource);

            Object oldValue = originalService.eGet(resource);
            Object newValue = newService.eGet(resource);

            Instant previousTimestamp = null;
            Instant newTimestamp = getNewTimestampFromMetadata(resource, newService);
            if (originalMetadata != null) {
                previousTimestamp = originalMetadata.getTimestamp();
            }
            boolean isEqual = false;
            if (resource instanceof EReference) {
                if (resource.isMany()) {
                    isEqual = EcoreUtil.equals((List<EObject>) oldValue, (List<EObject>) newValue);
                    if (!isEqual) {
                        oldValue = EcoreUtil.copyAll((List<EObject>) oldValue);
                        newValue = EcoreUtil.copyAll((List<EObject>) newValue);
                    }
                } else {
                    isEqual = EcoreUtil.equals((EObject) oldValue, (EObject) newValue);
                    if (!isEqual) {
                        oldValue = EcoreUtil.copy((EObject) oldValue);
                        newValue = EcoreUtil.copy((EObject) newValue);
                    }
                }
            } else {
                isEqual = Objects.equals(oldValue, newValue);
            }
            if (isEqual && Objects.equals(previousTimestamp, newTimestamp)) {
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
            if (isNew) {
                accumulator.addResource(packageUri, modelName, providerName, serviceName, resource.getName());
            }

            Map<String, Object> oldMetaData = null;

            if (previousTimestamp != null && !previousTimestamp.equals(Instant.EPOCH)) {
                oldMetaData = extractMetadataMap(oldValue, originalMetadata, resource);
            }

            Metadata updatedMetadata = updateMetadata(resource, serviceName, newService, originalService, newTimestamp);

            originalService.eSet(resource, newValue);

            accumulator.resourceValueUpdate(packageUri, modelName, providerName, serviceName, resource.getName(),
                    resource.getEType().getInstanceClass(), oldValue, newValue, newTimestamp);

            Map<String, Object> newMetaData = extractMetadataMap(newValue, updatedMetadata, resource);

            accumulator.metadataValueUpdate(packageUri, modelName, providerName, serviceName, resource.getName(),
                    oldMetaData, newMetaData, newTimestamp);

            if (newValue == null) {
                accumulator.removeResource(packageUri, modelName, providerName, serviceName, resource.getName());
            }
        }

    }

    public static Map<String, Object> extractMetadataMap(Object value, Metadata updatedMetadata,
            ETypedElement feature) {
        Map<String, Object> newMetaData = EMFUtil.toMetadataAttributesToMap(updatedMetadata, feature);
        newMetaData.put("value", value);
        return newMetaData;
    }

    private static ResourceMetadata updateMetadata(EStructuralFeature resource, String serviceName, Service newService,
            Service originalService, Instant newTimestamp) {
        ResourceMetadata resourceMetadata = checkMetadata(originalService, resource);
        resourceMetadata.setTimestamp(newTimestamp);
        Metadata update = newService.getMetadata().get(resource);
        if (update != null && update.eIsSet(ProviderPackage.Literals.METADATA__EXTRA)) {
            updateExtraMetadata(update.getExtra(), resourceMetadata.getExtra(), newTimestamp);
        }

        return resourceMetadata;
    }

    private static void updateExtraMetadata(EList<FeatureCustomMetadata> extraNew,
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

    private static Instant getNewTimestampFromMetadata(EStructuralFeature resource, Service service) {
        Metadata metadata = service.getMetadata().get(resource);
        if (metadata != null) {
            if (metadata.getTimestamp() != null) {
                return metadata.getTimestamp();
            }
        }
        return null;
    }

    private static void notifyServiceAdd(Provider container, Service service, String serviceName,
            NotificationAccumulator accumulator) {
        String packageUri = container.eClass().getEPackage().getNsURI();
        String model = EMFUtil.getModelName(container.eClass());
        String providerName = container.getId();

        accumulator.addService(packageUri, model, providerName, serviceName);

        EMFUtil.streamAttributes(service.eClass()).filter(ea -> service.eIsSet(ea)).forEach(ea -> {
            checkMetadata(service, ea);
            Metadata metadata = service.getMetadata().get(ea);
            accumulator.addResource(packageUri, model, providerName, serviceName, ea.getName());
            accumulator.resourceValueUpdate(packageUri, model, providerName, serviceName, ea.getName(),
                    ea.getEAttributeType().getInstanceClass(), null, service.eGet(ea), metadata.getTimestamp());
            Map<String, Object> newMetaData = EMFUtil.toEObjectAttributesToMap(metadata, true,
                    MetadataPackage.Literals.NEXUS_METADATA.getEStructuralFeatures(), null, null);
            newMetaData.put("value", service.eGet(ea));

            accumulator.metadataValueUpdate(packageUri, model, providerName, serviceName, ea.getName(), null,
                    newMetaData, metadata.getTimestamp());
        });
    }

    private static void notifyServiceRemove(Provider container, Service value, String serviceName,
            NotificationAccumulator accumulator) {
        String packageUri = container.eClass().getEPackage().getNsURI();
        String model = EMFUtil.getModelName(container.eClass());
        String providerName = container.getId();

        EMFUtil.streamAttributes(value.eClass()).filter(ea -> value.eIsSet(ea)).forEach(ea -> {
            accumulator.resourceValueUpdate(packageUri, model, providerName, serviceName, ea.getName(),
                    ea.getEAttributeType().getInstanceClass(), null, null, Instant.now());
            accumulator.removeResource(packageUri, model, providerName, serviceName, ea.getName());
        });
        accumulator.removeService(packageUri, model, providerName, serviceName);
    }

//    private static void checkMetadataRemove(Notification msg) {
//        Service service = (Service) msg.getNotifier();
//        EAttribute resource = (EAttribute) msg.getFeature();
//        service.getMetadata().removeKey(resource);
//    }

    protected static ResourceMetadata checkMetadata(Service service, EStructuralFeature attribute) {
        ResourceMetadata result = null;
        if (!service.getMetadata().containsKey(attribute)) {
            result = MetadataFactory.eINSTANCE.createResourceMetadata();
            result.setTimestamp(Instant.now());
            result.setOriginalName(attribute.getName());
            service.getMetadata().put(attribute, result);
        } else {
            Metadata metadata = service.getMetadata().get(attribute);
            if (metadata.eClass() == ProviderPackage.Literals.METADATA) {
                result = MetadataFactory.eINSTANCE.createResourceMetadata();
                result.setTimestamp(metadata.getTimestamp() == null ? Instant.now() : metadata.getTimestamp());
                result.setOriginalName(attribute.getName());
                final ResourceMetadata curMetadata = result;
                metadata.getExtra().stream().map(FeatureCustomMetadataImpl.class::cast).map(EcoreUtil::copy)
                        .map(fcm -> {
                            if (fcm.getTimestamp() == null) {
                                fcm.setTimestamp(Instant.now());
                            }
                            removeByName(fcm.getName(), curMetadata.getExtra());
                            return fcm;
                        }).forEach(result.getExtra()::add);
                service.getMetadata().put(attribute, result);
            } else {
                result = (ResourceMetadata) metadata;
            }
        }
        return result;
    }

}
