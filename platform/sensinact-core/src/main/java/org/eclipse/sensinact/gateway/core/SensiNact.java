/*
* Copyright (c) 2020-2021 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.core;

import java.security.AccessController;
import java.security.InvalidKeyException;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.constraint.Constraint;
import org.eclipse.sensinact.gateway.common.constraint.ConstraintFactory;
import org.eclipse.sensinact.gateway.common.constraint.InvalidConstraintDefinitionException;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.Sessions.KeyExtractor;
import org.eclipse.sensinact.gateway.core.Sessions.KeyExtractorType;
import org.eclipse.sensinact.gateway.core.filtering.FilteringCollection;
import org.eclipse.sensinact.gateway.core.message.AbstractMidAgentCallback;
import org.eclipse.sensinact.gateway.core.message.LocalAgent;
import org.eclipse.sensinact.gateway.core.message.LocalAgentImpl;
import org.eclipse.sensinact.gateway.core.message.MidAgentCallback;
import org.eclipse.sensinact.gateway.core.message.MidCallbackException;
import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.eclipse.sensinact.gateway.core.message.ResourceIntent;
import org.eclipse.sensinact.gateway.core.message.SnaAgent;
import org.eclipse.sensinact.gateway.core.message.SnaErrorfulMessage;
import org.eclipse.sensinact.gateway.core.message.SnaFilter;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.core.message.SnaUpdateMessageImpl;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse.Status;
import org.eclipse.sensinact.gateway.core.method.ActResponse;
import org.eclipse.sensinact.gateway.core.method.DescribeMethod;
import org.eclipse.sensinact.gateway.core.method.DescribeMethod.DescribeType;
import org.eclipse.sensinact.gateway.core.method.DescribeResponse;
import org.eclipse.sensinact.gateway.core.method.DescribeResponseBuilder;
import org.eclipse.sensinact.gateway.core.method.GetResponse;
import org.eclipse.sensinact.gateway.core.method.RemoteAccessMethodExecutable;
import org.eclipse.sensinact.gateway.core.method.SetResponse;
import org.eclipse.sensinact.gateway.core.method.SubscribeResponse;
import org.eclipse.sensinact.gateway.core.method.UnsubscribeResponse;
import org.eclipse.sensinact.gateway.core.remote.RemoteCore;
import org.eclipse.sensinact.gateway.core.remote.SensinactCoreBaseIFaceManager;
import org.eclipse.sensinact.gateway.core.remote.SensinactCoreBaseIFaceManagerFactory;
import org.eclipse.sensinact.gateway.core.remote.SensinactCoreBaseIface;
import org.eclipse.sensinact.gateway.core.security.AccessNode;
import org.eclipse.sensinact.gateway.core.security.AccessTree;
import org.eclipse.sensinact.gateway.core.security.AccountConnector;
import org.eclipse.sensinact.gateway.core.security.Authentication;
import org.eclipse.sensinact.gateway.core.security.InvalidCredentialException;
import org.eclipse.sensinact.gateway.core.security.MutableAccessTree;
import org.eclipse.sensinact.gateway.core.security.SecuredAccess;
import org.eclipse.sensinact.gateway.core.security.SecuredAccessException;
import org.eclipse.sensinact.gateway.core.security.SessionToken;
import org.eclipse.sensinact.gateway.core.security.User;
import org.eclipse.sensinact.gateway.core.security.UserKey;
import org.eclipse.sensinact.gateway.core.security.UserKeyBuilder;
import org.eclipse.sensinact.gateway.core.security.UserKeyBuilderFactory;
import org.eclipse.sensinact.gateway.core.security.UserManager;
import org.eclipse.sensinact.gateway.core.security.UserUpdater;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;
import org.eclipse.sensinact.gateway.security.signature.api.BundleValidation;
import org.eclipse.sensinact.gateway.util.CryptoUtils;
import org.eclipse.sensinact.gateway.util.ReflectUtils;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.condpermadmin.ConditionalPermissionAdmin;
import org.osgi.service.condpermadmin.ConditionalPermissionInfo;
import org.osgi.service.condpermadmin.ConditionalPermissionUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * {@link Core} service implementation
 * 
 * @author <a href="mailto:cmunilla@kentyou.com">Christophe Munilla</a>
 */
@Component(immediate=true)
public class SensiNact implements Core {
	/**
	 * Abstract {@link Session} service implementation
	 */
	public abstract class SensiNactSession extends AbstractSession {
		
		/**
		 * Constructor
		 * 
		 * @param identifier the String identifier of the Session to be instantiated
		 */
		public SensiNactSession(String identifier) {
			super(identifier);
		}		

		@Override
		public Set<ServiceProvider> serviceProviders(final String filter) {
			return AccessController.doPrivileged(new PrivilegedAction<Set<ServiceProvider>>() {
				@Override
				public Set<ServiceProvider> run() {
					return SensiNact.this.serviceProviders(SensiNactSession.this.getSessionId(), filter);
				}
			});
		}

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

		@Override
		public SubscribeResponse registerSessionAgent(String requestId, final MidAgentCallback callback, final SnaFilter filter) {

			final SessionKey sessionKey= SensiNact.this.getSessionKeyFromToken(this.getSessionId());
			
			boolean registered = AccessController.<Boolean>doPrivileged(new PrivilegedAction<Boolean>() {
				@Override public Boolean run() {return sessionKey.registerAgent(callback, filter);}});

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
			} else {
				response = SensiNact.<JSONObject, SubscribeResponse>createErrorResponse(
					mediator, AccessMethod.SUBSCRIBE, uri, 520, "Unable to subscribe", 
					    null);
			}
			return tatooRequestId(requestId, response);
		}

		@Override
		public UnsubscribeResponse unregisterSessionAgent(String requestId, final String agentId) {

			final SessionKey sessionKey= SensiNact.this.getSessionKeyFromToken(this.getSessionId());
			
			boolean unregistered = AccessController.<Boolean>doPrivileged(new PrivilegedAction<Boolean>() {
				@Override
				public Boolean run() {
					if (sessionKey != null && sessionKey.getPublicKey() != null) {
						return sessionKey.unregisterAgent(agentId);
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
		public SubscribeResponse registerSessionIntent(String requestId, Executable<Boolean, Void> callback, String... resourcePath) {

			final SessionKey sessionKey= SensiNact.this.getSessionKeyFromToken(this.getSessionId());
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

			final SessionKey sessionKey= SensiNact.this.getSessionKeyFromToken(this.getSessionId());
			
			boolean unregistered = AccessController.<Boolean>doPrivileged(new PrivilegedAction<Boolean>() {
				@Override
				public Boolean run() {
					if (sessionKey != null && sessionKey.getPublicKey() != null) {
						return sessionKey.unregisterAgent(intentId);
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

		@Override
		public GetResponse get(String requestId, final String serviceProviderId, final String serviceId,
				final String resourceId, final String attributeId, Object... args) {

			final SessionKey sessionKey= SensiNact.this.getSessionKeyFromToken(this.getSessionId());
			
			String uri = getUri((sessionKey.localID() != 0), SensiNact.this.namespace(), serviceProviderId, serviceId, resourceId);

			Resource resource = this.resource(serviceProviderId, serviceId, resourceId);
			GetResponse response = null;

			if (resource != null) {
				if (attributeId == null) {
					if (!resource.getType().equals(Resource.Type.ACTION))
						response = ((DataResource) resource).get(args);
					else 
						response = SensiNact.<JSONObject, GetResponse>createErrorResponse(mediator, AccessMethod.GET, uri, 404, "Unknown Method", null);
				} else 
					response = resource.get(attributeId, args);
				return tatooRequestId(requestId, response);
			}
			if (serviceProviderId.indexOf('(') < 0 && sessionKey.localID() != 0) {
				
				response = SensiNact.<JSONObject, GetResponse>createErrorResponse(mediator, AccessMethod.GET, uri,
						SnaErrorfulMessage.NOT_FOUND_ERROR_CODE, "Resource not found", null);
				
				return tatooRequestId(requestId, response);
			}
			JSONObject object = AccessController.doPrivileged(new PrivilegedAction<JSONObject>() {
				@Override
				public JSONObject run() {
					return SensiNact.this.get(sessionKey.getToken(), serviceProviderId, serviceId,
							resourceId, attributeId, args);
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
		
		@Override
		public SetResponse set(String requestId, final String serviceProviderId, final String serviceId,
				final String resourceId, final String attributeId, final Object parameter, Object... args) {

			final SessionKey sessionKey= SensiNact.this.getSessionKeyFromToken(this.getSessionId());
			
			String uri = getUri((sessionKey.localID() != 0), SensiNact.this.namespace(), serviceProviderId, serviceId,
					resourceId);
			Resource resource = this.resource(serviceProviderId, serviceId, resourceId);
			SetResponse response = null;

			if (resource != null) {
				if (attributeId == null) {
					if (!resource.getType().equals(Resource.Type.ACTION)) {
						response = ((DataResource) resource).set(parameter, args);

					} else {
						response = SensiNact.<JSONObject, SetResponse>createErrorResponse(mediator, AccessMethod.SET,
								uri, 404, "Unknown Method", null);
					}
				} else {
					response = resource.set(attributeId, parameter, args);
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
					return SensiNact.this.set(sessionKey, serviceProviderId, serviceId,
							resourceId, attributeId, parameter, args);
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

		@Override
		public ActResponse act(String requestId, final String serviceProviderId, final String serviceId,
				final String resourceId, final Object[] parameters) {

			final SessionKey sessionKey= SensiNact.this.getSessionKeyFromToken(this.getSessionId());
			
			ActResponse response = null;
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
					return SensiNact.this.act(sessionKey.getPublicKey(), serviceProviderId, serviceId,
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

		@Override
		public SubscribeResponse subscribe(final String requestId, final String serviceProviderId, final String serviceId,
				final String resourceId, final Recipient recipient, final JSONArray conditions, final String policy,
				Object... args) {

			final SessionKey sessionKey= SensiNact.this.getSessionKeyFromToken(this.getSessionId());
			
			SubscribeResponse response = null;	
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
							LOG.error(e.getLocalizedMessage(),e);
						}
					}
					response = ((DataResource) resource).subscribe(recipient, (constraint == null 
						? Collections.<Constraint>emptySet():Collections.<Constraint>singleton(constraint)), policy, args);
				} else {
					response = SensiNact.<JSONObject, SubscribeResponse>createErrorResponse(mediator,
							AccessMethod.SUBSCRIBE, uri, 404, "Unknown Method", null);
				}
				return tatooRequestId(requestId, response);
			}
			Boolean acc = SensiNact.this.remoteCoreInvocation(uri, new Executable<SensinactCoreBaseIface,Boolean>(){
				@Override
				public Boolean execute(SensinactCoreBaseIface core) throws Exception {
					return core.isAccessible(sessionKey.getPublicKey(), serviceProviderId, serviceId, resourceId);
				}
			});
			boolean accessible = acc==null?false:acc.booleanValue();
			
			if (sessionKey.localID() != 0 || !accessible) {
				response = SensiNact.<JSONObject, SubscribeResponse>createErrorResponse(mediator,
					AccessMethod.SUBSCRIBE, uri, SnaErrorfulMessage.NOT_FOUND_ERROR_CODE, "Resource not found", null);
				return tatooRequestId(requestId, response);
			}			
			final String uriRemote=String.format("/%s/%s/%s",serviceProviderId,serviceId,resourceId);
			response=new SubscribeResponse(mediator,uriRemote,Status.SUCCESS);
			response.setResponse(new JSONObject().put("subscriptionId",recipient.toString()));	
			MidAgentCallback cb = new AbstractMidAgentCallback(true,true,recipient.toString()){					
				@Override
				public void doHandle(SnaUpdateMessageImpl message) throws MidCallbackException {
					JSONObject oj=new JSONObject(message.getJSON());
					if(oj.getString("uri").startsWith(uriRemote)) {
						LOG.info("remote: Subscription response {}",oj.toString());
						try {
							recipient.callback(requestId, new SnaMessage[]{message});
						} catch (Exception e) {
							throw new MidCallbackException(e);
						}
					}
				}
			};
			SnaFilter filter = new SnaFilter(mediator, uriRemote.concat("/value"), true, false);
			filter.addHandledType(SnaMessage.Type.UPDATE);
			Constraint constraint = null;
			if (conditions != null && conditions.length() > 0) {
				try {
					constraint = ConstraintFactory.Loader.load(mediator.getClassLoader(), conditions);
				} catch (InvalidConstraintDefinitionException e) {
					LOG.error(e.getMessage(),e);
				}
			}
			filter.addCondition(constraint);
			SensiNact.this.registerAgent(mediator, cb, filter);
			return tatooRequestId(requestId, response);
		}

		@Override
		public UnsubscribeResponse unsubscribe(String requestId, final String serviceProviderId, final String serviceId,
				final String resourceId, final String subscriptionId, Object... args) {

			final SessionKey sessionKey= SensiNact.this.getSessionKeyFromToken(this.getSessionId());
			
			UnsubscribeResponse response = null;	
			String uri = getUri((sessionKey.localID() != 0), SensiNact.this.namespace(), serviceProviderId, serviceId,
					resourceId);

			Resource resource = this.resource(serviceProviderId, serviceId, resourceId);

			if (resource != null) {
				if (!resource.getType().equals(Resource.Type.ACTION)) {
					response = ((DataResource) resource).unsubscribe(subscriptionId, args);

				} else {
					response = SensiNact.<JSONObject, UnsubscribeResponse>createErrorResponse(mediator,
							AccessMethod.UNSUBSCRIBE, uri, 404, "Unknown Method", null);
				}
				return tatooRequestId(requestId, response);
			}
			Boolean acc = SensiNact.this.remoteCoreInvocation(uri, new Executable<SensinactCoreBaseIface,Boolean>(){
				@Override
				public Boolean execute(SensinactCoreBaseIface core) throws Exception {
					return core.isAccessible(sessionKey.getPublicKey(), serviceProviderId, serviceId, resourceId);
				}
			});
			boolean accessible = acc==null?false:acc.booleanValue();
			
			if (sessionKey.localID() != 0 || !accessible) {
				response = SensiNact.<JSONObject, UnsubscribeResponse>createErrorResponse(mediator,
					AccessMethod.UNSUBSCRIBE, uri, SnaErrorfulMessage.NOT_FOUND_ERROR_CODE, "Resource not found",null);
				return tatooRequestId(requestId, response);
			}
			final String uriRemote=String.format("/%s/%s/%s",serviceProviderId,serviceId,resourceId);
			response=new UnsubscribeResponse(mediator,uriRemote,Status.SUCCESS, 200);
			response.setResponse(new JSONObject().put("message","unsubscription done"));
			mediator.callService(SnaAgent.class, new StringBuilder().append("(org.eclipse.sensinact.gateway.agent.id="
				).append(subscriptionId).append(")").toString(), new Executable<SnaAgent,Void>(){
					@Override
					public Void execute(SnaAgent agent) throws Exception {
						agent.stop();
						return null;
					}
				}
			);
			return tatooRequestId(requestId, response);

		}

		@Override
		public DescribeResponse<String> getAll(String requestId, String filter, FilteringCollection filterCollection) {

			final SessionKey sessionKey= SensiNact.this.getSessionKeyFromToken(this.getSessionId());
			
			DescribeResponse<String> response = null;
			DescribeMethod<String> method = new DescribeMethod<String>(mediator, UriUtils.PATH_SEPARATOR, null,
					DescribeType.COMPLETE_LIST);

			DescribeResponseBuilder<String> builder = method.createAccessMethodResponseBuilder(null);

			final String ldapFilter;
			if (filterCollection != null) 
				ldapFilter = filterCollection.composeLDAPFormatedFilter(filter);
			else 
				ldapFilter = filter;
			
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

			if (filterCollection != null) 
				result = filterCollection.apply(result);
			
			builder.setAccessMethodObjectResult(result);
			response = builder.createAccessMethodResponse(Status.SUCCESS);

			if (filterCollection != null)
				response.put("filters", new JSONArray(filterCollection.filterJsonDefinition()),
						filterCollection.hideFilter());
			return tatooRequestId(requestId, response);
		}

		@Override
		public DescribeResponse<String> getProviders(String requestId, FilteringCollection filterCollection) {

			final SessionKey sessionKey= SensiNact.this.getSessionKeyFromToken(this.getSessionId());
			
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

		@Override
		public DescribeResponse<JSONObject> getProvider(String requestId, final String serviceProviderId) {

			final SessionKey sessionKey= SensiNact.this.getSessionKeyFromToken(this.getSessionId());

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

		@Override
		public DescribeResponse<String> getServices(String requestId, final String serviceProviderId,
				FilteringCollection filterCollection) {

			final SessionKey sessionKey= SensiNact.this.getSessionKeyFromToken(this.getSessionId());

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

		@Override
		public DescribeResponse<JSONObject> getService(String requestId, final String serviceProviderId,
				final String serviceId) {
			final SessionKey sessionKey= SensiNact.this.getSessionKeyFromToken(this.getSessionId());
			
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

		@Override
		public DescribeResponse<String> getResources(String requestId, final String serviceProviderId,
				final String serviceId, FilteringCollection filterCollection) {
			final SessionKey sessionKey= SensiNact.this.getSessionKeyFromToken(this.getSessionId());

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

		@Override
		public DescribeResponse<JSONObject> getResource(final String requestId, final String serviceProviderId,
				final String serviceId, final String resourceId) {
			final SessionKey sessionKey= SensiNact.this.getSessionKeyFromToken(this.getSessionId());
			
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
				DescribeType.RESOURCE, uri, SnaErrorfulMessage.NOT_FOUND_ERROR_CODE, "Resource not found", null);
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

		@Override
		public boolean isAccessible(String serviceProviderId, String serviceId, String resourceId) {
			return isAccessible(UriUtils.getUri(new String[] {serviceProviderId, serviceId, resourceId}));
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
			final SessionKey sessionKey= SensiNact.this.getSessionKeyFromToken(this.getSessionId());
			
			Boolean accessible = AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
				@Override
				public Boolean run() {
					boolean isAccessible =  SensiNact.this.isAccessible(sessionKey, path);				
					if(!isAccessible && sessionKey.localID() == 0){
						String[] uriElements = UriUtils.getUriElements(path);
						if(uriElements.length < 3)
							return false;	
						final String[] providerElements = uriElements[0].split(":");				
						Boolean bool = remoteCoreInvocation(uriElements[0], new Executable<SensinactCoreBaseIface, Boolean>() {
							@Override
							public Boolean execute(SensinactCoreBaseIface core) throws Exception {
								if(core.isAccessible(sessionKey.getPublicKey(), providerElements.length>1?providerElements[1]:providerElements[0], 
									uriElements[1], uriElements[2]))
									return true;
								return false;
							}
						});
						return bool==null?false:bool.booleanValue();
					}
					return isAccessible;
				}});
			return accessible==null?false:accessible.booleanValue();
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
		 * @param identifier the String identifier of the Session to be created
		 */
		SensiNactAuthenticatedSession(String identifier) {
			super(identifier);
		}

		@Override
		public final void changePassword(final String oldPassword, final String newPassword) throws SecuredAccessException {

			final SessionKey sessionKey= SensiNact.this.getSessionKeyFromToken(this.getSessionId());
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

	// ********************************************************************//
	// ABSTRACT DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// STATIC DECLARATIONS //
	// ********************************************************************//
	private static final Logger LOG=LoggerFactory.getLogger(SensiNact.class);
	protected static final int LOCAL_ID = 0;
	
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

	// ********************************************************************//
	// INSTANCE DECLARATIONS //
	// ********************************************************************//

	private final AtomicInteger count = new AtomicInteger(LOCAL_ID + 1);
	private final Sessions sessions;
	private AccessTree<? extends AccessNode> anonymousTree;

	public Mediator mediator;
	private RegistryEndpoint registry;
	
	@Reference
	SecuredAccess securedAccess;


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
	
	@Reference
	private ConditionalPermissionAdmin cpa;

	public SensiNact() {
		this.sessions = new Sessions();
	}
	
	@Activate
	public void activate(ComponentContext context) throws SecuredAccessException, 
	BundleException, DataStoreException  {
		
		Mediator mediator = new Mediator(context.getBundleContext());
		List<String> types = ReflectUtils.getAllStringTypes(mediator.getContext().getBundle());

		StringBuilder builder = new StringBuilder();

		for (int index = 0; index < types.size(); index++) {
			if (index > 0)
				builder.append("\\,");
			builder.append(types.get(index));
		}

		ConditionalPermissionUpdate cpu = cpa.newConditionalPermissionUpdate();
		List piList = cpu.getConditionalPermissionInfos();
		
		ConditionalPermissionInfo cpiDeny = cpa.newConditionalPermissionInfo(
		String.format("DENY { [org.eclipse.sensinact.gateway.core.security.perm.StrictCodeBaseCondition \"%s\" \"!\"]"
			+ " (org.osgi.framework.ServicePermission \"org.eclipse.sensinact.gateway.core.Core\" \"register\")"
			+ "(org.osgi.framework.ServicePermission \"org.eclipse.sensinact.gateway.core.SensiNactResourceModel\" \"register,get\")"
			+ "(org.osgi.framework.ServicePermission \"org.eclipse.sensinact.gateway.core.SensiNactResourceModelElement\" \"register,get\")"
			+ "(org.osgi.framework.ServicePermission \"org.eclipse.sensinact.gateway.core.message.LocalAgent\" \"register,get\")"
			+ "(org.osgi.framework.ServicePermission \"org.eclipse.sensinact.gateway.core.message.RemoteAgent\" \"register,get\")"
			+ "(org.osgi.framework.ServicePermission \"org.eclipse.sensinact.gateway.core.remote.RemoteCore\" \"register,get\")"
			+ "(org.osgi.framework.ServicePermission \"org.eclipse.sensinact.gateway.core.security.SecuredAccess\" \"register,get\")"
			+ "(org.osgi.framework.ServicePermission \"org.eclipse.sensinact.gateway.core.security.UserManager\" \"register,get\")"
			+ "(org.osgi.framework.ServicePermission \"org.eclipse.sensinact.gateway.core.security.UserKeyBuilder\" \"register,get\")"
			+ "(org.osgi.framework.ServicePermission \"org.eclipse.sensinact.gateway.core.security.SecurityDataStoreService\" \"register,get\")"
			+ "} null", builder.toString()));
		piList.add(cpiDeny);
		
		ConditionalPermissionInfo cpiAllow = null;

		cpiAllow = cpa.newConditionalPermissionInfo(
			"ALLOW {[org.eclipse.sensinact.gateway.core.security.perm.CodeBaseCondition \"*\"](java.security.AllPermission \"\" \"\")} null");
		
		piList.add(cpiAllow);

		if (!cpu.commit()) 
			throw new ConcurrentModificationException("Permissions changed during update");
		
		ServiceLoader<UserKeyBuilderFactory> userKeyBuilderFactoryLoader = ServiceLoader.load(UserKeyBuilderFactory.class,
			mediator.getClassLoader());	
				
		Iterator<UserKeyBuilderFactory> iterator = userKeyBuilderFactoryLoader.iterator();

		while(iterator.hasNext()) {
			UserKeyBuilderFactory<?, ?, ?> factory = iterator.next();
			if(factory!=null) 
				factory.newInstance(mediator);
		}
		
		if (securedAccess == null) 
			throw new BundleException("A SecuredAccess service was excepted");
		
		this.anonymousTree = securedAccess.getUserAccessTree(UserManager.ANONYMOUS_PKEY);
		
		this.mediator = mediator;
		this.registry = new RegistryEndpoint(mediator);
		
		ServiceLoader<SensinactCoreBaseIFaceManagerFactory> loader = ServiceLoader.load(
			SensinactCoreBaseIFaceManagerFactory.class, mediator.getClassLoader());
		
		Iterator<SensinactCoreBaseIFaceManagerFactory> it = loader.iterator();
		
		while(it.hasNext()){
			SensinactCoreBaseIFaceManagerFactory factory = it.next();
			SensinactCoreBaseIFaceManager manager = factory.instance();
			if(manager!=null) {
				manager.start(mediator);
				break;
			}
		}
	}
	
	@Deactivate
	public void deactivate() {
		this.close();
	}

//	/**
//	 * Constructor
//	 * 
//	 * @param mediator
//	 *            the extended {@link Mediator} allowing the {@link Core} to be
//	 *            instantiated to interact with the OSGi host environment
//	 * 
//	 * @throws SecuredAccessException
//	 * @throws BundleException
//	 */
//	public SensiNact(final Mediator mediator) throws SecuredAccessException, BundleException, DataStoreException {
//		
//		SecuredAccess securedAccess = null;
//		ServiceLoader<SecurityDataStoreServiceFactory> dataStoreServiceFactoryLoader = 
//			ServiceLoader.load(SecurityDataStoreServiceFactory.class, mediator.getClassLoader());
//
//		Iterator<SecurityDataStoreServiceFactory> dataStoreServiceFactoryIterator = 
//			dataStoreServiceFactoryLoader.iterator();
//
//		if (dataStoreServiceFactoryIterator.hasNext()) {
//			SecurityDataStoreServiceFactory<?> factory = dataStoreServiceFactoryIterator.next();
//			if (factory != null) {
//				factory.newInstance(mediator);
//			}
//		}
//		ServiceLoader<UserManagerFactory> userManagerFactoryLoader = ServiceLoader.load(
//			UserManagerFactory.class, mediator.getClassLoader());
//
//		Iterator<UserManagerFactory> userManagerFactoryIterator = userManagerFactoryLoader.iterator();
//
//		while (userManagerFactoryIterator.hasNext()) {
//			UserManagerFactory factory = userManagerFactoryIterator.next();
//			if (factory != null) {
//				factory.newInstance(mediator);
//				break;
//			}
//		}
//		ServiceLoader<SecuredAccessFactory> securedAccessFactoryLoader = ServiceLoader.load(
//				SecuredAccessFactory.class, mediator.getClassLoader());
//
//		Iterator<SecuredAccessFactory> securedAccessFactoryIterator = securedAccessFactoryLoader.iterator();
//
//		while (securedAccessFactoryIterator.hasNext()) {
//			SecuredAccessFactory factory = securedAccessFactoryIterator.next();
//			if (factory != null) {
//				securedAccess = factory.newInstance(mediator);
//				break;
//			}
//		}
//		if (securedAccess == null) {
//			throw new BundleException("A SecuredAccess service was excepted");
//		}
//		securedAccess.createAuthorizationService();
//		final SecuredAccess sa = securedAccess;
//
//		AccessController.doPrivileged(new PrivilegedAction<Void>() {
//			@Override
//			public Void run() {
//				mediator.register(sa, SecuredAccess.class, null);
//				return null;
//			}
//		});
//		this.defaultLocation = ModelInstance.defaultLocation(mediator);
//		this.sessions = new Sessions();
//
//		this.anonymousTree = mediator.callService(SecuredAccess.class,
//			new Executable<SecuredAccess, AccessTree<? extends AccessNode>>() {
//				@Override
//				public AccessTree<? extends AccessNode> execute(SecuredAccess securedAccess) throws Exception {
//					return securedAccess.getUserAccessTree(UserManager.ANONYMOUS_PKEY);
//				}
//			});
//		this.mediator = mediator;
//		this.registry = new RegistryEndpoint(mediator);
//		
//		ServiceLoader<SensinactCoreBaseIFaceManagerFactory> loader = ServiceLoader.load(
//			SensinactCoreBaseIFaceManagerFactory.class, mediator.getClassLoader());
//		
//		Iterator<SensinactCoreBaseIFaceManagerFactory> it = loader.iterator();
//		
//		while(it.hasNext()){
//			SensinactCoreBaseIFaceManagerFactory factory = it.next();
//			SensinactCoreBaseIFaceManager manager = factory.instance();
//			if(manager!=null) {
//				manager.start(mediator);
//				break;
//			}
//		}
//	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public AuthenticatedSession getSession(final Authentication<?> authentication)
			throws InvalidKeyException, InvalidCredentialException {
		AuthenticatedSession session = null;
		if (authentication == null) 
			return null;
		else if(SessionToken.class.isAssignableFrom(authentication.getClass())) 
			session = this.getSession(((SessionToken) authentication).getAuthenticationMaterial());
		else{
			UserKey userKey = this.doPrivilegedService(UserKeyBuilder.class, 
				String.format("(identityMaterial=%s)",authentication.getClass().getCanonicalName()),
				new Executable<UserKeyBuilder, UserKey>() {
					@Override
					public UserKey execute(UserKeyBuilder service) throws Exception {
						return service.buildKey(authentication);
					}
				});
			if (userKey == null) 
				throw new InvalidCredentialException("Invalid credentials");
			
			String pkey = userKey.getPublicKey();
			AccessTree<? extends AccessNode> tree = this.getUserAccessTree(pkey);
			SessionKey sessionKey = new SessionKey(mediator, LOCAL_ID, SensiNact.this.nextToken(), tree, null);
			sessionKey.setUserKey(userKey);
			session = new SensiNactAuthenticatedSession(sessionKey.getToken());
			synchronized(this.sessions) {
				sessions.put(sessionKey, session);
			}
		}
		return session;
	}

	@Override
	public AuthenticatedSession getSession(final String token) {
		AuthenticatedSession session;
		synchronized(this.sessions){
			session= (AuthenticatedSession) this.sessions.getSessionFromToken(token);
		}
		return session;
	}	
	
	@Override
	public Session getRemoteSession(final String publicKey) {
		final int sessionCount = count.incrementAndGet();

		Session session = null;
		String filteredKey = publicKey;
		Class<? extends Session> sessionClass = null;

		if (publicKey.startsWith(UserManager.ANONYMOUS_PKEY)) {
			filteredKey = new StringBuilder().append(publicKey).append("_remote"
					).append(sessionCount).toString();
			sessionClass = SensiNactAnonymousSession.class;
		} else {
			sessionClass = SensiNactAuthenticatedSession.class;
		}
		final String sessionToken;				
		synchronized(SensiNact.this.count) {
			sessionToken = SensiNact.this.nextToken();
		}
		AccessTree<? extends AccessNode> tree = SensiNact.this.getUserAccessTree(filteredKey);
		SessionKey sessionKey = new SessionKey(mediator, sessionCount, sessionToken, tree, null);	
		sessionKey.setUserKey(new UserKey(filteredKey));					
		try {
			session = sessionClass.getDeclaredConstructor(
				new Class<?>[] { SensiNact.class, String.class }).newInstance(
					new Object[] { SensiNact.this, sessionKey.getToken() });
			synchronized(SensiNact.this.sessions) {
				SensiNact.this.sessions.put(sessionKey, session);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(),e);
		}
		return session;
	}

	@Override
	public AnonymousSession getAnonymousSession() {
		int sessionCount = count.incrementAndGet();
		String sessionToken = this.nextToken();	
		
		String pkey = new StringBuilder().append(UserManager.ANONYMOUS_PKEY).append(
			"_").append(sessionCount).toString();
		SessionKey sessionKey = new SessionKey(mediator, LOCAL_ID, sessionToken, 
				this.getAnonymousTree(), null);
		sessionKey.setUserKey(new UserKey(pkey));

		AnonymousSession session = new SensiNactAnonymousSession(sessionToken);
		synchronized(this.sessions) {
			this.sessions.put(sessionKey, session);
		}
		return session;
	}
	
	@Override
	public Session getApplicationSession(final Mediator mediator, final String privateKey) {

		final int sessionCount = count.incrementAndGet();
		final String sessionToken = this.nextToken();	
		
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
		synchronized(this.sessions) {
			sessions.put(skey, session);
		}
		return session;
	}

	@Override
	public String namespace() {
		return this.registry.namespace();
	}

	@Override
	public String registerAgent(final Mediator mediator, final MidAgentCallback callback, final SnaFilter filter) {
		String agentKey = getAccessKey(mediator);		
		final LocalAgent agent = LocalAgentImpl.createAgent(mediator, callback, filter, agentKey);
		AccessController.<Void>doPrivileged(new PrivilegedAction<Void>() {
			@Override
			public Void run() {
				agent.start();
				return null;
			}
		});
		return callback.getName();
	}	
	
	//Retrieves the access key attaches to a specific bundle and agent
	private String getAccessKey(Mediator mediator) {
		final Bundle bundle = mediator.getContext().getBundle();

		final String bundleIdentifier = this.doPrivilegedService(BundleValidation.class, null,
				new Executable<BundleValidation, String>() {
					@Override
					public String execute(BundleValidation bundleValidation) throws Exception {
						return bundleValidation.check(bundle);
					}
				});		
		String publicKey = this.doPrivilegedService(SecuredAccess.class, null,
				new Executable<SecuredAccess, String>() {
					@Override
					public String execute(SecuredAccess securedAccess) throws Exception {
						return securedAccess.getAgentPublicKey(bundleIdentifier);
					}
				});
		return publicKey;
	}
	
	//builds an AthenticatedSession based on the String public key passed as parameter 
	private Session buildLocalSessionFromPublicKey(String publicKey) {
		Session session = null;

		count.incrementAndGet();
		String sessionToken = this.nextToken();		
		
		AccessTree<? extends AccessNode> tree = SensiNact.this.getUserAccessTree(publicKey);
		SessionKey sessionKey = new SessionKey(mediator, LOCAL_ID, sessionToken, tree, null);	
		sessionKey.setUserKey(new UserKey(publicKey));	
	
		try {
			session = new SensiNactAuthenticatedSession(sessionKey.getToken());
			synchronized(SensiNact.this.sessions) {
				SensiNact.this.sessions.put(sessionKey, session);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(),e);
		}
		return session;
	}

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
	 * @param identifier the identifier of the {@link SnaAgent} to register
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
		final SessionKey sessionKey = this.getSessionKeyFromToken(identifier);
		Set<ServiceProvider> set = new HashSet<ServiceProvider>();
		set.addAll(this.registry.serviceProviders(sessionKey, filter));
		return set;
	}

	/**
	 * Returns the {@link ServiceProvider} whose String identifier is passed as parameter
	 * 
	 * @param identifier the String identifier of the {@link Session} requiring the service provider
	 * @param serviceProviderId the String identifier of the service provider
	 * 
	 * @return the {@link ServiceProvider}
	 */
	protected ServiceProvider serviceProvider(String identifier, String serviceProviderId) {
		final SessionKey sessionKey = this.getSessionKeyFromToken(identifier);
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
		SessionKey sessionKey = this.getSessionKeyFromToken(identifier);
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
		SessionKey sessionKey = this.getSessionKeyFromToken(identifier);	
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
	protected boolean isAccessible(final String publicKey, AccessTree<?> tree, final String path) {
		String[] uriElements = UriUtils.getUriElements(path);
		String[] providerElements = uriElements[0].split(":");
		String[] uri = new String[uriElements.length];
		
		if(uriElements.length > 1) 
			System.arraycopy(uriElements, 1, uri, 1, uriElements.length -1);		
		
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
	private <F> F remoteCoreInvocation(String serviceProviderId, Executable<SensinactCoreBaseIface, F> executable) {
		String[] serviceProviderIdElements = serviceProviderId.split(":");
		String remoteNamespace = serviceProviderIdElements[0];
		F f = null;
		if (serviceProviderIdElements.length == 1 || remoteNamespace.length() == 0 || remoteNamespace.equals(this.namespace()) ) 
			return f;		
		f = mediator.callService(SensinactCoreBaseIface.class, String.format("(%s=%s)",
			SensinactCoreBaseIFaceManager.REMOTE_NAMESPACE_PROPERTY, remoteNamespace), executable);
		return f;
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
	private JSONObject act(String identifier, final String serviceProviderId, final String serviceId,
			final String resourceId, final Object[] parameters) {
		final SessionKey sessionKey = this.getSessionKeyFromToken(identifier);
		String response = remoteCoreInvocation(serviceProviderId, new RemoteAccessMethodExecutable(
			mediator,
			AccessMethod.Type.valueOf(AccessMethod.ACT), sessionKey.getPublicKey(
			)).withServiceProvider(serviceProviderId
			).withService(serviceId
		    ).withResource(resourceId
		    ).with(RemoteAccessMethodExecutable.ARGUMENTS_TK, parameters));
		JSONObject object = new JSONObject(response);
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
	private JSONObject set(SessionKey sessionKey, final String serviceProviderId, final String serviceId,
			final String resourceId, final String attributeId, final Object parameter, Object...args) {
		String response = remoteCoreInvocation(serviceProviderId, 
			new RemoteAccessMethodExecutable(mediator, AccessMethod.Type.valueOf(AccessMethod.SET
				), sessionKey.getPublicKey()
				).withServiceProvider(serviceProviderId
				).withService(serviceId
				).withResource(resourceId
				).withAttribute(attributeId
				).with(RemoteAccessMethodExecutable.VALUE_TK, parameter));
		JSONObject object = new JSONObject(response);
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
	private JSONObject get(String identifier, final String serviceProviderId, final String serviceId,
	final String resourceId, final String attributeId, Object...args) {
		final SessionKey sessionKey = this.getSessionKeyFromToken(identifier);
		if(serviceProviderId.indexOf('(') < 0) {			
			String response = remoteCoreInvocation(serviceProviderId, 
				new RemoteAccessMethodExecutable(mediator, AccessMethod.Type.valueOf(AccessMethod.GET
				), sessionKey.getPublicKey()
				).withServiceProvider(serviceProviderId
				).withService(serviceId
				).withResource(resourceId
				).withAttribute(attributeId));
		    JSONObject object = null;
		    if(response != null)
		    	object = new JSONObject(response);
		    return object;
		}
		List<Future<String>> results = new ArrayList<Future<String>>();		
		ExecutorService executor = Executors.newFixedThreadPool(20);
		
		if (sessionKey.localID() == 0) {
			
			Collection<ServiceReference<SensinactCoreBaseIface>> references = null;
			try {
				references = mediator.getContext().getServiceReferences(SensinactCoreBaseIface.class, String.format("(!(%s=%s))",
					SensinactCoreBaseIFaceManager.REMOTE_NAMESPACE_PROPERTY,namespace()));
			} catch (InvalidSyntaxException e) {
				LOG.debug(e.getMessage());
			}
			if(references!=null) {
				for (ServiceReference<SensinactCoreBaseIface> reference : references) {
					final SensinactCoreBaseIface core;
					if (reference == null || (core = mediator.getContext().getService(reference)) == null)
						continue;					
					results.add(executor.submit(new Callable<String>() {
						@Override
						public String call() throws Exception {
							return core.get(sessionKey.getPublicKey(), serviceProviderId, serviceId, resourceId, attributeId);
						}
					}));
				}
			}
		}
		Collection<ServiceReference<SensiNactResourceModel>> modelReferences = this.registry.getReferences(
				sessionKey.getAccessTree(), serviceProviderId);
		try {
			for (ServiceReference<SensiNactResourceModel> modelReference : modelReferences) {
				SensiNactResourceModel resourceModel;
				if (modelReference == null || (resourceModel = mediator.getContext().getService(modelReference)) == null)
					continue;
				
				ServiceProvider p = null;
				try {
					p = ((ServiceProviderImpl) resourceModel.getRootElement()).<ServiceProvider>getProxy(sessionKey);
				} catch (ModelElementProxyBuildException e) {
					continue;
				}
				List<Service> services = p.getServices();
				for (Service s : services) {
					if (s == null || !s.isAccessible())
						continue;
					if(!match(serviceId,s.getName()))
						continue;
					List<Resource> resources = s.getResources();
					for (Resource r : resources) {
						if (r == null || !r.isAccessible())
							continue;
						if(!match(resourceId,r.getName()))
							continue;
						final Resource resource = r;
						final Resource.Type type = resource.getType();
						if (attributeId == null && Resource.Type.ACTION.equals(type)) {
							continue;
						}
						results.add(executor.submit(new Callable<String>() {
							@Override
							public String call() throws Exception {
								if (attributeId == null) 
									return ((DataResource) resource).get().getJSON();								
								return resource.get(attributeId).getJSON();
							}
						}));
					}
				}
			}
			StringBuilder builder = new StringBuilder();
			String name = String.format("/%s/%s/%s/%s", serviceProviderId,
					serviceId==null?"*":serviceId,
					resourceId==null?"*":resourceId,
					attributeId==null?"{default-attribute}":attributeId);
			builder.append("{\"statusCode\":200,\"response\":{\"name\":\"");
			builder.append(name);
			builder.append("\",\"type\":\"array\",\"value\":[");
			int index = 0;
			for (Future<String> future : results) {
				try {
					String result = future.get();
					if (index > 0) 
						builder.append(",");
					builder.append(result);
					index++;
				} catch (CancellationException | ExecutionException | InterruptedException e) {
					continue;
				}
			}
			builder.append("]}}");
			return new JSONObject(builder.toString());
		} finally {
			for (ServiceReference<SensiNactResourceModel> modelReference : modelReferences) {
				if (modelReference != null) 
					mediator.getContext().ungetService(modelReference);
			}
		}
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
	private JSONObject getResource(String identifier, final String serviceProviderId, final String serviceId, final String resourceId) {
		final SessionKey sessionKey = this.getSessionKeyFromToken(identifier);
		final String localNamespace = this.namespace();
		JSONObject object = remoteCoreInvocation(serviceProviderId, new Executable<SensinactCoreBaseIface, JSONObject>() {
			@Override
			public JSONObject execute(SensinactCoreBaseIface core) throws Exception {
				if(core == null || core.namespace().equals(localNamespace)) {
					return null;
				}
				return new JSONObject(core.getResource(sessionKey.getPublicKey(),
					serviceProviderId.substring(serviceProviderId.indexOf(':') + 1), 
					    serviceId, resourceId));
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
	private String getResources(String identifier, final String serviceProviderId, final String serviceId) {
		final SessionKey sessionKey = this.getSessionKeyFromToken(identifier);
		final String localNamespace = this.namespace();
		String object = remoteCoreInvocation(serviceProviderId, new Executable<SensinactCoreBaseIface, String>() {
			@Override
			public String execute(SensinactCoreBaseIface core) throws Exception {
				if(core == null || core.namespace().equals(localNamespace)) {
					return null;
				}
				return core.getResources(sessionKey.getPublicKey(), serviceProviderId.substring(
						serviceProviderId.indexOf(':') + 1), serviceId);
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
	private JSONObject getService(String identifier, final String serviceProviderId, final String serviceId) {
		final SessionKey sessionKey = this.getSessionKeyFromToken(identifier);
		final String localNamespace = this.namespace();
		JSONObject object = remoteCoreInvocation(serviceProviderId, new Executable<SensinactCoreBaseIface, JSONObject>() {
			@Override
			public JSONObject execute(SensinactCoreBaseIface core) throws Exception {
				if(core == null || core.namespace().equals(localNamespace)) {
					return null;
				}
				return new JSONObject(core.getService(sessionKey.getPublicKey(),
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
	private String getServices(String identifier, final String serviceProviderId) {
		final SessionKey sessionKey = this.getSessionKeyFromToken(identifier);
		final String localNamespace = this.namespace();
		String object = remoteCoreInvocation(serviceProviderId, new Executable<SensinactCoreBaseIface, String>() {
			@Override
			public String execute(SensinactCoreBaseIface core) throws Exception {
				if(core == null || core.namespace().equals(localNamespace)) {
					return null;
				}
				return core.getServices(sessionKey.getPublicKey(), serviceProviderId.substring(
						serviceProviderId.indexOf(':') + 1));
			}
		});
		return object;
	}

	/**
	 * Returns the JSON formated description of the service provider whose String
	 * identifier is passed as parameter
	 * 
	 * @param identifier the String identifier of the {@link Session} requiring the 
	 * service provider description
	 * @param serviceProviderId the String identifier of the service provider to return 
	 * the description of
	 * 
	 * @return the JSON formated description of the specified service provider
	 */
	private JSONObject getProvider(String identifier, final String serviceProviderId) {
		final SessionKey sessionKey = this.getSessionKeyFromToken(identifier);
		final String localNamespace = this.namespace();
		JSONObject object = remoteCoreInvocation(serviceProviderId, new Executable<SensinactCoreBaseIface, JSONObject>() {
			@Override
			public JSONObject execute(SensinactCoreBaseIface core) throws Exception {
				if(core == null || core.namespace().equals(localNamespace)) {
					return null;
				}
				return new JSONObject(core.getProvider(sessionKey.getPublicKey(), serviceProviderId.substring(
						serviceProviderId.indexOf(':') + 1)));
			}
		});
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
	private String getProviders(String identifier, final String filter) {

		final SessionKey sessionKey = this.getSessionKeyFromToken(identifier);
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

		if (sessionKey.localID() != 0) 
			return local;
		
		final StringBuilder content = new StringBuilder();
		if (local != null && local.length() > 0) 
			content.append(local);
		
		final String localNamespace = this.namespace();
		AccessController.doPrivileged(new PrivilegedAction<Void>() {
			@Override
			public Void run() {
				SensiNact.this.mediator.callServices(SensinactCoreBaseIface.class, null, 
				new Executable<SensinactCoreBaseIface, Void>() {
					@Override
					public Void execute(SensinactCoreBaseIface core) throws Exception {
						if(core == null || core.namespace().equals(localNamespace)) {
							return null;
						}
						String o = core.getProviders(sessionKey.getPublicKey(), filter);
		
						if (o != null && o.length() > 0) {
							if (content.length() > 0) {
								content.append(",");
							}
							content.append(o);
						}
						return null;
					}
				});
				return null;
			}});
		return content.toString();
	}

	/**
	 * Returns the JSON formated description of the service provider whose identifier
	 * is passed as parameter for the {@link Session} whose String identifier is also 
	 * passed as parameter
	 * 
	 * @param identifier the String identifier of the {@link Session} requiring the list 
	 * of available service providers
	 * @param filter the (extra) LDAP String filter applying
	 * 
	 * @return the JSON String formated description of the specified service provider
	 */
	private String getProvider(String identifier, String providerId, String filter) {

		final SessionKey sessionKey = this.getSessionKeyFromToken(identifier);
		String effectiveFilter = null;
		if (filter != null && filter.length() > 0) {
			try {
				mediator.getContext().createFilter(filter);
				effectiveFilter = filter;
			} catch (InvalidSyntaxException e) {
				effectiveFilter = null;
			}
		}
		String provider = this.registry.getProvider(sessionKey, sessionKey.localID() != 0, providerId, effectiveFilter);
		return provider;
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
		
		final SessionKey sessionKey = this.getSessionKeyFromToken(identifier);
		
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
		final String localNamespace = this.namespace();
		AccessController.doPrivileged(new PrivilegedAction<Void>() {
			@Override
			public Void run() {
				SensiNact.this.mediator.callServices(SensinactCoreBaseIface.class, null, 
					new Executable<SensinactCoreBaseIface, Void>() {
					@Override
					public Void execute(SensinactCoreBaseIface core) throws Exception {
						if(core == null || core.namespace().equals(localNamespace)) {
							return null;
						}
						String o = core.getAll(sessionKey.getPublicKey(), filter);

						if (o != null && o.length() > 0) {
							if (content.length() > 0) {
								content.append(",");
							}
							content.append(o);
						}
						return null;
					}
				});
				return null;
			}});
		return content.toString();
	}

	public void close() {
		LOG.debug("closing sensiNact core");
		this.mediator.callService(SensinactCoreBaseIFaceManager.class, 
			new Executable<SensinactCoreBaseIFaceManager,Void>() {
				@Override
				public Void execute(SensinactCoreBaseIFaceManager sensinactCoreBaseIFaceManager) throws Exception {							
					sensinactCoreBaseIFaceManager.stop();
					return null;
				}
			}
		);
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

	private final SessionKey getSessionKeyFromToken(String sessionId) {
		SessionKey sessionKey;
		synchronized(this.sessions) {
			sessionKey = this.sessions.get(new KeyExtractor<KeyExtractorType>(KeyExtractorType.TOKEN, sessionId));
		}	
		if(sessionKey == null)
			throw new IllegalArgumentException("Invalid session token");
		return sessionKey;
	}
	
	private boolean match(String expected, String found) {
		if(expected == null)
			return true;
		if(found == null)
			return false;
		if(found.equals(expected))
			return true;
		if(!expected.startsWith("(") || !expected.endsWith(")"))
			return false;		
		String pattern = expected.substring(1,expected.length()-1);
		boolean match = true;
		if(pattern.startsWith("*")) {
			int pos = found.length()-1;
			for(int i=pattern.length()-1;i>0;i--) {
				if(pattern.charAt(i)!=found.charAt(pos)) {					
					match=false;
					break;
				}
				pos--;
			}
	    } else if(pattern.endsWith("*")){
			int pos = 0;
			for(int i=0;i<pattern.length()-1;i++) {
				if(pattern.charAt(i)!=found.charAt(pos)) {					
					match=false;
					break;
				}
				pos++;
			}
		} else {
			try {
				Pattern _pattern = Pattern.compile(pattern); 
				match = _pattern.matcher(found).matches();
			} catch(Exception e) {
				match = false;
			}
		}
		return match;
	}

	private String nextToken() {
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
