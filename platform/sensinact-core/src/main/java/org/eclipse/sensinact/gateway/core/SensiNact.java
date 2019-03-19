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

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.constraint.Constraint;
import org.eclipse.sensinact.gateway.common.constraint.ConstraintFactory;
import org.eclipse.sensinact.gateway.common.constraint.InvalidConstraintDefinitionException;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.Sessions.KeyExtractor;
import org.eclipse.sensinact.gateway.core.Sessions.KeyExtractorType;
import org.eclipse.sensinact.gateway.core.api.Sensinact;
import org.eclipse.sensinact.gateway.core.api.SensinactCoreBaseIface;
import org.eclipse.sensinact.gateway.core.message.*;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse.Status;
import org.eclipse.sensinact.gateway.core.method.RemoteAccessMethodExecutable;
import org.eclipse.sensinact.gateway.core.method.legacy.*;
import org.eclipse.sensinact.gateway.core.method.legacy.DescribeMethod.DescribeType;
import org.eclipse.sensinact.gateway.core.remote.AbstractRemoteEndpoint;
import org.eclipse.sensinact.gateway.core.remote.LocalEndpoint;
import org.eclipse.sensinact.gateway.core.remote.RemoteCore;
import org.eclipse.sensinact.gateway.core.remote.RemoteSensiNact;
import org.eclipse.sensinact.gateway.core.security.*;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;
import org.eclipse.sensinact.gateway.security.signature.api.BundleValidation;
import org.eclipse.sensinact.gateway.util.CryptoUtils;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.framework.*;
import org.osgi.service.component.annotations.Component;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import java.security.AccessController;
import java.security.InvalidKeyException;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link Core} service implementation
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
//@Component(immediate = false)
public class SensiNact implements Sensinact,Core {
	// ********************************************************************//
	// NESTED DECLARATIONS //
	// ********************************************************************//

	List<MessageRegisterer> messageRegisterers =Collections.synchronizedList(new ArrayList<MessageRegisterer>());
	List<MidAgentCallback> messageAgentCallback =Collections.synchronizedList(new ArrayList<MidAgentCallback>());

	//public List<SensinactCoreBaseIface> sensinactRemote=Collections.synchronizedList(new ArrayList<SensinactCoreBaseIface>());
	public Map<String,SensinactCoreBaseIface> sensinactRemote=Collections.synchronizedMap(new HashMap<String,SensinactCoreBaseIface>());

	public void notifyCallbacks(SnaMessage message){

		for(MessageRegisterer registerer: messageRegisterers){
			registerer.register(message);
		}

		for(MidAgentCallback callback: messageAgentCallback){
			try{
				if(message instanceof SnaLifecycleMessage){
					callback.doHandle((SnaLifecycleMessageImpl)message);
				}else if(message instanceof SnaUpdateMessage) {
					callback.doHandle((SnaUpdateMessageImpl) message);
				}
			}catch(Exception e){
				e.printStackTrace();
			}


		}

	}
	/**
	 * Abstract {@link Session} service implementation
	 */
	public abstract class SensiNactSession extends AbstractSession {
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
		public SubscribeResponse registerSessionAgent(String requestId, 
				final MidAgentCallback callback, final SnaFilter filter) {

			System.out.println("Register Session*****");



			boolean registered = AccessController.<Boolean>doPrivileged(new PrivilegedAction<Boolean>() {
				@Override
				public Boolean run() {
					SessionKey sessionKey = SensiNact.this.sessions.get(
					new KeyExtractor<KeyExtractorType>(KeyExtractorType.TOKEN, getSessionId()));
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
			if (registered) {
				response = new SubscribeResponse(mediator, uri, Status.SUCCESS, 200);
				response.setResponse(new JSONObject().put("subscriptionId", callback.getName()));
				messageAgentCallback.add(callback);
			} else {
				response = SensiNact.<JSONObject, SubscribeResponse>createErrorResponse(
					mediator, AccessMethod.SUBSCRIBE, uri, 520, "Unable to subscribe", 
					    null);
			}
			return tatooRequestId(requestId, response);
		}
		
		/**
		 * @inheritDoc
		 *
		 * @see org.eclipse.sensinact.gateway.core.Session#unregisterSessionAgent(java.lang.String)
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

		@Override
		public SubscribeResponse registerSessionIntent(String requestId, Executable<Boolean, Void> callback,
				String... resourcePath) {
			final SessionKey sessionKey = SensiNact.this.sessions.get(
					new KeyExtractor<KeyExtractorType>(KeyExtractorType.TOKEN, getSessionId()));
			final ResourceIntent intent = new ResourceIntent(mediator, sessionKey.getPublicKey(), callback, resourcePath) {
				@Override
				public boolean isAccessible(String path) {	
					return SensiNactSession.this.isAccessible(path);
				}

				@Override
				public String namespace() {
					return SensiNact.this.namespace();
				}

			}; 
			boolean registered = AccessController.<Boolean>doPrivileged(new PrivilegedAction<Boolean>() {
				@Override
				public Boolean run() {
					return sessionKey.registerAgent(intent.getName(), intent);
				}
			});
			SubscribeResponse response = null;
			if (registered) {
				response = new SubscribeResponse(mediator, intent.getCommonPath(), Status.SUCCESS, 200);
				response.setResponse(new JSONObject().put("subscriptionId", intent.getName()));
			} else {
				response = SensiNact.<JSONObject, SubscribeResponse>createErrorResponse(
					mediator, AccessMethod.SUBSCRIBE, intent.getCommonPath(), 520, "Unable to subscribe", 
					    null);
			}
			return tatooRequestId(requestId, response);
		}

		@Override
		public UnsubscribeResponse unregisterSessionIntent(String requestId, final String intentId) {
			boolean unregistered = AccessController.<Boolean>doPrivileged(new PrivilegedAction<Boolean>() {
				@Override
				public Boolean run() {
					SessionKey key = SensiNact.this.sessions.get(new KeyExtractor<KeyExtractorType>(
							KeyExtractorType.TOKEN, getSessionId()));
					if (key != null && key.getPublicKey() != null) {
						return key.unregisterAgent(intentId);
					}
					return false;
				}
			});
			UnsubscribeResponse response = null;
			if (unregistered) {
				response = new UnsubscribeResponse(mediator, UriUtils.PATH_SEPARATOR, Status.SUCCESS, 200);
				response.setResponse(new JSONObject().put("message", "The intent has been properly unregistered"));

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


			/*
			JSONObject object = AccessController.doPrivileged(new PrivilegedAction<JSONObject>() {
				@Override
				public JSONObject run() {
					return SensiNact.this.get(SensiNactSession.this.getSessionId(), serviceProviderId, serviceId,
							resourceId, attributeId);
				}
			});
			*/

			final boolean isRemoteProvider=serviceProviderId.contains(":");

			if(isRemoteProvider){
				final String remoteNamespace=serviceProviderId.split(":")[0];
				SensinactCoreBaseIface sensinactRemoteRef=sensinactRemote.get(remoteNamespace);
				final String remoteProviderName=serviceProviderId.split(":")[1];
				String jsonInStringRemote=sensinactRemoteRef.get(SensiNactSession.this.getSessionId(), remoteProviderName, serviceId,resourceId, attributeId);
				System.out.println("Received from remote "+jsonInStringRemote);
				try {
					GetResponseBuilder builder = new GetResponseBuilder(mediator, uri,null);
					builder.setAccessMethodObjectResult(new JSONObject(jsonInStringRemote));

					return builder.createAccessMethodResponse();
				} catch (Exception e) {
					e.printStackTrace();
					response = SensiNact.<JSONObject, GetResponse>createErrorResponse(mediator, AccessMethod.GET, uri,
							SnaErrorfulMessage.INTERNAL_SERVER_ERROR_CODE, "Internal server error", e);
				}

			}else {
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
			/*
			final boolean isRemoteProvider=serviceProviderId.contains(":");

			if(!isRemoteProvider){

			}else {
				final String remoteNamespace=serviceProviderId.split(":")[0];
				final String remoteProviderName=serviceProviderId.split(":")[1];
				new JSONObject(sensinactRemote.get(remoteNamespace).sub
			}
			*/


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

				DescribeResponse<JSONObject> responseB=builder.createAccessMethodResponse();

				System.out.println("On the session="+responseB.getJSON().toString());
				return tatooRequestId(requestId, responseB);
			}
			if (sessionKey.localID() != 0) {
				response = SensiNact.<JSONObject, DescribeResponse<JSONObject>>createErrorResponse(mediator,
						DescribeType.PROVIDER, uri, SnaErrorfulMessage.NOT_FOUND_ERROR_CODE,
						"Service provider not found", null);

				return tatooRequestId(requestId, response);
			}
			/*
			JSONObject object = AccessController.doPrivileged(new PrivilegedAction<JSONObject>() {
				@Override
				public JSONObject run() {
					return SensiNact.this.getProvider(SensiNactSession.this.getSessionId(), serviceProviderId);
				}
			});
			*/
			JSONObject object=SensiNact.this.getProvider(SensiNactSession.this.getSessionId(), serviceProviderId);
			System.out.println("AFTER:"+object);

			response = builder.createAccessMethodResponse();//describeFromJSONObject(mediator, builder, DescribeType.PROVIDER,new JSONObject(object));//object
			//response.remove("statusCode");
			//response.put("statusCode",200);
			response.setResponse(object.getJSONObject("response"));

			System.out.println("RESPONSE:"+response.getJSON());

			System.out.println("ENCORE:"+tatooRequestId(requestId, response).getJSON().toString());
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
			ServiceProvider provider = null;
			String services = null;

			final boolean isRemoteProvider=serviceProviderId.contains(":");

			if(!isRemoteProvider){
				provider = this.serviceProvider(serviceProviderId);
				services = this.joinElementNames(provider);
			}else {
				final String remoteNamespace=serviceProviderId.split(":")[0];
				final String remoteProviderName=serviceProviderId.split(":")[1];
				services=sensinactRemote.get(remoteNamespace).getServices(SensiNactSession.this.getSessionId(),remoteProviderName);
				/*
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
				*/
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

			final boolean isRemoteProvider=serviceProviderId.contains(":");

			if(!isRemoteProvider){

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

			}else {
				final String remoteNamespace=serviceProviderId.split(":")[0];
				final String remoteProviderName=serviceProviderId.split(":")[1];

				SensinactCoreBaseIface corebase=sensinactRemote.get(remoteNamespace);

				System.out.println("Found remote reference:"+corebase);

				String resources=corebase.getResources("none", remoteProviderName,
						serviceId);
				String result = new StringBuilder().append("[").append(resources).append("]").toString();
				builder.setAccessMethodObjectResult(result);
				response = builder.createAccessMethodResponse(Status.SUCCESS);
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
			final String remoteNamespace=serviceProviderId.split(":")[0];
			final String remoteProviderName=serviceProviderId.split(":")[1];
			JSONObject object=new JSONObject(sensinactRemote.get(remoteNamespace).getProvider("none",remoteProviderName));
			/*
			JSONObject object = AccessController.doPrivileged(new PrivilegedAction<JSONObject>() {
				@Override
				public JSONObject run() {
					return SensiNact.this.getResource(SensiNactSession.this.getSessionId(), serviceProviderId,
							serviceId, resourceId);
				}
			});
			*/


			response = builder.createAccessMethodResponse();//describeFromJSONObject(mediator, builder, DescribeType.PROVIDER,new JSONObject(object));//object
			//response.remove("statusCode");
			//response.put("statusCode",200);
			response.setResponse(object.getJSONObject("response"));
			return tatooRequestId(requestId, response);
		}

		/**
		 * Returns true if the model element targeted by the String path argument
		 * exists and is accessible to this {@link Session}. Returns false if the 
		 * model element does not exist or is not accessible to this {@link Session}
		 * 
		 * @param path the String path of the targeted model element
		 * 
		 * @return 
		 * <ul>
		 * 	<li>true if the targeted model element exists and is accessible to this 
		 *      {@link Session}</li>
		 * 	<li>false otherwise</li>
		 * </ul>
		 */
		private final boolean isAccessible(final String path) {
			final SessionKey sessionKey = SensiNact.this.sessions.get(new KeyExtractor<KeyExtractorType>(
					KeyExtractorType.TOKEN, this.getSessionId()));
			Boolean exists = AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
				@Override
				public Boolean run() {
					return SensiNact.this.isAccessible(sessionKey, path);
				}
			});
			return exists.booleanValue();
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

		String prop = System.getProperty(Core.NAMESPACE_PROP);

		if(prop==null){
			prop = (String) mediator.getProperty(Core.NAMESPACE_PROP);
		}

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

	public Mediator mediator;
	private RegistryEndpoint registry;

	private volatile AtomicInteger count = new AtomicInteger(LOCAL_ID + 1);
	private final String namespace;
	public final String defaultLocation;

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

	public final AccessTree<?> getUserAccessTree(final String publicKey) {
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
	 * Constructor
	 * 
	 * @param mediator
	 *            the extended {@link Mediator} allowing the {@link Core} to be
	 *            instantiated to interact with the OSGi host environment
	 * 
	 * @throws SecuredAccessException
	 * @throws BundleException
	 */
/*
	public SensiNact(){
		this.defaultLocation = ModelInstance.defaultLocation(mediator);
		this.sessions = new Sessions();
		this.namespace="SERVER";
		this.anonymousTree = null;
		this.registry = new RegistryEndpoint(this);
	}
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
		ServiceLoader<UserManagerFactory> userManagerFactoryLoader = ServiceLoader.load(UserManagerFactory.class,
				mediator.getClassLoader());

		Iterator<UserManagerFactory> userManagerFactoryIterator = userManagerFactoryLoader.iterator();

		while (userManagerFactoryIterator.hasNext()) {
			UserManagerFactory factory = userManagerFactoryIterator.next();
			if (factory != null) {
				factory.newInstance(mediator);
				break;
			}
		}
		ServiceLoader<SecuredAccessFactory> securedAccessFactoryLoader = ServiceLoader.load(SecuredAccessFactory.class,
				mediator.getClassLoader());

		Iterator<SecuredAccessFactory> securedAccessFactoryIterator = securedAccessFactoryLoader.iterator();

		while (securedAccessFactoryIterator.hasNext()) {
			SecuredAccessFactory factory = securedAccessFactoryIterator.next();
			if (factory != null) {
				securedAccess = factory.newInstance(mediator);
				break;
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
		registry = new RegistryEndpoint(this);

		//.SensinactCoreBaseIface.class
		//(objectClass=org.eclipse.sensinact.gateway.core
		//"(org.eclipse.sensinact.gateway.namespace!="+this.namespace+")"


		//String filterMain=null;String.format("(&(objectClass=%s)(org.eclipse.sensinact.gateway.namespace=%s))",SensinactCoreBaseIface.class.getCanonicalName(),this.namespace).toString();
		String filterMain="org.eclipse.sensinact.gateway.core.api.SensinactCoreBaseIface";
		System.out.println("---->"+filterMain);
/*
		Filter f=new Filter() {
				@Override
				public boolean match(ServiceReference<?> serviceReference) {
					return false;
				}

				@Override
				public boolean match(Dictionary<String, ?> dictionary) {
					return false;
				}

				@Override
				public boolean matchCase(Dictionary<String, ?> dictionary) {
					return false;
				}

				@Override
				public boolean matches(Map<String, ?> map) {
					return false;
				}

				public String toString(){
					return String.format("(&(objectClass=org.eclipse.sensinact.gateway.core.api.SensinactCoreBaseIface)(org.eclipse.sensinact.gateway.namespace!=%s))",namespace);
				}
			};
*/
		final BundleContext context=FrameworkUtil.getBundle(SensinactCoreBase.class).getBundleContext();

		ServiceTracker st=new ServiceTracker<SensinactCoreBaseIface,SensinactCoreBaseIface>(context,filterMain,new ServiceTrackerCustomizer<SensinactCoreBaseIface,SensinactCoreBaseIface>(){

			@Override
			public SensinactCoreBaseIface addingService(ServiceReference<SensinactCoreBaseIface> reference) {

				final SensinactCoreBaseIface sna=context.getService(reference);

				if(sna.namespace().equals(namespace)) return null;

				System.out.println("*************** Core received instance of: "+sna.namespace());

				try {
					MqttClient client = new MqttClient("tcp://localhost:1883", MqttClient.generateClientId(), new MemoryPersistence());
					client.connect();
					client.subscribe(String.format("/%s",sna.namespace()), new IMqttMessageListener() {
						@Override
						public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
							System.out.println(s+" - Received ----> "+new String(mqttMessage.getPayload()));

							//message.setNotification(new JSONObject("{\"name\":\"state\",\"type\":\"boolean\",\"value\":"+new String(mqttMessage.getPayload())+",\"timestamp\":1553074635780}"));
							JSONObject event=new JSONObject(new String(mqttMessage.getPayload()));
							JSONObject eventJson = event.getJSONObject("notification");
							String path=event.getString("uri");
							String provider = path.split("/")[1];

							//String service = path.split("/")[2];
							//String resource = path.split("/")[3];
							//String valueProperty = path.split("/")[4];
							//*String value=eventJson.getString(valueProperty);


							//SnaUpdateMessageImpl message=new SnaUpdateMessageImpl(mediator, "/PI", SnaUpdateMessage.Update.ATTRIBUTE_VALUE_UPDATED);
							//message.remove("uri");
							//final String uriTranslated=String.format("/%s/%s/%s/%s","PI:"+provider,service,resource,valueProperty);
							String uriTranslated=path.replace("/"+provider,String.format("/%s:%s",sna.namespace(),provider));
							//message.put("uri",uriTranslated);
							//event.remove("notification");
							event.remove("uri");
							//event.put("notification",eventJson);
							event.put("uri",uriTranslated);

							//message.setNotification(message);

							System.out.println("Sending on the bus3:"+event.toString());

							SnaMessage message=AbstractSnaMessage.fromJSON(mediator,event.toString());

							SensiNact.this.notifyCallbacks(message);
						}
					});


				} catch (MqttException e) {
					e.printStackTrace();
				}

				sensinactRemote.put(sna.namespace(),sna);

				return sna;
			}

			@Override
			public void modifiedService(ServiceReference<SensinactCoreBaseIface> reference, SensinactCoreBaseIface service) {

			}

			@Override
			public void removedService(ServiceReference<SensinactCoreBaseIface> reference, SensinactCoreBaseIface service) {
				sensinactRemote.remove(service);
			}
		});

		//ServiceTracker st=new ServiceTracker<Sensinact,Sensinact>(mediator.getContext(),Sensinact.class,new Tracker(mediator.getContext()));
		st.open(true);

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
	 * registerAgent(org.eclipse.sensinact.gateway.common.bundle.Mediator,org.eclipse.sensinact.gateway.core.message.MidAgentCallback,org.eclipse.sensinact.gateway.core.message.SnaFilter)
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
		final LocalAgent agent = LocalAgentImpl.createAgent(mediator, callback, filter, agentKey);

		AccessController.<Void>doPrivileged(new PrivilegedAction<Void>() {
			@Override
			public Void run() {
				agent.start();
				messageRegisterers.add(agent);
				return null;
			}
		});
		return callback.getName();
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Core#registerIntent(org.eclipse.sensinact.gateway.common.bundle.Mediator, org.eclipse.sensinact.gateway.common.execution.Executable, String...)
	 */
	@Override
	public String registerIntent(Mediator mediator, Executable<Boolean,Void> onAccessible, final String... path) {
		
		final Bundle bundle = mediator.getContext().getBundle();

		final String bundleIdentifier = this.doPrivilegedService(BundleValidation.class, null,
			new Executable<BundleValidation, String>() {
				@Override
				public String execute(BundleValidation bundleValidation) throws Exception {
					return bundleValidation.check(bundle);
				}
			});
		final String intentKey = this.doPrivilegedService(SecuredAccess.class, null,
			new Executable<SecuredAccess, String>() {
				@Override
				public String execute(SecuredAccess securedAccess) throws Exception {
					return securedAccess.getAgentPublicKey(bundleIdentifier);
				}
			});
		final ResourceIntent intent = new ResourceIntent(mediator, intentKey, onAccessible, path) {
			@Override
			public boolean isAccessible(String path) {
				return SensiNact.this.isAccessible(this.getPublicKey(), path);
			}

			@Override
			public String namespace() {
				return SensiNact.this.namespace();
			}
		};
		AccessController.<Void>doPrivileged(new PrivilegedAction<Void>() {
			@Override
			public Void run() {
				intent.start();
				return null;
			}
		});
		return intent.getName();
	}
	
	/**
	 * Unregisters the {@link SnaAgent} whose identifier is passed as parameter
	 * 
	 * @param identifier
	 *            the identifier of the {@link SnaAgent} to register
	 */
	public void unregisterAgent(final String identifier) {
		doPrivilegedService(SnaAgent.class,
			String.format("(org.eclipse.sensinact.gateway.agent.id=%s",identifier),
			new Executable<SnaAgent, Void>() {
				@Override
				public Void execute(SnaAgent agent) throws Exception {
					agent.stop();
					return null;
				}
			}
		);
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Core#
	 *      createRemoteCore(org.eclipse.sensinact.gateway.core.remote.AbstractRemoteEndpoint)
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
	 *      createRemoteCore(org.eclipse.sensinact.gateway.core.remote.AbstractRemoteEndpoint,
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
			public String localNamespace() {
				return SensiNact.this.namespace();
			}

			@Override
			public boolean isAccessible(String publicKey, String path) {
				return SensiNact.this.isAccessible(publicKey, path);
			}

			@Override
			public void unregisterAgent(String localAgentId) {
				synchronized(this.remoteSessions) {
					Iterator<Session> iterator = this.remoteSessions.values().iterator();
					while(iterator.hasNext()) {
						SessionKey sessionKey = SensiNact.this.sessions.get(iterator.next());
						if(sessionKey.unregisterAgent(localAgentId)) {
							break;
						}
					}
				}
			}
			
			@Override
			public void closeSession(String publicKey) {
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
			public void close() {
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
		});
		remoteCore.onConnected(onConnectedCallbacks);
		remoteCore.onDisconnected(onDisconnectedCallbacks);
		remoteCore.onConnected(Collections.<Executable<String, Void>>singletonList(new Executable<String, Void>() {
			@Override
			public Void execute(String namespace) throws Exception {
				mediator.callServices(LocalAgent.class, new Executable<LocalAgent, Void>() {
					@Override
					public Void execute(LocalAgent agent) throws Exception {
						agent.registerRemote(remoteCore);
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
		final SessionKey sessionKey = sessions.get(new KeyExtractor<KeyExtractorType>(KeyExtractorType.TOKEN, identifier));
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
	 * @param serviceId
	 *            the String identifier of the service
	 * 
	 * @return the {@link Service}
	 */
	protected Service service(String identifier, String serviceProviderId, String serviceId) {
		final SessionKey sessionKey = sessions.get(new KeyExtractor<KeyExtractorType>(KeyExtractorType.TOKEN, identifier));
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
	 * @param serviceId
	 *            the String identifier of the service providing the resource
	 * @param resourceId
	 *            the String identifier of the resource
	 * 
	 * @return the {@link Resource}
	 */
	protected Resource resource(String identifier, String serviceProviderId, String serviceId, String resourceId) {
		final SessionKey sessionKey = sessions.get(new KeyExtractor<KeyExtractorType>(KeyExtractorType.TOKEN, identifier));
		return this.registry.resource(sessionKey, serviceProviderId, serviceId, resourceId);
	}
	
	/**
	 * Returns true if the model element targeted by the String path argument
	 * exists and is accessible to the {@link Session} whose {@link SessionKey}  
	 * is also passed as parameter. Returns false if the model element does not 
	 * exist or is not accessible to the {@link Session}
	 * 
	 * @param sessionKey the {@link SessionKey} of the {@link Session} for which to
	 * define the targeted model element accessibility
	 * @param path the String path of the targeted model element
	 * 
	 * @return 
	 * <ul>
	 * 	<li>true if the targeted model element exists and is accessible to the 
	 * 		specified {@link Session}</li>
	 * 	<li>false otherwise</li>
	 * </ul>
	 */
	protected boolean isAccessible(final SessionKey sessionKey , final String path) {
		return isAccessible(sessionKey.getPublicKey(),sessionKey.getAccessTree(),path);
	}
	
	/**
	 * Returns true if the model element targeted by the String path argument
	 * exists and is accessible to the user whose String public key 
	 * is also passed as parameter. Returns false if the model element does not 
	 * exist or is not accessible to the {@link Session}
	 * 
	 * @param publicKey the String public key of the user for which to
	 * define the targeted model element accessibility
	 * @param path the String path of the targeted model element
	 * 
	 * @return 
	 * <ul>
	 * 	<li>true if the targeted model element exists and is accessible to the 
	 * 		specified user</li>
	 * 	<li>false otherwise</li>
	 * </ul>
	 */
	public boolean isAccessible(final String publicKey , final String path) {
		return isAccessible(publicKey, getUserAccessTree(publicKey),path);
	}
	
	/**
	 * Returns true if the model element targeted by the String path argument
	 * exists and is accessible to the user whose String public key and {@link AccessTree} 
	 * are also passed as parameters. Returns false if the model element does not 
	 * exist or is not accessible to the {@link Session}
	 * 
	 * @param publicKey the String public key of the user for which to
	 * define the targeted model element accessibility
	 * @param tree the {@link AccessTree} of the user for which to
	 * define the targeted model element accessibility
	 * @param path the String path of the targeted model element
	 * 
	 * @return 
	 * <ul>
	 * 	<li>true if the targeted model element exists and is accessible to the 
	 * 		specified user</li>
	 * 	<li>false otherwise</li>
	 * </ul>
	 */
	protected boolean isAccessible(final String publicKey, AccessTree<?> tree , final String path) {
		String[] uriElements = UriUtils.getUriElements(path);
		String[] providerElements = uriElements[0].split(":");
		String namespace = providerElements.length>1?providerElements[0]:null;
		if(namespace != null && !namespace.equals(SensiNact.this.namespace())) {
			Boolean exists = mediator.callService(RemoteCore.class, new StringBuilder().append(
				"(namespace=").append(namespace).append(")").toString(), new Executable<RemoteCore,Boolean>(){
					@Override
					public Boolean execute(RemoteCore remoteCore) throws Exception {
						if(remoteCore == null) {
							return false;
						}
						return remoteCore.endpoint().isAccessible(publicKey,path);
					}
				});
			return exists==null?false:exists.booleanValue();
		}
		String[] uri = new String[uriElements.length];		
		if(uriElements.length > 1) {
			System.arraycopy(uriElements, 1, uri, 1, uriElements.length -1);
		}
		uri[0] = providerElements.length > 1?providerElements[1]:providerElements[0];
		return this.registry.isAccessible(tree, UriUtils.getUri(uri));
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

	public JSONObject translateRemoteCallResponseToLocal(String remoteNamespace,String remoteProviderName,JSONObject objectIncome){

		System.out.println("input:"+objectIncome.toString());

		JSONObject object=new JSONObject(objectIncome.toString());

		final String localURI=object.getString("uri");
		final String remoteURI=localURI.replace("/"+remoteProviderName,String.format("/%s:%s",remoteNamespace,remoteProviderName));
		try{
			object.remove("uri");
		}catch(Exception e){
			e.printStackTrace();
		}

		object.put("uri",remoteURI);

		final JSONObject responsePayload=object.getJSONObject("response");

		try{
			responsePayload.remove("name");
		}catch(Exception e){
			e.printStackTrace();
		}

		responsePayload.put("name",String.format("%s:%s",remoteNamespace,remoteProviderName));

		try{
			object.remove("response");
		}catch(Exception e){
			e.printStackTrace();
		}

		object.put("response",responsePayload);
		System.out.println("LOCAL:"+object.toString());

		return object;

	}

	protected JSONObject getResource(String identifier, final String serviceProviderId, final String serviceId,
			final String resourceId) {
		final SessionKey sessionKey = sessions
				.get(new KeyExtractor<KeyExtractorType>(KeyExtractorType.TOKEN, identifier));

		final boolean isRemoteProvider=serviceProviderId.contains(":");
		JSONObject object=new JSONObject();
		if(isRemoteProvider) {

			final String remoteNamespace=serviceProviderId.split(":")[0];
			final String remoteProviderName=serviceProviderId.split(":")[1];
			object=new JSONObject(sensinactRemote.get(remoteNamespace).getProvider("none",remoteProviderName));
			object=translateRemoteCallResponseToLocal(remoteNamespace,remoteProviderName,object);
			/*
			object = remoteCoreInvocation(serviceProviderId, new Executable<RemoteCore, JSONObject>() {
				@Override
				public JSONObject execute(RemoteCore connector) throws Exception {
					if (connector == null) {
						return null;
					}
					return new JSONObject(connector.endpoint().getResource(sessionKey.getPublicKey(),
							serviceProviderId.substring(serviceProviderId.indexOf(':') + 1), serviceId, resourceId));
				}
			});
			*/

		}
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
	public String getResources(String identifier, final String serviceProviderId, final String serviceId) {
		final SessionKey sessionKey = sessions.get(new KeyExtractor<KeyExtractorType>(KeyExtractorType.TOKEN, identifier));

		SensiNact.SensiNactAnonymousSession session=(SensiNact.SensiNactAnonymousSession)getAnonymousSession();

		String object=session.getResources(serviceProviderId,serviceId).getResponse();


		object=object.substring(1,object.length()-1);

		/*
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
		*/
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

		SensiNact.SensiNactAnonymousSession session=(SensiNact.SensiNactAnonymousSession)getAnonymousSession();
		JSONObject object=session.getService(serviceProviderId,serviceId).getResponse();
		/*

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

		*/

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
	public String getServices(String identifier, final String serviceProviderId) {
		final SessionKey sessionKey = sessions
				.get(new KeyExtractor<KeyExtractorType>(KeyExtractorType.TOKEN, identifier));
		String object = null;

		SensiNact.SensiNactAnonymousSession session=(SensiNact.SensiNactAnonymousSession)getAnonymousSession();

		String response=session.getServices(serviceProviderId).getResponse();

		object=response.substring(1,response.length()-1);

		/*
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
		*/
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
	public JSONObject getProvider(String identifier, final String serviceProviderId) {
		final SessionKey sessionKey = sessions
				.get(new KeyExtractor<KeyExtractorType>(KeyExtractorType.TOKEN, identifier));

		SensiNact.SensiNactAnonymousSession session=(SensiNact.SensiNactAnonymousSession)getAnonymousSession();
		ServiceProvider provider = null;
		String services = null;
		JSONObject object=null;
		final boolean isRemoteProvider=serviceProviderId.contains(":");

		if(!isRemoteProvider){
			object=new JSONObject(session.getProvider(serviceProviderId).getJSON());//SensiNact.this.getProvider(session.identifier,serviceProviderId));//session.getProvider(serviceProviderId).getResponse();
			System.out.println("Object returned="+object.toString());
		}else {
			final String remoteNamespace=serviceProviderId.split(":")[0];
			final String remoteProviderName=serviceProviderId.split(":")[1];

			object=new JSONObject(sensinactRemote.get(remoteNamespace).getProvider(session.identifier,remoteProviderName));

			/*
			final String localURI=object.getString("uri");
			final String remoteURI=localURI.replace("/"+remoteProviderName,String.format("/%s:%s",remoteNamespace,remoteProviderName));
			object.remove("uri");
			object.put("uri",remoteURI);

			final JSONObject responsePayload=object.getJSONObject("response");
			responsePayload.remove("name");
			responsePayload.put("name",serviceProviderId);
			object.remove("response");
			object.put("response",responsePayload);
			System.out.println("LOCAL:"+object.toString());
			*/
			object=translateRemoteCallResponseToLocal(remoteNamespace,remoteProviderName,object);


				/*
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
				*/
		}

		/*
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
		*/
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
	public JSONObject get(String identifier, final String serviceProviderId, final String serviceId,
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

	public String getProvidersRemote(String identifier, String filter) {
		final StringBuilder content = new StringBuilder();
		for(SensinactCoreBaseIface remote:sensinactRemote.values()){
			String providersFromRemote=remote.getProviders(identifier,filter);
			content.append(providersFromRemote);
		}
		return content.toString();
	}

	public String getProvidersLocal(String identifier, String filter){
		SessionKey sessionKey = sessions
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
		System.out.println("BING!!!!!");
		//System.out.println("Local ID1 --->"+sessionKey);
		//System.out.println("Local ID2 --->"+sessionKey.localID());
		String local = registry.getProviders(sessionKey, false, effectiveFilter);//sessionKey.localID() != 0

		if (sessionKey.localID() != 0) {
			return local;
		}
		final StringBuilder content = new StringBuilder();
		if (local != null && local.length() > 0) {
			content.append(local);
		}
		return content.toString();
	}

	public String getProviders(String identifier, String filter) {

		try{
			StringBuilder content=new StringBuilder(getProvidersLocal(identifier, filter));//
			content.append(getProvidersRemote(identifier,filter));
			System.out.println("-------------->"+content.toString());
/*
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
*/
			return content.toString();

		}catch(Exception e){
			e.printStackTrace();
			return "";
		}

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
						agent.stop();
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
