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
package org.eclipse.sensinact.gateway.sthbnd.http.smpl;

import org.eclipse.sensinact.gateway.generic.Task.CommandType;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.HttpChildTaskConfiguration;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.HttpTaskConfiguration;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.KeyValuePair;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpTask;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SimpleTaskConfigurator implements HttpTaskBuilder {
    public static final String VALUE_PATTERN = "\\$\\(([^\\)\\@]+)\\)";
    public static final String CONTEXT_PATTERN = "\\@context\\[([^\\]\\$]+)\\]";

    private static KeyValuePair[] join(KeyValuePair[] pa, KeyValuePair[] pb) {
        int paLength = pa == null ? 0 : pa.length;
        int pbLength = pb == null ? 0 : pb.length;
        int length = paLength + pbLength;

        KeyValuePair[] joined = new KeyValuePair[length];
        int index = 0;

        if (paLength > 0) {
            for (; index < paLength; index++) {
                joined[index] = pa[index];
            }
        }
        if (pbLength > 0) {
            for (; index < length; index++) {
                joined[index] = pb[index - paLength];
            }
        }
        return joined;
    }

    protected SimpleHttpProtocolStackEndpoint endpoint;

    private Pattern valuePattern;
    private Pattern contextPattern;

    private String acceptType = null;
    private String contentType = null;
    private String httpMethod = null;

    private String scheme = null;
    private String host = null;
    private String port = null;
    private String path = null;
    private String profile = null;
    private boolean direct = false;
    private CommandType command = null;

    private Map<String, List<String>> query = null;
    private Map<String, List<String>> headers = null;

    private HttpTaskConfigurator contentBuilder = null;
    private HttpTaskConfigurator urlBuilder = null;

    /**
     * @param mediator
     * @param profile
     * @param command
     * @param urlBuilder
     * @param annotation
     */
    public SimpleTaskConfigurator(SimpleHttpProtocolStackEndpoint endpoint, String profile, CommandType command, HttpTaskConfigurator urlBuilder, HttpTaskConfiguration annotation) {
        this(endpoint, profile, command, urlBuilder, annotation, null);
    }

    /**
     * @param mediator
     * @param profile
     * @param command
     * @param urlBuilder
     * @param parent
     * @param annotation
     */
    public SimpleTaskConfigurator(SimpleHttpProtocolStackEndpoint endpoint, String profile, CommandType command, HttpTaskConfigurator urlBuilder, HttpTaskConfiguration parent, HttpChildTaskConfiguration annotation) {
        this.endpoint = endpoint;

        this.valuePattern = Pattern.compile(VALUE_PATTERN);
        this.contextPattern = Pattern.compile(CONTEXT_PATTERN);
        this.acceptType = annotation != null && !HttpChildTaskConfiguration.DEFAULT_ACCEPT_TYPE.equals(annotation.acceptType()) ? annotation.acceptType() : parent.acceptType();
        this.contentType = annotation != null && !HttpChildTaskConfiguration.DEFAULT_CONTENT_TYPE.equals(annotation.contentType()) ? annotation.contentType() : parent.contentType();

        this.httpMethod = annotation != null && !HttpChildTaskConfiguration.DEFAULT_HTTP_METHOD.equals(annotation.httpMethod()) ? annotation.httpMethod() : parent.httpMethod();
        this.scheme = annotation != null && !HttpChildTaskConfiguration.DEFAULT_SCHEME.equals(annotation.scheme()) ? annotation.scheme() : parent.scheme();
        this.host = annotation != null && !HttpChildTaskConfiguration.DEFAULT_HOST.equals(annotation.host()) ? annotation.host() : parent.host();

        this.port = annotation != null && !HttpChildTaskConfiguration.DEFAULT_PORT.equals(annotation.port()) ? annotation.port() : parent.port();

        this.path = annotation != null && !HttpChildTaskConfiguration.DEFAULT_PATH.equals(annotation.path()) ? annotation.path() : parent.path();
        this.profile = profile;
        this.urlBuilder = urlBuilder;
        this.command = command;
        this.direct = annotation != null ? annotation.direct() : parent.direct();

        this.query = new HashMap<String, List<String>>();

        KeyValuePair[] queryParameters = join(annotation == null ? null : annotation.query(), parent.query());

        int index = 0;
        int length = queryParameters == null ? 0 : queryParameters.length;

        for (; index < length; index++) {
            String key = queryParameters[index].key();
            String value = queryParameters[index].value();
            List<String> values = query.get(key);
            if (values == null) {
                values = new ArrayList<String>();
                query.put(key, values);
            }
            values.add(value);
        }
        this.headers = new HashMap<String, List<String>>();
        KeyValuePair[] headersParameters = join(annotation == null ? null : annotation.headers(), parent.headers());
        index = 0;
        length = headersParameters == null ? 0 : headersParameters.length;

        for (; index < length; index++) {
            String key = headersParameters[index].key();
            String value = headersParameters[index].value();
            List<String> values = headers.get(key);
            if (values == null) {
                values = new ArrayList<String>();
                headers.put(key, values);
            }
            values.add(value);
        }
        Class<? extends HttpTaskConfigurator> taskContentConfigurationType = annotation != null && HttpTaskConfigurator.class != annotation.content() ? annotation.content() : parent.content();

        if (taskContentConfigurationType == null || taskContentConfigurationType == HttpTaskConfigurator.class) {
            return;
        }
        try {
            this.contentBuilder = taskContentConfigurationType.newInstance();
        } catch (Exception e) {
            this.endpoint.getMediator().error(e);
        }
    }

    /**
     * @param key
     * @param value
     * @return
     */
    private String resolve(HttpTask<?, ?> key, String value) {
        String argument = value;
        String resolved = null;

        while (argument != null) {
            Matcher valueMatcher = this.valuePattern.matcher(argument);
            Matcher contextMatcher = this.contextPattern.matcher(argument);
            
            int valueIndex = valueMatcher.find()?valueMatcher.start(1):-1;
            int contextIndex = contextMatcher.find()?contextMatcher.start(1):-1;
            
            if (valueIndex > contextIndex) {            	
                String val = (String) this.endpoint.getMediator().getProperty(valueMatcher.group(1));
                StringBuilder builder = new StringBuilder().append(argument.substring(0, valueMatcher.start(1) - 2)).append(val).append(argument.substring(valueMatcher.end(1) + 1, argument.length()));
                argument = builder.toString();
            } else if (contextIndex > valueIndex) {            	
            	String val = this.endpoint.getMediator().resolve(key, contextMatcher.group(1));
                StringBuilder builder = new StringBuilder().append(argument.substring(0, contextMatcher.start(1) - 9)).append(val).append(argument.substring(contextMatcher.end(1) + 1, argument.length()));
                argument = builder.toString();
            } else {
                resolved = argument;
                break;
            }
        }
        return resolved;
    }

    /**
     * @inheritDoc
     * @see Executable#
     * execute(java.lang.Object)
     */
    public <T extends HttpTask<?, ?>> void configure(T task) throws Exception {
        String portStr = this.resolve(task, this.port);
        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            port = 80;
        }
        String uri = new URL(this.resolve(task, scheme), this.resolve(task, host), port, this.resolve(task, path)).toExternalForm();

        StringBuilder queryBuilder = new StringBuilder();

        Iterator<Map.Entry<String, List<String>>> iterator = query.entrySet().iterator();
        Map.Entry<String, List<String>> entry = null;
        int position = -1;

        for (; iterator.hasNext(); ) {
            entry = iterator.next();

            List<String> values = entry.getValue();
            if (values == null || values.isEmpty()) {
                continue;
            }
            queryBuilder.append(++position == 0 ? "?" : "");

            int index = 0;
            int length = values == null ? 0 : values.size();
            for (; index < length; index++) {
                queryBuilder.append((position > 0 || index > 0) ? "&" : "");
                queryBuilder.append(this.resolve(task, entry.getKey()));
                queryBuilder.append("=");
                queryBuilder.append(this.resolve(task, values.get(index)));
            }
        }
        String queryRequest = queryBuilder.toString();
        if (queryRequest.length() > 1) {
            uri = new StringBuilder().append(uri).append(queryRequest).toString();
        }
        task.setUri(uri);

        if (this.urlBuilder != null) {
            this.urlBuilder.configure(task);
        }
        task.setDirect(direct);
        iterator = headers.entrySet().iterator();
        entry = null;
        position = -1;

        for (; iterator.hasNext(); ) {
            entry = iterator.next();

            List<String> values = entry.getValue();
            if (values == null || values.isEmpty()) {
                continue;
            }
            int index = 0;
            int length = values == null ? 0 : values.size();
            for (; index < length; index++) {
                task.addHeader(this.resolve(task, entry.getKey()), this.resolve(task, values.get(index)));
            }
        }
        task.setAccept(this.resolve(task, acceptType));
        task.setContentType(this.resolve(task, contentType));
        task.setHttpMethod(this.resolve(task, httpMethod));

        if (contentBuilder != null) {
            contentBuilder.configure(task);
        }
    }

    /**
     * Returns the {@link CommandType} of the task to be
     * configured
     *
     * @return the {@link CommandType} of the task to be
     * configured
     */
    public CommandType handled() {
        return this.command;
    }
}