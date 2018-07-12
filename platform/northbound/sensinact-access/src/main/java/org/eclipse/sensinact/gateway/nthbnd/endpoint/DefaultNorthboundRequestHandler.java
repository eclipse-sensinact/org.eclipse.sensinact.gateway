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
package org.eclipse.sensinact.gateway.nthbnd.endpoint;

import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.core.DataResource;
import org.eclipse.sensinact.gateway.core.FilteringDefinition;
import org.eclipse.sensinact.gateway.core.message.SnaFilter;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.method.Parameter;
import org.eclipse.sensinact.gateway.util.CastUtils;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
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
    public static String RAW_QUERY_PARAMETER = "#RAW#";
    public static final String FILTER = "([^\\/:]+):";
    public static final String ROOT = "\\/((" + FILTER + ")*)sensinact";
    public static final String ELEMENT_SCHEME = "\\/([^\\/]+)";
    public static final String PROVIDERS_SCHEME = ROOT + "\\/providers";
    public static final String PROVIDER_SCHEME = PROVIDERS_SCHEME + ELEMENT_SCHEME;
    public static final String SIMPLIFIED_PROVIDER_SCHEME = ROOT + "\\/(([^p]|p[^r]|pr[^o]|pro[^v]|prov[^i]|provi[^d]|provid[^e]|provide[^r]|provider[^s]|providers[^\\/])[^\\/]*)";
    public static final String SERVICES_SCHEME = PROVIDER_SCHEME + "\\/services";
    public static final String SERVICE_SCHEME = SERVICES_SCHEME + ELEMENT_SCHEME;
    public static final String SIMPLIFIED_SERVICE_SCHEME = SIMPLIFIED_PROVIDER_SCHEME + ELEMENT_SCHEME;
    public static final String RESOURCES_SCHEME = SERVICE_SCHEME + "\\/resources";
    public static final String RESOURCE_SCHEME = RESOURCES_SCHEME + ELEMENT_SCHEME;
    public static final String SIMPLIFIED_RESOURCE_SCHEME = SIMPLIFIED_SERVICE_SCHEME + ELEMENT_SCHEME;

    //**************************************************************************
    //**************************************************************************
    private static final String METHOD_SCHEME = "\\/(GET|SET|ACT|SUBSCRIBE|UNSUBSCRIBE)";

    public static final String GENERIC_METHOD_SCHEME = ROOT + "(" + ELEMENT_SCHEME + ")*" + METHOD_SCHEME;
    public static final String ROOT_PROPAGATED_METHOD_SCHEME = ROOT + METHOD_SCHEME;

    public static final String PROVIDERS_PROPAGATED_METHOD_SCHEME = PROVIDERS_SCHEME + METHOD_SCHEME;
    public static final String PROVIDER_PROPAGATED_METHOD_SCHEME = PROVIDER_SCHEME + METHOD_SCHEME;

    public static final String SIMPLIFIED_PROVIDER_PROPAGATED_METHOD_SCHEME = SIMPLIFIED_PROVIDER_SCHEME + METHOD_SCHEME;
    public static final String SERVICE_PROPAGATED_METHOD_SCHEME = SERVICE_SCHEME + METHOD_SCHEME;
    public static final String SIMPLIFIED_SERVICE_PROPAGATED_METHOD_SCHEME = SIMPLIFIED_SERVICE_SCHEME + METHOD_SCHEME;
    public static final String RESOURCE_PROPAGATED_METHOD_SCHEME = RESOURCE_SCHEME + METHOD_SCHEME;
    public static final String SIMPLIFIED_RESOURCE_PROPAGATED_METHOD_SCHEME = SIMPLIFIED_RESOURCE_SCHEME + METHOD_SCHEME;

    //**************************************************************************
    //**************************************************************************
    private static final Pattern ROOT_PATTERN = Pattern.compile(ROOT);
    private static final Pattern FILTER_PATTERN = Pattern.compile(FILTER);

    private static final Pattern GENERIC_METHOD_SCHEME_PATTERN = Pattern.compile(GENERIC_METHOD_SCHEME);

    private static final Pattern PROVIDERS_PATTERN = Pattern.compile(PROVIDERS_SCHEME);
    private static final Pattern PROVIDER_PATTERN = Pattern.compile(PROVIDER_SCHEME);

    private static final Pattern SIMPLIFIED_PROVIDER_PATTERN = Pattern.compile(SIMPLIFIED_PROVIDER_SCHEME);

    private static final Pattern SERVICES_PATTERN = Pattern.compile(SERVICES_SCHEME);

    private static final Pattern SERVICE_PATTERN = Pattern.compile(SERVICE_SCHEME);
    private static final Pattern SIMPLIFIED_SERVICE_PATTERN = Pattern.compile(SIMPLIFIED_SERVICE_SCHEME);

    private static final Pattern RESOURCES_PATTERN = Pattern.compile(RESOURCES_SCHEME);

    private static final Pattern RESOURCE_PATTERN = Pattern.compile(RESOURCE_SCHEME);
    private static final Pattern SIMPLIFIED_RESOURCE_PATTERN = Pattern.compile(SIMPLIFIED_RESOURCE_SCHEME);

    private static final Pattern RESOURCE_PROPAGATED_METHOD_SCHEME_PATTERN = Pattern.compile(RESOURCE_PROPAGATED_METHOD_SCHEME);

    private static final Pattern SIMPLIFIED_RESOURCE_PROPAGATED_METHOD_SCHEME_PATTERN = Pattern.compile(SIMPLIFIED_RESOURCE_PROPAGATED_METHOD_SCHEME);

    private static final Pattern ROOT_PROPAGATED_METHOD_SCHEME_PATTERN = Pattern.compile(ROOT_PROPAGATED_METHOD_SCHEME);
    private static final Pattern PROVIDERS_PROPAGATED_METHOD_SCHEME_PATTERN = Pattern.compile(PROVIDERS_PROPAGATED_METHOD_SCHEME);

    private static final Pattern PROVIDER_PROPAGATED_METHOD_SCHEME_PATTERN = Pattern.compile(PROVIDER_PROPAGATED_METHOD_SCHEME);

    private static final Pattern SIMPLIFIED_PROVIDER_PROPAGATED_METHOD_SCHEME_PATTERN = Pattern.compile(SIMPLIFIED_PROVIDER_PROPAGATED_METHOD_SCHEME);
    private static final Pattern SERVICE_PROPAGATED_METHOD_SCHEME_PATTERN = Pattern.compile(SERVICE_PROPAGATED_METHOD_SCHEME);
    private static final Pattern SIMPLIFIED_SERVICE_PROPAGATED_METHOD_SCHEME_PATTERN = Pattern.compile(SIMPLIFIED_SERVICE_PROPAGATED_METHOD_SCHEME);

    //********************************************************************//
    //						INSTANCE DECLARATIONS						  //
    //********************************************************************//

    protected NorthboundMediator mediator = null;

    private String serviceProvider = null;
    private String service = null;
    private String resource = null;
    private String attribute = null;

    private boolean isElementList = false;
    private boolean multi = false;
    protected String rid;
    protected String method = null;

    private String filtered = null;

    private Map<String, List<String>> query;
    private NorthboundRequestWrapper request;
    private NorthboundResponseBuildError buildError;

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequestHandler#init(org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundAccessWrapper)
     */
    @Override
    public void init(NorthboundRequestWrapper request) throws IOException {
        this.mediator = request.getMediator();
        if (this.mediator == null) {
            throw new IOException("Unable to process the request");
        }
        this.request = request;
        this.buildError = null;
        this.query = request.getQueryMap();
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequestHandler#getBuildError()
     */
    @Override
    public NorthboundResponseBuildError getBuildError() {
        return this.buildError;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequestHandler#processRequestURI()
     */
    @Override
    public boolean processRequestURI() {
        String path = null;
        String requestURI = request.getRequestURI();
        try {
            path = UriUtils.formatUri(URLDecoder.decode(requestURI, "UTF-8"));

        } catch (UnsupportedEncodingException e) {
            mediator.error(e.getMessage(), e);
            return false;
        }
        this.serviceProvider = null;
        this.service = null;
        this.resource = null;
        this.attribute = null;
        this.method = null;
        this.multi = false;
        this.filtered = null;
        Matcher matcher = GENERIC_METHOD_SCHEME_PATTERN.matcher(path);
        if (matcher.matches()) {
            matcher = ROOT_PROPAGATED_METHOD_SCHEME_PATTERN.matcher(path);
            if (matcher.matches()) {
                this.filtered = matcher.group(1);
                this.method = matcher.group(4);
                this.multi = true;
                return true;
            }
            matcher = PROVIDERS_PROPAGATED_METHOD_SCHEME_PATTERN.matcher(path);
            if (matcher.matches()) {
                this.filtered = matcher.group(1);
                this.method = matcher.group(4);
                this.multi = true;
                return true;
            }
            matcher = PROVIDER_PROPAGATED_METHOD_SCHEME_PATTERN.matcher(path);
            if (matcher.matches()) {
                this.filtered = matcher.group(1);
                this.serviceProvider = matcher.group(4);
                this.method = matcher.group(5);
                this.multi = true;
                return true;
            }
            matcher = SIMPLIFIED_PROVIDER_PROPAGATED_METHOD_SCHEME_PATTERN.matcher(path);
            if (matcher.matches()) {
                this.filtered = matcher.group(1);
                this.serviceProvider = matcher.group(4);
                this.method = matcher.group(6);
                this.multi = true;
                return true;
            }
            matcher = SERVICE_PROPAGATED_METHOD_SCHEME_PATTERN.matcher(path);
            if (matcher.matches()) {
                this.filtered = matcher.group(1);
                this.serviceProvider = matcher.group(4);
                this.service = matcher.group(6);
                this.method = matcher.group(7);
                this.multi = true;
                return true;
            }
            matcher = SIMPLIFIED_SERVICE_PROPAGATED_METHOD_SCHEME_PATTERN.matcher(path);
            if (matcher.matches()) {
                this.filtered = matcher.group(1);
                this.serviceProvider = matcher.group(4);
                this.service = matcher.group(6);
                this.method = matcher.group(7);
                this.multi = true;
                return true;
            }
            matcher = RESOURCE_PROPAGATED_METHOD_SCHEME_PATTERN.matcher(path);
            if (matcher.matches()) {
                this.filtered = matcher.group(1);
                this.serviceProvider = matcher.group(4);
                this.service = matcher.group(5);
                this.resource = matcher.group(6);
                this.method = matcher.group(7);
                this.multi = false;
                return true;
            }
            matcher = SIMPLIFIED_RESOURCE_PROPAGATED_METHOD_SCHEME_PATTERN.matcher(path);
            if (matcher.matches()) {
                this.filtered = matcher.group(1);
                this.serviceProvider = matcher.group(4);
                this.service = matcher.group(6);
                this.resource = matcher.group(7);
                this.method = matcher.group(8);
                this.multi = false;
                return true;
            }
        }

        matcher = RESOURCE_PATTERN.matcher(path);
        if (matcher.matches()) {
            this.method = AccessMethod.DESCRIBE;
            this.filtered = matcher.group(1);
            this.serviceProvider = matcher.group(4);
            this.service = matcher.group(5);
            this.resource = matcher.group(6);
            this.multi = false;
            return true;
        }
        matcher = SIMPLIFIED_RESOURCE_PATTERN.matcher(path);
        if (matcher.matches()) {
            this.method = AccessMethod.DESCRIBE;
            this.filtered = matcher.group(1);
            this.serviceProvider = matcher.group(4);
            this.service = matcher.group(6);
            this.resource = matcher.group(7);
            this.multi = false;
            return true;
        }
        matcher = RESOURCES_PATTERN.matcher(path);
        if (matcher.matches()) {
            this.method = AccessMethod.DESCRIBE;
            this.filtered = matcher.group(1);
            this.serviceProvider = matcher.group(4);
            this.service = matcher.group(5);
            this.isElementList = true;
            this.multi = true;
            return true;
        }
        matcher = SERVICE_PATTERN.matcher(path);
        if (matcher.matches()) {
            this.method = AccessMethod.DESCRIBE;
            this.filtered = matcher.group(1);
            this.serviceProvider = matcher.group(4);
            this.service = matcher.group(5);
            this.multi = true;
            return true;
        }
        matcher = SIMPLIFIED_SERVICE_PATTERN.matcher(path);
        if (matcher.matches()) {
            this.method = AccessMethod.DESCRIBE;
            this.filtered = matcher.group(1);
            this.serviceProvider = matcher.group(4);
            this.service = matcher.group(6);
            this.multi = true;
            return true;
        }
        matcher = SERVICES_PATTERN.matcher(path);
        if (matcher.matches()) {
            this.method = AccessMethod.DESCRIBE;
            this.filtered = matcher.group(1);
            this.serviceProvider = matcher.group(4);
            this.isElementList = true;
            this.multi = true;
            return true;
        }
        matcher = PROVIDER_PATTERN.matcher(path);
        if (matcher.matches()) {
            this.method = AccessMethod.DESCRIBE;
            this.filtered = matcher.group(1);
            this.serviceProvider = matcher.group(4);
            this.multi = true;
            return true;
        }
        matcher = SIMPLIFIED_PROVIDER_PATTERN.matcher(path);
        if (matcher.matches()) {
            this.method = AccessMethod.DESCRIBE;
            this.filtered = matcher.group(1);
            this.serviceProvider = matcher.group(4);
            this.multi = true;
            return true;
        }
        matcher = PROVIDERS_PATTERN.matcher(path);
        if (matcher.matches()) {
            this.method = AccessMethod.DESCRIBE;
            this.filtered = matcher.group(1);
            this.isElementList = true;
            this.multi = true;
            return true;
        }
        matcher = ROOT_PATTERN.matcher(path);
        if (matcher.matches()) {
            this.method = "ALL";
            this.filtered = matcher.group(1);
            this.multi = true;
            return true;
        }
        return false;
    }

    /**
     * @return
     * @throws IOException
     * @throws JSONException
     */
    private Parameter[] processParameters() throws IOException, JSONException {
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
                    mediator.debug("No JSON formated content in %s", content);
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
                mediator.error(e);
                continue;
            }
            if ("attributeName".equals(parameter.getName()) && String.class == parameter.getType()) {
                this.attribute = (String) parameter.getValue();
                continue;
            }
            parametersList.add(parameter);
        }
        Iterator<Map.Entry<String, List<String>>> iterator = this.query.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, List<String>> entry = iterator.next();
            Parameter parameter = null;
            try {
                parameter = new Parameter(mediator, entry.getKey(), entry.getValue().size() > 1 ? JSONArray.class : String.class, entry.getValue().size() == 0 ? "true" : (entry.getValue().size() == 1 ? entry.getValue().get(0) : new JSONArray(entry.getValue())));
            } catch (InvalidValueException e) {
                throw new JSONException(e);
            }
            if ("attributeName".equals(parameter.getName()) && String.class == parameter.getType()) {
                this.attribute = (String) parameter.getValue();
                continue;
            }
            parametersList.add(parameter);
        }
        return parametersList.toArray(new Parameter[0]);
    }

    /**
     * @param builder
     */
    private void processAttribute(NorthboundRequestBuilder builder) {
        String attribute = this.attribute;
        if (attribute == null) {
            List<String> list = this.query.get("attributeName");
            if (list != null && !list.isEmpty()) {
                attribute = list.get(0);
            }
        }
        if (attribute != null) {
            builder.withAttribute(attribute);

        } else {
            builder.withAttribute(DataResource.VALUE);
        }
    }

    /**
     * @param builder
     * @param parameters
     * @return
     * @throws IOException
     */
    private void processFilters(NorthboundRequestBuilder builder, Parameter[] parameters) throws IOException {
        if (filtered == null || filtered.length() == 0) {
            return;
        }
        LinkedList<String> engines = new LinkedList<String>();
        Matcher matcher = FILTER_PATTERN.matcher(this.filtered);
        while (matcher.find()) {
            engines.addFirst(matcher.group(1));
        }
        if (engines.isEmpty()) {
            return;
        }
        builder.withFilter(engines.size());

        String filter = null;
        boolean hidden = false;

        int index = 0;
        int length = parameters == null ? 0 : parameters.length;
        for (; index < length; index++) {
            Parameter parameter = parameters[index];
            String name = parameter.getName();
            int filterIndex = -1;
            if ((filterIndex = engines.indexOf(name)) > -1) {
                filter = CastUtils.castPrimitive(String.class, parameter.getValue());

                builder.withFilter(new FilteringDefinition(name, filter), filterIndex);
            }
            if ("hideFilter".equals(name)) {
                hidden = CastUtils.castPrimitive(boolean.class, parameter.getValue());
            }
        }
        builder.withHiddenFilter(hidden);
    }

    /**
     * @return
     */
    public NorthboundRequestBuilder handle() throws IOException {
        Parameter[] parameters = null;
        try {
            parameters = processParameters();

        } catch (IOException e) {
            mediator.error(e);
            this.buildError = new NorthboundResponseBuildError(500, "Error processing the request content");
            return null;

        } catch (JSONException e) {
            mediator.error(e);
            String content = this.request.getContent();
            if (content != null && !content.isEmpty()) {
                this.buildError = new NorthboundResponseBuildError(400, "Invalid parameter(s) format");
                return null;
            }
        }
        return handle(parameters);
    }

    /**
     * @param parameters
     * @return
     * @throws IOException
     */
    private NorthboundRequestBuilder handle(Parameter[] parameters) throws IOException {
        NorthboundRequestBuilder builder = new NorthboundRequestBuilder(mediator);

        processFilters(builder, parameters);

        builder.withMethod(this.method).withServiceProvider(this.serviceProvider).withService(this.service).withResource(this.resource);

        if (!this.multi && !this.method.equals(AccessMethod.ACT) && !this.method.equals(AccessMethod.DESCRIBE)) {
            this.processAttribute(builder);
        }
        this.rid = request.getRequestID(parameters);
        builder.withRequestId(this.rid);

        switch (method) {
            case "DESCRIBE":
                builder.isElementsList(isElementList);
                break;
            case "ACT":
                int index = 0;
                int length = parameters == null ? 0 : parameters.length;

                Object[] arguments = length == 0 ? null : new Object[length];
                for (; index < length; index++) {
                    arguments[index] = parameters[index].getValue();
                }
                builder.withArgument(arguments);
                break;
            case "UNSUBSCRIBE":
                if (parameters == null || parameters.length != 1 || parameters[0] == null) {
                    this.buildError = new NorthboundResponseBuildError(400, "A Parameter was expected");
                    return null;
                }
                if (parameters[0].getType() != String.class) {
                    this.buildError = new NorthboundResponseBuildError(400, "Invalid parameter format");
                    return null;
                }
                builder.withArgument(parameters[0].getValue());
                break;
            case "SET":
                if (parameters == null || parameters.length != 1 || parameters[0] == null) {
                    this.buildError = new NorthboundResponseBuildError(400, "A Parameter was expected");
                    return null;
                }
                builder.withArgument(parameters[0].getValue());
                break;
            case "SUBSCRIBE":
                NorthboundRecipient recipient = this.request.createRecipient(parameters);
                if (recipient == null) {
                    this.buildError = new NorthboundResponseBuildError(400, "Unable to create the appropriate recipient");
                    return null;
                }
                index = 0;
                length = parameters == null ? 0 : parameters.length;

                String sender = null;
                boolean isPattern = false;
                boolean isComplement = false;
                SnaMessage.Type[] types = null;
                JSONArray conditions = null;

                for (; index < length; index++) {
                    Parameter parameter = parameters[index];
                    String name = parameter.getName();

                    switch (name) {
                        case "conditions":
                            conditions = CastUtils.cast(mediator.getClassLoader(), JSONArray.class, parameter.getValue());
                            break;
                        case "sender":
                            sender = CastUtils.cast(mediator.getClassLoader(), String.class, parameter.getValue());
                            break;
                        case "pattern":
                            isPattern = CastUtils.cast(mediator.getClassLoader(), boolean.class, parameter.getValue());
                            break;
                        case "complement":
                            isComplement = CastUtils.cast(mediator.getClassLoader(), boolean.class, parameter.getValue());
                            break;
                        case "types":
                            types = CastUtils.castArray(mediator.getClassLoader(), SnaMessage.Type[].class, parameter.getValue());
                        default:
                            ;
                    }
                }
                if (sender == null) {
                    sender = "/[^/]+(/[^/]+)*";
                }
                if (types == null) {
                    types = SnaMessage.Type.values();
                }
                if (conditions == null) {
                    conditions = new JSONArray();
                }
                Object argument = null;

                if (this.resource == null) {
                    SnaFilter snaFilter = new SnaFilter(mediator, sender, isPattern, isComplement, conditions);
                    snaFilter.addHandledType(types);
                    argument = snaFilter;

                } else {
                    argument = conditions;
                }
                builder.withArgument(new Object[]{recipient, argument});
                break;
            default:
                break;
        }
        return builder;
    }
}
