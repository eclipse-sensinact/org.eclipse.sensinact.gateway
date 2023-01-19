/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.gateway.southbound.http.callback;

import static org.osgi.namespace.implementation.ImplementationNamespace.IMPLEMENTATION_NAMESPACE;
import static org.osgi.service.component.annotations.ReferenceCardinality.MULTIPLE;
import static org.osgi.service.component.annotations.ReferencePolicy.DYNAMIC;
import static org.osgi.service.servlet.runtime.HttpServiceRuntimeConstants.HTTP_SERVICE_ENDPOINT;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.sensinact.gateway.southbound.http.callback.api.HttpCallback;
import org.osgi.annotation.bundle.Capability;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.servlet.runtime.HttpServiceRuntime;
import org.osgi.service.servlet.whiteboard.propertytypes.HttpWhiteboardServletPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * HTTP device factory
 */
@SuppressWarnings("serial")
@HttpWhiteboardServletPattern("/southbound/callback/*")
@Capability(namespace = IMPLEMENTATION_NAMESPACE, name = "sensinact.http.callback.whiteboard", version = "0.0.1")
@Component(configurationPid = "sensinact.http.callback.whiteboard", configurationPolicy = ConfigurationPolicy.REQUIRE)
public class HttpCallbackWhiteboard extends HttpServlet implements Servlet {
    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(HttpCallbackWhiteboard.class);

    private static final String NOT_SET = "<NOT SET>";

    private final String baseURI;

    private final Map<String, HttpCallback> callbacks = new HashMap<>();

    private final Map<String, String> pathToURI = new HashMap<>();

    private final Map<Long, String> serviceToPath = new HashMap<>();

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    @interface Config {
        String base_uri() default NOT_SET;
    }

    @Activate
    public HttpCallbackWhiteboard(Config config, @Reference Map.Entry<Map<String, Object>, HttpServiceRuntime> props) {
        String prefix;
        if (NOT_SET.equals(config.base_uri())) {
            Map<String, Object> runtimeProps = props.getKey();
            Object o = runtimeProps.get(HTTP_SERVICE_ENDPOINT);
            if (o == null) {
                logger.error("No base.uri is set, and the HttpServiceRuntime does not declare an endpoint URI");
                throw new IllegalArgumentException("No base.uri is set and no runtime uri is available");
            } else if (o instanceof String[]) {
                String[] s = (String[]) o;
                if (s.length == 0) {
                    logger.error(
                            "No base.uri is set, and the HttpServiceRuntime does declares an empty endpoint URI array");
                    throw new IllegalArgumentException("No base.uri is set and no runtime uri is available");
                }
                o = s[0];
            } else if (o instanceof Collection) {
                Collection<?> c = (Collection<?>) o;
                if (c.isEmpty()) {
                    logger.error(
                            "No base.uri is set, and the HttpServiceRuntime does declares an empty endpoint URI list");
                    throw new IllegalArgumentException("No base.uri is set and no runtime uri is available");
                }
                o = c.iterator().next();
            }
            prefix = o.toString();
            if (logger.isInfoEnabled()) {
                logger.info("Using URI prefix {} from the HttpServiceRuntime", prefix);
            }
        } else {
            prefix = config.base_uri();
            if (logger.isInfoEnabled()) {
                logger.info("Using URI prefix {} from the base.uri configuration property", prefix);
            }
        }

        baseURI = prefix.endsWith("/") ? prefix + "southbound/callback/" : prefix + "/southbound/callback/";
    }

    @Reference(cardinality = MULTIPLE, policy = DYNAMIC)
    void addCallback(HttpCallback callback, Map<String, Object> props) throws InterruptedException {
        String path = UUID.randomUUID().toString();
        String uri = baseURI + path;
        Long serviceId = getServiceId(props);

        if (logger.isDebugEnabled()) {
            logger.debug("Adding callback for service {} with path {}", serviceId, path);
        }

        Lock wl = lock.writeLock();
        wl.lockInterruptibly();
        try {
            serviceToPath.put(serviceId, path);
            callbacks.put(path, callback);
            pathToURI.put(path, uri);
        } finally {
            wl.unlock();
        }

        try {
            callback.activate(uri);
        } catch (Exception e) {
            logger.warn("An error occurred activating callback service {}", serviceId, e);
            removeCallback(props);
        }
    }

    private Long getServiceId(Map<String, Object> props) {
        return (Long) props.get("service.id");
    }

    void modifiedCallback(HttpCallback callback, Map<String, Object> props) {
        // No op
    }

    void removeCallback(Map<String, Object> props) throws InterruptedException {
        Long serviceId = getServiceId(props);

        if (logger.isDebugEnabled()) {
            logger.debug("Removing callback for service {}", serviceId);
        }

        HttpCallback httpCallback = null;
        String uri = null;

        Lock wl = lock.writeLock();
        wl.lockInterruptibly();
        try {
            String path = serviceToPath.remove(serviceId);
            if (path != null) {
                httpCallback = callbacks.remove(path);
                uri = pathToURI.remove(path);
            }
        } finally {
            wl.unlock();
        }

        if (httpCallback != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Deactivating service {} for uri {}", serviceId, uri);
            }
            httpCallback.deactivate(uri);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getRequestURI().substring(req.getContextPath().length() + req.getServletPath().length() + 1);

        if (logger.isDebugEnabled()) {
            logger.debug("POST request received for URI {} - calculated path is {}", req.getRequestURI(), path);
        }

        HttpCallback httpCallback;
        String uri;

        Lock readLock = lock.readLock();
        try {
            if (readLock.tryLock(2, TimeUnit.SECONDS)) {
                try {
                    httpCallback = callbacks.get(path);
                    uri = pathToURI.get(path);
                } finally {
                    readLock.unlock();
                }
            } else {
                if (logger.isWarnEnabled()) {
                    logger.debug("Not able to acquire a read lock in 2 seconds");
                }
                resp.sendError(503);
                return;
            }
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.debug("Not able to check the callbacks", e);
            }
            resp.sendError(500);
            return;
        }

        if (httpCallback != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Processing callback for path {}", path);
            }

            Map<String, List<String>> headers = new HashMap<>();

            Enumeration<String> headerNames = req.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String header = headerNames.nextElement();
                headers.put(header, Collections.list(req.getHeaders(header)));
            }

            try {
                httpCallback.call(uri, headers, req.getReader());
            } catch (Exception e) {
                if (logger.isWarnEnabled()) {
                    logger.debug("An error occurred for a callback on path {}", path, e);
                }
                resp.sendError(500);
                return;
            }
            resp.setStatus(204);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("No callback present for path {}", path);
            }
            resp.sendError(404);
            return;
        }
    }
}
