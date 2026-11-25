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
package org.eclipse.sensinact.core.twin.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Collections;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.URIMappingRegistryImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.sensinact.core.command.impl.ActionHandler;
import org.eclipse.sensinact.core.emf.model.EMFModel;
import org.eclipse.sensinact.core.emf.model.EMFService;
import org.eclipse.sensinact.core.emf.twin.SensinactEMFProvider;
import org.eclipse.sensinact.core.emf.twin.SensinactEMFService;
import org.eclipse.sensinact.core.emf.util.EMFTestUtil;
import org.eclipse.sensinact.core.model.impl.SensinactModelManagerImpl;
import org.eclipse.sensinact.core.model.nexus.ModelNexus;
import org.eclipse.sensinact.core.notification.impl.NotificationAccumulator;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.model.core.provider.DynamicProvider;
import org.eclipse.sensinact.model.core.provider.Provider;
import org.eclipse.sensinact.model.core.provider.ProviderPackage;
import org.eclipse.sensinact.model.core.provider.Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.util.promise.PromiseFactory;

/**
 * Two APIs take an {@link EClass} for a service, and these tests pin what each of
 * them does with it.
 *
 * <ul>
 * <li>{@code SensinactEMFProvider.getOrCreateService(name, eClass)} honours a sub
 * type of the EClass the service has, by creating or retyping the stored instance
 * with it. That is the supported way to give one provider's service - the admin
 * included - resources that its declaration does not list. The EClass the service
 * already has, and any super type of it, change nothing, and an unrelated EClass is
 * refused.</li>
 * <li>{@code EMFModel.createServiceWithEClass(name, eClass)} builds an unattached
 * handle and writes nothing into the model, so it cannot retype a service name the
 * model declares and refuses one. {@code admin} is declared by {@code Provider} for
 * every model there is, and its declaration belongs to the shared provider package,
 * so no model-level API can retype it.</li>
 * </ul>
 *
 * Which resources a service has is read from the stored instance, so a service
 * created or retyped here is read back in full through every path -
 * {@code SensinactDigitalTwin.getResource}, {@code SensinactProvider.getResource},
 * the service listing and the snapshots.
 */
@ExtendWith(MockitoExtension.class)
public class DeclaredServiceEClassTest {

    private static final String TEST_MODEL = "testmodel";
    private static final String TEST_PROVIDER = "testprovider";
    private static final String ADMIN = "admin";

    private static final String EXTRA_ADMIN_RESOURCE = "testAdmin";
    private static final BigInteger EXTRA_ADMIN_VALUE = new BigInteger("1000");

    private static final String DECLARED_SERVICE = "testService1";
    private static final String EXTRA_SERVICE_RESOURCE = "complexTest";

    @Mock
    NotificationAccumulator accumulator;

    @Mock
    ActionHandler actionHandler;

    private final PromiseFactory promiseFactory = new PromiseFactory(PromiseFactory.inlineExecutor());

    private ResourceSet resourceSet;
    private ModelNexus nexus;
    private SensinactModelManagerImpl manager;
    private SensinactDigitalTwinImpl twinImpl;
    private EPackage ePackage;

    @BeforeEach
    void start() throws IOException {
        resourceSet = EMFTestUtil.createResourceSet();
        nexus = new ModelNexus(resourceSet, ProviderPackage.eINSTANCE, () -> accumulator, actionHandler);
        manager = new SensinactModelManagerImpl(nexus);
        twinImpl = new SensinactDigitalTwinImpl(nexus, promiseFactory);

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

    private EClass eClass(String name) {
        return (EClass) ePackage.getEClassifier(name);
    }

    private EClass testAdmin() {
        return eClass("TestAdmin");
    }

    /**
     * Asserts that a resource is visible through every read path, not only the one
     * that resolves the service EClass from the stored instance.
     */
    private void assertReadableEverywhere(SensinactEMFProvider provider, String service, String resource) {
        assertNotNull(twinImpl.getResource(provider.getName(), service, resource),
                "SensinactDigitalTwin.getResource cannot see " + service + "/" + resource);
        assertNotNull(provider.getResource(service, resource),
                "SensinactProvider.getResource cannot see " + service + "/" + resource);
        assertNotNull(twinImpl.snapshotResource(provider.getName(), service, resource),
                "snapshotResource cannot see " + service + "/" + resource);
        assertTrue(provider.getServices().get(service).getResources().containsKey(resource),
                "the service listing does not contain " + service + "/" + resource);
    }

    /**
     * {@code createServiceWithEClass} cannot change what a model declares, so it
     * refuses a service name the model declares rather than returning a handle that
     * leads nowhere. {@code admin} is declared by {@code Provider} for every model.
     */
    @Test
    void createServiceWithEClass_onTheDeclaredAdmin_isRefused() {
        EMFModel model = manager.createModel(TEST_MODEL).build();

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> model.createServiceWithEClass(ADMIN, testAdmin()));
        assertTrue(e.getMessage().contains(ADMIN), "the message does not name the declared service: " + e.getMessage());

        // and nothing was changed on the way out
        assertEquals(ProviderPackage.Literals.ADMIN,
                manager.getModel(TEST_MODEL).getServices().get(ADMIN).getServiceEClass());
        SensinactEMFProvider provider = twinImpl.createProvider(TEST_MODEL, TEST_PROVIDER);
        assertEquals(ProviderPackage.Literals.ADMIN, provider.getEMFProvider().getAdmin().eClass());
    }

    /**
     * The handle is still what it always was for a service name the model does not
     * declare. This is the one gateway caller of the method, {@code SetValueCommand},
     * which pushes a value carrying a {@code DataUpdateDto.serviceEClass}.
     */
    @Test
    void createServiceWithEClass_onAnUndeclaredName_stillBuildsTheHandle() {
        EMFModel model = manager.createModel(TEST_MODEL).build();

        EMFService handle = model.createServiceWithEClass("notDeclared", eClass("TestService1"));

        assertEquals(eClass("TestService1"), handle.getServiceEClass());
        assertTrue(handle.getResources().containsKey("foo"), "the handle does not list the resources of its EClass");
    }

    /**
     * The live provider: {@code getOrCreateService} retypes the stored admin to the
     * sub type, and the resource the sub type adds is then readable through every
     * path. It has no value until it is given one, which a caller can tell from a
     * resource that does not exist.
     */
    @Test
    void getOrCreateService_onTheDeclaredAdmin_retypesTheStoredInstance() throws Exception {
        manager.createModel(TEST_MODEL).build();
        SensinactEMFProvider provider = twinImpl.createProvider(TEST_MODEL, TEST_PROVIDER);

        SensinactEMFService service = provider.getOrCreateService(ADMIN, testAdmin());

        assertEquals(testAdmin(), service.getServiceEClass());
        assertEquals(testAdmin(), provider.getEMFProvider().getAdmin().eClass(), "the stored admin was not retyped");
        assertReadableEverywhere(provider, ADMIN, EXTRA_ADMIN_RESOURCE);

        // it exists and reads as unset, and it takes a value
        SensinactResource resource = twinImpl.getResource(TEST_PROVIDER, ADMIN, EXTRA_ADMIN_RESOURCE);
        TimedValue<?> unset = resource.getValue().getValue();
        assertNull(unset.getValue());
        assertNull(unset.getTimestamp());

        resource.setValue(EXTRA_ADMIN_VALUE, Instant.now()).getValue();
        assertEquals(EXTRA_ADMIN_VALUE,
                twinImpl.getResource(TEST_PROVIDER, ADMIN, EXTRA_ADMIN_RESOURCE).getValue().getValue().getValue());
    }

    /**
     * Retyping keeps what the admin already carried, values and metadata alike. The
     * admin of a new provider holds its friendly name, its model and its package uri,
     * each with a timestamp.
     */
    @Test
    void getOrCreateService_retypingTheAdmin_keepsItsValuesAndMetadata() throws Exception {
        manager.createModel(TEST_MODEL).build();
        SensinactEMFProvider provider = twinImpl.createProvider(TEST_MODEL, TEST_PROVIDER);

        TimedValue<?> before = twinImpl.getResource(TEST_PROVIDER, ADMIN, "friendlyName").getValue().getValue();
        assertEquals(TEST_PROVIDER, before.getValue());
        assertNotNull(before.getTimestamp());

        provider.getOrCreateService(ADMIN, testAdmin());

        assertEquals(TEST_PROVIDER, provider.getEMFProvider().getAdmin().getFriendlyName());
        TimedValue<?> after = twinImpl.getResource(TEST_PROVIDER, ADMIN, "friendlyName").getValue().getValue();
        assertEquals(before.getValue(), after.getValue(), "the retyped admin lost its friendly name");
        assertEquals(before.getTimestamp(), after.getTimestamp(), "the retyped admin lost its metadata");
        assertEquals(TEST_MODEL, provider.getEMFProvider().getAdmin().getModel());
    }

    /**
     * The EClass a service already has, and any super type of it, change nothing. This
     * is what keeps a retyped service retyped: every value push resolves the service
     * EClass from the model, which is the super type, and must not downgrade the
     * instance.
     */
    @Test
    void getOrCreateService_withTheDeclaredEClass_doesNotDowngradeARetypedService() {
        manager.createModel(TEST_MODEL).build();
        SensinactEMFProvider provider = twinImpl.createProvider(TEST_MODEL, TEST_PROVIDER);
        provider.getOrCreateService(ADMIN, testAdmin());

        // the super type, as SetValueCommand passes it for every push
        SensinactEMFService again = provider.getOrCreateService(ADMIN, ProviderPackage.Literals.ADMIN);

        assertEquals(testAdmin(), again.getServiceEClass(), "the service was downgraded to the declared EClass");
        assertEquals(testAdmin(), provider.getEMFProvider().getAdmin().eClass(), "the stored admin was downgraded");
        assertReadableEverywhere(provider, ADMIN, EXTRA_ADMIN_RESOURCE);

        // and the same for the EClass it already has
        assertEquals(testAdmin(), provider.getOrCreateService(ADMIN, testAdmin()).getServiceEClass());
    }

    /**
     * It is not an admin-only concern. {@code testService1} is declared by
     * {@code TemperatureSensor} and does not exist on a fresh provider, so the call
     * really <em>creates</em> the instance, with the sub type it was given.
     */
    @Test
    void getOrCreateService_onADeclaredService_createsItWithTheGivenEClass() {
        EClass subType = eClass("TestServiceWithComplex");

        Provider sensor = (Provider) EcoreUtil.create(eClass("TemperatureSensor"));
        sensor.setId("sensor");
        nexus.save(sensor);

        SensinactEMFProvider provider = twinImpl.getProvider("sensor");
        assertNotNull(provider);

        SensinactEMFService service = provider.getOrCreateService(DECLARED_SERVICE, subType);

        assertEquals(subType, service.getServiceEClass());
        assertEquals(subType, provider.getEMFProvider().getService(DECLARED_SERVICE).eClass(),
                "the stored instance was created with the declared EType");
        assertReadableEverywhere(provider, DECLARED_SERVICE, EXTRA_SERVICE_RESOURCE);
    }

    /**
     * An EClass that is neither a sub type nor a super type of the one the service has
     * cannot be applied, and saying so is the whole point: a caller cannot tell a
     * service that was retyped from one that silently was not.
     */
    @Test
    void getOrCreateService_withAnUnrelatedEClass_isRefused() {
        Provider sensor = (Provider) EcoreUtil.create(eClass("TemperatureSensor"));
        sensor.setId("sensor");
        nexus.save(sensor);

        SensinactEMFProvider provider = twinImpl.getProvider("sensor");
        assertNotNull(provider);

        // on creation of the instance
        IllegalArgumentException onCreate = assertThrows(IllegalArgumentException.class,
                () -> provider.getOrCreateService(DECLARED_SERVICE, eClass("TestService2")));
        assertTrue(onCreate.getMessage().contains("TestService2"), onCreate.getMessage());

        // and on an instance that exists already
        IllegalArgumentException onRetype = assertThrows(IllegalArgumentException.class,
                () -> provider.getOrCreateService(ADMIN, eClass("TestService2")));
        assertTrue(onRetype.getMessage().contains("TestService2"), onRetype.getMessage());
        assertEquals(ProviderPackage.Literals.ADMIN, provider.getEMFProvider().getAdmin().eClass(),
                "the refused call changed the stored admin");
    }

    /**
     * For a name the model does <em>not</em> declare, on a {@link DynamicProvider},
     * the EClass is taken as it stands - it has no declaration to agree with.
     */
    @Test
    void getOrCreateService_onAnUndeclaredName_honoursTheEClass() {
        EClass serviceType = eClass("TestService1");

        DynamicProvider sensor = (DynamicProvider) EcoreUtil.create(eClass("DynamicTemperatureSensor"));
        sensor.setId("dynamicsensor");
        nexus.save(sensor);

        SensinactEMFProvider provider = twinImpl.getProvider("dynamicsensor");
        assertNotNull(provider);

        SensinactEMFService service = provider.getOrCreateService("extra", serviceType);
        assertEquals(serviceType, service.getServiceEClass());

        Provider stored = provider.getEMFProvider();
        if (!(stored instanceof DynamicProvider)) {
            fail("expected a DynamicProvider");
        }
        Service extra = ((DynamicProvider) stored).getServices().get("extra");
        assertNotNull(extra, "the service was not stored");
        assertEquals(serviceType, extra.eClass());
    }
}
