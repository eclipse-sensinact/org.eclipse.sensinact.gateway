/*********************************************************************
* Copyright (c) 2025 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.northbound.ws.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import org.eclipse.jetty.websocket.api.Frame;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketFrame;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.sensinact.core.notification.ClientDataListener;
import org.eclipse.sensinact.core.notification.ClientLifecycleListener;
import org.eclipse.sensinact.core.notification.LifecycleNotification;
import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.core.notification.ResourceNotification;
import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceValueFilter;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.northbound.query.api.AbstractQueryDTO;
import org.eclipse.sensinact.northbound.query.api.AbstractResultDTO;
import org.eclipse.sensinact.northbound.query.api.IQueryHandler;
import org.eclipse.sensinact.northbound.query.api.StatusException;
import org.eclipse.sensinact.northbound.query.dto.SensinactPath;
import org.eclipse.sensinact.northbound.query.dto.notification.AbstractResourceNotificationDTO;
import org.eclipse.sensinact.northbound.query.dto.notification.ErrorResultNotificationDTO;
import org.eclipse.sensinact.northbound.query.dto.notification.ResourceDataNotificationDTO;
import org.eclipse.sensinact.northbound.query.dto.notification.ResourceLifecycleNotificationDTO;
import org.eclipse.sensinact.northbound.query.dto.notification.ResultResourceNotificationDTO;
import org.eclipse.sensinact.northbound.query.dto.query.QuerySubscribeDTO;
import org.eclipse.sensinact.northbound.query.dto.query.QueryUnsubscribeDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ErrorResultDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResultSubscribeDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResultUnsubscribeDTO;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.osgi.util.promise.Deferred;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Websocket endpoint
 */
@WebSocket(idleTimeout = 0)
public class WebSocketEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEndpoint.class);

    /**
     * JSON mapper
     */
    private final ObjectMapper mapper = JsonMapper.builder().addModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false).build();

    /**
     * Current WebSocket session
     */
    private final AtomicReference<Session> wsSession = new AtomicReference<>();

    /**
     * Current user session
     */
    private final SensiNactSession userSession;

    /**
     * Parent socket pool
     */
    private final WebSocketCreator pool;

    /**
     * Query handler
     */
    private final IQueryHandler queryHandler;

    /**
     * Active subscriptions
     */
    private final Set<String> subscriptions = new HashSet<>();

    /**
     * Activity check in progress flag
     */
    private final AtomicReference<Deferred<Boolean>> activityCheckInProgress = new AtomicReference<>();

    /**
     * Activity check ping payload
     */
    private final byte[] pingPayload = "liveness-check".getBytes();

    /**
     * @param pool             WebSocket connections pool
     * @param sensiNactSession User session manager
     * @param queryHandler     Query handler
     */
    public WebSocketEndpoint(final WebSocketCreator pool, final SensiNactSession sensiNactSession,
            final IQueryHandler queryHandler) {
        this.pool = pool;
        this.userSession = sensiNactSession;
        this.queryHandler = queryHandler;
    }

    /**
     * {@return the sensiNact session ID}
     */
    public String getSessionId() {
        return this.userSession.getSessionId();
    }

    /**
     * Explicit WebSocket closure
     */
    public synchronized void close() {
        Session ws = wsSession.getAndSet(null);
        if (ws == null) {
            return;
        }

        // Disable activity checking
        activityCheckInProgress.set(null);

        // Close subscriptions first
        if (userSession != null) {
            for (final String listenerId : subscriptions) {
                userSession.removeListener(listenerId);
            }
        }
        subscriptions.clear();

        if (ws.isOpen()) {
            // Close websocket session
            try {
                ws.close();
            } catch (Throwable t) {
                logger.error("Error closing WebSocket: {}", t.getMessage(), t);
            }
        }

        userSession.expire();
    }

    @OnWebSocketConnect
    public void open(final Session session) {
        logger.debug("WebSocket opening - {}", session);
        wsSession.set(session);
    }

    @OnWebSocketClose
    public void onClose(final Session session, int statusCode, final String reason) {
        logger.debug("WebSocket closing - {} ({}: {})", session, statusCode, reason);
        pool.deleteSocketEndpoint(this);

        userSession.expire();
    }

    @OnWebSocketError
    public void onError(final Session session, final Throwable t) {
        logger.warn("Error from WebSocket session {}: {}", session, t.getMessage());
    }

    @OnWebSocketMessage
    public void onMessage(final Session wsSession, final String strContent) {

        if (userSession.isExpired()) {
            wsSession.close(StatusCode.ABNORMAL, "User session expired due to inactivity");
        }

        final AbstractQueryDTO query;
        try {
            query = mapper.readValue(strContent, AbstractQueryDTO.class);
        } catch (Throwable t) {
            logger.error("Error parsing WebSocket query: {}", t.getMessage(), t);
            sendError(wsSession, null, 400, "Error parsing query: " + t.getMessage());
            return;
        }

        try {
            final AbstractResultDTO result;
            switch (query.operation) {
                case SUBSCRIBE:
                    handleSubscribe(query.requestId, (QuerySubscribeDTO) query);
                    return;

                case UNSUBSCRIBE:
                    result = handleUnsubscribe((QueryUnsubscribeDTO) query);
                    break;

                default:
                    result = queryHandler.handleQuery(userSession, query);
                    break;
            }

            result.requestId = query.requestId;

            sendResult(wsSession, result);
        } catch (StatusException e) {
            logger.error("Error handling query {}: {}", query, e.getMessage(), e);
            sendError(wsSession, query.uri, e.statusCode, "Error running query: " + e.getMessage());
        } catch (Throwable t) {
            logger.error("Error handling query {}: {}", query, t.getMessage(), t);
            sendError(wsSession, query.uri, 500, "Error running query: " + t.getMessage());
        }
    }

    /**
     * Sends a ping to the client
     */
    public Promise<Boolean> checkActivity(PromiseFactory pf) {
        Session ws = wsSession.get();
        if (ws != null && ws.isOpen()) {
            try {
                final Deferred<Boolean> deferred = activityCheckInProgress.updateAndGet(current -> {
                    if (current == null) {
                        // No ping in progress: create a new promise
                        return pf.<Boolean>deferred();
                    } else {
                        logger.warn("New ping requested while previous one is in progress. Using current promise.");
                        return current;
                    }
                });

                // Send a new ping
                ws.getRemote().sendPing(ByteBuffer.wrap(pingPayload));
                return deferred.getPromise();
            } catch (IOException e) {
                // Something went wrong, fail the activity check
                logger.error("Error sending ping to client: {}", e.getMessage(), e);
                return pf.failed(e);
            }
        } else {
            // WebSocket is closed, so inactive
            return pf.resolved(false);
        }
    }

    @OnWebSocketFrame
    public void onFrame(final Session wsSession, final Frame frame) {
        try {
            switch (frame.getType()) {
                case PING:
                    // Respond to ping with pong
                    wsSession.getRemote().sendPong(frame.getPayload());
                    break;
                case PONG:
                    // Check pong content
                    if (!frame.hasPayload() || frame.getPayloadLength() != pingPayload.length) {
                        logger.warn("Ignoring PONG frame with no or invalid payload from client: {}", wsSession);
                        break;
                    }

                    // Extract payload
                    final byte[] payload = new byte[frame.getPayloadLength()];
                    frame.getPayload().get(payload);

                    if (Arrays.equals(payload, pingPayload)) {
                        // Notify pong received
                        Optional.ofNullable(activityCheckInProgress.getAndSet(null)).ifPresent(deferred -> {
                            // Pong received, complete the activity check successfully
                            deferred.resolve(true);
                        });
                    } else {
                        logger.warn("Ignoring PONG frame payload with invalid content from client: {}", wsSession);
                    }
                    break;

                default:
                    // Ignore other frame types
                    logger.debug("Ignoring WebSocket frame of type {} from client: {}", frame.getType(), wsSession);
                    break;
            }
        } catch (Exception e) {
            logger.error("Error handling WebSocket frame {}: {}", frame.getType(), e.getMessage(), e);
        }
    }

    /**
     * Converts the given notification to a snapshot
     *
     * @param notif Received notification
     * @return
     */
    private NotificationSnapshot convertToSnapshot(final ResourceNotification notif) {
        if (notif.getClass() == LifecycleNotification.class) {
            return new NotificationSnapshot((LifecycleNotification) notif);
        } else if (notif.getClass() == ResourceDataNotification.class) {
            return new NotificationSnapshot((ResourceDataNotification) notif);
        } else {
            // Not a notification we support
            return null;
        }
    }

    /**
     * Prepares the predicate to filter notifications
     *
     * @param query Subscription query
     * @return The filter to apply on all notifications
     * @throws StatusException Error creating the filter
     */
    private Predicate<ResourceNotification> prepareFilter(QuerySubscribeDTO query) throws StatusException {
        // Parse filter
        final ICriterion parsedFilter = queryHandler.parseFilter(query.filter, query.filterLanguage);
        if (parsedFilter == null) {
            return null;
        }

        // TODO this could be replaced with:
        //
        // parsedFilter.dataEventFilter();
        // parsedFilter.dataTopics();

        Predicate<ResourceNotification> predicate = null;

        // filters
        final Predicate<ProviderSnapshot> providerFilter = parsedFilter.getProviderFilter();
        final Predicate<ServiceSnapshot> serviceFilter = parsedFilter.getServiceFilter();
        final Predicate<ResourceSnapshot> resourceFilter = parsedFilter.getResourceFilter();
        final ResourceValueFilter resourceValueFilter = parsedFilter.getResourceValueFilter();
        final BiPredicate<ProviderSnapshot, GeoJsonObject> locationFilter = parsedFilter.getLocationFilter();

        if (providerFilter != null || serviceFilter != null || resourceFilter != null || resourceValueFilter != null) {
            // We have at least one basic filter
            predicate = notif -> {
                // Mimic a snapshot to apply the filter
                final NotificationSnapshot snapshot = convertToSnapshot(notif);
                if (snapshot == null) {
                    // Couldn't create the snapshot, consider the result invalid
                    return false;
                }

                if (providerFilter != null && !providerFilter.test(snapshot.provider)) {
                    return false;
                }

                if (serviceFilter != null && !serviceFilter.test(snapshot.service)) {
                    return false;
                }

                if (resourceFilter != null && !resourceFilter.test(snapshot.resource)) {
                    return false;
                }

                if (resourceValueFilter != null
                        && !resourceValueFilter.test(snapshot.provider, List.of(snapshot.resource))) {
                    return false;
                }
                if (locationFilter != null && "admin".equals(notif.service()) && "location".equals(notif.resource())) {
                    if (notif.getClass() == LifecycleNotification.class) {
                        final LifecycleNotification lifecycleNotif = (LifecycleNotification) notif;
                        if (!locationFilter.test(snapshot.provider, (GeoJsonObject) lifecycleNotif.initialValue())) {
                            return false;
                        }
                    } else if (notif.getClass() == ResourceDataNotification.class) {
                        final ResourceDataNotification dataNotif = (ResourceDataNotification) notif;
                        if (!locationFilter.test(snapshot.provider, (GeoJsonObject) dataNotif.newValue())) {
                            return false;
                        }
                    }
                }

                // Passed all tests
                return true;
            };
        }
        return predicate;
    }

    /**
     * Registers a subscription of the current session
     *
     * @param requestId
     *
     * @param userSession Caller session
     * @param query       Subscription query
     * @return Result DTO
     */
    private void handleSubscribe(String requestId, final QuerySubscribeDTO query) throws Exception {
        final SensinactPath path = query.uri;
        CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<String> listenerId = new AtomicReference<>();

        final ResultSubscribeDTO result = new ResultSubscribeDTO();
        List<String> topics;
        final Predicate<ResourceNotification> eventPredicate;
        if (path.targetsSpecificResource()) {
            result.uri = path.toUri();
            // TODO replace this with single level wildcard
            topics = List.of("*");
            eventPredicate = n -> query.uri.provider.equals(n.provider()) && query.uri.service.equals(n.service())
                    && query.uri.resource.equals(n.resource());
        } else {
            result.uri = "/";
            topics = List.of("*");
            eventPredicate = n -> true;
        }

        final Predicate<ResourceNotification> p;
        if (query.filter != null) {
            Predicate<ResourceNotification> queryFilter = prepareFilter(query);
            if (queryFilter != null) {
                p = eventPredicate.and(queryFilter);
            } else {
                p = eventPredicate;
            }
        } else {
            p = eventPredicate;
        }

        final ClientDataListener cld = (topic, evt) -> {
            try {
                Session ws = wsSession.get();
                if (ws == null || !ws.isOpen()) {
                    logger.warn("Detected closed WebSocket. Stop listening");
                    userSession.removeListener(listenerId.get());
                } else if ((p == null || p.test(evt)) && checkLatch(latch)) {
                    sendNotification(ws, listenerId.get(), new ResourceDataNotificationDTO(evt));
                }
            } catch (Throwable e) {
                logger.warn("Error notifying WebSocket of life cycle update", e);
            }
        };

        final ClientLifecycleListener cll = (topic, evt) -> {
            try {
                Session ws = wsSession.get();
                if (ws == null || !ws.isOpen()) {
                    logger.warn("Detected closed WebSocket. Stop listening");
                    userSession.removeListener(listenerId.get());
                } else if ((p == null || p.test(evt)) && checkLatch(latch)) {
                    sendNotification(ws, listenerId.get(), new ResourceLifecycleNotificationDTO(evt));
                }
            } catch (Throwable e) {
                logger.error("Error notifying WebSocket of a data update", e);
            }
        };

        String id = userSession.addListener(topics, cld, null, cll, null);
        listenerId.set(id);

        // Store listener details
        subscriptions.add(id);
        // Send the subscription response to the client
        result.statusCode = 200;
        result.subscriptionId = id;
        result.requestId = requestId;
        try {
            Session ws = wsSession.get();
            if (ws == null || !ws.isOpen()) {
                subscriptions.remove(id);
                userSession.removeListener(id);
            } else {
                sendResult(ws, result);
            }
        } catch (Exception e) {
            subscriptions.remove(id);
            userSession.removeListener(id);

            // Don't release the latch so any pending notifications time out
            throw e;
        }

        // Release the listeners
        latch.countDown();
    }

    private boolean checkLatch(CountDownLatch latch) {
        try {
            return latch.await(500, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Sends a notification to the client
     *
     * @param ws
     *
     * @param listenerId   ID of the subscription
     * @param notification Notification DTO
     */
    private void sendNotification(Session ws, final String listenerId,
            final AbstractResourceNotificationDTO notification) {
        try {
            final ResultResourceNotificationDTO result = new ResultResourceNotificationDTO();
            result.statusCode = 200;
            result.uri = new SensinactPath(notification.provider, notification.service, notification.resource).toUri();
            result.subscriptionId = listenerId;
            result.notification = notification;
            ws.getRemote().sendString(mapper.writeValueAsString(result));
        } catch (IOException e) {
            logger.error("Error sending notification to client: {}", e.getMessage(), e);
            try {
                final ErrorResultNotificationDTO errorNotification = new ErrorResultNotificationDTO(listenerId);
                ws.getRemote().sendString(mapper.writeValueAsString(errorNotification));
            } catch (IOException e2) {
                logger.error("Error sending notification to client: {}. Closing WebSocket.", e2.getMessage(), e2);
                userSession.removeListener(listenerId);
                WebSocketEndpoint.this.close();
            }
        }
    }

    /**
     * Removes a subscription from the current session
     *
     * @param query Subscription removal query
     * @return Result DTO
     */
    private AbstractResultDTO handleUnsubscribe(final QueryUnsubscribeDTO query) {
        if (subscriptions.remove(query.subscriptionId)) {
            userSession.removeListener(query.subscriptionId);
            final ResultUnsubscribeDTO result = new ResultUnsubscribeDTO();
            result.statusCode = 200;
            result.subscriptionId = query.subscriptionId;
            return result;
        }

        return new ErrorResultDTO(404, "Unknown subscription");
    }

    /**
     * Sends a result
     *
     * @param session WebSocket session
     * @param dto     Result DTO
     */
    private void sendResult(final Session session, final AbstractResultDTO dto) {
        try {
            session.getRemote().sendString(mapper.writeValueAsString(dto));
        } catch (IOException e) {
            sendError(session, null, 500, "Error sending results: " + e.getMessage());
        }
    }

    /**
     * Sends an error to the client
     *
     * @param session      WebSocket session
     * @param uri          Target URI, if known
     * @param statusCode   Error status code
     * @param errorMessage Error message
     */
    private void sendError(final Session session, final SensinactPath target, final int statusCode,
            final String errorMessage) {
        final AbstractResultDTO dto = new ErrorResultDTO();
        dto.uri = target != null ? target.toUri() : null;
        dto.statusCode = statusCode;
        dto.error = errorMessage;

        String payload;
        try {
            payload = mapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            logger.error("Error preparing error message payload to client: {}", e.getMessage(), e);
            payload = "{\"uri\": null, \"statusCode\": 500, \"error\": \"Error sending error\", \"result\": null}";
        }

        try {
            session.getRemote().sendString(payload);
        } catch (IOException e) {
            logger.error("Error sending error to client: {}", e.getMessage(), e);
        }
    }
}
