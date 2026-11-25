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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
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
import org.eclipse.sensinact.core.emf.twin.SensinactEMFProvider;
import org.eclipse.sensinact.core.emf.util.EMFTestUtil;
import org.eclipse.sensinact.core.model.impl.SensinactModelManagerImpl;
import org.eclipse.sensinact.core.model.nexus.ModelNexus;
import org.eclipse.sensinact.core.notification.impl.NotificationAccumulator;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.model.core.provider.Admin;
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
 * A provider can hold a service instance whose EClass is a <em>subclass</em> of
 * the declared service type - {@code TestAdmin extends Admin} is the case the
 * gateway's own {@code EMFUpdateServiceTest.admin()} and
 * {@code NexusTest.AdminTests} already cover. The extra attribute of that
 * subclass is a resource, and the three read paths of the twin disagree about
 * whether it exists:
 *
 * <table>
 * <caption>the three lookups</caption>
 * <tr>
 * <th>lookup</th>
 * <th>service EClass it uses</th>
 * <th>finds it</th>
 * </tr>
 * <tr>
 * <td>{@code SensinactDigitalTwin.getResource(provider, service, resource)},
 * which {@code ResourceCommand} uses</td>
 * <td>{@code Provider.getServiceEClass(name)} - the <b>instance</b></td>
 * <td>yes</td>
 * </tr>
 * <tr>
 * <td>{@code SensinactProvider.getServices()} and {@code .getResource(…)}</td>
 * <td>{@code ModelNexus.getDefinedServiceForProvider} - the
 * <b>declaration</b></td>
 * <td>no</td>
 * </tr>
 * <tr>
 * <td>{@code twin.snapshotResource(…)}: SensorThings, the northbound provider
 * description, resource filters</td>
 * <td>{@code (EClass) sf.getEType()} - the <b>declaration</b></td>
 * <td>no</td>
 * </tr>
 * </table>
 *
 * <p>
 * The two declaration-based paths answer {@code null}, which a caller cannot
 * tell from "no such resource". The value also travels on the typed-event bus,
 * because the notifications stream the <b>instance</b> features, so the history
 * engine records a resource that the northbound description does not show.
 *
 * <p>
 * {@code snapshotResource} is inconsistent with itself as well: it finds the
 * <em>service</em> through {@code getServiceInstancesForProvider}, the
 * instance, and then looks for the <em>resource</em> in
 * {@code sf.getEType()}, the declaration.
 *
 * <p>
 * The first test is the premise and passes today. The others fail.
 */
@ExtendWith(MockitoExtension.class)
public class ServiceSubclassResourceVisibilityTest {

    private static final String PROVIDER = "sensor";
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
    private SensinactDigitalTwinImpl twinImpl;
    private EPackage ePackage;

    @BeforeEach
    void start() throws IOException {
        resourceSet = EMFTestUtil.createResourceSet();
        nexus = new ModelNexus(resourceSet, ProviderPackage.eINSTANCE, () -> accumulator, actionHandler);
        new SensinactModelManagerImpl(nexus);
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

    /**
     * A {@code TemperatureSensor} whose admin is a {@code TestAdmin}. The subclass
     * arrives as an <em>instance</em>, which is the one route that retypes an
     * admin, and the model does not redeclare the admin reference - exactly the
     * shape of {@code EMFUpdateServiceTest.admin()}.
     */
    private SensinactEMFProvider providerWithAdminSubclass() {
        Provider sensor = (Provider) EcoreUtil.create((EClass) ePackage.getEClassifier("TemperatureSensor"));
        sensor.setId(PROVIDER);

        Admin testAdmin = (Admin) EcoreUtil.create((EClass) ePackage.getEClassifier("TestAdmin"));
        testAdmin.setFriendlyName(PROVIDER);
        testAdmin.eSet(testAdmin.eClass().getEStructuralFeature(EXTRA_ADMIN_RESOURCE), EXTRA_ADMIN_VALUE);
        sensor.setAdmin(testAdmin);

        nexus.save(sensor);

        SensinactEMFProvider provider = twinImpl.getProvider(PROVIDER);
        assertNotNull(provider);
        return provider;
    }

    /**
     * The premise, and it passes today: the resource really exists, it carries the
     * pushed value, and the instance-based lookup reads it.
     */
    @Test
    void theInstanceBasedLookup_findsTheExtraAdminResource() throws Exception {
        providerWithAdminSubclass();

        SensinactResource resource = twinImpl.getResource(PROVIDER, ADMIN, EXTRA_ADMIN_RESOURCE);
        assertNotNull(resource, "twin.getResource did not find the extra admin resource");
        assertEquals(EXTRA_ADMIN_VALUE, resource.getValue().getValue().getValue());
    }

    /**
     * The same resource, through the provider. It answers {@code null}, which no
     * caller can tell from "no such resource".
     */
    @Test
    void theProviderLookup_mustAlsoFindTheExtraAdminResource() {
        SensinactEMFProvider provider = providerWithAdminSubclass();

        assertNotNull(provider.getResource(ADMIN, EXTRA_ADMIN_RESOURCE),
                "SensinactProvider.getResource resolves the declared service EClass, so it cannot see "
                        + "a resource of the stored subclass");
    }

    /**
     * The service listing of the provider leaves it out for the same reason.
     */
    @Test
    void theServiceListing_mustContainTheExtraAdminResource() {
        SensinactEMFProvider provider = providerWithAdminSubclass();

        assertTrue(provider.getServices().get(ADMIN).getResources().containsKey(EXTRA_ADMIN_RESOURCE),
                "the admin service lists only the five resources of the declared Admin, but the stored "
                        + "instance is a TestAdmin");
    }

    /**
     * And the snapshot, which is what SensorThings, the northbound provider
     * description and the resource filters read.
     */
    @Test
    void theSnapshot_mustAlsoFindTheExtraAdminResource() {
        providerWithAdminSubclass();

        ResourceSnapshot snapshot = twinImpl.snapshotResource(PROVIDER, ADMIN, EXTRA_ADMIN_RESOURCE);
        assertNotNull(snapshot, "snapshotResource finds the service through the instance and the resource "
                + "through the declaration, so it cannot see a resource of the stored subclass");
    }

    /**
     * The same line of {@code snapshotResource} carries a second defect: a service of a
     * {@link DynamicProvider} is no feature of the provider EClass, so
     * {@code nexusProvider.eClass().getEStructuralFeature(serviceName)} is {@code null} and
     * {@code sf.getEType()} throws a {@link NullPointerException} - although the service
     * and the resource both exist and the bulk snapshot paths describe them.
     */
    @Test
    void theSnapshot_ofADynamicServiceResource_mustNotThrow() {
        DynamicProvider sensor = (DynamicProvider) EcoreUtil
                .create((EClass) ePackage.getEClassifier("DynamicTemperatureSensor"));
        sensor.setId(PROVIDER);

        Service extra = (Service) EcoreUtil.create((EClass) ePackage.getEClassifier("TestService1"));
        extra.eSet(extra.eClass().getEStructuralFeature("foo"), "fizz");
        sensor.getServices().put("extra", extra);
        nexus.save(sensor);

        assertNotNull(twinImpl.snapshotResource(PROVIDER, "extra", "foo"),
                "snapshotResource could not describe a resource of a dynamic service");
    }

    /**
     * It is not an admin-only disagreement. {@code TemperatureSensor} declares
     * {@code testService1} as a {@code TestService1}, and the stored instance is a
     * {@code TestServiceWithComplex}, which adds {@code complexTest}.
     */
    @Test
    void theDisagreementIsNotAdminOnly_aServiceSubclassIsReadTheSameWay() {
        Provider sensor = (Provider) EcoreUtil.create((EClass) ePackage.getEClassifier("TemperatureSensor"));
        sensor.setId(PROVIDER);

        Service subType = (Service) EcoreUtil.create((EClass) ePackage.getEClassifier("TestServiceWithComplex"));
        sensor.eSet(sensor.eClass().getEStructuralFeature(DECLARED_SERVICE), subType);
        nexus.save(sensor);

        SensinactEMFProvider provider = twinImpl.getProvider(PROVIDER);
        assertNotNull(provider);

        // the premise: the instance-based lookup sees it
        assertNotNull(twinImpl.getResource(PROVIDER, DECLARED_SERVICE, EXTRA_SERVICE_RESOURCE),
                "twin.getResource did not find the resource of the stored subclass");

        assertNotNull(provider.getResource(DECLARED_SERVICE, EXTRA_SERVICE_RESOURCE),
                "SensinactProvider.getResource cannot see the resource of the stored subclass");
        assertNotNull(twinImpl.snapshotResource(PROVIDER, DECLARED_SERVICE, EXTRA_SERVICE_RESOURCE),
                "snapshotResource cannot see the resource of the stored subclass");
    }
}
