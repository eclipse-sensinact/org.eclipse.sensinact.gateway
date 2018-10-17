/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.core;

import java.security.AccessController;
import java.security.InvalidKeyException;
import java.security.PrivilegedAction;
import java.util.ArrayList;
//import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
//import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
//import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.constraint.Constraint;
import org.eclipse.sensinact.gateway.common.constraint.ConstraintFactory;
import org.eclipse.sensinact.gateway.common.constraint.InvalidConstraintDefinitionException;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.Sessions.KeyExtractor;
import org.eclipse.sensinact.gateway.core.Sessions.KeyExtractorType;
import org.eclipse.sensinact.gateway.core.message.MidAgentCallback;
import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.eclipse.sensinact.gateway.core.message.SnaAgent;
import org.eclipse.sensinact.gateway.core.message.SnaAgentImpl;
import org.eclipse.sensinact.gateway.core.message.SnaErrorfulMessage;
import org.eclipse.sensinact.gateway.core.message.SnaFilter;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse.Status;
import org.eclipse.sensinact.gateway.core.method.RemoteAccessMethodExecutable;
import org.eclipse.sensinact.gateway.core.method.legacy.ActResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.DescribeMethod;
import org.eclipse.sensinact.gateway.core.method.legacy.DescribeMethod.DescribeType;
import org.eclipse.sensinact.gateway.core.method.legacy.DescribeResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.DescribeResponseBuilder;
import org.eclipse.sensinact.gateway.core.method.legacy.GetResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.SetResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.SubscribeResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.UnsubscribeResponse;
import org.eclipse.sensinact.gateway.core.security.AccessLevelOption;
import org.eclipse.sensinact.gateway.core.security.AccessNode;
import org.eclipse.sensinact.gateway.core.security.AccessTree;
import org.eclipse.sensinact.gateway.core.security.AccountConnector;
import org.eclipse.sensinact.gateway.core.security.Authentication;
import org.eclipse.sensinact.gateway.core.security.AuthenticationService;
import org.eclipse.sensinact.gateway.core.security.AuthenticationToken;
import org.eclipse.sensinact.gateway.core.security.Credentials;
import org.eclipse.sensinact.gateway.core.security.InvalidCredentialException;
import org.eclipse.sensinact.gateway.core.security.MutableAccessTree;
import org.eclipse.sensinact.gateway.core.security.SecuredAccess;
import org.eclipse.sensinact.gateway.core.security.SecuredAccessException;
import org.eclipse.sensinact.gateway.core.security.SecuredAccessFactory;
import org.eclipse.sensinact.gateway.core.security.SecurityDataStoreServiceFactory;
import org.eclipse.sensinact.gateway.core.security.User;
import org.eclipse.sensinact.gateway.core.security.UserKey;
import org.eclipse.sensinact.gateway.core.security.UserManager;
import org.eclipse.sensinact.gateway.core.security.UserManagerFactory;
import org.eclipse.sensinact.gateway.core.security.UserUpdater;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;
import org.eclipse.sensinact.gateway.security.signature.api.BundleValidation;
import org.eclipse.sensinact.gateway.util.CryptoUtils;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * {@link Core} service implementation
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class SensiNact implements Core {
	// ********************************************************************//
	// NESTED DECLARATIONS //
	// ********************************************************************//

	/**
	 * Abstract {@link Session} service implementation
	 */
	abstract class SensiNactSession extends AbstractSession {
		/**
		 * Constructor
		 * 
		 * @param identifier
		 *            the String identifier of the Session to be instantiated
		 */
		public SensiNactSession(String identifier) {
			super(identifier);
		}

		/**
		 * @inheritDoc
		 * 
		 * @see org.eclipse.sensinact.gateway.core.Session# getServiceProviders()
		 */
		@Override
		public Set<ServiceProvider> serviceProviders(final String filter) {
			return AccessController.doPrivileged(new PrivilegedAction<Set<ServiceProvider>>() {
				@Override
				public Set<ServiceProvider> run() {
					return SensiNact.this.serviceProviders(SensiNactSession.this.getSessionId(), filter);
				}
			});
		}

		/**
		 * @inheritDoc
		 *
		 * @see org.eclipse.sensinact.gateway.core.Session#
		 *      getServiceProvider(java.lang.String)
		 */
		@Override
		public ServiceProvider serviceProvider(final String serviceProviderName) {
			ServiceProvider provider = AccessController.doPrivileged(new PrivilegedAction<ServiceProvider>() {
				@Override
				public ServiceProvider run() {
					return SensiNact.this.serviceProvider(SensiNactSession.this.getSessionId(), serviceProviderName);
				}
			});
			return provider;
		}

		/**
		 * @inheritDoc
		 *
		 * @see org.eclipse.sensinact.gateway.core.Session#
		 *      registerSessionAgent(org.eclipse.sensinact.gateway.core.message.MidAgentCallback,
		 *      org.eclipse.sensinact.gateway.core.message.SnaFilter)
		 */
		@Override
		public SubscribeResponse registerSessionAgent(String requestId, final MidAgentCallback callback,
				final SnaFilter filter) {
			String agentId = AccessController.<String>doPrivileged(new PrivilegedAction<String>() {
				@Override
				public String run() {
					SessionKey sessionKey = SensiNact.this.sessions.get(new KeyExtractor<KeyExtractorType>(
							KeyExtractorType.TOKEN, getSessionId()));
					return sessionKey.registerAgent(callback, filter);
				}
			});
			String uri = null;
			if (filter != null) {
				uri = filter.getSender();
			} else {
				uri = UriUtils.PATH_SEPARATOR;
			}
			SubscribeResponse response = null;
			if (agentId != null) {
				response = new SubscribeResponse(mediator, uri, Status.SUCCESS, 200);
				response.setResponse(new JSONObject().put("subscriptionId", agentId));

			} else {
				response = SensiNact.<JSONObject, SubscribeResponse>createErrorResponse(mediator,
						AccessMethod.SUBSCRIBE, uri, 520, "Unable to subscribe", null);
			}
			return tatooRequestId(requestId, response);
		}

		/**
		 * @inheritDoc
		 *
		 * @see org.eclipse.sensinact.gateway.core.Session#
		 *      unregisterSessionAgent(java.lang.String)
		 */
		@Override
		public UnsubscribeResponse unregisterSessionAgent(String requestId, final String agentId) {
			boolean unregistered = AccessController.<Boolean>doPrivileged(new PrivilegedAction<Boolean>() {
				@Override
				public Boolean run() {
					SessionKey key = SensiNact.this.sessions.get(new KeyExtractor<KeyExtractorType>(
							KeyExtractorType.TOKEN, getSessionId()));
					if (key != null && key.getPublicKey() != null) {
						return key.unregisterAgent(agentId);
					}
					return false;
				}
			});
			UnsubscribeResponse response = null;
			if (unregistered) {
				response = new UnsubscribeResponse(mediator, UriUtils.PATH_SEPARATOR, Status.SUCCESS, 200);
				response.setResponse(new JSONObject().put("message", "The agent has been properly unregistered"));

			} else {
				response = SensiNact.<JSONObject, UnsubscribeResponse>createErrorResponse(mediator,
						AccessMethod.UNSUBSCRIBE, UriUtils.PATH_SEPARATOR, 520, "Unable to unsubscribe", null);
			}
			return tatooRequestId(requestId, response);
		}

		/**
		 * @inheritDoc
		 *
		 * @see org.eclipse.sensinact.gateway.core.Session# get(java.lang.String,
		 *      java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		public GetResponse get(String requestId, final String serviceProviderId, final String serviceId,
				final String resourceId, final String attributeId) {			
			SessionKey sessionKey = SensiNact.this.sessions.get(new KeyExtractor<KeyExtractorType>(
					KeyExtractorType.TOKEN, this.getSessionId()));			
			String uri = getUri((sessionKey.localID() != 0), SensiNact.this.namespace(), 
				serviceProviderId, serviceId, resourceId);

			Resource resource = this.resource(serviceProviderId, serviceId, resourceId);

			GetResponse response = null;

			if (resource != null) {
				if (attributeId == null) {
					if (!resource.getType().equals(Resource.Type.ACTION)) {
						response = ((DataResource) resource).get();

					} else {
						response = SensiNact.<JSONObject, GetResponse>createErrorResponse(mediator, AccessMethod.GET,
								uri, 404, "Unknown Method", null);
					}
				} else {
					response = resource.get(attributeId);
				}
				return tatooRequestId(requestId, response);
			}
			if (sessionKey.localID() != 0) {
				response = SensiNact.<JSONObject, GetResponse>createErrorResponse(mediator, AccessMethod.GET, uri,
						SnaErrorfulMessage.NOT_FOUND_ERROR_CODE, "Resource not found", null);

				return tatooRequestId(requestId, response);
			}
			JSONObject object = AccessController.doPrivileged(new PrivilegedAction<JSONObject>() {
				@Override
				public JSONObject run() {
					return SensiNact.this.get(SensiNactSession.this.getSessionId(), serviceProviderId, serviceId,
							resourceId, attributeId);
				}
			});
			try {
				response = this.<GetResponse>responseFromJSONObject(mediator, uri, AccessMethod.GET, object);
			} catch (Exception e) {
				response = SensiNact.<JSONObject, GetResponse>createErrorResponse(mediator, AccessMethod.GET, uri,
						SnaErrorfulMessage.INTERNAL_SERVER_ERROR_CODE, "Internal server error", e);
			}
			return tatooRequestId(requestId, response);
		}

		/**
		 * @inheritDoc
		 *
		 * @see org.eclipse.sensinact.gateway.core.Session# set(java.lang.String,
		 *      java.lang.String, java.lang.String, java.lang.String, java.lang.Object)
		 */
		@Override
		public SetResponse set(String requestId, final String serviceProviderId, final String serviceId,
				final String resourceId, final String attributeId, final Object parameter) {
			SessionKey sessionKey = SensiNact.this.sessions.get(new KeyExtractor<KeyExtractorType>(
						KeyExtractorType.TOKEN, this.getSessionId()));
			String uri = getUri((sessionKey.localID() != 0), SensiNact.this.namespace(), serviceProviderId, serviceId,
					resourceId);
			Resource resource = this.resource(serviceProviderId, serviceId, resourceId);
			SetResponse response = null;

			if (resource != null) {
				if (attributeId == null) {
					if (!resource.getType().equals(Resource.Type.ACTION)) {
						response = ((DataResource) resource).set(parameter);

					} else {
						response = SensiNact.<JSONObject, SetResponse>createErrorResponse(mediator, AccessMethod.SET,
								uri, 404, "Unknown Method", null);
					}
				} else {
					response = resource.set(attributeId, parameter);
				}
				return tatooRequestId(requestId, response);
			}
			if (sessionKey.localID() != 0) {
				response = SensiNact.<JSONObject, SetResponse>createErrorResponse(mediator, AccessMethod.SET, uri,
						SnaErrorfulMessage.NOT_FOUND_ERROR_CODE, "Resource not found", null);
				return tatooRequestId(requestId, response);
			}
			JSONObject object = AccessController.doPrivileged(new PrivilegedAction<JSONObject>() {
				@Override
				public JSONObject run() {
					return SensiNact.this.set(SensiNactSession.this.getSessionId(), serviceProviderId, serviceId,
							resourceId, attributeId, parameter);
				}
			});
			try {
				response = this.<SetResponse>responseFromJSONObject(mediator, uri, AccessMethod.SET, object);
			} catch (Exception e) {
				response = SensiNact.<JSONObject, SetResponse>createErrorResponse(mediator, AccessMethod.SET, uri,
						SnaErrorfulMessage.INTERNAL_SERVER_ERROR_CODE, "Internal server error", e);
			}
			return tatooRequestId(requestId, response);
		}

		/**
		 * @inheritDoc
		 *
		 * @see org.eclipse.sensinact.gateway.core.Session# act(java.lang.String,
		 *      java.lang.String, java.lang.String, java.lang.Object[])
		 */
		@Override
		public ActResponse act(String requestId, final String serviceProviderId, final String serviceId,
				final String resourceId, final Object[] parameters) {
			ActResponse response = null;
			SessionKey sessionKey = SensiNact.this.sessions.get(new KeyExtractor<KeyExtractorType>(
					KeyExtractorType.TOKEN, this.getSessionId()));
			String uri = getUri((sessionKey.localID() != 0), SensiNact.this.namespace(), serviceProviderId, serviceId,
					resourceId);

			Resource resource = this.resource(serviceProviderId, serviceId, resourceId);

			if (resource != null) {
				if (!resource.getType().equals(Resource.Type.ACTION)) {
					response = SensiNact.<JSONObject, ActResponse>createErrorResponse(mediator, AccessMethod.ACT, uri,
							404, "Unknown Method", null);
				} else {
					if (parameters != null && parameters.length > 0) {
						response = ((ActionResource) resource).act(parameters);

					} else {
						response = ((ActionResource) resource).act();
					}
				}
				return tatooRequestId(requestId, response);
			}
			if (sessionKey.localID() != 0) {
				response = SensiNact.<JSONObject, ActResponse>createErrorResponse(mediator, AccessMethod.ACT, uri,
						SnaErrorfulMessage.NOT_FOUND_ERROR_CODE, "Resource not found", null);
				return tatooRequestId(requestId, response);
			}
			JSONObject object = AccessController.doPrivileged(new PrivilegedAction<JSONObject>() {
				@Override
				public JSONObject run() {
					return SensiNact.this.act(SensiNactSession.this.getSessionId(), serviceProviderId, serviceId,
							resourceId, parameters);
				}
			});
			try {
				response = this.<ActResponse>responseFromJSONObject(mediator, uri, AccessMethod.ACT, object);
			} catch (Exception e) {
				response = SensiNact.<JSONObject, ActResponse>createErrorResponse(mediator, AccessMethod.ACT, uri,
						SnaErrorfulMessage.INTERNAL_SERVER_ERROR_CODE, "Internal server error", e);
			}
			return tatooRequestId(requestId, response);
		}

		/**
		 * @inheritDoc
		 * 
		 * @see org.eclipse.sensinact.gateway.core.Session# subscribe(java.lang.String,
		 *      java.lang.String, java.lang.String,
		 *      org.eclipse.sensinact.gateway.core.message.Recipient,
		 *      org.json.JSONArray)
		 */
		@Override
		public SubscribeResponse subscribe(String requestId, final String serviceProviderId, final String serviceId,
				final String resourceId, final Recipient recipient, final JSONArray conditions, final String policy) {
			SubscribeResponse response = null;
			SessionKey sessionKey = SensiNact.this.sessions.get(new KeyExtractor<KeyExtractorType>(
					KeyExtractorType.TOKEN, this.getSessionId()));			
			String uri = getUri((sessionKey.localID() != 0), SensiNact.this.namespace(), serviceProviderId, 
				serviceId, resourceId);
			Resource resource = this.resource(serviceProviderId, serviceId, resourceId);

			if (resource != null) {
				if (!resource.getType().equals(Resource.Type.ACTION)) {
					Constraint constraint = null;
					if (conditions != null && conditions.length() > 0) {
						try {
							constraint = ConstraintFactory.Loader.load(mediator.getClassLoader(), conditions);

						} catch (InvalidConstraintDefinitionException e) {
							mediator.error(e);
						}
					}
					response = ((DataResource) resource).subscribe(recipient, (constraint == null 
						? Collections.<Constraint>emptySet():Collections.<Constraint>singleton(constraint)),policy);
				} else {
					response = SensiNact.<JSONObject, SubscribeResponse>createErrorResponse(mediator,
							AccessMethod.SUBSCRIBE, uri, 404, "Unknown Method", null);
				}
				return tatooRequestId(requestId, response);
			}
			if (sessionKey.localID() != 0) {
				response = SensiNact.<JSONObject, SubscribeResponse>createErrorResponse(mediator,
						AccessMethod.SUBSCRIBE, uri, SnaErrorfulMessage.NOT_FOUND_ERROR_CODE, "Resource not found",
						null);
				return tatooRequestId(requestId, response);
			}
			JSONObject object = AccessController.doPrivileged(new PrivilegedAction<JSONObject>() {
				@Override
				public JSONObject run() {
					return SensiNact.this.subscribe(SensiNactSession.this.getSessionId(), serviceProviderId, serviceId,
							resourceId, recipient, conditions);
				}
			});
			try {
				response = this.<SubscribeResponse>responseFromJSONObject(mediator, uri, AccessMethod.SUBSCRIBE,
						object);
			} catch (Exception e) {
				response = SensiNact.<JSONObject, SubscribeResponse>createErrorResponse(mediator,
						AccessMethod.SUBSCRIBE, uri, SnaErrorfulMessage.INTERNAL_SERVER_ERROR_CODE,
						"Internal server error", e);
			}
			return tatooRequestId(requestId, response);
		}

		/**
		 * @inheritDoc
		 *
		 * @see org.eclipse.sensinact.gateway.core.Session#
		 *      unsubscribe(java.lang.String, java.lang.String, java.lang.String,
		 *      java.lang.String)
		 */
		@Override
		public UnsubscribeResponse unsubscribe(String requestId, final String serviceProviderId, final String serviceId,
				final String resourceId, final String subscriptionId) {
			UnsubscribeResponse response = null;
			SessionKey sessionKey = SensiNact.this.sessions.get(new KeyExtractor<KeyExtractorType>(
						KeyExtractorType.TOKEN, this.getSessionId()));			
			String uri = getUri((sessionKey.localID() != 0), SensiNact.this.namespace(), serviceProviderId, serviceId,
					resourceId);

			Resource resource = this.resource(serviceProviderId, serviceId, resourceId);

			if (resource != null) {
				if (!resource.getType().equals(Resource.Type.ACTION)) {
					response = ((DataResource) resource).unsubscribe(subscriptionId);

				} else {
					response = SensiNact.<JSONObject, UnsubscribeResponse>createErrorResponse(mediator,
							AccessMethod.UNSUBSCRIBE, uri, 404, "Unknown Method", null);
				}
				return tatooRequestId(requestId, response);
			}
			if (sessionKey.localID() != 0) {
				response = SensiNact.<JSONObject, UnsubscribeResponse>createErrorResponse(mediator,
						AccessMethod.UNSUBSCRIBE, uri, SnaErrorfulMessage.NOT_FOUND_ERROR_CODE, "Resource not found",
						null);
				return tatooRequestId(requestId, response);
			}
			JSONObject object = AccessController.doPrivileged(new PrivilegedAction<JSONObject>() {
				@Override
				public JSONObject run() {
					return SensiNact.this.unsubscribe(SensiNactSession.this.getSessionId(), serviceProviderId,
							serviceId, resourceId, subscriptionId);
				}
			});
			try {
				response = this.<UnsubscribeResponse>responseFromJSONObject(mediator, uri, AccessMethod.UNSUBSCRIBE,
						object);
			} catch (Exception e) {
				response = SensiNact.<JSONObject, UnsubscribeResponse>createErrorResponse(mediator,
						AccessMethod.UNSUBSCRIBE, uri, SnaErrorfulMessage.INTERNAL_SERVER_ERROR_CODE,
						"Internal server error", e);
			}
			return tatooRequestId(requestId, response);
		}

		/**
		 * @inheritDoc
		 *
		 * @see org.eclipse.sensinact.gateway.core.Session# getAll(java.lang.String,
		 *      org.eclipse.sensinact.gateway.core.FilteringDefinition)
		 */
		@Override
		public DescribeResponse<String> getAll(String requestId, String filter, FilteringCollection filterCollection) {
			SessionKey sessionKey = null;
			sessionKey = SensiNact.this.sessions.get(new KeyExtractor<KeyExtractorType>(
						KeyExtractorType.TOKEN, this.getSessionId()));
			
			DescribeResponse<String> response = null;
			DescribeMethod<String> method = new DescribeMethod<String>(mediator, UriUtils.PATH_SEPARATOR, null,
					DescribeType.COMPLETE_LIST);

			DescribeResponseBuilder<String> builder = method.createAccessMethodResponseBuilder(null);

			final String ldapFilter;
			if (filterCollection != null) {
				ldapFilter = filterCollection.composeLDAPFormatedFilter(filter);

			} else {
				ldapFilter = filter;
			}
			String all = AccessController.doPrivileged(new PrivilegedAction<String>() {
				@Override
				public String run() {
					return SensiNact.this.getAll(SensiNactSession.this.getSessionId(), ldapFilter);
				}
			});
			if (all == null) {
				response = SensiNact.<String, DescribeResponse<String>>createErrorResponse(mediator,
						DescribeType.COMPLETE_LIST, UriUtils.PATH_SEPARATOR,
						SnaErrorfulMessage.INTERNAL_SERVER_ERROR_CODE, "Internal server error", null);
				return tatooRequestId(requestId, response);
			}
			if (sessionKey.localID() != 0) {
				builder.setAccessMethodObjectResult(all);
				response = builder.createAccessMethodResponse(Status.SUCCESS);
				return tatooRequestId(requestId, response);
			}
			String result = new StringBuilder().append("[").append(all).append("]").toString();

			if (filterCollection != null) {
				result = filterCollection.apply(result);
			}
			builder.setAccessMethodObjectResult(result);
			response = builder.createAccessMethodResponse(Status.SUCCESS);

			if (filterCollection != null) {
				response.put("filters", new JSONArray(filterCollection.filterJsonDefinition()),
						filterCollection.hideFilter());
			}
			return tatooRequestId(requestId, response);
		}

		/**
		 * @inheritDoc
		 *
		 * @see org.eclipse.sensinact.gateway.core.Session#
		 *      getProviders(java.lang.String,
		 *      org.eclipse.sensinact.gateway.core.FilteringCollection)
		 */
		@Override
		public DescribeResponse<String> getProviders(String requestId, FilteringCollection filterCollection) {
			SessionKey sessionKey = SensiNact.this.sessions.get(new KeyExtractor<KeyExtractorType>(
						KeyExtractorType.TOKEN, this.getSessionId()));
			
			DescribeResponse<String> response = null;
			DescribeMethod<String> method = new DescribeMethod<String>(mediator, UriUtils.PATH_SEPARATOR, null,
					DescribeType.PROVIDERS_LIST);

			DescribeResponseBuilder<String> builder = method.createAccessMethodResponseBuilder(null);
			final String ldapFilter;
			if (filterCollection != null) {
				ldapFilter = filterCollection.composeLDAPFormatedFilter(null);
			} else {
				ldapFilter = null;
			}
			String providers = AccessController.doPrivileged(new PrivilegedAction<String>() {
				@Override
				public String run() {
					return SensiNact.this.getProviders(SensiNactSession.this.getSessionId(), ldapFilter);
				}
			});
			if (providers == null) {
				response = SensiNact.<String, DescribeResponse<String>>createErrorResponse(mediator,
						DescribeType.PROVIDERS_LIST, UriUtils.PATH_SEPARATOR, SnaErrorfulMessage.NOT_FOUND_ERROR_CODE,
						"Internal server error", null);

				return tatooRequestId(requestId, response);
			}
			if (sessionKey.localID() != 0) {
				builder.setAccessMethodObjectResult(providers);
				response = builder.createAccessMethodResponse(Status.SUCCESS);
				return tatooRequestId(requestId, response);
			}
			String result = new StringBuilder().append("[").append(providers).append("]").toString();

			if (filterCollection != null) {
				result = filterCollection.apply(result);
			}
			builder.setAccessMethodObjectResult(result);
			response = builder.createAccessMethodResponse(Status.SUCCESS);
			if (filterCollection != null) {
				response.put("filters", new JSONArray(filterCollection.filterJsonDefinition()),
						filterCollection.hideFilter());
			}
			return tatooRequestId(requestId, response);
		}

		/**
		 * @inheritDoc
		 *
		 * @see org.eclipse.sensinact.gateway.core.Session#
		 *      getProvider(java.lang.String, java.lang.String)
		 */
		@Override
		public DescribeResponse<JSONObject> getProvider(String requestId, final String serviceProviderId) {
			SessionKey sessionKey = SensiNact.this.sessions.get(new KeyExtractor<KeyExtractorType>(
					KeyExtractorType.TOKEN, this.getSessionId()));

			String uri = getUri((sessionKey.localID() != 0), SensiNact.this.namespace(), serviceProviderId);

			DescribeResponse<JSONObject> response = null;

			DescribeMethod<JSONObject> method = new DescribeMethod<JSONObject>(mediator, uri, null,
					DescribeType.PROVIDER);

			DescribeResponseBuilder<JSONObject> builder = method.createAccessMethodResponseBuilder(null);

			ServiceProvider provider = this.serviceProvider(serviceProviderId);

			if (provider != null) {
				builder.setAccessMethodObjectResult(new JSONObject(provider.getDescription().getJSON()));

				return tatooRequestId(requestId, builder.createAccessMethodResponse());
			}
			if (sessionKey.localID() != 0) {
				response = SensiNact.<JSONObject, DescribeResponse<JSONObject>>createErrorResponse(mediator,
						DescribeType.PROVIDER, uri, SnaErrorfulMessage.NOT_FOUND_ERROR_CODE,
						"Service provider not found", null);

				return tatooRequestId(requestId, response);
			}
			JSONObject object = AccessController.doPrivileged(new PrivilegedAction<JSONObject>() {
				@Override
				public JSONObject run() {
					return SensiNact.this.getProvider(SensiNactSession.this.getSessionId(), serviceProviderId);
				}
			});
			response = describeFromJSONObject(mediator, builder, DescribeType.PROVIDER, object);
			return tatooRequestId(requestId, response);
		}

		/**
		 * @inheritDoc
		 * 
		 * @see org.eclipse.sensinact.gateway.core.Session#
		 *      getServices(java.lang.String, java.lang.String,
		 *      org.eclipse.sensinact.gateway.core.FilteringCollection)
		 */
		@Override
		public DescribeResponse<String> getServices(String requestId, final String serviceProviderId,
				FilteringCollection filterCollection) {
			SessionKey sessionKey = SensiNact.this.sessions.get(new KeyExtractor<KeyExtractorType>(
					KeyExtractorType.TOKEN, this.getSessionId()));

			String uri = getUri((sessionKey.localID() != 0), SensiNact.this.namespace(), serviceProviderId);

			DescribeResponse<String> response = null;

			DescribeMethod<String> method = new DescribeMethod<String>(mediator, uri, null, DescribeType.SERVICES_LIST);

			DescribeResponseBuilder<String> builder = method.createAccessMethodResponseBuilder(null);

			ServiceProvider provider = this.serviceProvider(serviceProviderId);

			String services = null;

			if (provider == null) {
				if (sessionKey.localID() != 0) {
					response = SensiNact.<String, DescribeResponse<String>>createErrorResponse(mediator,
							DescribeType.SERVICES_LIST, uri, SnaErrorfulMessage.NOT_FOUND_ERROR_CODE,
							"Service provider not found", null);

					return tatooRequestId(requestId, response);
				}
				services = AccessController.doPrivileged(new PrivilegedAction<String>() {
					@Override
					public String run() {
						return SensiNact.this.getServices(SensiNactSession.this.getSessionId(), serviceProviderId);
					}
				});
			} else {
				services = this.joinElementNames(provider);
			}
			if (services == null) {
				response = SensiNact.<String, DescribeResponse<String>>createErrorResponse(mediator,
						DescribeType.SERVICES_LIST, uri, SnaErrorfulMessage.NOT_FOUND_ERROR_CODE,
						"Service provider not found", null);

				return tatooRequestId(requestId, response);
			}
			if (sessionKey.localID() != 0) {
				builder.setAccessMethodObjectResult(services);
				response = builder.createAccessMethodResponse(Status.SUCCESS);
				return tatooRequestId(requestId, response);
			}
			String result = new StringBuilder().append("[").append(services).append("]").toString();

			if (filterCollection != null) {
				result = filterCollection.apply(result);
			}
			builder.setAccessMethodObjectResult(result);
			response = builder.createAccessMethodResponse(Status.SUCCESS);

			if (filterCollection != null) {
				response.put("filters", new JSONArray(filterCollection.filterJsonDefinition()),
						filterCollection.hideFilter());
			}
			return tatooRequestId(requestId, response);
		}

		/**
		 * @inheritDoc
		 *
		 * @see org.eclipse.sensinact.gateway.core.Endpoint#
		 *      jsonService(java.lang.String, java.lang.String)
		 */
		@Override
		public DescribeResponse<JSONObject> getService(String requestId, final String serviceProviderId,
				final String serviceId) {
			SessionKey sessionKey = SensiNact.this.sessions.get(new KeyExtractor<KeyExtractorType>(
						KeyExtractorType.TOKEN, this.getSessionId()));

			String uri = getUri((sessionKey.localID() != 0), SensiNact.this.namespace(), serviceProviderId, serviceId);

			DescribeResponse<JSONObject> response = null;

			DescribeMethod<JSONObject> method = new DescribeMethod<JSONObject>(mediator, uri, null,
					DescribeType.SERVICE);

			DescribeResponseBuilder<JSONObject> builder = method.createAccessMethodResponseBuilder(null);

			Service service = this.service(serviceProviderId, serviceId);

			if (service != null) {
				builder.setAccessMethodObjectResult(new JSONObject(service.getDescription().getJSON()));

				return tatooRequestId(requestId, builder.createAccessMethodResponse());
			}
			if (sessionKey.localID() != 0) {
				response = SensiNact.<JSONObject, DescribeResponse<JSONObject>>createErrorResponse(mediator,
						DescribeType.SERVICE, uri, SnaErrorfulMessage.NOT_FOUND_ERROR_CODE, "Service not found", null);
				return tatooRequestId(requestId, response);
			}
			JSONObject object = AccessController.doPrivileged(new PrivilegedAction<JSONObject>() {
				@Override
				public JSONObject run() {
					return SensiNact.this.getService(SensiNactSession.this.getSessionId(), serviceProviderId,
							serviceId);
				}
			});
			return tatooRequestId(requestId, describeFromJSONObject(mediator, builder, DescribeType.SERVICE, object));
		}

		/**
		 * @inheritDoc
		 *
		 * @see org.eclipse.sensinact.gateway.core.Session#
		 *      getResources(java.lang.String, java.lang.String,
		 *      org.eclipse.sensinact.gateway.core.FilteringDefinition)
		 */
		@Override
		public DescribeResponse<String> getResources(String requestId, final String serviceProviderId,
				final String serviceId, FilteringCollection filterCollection) {
			SessionKey sessionKey = SensiNact.this.sessions.get(new KeyExtractor<KeyExtractorType>(
					KeyExtractorType.TOKEN, this.getSessionId()));

			String uri = getUri((sessionKey.localID() != 0), SensiNact.this.namespace(), serviceProviderId, serviceId);

			DescribeResponse<String> response = null;

			DescribeMethod<String> method = new DescribeMethod<String>(mediator, uri, null,
					DescribeType.RESOURCES_LIST);

			DescribeResponseBuilder<String> builder = method.createAccessMethodResponseBuilder(null);

			Service service = this.service(serviceProviderId, serviceId);
			String resources = null;

			if (service == null) {
				if (sessionKey.localID() != 0) {
					response = SensiNact.<String, DescribeResponse<String>>createErrorResponse(mediator,
							DescribeType.RESOURCES_LIST, uri, SnaErrorfulMessage.NOT_FOUND_ERROR_CODE,
							"Service not found", null);

					return tatooRequestId(requestId, response);
				}
				resources = AccessController.doPrivileged(new PrivilegedAction<String>() {
					@Override
					public String run() {
						return SensiNact.this.getResources(SensiNactSession.this.getSessionId(), serviceProviderId,
								serviceId);
					}
				});
			} else {
				resources = this.joinElementNames(service);
			}
			if (resources == null) {
				response = SensiNact.<String, DescribeResponse<String>>createErrorResponse(mediator,
						DescribeType.RESOURCES_LIST, uri, SnaErrorfulMessage.NOT_FOUND_ERROR_CODE, "Service not found",
						null);

				return tatooRequestId(requestId, response);
			}
			if (sessionKey.localID() != 0) {
				builder.setAccessMethodObjectResult(resources);
				response = builder.createAccessMethodResponse(Status.SUCCESS);
				return tatooRequestId(requestId, response);
			}
			String result = new StringBuilder().append("[").append(resources).append("]").toString();

			if (filterCollection != null) {
				result = filterCollection.apply(result);
			}
			builder.setAccessMethodObjectResult(result);
			response = builder.createAccessMethodResponse(Status.SUCCESS);

			if (filterCollection != null) {
				response.put("filters", new JSONArray(filterCollection.filterJsonDefinition()),
						filterCollection.hideFilter());
			}
			return tatooRequestId(requestId, response);
		}

		/**
		 * @inheritDoc
		 *
		 * @see org.eclipse.sensinact.gateway.core.Session#
		 *      getResource(java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		public DescribeResponse<JSONObject> getResource(final String requestId, final String serviceProviderId,
				final String serviceId, final String resourceId) {
			SessionKey sessionKey = SensiNact.this.sessions.get(new KeyExtractor<KeyExtractorType>(
					KeyExtractorType.TOKEN, this.getSessionId()));

			String uri = getUri((sessionKey.localID() != 0), SensiNact.this.namespace(), serviceProviderId, serviceId,
					resourceId);

			DescribeResponseBuilder<JSONObject> builder = new DescribeMethod<JSONObject>(mediator, uri, null,
					DescribeType.RESOURCE).createAccessMethodResponseBuilder(null);

			Resource resource = this.resource(serviceProviderId, serviceId, resourceId);

			DescribeResponse<JSONObject> response = null;

			if (resource != null) {
				builder.setAccessMethodObjectResult(new JSONObject(resource.getDescription().getJSONDescription()));
				return tatooRequestId(requestId, builder.createAccessMethodResponse());
			}
			if (sessionKey.localID() != 0) {
				response = SensiNact.<JSONObject, DescribeResponse<JSONObject>>createErrorResponse(mediator,
						DescribeType.RESOURCE, uri, SnaErrorfulMessage.NOT_FOUND_ERROR_CODE, "Resource not found",
						null);
				return tatooRequestId(requestId, response);
			}
			JSONObject object = AccessController.doPrivileged(new PrivilegedAction<JSONObject>() {
				@Override
				public JSONObject run() {
					return SensiNact.this.getResource(SensiNactSession.this.getSessionId(), serviceProviderId,
							serviceId, resourceId);
				}
			});
			return tatooRequestId(requestId, describeFromJSONObject(mediator, builder, DescribeType.RESOURCE, object));
		}
	};

	/**
	 * {@link Session} service implementation for anonymous user
	 */
	final class SensiNactAnonymousSession extends SensiNactSession implements AnonymousSession {
		
		/**
		 * Constructor
		 * 
		 * @param identifier 
		 * 		the String identifier of the Session to be created
		 */
		SensiNactAnonymousSession(String identifier) {
			super(identifier);
		}
		
		/**
		 * @inheritDoc
		 * 
		 * @see org.eclipse.sensinact.gateway.core.AnonymousSession#registerUser(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		public final void registerUser(final String login, final String password, final String account, final String accountType) 
			throws SecuredAccessException {
			SecuredAccessException exception = SensiNact.this.mediator.callService(
				UserManager.class, new Executable<UserManager,SecuredAccessException>(){
					@Override
					public SecuredAccessException execute(UserManager userManager) throws Exception {
					    try {
					    	if(userManager.accountExists(account)||userManager.loginExists(login)) {
					    		throw new SecuredAccessException("A user with this login or account already exists");
					    	}
					    	final String token = SensiNact.this.nextToken();
					        final UserUpdater userUpdater = userManager.createUser(token, login, password, account, accountType);

					        ServiceReference[] references = SensiNact.this.mediator.getContext().getServiceReferences(
					        	AccountConnector.class.getName(), new StringBuilder().append("(org.eclipse.sensinact.security.account.type="
					        		).append(accountType).append(")").toString());
					        
					        if(references == null || references.length == 0){
					        	throw new SecuredAccessException("No account connector");
					        }
					        int index = 0;
					        for(;index < references.length;index++) {
					        	AccountConnector connector = (AccountConnector)SensiNact.this.mediator.getContext(
					        			).getService(references[index]);
					        	if(connector != null) {
					        		connector.connect(token, userUpdater);
					        		SensiNact.this.mediator.getContext().ungetService(references[index]);
					        		break;
					        	}
					        }
					   } catch(SecuredAccessException e) {
						   return e;
					   } catch(Exception e) {
						   return new SecuredAccessException(e);
					   }
					   return null;
					}
				}
			);
			if(exception != null) {
				throw exception;
			}
		}

		/**
		 * @inheritDoc
		 * 
		 * @see org.eclipse.sensinact.gateway.core.AnonymousSession#renewPassword(java.lang.String)
		 */
		@Override
		public final void renewPassword(final String account) throws SecuredAccessException {
			SecuredAccessException exception = SensiNact.this.mediator.callService(
				UserManager.class, new Executable<UserManager,SecuredAccessException>(){
				    @Override
				    public SecuredAccessException execute(UserManager userManager) throws Exception {
				    	User user = userManager.getUserFromAccount(account);
					    if(user==null) {
					    	return new SecuredAccessException("No user found for this account");
					    }
				    	try{
					    	final String token = SensiNact.this.nextToken();
					        final UserUpdater userUpdater = userManager.renewUserPassword(token, account, user.getAccountType());
					        
					        ServiceReference[] references = SensiNact.this.mediator.getContext().getServiceReferences(
					        	AccountConnector.class.getName(), new StringBuilder().append("(org.eclipse.sensinact.security.account.type="
					        		).append(user.getAccountType()).append(")").toString());
					        
					        if(references == null || references.length == 0){
					        	return new SecuredAccessException("No account connector");
					        }
					        int index = 0;
					        for(;index < references.length;index++) {
					        	AccountConnector connector = (AccountConnector)SensiNact.this.mediator.getContext(
					        			).getService(references[index]);
					        	if(connector != null) {
					        		connector.connect(token, userUpdater);
					        		SensiNact.this.mediator.getContext().ungetService(references[index]);
					        		break;
					        	}
					        }
					   } catch(SecuredAccessException e) {
						   return e;
					   } catch(Exception e) {
						   return new SecuredAccessException(e);
					   }
					   return null;
				    }
				}
			);
			if(exception != null) {
				throw exception;
			}
		}
	}

	/**
	 * {@link Session} service implementation for authenticated user
	 */
	final class SensiNactAuthenticatedSession extends SensiNactSession implements AuthenticatedSession {
		
		/**
		 * Constructor 
		 * 
		 * @param identifier
		 * 		the String identifier of the Session to be created
		 */
		SensiNactAuthenticatedSession(String identifier) {
			super(identifier);
		}

		/**
		 * @inheritDoc
		 * 
		 * @see org.eclipse.sensinact.gateway.core.AuthenticatedSession#changePassword(java.lang.String, java.lang.String)
		 */
		@Override
		public final void changePassword(final String oldPassword, final String newPassword) throws SecuredAccessException {
			SessionKey sessionKey = SensiNact.this.sessions.get(new KeyExtractor<KeyExtractorType>(
					KeyExtractorType.TOKEN, getSessionId()));

			final String publicKey = sessionKey.getPublicKey();
			
			SecuredAccessException exception = SensiNact.this.mediator.callService(
				UserManager.class, new Executable<UserManager,SecuredAccessException>(){
				    @Override
				    public SecuredAccessException execute(UserManager userManager) throws Exception {
				    	User user = userManager.getUserFromPublicKey(publicKey);
				    	try {
							String encryptedPassword = CryptoUtils.cryptWithMD5(oldPassword);
				    		userManager.updateField(user, "password", encryptedPassword, newPassword);
				    	} catch(SecuredAccessException e){
				    		return e;
				    	}catch(Exception e){
				    		return new SecuredAccessException(e);
				    	}
				    	return null;
				    }
				}
			);
		}
	}

	/**
	 * Endpoint of the local OSGi host environment
	 */
	final class RegistryEndpoint {
		/**
		 * @param publicKey
		 * @param filter
		 * @return
		 */
		private Collection<ServiceReference<SensiNactResourceModel>> getReferences(SessionKey sessionKey,
				String filter) {
			AccessTree<? extends AccessNode> tree = sessionKey.getAccessTree();
			AccessMethod.Type describe = AccessMethod.Type.valueOf(AccessMethod.DESCRIBE);

			Collection<ServiceReference<SensiNactResourceModel>> result = new ArrayList<ServiceReference<SensiNactResourceModel>>();

			Collection<ServiceReference<SensiNactResourceModel>> references = null;
			try {
				references = SensiNact.this.mediator.getContext().getServiceReferences(SensiNactResourceModel.class,
						filter);
				Iterator<ServiceReference<SensiNactResourceModel>> iterator = references.iterator();

				while (iterator.hasNext()) {
					ServiceReference<SensiNactResourceModel> reference = iterator.next();
					String name = (String) reference.getProperty("name");
					Integer level = (Integer) reference.getProperty(name.concat(".DESCRIBE"));
					if (level == null) {
						level = Integer.valueOf(AccessLevelOption.OWNER.getAccessLevel().getLevel());
					}
					AccessNode node = sessionKey.getAccessTree().getRoot().get(UriUtils.getUri(new String[] { name }));

					if (node == null) {
						node = tree.getRoot();
					}
					if (node.getAccessLevelOption(describe).getAccessLevel().getLevel() >= level.intValue()) {
						result.add(reference);
					}
				}
			} catch (InvalidSyntaxException e) {
				mediator.error(e.getMessage(), e);
			}
			return result;
		}

		/**
		 * @param publicKey
		 * @param filter
		 * @return
		 */
		private Set<ServiceProvider> serviceProviders(final SessionKey sessionKey, String filter) {
			String activeFilter = "(lifecycle.status=ACTIVE)";
			String providersFilter = null;

			if (filter == null) {
				providersFilter = activeFilter;

			} else {
				StringBuilder filterBuilder = new StringBuilder().append("(&");
				if (!filter.startsWith("(")) {
					filterBuilder.append("(");
				}
				filterBuilder.append(filter);
				if (!filter.endsWith(")")) {
					filterBuilder.append(")");
				}
				filterBuilder.append(activeFilter);
				filterBuilder.append(")");
				providersFilter = filterBuilder.toString();
			}
			final String fltr = providersFilter;

			Set<ServiceProvider> serviceProviders = AccessController
					.<Set<ServiceProvider>>doPrivileged(new PrivilegedAction<Set<ServiceProvider>>() {
						@SuppressWarnings("unchecked")
						@Override
						public Set<ServiceProvider> run() {
							Collection<ServiceReference<SensiNactResourceModel>> references = RegistryEndpoint.this
									.getReferences(sessionKey, fltr);

							Iterator<ServiceReference<SensiNactResourceModel>> iterator = references.iterator();

							Set<ServiceProvider> providers = new HashSet<ServiceProvider>();

							while (iterator.hasNext()) {
								ServiceReference<SensiNactResourceModel> ref = iterator.next();
								SensiNactResourceModel model = SensiNact.this.mediator.getContext().getService(ref);
								ServiceProvider provider = null;
								try {
									provider = (ServiceProvider) model.getRootElement()
											.getProxy(sessionKey.getAccessTree());

								} catch (ModelElementProxyBuildException e) {
									SensiNact.this.mediator.error(e);
								}
								if (provider != null && provider.isAccessible()) {
									providers.add(provider);
								}
							}
							return providers;
						}
					});
			return serviceProviders;
		}

		/**
		 * @param publicKey
		 * @param serviceProviderName
		 * @return
		 */
		private ServiceProvider serviceProvider(SessionKey sessionKey, final String serviceProviderName) {
			ServiceProvider provider = null;

			Set<ServiceProvider> providers = this.serviceProviders(sessionKey,
					new StringBuilder().append("(name=").append(serviceProviderName).append(")").toString());

			if (providers == null || providers.size() != 1) {
				return provider;
			}
			provider = providers.iterator().next();
			return provider;
		}

		/**
		 * @param publicKey
		 * @param serviceProviderName
		 * @param serviceName
		 * @return
		 */
		private Service service(SessionKey sessionKey, String serviceProviderName, String serviceName) {
			ServiceProvider serviceProvider = serviceProvider(sessionKey, serviceProviderName);
			Service service = null;
			if (serviceProvider != null) {
				service = serviceProvider.getService(serviceName);
			}
			return service;
		}

		/**
		 * @param publicKey
		 * @param serviceProviderName
		 * @param serviceName
		 * @param resourceName
		 * @return
		 */
		private Resource resource(SessionKey sessionKey, String serviceProviderName, String serviceName,
				String resourceName) {
			Service service = this.service(sessionKey, serviceProviderName, serviceName);
			Resource resource = null;
			if (service != null) {
				resource = service.getResource(resourceName);
			}
			return resource;
		}

		/**
		 * @param publicKey
		 * @param resolveNamespace
		 * @param filter
		 * @param filterDefinition
		 * @return
		 */
		private String getAll(SessionKey sessionKey, boolean resolveNamespace, String filter) {
			StringBuilder builder = new StringBuilder();
			String prefix = resolveNamespace
					? new StringBuilder().append(SensiNact.this.namespace()).append(":").toString()
					: "";

			int index = -1;

			Collection<ServiceReference<SensiNactResourceModel>> references = RegistryEndpoint.this
					.getReferences(sessionKey, filter);
			Iterator<ServiceReference<SensiNactResourceModel>> iterator = references.iterator();

			AccessTree<? extends AccessNode> tree = sessionKey.getAccessTree();
			AccessMethod.Type describe = AccessMethod.Type.valueOf(AccessMethod.DESCRIBE);

			while (iterator.hasNext()) {
				index++;
				ServiceReference<SensiNactResourceModel> reference = iterator.next();
				String name = (String) reference.getProperty("name");

				String provider = new StringBuilder().append(prefix).append(name).toString();
				String location = (String) reference
						.getProperty(ModelInstanceRegistration.LOCATION_PROPERTY.concat(".value"));
				location = (location == null || location.length() == 0) ? defaultLocation : location;
				List<String> serviceList = (List<String>) reference.getProperty("services");

				builder.append(index > 0 ? ',' : "");
				builder.append('{');
				builder.append("\"name\":");
				builder.append('"');
				builder.append(provider);
				builder.append('"');
				builder.append(",\"location\":");
				builder.append('"');
				builder.append(location);
				builder.append('"');
				builder.append(",\"services\":");
				builder.append('[');

				int sindex = 0;
				int slength = serviceList == null ? 0 : serviceList.size();
				for (; sindex < slength; sindex++) {
					String service = serviceList.get(sindex);
					String serviceUri = UriUtils.getUri(new String[] { name, service });
					Integer serviceLevel = (Integer) reference.getProperty(service.concat(".DESCRIBE"));
					if (serviceLevel == null) {
						serviceLevel = Integer.valueOf(AccessLevelOption.OWNER.getAccessLevel().getLevel());
					}
					AccessNode node = sessionKey.getAccessTree().getRoot().get(serviceUri);
					if (node == null) {
						node = tree.getRoot();
					}
					int describeAccessLevel = node.getAccessLevelOption(describe).getAccessLevel().getLevel();
					int serviceLevelLevel = serviceLevel.intValue();

					if (node.getAccessLevelOption(describe).getAccessLevel().getLevel() < serviceLevel.intValue()) {
						continue;
					}
					List<String> resourceList = (List<String>) reference.getProperty(service.concat(".resources"));

					builder.append(sindex > 0 ? ',' : "");
					builder.append('{');
					builder.append("\"name\":");
					builder.append('"');
					builder.append(service);
					builder.append('"');
					builder.append(",\"resources\":");
					builder.append('[');

					int rindex = 0;
					int rlength = resourceList == null ? 0 : resourceList.size();
					for (; rindex < rlength; rindex++) {
						String resource = resourceList.get(rindex);
						String resolvedResource = new StringBuilder().append(service).append(".").append(resource)
								.toString();
						String resourceUri = UriUtils.getUri(new String[] { name, service, resource });
						Integer resourceLevel = (Integer) reference.getProperty(resolvedResource.concat(".DESCRIBE"));
						if (resourceLevel == null) {
							resourceLevel = Integer.valueOf(AccessLevelOption.OWNER.getAccessLevel().getLevel());
						}
						node = sessionKey.getAccessTree().getRoot().get(resourceUri);
						if (node == null) {
							node = tree.getRoot();
						}
						if (node.getAccessLevelOption(describe).getAccessLevel().getLevel() < resourceLevel
								.intValue()) {
							continue;
						}
						String type = (String) reference.getProperty(resolvedResource.concat(".type"));
						builder.append(rindex > 0 ? ',' : "");
						builder.append('{');
						builder.append("\"name\":");
						builder.append('"');
						builder.append(resource);
						builder.append('"');
						builder.append(",\"type\":");
						builder.append('"');
						builder.append(type);
						builder.append('"');
						builder.append('}');
					}
					builder.append(']');
					builder.append('}');
				}
				builder.append(']');
				builder.append('}');
			}
			String content = builder.toString();
			return content;
		}

		/**
		 * @param publicKey
		 * @param resolveNamespace
		 * @param filter
		 * @return
		 */
		private String getProviders(SessionKey sessionKey, boolean resolveNamespace, String filter) {
			String prefix = resolveNamespace
					? new StringBuilder().append(SensiNact.this.namespace()).append(":").toString()
					: "";
			Collection<ServiceReference<SensiNactResourceModel>> references = this.getReferences(sessionKey, filter);
			Iterator<ServiceReference<SensiNactResourceModel>> iterator = references.iterator();

			StringBuilder builder = new StringBuilder();
			int index = 0;
			while (iterator.hasNext()) {
				ServiceReference<SensiNactResourceModel> reference = iterator.next();
				String name = (String) reference.getProperty("name");
				String provider = new StringBuilder().append(prefix).append(name).toString();
				if (index > 0) {
					builder.append(",");
				}
				builder.append('"');
				builder.append(provider);
				builder.append('"');
				index++;
			}
			String content = builder.toString();
			return content;
		}
	};

	// ********************************************************************//
	// ABSTRACT DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// STATIC DECLARATIONS //
	// ********************************************************************//

	private static String getUri(boolean resolved, String namespace, String... pathElements) {
		if (pathElements == null || pathElements.length == 0) {
			return null;
		}
		String providerId = resolved
				? new StringBuilder().append(namespace).append(":").append(pathElements[0]).toString()
				: pathElements[0];

		String[] uriElements = new String[pathElements.length];
		if (pathElements.length > 1) {
			System.arraycopy(pathElements, 1, uriElements, 1, pathElements.length - 1);
		}
		uriElements[0] = providerId;
		return UriUtils.getUri(uriElements);
	}

	private static <T, R extends AccessMethodResponse<T>> R createErrorResponse(Mediator mediator, String type,
			String uri, int statusCode, String message, Exception e) {
		R response = AccessMethodResponse.<T, R>error(mediator, uri, AccessMethod.Type.valueOf(type), statusCode,
				message, e);
		return response;
	}

	private static <T, R extends DescribeResponse<T>> R createErrorResponse(Mediator mediator,
			DescribeMethod.DescribeType type, String uri, int statusCode, String message, Exception e) {
		R response = AccessMethodResponse.<T, R>error(mediator, uri, type, statusCode, message, e);
		return response;
	}

	private static final String namespace(Mediator mediator) {
		String prop = (String) mediator.getProperty(Core.NAMESPACE_PROP);
		if (prop == null) {
			prop = new StringBuilder().append("sNa")
					.append(Math.round((float) (System.currentTimeMillis() / 100000L)) + mediator.hashCode())
					.toString();
		}
		return prop;
	}

	protected static final int LOCAL_ID = 0;

	// ********************************************************************//
	// INSTANCE DECLARATIONS //
	// ********************************************************************//

	private final AccessTree<? extends AccessNode> anonymousTree;
	private final Sessions sessions;

	private Mediator mediator;
	private RegistryEndpoint registry;

	private volatile AtomicInteger count = new AtomicInteger(LOCAL_ID + 1);
	private final String namespace;
	private final String defaultLocation;

	/**
	 * Constructor
	 * 
	 * @param mediator
	 *            the extended {@link Mediator} allowing the {@link Core} to be
	 *            instantiated to interact with the OSGi host environment
	 * 
	 * @throws SecuredAccessException
	 * @throws BundleException
	 */
	public SensiNact(final Mediator mediator) throws SecuredAccessException, BundleException, DataStoreException {
		this.namespace = SensiNact.namespace(mediator);

		SecuredAccess securedAccess = null;

		ServiceLoader<SecurityDataStoreServiceFactory> dataStoreServiceFactoryLoader = ServiceLoader
				.load(SecurityDataStoreServiceFactory.class, mediator.getClassLoader());

		Iterator<SecurityDataStoreServiceFactory> dataStoreServiceFactoryIterator = dataStoreServiceFactoryLoader
				.iterator();

		if (dataStoreServiceFactoryIterator.hasNext()) {
			SecurityDataStoreServiceFactory<?> factory = dataStoreServiceFactoryIterator.next();
			if (factory != null) {
				factory.newInstance(mediator);
			}
		}
		// do not constrain that a DataStoreService exists
		// if (dataStoreService == null)
		// {
		// throw new BundleException("A data store service was excepted");
		// }

		// org.eclipse.sensinact.user.manager capability required
		ServiceLoader<UserManagerFactory> userManagerFactoryLoader = ServiceLoader.load(UserManagerFactory.class,
				mediator.getClassLoader());

		Iterator<UserManagerFactory> userManagerFactoryIterator = userManagerFactoryLoader.iterator();

		if (userManagerFactoryIterator.hasNext()) {
			UserManagerFactory factory = userManagerFactoryIterator.next();
			if (factory != null) {
				factory.newInstance(mediator);
			}
		}
		// do not constrain that a UserManager exists
		// if (userManager == null)
		// {
		// throw new BundleException("A UserManager service was excepted");
		// }
		// org.eclipse.sensinact.security capability required
		ServiceLoader<SecuredAccessFactory> securedAccessFactoryLoader = ServiceLoader.load(SecuredAccessFactory.class,
				mediator.getClassLoader());

		Iterator<SecuredAccessFactory> securedAccessFactoryIterator = securedAccessFactoryLoader.iterator();

		if (securedAccessFactoryIterator.hasNext()) {
			SecuredAccessFactory factory = securedAccessFactoryIterator.next();
			if (factory != null) {
				securedAccess = factory.newInstance(mediator);
			}
		}
		if (securedAccess == null) {
			throw new BundleException("A SecuredAccess service was excepted");
		}
		securedAccess.createAuthorizationService();
		final SecuredAccess sa = securedAccess;

		AccessController.doPrivileged(new PrivilegedAction<Void>() {
			@Override
			public Void run() {
				mediator.register(sa, SecuredAccess.class, null);
				return null;
			}
		});

		this.defaultLocation = ModelInstance.defaultLocation(mediator);
		this.sessions = new Sessions();

		this.anonymousTree = mediator.callService(SecuredAccess.class,
				new Executable<SecuredAccess, AccessTree<? extends AccessNode>>() {
					@Override
					public AccessTree<? extends AccessNode> execute(SecuredAccess securedAccess) throws Exception {
						return securedAccess.getUserAccessTree(UserManager.ANONYMOUS_PKEY);
					}
				});
		this.mediator = mediator;
		this.registry = new RegistryEndpoint();
	}

	private final <R, P> R doPrivilegedService(final Class<P> p, final String f, final Executable<P, R> e) {
		R r = AccessController.<R>doPrivileged(new PrivilegedAction<R>() {
			@Override
			public R run() {
				return mediator.callService(p, f, e);
			}
		});
		return r;
	}

	private final <P> Void doPrivilegedVoidServices(final Class<P> p, final String f, final Executable<P, Void> e) {
		return AccessController.<Void>doPrivileged(new PrivilegedAction<Void>() {
			@Override
			public Void run() {
				mediator.<P>callServices(p, f, e);
				return null;
			}
		});
	}

	private final AccessTree<?> getAnonymousTree() {
		AccessTree<?> tree = null;
		if (MutableAccessTree.class.isAssignableFrom(this.anonymousTree.getClass())) {
			tree = ((MutableAccessTree<?>) this.anonymousTree).clone();
		} else {
			tree = this.anonymousTree;
		}
		return tree;
	}

	private final AccessTree<?> getUserAccessTree(final String publicKey) {
		AccessTree<? extends AccessNode> tree = null;
		if (publicKey != null && !publicKey.startsWith(UserManager.ANONYMOUS_PKEY)) {
			tree = doPrivilegedService(SecuredAccess.class, null,
					new Executable<SecuredAccess, AccessTree<? extends AccessNode>>() {
						@Override
						public AccessTree<? extends AccessNode> execute(SecuredAccess securedAccess) throws Exception {
							AccessTree<? extends AccessNode> tree = securedAccess.getUserAccessTree(publicKey);
							return tree;
						}
					});
		}
		if (tree == null) {
			tree = getAnonymousTree();
		}
		return tree;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Core#
	 *      getSession(org.eclipse.sensinact.gateway.core.security.Authentication)
	 */
	@Override
	public AuthenticatedSession getSession(final Authentication<?> authentication)
			throws InvalidKeyException, InvalidCredentialException {
		AuthenticatedSession session = null;
		if (authentication == null) {
			return null;

		} else if (Credentials.class.isAssignableFrom(authentication.getClass())) {
			UserKey userKey = this.doPrivilegedService(AuthenticationService.class, null,
					new Executable<AuthenticationService, UserKey>() {
						@Override
						public UserKey execute(AuthenticationService service) throws Exception {
							return service.buildKey((Credentials) authentication);
						}
					});
			if (userKey == null) {
				throw new InvalidCredentialException("Invalid credentials");
			}
			String pkey = userKey.getPublicKey();
			AccessTree<? extends AccessNode> tree = this.getUserAccessTree(pkey);
			SessionKey sessionKey = new SessionKey(mediator, LOCAL_ID, SensiNact.this.nextToken(), tree, null);
			sessionKey.setUserKey(userKey);
			session = new SensiNactAuthenticatedSession(sessionKey.getToken());
			sessions.put(sessionKey, session);
		} else if (AuthenticationToken.class.isAssignableFrom(authentication.getClass())) {
			session = this.getSession(((AuthenticationToken) authentication).getAuthenticationMaterial());
		}
		return session;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Core#getSession(java.lang.String)
	 */
	@Override
	public AuthenticatedSession getSession(final String token) {
		AuthenticatedSession session = (AuthenticatedSession) 
				this.sessions.getSessionFromToken(token);
		return session;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Core#getAnonymousSession()
	 */
	@Override
	public AnonymousSession getAnonymousSession() {
		int sessionCount = -1;
		String sessionToken = null;	
		
		synchronized(count) {
			sessionCount = count.incrementAndGet();
			sessionToken = this.nextToken();
		}
		String pkey = new StringBuilder().append(UserManager.ANONYMOUS_PKEY).append(
			"_").append(sessionCount).toString();
		SessionKey sessionKey = new SessionKey(mediator, LOCAL_ID, sessionToken, 
				this.getAnonymousTree(), null);
		sessionKey.setUserKey(new UserKey(pkey));
		AnonymousSession session = new SensiNactAnonymousSession(sessionToken);
		this.sessions.put(sessionKey, session);
		return session;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Core#
	 *      getApplicationSession(java.lang.String)
	 */
	@Override
	public Session getApplicationSession(final Mediator mediator, final String privateKey) {

		final int sessionCount;
		final String sessionToken;	
		
		synchronized(count) {
			sessionCount = count.incrementAndGet();
			sessionToken = this.nextToken();
		}
		SessionKey skey = this.doPrivilegedService(SecuredAccess.class, null,
		new Executable<SecuredAccess, SessionKey>() {
			@Override
			public SessionKey execute(SecuredAccess securedAccess) throws Exception {
				String publicKey = securedAccess.getApplicationPublicKey(privateKey);
				AccessTree<? extends AccessNode> tree = null;
				if (publicKey == null) {
					publicKey = new StringBuilder().append(UserManager.ANONYMOUS_PKEY
						).append("_").append(sessionCount).toString();
					tree = SensiNact.this.getAnonymousTree();
				} else {
					tree = securedAccess.getApplicationAccessTree(publicKey);
				}
				SessionKey sessionKey = new SessionKey(mediator, LOCAL_ID, 
					sessionToken, tree, null);

				sessionKey.setUserKey(new UserKey(publicKey));
				return sessionKey;
			}
		});
		Session session = new SensiNactAuthenticatedSession(skey.getToken());
		sessions.put(skey, session);
		return session;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Core#namespace()
	 */
	@Override
	public String namespace() {
		return this.namespace;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Core#
	 *      registerAgent(org.eclipse.sensinact.gateway.common.bundle.Mediator,
	 *      org.eclipse.sensinact.gateway.core.message.MidAgentCallback,
	 *      org.eclipse.sensinact.gateway.core.message.SnaFilter)
	 */
	@Override
	public String registerAgent(final Mediator mediator, final MidAgentCallback callback, final SnaFilter filter) {
		final Bundle bundle = mediator.getContext().getBundle();

		final String bundleIdentifier = this.doPrivilegedService(BundleValidation.class, null,
				new Executable<BundleValidation, String>() {
					@Override
					public String execute(BundleValidation bundleValidation) throws Exception {
						return bundleValidation.check(bundle);
					}
				});
		final String agentKey = this.doPrivilegedService(SecuredAccess.class, null,
				new Executable<SecuredAccess, String>() {
					@Override
					public String execute(SecuredAccess securedAccess) throws Exception {
						return securedAccess.getAgentPublicKey(bundleIdentifier);
					}
				});
		final SnaAgentImpl agent = SnaAgentImpl.createAgent(mediator, callback, filter, agentKey);

		String identifier = new StringBuilder().append("agent_").append(agent.hashCode()).toString();
		callback.setIdentifier(identifier);

		AccessController.<Void>doPrivileged(new PrivilegedAction<Void>() {
			@Override
			public Void run() {
				agent.start(true);
				return null;
			}
		});
		return identifier;
	}

	/**
	 * Unregisters the {@link SnaAgent} whose identifier is passed as parameter
	 * 
	 * @param identifier
	 *            the identifier of the {@link SnaAgent} to register
	 */
	public void unregisterAgent(final String identifier) {
		doPrivilegedService(SnaAgent.class,
			new StringBuilder().append("(&(org.eclipse.sensinact.gateway.agent.id=").append(identifier)
					.append(")(org.eclipse.sensinact.gateway.agent.local=true))").toString(),
			new Executable<SnaAgent, Void>() {
				@Override
				public Void execute(SnaAgent agent) throws Exception {
					agent.stop(true);
					return null;
				}
			}
		);
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Core#
	 *      createRemoteCore(org.eclipse.sensinact.gateway.core.AbstractRemoteEndpoint)
	 */
	@Override
	public void createRemoteCore(AbstractRemoteEndpoint remoteEndpoint) {
		this.createRemoteCore(remoteEndpoint, Collections.<Executable<String, Void>>emptyList(),
				Collections.<Executable<String, Void>>emptyList());
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Core#
	 *      createRemoteCore(org.eclipse.sensinact.gateway.core.AbstractRemoteEndpoint,
	 *      java.util.Collection, java.util.Collection)
	 */
	@Override
	public void createRemoteCore(final AbstractRemoteEndpoint remoteEndpoint,
			Collection<Executable<String, Void>> onConnectedCallbacks,
			Collection<Executable<String, Void>> onDisconnectedCallbacks) {	

		final int sessionCount;
		synchronized(this.count) {
			sessionCount = count.incrementAndGet();
		}
		final RemoteSensiNact remoteCore = new RemoteSensiNact(mediator, new LocalEndpoint(sessionCount) {
			private Map<String, Session> remoteSessions = new HashMap<String, Session>();

			private Session createSession(String publicKey) {
				
				Session session = null;
				synchronized(this.remoteSessions) {
					String filteredKey = publicKey;
					Class<? extends Session> sessionClass = null;
	
					if (publicKey.startsWith(UserManager.ANONYMOUS_PKEY)) {
						filteredKey = new StringBuilder().append(publicKey).append("_remote"
							).append(super.localID()).toString();
						sessionClass = SensiNactAnonymousSession.class;
					} else {
						sessionClass = SensiNactAuthenticatedSession.class;
					}
					final String sessionToken;				
					synchronized(SensiNact.this.count) {
						sessionToken = SensiNact.this.nextToken();
					}
					AccessTree<? extends AccessNode> tree = SensiNact.this.getUserAccessTree(filteredKey);
					SessionKey sessionKey = new SessionKey(mediator, localID(), sessionToken, tree,remoteEndpoint);	
					sessionKey.setUserKey(new UserKey(filteredKey));					
					try {
						session = sessionClass.getDeclaredConstructor(
							new Class<?>[] { SensiNact.class, String.class }).newInstance(
									new Object[] { SensiNact.this, sessionKey.getToken() });
						SensiNact.this.sessions.put(sessionKey, session);
						this.remoteSessions.put(filteredKey, session);
					} catch (Exception e) {
						e.printStackTrace();
						mediator.error(e);
					}
				}
				return session;
			}

			@Override
			public Session getSession(String publicKey) {
				String filteredKey = publicKey;
				if (publicKey.startsWith(UserManager.ANONYMOUS_PKEY)) {
					filteredKey = new StringBuilder().append(publicKey).append("_remote"
							).append(super.localID()).toString();
				}
				Session session = null;
				synchronized(this.remoteSessions) {
					session = this.remoteSessions.get(filteredKey);
				}
				if (session == null) {
					session = createSession(publicKey);
				}
				return session;
			}

			@Override
			void closeSession(String publicKey) {
				String filteredKey = publicKey;
				if (publicKey.startsWith(UserManager.ANONYMOUS_PKEY)) {
					filteredKey = new StringBuilder().append(publicKey).append("_remote"
							).append(super.localID()).toString();
				}
				Session session = null;
				synchronized(this.remoteSessions) {
					session = this.remoteSessions.remove(filteredKey);
				}
				if(session == null) {
					return;
				}
				SessionKey sessionKey = SensiNact.this.sessions.get(session);
				sessionKey.unregisterAgents();
			}

			@Override
			void close() {
				synchronized(this.remoteSessions) {
					String[] keys = this.remoteSessions.keySet().toArray(new String[0]);
					int index = 0;
					while (index < keys.length) {			
						Session session = this.remoteSessions.remove(keys[index]);
						SessionKey sessionKey = SensiNact.this.sessions.get(new KeyExtractor<KeyExtractorType>(
							KeyExtractorType.TOKEN, session.getSessionId()));
						sessionKey.unregisterAgents();
						index++;
					}
				}
			}

			@Override
			String localNamespace() {
				return SensiNact.this.namespace();
			}
		});
		remoteCore.onConnected(onConnectedCallbacks);
		remoteCore.onDisconnected(onDisconnectedCallbacks);
		remoteCore.onConnected(Collections.<Executable<String, Void>>singletonList(new Executable<String, Void>() {
			@Override
			public Void execute(String parameter) throws Exception {
				mediator.callServices(SnaAgent.class, "(org.eclipse.sensinact.gateway.agent.local=true)",
						new Executable<SnaAgent, Void>() {
							@Override
							public Void execute(SnaAgent agent) throws Exception {
								((SnaAgentImpl) agent).registerRemote(remoteCore);
								return null;
							}
						});
				return null;
			}
		}));
		remoteCore.open(remoteEndpoint);
	}

	/**
	 * Unregisters the {@link RemoteCore} whose String namespace is passed as
	 * parameter
	 * 
	 * @param namespace
	 *            the String namespace of the {@link RemoteCore} to be unregistered
	 */
	protected void unregisterEndpoint(final String namespace) {
		if (namespace == null) {
			return;
		}
		this.doPrivilegedService(RemoteCore.class, String.format("(namespace=%s)", namespace),
				new Executable<RemoteCore, Void>() {
					@Override
					public Void execute(RemoteCore remoteCore) throws Exception {
						remoteCore.close();
						return null;
					}
				});
	}

	/**
	 * Returns the Set of available {@link ServiceProvider}s compliant to the LDAP
	 * formated filter passed as parameter
	 * 
	 * @param identifier
	 *            the String identifier of the {@link Session} requiring the list of
	 *            available service providers
	 * @param filter
	 *            the String LDAP formated filter
	 * 
	 * @return the Set of available {@link ServiceProvider}s compliant to the
	 *         specified filter and for the specified {@link Session}
	 */
	protected Set<ServiceProvider> serviceProviders(String identifier, String filter) {
		final SessionKey sessionKey = sessions
				.get(new KeyExtractor<KeyExtractorType>(KeyExtractorType.TOKEN, identifier));

		Set<ServiceProvider> set = new HashSet<ServiceProvider>();
		set.addAll(this.registry.serviceProviders(sessionKey, filter));
		return set;
	}

	/**
	 * Returns the {@link ServiceProvider} whose String identifier is passed as
	 * parameter
	 * 
	 * @param identifier
	 *            the String identifier of the {@link Session} requiring the service
	 *            provider
	 * @param serviceProviderId
	 *            the String identifier of the service provider
	 * 
	 * @return the {@link ServiceProvider}
	 */
	protected ServiceProvider serviceProvider(String identifier, String serviceProviderId) {
		final SessionKey sessionKey = sessions
				.get(new KeyExtractor<KeyExtractorType>(KeyExtractorType.TOKEN, identifier));

		return this.registry.serviceProvider(sessionKey, serviceProviderId);
	}

	/**
	 * Returns the {@link Service} whose String identifier is passed as parameter,
	 * held by the specified service provider
	 * 
	 * @param identifier
	 *            the String identifier of the {@link Session} requiring the service
	 * @param serviceProviderId
	 *            the String identifier of the service provider holding the service
	 * @param servideId
	 *            the String identifier of the service
	 * 
	 * @return the {@link Service}
	 */
	protected Service service(String identifier, String serviceProviderId, String serviceId) {
		final SessionKey sessionKey = sessions
				.get(new KeyExtractor<KeyExtractorType>(KeyExtractorType.TOKEN, identifier));

		return this.registry.service(sessionKey, serviceProviderId, serviceId);
	}

	/**
	 * Returns the {@link Resource} whose String identifier is passed as parameter,
	 * held by the specified service provider and service
	 * 
	 * @param identifier
	 *            the String identifier of the {@link Session} requiring the
	 *            resource
	 * @param serviceProviderId
	 *            the String identifier of the service provider holding the service
	 *            providing the resource
	 * @param servideId
	 *            the String identifier of the service providing the resource
	 * @param resourceId
	 *            the String identifier of the resource
	 * 
	 * @return the {@link Resource}
	 */
	protected Resource resource(String identifier, String serviceProviderId, String serviceId, String resourceId) {
		final SessionKey sessionKey = sessions
				.get(new KeyExtractor<KeyExtractorType>(KeyExtractorType.TOKEN, identifier));

		return this.registry.resource(sessionKey, serviceProviderId, serviceId, resourceId);
	}

	/**
	 * Executes the {@link Executable} passed as parameter which expects a
	 * {@link RemoteCore} parameter, and returns its JSON formated execution result
	 * 
	 * @param serviceProviderId
	 *            the String identifier of the service provider whose prefix allows
	 *            to identified the targeted {@link RemoteCore}
	 * @param executable
	 *            the {@link Executable} to be executed
	 * 
	 * @return the JSON formated result of the {@link Executable} execution
	 */
	private <F> F remoteCoreInvocation(String serviceProviderId, Executable<RemoteCore, F> executable) {
		String[] serviceProviderIdElements = serviceProviderId.split(":");
		String domain = serviceProviderIdElements[0];
		F object = null;

		if (serviceProviderIdElements.length == 1 || domain.length() == 0) {
			return (F) null;
		}
		object = mediator.callService(RemoteCore.class,
				new StringBuilder().append("(namespace=").append(domain).append(")").toString(), executable);

		return object;
	}

	/**
	 * Returns the JSON formated description of the resource whose String identifier
	 * is passed as parameter, and held by the service provider and service whose
	 * String identifiers are also passed as parameter
	 * 
	 * @param identifier
	 *            the String identifier of the {@link Session} requiring the
	 *            resource description
	 * @param serviceProviderId
	 *            the String identifier of the service provider holding the service,
	 *            providing the resource
	 * @param serviceId
	 *            the String identifier of the service providing the resource
	 * @param resourceId
	 *            the String identifier of the resource to return the description of
	 * 
	 * @return the JSON formated description of the specified resource
	 */
	protected JSONObject getResource(String identifier, final String serviceProviderId, final String serviceId,
			final String resourceId) {
		final SessionKey sessionKey = sessions
				.get(new KeyExtractor<KeyExtractorType>(KeyExtractorType.TOKEN, identifier));

		JSONObject object = remoteCoreInvocation(serviceProviderId, new Executable<RemoteCore, JSONObject>() {
			@Override
			public JSONObject execute(RemoteCore connector) throws Exception {
				if (connector == null) {
					return null;
				}
				return new JSONObject(connector.endpoint().getResource(sessionKey.getPublicKey(),
						serviceProviderId.substring(serviceProviderId.indexOf(':') + 1), serviceId, resourceId));
			}
		});
		return object;
	}

	/**
	 * Returns the JSON formated list of available resources, for the service and
	 * service provider whose String identifiers are passed as parameter
	 * 
	 * @param identifier
	 *            the String identifier of the {@link Session} requiring the
	 *            description
	 * @param serviceProviderId
	 *            the String identifier of the service provider holding the service
	 * @param serviceId
	 *            the String identifier of the service providing the resources
	 * 
	 * @return the JSON formated list of available resources for the specified
	 *         service and service provider
	 */
	protected String getResources(String identifier, final String serviceProviderId, final String serviceId) {
		final SessionKey sessionKey = sessions
				.get(new KeyExtractor<KeyExtractorType>(KeyExtractorType.TOKEN, identifier));

		String object = remoteCoreInvocation(serviceProviderId, new Executable<RemoteCore, String>() {
			@Override
			public String execute(RemoteCore connector) throws Exception {
				if (connector == null) {
					return null;
				}
				return connector.endpoint().getResources(sessionKey.getPublicKey(),
						serviceProviderId.substring(serviceProviderId.indexOf(':') + 1), serviceId);
			}
		});
		return object;
	}

	/**
	 * Returns the JSON formated description of the service whose String identifier
	 * is passed as parameter, and held by the specified service provider
	 * 
	 * @param identifier
	 *            the String identifier of the {@link Session} requiring the service
	 *            description
	 * @param serviceProviderId
	 *            the String identifier of the service provider holding the service
	 * @param serviceId
	 *            the String identifier of the service to return the description of
	 * 
	 * @return the JSON formated description of the specified service
	 */
	protected JSONObject getService(String identifier, final String serviceProviderId, final String serviceId) {
		final SessionKey sessionKey = sessions
				.get(new KeyExtractor<KeyExtractorType>(KeyExtractorType.TOKEN, identifier));

		JSONObject object = remoteCoreInvocation(serviceProviderId, new Executable<RemoteCore, JSONObject>() {
			@Override
			public JSONObject execute(RemoteCore connector) throws Exception {
				if (connector == null) {
					return null;
				}
				return new JSONObject(connector.endpoint().getService(sessionKey.getPublicKey(),
						serviceProviderId.substring(serviceProviderId.indexOf(':') + 1), serviceId));
			}
		});
		return object;
	}

	/**
	 * Returns the JSON formated list of available services for the service provider
	 * whose String identifier is passed as parameter
	 * 
	 * @param identifier
	 *            the String identifier of the {@link Session} requiring the list of
	 *            available services
	 * @param serviceProviderId
	 *            the String identifier of the service provider holding the services
	 * 
	 * @return the JSON formated list of available services for the specified
	 *         service provider
	 */
	protected String getServices(String identifier, final String serviceProviderId) {
		final SessionKey sessionKey = sessions
				.get(new KeyExtractor<KeyExtractorType>(KeyExtractorType.TOKEN, identifier));

		String object = remoteCoreInvocation(serviceProviderId, new Executable<RemoteCore, String>() {
			@Override
			public String execute(RemoteCore connector) throws Exception {
				if (connector == null) {
					return null;
				}
				return connector.endpoint().getServices(sessionKey.getPublicKey(),
						serviceProviderId.substring(serviceProviderId.indexOf(':') + 1));
			}
		});
		return object;
	}

	/**
	 * Returns the JSON formated description of the service provider whose String
	 * identifier is passed as parameter
	 * 
	 * @param identifier
	 *            the String identifier of the {@link Session} requiring the service
	 *            provider description
	 * @param serviceProviderId
	 *            the String identifier of the service provider to return the
	 *            description of
	 * 
	 * @return the JSON formated description of the specified service provider
	 */
	protected JSONObject getProvider(String identifier, final String serviceProviderId) {
		final SessionKey sessionKey = sessions
				.get(new KeyExtractor<KeyExtractorType>(KeyExtractorType.TOKEN, identifier));

		JSONObject object = remoteCoreInvocation(serviceProviderId, new Executable<RemoteCore, JSONObject>() {
			@Override
			public JSONObject execute(RemoteCore connector) throws Exception {
				if (connector == null) {
					return null;
				}
				return new JSONObject(connector.endpoint().getProvider(sessionKey.getPublicKey(),
						serviceProviderId.substring(serviceProviderId.indexOf(':') + 1)));
			}
		});
		return object;
	}

	/**
	 * Invokes the UNSUBSCRIBE access method on the resource whose String identifier
	 * is passed as parameter, held by the specified service provider and service
	 * 
	 * @param identifier
	 *            the String identifier of the {@link Session} invoking the access
	 *            method
	 * @param serviceProviderId
	 *            the String identifier of the service provider holding the service
	 *            providing the resource on which applies the access method call
	 * @param serviceId
	 *            the String identifier of the service providing the resource on
	 *            which applies the access method call
	 * @param resourceId
	 *            the String identifier of the resource on which applies the access
	 *            method call
	 * @param subscriptionId
	 *            the String identifier of the subscription to be deleted
	 * 
	 * @return the JSON formated response of the UNSUBSCRIBE access method
	 *         invocation
	 */
	protected JSONObject unsubscribe(String identifier, final String serviceProviderId, final String serviceId,
			final String resourceId, final String subscriptionId) {
		final SessionKey sessionKey = sessions
				.get(new KeyExtractor<KeyExtractorType>(KeyExtractorType.TOKEN, identifier));

		JSONObject object = remoteCoreInvocation(serviceProviderId,
				new RemoteAccessMethodExecutable(AccessMethod.Type.valueOf(AccessMethod.UNSUBSCRIBE),
						sessionKey.getPublicKey()).withServiceProvider(serviceProviderId).withService(serviceId)
								.withResource(resourceId)
								.with(RemoteAccessMethodExecutable.SUBSCRIPTION_ID_TK, subscriptionId));
		return object;
	}

	/**
	 * Invokes the SUBSCRIBE access method on the resource whose String identifier
	 * is passed as parameter, held by the specified service provider and service
	 * 
	 * @param identifier
	 *            the String identifier of the {@link Session} invoking the access
	 *            method
	 * @param serviceProviderId
	 *            the String identifier of the service provider holding the service
	 *            providing the resource on which applies the access method call
	 * @param serviceId
	 *            the String identifier of the service providing the resource on
	 *            which applies the access method call
	 * @param resourceId
	 *            the String identifier of the resource on which applies the access
	 *            method call
	 * @param recipient
	 *            the {@link Recipient} to which the update events generated by the
	 *            subscription will be transmitted
	 * @param conditions
	 *            the JSON formated set of constraints applying on the subscription
	 *            to be created
	 * 
	 * @return the JSON formated response of the SUBSCRIBE access method invocation
	 */
	protected JSONObject subscribe(String identifier, final String serviceProviderId, final String serviceId,
			final String resourceId, final Recipient recipient, final JSONArray conditions) {
		final SessionKey sessionKey = sessions
				.get(new KeyExtractor<KeyExtractorType>(KeyExtractorType.TOKEN, identifier));

		JSONObject object = remoteCoreInvocation(serviceProviderId,
				new RemoteAccessMethodExecutable(AccessMethod.Type.valueOf(AccessMethod.SUBSCRIBE),
						sessionKey.getPublicKey()).withServiceProvider(serviceProviderId).withService(serviceId)
								.withResource(resourceId).with(RemoteAccessMethodExecutable.RECIPIENT_TK, recipient)
								.with(RemoteAccessMethodExecutable.CONDITIONS_TK, conditions));
		return object;
	}

	/**
	 * Invokes the ACT access method on the resource whose String identifier is
	 * passed as parameter, held by the specified service provider and service
	 * 
	 * @param identifier
	 *            the String identifier of the {@link Session} invoking the access
	 *            method
	 * @param serviceProviderId
	 *            the String identifier of the service provider holding the service
	 *            providing the resource on which applies the access method call
	 * @param serviceId
	 *            the String identifier of the service providing the resource on
	 *            which applies the access method call
	 * @param resourceId
	 *            the String identifier of the resource on which applies the access
	 *            method call
	 * @param parameters
	 *            the Objects array parameterizing the call
	 * 
	 * @return the JSON formated response of the ACT access method invocation
	 */
	protected JSONObject act(String identifier, final String serviceProviderId, final String serviceId,
			final String resourceId, final Object[] parameters) {
		final SessionKey sessionKey = sessions
				.get(new KeyExtractor<KeyExtractorType>(KeyExtractorType.TOKEN, identifier));

		JSONObject object = remoteCoreInvocation(serviceProviderId,
				new RemoteAccessMethodExecutable(AccessMethod.Type.valueOf(AccessMethod.ACT), sessionKey.getPublicKey())
						.withServiceProvider(serviceProviderId).withService(serviceId).withResource(resourceId)
						.with(RemoteAccessMethodExecutable.ARGUMENTS_TK, parameters));
		return object;
	}

	/**
	 * Invokes the SET access method on the resource whose String identifier is
	 * passed as parameter, held by the specified service provider and service
	 * 
	 * @param identifier
	 *            the String identifier of the {@link Session} invoking the access
	 *            method
	 * @param serviceProviderId
	 *            the String identifier of the service provider holding the service
	 *            providing the resource on which applies the access method call
	 * @param serviceId
	 *            the String identifier of the service providing the resource on
	 *            which applies the access method call
	 * @param resourceId
	 *            the String identifier of the resource on which applies the access
	 *            method call
	 * @param attributeId
	 *            the String identifier of the resource's attribute targeted by the
	 *            access method call
	 * @param parameter
	 *            the value object to be set
	 * 
	 * @return the JSON formated response of the SET access method invocation
	 */
	protected JSONObject set(String identifier, final String serviceProviderId, final String serviceId,
			final String resourceId, final String attributeId, final Object parameter) {
		final SessionKey sessionKey = sessions
				.get(new KeyExtractor<KeyExtractorType>(KeyExtractorType.TOKEN, identifier));

		JSONObject object = remoteCoreInvocation(serviceProviderId,
				new RemoteAccessMethodExecutable(AccessMethod.Type.valueOf(AccessMethod.SET), sessionKey.getPublicKey())
						.withServiceProvider(serviceProviderId).withService(serviceId).withResource(resourceId)
						.withAttribute(attributeId).with(RemoteAccessMethodExecutable.VALUE_TK, parameter));
		return object;
	}

	/**
	 * Invokes the GET access method on the resource whose String identifier is
	 * passed as parameter, held by the specified service provider and service
	 * 
	 * @param identifier
	 *            the String identifier of the {@link Session} invoking the access
	 *            method
	 * @param serviceProviderId
	 *            the String identifier of the service provider holding the service
	 *            providing the resource on which applies the access method call
	 * @param serviceId
	 *            the String identifier of the service providing the resource on
	 *            which applies the access method call
	 * @param resourceId
	 *            the String identifier of the resource on which applies the access
	 *            method call
	 * @param attributeId
	 *            the String identifier of the resource's attribute targeted by the
	 *            access method call
	 * 
	 * @return the JSON formated response of the GET access method invocation
	 */
	protected JSONObject get(String identifier, final String serviceProviderId, final String serviceId,
			final String resourceId, final String attributeId) {
		final SessionKey sessionKey = sessions
				.get(new KeyExtractor<KeyExtractorType>(KeyExtractorType.TOKEN, identifier));

		JSONObject object = remoteCoreInvocation(serviceProviderId,
				new RemoteAccessMethodExecutable(AccessMethod.Type.valueOf(AccessMethod.GET), sessionKey.getPublicKey())
						.withServiceProvider(serviceProviderId).withService(serviceId).withResource(resourceId)
						.withAttribute(attributeId));
		return object;
	}

	/**
	 * Returns the JSON formated list of available service providers for the
	 * {@link Session} whose String identifier is passed as parameter
	 * 
	 * @param identifier
	 *            the String identifier of the {@link Session} requiring the list of
	 *            available service providers
	 * @param filter
	 * @param filterDefinition
	 * 
	 * @return the JSON formated list of available service providers
	 */
	protected String getProviders(String identifier, String filter) {
		final SessionKey sessionKey = sessions
				.get(new KeyExtractor<KeyExtractorType>(KeyExtractorType.TOKEN, identifier));

		String effectiveFilter = null;
		if (filter != null && filter.length() > 0) {
			try {
				mediator.getContext().createFilter(filter);
				effectiveFilter = filter;

			} catch (InvalidSyntaxException e) {
				effectiveFilter = null;
			}
		}
		String local = this.registry.getProviders(sessionKey, sessionKey.localID() != 0, effectiveFilter);

		if (sessionKey.localID() != 0) {
			return local;
		}
		final StringBuilder content = new StringBuilder();
		if (local != null && local.length() > 0) {
			content.append(local);
		}
		SensiNact.this.doPrivilegedVoidServices(RemoteCore.class, null, new Executable<RemoteCore, Void>() {
			@Override
			public Void execute(RemoteCore core) throws Exception {
				String o = core.endpoint().getProviders(sessionKey.getPublicKey());

				if (o != null && o.length() > 0) {
					if (content.length() > 0) {
						content.append(",");
					}
					content.append(o);
				}
				return null;
			}
		});
		return content.toString();
	}

	/**
	 * Returns the JSON formated list of all registered resource model instances,
	 * accessible by the {@link Session} whose String identifier is passed as
	 * parameter and compliant to the specified String LDAP formated filter.
	 * 
	 * @param identifier
	 *            the String identifier of the {@link Session} for which to retrieve
	 *            the list of accessible resource model instances
	 * @return the JSON formated list of the resource model instances for the
	 *         specified {@link Session} and compliant to the specified filter.
	 */
	protected String getAll(String identifier, final String filter) {
		final SessionKey sessionKey = sessions
				.get(new KeyExtractor<KeyExtractorType>(KeyExtractorType.TOKEN, identifier));

		String effectiveFilter = null;
		if (filter != null && filter.length() > 0) {
			try {
				mediator.getContext().createFilter(filter);
				effectiveFilter = filter;

			} catch (InvalidSyntaxException e) {
				effectiveFilter = null;
			}
		}
		String local = this.registry.getAll(sessionKey, sessionKey.localID() != 0, effectiveFilter);

		if (sessionKey.localID() != 0) {
			return local;
		}
		final StringBuilder content = new StringBuilder();
		if (local != null && local.length() > 0) {
			content.append(local);
		}
		SensiNact.this.doPrivilegedVoidServices(RemoteCore.class, null, new Executable<RemoteCore, Void>() {
			@Override
			public Void execute(RemoteCore core) throws Exception {
				String o = core.endpoint().getAll(sessionKey.getPublicKey(), filter);

				if (o != null && o.length() > 0) {
					if (content.length() > 0) {
						content.append(",");
					}
					content.append(o);
				}
				return null;
			}
		});
		return content.toString();
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Core#close()
	 */
	public void close() {
		mediator.debug("closing sensiNact core");
		AccessController.<Void>doPrivileged(new PrivilegedAction<Void>() {
			@Override
			public Void run() {
				SensiNact.this.mediator.callServices(SensiNactResourceModel.class,
						new Executable<SensiNactResourceModel, Void>() {
							@Override
							public Void execute(SensiNactResourceModel instance) throws Exception {
								instance.unregister();
								return null;
							}
						});
				SensiNact.this.mediator.callServices(RemoteCore.class, new Executable<RemoteCore, Void>() {
					@Override
					public Void execute(RemoteCore instance) throws Exception {
						instance.close();
						return null;
					}
				});
				SensiNact.this.mediator.callServices(SnaAgent.class, new Executable<SnaAgent, Void>() {
					@Override
					public Void execute(SnaAgent agent) throws Exception {
						agent.stop(true);
						return null;
					}
				});
				return null;
			}
		});
	}

	/**
	 */
	String nextToken() {
		boolean exists = false;
		String token = null;
		do {
			try {
				token = CryptoUtils.createToken();
			} catch (InvalidKeyException e) {
				token = Long.toHexString(System.currentTimeMillis());
			}
			exists = this.sessions.get(new Sessions.KeyExtractor<Sessions.KeyExtractorType>(
					Sessions.KeyExtractorType.TOKEN, token)) != null;
		} while (exists);
		return token;
	}

}
