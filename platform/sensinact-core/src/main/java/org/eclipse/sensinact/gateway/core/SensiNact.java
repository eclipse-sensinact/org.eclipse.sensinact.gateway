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

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.constraint.Constraint;
import org.eclipse.sensinact.gateway.common.constraint.ConstraintFactory;
import org.eclipse.sensinact.gateway.common.constraint.InvalidConstraintDefinitionException;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.Sessions.KeyExtractor;
import org.eclipse.sensinact.gateway.core.Sessions.KeyExtractorType;
import org.eclipse.sensinact.gateway.core.api.MQTTURLExtract;
import org.eclipse.sensinact.gateway.core.api.Sensinact;
import org.eclipse.sensinact.gateway.core.api.SensinactCoreBaseIface;
import org.eclipse.sensinact.gateway.core.message.*;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse.Status;
import org.eclipse.sensinact.gateway.core.method.legacy.*;
import org.eclipse.sensinact.gateway.core.method.legacy.DescribeMethod.DescribeType;
import org.eclipse.sensinact.gateway.core.security.*;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;
import org.eclipse.sensinact.gateway.security.signature.api.BundleValidation;

import org.eclipse.sensinact.gateway.sthbnd.mqtt.util.api.MqttBroker;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.util.api.MqttTopic;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.util.listener.MqttTopicMessage;
import org.eclipse.sensinact.gateway.util.CryptoUtils;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.framework.*;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
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
	private static final Logger LOG=LoggerFactory.getLogger(SensiNact.class);

	List<MessageRegisterer> messageRegisterers =Collections.synchronizedList(new ArrayList<MessageRegisterer>());
	List<MidAgentCallback> messageAgentCallback =Collections.synchronizedList(new ArrayList<MidAgentCallback>());

	public Map<String,SensinactCoreBaseIface> sensinactRemote=Collections.synchronizedMap(new HashMap<String,SensinactCoreBaseIface>());
	public Map<String,String> sensinactRemoteServiceDomainMap =Collections.synchronizedMap(new HashMap<String,String>());

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
				LOG.error("Failed to notify callback",e);
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
				try {
					GetResponseBuilder builder = new GetResponseBuilder(mediator, uri,null);
					builder.createAccessMethodResponse(AccessMethodResponse.Status.SUCCESS);
					//builder.setAccessMethodObjectResult(translateRemoteCallResponseToLocal(remoteNamespace,remoteProviderName,new JSONObject(jsonInStringRemote)));
					JSONObject object=new JSONObject(jsonInStringRemote);//SensiNact.this.getProvider(SensiNactSession.this.getSessionId(), serviceProviderId);
					response = builder.createAccessMethodResponse(Status.SUCCESS);//describeFromJSONObject(mediator, builder, DescribeType.PROVIDER,new JSONObject(object));//object
					response.setResponse(object.getJSONObject("response"));
					//translateRemoteCallResponseToLocal(remoteNamespace,remoteProviderName,object)

					return tatooRequestId(requestId, response);//describeFromJSONObject(mediator, builder, DescribeType.SERVICE, object)
					/** sample **/

				} catch (Exception e) {
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

			SensinactCoreBaseIface remote=getRemoteFromServiceProviderId(serviceProviderId);

			if(remote==null){
				response = SensiNact.<JSONObject, SetResponse>createErrorResponse(mediator, AccessMethod.SET, uri,
						SnaErrorfulMessage.NOT_FOUND_ERROR_CODE, "Resource not found", null);
				return tatooRequestId(requestId, response);
			}else {

				final String remoteProviderName=serviceProviderId.split(":")[1];

				String responseString=remote.set("none",remoteProviderName, serviceId,
						resourceId, attributeId, parameter==null?"":parameter.toString());
				try {
					response = this.<SetResponse>responseFromJSONObject(mediator, uri, AccessMethod.SET, new JSONObject(responseString));
				} catch (Exception e) {
					response = SensiNact.<JSONObject, SetResponse>createErrorResponse(mediator, AccessMethod.SET, uri,
							SnaErrorfulMessage.INTERNAL_SERVER_ERROR_CODE, "Internal server error", e);
				}
				return tatooRequestId(requestId, response);
			}
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

			SensinactCoreBaseIface remote=getRemoteFromServiceProviderId(serviceProviderId);

			if(remote==null){
				response = SensiNact.<JSONObject, ActResponse>createErrorResponse(mediator, AccessMethod.ACT, uri,
						SnaErrorfulMessage.NOT_FOUND_ERROR_CODE, "Resource not found", null);
				return tatooRequestId(requestId, response);
			}else {
				final String remoteProviderName=serviceProviderId.split(":")[1];
				JSONArray array=new JSONArray();

				for(int x=0;parameters!=null&&x<parameters.length;x++){
					array.put(parameters[x].toString());
				}

				String responseString=remote.act("none", remoteProviderName, serviceId,
						resourceId, array.toString());
				try {
					response = this.<ActResponse>responseFromJSONObject(mediator, uri, AccessMethod.ACT, new JSONObject(responseString));
				} catch (Exception e) {
					response = SensiNact.<JSONObject, ActResponse>createErrorResponse(mediator, AccessMethod.ACT, uri,
							SnaErrorfulMessage.INTERNAL_SERVER_ERROR_CODE, "Internal server error", e);
				}
				return tatooRequestId(requestId, response);
			}


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
		public SubscribeResponse subscribe(final String requestId, final String serviceProviderId, final String serviceId,
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

			if(!isRemoteProvider(serviceProviderId)){

			}else {
				final String remoteNamespace=serviceProviderId.split(":")[0];
				final String remoteProviderName=serviceProviderId.split(":")[1];
				//new JSONObject(sensinactRemote.get(remoteNamespace).subscribe(remoteProviderName,serviceId,resourceId,"none")
				final String uriRemote=String.format("/%s/%s/%s",serviceProviderId,serviceId,resourceId);
				SubscribeResponse subscribeResponse=new SubscribeResponse(mediator,uriRemote,Status.SUCCESS);
				subscribeResponse.put("subscriptionId",recipient.toString());
				SensiNact.this.messageRegisterers.add(new MessageRegisterer() {
					@Override
					public void register(SnaMessage<?> message) {
						try {
							JSONObject oj=new JSONObject(message.getJSON());

							//System.out.println("URI REMOTE "+uriRemote+"  ACTUAL URI "+oj.getString("uri"));
							if(oj.getString("uri").startsWith(uriRemote)&&oj.getString("type").equals("ATTRIBUTE_VALUE_UPDATED")
									||oj.getString("type").equals("RESOURCE_APPEARING")||oj.getString("type").equals("RESOURCE_DISAPPEARING")) { //||oj.getString("type").contains("RESOURCE_"))
								LOG.info("remote: Subscription response {}",oj.toString());
								recipient.callback(requestId, new SnaMessage[]{message});
							}
						} catch (Exception e) {
							LOG.error("Error",e);
						}
					}
				});
				return subscribeResponse;
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
/*
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
			}*/

			response = SensiNact.<JSONObject, UnsubscribeResponse>createErrorResponse(mediator,
					AccessMethod.UNSUBSCRIBE, uri, SnaErrorfulMessage.NOT_FOUND_ERROR_CODE, "Resource not found",
					null);

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
					return SensiNact.this.getAll(SensiNactSession.this.getSessionId(), ldapFilter,false);
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

			JSONArray namesspacesArray=new JSONArray(result);
			//JSONObject namespaceLocal=new JSONObject();
			//namespaceLocal.put("name",namespace);
			//namespaceLocal.put("providers",new JSONArray(result));
			//namesspacesArray.put(new JSONArray(result));


			for(Map.Entry<String,SensinactCoreBaseIface> entry:sensinactRemote.entrySet()){
				try {
					LOG.info("Dispatching a getAll to the remote instance of sensinact {} with filter {}",entry.getValue().namespace(),filter);
					String allFromRemote=entry.getValue().getAll(identifier,filter);
					LOG.info("Filter result {}",allFromRemote);
					String resultRemote = new StringBuilder().append("[").append(allFromRemote).append("]").toString();
					JSONArray remoteArray=new JSONArray(resultRemote);
					for (int i = 0; i < remoteArray.length(); i++) {
						namesspacesArray.put(remoteArray.getJSONObject(i));
					}

				}catch(Exception e){
					LOG.error("Error when retrieving list of remoteProviders",e);
					sensinactRemote.remove(entry.getKey());
				}
			}

			result=namesspacesArray.toString();

			if (filterCollection != null) {
				result = filterCollection.apply(namesspacesArray.toString());
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
				return tatooRequestId(requestId, responseB);
			}
			if (sessionKey.localID() != 0) {
				response = SensiNact.<JSONObject, DescribeResponse<JSONObject>>createErrorResponse(mediator,
						DescribeType.PROVIDER, uri, SnaErrorfulMessage.NOT_FOUND_ERROR_CODE,
						"Service provider not found", null);

				return tatooRequestId(requestId, response);
			}

			JSONObject object=SensiNact.this.getProvider(SensiNactSession.this.getSessionId(), serviceProviderId);
			response = builder.createAccessMethodResponse(AccessMethodResponse.Status.SUCCESS);
			response.setResponse(object.getJSONObject("response"));
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

			if(!isRemoteProvider(serviceProviderId)){
				provider = this.serviceProvider(serviceProviderId);
				services = this.joinElementNames(provider);
			}else {
				final String remoteNamespace=serviceProviderId.split(":")[0];
				final String remoteProviderName=serviceProviderId.split(":")[1];
				services=sensinactRemote.get(remoteNamespace).getServices(SensiNactSession.this.getSessionId(),remoteProviderName);
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

			if(!isRemoteProvider(serviceProviderId)){
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
			}else {

				final String remoteNamespace=serviceProviderId.split(":")[0];
				final String remoteProviderName=serviceProviderId.split(":")[1];

				SensinactCoreBaseIface remote=getRemoteFromServiceProviderId(serviceProviderId);
				String stringObjectResponse=remote.getService(identifier,remoteProviderName,serviceId);

				/** sample **/
				JSONObject object=new JSONObject(stringObjectResponse);//SensiNact.this.getProvider(SensiNactSession.this.getSessionId(), serviceProviderId);
				//Remote case
				response = builder.createAccessMethodResponse(Status.SUCCESS);//describeFromJSONObject(mediator, builder, DescribeType.PROVIDER,new JSONObject(object));//object
				//response.remove("statusCode");
				//response.put("statusCode",200);
				response.setResponse(object);
				return tatooRequestId(requestId, response);//describeFromJSONObject(mediator, builder, DescribeType.SERVICE, object)

				/** sample **/
			}


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

			if(!isRemoteProvider(serviceProviderId)){

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

			SensinactCoreBaseIface remoteReference=sensinactRemote.get(remoteNamespace);

			if(remoteReference==null){
				response = SensiNact.<JSONObject, DescribeResponse<JSONObject>>createErrorResponse(mediator,
						DescribeType.RESOURCE, uri, SnaErrorfulMessage.NOT_FOUND_ERROR_CODE, "Resource not found",
						null);
				return tatooRequestId(requestId, response);
			}

			JSONObject object=new JSONObject(remoteReference.getResource("none",remoteProviderName,serviceId,resourceId));
			response = builder.createAccessMethodResponse();
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

			try {
				Properties properties=new Properties();
				properties.load(new FileInputStream("cfgs/sensinact.config"));
				prop=properties.getProperty(Core.NAMESPACE_PROP);
				final String broker=properties.getProperty("broker");
				mediator.setProperty("namespace",prop.toString());
				mediator.setProperty("broker",broker.toString());
			} catch (IOException e) {
				LOG.debug("Error while loading namespace",e);
			}
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
	public SensiNact(final String namespace, final Mediator mediator) throws SecuredAccessException, BundleException, DataStoreException {
		this.namespace = SensiNact.namespace(mediator);//namespace;//namespace;//
		LOG.info("Using {} as namespace for the sensinact instance",this.namespace);
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
		String filterMain="org.eclipse.sensinact.gateway.core.api.SensinactCoreBaseIface";

		final BundleContext context=mediator.getContext();

		ServiceTracker st=new ServiceTracker<SensinactCoreBaseIface,MqttBroker>(context,filterMain,new ServiceTrackerCustomizer<SensinactCoreBaseIface,MqttBroker>(){

			@Override
			public MqttBroker addingService(ServiceReference<SensinactCoreBaseIface> reference) {

				final SensinactCoreBaseIface sna=context.getService(reference);

				LOG.info("Receiving RSA discovery notification about remote instance {}",sna.namespace());

				if(sna.namespace().equals(SensiNact.this.namespace)) return null;

				if(sensinactRemote.keySet().contains(sna.namespace())){
					LOG.info("RSA remote sensinact instance with namespace {} already exists, ignoring entry",sna.namespace());
					return null;
				}

				LOG.info("Connecting to RSA remote sensinact instance with namespace {}",sna.namespace());

				final String brokerAddr=mediator.getProperty("broker").toString();

				MQTTURLExtract mqttURL=new MQTTURLExtract(brokerAddr);

				MqttBroker mb=new MqttBroker.Builder().host(mqttURL.getHost()).port(mqttURL.getPort()).protocol(MqttBroker.Protocol.valueOf(mqttURL.getProtocol().toUpperCase())).build();

				MqttTopic topic=new MqttTopic(String.format("/%s",sna.namespace()),new MqttTopicMessage(){
					@Override
					protected void messageReceived(String topic, String mqttMessage) {
						LOG.info("Received remote notification from namespace {} on topic {} with message {}",sna.namespace(),topic,mqttMessage);

						JSONObject event=new JSONObject(mqttMessage);
						String path=event.getString("uri");
						String provider = path.split("/")[1];
						String uriTranslated=path.replaceFirst("/"+provider,String.format("/%s:%s",sna.namespace(),provider));
						event.remove("uri");
						event.put("uri",uriTranslated);
						LOG.debug("Forwarding message received in local sensinact as {}",event.toString());
						SnaMessage message=AbstractSnaMessage.fromJSON(mediator,event.toString());
						SensiNact.this.notifyCallbacks(message);
					}
				});

				try {
					mb.subscribeToTopic(topic);
					mb.connect();
					sensinactRemote.put(sna.namespace(),sna);
					sensinactRemoteServiceDomainMap.put(mb.toString(),sna.namespace());
				} catch (Exception e) {
					LOG.error("Failed to connect to broker {}",brokerAddr,e);
				}

				return mb;
			}

			@Override
			public void modifiedService(ServiceReference<SensinactCoreBaseIface> reference, MqttBroker service) {

			}

			@Override
			public void removedService(ServiceReference<SensinactCoreBaseIface> reference, MqttBroker service) {
				String key= sensinactRemoteServiceDomainMap.remove(service.toString());
				LOG.info("Removing RSA sensinact remote instance {} from the pool",key.toString());//,
				sensinactRemote.remove(key);
				LOG.info("RSA sensinact remote instance removed from the pool {} instances remain in the pool",sensinactRemote.keySet().size());
				try {
					service.disconnect();
				} catch (Exception e) {
					LOG.error("Failing disconnecting from broker {}",service.getHost());
				}
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

		String[] uri = new String[uriElements.length];
		if(uriElements.length > 1) {
			System.arraycopy(uriElements, 1, uri, 1, uriElements.length -1);
		}
		uri[0] = providerElements.length > 1?providerElements[1]:providerElements[0];
		return this.registry.isAccessible(tree, UriUtils.getUri(uri));
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

	public boolean isRemoteProvider(String serviceProviderId){
		return serviceProviderId.contains(":");
	}

	public SensinactCoreBaseIface getRemoteFromServiceProviderId(String serviceProviderId){
		final String remoteNamespace=serviceProviderId.split(":")[0];
		return sensinactRemote.get(remoteNamespace);

	}

	public JSONObject translateRemoteCallResponseToLocal(String remoteNamespace,String remoteProviderName,JSONObject objectIncome){

		JSONObject object=new JSONObject(objectIncome.toString());

		final String localURI=object.getString("uri");
		final String remoteURI=localURI.replaceFirst("/"+remoteProviderName,String.format("/%s:%s",remoteNamespace,remoteProviderName));
		try{
			object.remove("uri");
		}catch(Exception e){
			LOG.error("Error when translating URI from remote to local",e);
		}

		object.put("uri",remoteURI);

		final JSONObject responsePayload=object.getJSONObject("response");

		/*
		try{
			responsePayload.remove("name");
		}catch(Exception e){
			e.printStackTrace();
		}
		responsePayload.put("name",String.format("%s:%s",remoteNamespace,remoteProviderName));
		*/

		try{
			object.remove("response");
		}catch(Exception e){
			LOG.error("Error when translating response from remote to local",e);
		}

		object.put("response",responsePayload);

		return object;

	}

	protected JSONObject getResource(String identifier, final String serviceProviderId, final String serviceId,
			final String resourceId) {
		final SessionKey sessionKey = sessions
				.get(new KeyExtractor<KeyExtractorType>(KeyExtractorType.TOKEN, identifier));

		JSONObject object=new JSONObject();
		if(isRemoteProvider(serviceProviderId)) {

			final String remoteNamespace=serviceProviderId.split(":")[0];
			final String remoteProviderName=serviceProviderId.split(":")[1];
			object=new JSONObject(sensinactRemote.get(remoteNamespace).getResource("none",remoteProviderName,serviceId,resourceId));
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
	public JSONObject getService(String identifier, final String serviceProviderId, final String serviceId) {
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

		for(Map.Entry<String,SensinactCoreBaseIface> entry:sensinactRemote.entrySet()){
			try {
				String providersFromRemote=entry.getValue().getProviders(identifier,filter);
				content.append(providersFromRemote);
			}catch(Exception e){
				LOG.error("Error when retrieving list of remoteProviders",e);
				sensinactRemote.remove(entry.getKey());
			}
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
	public String getAll(String identifier, final String filter,Boolean attachNamespace) {
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
		String local = this.registry.getAll(sessionKey, sessionKey.localID() != 0, effectiveFilter,attachNamespace);

		if (sessionKey.localID() != 0) {
			return local;
		}
		final StringBuilder content = new StringBuilder();
		if (local != null && local.length() > 0) {
			content.append(local);
		}
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
