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
package org.eclipse.sensinact.gateway.remote.http;

import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.remote.AbstractRemoteEndpoint;
import org.eclipse.sensinact.gateway.core.message.AbstractSnaMessage;
import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.eclipse.sensinact.gateway.core.message.SnaFilter;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.nthbnd.http.callback.CallbackContext;
import org.eclipse.sensinact.gateway.nthbnd.http.callback.CallbackService;
import org.eclipse.sensinact.gateway.protocol.http.client.ConnectionConfigurationImpl;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleRequest;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleResponse;
import org.eclipse.sensinact.gateway.util.IOUtils;
import org.eclipse.sensinact.gateway.util.JSONUtils;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.ServiceRegistration;

/**
 * Simple RemoteEndpoint implementation using Http connection
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class HttpEndpoint extends AbstractRemoteEndpoint {

    protected String remoteNamespace;
    
    private final String localPath;
    private final String localToken;

    private final String remoteToken;
	private final String targetURL;

    private ServiceRegistration<?> registration;
    
    private Timer connectionTimer;
	
	private final Executable<CallbackContext, Void> callbackService = new Executable<CallbackContext, Void>(){
		@Override
		public Void execute(CallbackContext context) throws Exception {
			String token = context.getRequest().getHeader("X-Auth-Token");
			if(token == null ||!HttpEndpoint.this.localToken.equals(token)) {
				context.setResponseContent(new JSONObject(
					).put("message", "Invalid token"
					).put("statusCode",403
					).toString().getBytes());
			} else {
				byte[] bytes = IOUtils.read(context.getRequest().getInputStream(),false);
				JSONObject request = new JSONObject(new String(bytes));
	            JSONObject response = null;
	            if (request != null) {
	                response = HttpEndpoint.this.incomingRequest(request);
	            }
	            if (response == null) {
					context.setResponseContent(new JSONObject(
							).put("message", "Unable to process the request"
							).put("statusCode",520
							).toString().getBytes());
	            } else {
	            	context.setResponseContent(response.toString().getBytes());
	            }
			}
            context.setResponseStatus(200);
			return null;
		}
	};

    /**
     * @param mediator
     * @param localAddress
     * @param localPort
     * @param remoteAddress
     * @param remotePort
     */
    public HttpEndpoint(Mediator mediator, String localPath, String localToken, 
    	String remoteAddress, int remotePort, String remotePath, String remoteToken) {
        super(mediator);
        if (remoteAddress == null) {
            throw new NullPointerException("Local and remote addresses are needed");
        }
        this.localPath = localPath;
        this.localToken = localToken;
        
        if(this.localToken == null) {
        	throw new NullPointerException("Token is required");
        }
        this.remoteToken = remoteToken;
        int port = remotePort <= 0 ? 80 : remotePort;
        
        String url = null;
        try {
			 url = new URL("http", remoteAddress, port, remotePath).toExternalForm();
		} catch (MalformedURLException e) {
			//e.printStackTrace();
		} 
        this.targetURL = url;
        if(this.targetURL == null) {
        	throw new NullPointerException("target URL is required");
        }
    }

	private String outgoingRequest(JSONObject object){
	    String result = null;
	    try {
	        SimpleResponse response;	        
	        ConnectionConfigurationImpl<SimpleResponse, SimpleRequest> builder = 
	        new ConnectionConfigurationImpl<SimpleResponse, SimpleRequest>();
	        
	        builder.setUri(this.targetURL);
	        builder.setAccept("application/json");
	        builder.setContentType("application/json");
	        builder.setConnectTimeout(5000);
	        builder.setReadTimeout(5000);
	        builder.setContent(object.toString()); 
	        builder.addHeader("X-Auth-Token", this.remoteToken);
	        builder.setHttpMethod("POST");
	        SimpleRequest request = new SimpleRequest(builder);
	        response = request.send();
	        byte[] responseContent = response.getContent();
	        String content = (responseContent == null ? null : new String(responseContent));

	        if(response.getStatusCode() == 400) {
	        	throw new ConnectException();
	        }
	        JSONObject resultObject = new JSONObject(content);
	        result = resultObject.optString("response");
	    } catch(UnknownHostException | SocketException e) { 
	    	this.clientDisconnected();        
		}catch (JSONException |IOException e) {
	        this.mediator.error(e);
	    }
	    return result;
	}

    /**
     * @param request
     * @return
     */
    private JSONObject incomingRequest(JSONObject request) {
        String response = null;
        JSONObject accessMethodResponse = null;
        if (request == null) {
            return null;
        }        
        String uri = request.optString("uri");
        String publicKey = request.optString("pkey");

        String[] uriElements = UriUtils.getUriElements(uri);
        int length = uriElements.length;

        switch (length) {
            case 1:
                String[] subUriELements = uriElements[0].split("\\?");
                switch (subUriELements[0]) {
                    case "namespace":
                        response = super.getLocalNamespace();
                        break;
                    case "agent":
                        if (subUriELements.length != 2) {
                            break;
                        }
                        String agentId = subUriELements[1];
                        JSONObject object = request.optJSONObject("agent");
                        if (JSONObject.NULL.equals(object)) {
                            object = request.optJSONObject("message");
                            if (JSONObject.NULL.equals(object)) {
                                super.remoteCore.unregisterAgent(agentId);

                            } else {
                                super.remoteCore.dispatch(agentId, AbstractSnaMessage.fromJSON(
                                		mediator, object.toString()));
                            }
                        } else {
                            SnaFilter filter = null;
                            JSONObject f = object.optJSONObject("filter");
                            if (!JSONObject.NULL.equals(f)) {
                                filter = new SnaFilter(mediator, f.getString("sender"), 
                                		f.optBoolean("pattern"), f.optBoolean("complement"), 
                                		f.optJSONArray("conditions"));

                                JSONArray t = object.optJSONArray("types");
                                int i = 0;
                                int l = t == null ? 0 : t.length();
                                for (; i < l; i++) {
                                    filter.addHandledType(SnaMessage.Type.valueOf(t.getString(i)));
                                }
                            }
                            super.remoteCore.registerAgent(agentId, filter, object.getString("agentKey"));
                        }
                        break;
                    case "accessible":
                        if (subUriELements.length != 2) {
                            break;
                        }
                        String els = subUriELements[1].substring(subUriELements[1].indexOf('[')+1, subUriELements[1].lastIndexOf(']'));                        
                        String[] array = els.split(",");
                        StringBuilder builder = new StringBuilder();
                        for(int i=0;i<array.length;i++) {
                        	builder.append(UriUtils.PATH_SEPARATOR);
                        	builder.append(array[i]);
                        }
                        boolean accessible = super.remoteCore.isAccessible(publicKey, builder.toString());
                        response = String.valueOf(accessible);
                        break;
                    case "session":
                        super.remoteCore.closeSession(publicKey);
                        break;
                    case "callback":
                        if (subUriELements.length != 2) {
                            break;
                        }
                        String callbackId = subUriELements[1];
                        Recipient recipient = super.recipients.get(callbackId);
                        if (recipient == null) {
                            break;
                        }
                        JSONArray messagesArray = request.optJSONArray("messages");
                        int index = 0;
                        int arrayLength = messagesArray == null ? 0 : messagesArray.length();
                        SnaMessage<?>[] messages = new SnaMessage[arrayLength];
                        try {
                            for (; index < arrayLength; index++) {
                                messages[index] = AbstractSnaMessage.fromJSON(mediator, 
                                		messagesArray.getJSONObject(index).toString());
                            }
                            recipient.callback(callbackId, messages);

                        } catch (Exception e) {
                            mediator.error(e);
                        }
                        break;
                    case "all":
                        if (subUriELements.length > 2) {
                            break;
                        }
                        response = (subUriELements.length == 1) ? super.remoteCore.getAll(publicKey) 
                        		: super.remoteCore.getAll(publicKey, subUriELements[1]);
                        break;
                    case "providers":
                        response = super.remoteCore.getProviders(publicKey);
                        break;
                }
                break;
            case 2:
                response = super.remoteCore.getProvider(publicKey, uriElements[1]);
                break;
            case 3:
                response = super.remoteCore.getServices(publicKey, uriElements[1]);
                break;
            case 4:
                response = super.remoteCore.getService(publicKey, uriElements[1], uriElements[3]);
                break;
            case 5:
                response = super.remoteCore.getResources(publicKey, uriElements[1], uriElements[3]);
                break;
            case 6:
                response = super.remoteCore.getResource(publicKey, uriElements[1], uriElements[3], uriElements[5]);
                break;
            case 7:
                switch (uriElements[6]) {
                    case "GET":
                        accessMethodResponse = super.remoteCore.get(publicKey, uriElements[1], uriElements[3], 
                        		uriElements[5], null);
                        break;
                    case "SET":
                        JSONArray parameters = request.optJSONArray("parameters");
                        JSONObject attr = parameters.optJSONObject(0);
                        String attributeId = null;
                        if (attr != null) {
                            attributeId = attr.optString("value");
                        }
                        attr = parameters.optJSONObject(1);
                        Object data = null;
                        if (attr != null) {
                            data = attr.opt("value");
                        }
                        accessMethodResponse = super.remoteCore.set(publicKey, uriElements[1], uriElements[3], 
                        		uriElements[5], attributeId, data);
                        break;
                    case "ACT":
                        parameters = request.optJSONArray("parameters");
                        int index = 0;
                        int paramsLength = parameters == null ? 0 : parameters.length();

                        Object[] args = new Object[paramsLength];
                        for (; index < paramsLength; index++) {
                            args[index] = parameters.getJSONObject(index).opt("value");
                        }
                        accessMethodResponse = super.remoteCore.act(publicKey, uriElements[1], uriElements[3], 
                        		uriElements[5], args);
                        break;
                    case "SUBSCRIBE":
                        JSONArray conditions = null;
                        parameters = request.optJSONArray("parameters");
                        paramsLength = parameters == null ? 0 : parameters.length();
                        if (paramsLength == 1) {
                            conditions = parameters.getJSONObject(0).optJSONArray("value");
                        }
                        accessMethodResponse = super.remoteCore.subscribe(publicKey, uriElements[1], 
                        		uriElements[3], uriElements[5], conditions);
                        break;
                    case "UNSUBSCRIBE":
                        String subscriptionId = null;
                        parameters = request.optJSONArray("parameters");
                        paramsLength = parameters == null ? 0 : parameters.length();
                        if (paramsLength == 1) {
                            subscriptionId = parameters.getJSONObject(0).optString("value");
                        }
                        accessMethodResponse = super.remoteCore.unsubscribe(publicKey, uriElements[1], uriElements[3], uriElements[5], subscriptionId);
                        break;
                    default:
                        break;
                }
            default:
                break;
        }
        if (accessMethodResponse != null) {
            response = accessMethodResponse.toString();
        }
        return new JSONObject().put("response", response);
    }

    /**
     * @inheritDoc
     * 
     * @see org.eclipse.sensinact.gateway.core.RemoteEndpoint#namespace()
     */
    @Override
    public String namespace() {
        if (this.remoteNamespace == null) {
            String response = null;
			try {
				response = this.outgoingRequest(new JSONObject().put("uri", "/namespace"));
			} catch (Exception e) {
				mediator.error(e);
			}
            if (response != null && response.length() > 0) {
                this.remoteNamespace = response;
            }
        }
        return this.remoteNamespace;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.message.Recipient#
     * callback(java.lang.String, org.eclipse.sensinact.gateway.core.message.SnaMessage[])
     */
    @Override
    public void callback(String callbackId, SnaMessage[] messages) {
        if (!super.getConnected()) {
            return;
        }
        String uri = String.format("/callback?%s", callbackId);
        JSONArray messagesArray = new JSONArray();
        int index = 0;
        int length = messages == null ? 0 : messages.length;
        for (; index < length; index++) {
            JSONObject object = new JSONObject(messages[index].getJSON());
            String path = (String) object.remove("uri");
            object.put("uri", new StringBuilder().append("/").append(super.getLocalNamespace()
            		).append(":").append(path.substring(1)).toString());
            messagesArray.put(object);
        }
        String response = null;
		try {
			response = this.outgoingRequest(new JSONObject().put("uri", uri).put("messages", messagesArray));
		} catch (JSONException e) {
			mediator.error(e);
		}
        if (!JSONObject.NULL.equals(response)) {
            mediator.debug(response.toString());
        }
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.common.primitive.JSONable#getJSON()
     */
    @Override
    public String getJSON() {
        return String.format("{\"local\":\"%s\",\"remote\":\"%s\"}", super.getLocalNamespace(), this.namespace());
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.AbstractRemoteEndpoint#doOpen()
     */
    @Override
    public void doOpen() {
        if (this.remoteNamespace != null) {
            return;
        }
        connectionTimer();
        int timeout = 60 * 3000;
        while (true) {
            if (timeout <= 0 || remoteNamespace != null) {
                break;
            }
            //this.namespace();
            try {
                timeout -= 100;
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.interrupted();
                break;
            }
        }
        this.connectionTimer.cancel();
        this.connectionTimer = null;
        if (this.remoteNamespace != null) {
            super.connected();
        }
    }

    private void connectionTimer() {
        this.connectionTimer = new Timer();
        this.connectionTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try { 
                	if(HttpEndpoint.this.registration==null) {
                		HttpEndpoint.this.registration = mediator.getContext().registerService(
                			CallbackService.class,  new CallbackService() {
                				@Override
                				public String getPattern() {
                					return HttpEndpoint.this.localPath;
                				}
                				@Override
                				public Dictionary getProperties() {
                					return new Hashtable();
                				}
                				@Override
                				public Executable<CallbackContext, Void> getCallbackProcessor() {
                					return HttpEndpoint.this.callbackService;
                				}	        	
                	        }, null);
                	}
	                if (HttpEndpoint.this.namespace() != null) {
	                    mediator.debug("Client socket thread connected - remote namespace : %s",  HttpEndpoint.this.remoteNamespace);
	                    HttpEndpoint.this.connectionTimer.cancel();
	                }
                } catch (Exception e) {
                    mediator.error(e);
                }
            }
        }, 0, 1000 * 5);
    }

    /**
     * The ClientSocketThread attached to this SocketEndpoint
     * stopped
     */
    public void clientDisconnected() {
        if (super.getConnected()) {
            super.disconnected();
            this.remoteNamespace = null;
        } else {
            mediator.warn("An error occurred that impeded the client connection");
        }
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.AbstractRemoteEndpoint#
     * doClose()
     */
    @Override
    protected void doClose() {
        if(this.registration != null) {
	        try {
	        	this.registration.unregister();
	        } catch(IllegalStateException e) {
	        	mediator.error(e);
	        }
	        this.registration = null;
        }
        this.remoteNamespace = null;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.RemoteEndpoint#
     * registerAgent(java.lang.String, org.eclipse.sensinact.gateway.core.message.SnaFilter, java.lang.String)
     */
    @Override
    public void registerAgent(String identifier, SnaFilter filter, String agentKey) {
        String uri = String.format("/agent?%s", identifier);
        String response = null;
		try {
			response = this.outgoingRequest(new JSONObject().put("uri", uri).put("agent", 
				new JSONObject().put("agentKey", agentKey).put("filter", filter == null 
				    ? null : filter.toJSONObject())));
		} catch (JSONException e) {
			mediator.error(e);
		}
        if (response != null) {
            mediator.debug(response.toString());
        }
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.RemoteEndpoint#
     * unregisterAgent(java.lang.String)
     */
    @Override
    public void unregisterAgent(String identifier) {
        String uri = String.format("/agent?%s", identifier);

        String response=null;
		try {
			response = this.outgoingRequest(new JSONObject().put("uri", uri));
		} catch (JSONException e) {
			mediator.error(e);
		}
        if (response != null) {
            mediator.debug(response.toString());
        }
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.RemoteEndpoint#
     * dispatch(java.lang.String, org.eclipse.sensinact.gateway.core.message.SnaMessage)
     */
    @Override
    public void dispatch(String agentId, SnaMessage<?> message) {
        if (!super.getConnected()) {
            return;
        }
        String uri = String.format("/agent?%s", agentId);

        JSONObject object = new JSONObject(message.getJSON());
        String path = (String) object.remove("uri");

        object.put("uri", new StringBuilder().append("/").append(super.getLocalNamespace()).append(":").append(path.substring(1)).toString());

        String response=null;
		try {
			response = this.outgoingRequest(new JSONObject().put("uri", uri).put("message", object));
		} catch (JSONException e) {
			mediator.error(e);
		}

        if (response != null) {
            mediator.debug(response.toString());
        }
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.AbstractRemoteEndpoint#
     * closeSession(java.lang.String)
     */
    @Override
    protected void closeSession(String publicKey) {
        if (!super.getConnected()) {
            return;
        }
        String response=null;
		try {
			response = this.outgoingRequest(new JSONObject().put("uri", "/session").put("pkey", publicKey));
		} catch (JSONException e) {
			mediator.error(e);
		}
        if (response != null) {
            mediator.debug(response.toString());
        }
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.Endpoint#getAll(java.lang.String)
     */
    @Override
    public String getAll(String publicKey) {
        return this.getAll(publicKey, null);
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.Endpoint#
     * getAll(java.lang.String, java.lang.String)
     */
    @Override
    public String getAll(String publicKey, String filter) {
        if (!super.getConnected()) {
            return null;
        }
        String uri = String.format("/all?%s", filter);
        String response=null;
		try {
			response = this.outgoingRequest(new JSONObject().put("uri", uri).put("pkey", publicKey));
		} catch (JSONException e) {
			mediator.error(e);
		}
        if (response != null) {
            mediator.debug(response.toString());
        }
        return response;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.Endpoint#getProviders(java.lang.String)
     */
    @Override
    public String getProviders(String publicKey) {
        if (!super.getConnected()) {
            return null;
        }
        String response = null;
		try {
			response = this.outgoingRequest(new JSONObject().put("uri", "/providers").put("pkey", publicKey));
		} catch (JSONException e) {
			mediator.error(e);
		}
        if (response != null) {
            mediator.debug(response.toString());
        }
        return response;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.Endpoint#
     * getProvider(java.lang.String, java.lang.String)
     */
    @Override
    public String getProvider(String publicKey, String serviceProviderId) {
        if (!super.getConnected()) {
            return null;
        }
        String uri = String.format("/providers/%s", serviceProviderId);
        String response=null;
		try {
			response = this.outgoingRequest(new JSONObject().put("uri", uri).put("pkey", publicKey));
		} catch (JSONException e) {
			mediator.error(e);
		}
        if (response != null) {
            mediator.debug(response.toString());
        }
        return response;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.Endpoint#
     * getServices(java.lang.String, java.lang.String)
     */
    @Override
    public String getServices(String publicKey, String serviceProviderId) {
        if (!super.getConnected()) {
            return null;
        }
        String uri = String.format("/providers/%s/services", serviceProviderId);
        String response = null;
		try {
			response = this.outgoingRequest(new JSONObject().put("uri", uri).put("pkey", publicKey));
		} catch (JSONException e) {
			mediator.error(e);
		}
        if (response != null) {
            mediator.debug(response.toString());
        }
        return response;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.Endpoint#
     * getService(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public String getService(String publicKey, String serviceProviderId, String serviceId) {
        if (!super.getConnected()) {
            return null;
        }
        String uri = String.format("/providers/%s/services/%s", serviceProviderId, serviceId);
        String response=null;
		try {
			response = this.outgoingRequest(new JSONObject().put("uri", uri).put("pkey", publicKey));
		} catch (JSONException e) {
			mediator.error(e);
		}
        if (response != null) {
            mediator.debug(response.toString());
        }
        return response;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.Endpoint#
     * getResources(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public String getResources(String publicKey, String serviceProviderId, String serviceId) {
        if (!super.getConnected()) {
            return null;
        }
        String uri = String.format("/providers/%s/services/%s/resources", serviceProviderId, serviceId);
        String response=null;
		try {
			response = this.outgoingRequest(new JSONObject().put("uri", uri).put("pkey", publicKey));
		} catch (JSONException e) {
			mediator.error(e);
		}
        if (response != null) {
            mediator.debug(response.toString());
        }
        return response;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.Endpoint#
     * getResource(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public String getResource(String publicKey, String serviceProviderId, String serviceId, String resourceId) {
        if (!super.getConnected()) {
            return null;
        }
        String uri = String.format("/providers/%s/services/%s/resources/%s", serviceProviderId, serviceId, resourceId);
        String response=null;
		try {
			response = this.outgoingRequest(new JSONObject().put("uri", uri).put("pkey", publicKey));
		} catch (JSONException e) {
			mediator.error(e);
		}
        if (response != null) {
            mediator.debug(response.toString());
        }
        return response;
    }
    
    /**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Endpoint#isAccessible(java.lang.String,java.lang.String)
	 */
	public boolean isAccessible(String publicKey, String path) {
		if (!super.getConnected()) {
            return false;
        }
		String[] elements = UriUtils.getUriElements(path);
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		for(int i=0;i<elements.length;i++) {
			if(i>0) {
				builder.append(",");
			}
			builder.append(elements[i]);
		}
		builder.append("]");
        String response = this.outgoingRequest(new JSONObject(
        	).put("uri", String.format("/accessible?path=%s",builder.toString())
        	).put("pkey", publicKey));
        
        if (response != null) {
            mediator.debug(response);
        }
        return Boolean.parseBoolean(response);
	}
	
    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.Endpoint#
     * get(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public JSONObject get(String publicKey, String serviceProviderId, String serviceId, String resourceId, String attributeId) {
        if (!super.getConnected()) {
            return null;
        }
        String uri = String.format("/providers/%s/services/%s/resources/%s/GET", serviceProviderId, serviceId, resourceId);
        String response=null;
		try {
			response = this.outgoingRequest(new JSONObject().put("uri", uri).put("pkey", publicKey));
		} catch (JSONException e) {
			mediator.error(e);
		}

        JSONObject result = null;
        if (response != null) {
            mediator.debug(response.toString());
            try {
                result = new JSONObject(response);
            } catch (JSONException | NullPointerException e) {
                mediator.error(e);
            }
        }
        return result;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.Endpoint#
     * set(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Object)
     */
    @Override
    public JSONObject set(String publicKey, String serviceProviderId, String serviceId, String resourceId, String attributeId, Object parameter) {
        if (!super.getConnected()) {
            return null;
        }
        String uri = String.format("/providers/%s/services/%s/resources/%s/SET", serviceProviderId, serviceId, resourceId);
        JSONArray parameters = new JSONArray();
        JSONObject object = new JSONObject();
        object.put("name", "attributeName");
        object.put("type", "string");
        object.put("value", attributeId == null ? "value" : attributeId);
        parameters.put(object);

        Object value = JSONUtils.toJSONFormat(parameter);
        object = new JSONObject();
        object.put("name", "arg0");
        object.put("type", value.getClass().getName());
        object.put("value", value);
        parameters.put(object);

        String response=null;
		try {
			response = this.outgoingRequest(new JSONObject().put("uri", uri).put("pkey", publicKey).put("parameters", parameters));
		} catch (JSONException e) {
			mediator.error(e);
		}
        JSONObject result = null;
        if (response != null) {
            mediator.debug(response.toString());
            try {
                result = new JSONObject(response);
            } catch (JSONException | NullPointerException e) {
                mediator.error(e);
            }
        }
        return result;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.Endpoint#
     * act(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Object[])
     */
    @Override
    public JSONObject act(String publicKey, String serviceProviderId, String serviceId, String resourceId, Object[] parameters) {
        if (!super.getConnected()) {
            return null;
        }
        String uri = String.format("/providers/%s/services/%s/resources/%s/ACT", serviceProviderId, serviceId, resourceId);
        JSONArray parametersArray = new JSONArray();
        int index = 0;
        int length = parameters == null ? 0 : parameters.length;
        for (; index < length; index++) {
            Object value = JSONUtils.toJSONFormat(parameters[index]);
            JSONObject object = new JSONObject();
            object.put("name", "arg" + index);
            object.put("type", value.getClass().getName());
            object.put("value", value);
            parametersArray.put(object);
        }
        String response=null;
		try {
			response = this.outgoingRequest(new JSONObject().put("uri", uri).put("pkey", publicKey).put("parameters", parametersArray));
		} catch (JSONException e) {
			mediator.error(e);
		}
        JSONObject result = null;
        if (response != null) {
            mediator.debug(response.toString());
            try {
                result = new JSONObject(response);
            } catch (JSONException | NullPointerException e) {
                mediator.error(e);
            }
        }
        return result;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.Endpoint#
     * unsubscribe(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public JSONObject unsubscribe(String publicKey, String serviceProviderId, String serviceId, String resourceId, String subscriptionId) {
        if (!super.getConnected()) {
            return null;
        }
        String uri = String.format("/providers/%s/services/%s/resources/%s/UNSUBSCRIBE", serviceProviderId, serviceId, resourceId);
        JSONArray parametersArray = new JSONArray();
        JSONObject object = new JSONObject();
        object.put("name", "subscriptionId");
        object.put("type", "string");
        object.put("value", subscriptionId);
        parametersArray.put(object);
        String response=null;
		try {
			response = this.outgoingRequest(new JSONObject().put("uri", uri).put("pkey", publicKey).put("parameters", parametersArray));
		} catch (JSONException e) {
			mediator.error(e);
		}
        JSONObject result = null;
        if (response != null) {
            mediator.debug(response.toString());
            try {
                result = new JSONObject(response);
            } catch (JSONException | NullPointerException e) {
                mediator.error(e);
            }
        }
        return result;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.AbstractRemoteEndpoint#
     * doSubscribe(java.lang.String, java.lang.String, java.lang.String, java.lang.String, org.json.JSONArray)
     */
    @Override
    protected JSONObject doSubscribe(String publicKey, String serviceProviderId, String serviceId, String resourceId, JSONArray conditions) {
        if (!super.getConnected()) {
            return null;
        }
        String uri = String.format("/providers/%s/services/%s/resources/%s/SUBSCRIBE", serviceProviderId, serviceId, resourceId);
        JSONArray parametersArray = new JSONArray();
        JSONObject object = new JSONObject();
        object.put("name", "conditions");
        object.put("type", "array");
        object.put("value", conditions);
        parametersArray.put(object);
        String response = null;
		try {
			response = this.outgoingRequest(new JSONObject().put("uri", uri).put("pkey", publicKey).put("parameters", parametersArray));
		} catch (JSONException e) {
			mediator.error(e);
		}
        JSONObject result = null;
        if (response != null) {
            mediator.debug(response.toString());
            try {
                result = new JSONObject(response);
            } catch (JSONException | NullPointerException e) {
                mediator.error(e);
            }
        }
        return result;
    }
}