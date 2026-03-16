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
*   Kentyou - initial implementation
**********************************************************************/

package org.eclipse.sensinact.core.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.sensinact.core.annotation.dto.NullAction;
import org.eclipse.sensinact.core.command.AbstractTwinCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.model.core.testdata.ComplexTestSensor;
import org.eclipse.sensinact.model.core.testdata.TestResource;
import org.eclipse.sensinact.model.core.testdata.TestTemperaturWithComplex;
import org.eclipse.sensinact.model.core.testdata.TestdataFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

/**
 * Tests the metadata overlay handling
 */
public class MetadataOverlayTest {

    private static final String PROVIDER = "MetadataOverlayTestProvider";

    @InjectService
    GatewayThread gt;

    @InjectService
    DataUpdate dataUpdate;

    @AfterEach
    void stop(@InjectService GatewayThread gt) throws InvocationTargetException, InterruptedException {
        gt.execute(new AbstractTwinCommand<Void>() {
            @Override
            protected Promise<Void> call(SensinactDigitalTwin twin, PromiseFactory pf) {
                Optional.ofNullable(twin.getProvider(PROVIDER)).ifPresent(SensinactProvider::delete);
                return pf.resolved(null);
            }
        }).getValue();
    }

    @Test
    void testMetadataOverlay() throws InvocationTargetException, InterruptedException {

        // Constants
        final String svcName = "temp";
        final String rcName = "testResource";
        final String metaKey = "answer";

        // Create the EMF provider
        ComplexTestSensor sensor = TestdataFactory.eINSTANCE.createComplexTestSensor();
        sensor.setId(PROVIDER);
        TestTemperaturWithComplex temp = TestdataFactory.eINSTANCE.createTestTemperaturWithComplex();
        TestResource resource = TestdataFactory.eINSTANCE.createTestResource();
        temp.setTestResource(resource);
        sensor.setTemp(temp);

        // Create the provider
        dataUpdate.pushUpdate(sensor).getValue();

        // Check the initial resource metadata
        ProviderSnapshot snapshot = getProviderSnapshot();
        ResourceSnapshot rcSnapshot = snapshot.getResource(svcName, rcName);
        assertNotNull(rcSnapshot, "Resource snapshot is null");
        assertNotNull(rcSnapshot.getMetadata(), "Resource has no metadata");
        assertEquals("42", rcSnapshot.getMetadata().get(metaKey));

        // Set a new metadata value
        GenericDto metadataUpdate = new GenericDto();
        metadataUpdate.provider = PROVIDER;
        metadataUpdate.service = svcName;
        metadataUpdate.resource = rcName;
        metadataUpdate.metadata = Map.of(metaKey, 51L);
        metadataUpdate.timestamp = Instant.now();
        dataUpdate.pushUpdate(metadataUpdate).getValue();

        // Check the updated metadata value
        snapshot = getProviderSnapshot();
        rcSnapshot = snapshot.getResource(svcName, rcName);
        assertNotNull(rcSnapshot, "Resource snapshot is null");
        assertNotNull(rcSnapshot.getMetadata(), "Resource has no metadata");
        assertEquals(51L, rcSnapshot.getMetadata().get(metaKey));

        // Set the metadata value to null
        metadataUpdate = new GenericDto();
        metadataUpdate.provider = PROVIDER;
        metadataUpdate.service = svcName;
        metadataUpdate.resource = rcName;
        Map<String, Object> nullMetadataMap = new HashMap<>();
        nullMetadataMap.put(metaKey, null);
        metadataUpdate.metadata = nullMetadataMap;
        metadataUpdate.nullAction = NullAction.UPDATE;
        metadataUpdate.timestamp = Instant.now();
        dataUpdate.pushUpdate(metadataUpdate).getValue();

        // Check the updated metadata value
        snapshot = getProviderSnapshot();
        rcSnapshot = snapshot.getResource(svcName, rcName);
        assertNotNull(rcSnapshot, "Resource snapshot is null");
        assertNotNull(rcSnapshot.getMetadata(), "Resource has no metadata");
        assertNull(rcSnapshot.getMetadata().get(metaKey), "Metadata value has not been set to null");

        // Remove the metadata overlay
        metadataUpdate = new GenericDto();
        metadataUpdate.provider = PROVIDER;
        metadataUpdate.service = svcName;
        metadataUpdate.resource = rcName;
        metadataUpdate.metadata = nullMetadataMap;
        metadataUpdate.nullAction = NullAction.REMOVE_OVERLAY;
        metadataUpdate.timestamp = Instant.now();
        dataUpdate.pushUpdate(metadataUpdate).getValue();

        // Check the original metadata value is back
        snapshot = getProviderSnapshot();
        rcSnapshot = snapshot.getResource(svcName, rcName);
        assertNotNull(rcSnapshot, "Resource snapshot is null");
        assertNotNull(rcSnapshot.getMetadata(), "Resource has no metadata");
        assertEquals("42", rcSnapshot.getMetadata().get(metaKey));
    }

    private ProviderSnapshot getProviderSnapshot() throws InvocationTargetException, InterruptedException {
        return gt.execute(new AbstractTwinCommand<ProviderSnapshot>() {
            @Override
            protected Promise<ProviderSnapshot> call(SensinactDigitalTwin twin, PromiseFactory pf) {
                ProviderSnapshot snapshot = twin.snapshotProvider(PROVIDER);
                if (snapshot == null) {
                    return pf.failed(new NullPointerException("Provider snapshot is null"));
                }
                return pf.resolved(snapshot);
            }
        }).getValue();
    }
}
