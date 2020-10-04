/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.core.security.test;

import java.util.Dictionary;

import org.eclipse.sensinact.gateway.core.ModelElement;
import org.eclipse.sensinact.gateway.core.SensiNactResourceModel;
import org.eclipse.sensinact.gateway.core.security.AuthenticationService;
import org.eclipse.sensinact.gateway.core.security.AuthorizationService;
import org.junit.Before;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.InvalidServiceProviderException;
import org.eclipse.sensinact.gateway.core.message.SnaAgent;
import org.eclipse.sensinact.gateway.core.message.MessageRouter;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreService;

/**
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class TestSecuredAccess {

	// ********************************************************************//
	// NESTED DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// ABSTRACT DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// STATIC DECLARATIONS //
	// ********************************************************************//

	private static final String LOG_FILTER = "(" + Constants.OBJECTCLASS + "=" + LogService.class.getCanonicalName()
			+ ")";

	private static final String MESSAGE_HANDLER_FILTER = "(&(" + Constants.OBJECTCLASS + "="
			+ MessageRouter.class.getCanonicalName() + ")(uri=/serviceProvider)";

	private static final String DATA_STORE_FILTER = "(" + Constants.OBJECTCLASS + "="
			+ DataStoreService.class.getCanonicalName() + ")";

	private static final String AUTHENTICATION_FILTER = "(" + Constants.OBJECTCLASS + "="
			+ AuthenticationService.class.getCanonicalName() + ")";

	private static final String AGENT_FILTER = "(" + Constants.OBJECTCLASS + "=" + SnaAgent.class.getCanonicalName()
			+ ")";

	private static final String AUTHORIZATION_FILTER = "(" + Constants.OBJECTCLASS + "="
			+ AuthorizationService.class.getCanonicalName() + ")";

	private static final String MOCK_BUNDLE_NAME = "MockedBundle";
	private static final long MOCK_BUNDLE_ID = 1;

	// ********************************************************************//
	// INSTANCE DECLARATIONS //
	// ********************************************************************//

	private final Filter filterHandler = Mockito.mock(Filter.class);
	private final Filter filterAgent = Mockito.mock(Filter.class);
	private final Filter filterDataStore = Mockito.mock(Filter.class);
	private final Filter filterAuthentication = Mockito.mock(Filter.class);
	private final Filter filterAuthorization = Mockito.mock(Filter.class);

	private final BundleContext context = Mockito.mock(BundleContext.class);
	private final Bundle bundle = Mockito.mock(Bundle.class);

	private final ServiceReference referenceHandler = Mockito.mock(ServiceReference.class);
	private final ServiceReference referenceAgent = Mockito.mock(ServiceReference.class);
	private final ServiceRegistration registrationHandler = Mockito.mock(ServiceRegistration.class);
	private final ServiceRegistration snaObjectRegistration = Mockito.mock(ServiceRegistration.class);
	private final ServiceReference referenceAuthorization = Mockito.mock(ServiceReference.class);
	private final ServiceRegistration registration = Mockito.mock(ServiceRegistration.class);
	private final ServiceRegistration registrationAgent = Mockito.mock(ServiceRegistration.class);
	private final ServiceReference referenceProvider = Mockito.mock(ServiceReference.class);

	private Mediator mediator;
	private SensiNactResourceModel provider;
	private MessageRouter handler;
	private SnaAgent agent;

	private int callbackCount;
	private int linkCallbackCount;
	private int agentCallbackCount;

	@Before
	public void init() throws InvalidServiceProviderException, InvalidSyntaxException {
		Filter filter = Mockito.mock(Filter.class);
		Mockito.when(filter.toString()).thenReturn(LOG_FILTER);

		Mockito.when(context.createFilter(LOG_FILTER)).thenReturn(filter);
		Mockito.when(context.getServiceReferences((String) Mockito.eq(null), Mockito.eq(LOG_FILTER))).thenReturn(null);
		Mockito.when(context.getServiceReference(LOG_FILTER)).thenReturn(null);

		Mockito.when(context.createFilter(MESSAGE_HANDLER_FILTER)).thenReturn(filterHandler);
		Mockito.when(filterHandler.toString()).thenReturn(MESSAGE_HANDLER_FILTER);

		Mockito.when(context.createFilter(AGENT_FILTER)).thenReturn(filterAgent);
		Mockito.when(filterAgent.toString()).thenReturn(AGENT_FILTER);

		Mockito.when(context.createFilter(DATA_STORE_FILTER)).thenReturn(filterDataStore);
		Mockito.when(filterDataStore.toString()).thenReturn(DATA_STORE_FILTER);

		Mockito.when(context.createFilter(AUTHENTICATION_FILTER)).thenReturn(filterAuthentication);
		Mockito.when(filterAuthentication.toString()).thenReturn(AUTHENTICATION_FILTER);

		Mockito.when(context.createFilter(AUTHORIZATION_FILTER)).thenReturn(filterAuthorization);
		Mockito.when(filterAuthorization.toString()).thenReturn(AUTHORIZATION_FILTER);

		Mockito.when(filterAuthorization.match(referenceAuthorization)).thenReturn(true);

		Mockito.when(context.getServiceReferences(Mockito.anyString(), Mockito.anyString()))
				.then(new Answer<ServiceReference[]>() {
					@Override
					public ServiceReference[] answer(InvocationOnMock invocation) throws Throwable {
						Object[] arguments = invocation.getArguments();
						if (arguments == null || arguments.length != 2) {
							return null;
						}
						if (arguments[0] != null && arguments[0].equals(MessageRouter.class.getCanonicalName())
								&& arguments[1] != null && arguments[1].equals("(uri=/serviceProvider)")) {
							if (handler == null) {
								return null;
							}
							return new ServiceReference[] { referenceHandler };

						} else if (arguments[0] != null && arguments[0].equals(SnaAgent.class.getCanonicalName())) {
							if (agent == null) {
								return null;
							}
							return new ServiceReference[] { referenceAgent };

						} else if (arguments[0] != null
								&& arguments[0].equals(SensiNactResourceModel.class.getCanonicalName())
								&& arguments[1] != null && arguments[1].equals("(uri=/serviceProvider)")) {
							return new ServiceReference[] { referenceProvider };

						} else if ((arguments[0] != null
								&& arguments[0].equals(AuthorizationService.class.getCanonicalName())
								&& arguments[1] == null)
								|| (arguments[0] == null && arguments[1].equals(AUTHORIZATION_FILTER))) {
							return new ServiceReference[] { referenceAuthorization };
						}
						return null;
					}
				});
		Mockito.when(context.getService(Mockito.any(ServiceReference.class))).then(new Answer<Object>() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				Object[] arguments = invocation.getArguments();
				if (arguments == null || arguments.length != 1) {
					return null;

				}
				// else if(arguments[0]==referenceAuthorization)
				// {
				// return new AuthorizationTest();
				//
				// }
				else if (arguments[0] == referenceAgent) {
					return agent;

				} else if (arguments[0] == referenceHandler) {
					return handler;

				} else if (arguments[0] == referenceProvider) {
					return provider;
				}
				return null;
			}
		});
		Mockito.when(context.registerService(Mockito.eq(MessageRouter.class.getCanonicalName()),
				Mockito.any(MessageRouter.class), Mockito.any(Dictionary.class)))
				.thenAnswer(new Answer<ServiceRegistration>() {
					@Override
					public ServiceRegistration answer(InvocationOnMock invocation) throws Throwable {
						// TestResourceBuilder.this.setHandler(
						// (SnaMessageHandler) invocation.getArguments()[1]);
						return registrationHandler;
					}
				});
		Mockito.when(context.registerService(Mockito.eq(SnaAgent.class.getCanonicalName()),
				Mockito.any(SnaAgent.class), Mockito.any(Dictionary.class)))
				.thenAnswer(new Answer<ServiceRegistration>() {
					@Override
					public ServiceRegistration answer(InvocationOnMock invocation) throws Throwable {
						// TestResourceBuilder.this.setAgent(
						// (SnaAgent) invocation.getArguments()[1]);
						return registrationAgent;
					}
				});
		Mockito.when(context.registerService(Mockito.any(String[].class),
				Mockito.any(SnaAgent.class), Mockito.any(Dictionary.class)))
				.thenAnswer(new Answer<ServiceRegistration>() {
					@Override
					public ServiceRegistration answer(InvocationOnMock invocation) throws Throwable {
						// TestResourceBuilder.this.setAgent(
						// (SnaAgent) invocation.getArguments()[1]);
						return registrationAgent;
					}
				});

		Mockito.when(context.registerService(Mockito.eq(SensiNactResourceModel.class.getCanonicalName()),
				Mockito.any(ModelElement.class), Mockito.any(Dictionary.class)))
				.thenAnswer(new Answer<ServiceRegistration>() {
					@Override
					public ServiceRegistration answer(InvocationOnMock invocation) throws Throwable {
						return snaObjectRegistration;
					}
				});
		Mockito.when(registration.getReference()).thenReturn(referenceProvider);
		Mockito.when(registrationHandler.getReference()).thenReturn(referenceHandler);
		Mockito.when(registrationAgent.getReference()).thenReturn(referenceAgent);

		Mockito.when(filterHandler.match(referenceHandler)).thenReturn(true);

		Mockito.when(context.getBundle()).thenReturn(bundle);
		Mockito.when(bundle.getSymbolicName()).thenReturn(MOCK_BUNDLE_NAME);
		Mockito.when(bundle.getBundleId()).thenReturn(MOCK_BUNDLE_ID);

		mediator = new Mediator(context);
		callbackCount = 0;
		linkCallbackCount = 0;
	}
}
