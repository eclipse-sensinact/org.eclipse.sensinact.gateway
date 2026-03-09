/*********************************************************************
* Copyright (c) 2026 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial API and implementation
**********************************************************************/
package org.eclipse.sensinact.core.model.nexus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import java.util.Map;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EModelElement;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.sensinact.core.emf.util.EMFTestUtil;
import org.eclipse.sensinact.core.model.nexus.emf.EMFUtil;
import org.eclipse.sensinact.core.notification.impl.NotificationAccumulator;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.impl.SensinactDigitalTwinImpl;
import org.eclipse.sensinact.model.core.provider.NexusMetadata;
import org.eclipse.sensinact.model.core.provider.Provider;
import org.eclipse.sensinact.model.core.provider.ProviderPackage;
import org.junit.jupiter.api.AssertionFailureBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.util.promise.PromiseFactory;

/**
 * Ensures that instance-level metadata override the model defined ones
 */
@ExtendWith(MockitoExtension.class)
public class MetadataOverlayTest {

    @Mock
    NotificationAccumulator accumulator;

    private ResourceSet resourceSet;

    private PromiseFactory promiseFactory = new PromiseFactory(PromiseFactory.inlineExecutor());

    private ModelNexus nexus;

    private SensinactDigitalTwin twin;

    @BeforeEach
    void start() {
        resourceSet = EMFTestUtil.createResourceSet();
        nexus = new ModelNexus(resourceSet, ProviderPackage.eINSTANCE, () -> accumulator);
        twin = new SensinactDigitalTwinImpl(nexus, promiseFactory);
    }

    @Test
    void testMetadataOverlay() throws Exception {
        // Constants
        final String modelName = "TestModel";
        final String modelPkg = EMFUtil.constructPackageUri(modelName);
        final String providerName = "TestProvider";
        final String providerName2 = "TestProvider2";
        final String svcName = "sensor";
        final String rcTemp = "temperature";
        final String rcHumidity = "humidity";
        final String rcSerial = "serialNumber";
        final String metaUnit = "unit";

        // Create the EMF model
        final Instant now = Instant.now();
        final EClass model = nexus.createModel(modelName, now);
        final EReference svcRef = nexus.createService(model, svcName, svcName, now);
        final EAttribute temperature = nexus.createResource(svcRef.getEReferenceType(), rcTemp, Float.class, now,
                null, Map.of(metaUnit, "°C"), false, 0, false, 0, 1);
        nexus.createResource(svcRef.getEReferenceType(), rcHumidity, Float.class, now, null, Map.of(metaUnit, "%"),
                false,
                0, false, 0, 1);
        nexus.createResource(svcRef.getEReferenceType(), rcSerial, String.class, now, null);

        // Create the two provider instances
        final Provider provider = nexus.createProviderInstance(modelPkg, modelName, providerName, now);
        assertNotNull(provider, "Error creating first provider");

        final Provider provider2 = nexus.createProviderInstance(modelPkg, modelName, providerName2, now);
        assertNotNull(provider2, "Error creating second provider");

        // Check initial metadata values
        ProviderSnapshot snapshot = getProviderSnapshot(providerName);
        assertEquals("°C", snapshot.getResource(svcName, rcTemp).getMetadata().get(metaUnit));
        assertEquals("%", snapshot.getResource(svcName, rcHumidity).getMetadata().get(metaUnit));
        assertNull(snapshot.getResource(svcName, rcSerial).getMetadata().get(metaUnit));

        ProviderSnapshot snapshot2 = getProviderSnapshot(providerName2);
        assertEquals("°C", snapshot2.getResource(svcName, rcTemp).getMetadata().get(metaUnit));
        assertEquals("%", snapshot2.getResource(svcName, rcHumidity).getMetadata().get(metaUnit));
        assertNull(snapshot2.getResource(svcName, rcSerial).getMetadata().get(metaUnit));

        // Update resource metadata at instance level for provider 1
        twin.getResource(providerName, svcName, rcTemp).setMetadataValue(metaUnit, "K", now).getValue();

        // Check updated metadata values
        snapshot = getProviderSnapshot(providerName);
        assertEquals("K", snapshot.getResource(svcName, rcTemp).getMetadata().get(metaUnit));
        assertEquals("%", snapshot.getResource(svcName, rcHumidity).getMetadata().get(metaUnit));
        assertNull(snapshot.getResource(svcName, rcSerial).getMetadata().get(metaUnit));

        snapshot2 = getProviderSnapshot(providerName2);
        assertEquals("°C", snapshot2.getResource(svcName, rcTemp).getMetadata().get(metaUnit));
        assertEquals("%", snapshot2.getResource(svcName, rcHumidity).getMetadata().get(metaUnit));
        assertNull(snapshot2.getResource(svcName, rcSerial).getMetadata().get(metaUnit));

        // Update resource metadata at model level for provider
        getModelResourceMetadata(temperature).getExtra().put(metaUnit,
                EMFUtil.createMetadataValue(Instant.now(), "°F"));

        // ... provider with instance-level override should keep its value
        snapshot = getProviderSnapshot(providerName);
        assertEquals("K", snapshot.getResource(svcName, rcTemp).getMetadata().get(metaUnit));
        assertEquals("%", snapshot.getResource(svcName, rcHumidity).getMetadata().get(metaUnit));
        assertNull(snapshot.getResource(svcName, rcSerial).getMetadata().get(metaUnit));

        // ... provider without instance-level override should get the updated model
        // value
        snapshot2 = getProviderSnapshot(providerName2);
        assertEquals("°F", snapshot2.getResource(svcName, rcTemp).getMetadata().get(metaUnit));
        assertEquals("%", snapshot2.getResource(svcName, rcHumidity).getMetadata().get(metaUnit));
        assertNull(snapshot2.getResource(svcName, rcSerial).getMetadata().get(metaUnit));

        // Remove resource metadata
        twin.getResource(providerName, svcName, rcTemp).setMetadataValue(metaUnit, null, now).getValue();

        // Check metadata values are back to model defaults
        snapshot = getProviderSnapshot(providerName);
        assertEquals("°F", snapshot.getResource(svcName, rcTemp).getMetadata().get(metaUnit));
        assertEquals("%", snapshot.getResource(svcName, rcHumidity).getMetadata().get(metaUnit));
        assertNull(snapshot.getResource(svcName, rcSerial).getMetadata().get(metaUnit));

        snapshot2 = getProviderSnapshot(providerName2);
        assertEquals("°F", snapshot2.getResource(svcName, rcTemp).getMetadata().get(metaUnit));
        assertEquals("%", snapshot2.getResource(svcName, rcHumidity).getMetadata().get(metaUnit));
        assertNull(snapshot2.getResource(svcName, rcSerial).getMetadata().get(metaUnit));

        // Remove unit from model metadata
        getModelResourceMetadata(temperature).getExtra().removeKey(metaUnit);
        snapshot = getProviderSnapshot(providerName);
        assertNull(snapshot.getResource(svcName, rcTemp).getMetadata().get(metaUnit));
        assertEquals("%", snapshot.getResource(svcName, rcHumidity).getMetadata().get(metaUnit));
        assertNull(snapshot.getResource(svcName, rcSerial).getMetadata().get(metaUnit));

        snapshot2 = getProviderSnapshot(providerName2);
        assertNull(snapshot2.getResource(svcName, rcTemp).getMetadata().get(metaUnit));
        assertEquals("%", snapshot2.getResource(svcName, rcHumidity).getMetadata().get(metaUnit));
        assertNull(snapshot2.getResource(svcName, rcSerial).getMetadata().get(metaUnit));
    }

    @Test
    void testMetadataVsDetailsOverlay() throws Exception {
        // Constants
        final String modelName = "TestModel";
        final String modelPkg = EMFUtil.constructPackageUri(modelName);
        final String providerName = "TestProvider";
        final String providerName2 = "TestProvider2";
        final String svcName = "sensor";
        final String rcTemp = "temperature";
        final String rcHumidity = "humidity";
        final String rcSerial = "serialNumber";
        final String metaUnit = "unit";

        // Create the EMF model
        final Instant now = Instant.now();
        final EClass model = nexus.createModel(modelName, now);
        final EReference svcRef = nexus.createService(model, svcName, svcName, now);
        final EAttribute temperature = nexus.createResource(svcRef.getEReferenceType(), rcTemp, Float.class, now,
                null, Map.of(metaUnit, "°C"), false, 0, false, 0, 1);
        nexus.createResource(svcRef.getEReferenceType(), rcHumidity, Float.class, now, null, Map.of(metaUnit, "%"),
                false,
                0, false, 0, 1);
        nexus.createResource(svcRef.getEReferenceType(), rcSerial, String.class, now, null);

        // Create the two provider instances
        final Provider provider = nexus.createProviderInstance(modelPkg, modelName, providerName, now);
        assertNotNull(provider, "Error creating first provider");

        final Provider provider2 = nexus.createProviderInstance(modelPkg, modelName, providerName2, now);
        assertNotNull(provider2, "Error creating second provider");

        // Add unit as detail
        temperature.getEAnnotation(EMFUtil.METADATA_ANNOTATION_SOURCE).getDetails().put(metaUnit, "°C");
        ProviderSnapshot snapshot = getProviderSnapshot(providerName);
        assertEquals("°C", snapshot.getResource(svcName, rcTemp).getMetadata().get(metaUnit));
        assertEquals("%", snapshot.getResource(svcName, rcHumidity).getMetadata().get(metaUnit));
        assertNull(snapshot.getResource(svcName, rcSerial).getMetadata().get(metaUnit));

        ProviderSnapshot snapshot2 = getProviderSnapshot(providerName2);
        assertEquals("°C", snapshot2.getResource(svcName, rcTemp).getMetadata().get(metaUnit));
        assertEquals("%", snapshot2.getResource(svcName, rcHumidity).getMetadata().get(metaUnit));
        assertNull(snapshot2.getResource(svcName, rcSerial).getMetadata().get(metaUnit));

        // Update resource metadata at instance level for provider 1
        twin.getResource(providerName, svcName, rcTemp).setMetadataValue(metaUnit, "°F", now).getValue();

        // ... provider with instance-level override should keep its value
        snapshot = getProviderSnapshot(providerName);
        assertEquals("°F", snapshot.getResource(svcName, rcTemp).getMetadata().get(metaUnit));
        assertEquals("%", snapshot.getResource(svcName, rcHumidity).getMetadata().get(metaUnit));
        assertNull(snapshot.getResource(svcName, rcSerial).getMetadata().get(metaUnit));

        // ... provider without instance-level override should get the updated model
        // value
        snapshot2 = getProviderSnapshot(providerName2);
        assertEquals("°C", snapshot2.getResource(svcName, rcTemp).getMetadata().get(metaUnit));
        assertEquals("%", snapshot2.getResource(svcName, rcHumidity).getMetadata().get(metaUnit));
        assertNull(snapshot2.getResource(svcName, rcSerial).getMetadata().get(metaUnit));

        // Update unit as metadata extra
        getModelResourceMetadata(temperature).getExtra().put(metaUnit, EMFUtil.createMetadataValue(Instant.now(), "K"));
        snapshot = getProviderSnapshot(providerName);
        assertEquals("°F", snapshot.getResource(svcName, rcTemp).getMetadata().get(metaUnit));
        assertEquals("%", snapshot.getResource(svcName, rcHumidity).getMetadata().get(metaUnit));
        assertNull(snapshot.getResource(svcName, rcSerial).getMetadata().get(metaUnit));

        snapshot2 = getProviderSnapshot(providerName2);
        assertEquals("°C", snapshot2.getResource(svcName, rcTemp).getMetadata().get(metaUnit));
        assertEquals("%", snapshot2.getResource(svcName, rcHumidity).getMetadata().get(metaUnit));
        assertNull(snapshot2.getResource(svcName, rcSerial).getMetadata().get(metaUnit));

        // Metadata should have been overridden by details
        assertEquals("°C", getModelResourceMetadata(temperature).getExtra().get(metaUnit).getValue());

        // Remove unit as detail
        temperature.getEAnnotation(EMFUtil.METADATA_ANNOTATION_SOURCE).getDetails().removeKey(metaUnit);
        // Metadata can't change by itself
        assertEquals("°C", getModelResourceMetadata(temperature).getExtra().get(metaUnit).getValue());

        // Update metadata
        getModelResourceMetadata(temperature).getExtra().put(metaUnit, EMFUtil.createMetadataValue(Instant.now(), "K"));

        snapshot = getProviderSnapshot(providerName);
        assertEquals("°F", snapshot.getResource(svcName, rcTemp).getMetadata().get(metaUnit));
        assertEquals("%", snapshot.getResource(svcName, rcHumidity).getMetadata().get(metaUnit));
        assertNull(snapshot.getResource(svcName, rcSerial).getMetadata().get(metaUnit));

        snapshot2 = getProviderSnapshot(providerName2);
        assertEquals("K", snapshot2.getResource(svcName, rcTemp).getMetadata().get(metaUnit));
        assertEquals("%", snapshot2.getResource(svcName, rcHumidity).getMetadata().get(metaUnit));
        assertNull(snapshot2.getResource(svcName, rcSerial).getMetadata().get(metaUnit));

        // Remove unit from model metadata
        getModelResourceMetadata(temperature).getExtra().removeKey(metaUnit);
        snapshot = getProviderSnapshot(providerName);
        assertEquals("°F", snapshot.getResource(svcName, rcTemp).getMetadata().get(metaUnit));
        assertEquals("%", snapshot.getResource(svcName, rcHumidity).getMetadata().get(metaUnit));
        assertNull(snapshot.getResource(svcName, rcSerial).getMetadata().get(metaUnit));

        snapshot2 = getProviderSnapshot(providerName2);
        assertNull(snapshot2.getResource(svcName, rcTemp).getMetadata().get(metaUnit));
        assertEquals("%", snapshot2.getResource(svcName, rcHumidity).getMetadata().get(metaUnit));
        assertNull(snapshot2.getResource(svcName, rcSerial).getMetadata().get(metaUnit));
    }

    private ProviderSnapshot getProviderSnapshot(String providerName) {
        return twin.filteredSnapshot(null,
                p -> p.getName().equals(providerName), null, null).stream().findFirst().orElseThrow(
                        () -> AssertionFailureBuilder.assertionFailure().reason("Provider not found").build());
    }

    private NexusMetadata getModelResourceMetadata(EModelElement element) {
        EAnnotation eAnnotation = element.getEAnnotation(EMFUtil.METADATA_ANNOTATION_SOURCE);
        if (eAnnotation != null) {
            return eAnnotation.getContents().stream().filter(meta -> meta instanceof NexusMetadata)
                    .map(NexusMetadata.class::cast).findFirst().orElseGet(() -> null);
        }
        return null;
    }
}
