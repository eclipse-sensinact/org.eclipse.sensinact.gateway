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
package org.eclipse.sensinact.gateway.nthbnd.endpoint;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.sensinact.gateway.common.execution.ErrorHandler;
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.core.filtering.Filtering;
import org.eclipse.sensinact.gateway.core.filtering.FilteringDefinition;
import org.eclipse.sensinact.gateway.core.message.SnaFilter;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.method.Parameter;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequestWrapper.QueryKey;
import org.eclipse.sensinact.gateway.util.CastUtils;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default {@link NorthboundRequestHandler} implementation
 */
public class DefaultNorthboundRequestHandler implements NorthboundRequestHandler {
	
	//********************************************************************//
    //						NESTED DECLARATIONS			  			      //
    //********************************************************************//

    //********************************************************************//
    //						ABSTRACT DECLARATIONS						  //
    //********************************************************************//

    //********************************************************************//
    //						STATIC DECLARATIONS							  //
    //********************************************************************//
	
	private static final Logger LOG = LoggerFactory.getLogger(DefaultNorthboundRequestHandler.class);
    public static String RAW_QUERY_PARAMETER = "#RAW#";

    //********************************************************************//
    //						INSTANCE DECLARATIONS						  //
    //********************************************************************//

    protected NorthboundMediator mediator = null;

    private String serviceProvider = null;
    private String service = null;
    private String resource = null;
    private String attribute = null;

    private boolean isElementsList = false;
    private boolean multi = false;
    protected String rid;
    protected String method = null;

    private Map<NorthboundRequestWrapper.QueryKey, List<String>> query;
    private NorthboundRequestWrapper request;
    private NorthboundResponseBuildError buildError;
    private Set<String> methods;

    @Override
    public void init(NorthboundRequestWrapper request, Set<AccessMethod.Type> methods) throws IOException {
        this.mediator = request.getMediator();
        if (this.mediator == null) {
            throw new IOException("Unable to process the request");
        }
        this.request = request;
        this.buildError = null;
        this.query = request.getQueryMap();
        this.methods = methods.stream().collect(HashSet::new, (l, r)->{l.add(r.name());}, Set::addAll);
    }
    
    /**
     * Initializes this handler using the request wrapper passed as
     * parameter to set the appropriate fields
     *
     * @param request the request wrapper allowing to initialize this handler
     * 
     * @throws IOException if an error occurred while initializing
     */
    public void init(NorthboundRequestWrapper request) throws IOException {
        this.init(request, new HashSet<>(Arrays.<AccessMethod.Type>asList(AccessMethod.Type.values())));
    }

    @Override
    public NorthboundResponseBuildError getBuildError() {
        return this.buildError;
    }

    @Override
    public boolean processRequestURI() {
        String path = null;
        String requestURI = request.getRequestURI();
        try {
            path = UriUtils.formatUri(URLDecoder.decode(requestURI, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            LOG.error(e.getMessage(), e);
            return false;
        }
        this.serviceProvider = null;
        this.service = null;
        this.resource = null;
        this.attribute = null;
        this.method = null;
        this.multi = false;
        
        String[] pathElements = UriUtils.getUriElements(path);
        int lastIndex = pathElements.length-1;
        
        if(this.methods.contains(pathElements[pathElements.length-1])){
        	this.method = pathElements[pathElements.length-1];
        	lastIndex-=1;
        }        
        switch(pathElements[lastIndex]){
        	case "resources":
	        	this.service = pathElements[lastIndex-1];
	        	lastIndex-=2;
	        case "services":
	        	this.serviceProvider = pathElements[lastIndex-1];
	        case "providers":
                this.multi = true;
                this.isElementsList = true;	
                break;
	        case "sensinact":
	        	if(this.method==null) 
	        		this.method = "ALL";	        	
	        	this.multi = true;
                break;
            default:
            	break;
        }        
        if(!this.multi) {            
            switch(pathElements[lastIndex-1]){
    	        case "resources":
    	        	this.resource = pathElements[lastIndex];
    	        	lastIndex-=2;
    	        case "services":
    	        	this.service = pathElements[lastIndex];
    	        	lastIndex-=2;
    	        case "providers":
    	        	this.serviceProvider = pathElements[lastIndex];
                    break;
                default:
                	switch(lastIndex) {
                		case 3:
                			resource = pathElements[lastIndex];
                			lastIndex-=1;
                		case 2:
                			service = pathElements[lastIndex];
                			lastIndex-=1;
                		case 1:
                			serviceProvider = pathElements[lastIndex];
                		default:
                			break;
                	}
                	break;
            }  
        }
        this.method = this.method==null?AccessMethod.DESCRIBE:this.method;       
        return true;
    }

    private List<Parameter> processParameters() throws IOException, JSONException {
        String content = this.request.getContent();
        JSONArray parameters = null;
        if (content == null) {
            parameters = new JSONArray();
        } else {
            try {
                JSONObject jsonObject = new JSONObject(content);
                parameters = jsonObject.optJSONArray("parameters");

            } catch (JSONException e) {
                try {
                    parameters = new JSONArray(content);
                } catch (JSONException je) {
                    LOG.debug("No JSON formated content in %s", content);
                }
            }
        }
        int index = 0;
        int length = parameters == null ? 0 : parameters.length();

        List<Parameter> parametersList = new ArrayList<Parameter>();
        for (; index < length; index++) {
            Parameter parameter = null;
            try {
                parameter = new Parameter(mediator, parameters.optJSONObject(index));
            } catch (InvalidValueException e) {
                LOG.error(e.getMessage(), e);
                continue;
            }
            if ("attributeName".equals(parameter.getName()) && String.class == parameter.getType()) {
                this.attribute = (String) parameter.getValue();
                continue;
            }
            parametersList.add(parameter);
        }
        Iterator<Map.Entry<QueryKey, List<String>>> iterator = this.query.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<QueryKey, List<String>> entry = iterator.next();
            Parameter parameter = null;
            try {
                parameter = new Parameter(mediator, 
                	entry.getKey().name, 
                	entry.getValue().size() > 1 ? JSONArray.class : String.class, 
                	entry.getValue().size() == 0 ? "true" : (entry.getValue().size() == 1 ? entry.getValue().get(0) : new JSONArray(entry.getValue()))
                );
            } catch (InvalidValueException e) {
                throw new JSONException(e);
            }
            if ("attributeName".equals(parameter.getName()) && String.class == parameter.getType()) {
                this.attribute = (String) parameter.getValue();
                continue;
            }
            parametersList.add(parameter);
        }
        return parametersList;
    }

    private void processAttribute(NorthboundRequestBuilder builder) {
        if (this.attribute != null)
            builder.withAttribute(this.attribute);
    }

    private void processFilters(NorthboundRequestBuilder builder, List<Parameter> parameters) throws IOException {
        List<FilteringDefinition> defs = new ArrayList<>();
        String filter = null;
        boolean hidden = false;

        Iterator<Parameter> it = parameters.iterator();
        while(it.hasNext()) {
        	Parameter parameter = it.next();
            String name = parameter.getName();
            if ("hideFilter".equals(name)) {
                hidden = CastUtils.castPrimitive(boolean.class, parameter.getValue());
                it.remove();
                continue;
            }
            int rank = FilteringDefinition.UNRANKED;
            int ind = name.lastIndexOf(".");
            if(ind > 0) {
            	try {
            		rank = Integer.parseInt(name.substring(ind+1));
            		name = name.substring(0,ind);
            	} catch(NumberFormatException e){
            		rank = FilteringDefinition.UNRANKED;
            	}
            }
            try {
    			Collection<ServiceReference<Filtering>> references = mediator.getContext(
    				).getServiceReferences(Filtering.class, String.format("(%s=%s)",Filtering.TYPE, name));
    			
    			if (references != null && references.size() == 1) {
    				filter = CastUtils.castPrimitive(String.class, parameter.getValue());
    				int i=0;
    				for(;i<defs.size();i++){
    					if(rank > defs.get(i).rank)
    						continue;
						defs.add(i,new FilteringDefinition(name, filter,rank));
						break;
    				}
    				if(i == defs.size()) 
    					defs.add(new FilteringDefinition(name, filter,rank));
    				it.remove();
    			}
    		} catch (InvalidSyntaxException e) {
    			continue;
    		}
        }
        if(defs.size()==0) 
        	return;
        builder.withFilter(defs.size());
        final AtomicInteger n = new AtomicInteger(-1);
        defs.stream().forEach(d -> {builder.withFilter(d, n.incrementAndGet());});
        builder.withHiddenFilter(hidden);
    }

    public NorthboundRequestBuilder handle() throws IOException {
        List<Parameter> parameters = null;
        try {
            parameters = processParameters();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            this.buildError = new NorthboundResponseBuildError(500, "Error processing the request content");
            return null;
        } catch (JSONException e) {
            LOG.error(e.getMessage(), e);
            String content = this.request.getContent();
            if (content != null && !content.isEmpty()) {
                this.buildError = new NorthboundResponseBuildError(400, "Invalid parameter(s) format");
                return null;
            }
        }
        return handle(parameters);
    }

    private NorthboundRequestBuilder handle(List<Parameter> parameters) throws IOException {
        NorthboundRequestBuilder builder = new NorthboundRequestBuilder(mediator);
        processFilters(builder, parameters);
        builder.withMethod(this.method
        	).withServiceProvider(this.serviceProvider
        	).withService(this.service
        	).withResource(this.resource);

        if (!this.multi && !this.method.equals(AccessMethod.ACT) && !this.method.equals(AccessMethod.DESCRIBE))
            this.processAttribute(builder);
            
        this.rid = request.getRequestId();
        if(this.rid == null) {
	        String requestIdName = request.getRequestIdProperty();
            Iterator<Parameter> it = parameters.iterator();
            while(it.hasNext()) {
            	Parameter parameter = it.next();
                String name = parameter.getName();
                if(name.equals(requestIdName)) {
                	this.rid = String.valueOf(parameter.getValue());
                	it.remove();
                	break;
                }
            }
        }
        builder.withRequestId(this.rid);
        switch (method) {
            case "DESCRIBE":
                builder.isElementsList(isElementsList);
                break;
            case "ACT":
            case "GET":
            	int index = 0;
            	int length = parameters.size();
                for (; index < length; index++) 
                    builder.withArgument(new Argument(parameters.get(index).getType(), parameters.get(index).getValue()));
                break;
            case "UNSUBSCRIBE":
                if (parameters == null || parameters.isEmpty()) {
                    this.buildError = new NorthboundResponseBuildError(400, "A Parameter was expected");
                    return null;
                }
                if (parameters.get(0) == null || parameters.get(0).getType() != String.class) {
                    this.buildError = new NorthboundResponseBuildError(400, "Invalid parameter format");
                    return null;
                }
                builder.withArgument(new Argument(String.class, parameters.get(0).getValue()));
                index = 1;
                length = parameters.size();
                for (; index < length; index++) 
                    builder.withArgument(new Argument(parameters.get(index).getType(), parameters.get(index).getValue()));
                break;
            case "SET":
                if (parameters == null || parameters.get(0) == null) {
                    this.buildError = new NorthboundResponseBuildError(400, "A Parameter was expected");
                    return null;
                }
                builder.withArgument(new Argument(Object.class, parameters.get(0).getValue()));
                index = 1;
                length = parameters.size();
                for (; index < length; index++) 
                    builder.withArgument(new Argument(parameters.get(index).getType(), parameters.get(index).getValue()));  
                break;
            case "SUBSCRIBE":
                NorthboundRecipient recipient = this.request.createRecipient(parameters);
                if (recipient == null) {
                    this.buildError = new NorthboundResponseBuildError(400, "Unable to create the appropriate recipient");
                    return null;
                }                
                String sender = null;
                boolean isPattern = false;
                boolean isComplement = false;
                String policy = String.valueOf(ErrorHandler.Policy.DEFAULT_POLICY);
                SnaMessage.Type[] types = null;
                JSONArray conditions = null;

                List<Parameter> extraParameters = new ArrayList<>();
                
                length = parameters == null ? 0 : parameters.size();                
                for (; length > 0;) {
                	boolean found = false;
                    Parameter parameter = parameters.remove(0);
                    String name = parameter.getName();
                    switch (name) {
	                    case "callback":
	                    	found = true;
	                        break;
	                    case "conditions":
	                        conditions = CastUtils.cast(JSONArray.class, parameter.getValue());
	                    	found = true;
	                        break;
                        case "sender":
                            sender = CastUtils.cast(String.class, parameter.getValue());
	                    	found = true;
                            break;
                        case "pattern":
                            isPattern = CastUtils.cast(boolean.class, parameter.getValue());
	                    	found = true;
                            break;
                        case "complement":
                            isComplement = CastUtils.cast(boolean.class, parameter.getValue());
	                    	found = true;
                            break;
                        case "types":
                            types = CastUtils.castArray(SnaMessage.Type[].class, parameter.getValue());
	                    	found = true;
                        case "policy":
                            policy = CastUtils.cast(String.class, parameter.getValue());
	                    	found = true;
                            break;
                        default:
                        	break;
                    }
                    if(!found)
                    	extraParameters.add(parameter);
                    length = parameters.size();
                }

                if (sender == null) {
                    sender = "(/[^/]+)+";
                    isPattern = true;
                }
                if (types == null) 
                    types = SnaMessage.Type.values();
                
                if (conditions == null) 
                    conditions = new JSONArray();
                
                builder.withArgument(new Argument(NorthboundRecipient.class, recipient));
                if (this.resource == null) {
                    SnaFilter snaFilter = new SnaFilter(mediator, sender, isPattern, isComplement, conditions);
                    snaFilter.addHandledType(types);       
                    builder.withArgument(new Argument(SnaFilter.class, snaFilter));
                } else {
                    builder.withArgument(new Argument(JSONArray.class, conditions));
                    builder.withArgument(new Argument(String.class, policy));
                }                
                index = 0;
                length = extraParameters == null ? 0 : extraParameters.size();
                for (; index < length; index++) 
                    builder.withArgument(new Argument(extraParameters.get(index).getType(), extraParameters.get(index).getValue()));
                break;
            default:
                break;
        }
        return builder;
    }
}
