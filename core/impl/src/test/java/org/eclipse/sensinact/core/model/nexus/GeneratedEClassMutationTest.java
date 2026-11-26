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
*   Data In Motion - initial API and implementation
**********************************************************************/
package org.eclipse.sensinact.core.model.nexus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.URIMappingRegistryImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.sensinact.core.command.impl.ActionHandler;
import org.eclipse.sensinact.core.emf.util.EMFTestUtil;
import org.eclipse.sensinact.core.notification.impl.NotificationAccumulator;
import org.eclipse.sensinact.model.core.provider.ProviderPackage;
import org.eclipse.sensinact.model.core.testdata.TestdataPackage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * A feature added at run time to an EClass that has generated Java is never seen by
 * the generated switches, because such an object answers
 * {@code eClass() == eStaticClass()}. {@code eDerivedStructuralFeatureID} then falls
 * through to the open-feature path, which calls back into the custom
 * {@code Service.eIsSet(EStructuralFeature)}, and the two recurse until the stack is
 * gone.
 * <p>
 * A generated EClass is also a singleton of its package, so the damage is not
 * confined to the model that caused it. {@code Admin} is shared by every provider of
 * every model, and the measured consequence of adding one resource to it was a
 * {@link StackOverflowError} out of every later {@code createProvider}, for any
 * model, with the nexus otherwise untouched.
 * <p>
 * These tests therefore assert the <em>refusal</em> rather than the crash. A test
 * that actually added the feature would mutate a static singleton for the rest of the
 * JVM and take the other tests of the fork down with it.
 */
@ExtendWith(MockitoExtension.class)
public class GeneratedEClassMutationTest {

    @Mock
    NotificationAccumulator accumulator;

    @Mock
    ActionHandler actionHandler;

    private ResourceSet resourceSet;
    private ModelNexus nexus;
    private EPackage ePackage;

    @BeforeEach
    void start() throws IOException {
        resourceSet = EMFTestUtil.createResourceSet();
        nexus = new ModelNexus(resourceSet, ProviderPackage.eINSTANCE, () -> accumulator, actionHandler);

        final URI providerPackageURI = URI.createURI(ProviderPackage.eNS_URI);
        URIMappingRegistryImpl.INSTANCE.put(
                URI.createURI("https://eclipse.org/../../../models/src/main/resources/model/sensinact.ecore"),
                providerPackageURI);

        XMLResource.URIHandler handler = new XMLResource.URIHandler() {

            @Override
            public URI deresolve(URI uri) {
                return uri;
            }

            @Override
            public URI resolve(URI uri) {
                if (uri.lastSegment().equals("sensinact.ecore")) {
                    return providerPackageURI.appendFragment(uri.fragment());
                }
                return uri;
            }

            @Override
            public void setBaseURI(URI uri) {
                // nothing to do
            }
        };

        Resource extendedPackageResource = resourceSet
                .createResource(URI.createURI("https://eclipse.org/sensinact/test/1.0"));
        InputStream in = getClass().getResourceAsStream("/model/extended.ecore");
        assertNotNull(in);
        extendedPackageResource.load(in, Collections.singletonMap(XMLResource.OPTION_URI_HANDLER, handler));
        ePackage = (EPackage) extendedPackageResource.getContents().get(0);
        assertNotNull(ePackage);
    }

    /**
     * The one that broke the whole runtime: a resource on the shared {@code Admin}.
     */
    @Test
    void createResource_onTheSharedAdmin_isRefused() {
        int before = ProviderPackage.Literals.ADMIN.getEAllStructuralFeatures().size();

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> nexus.createResource(
                ProviderPackage.Literals.ADMIN, "viewport", String.class, Instant.now(), null, Map.of(), false, 0,
                false, 0, 1));
        assertTrue(e.getMessage().contains("Admin"), e.getMessage());

        assertEquals(before, ProviderPackage.Literals.ADMIN.getEAllStructuralFeatures().size(),
                "the shared Admin EClass was modified");
    }

    /**
     * An action resource travels a different method to the same EClass.
     */
    @Test
    void createActionResource_onTheSharedAdmin_isRefused() {
        int before = ProviderPackage.Literals.ADMIN.getEOperations().size();

        assertThrows(IllegalArgumentException.class, () -> nexus.createActionResource(ProviderPackage.Literals.ADMIN,
                "doIt", String.class, List.of(), Map.of(), 0, 1));

        assertEquals(before, ProviderPackage.Literals.ADMIN.getEOperations().size(),
                "the shared Admin EClass was modified");
    }

    /**
     * It is not only the core package. Any generated service EClass has generated
     * switches that a run time feature would escape.
     */
    @Test
    void createResource_onAGeneratedServiceEClass_isRefused() {
        int before = TestdataPackage.Literals.TEST_TEMPERATUR.getEAllStructuralFeatures().size();

        assertThrows(IllegalArgumentException.class,
                () -> nexus.createResource(TestdataPackage.Literals.TEST_TEMPERATUR, "v3", String.class, Instant.now(),
                        null, Map.of(), false, 0, false, 0, 1));

        assertEquals(before, TestdataPackage.Literals.TEST_TEMPERATUR.getEAllStructuralFeatures().size(),
                "the generated service EClass was modified");
    }

    /**
     * The same for a service reference on a generated provider EClass.
     */
    @Test
    void createService_onAGeneratedProviderEClass_isRefused() {
        int before = TestdataPackage.Literals.TEST_SENSOR.getEAllReferences().size();

        assertThrows(IllegalArgumentException.class,
                () -> nexus.createService(TestdataPackage.Literals.TEST_SENSOR, "extra", "Extra", Instant.now()));

        assertEquals(before, TestdataPackage.Literals.TEST_SENSOR.getEAllReferences().size(),
                "the generated provider EClass was modified");
    }

    /**
     * The guard must not touch the ordinary case. A model built at run time has no
     * generated Java, and extending it is what the data first and code first styles
     * do on every unknown resource.
     */
    @Test
    void createResource_onARuntimeModel_isAllowed() {
        Instant now = Instant.now();
        EClass model = nexus.createModel("runtimemodel", now);
        EClass service = nexus.createService(model, "svc", "Svc", now).getEReferenceType();

        assertNotNull(nexus.createResource(service, "value", String.class, now, null, Map.of(), false, 0, false, 0, 1));
        assertNotNull(service.getEStructuralFeature("value"));
    }

    /**
     * And it must not block the supported way to give a core service extra resources:
     * a sub type of the generated EClass, which has no generated Java of its own.
     */
    @Test
    void createResource_onASubTypeOfTheSharedAdmin_isAllowed() {
        EClass testAdmin = (EClass) ePackage.getEClassifier("TestAdmin");
        assertTrue(ProviderPackage.Literals.ADMIN.isSuperTypeOf(testAdmin));
        int sharedBefore = ProviderPackage.Literals.ADMIN.getEAllStructuralFeatures().size();

        assertNotNull(nexus.createResource(testAdmin, "viewport", String.class, Instant.now(), null, Map.of(), false, 0,
                false, 0, 1));

        assertNotNull(testAdmin.getEStructuralFeature("viewport"));
        assertEquals(sharedBefore, ProviderPackage.Literals.ADMIN.getEAllStructuralFeatures().size(),
                "the feature landed on the shared Admin instead of the sub type");
    }

    /**
     * An Ecore model may name an instance class that has no Java on the class path.
     * {@code getInstanceClassName()} is then set and {@link EClass#getInstanceClass()}
     * is {@code null}, the EPackage factory builds a dynamic {@code EObject}, and the
     * recursion the guard exists for cannot happen. Keying the guard on the name
     * refused this model.
     */
    @Test
    void createResource_onAnEClassWhoseInstanceClassIsAbsent_isAllowed() {
        EClass ghost = (EClass) ePackage.getEClassifier("GhostService");
        assertNotNull(ghost.getInstanceClassName(), "the test model no longer names an instance class");
        assertNull(ghost.getInstanceClass(), "the named class is on the class path, the test proves nothing");

        assertNotNull(
                nexus.createResource(ghost, "added", String.class, Instant.now(), null, Map.of(), false, 0, false, 0,
                        1));

        EStructuralFeature added = ghost.getEStructuralFeature("added");
        assertNotNull(added);

        // The stated cause, pinned: an instance resolves the run time feature
        // dynamically instead of recursing through the generated switches.
        EObject instance = EcoreUtil.create(ghost);
        assertEquals(ghost, instance.eClass());
        instance.eSet(added, "a value");
        assertTrue(instance.eIsSet(added));
        assertEquals("a value", instance.eGet(added));
    }

    /**
     * An instance class that resolves to something which is not an {@code EObject}
     * describes a wrapped Java type, not generated model code. Its instances are
     * dynamic in the same way, so the feature is allowed.
     */
    @Test
    void createResource_onAnEClassWrappingANonEObjectType_isAllowed() {
        EClass wrapper = (EClass) ePackage.getEClassifier("WrapperService");
        assertEquals(Runnable.class, wrapper.getInstanceClass());

        assertNotNull(nexus.createResource(wrapper, "added", String.class, Instant.now(), null, Map.of(), false, 0,
                false, 0, 1));

        assertNotNull(wrapper.getEStructuralFeature("added"));
    }
}
