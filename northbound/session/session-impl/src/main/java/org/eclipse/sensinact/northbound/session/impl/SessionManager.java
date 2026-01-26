/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.northbound.session.impl;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static org.osgi.service.component.annotations.ReferenceCardinality.OPTIONAL;
import static org.osgi.service.component.annotations.ReferencePolicy.DYNAMIC;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.eclipse.sensinact.core.authorization.Authorizer;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.metrics.IMetricsGauge;
import org.eclipse.sensinact.core.notification.ResourceNotification;
import org.eclipse.sensinact.northbound.security.api.AuthorizationEngine;
import org.eclipse.sensinact.northbound.security.api.PreAuthorizer;
import org.eclipse.sensinact.northbound.security.api.UserInfo;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.northbound.session.SensiNactSessionActivityChecker;
import org.eclipse.sensinact.northbound.session.SensiNactSessionManager;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.typedevent.TypedEventHandler;
import org.osgi.service.typedevent.propertytypes.EventTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(configurationPid = SessionManager.CONFIGURATION_PID, property = IMetricsGauge.NAME + "=sensinact.sessions")
@EventTopics({ "LIFECYCLE/*", "METADATA/*", "DATA/*", "ACTION/*" })
public class SessionManager
        implements SensiNactSessionManager, TypedEventHandler<ResourceNotification>, IMetricsGauge {

    /**
     * Session Manager OSGi Configuration Admin PID
     */
    public static final String CONFIGURATION_PID = "sensinact.session.manager";

    private static final Logger LOG = LoggerFactory.getLogger(SessionManager.class);

    /**
     * Gateway Thread
     */
    @Reference
    GatewayThread thread;

    /**
     * Lock object for synchronizing access to the session maps
     */
    private final Object lock = new Object();

    /**
     * Maps session ID to its session implementation
     */
    private final Map<String, SensiNactSessionImpl> sessions = new HashMap<>();

    /**
     * Maps user ID to a set of session IDs
     */
    private final Map<String, Set<String>> sessionsByUser = new HashMap<>();

    /**
     * Maps user ID to its default session ID
     */
    private final Map<String, String> userDefaultSessionIds = new HashMap<>();

    /**
     * Authorization Engine
     */
    private AuthorizationEngine authEngine;

    /**
     * Component activation flag
     */
    private boolean active;

    /**
     * Component configuration
     */
    private Config config;

    /**
     * Scheduler for session expiration checks
     */
    private ScheduledExecutorService activityCheckScheduler;

    /**
     * Session lifespan extension upon positive activity check
     */
    private Duration sessionActivityExtension;

    /**
     * Threshold to trigger to the activity check
     */
    private Duration sessionActivityThreshold;

    /**
     * Definition of the component configuration properties
     */
    public @interface Config {
        /**
         * Session expiry time in seconds (defaults to 10 minutes)
         */
        int expiry() default 600;

        /**
         * Interval in seconds between two activity checks (defaults to 60 seconds)
         */
        int activity_check_interval() default 60;

        /**
         * Minimal interval in seconds between session expiration and activity check (defaults to 10 seconds).
         * The activity check will not be scheduled if the expiry is greater than this threshold.
         */
        int activity_check_threshold() default 10;

        /**
         * Session lifespan extension in seconds upon positive activity check (defaults
         * to expiry time)
         */
        int activity_check_extension() default -1;

        /**
         * Default authorization policy (defaults to {@link DefaultAuthPolicy#DENY_ALL})
         */
        DefaultAuthPolicy auth_policy() default DefaultAuthPolicy.DENY_ALL;
    }

    @Reference(cardinality = OPTIONAL, policy = DYNAMIC)
    void setAuthorization(AuthorizationEngine auth) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Setting an external Authorization Engine. Existing sessions will be invalidated");
        }
        List<SensiNactSessionImpl> toInvalidate;
        synchronized (lock) {
            authEngine = auth;
            toInvalidate = new ArrayList<>(sessions.values());
            sessions.clear();
            sessionsByUser.clear();
            userDefaultSessionIds.clear();
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("{} sessions will be invalidated", toInvalidate.size());
        }
        toInvalidate.forEach(SensiNactSession::expire);
    }

    void unsetAuthorization(AuthorizationEngine auth) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Removing an external Authorization Engine. Existing sessions will be invalidated");
        }
        List<SensiNactSessionImpl> toInvalidate;
        synchronized (lock) {
            if (authEngine == auth) {
                authEngine = null;
                toInvalidate = new ArrayList<>(sessions.values());
                sessions.clear();
                sessionsByUser.clear();
                userDefaultSessionIds.clear();
            } else {
                toInvalidate = List.of();
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("{} sessions will be invalidated", toInvalidate.size());
        }
        toInvalidate.forEach(SensiNactSession::expire);
    }

    @Activate
    void start(Config config) {
        final int expiry = config.expiry();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting the Session Manager with session lifetime {}s and default authorization policy {}",
                    expiry, config.auth_policy());
        }

        // Start the scheduler for session expiration checks
        sessionActivityExtension = null;
        sessionActivityThreshold = null;
        final long activityCheckPeriod = config.activity_check_interval();
        if (expiry <= 0 || activityCheckPeriod <= 0) {
            LOG.debug("Activity checker disabled");
        } else if (activityCheckPeriod > expiry - 1) {
            LOG.warn("Activity check interval too long ({}) compared to expiry ({}). Disabling it.",
                    activityCheckPeriod, expiry);
        } else {
            // Initialize the activity checker
            this.activityCheckScheduler = Executors.newSingleThreadScheduledExecutor(
                    r -> new Thread(r, "sensinact-session-activity-checker"));
            this.activityCheckScheduler.scheduleAtFixedRate(this::checkSessionsLiveness, activityCheckPeriod,
                    activityCheckPeriod, TimeUnit.SECONDS);

            sessionActivityThreshold = Duration.ofSeconds(
                    config.activity_check_threshold() > 0 ? config.activity_check_threshold() : 10);

            sessionActivityExtension = Duration.ofSeconds(
                    config.activity_check_extension() > 0 ? config.activity_check_extension() : expiry);
            LOG.debug(
                    "Setting up activity check to run every {} seconds. Check threshold set to {}. Session extension set to {}",
                    activityCheckPeriod, sessionActivityThreshold, sessionActivityExtension);
        }

        synchronized (lock) {
            active = true;
            this.config = config;
        }
    }

    @Deactivate
    void stop() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Shutting down the session manager");
        }

        // Stop the scheduler for session expiration checks
        if (this.activityCheckScheduler != null) {
            this.activityCheckScheduler.shutdownNow();
            this.activityCheckScheduler = null;
        }

        List<SensiNactSessionImpl> toInvalidate;
        synchronized (lock) {
            active = false;
            toInvalidate = new ArrayList<>(sessions.values());
            sessions.clear();
            sessionsByUser.clear();
            userDefaultSessionIds.clear();
            sessionActivityExtension = null;
            sessionActivityThreshold = null;
        }
        toInvalidate.forEach(SensiNactSession::expire);
    }

    @Override
    public Object gauge() {
        synchronized (lock) {
            return sessions.size();
        }
    }

    /**
     * Only call when synchronized on lock
     */
    private void doCheck() {
        if (!active) {
            throw new IllegalStateException("The session manager is closed");
        }
    }

    /**
     * Checks the liveness of all sessions with an activity checker
     */
    private void checkSessionsLiveness() {
        final Duration extension = this.sessionActivityExtension;
        if (extension == null) {
            // Nothing to do
            return;
        }

        final List<SensiNactSessionImpl> sessionsToCheck = new ArrayList<>();
        synchronized (lock) {
            doCheck();

            try {
                final Instant checkTime = Instant.now();

                for (SensiNactSessionImpl session : List.copyOf(sessions.values())) {
                    // Expiration checks update the sessions maps (removing expired sessions),
                    // hence the use of an intermediate copy instead of a view
                    Instant expiry = session.getExpiry();
                    if (expiry != null
                            && session.hasActivityChecker()
                            && expiry.minus(sessionActivityThreshold).isBefore(checkTime)) {
                        sessionsToCheck.add(session);
                    }
                }
            } catch (Exception e) {
                LOG.error("Error collecting sessions to check", e);
                return;
            }
        }

        if (sessionsToCheck.isEmpty()) {
            return;
        }

        sessionsToCheck.forEach(s -> s.checkActivity(isActive -> {
            if (!active) {
                // Session manager is stopped, do nothing
                return;
            }

            if (s.isExpired()) {
                // Session already expired, do "nothing" (the call might have triggered the
                // expiration call chain)
                return;
            }

            if (isActive) {
                try {
                    // Session is active and not expired, extend it
                    s.extend(extension);
                } catch (Exception e) {
                    // Extension can fail if we were nanoseconds away from expiration
                    LOG.error("Error extending session {} expiry", s.getSessionId(), e);
                }
            }
        }));
    }

    @Override
    public SensiNactSession getDefaultSession(UserInfo user) {
        Objects.requireNonNull(user);
        SensiNactSession session = null;
        String userId = user.getUserId();
        String sessionId;

        if (LOG.isDebugEnabled()) {
            LOG.debug("Getting the default session for user {}", userId);
        }

        synchronized (lock) {
            doCheck();
            sessionId = userDefaultSessionIds.get(userId);

            if (sessionId != null) {
                session = sessions.get(sessionId);
            }
        }

        // Outside of lock to check/create the session
        if (session == null) {
            if (sessionId != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("No default session {} for user {}. Creating a new session", sessionId, userId);
                }
                removeSession(userId, sessionId);
            }

            session = createNewSession(user);
            sessionId = session.getSessionId();

            synchronized (lock) {
                sessionId = userDefaultSessionIds.putIfAbsent(userId, sessionId);
            }

            if (sessionId != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Another default session {} was created for user {}. That session will be used instead",
                            sessionId, userId);
                }
                // Someone beat us to the punch
                removeSession(userId, session.getSessionId());
                session.expire();
                return getDefaultSession(user);
            }
        } else if (session.isExpired()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("The default session {} for user {} has expired. Creating a new session", sessionId, userId);
            }
            removeSession(userId, sessionId);
            session = getDefaultSession(user);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Located default session {} for user {}", sessionId, userId);
        }
        return session;
    }

    /**
     * Removes and expires the session with the given ID for the given user ID
     *
     * @param userId    User ID
     * @param sessionId Session ID
     */
    private void removeSession(String userId, String sessionId) {
        removeSession(userId, sessionId, true);
    }

    /**
     * Removes the session with the given ID for the given user ID. Expires it if
     * requested.
     *
     * @param userId        User ID
     * @param sessionId     Session ID
     * @param expireSession Flag to expire the session upon removal
     */
    private void removeSession(String userId, String sessionId, boolean expireSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Clearing session {} for user {}", sessionId, userId);
        }

        synchronized (lock) {
            if (!active) {
                // Silent check as this method can be called by session upon component
                // invalidation
                return;
            }

            // Remove references to the session
            userDefaultSessionIds.remove(userId, sessionId);
            sessionsByUser.computeIfPresent(userId,
                    (k, v) -> v.stream().filter(s -> !s.equals(sessionId)).collect(toCollection(LinkedHashSet::new)));
            SensiNactSession session = sessions.remove(sessionId);

            if (session != null && expireSession) {
                // Expire the session
                session.expire();
            }
        }
    }

    @Override
    public SensiNactSession getSession(UserInfo user, String sessionId) {
        Objects.requireNonNull(user);
        SensiNactSessionImpl session = null;
        String userId = user.getUserId();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Getting session {} for user {}", sessionId, userId);
        }

        synchronized (lock) {
            doCheck();
            if (sessionsByUser.getOrDefault(userId, Set.of()).contains(sessionId)) {
                session = sessions.get(sessionId);
            }
        }

        if (session != null && session.isExpired()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Session {} for user {} has expired", sessionId, userId);
            }
            removeSession(userId, sessionId);
            session = null;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Returning session {} for user {}", session == null ? null : sessionId, userId);
        }
        return session;
    }

    @Override
    public List<String> getSessionIds(UserInfo user) {
        Objects.requireNonNull(user);
        String userId = user.getUserId();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrieving active session ids for user {}", userId);
        }

        List<String> ids;
        synchronized (lock) {
            doCheck();
            ids = sessionsByUser.getOrDefault(userId, Set.of()).stream().collect(toList());
        }

        Iterator<String> it = ids.iterator();
        while (it.hasNext()) {
            String sessionId = it.next();
            SensiNactSession session;
            synchronized (lock) {
                session = sessions.get(sessionId);
            }

            if (session == null || session.isExpired()) {
                removeSession(userId, sessionId);
                it.remove();
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("User {} has sessions {}", userId, ids);
        }

        return ids;
    }

    @Override
    public SensiNactSession createNewSession(UserInfo user, SensiNactSessionActivityChecker activityChecker) {
        Objects.requireNonNull(user);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating a new session for user {}", user.getUserId());
        }

        final AuthorizationEngine auth;
        final DefaultAuthPolicy policy;
        final Duration expiry;
        synchronized (lock) {
            doCheck();
            auth = authEngine;
            policy = config.auth_policy();
            expiry = Duration.ofSeconds(config.expiry());
        }

        final AuthorizationEngine defaultAuthEngine = new DefaultSessionAuthorizationEngine(policy);
        PreAuthorizer preAuthorizer;
        Authorizer authorizer;
        if (auth == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No Authorization Engine is set. Using policy {}", policy);
            }

            preAuthorizer = defaultAuthEngine.createPreAuthorizer(user);
            authorizer = defaultAuthEngine.createAuthorizer(user);
        } else {
            preAuthorizer = auth.createPreAuthorizer(user);
            if (preAuthorizer == null) {
                LOG.debug("No PreAuthorizer provided. Using policy {}", policy);
                preAuthorizer = defaultAuthEngine.createPreAuthorizer(user);
            }

            authorizer = auth.createAuthorizer(user);
            if (authorizer == null) {
                LOG.debug("No Authorizer provided. Using policy {}", policy);
                authorizer = defaultAuthEngine.createAuthorizer(user);
            }
        }

        final SensiNactSessionImpl session = new SensiNactSessionImpl(user, preAuthorizer, authorizer, thread, expiry,
                activityChecker);
        String sessionId = session.getSessionId();

        // Add an expiration listener to clean up session when explicitly expired
        session.addExpirationListener(this::handleExplicitSessionExpiration);

        boolean authChanged;
        synchronized (lock) {
            if (auth == authEngine) {
                authChanged = false;
                sessionsByUser.merge(user.getUserId(), Set.of(sessionId),
                        (a, b) -> Stream.concat(b.stream(), a.stream()).collect(toCollection(LinkedHashSet::new)));
                sessions.put(sessionId, session);
            } else {
                authChanged = true;
            }
        }

        if (authChanged) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("The Authorization Engine changed. Recreating the new session");
            }
            return createNewSession(user);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Created a new session {} for user {}", sessionId, user.getUserId());
            }
            return session;
        }
    }

    /**
     * Removes the session from the manager
     *
     * @param session Explicitly closed session
     */
    private void handleExplicitSessionExpiration(final SensiNactSession session) {
        removeSession(session.getUserInfo().getUserId(), session.getSessionId(), false);
    }

    @Override
    public void notify(String topic, ResourceNotification event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Session Manager received a notification on topic {}", topic);
        }
        List<SensiNactSessionImpl> sessions;
        synchronized (lock) {
            sessions = new ArrayList<>(this.sessions.values());
        }
        for (SensiNactSessionImpl session : sessions) {
            if (!session.isExpired()) {
                try {
                    session.notify(topic, event);
                } catch (Exception e) {
                    LOG.error("Error notifying session {} on topic {}", session.getSessionId(), topic);
                }
            } else {
                removeSession(session.getUserInfo().getUserId(), session.getSessionId());
            }
        }
    }

    @Override
    public SensiNactSession getDefaultAnonymousSession() {
        return getDefaultSession(UserInfo.ANONYMOUS);
    }

    @Override
    public SensiNactSession getAnonymousSession(String sessionId) {
        return getSession(UserInfo.ANONYMOUS, sessionId);
    }

    @Override
    public List<String> getAnonymousSessionIds() {
        return getSessionIds(UserInfo.ANONYMOUS);
    }

    @Override
    public SensiNactSession createNewAnonymousSession(SensiNactSessionActivityChecker activityChecker) {
        return createNewSession(UserInfo.ANONYMOUS, activityChecker);
    }
}
