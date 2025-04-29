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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.metrics.IMetricsGauge;
import org.eclipse.sensinact.core.notification.ResourceNotification;
import org.eclipse.sensinact.northbound.security.api.AuthorizationEngine;
import org.eclipse.sensinact.northbound.security.api.UserInfo;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.northbound.session.SensiNactSessionManager;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.typedevent.TypedEventHandler;
import org.osgi.service.typedevent.propertytypes.EventTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(configurationPid = "sensinact.session.manager", property = IMetricsGauge.NAME + "=sensinact.sessions")
@EventTopics({ "LIFECYCLE/*", "METADATA/*", "DATA/*", "ACTION/*" })
public class SessionManager
        implements SensiNactSessionManager, TypedEventHandler<ResourceNotification>, IMetricsGauge {

    private static final Logger LOG = LoggerFactory.getLogger(SessionManager.class);

    @Reference
    GatewayThread thread;

    private final Object lock = new Object();

    private final Map<String, SensiNactSessionImpl> sessions = new HashMap<>();

    private final Map<String, Set<String>> sessionsByUser = new HashMap<>();

    private final Map<String, String> userDefaultSessionIds = new HashMap<>();

    private AuthorizationEngine authEngine;

    private boolean active;

    private Config config;

    public @interface Config {
        int expiry() default 600;
        DefaultAuthPolicy auth_policy() default DefaultAuthPolicy.DENY_ALL;
    }

    @Reference(cardinality = OPTIONAL, policy = DYNAMIC)
    void setAuthorization(AuthorizationEngine auth) {
        if(LOG.isDebugEnabled()) {
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
        if(LOG.isDebugEnabled()) {
            LOG.debug("{} sessions will be invalidated", toInvalidate.size());
        }
        toInvalidate.forEach(SensiNactSession::expire);
    }

    void unsetAuthorization(AuthorizationEngine auth) {
        if(LOG.isDebugEnabled()) {
            LOG.debug("Removing an external Authorization Engine. Existing sessions will be invalidated");
        }
        List<SensiNactSessionImpl> toInvalidate;
        synchronized (lock) {
            if(authEngine == auth) {
                authEngine = null;
                toInvalidate = new ArrayList<>(sessions.values());
                sessions.clear();
                sessionsByUser.clear();
                userDefaultSessionIds.clear();
            } else {
                toInvalidate = List.of();
            }
        }
        if(LOG.isDebugEnabled()) {
            LOG.debug("{} sessions will be invalidated", toInvalidate.size());
        }
        toInvalidate.forEach(SensiNactSession::expire);
    }

    @Activate
    void start(Config config) {
        if(LOG.isDebugEnabled()) {
            LOG.debug("Starting the Session Manager with session lifetime {} and default authorization policy {}",
                    config.expiry(), config.auth_policy());
        }
        synchronized (lock) {
            active = true;
            this.config = config;
        }
    }

    @Deactivate
    void stop() {
        if(LOG.isDebugEnabled()) {
            LOG.debug("Shutting down the session manager");
        }
        List<SensiNactSessionImpl> toInvalidate;
        synchronized (lock) {
            active = false;
            toInvalidate = new ArrayList<>(sessions.values());
            sessions.clear();
            sessionsByUser.clear();
            userDefaultSessionIds.clear();
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
        if(!active) {
            throw new IllegalStateException("The session manager is closed");
        }
    }

    @Override
    public SensiNactSession getDefaultSession(UserInfo user) {
        Objects.requireNonNull(user);
        SensiNactSession session = null;
        String userId = user.getUserId();
        String sessionId;

        if(LOG.isDebugEnabled()) {
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
                if(LOG.isDebugEnabled()) {
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
                if(LOG.isDebugEnabled()) {
                    LOG.debug("Another default session {} was created for user {}. That session will be used instead", sessionId, userId);
                }
                // Someone beat us to the punch
                removeSession(userId, session.getSessionId());
                session.expire();
                return getDefaultSession(user);
            }
        } else if (session.isExpired()) {
            if(LOG.isDebugEnabled()) {
                LOG.debug("The default session {} for user {} has expired. Creating a new session", sessionId, userId);
            }
            removeSession(userId, sessionId);
            session = getDefaultSession(user);
        }

        if(LOG.isDebugEnabled()) {
            LOG.debug("Located default session {} for user {}", sessionId, userId);
        }
        return session;
    }

    /**
     * @param userToken
     * @param sessionId
     */
    private void removeSession(String userId, String sessionId) {
        if(LOG.isDebugEnabled()) {
            LOG.debug("Clearing session {} for user {}", sessionId, userId);
        }
        synchronized (lock) {
            doCheck();
            userDefaultSessionIds.remove(userId, sessionId);
            sessionsByUser.computeIfPresent(userId,
                    (k, v) -> v.stream().filter(s -> !s.equals(sessionId)).collect(toCollection(LinkedHashSet::new)));
            sessions.remove(sessionId);
        }
    }

    @Override
    public SensiNactSession getSession(UserInfo user, String sessionId) {
        Objects.requireNonNull(user);
        SensiNactSessionImpl session = null;
        String userId = user.getUserId();

        if(LOG.isDebugEnabled()) {
            LOG.debug("Getting session {} for user {}", sessionId, userId);
        }

        synchronized (lock) {
            doCheck();
            if (sessionsByUser.getOrDefault(userId, Set.of()).contains(sessionId)) {
                session = sessions.get(sessionId);
            }
        }

        if (session != null && session.isExpired()) {
            if(LOG.isDebugEnabled()) {
                LOG.debug("Session {} for user {} has expired", sessionId, userId);
            }
            removeSession(userId, sessionId);
            session = null;
        }

        if(LOG.isDebugEnabled()) {
            LOG.debug("Returning session {} for user {}", session == null ? null : sessionId, userId);
        }
        return session;
    }

    @Override
    public List<String> getSessionIds(UserInfo user) {
        Objects.requireNonNull(user);
        String userId = user.getUserId();

        if(LOG.isDebugEnabled()) {
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

        if(LOG.isDebugEnabled()) {
            LOG.debug("User {} has sessions {}", userId, ids);
        }

        return ids;
    }

    @Override
    public SensiNactSession createNewSession(UserInfo user) {
        Objects.requireNonNull(user);

        if(LOG.isDebugEnabled()) {
            LOG.debug("Creating a new session for user {}", user.getUserId());
        }

        AuthorizationEngine auth;
        DefaultAuthPolicy policy;
        synchronized (lock) {
            doCheck();
            auth = authEngine;
            policy = config.auth_policy();
        }

        SensiNactSessionImpl session;
        if(auth == null) {
            if(LOG.isDebugEnabled()) {
                LOG.debug("No Authorization Engine is set. Using policy {}", policy);
            }
            session = new SensiNactSessionImpl(user, new DefaultSessionAuthorizationEngine(policy)
                    .createAuthorizer(user), thread);
        } else {
            session = new SensiNactSessionImpl(user, auth.createAuthorizer(user), thread);
        }

        String sessionId = session.getSessionId();

        boolean authChanged;
        synchronized (lock) {
            if(auth == authEngine) {
                authChanged = false;
                sessionsByUser.merge(user.getUserId(), Set.of(sessionId),
                        (a, b) -> Stream.concat(b.stream(), a.stream()).collect(toCollection(LinkedHashSet::new)));
                sessions.put(sessionId, session);
            } else {
                authChanged = true;
            }
        }

        if(authChanged) {
            if(LOG.isDebugEnabled()) {
                LOG.debug("The Authorization Engine changed. Recreating the new session");
            }
            return createNewSession(user);
        } else {
            if(LOG.isDebugEnabled()) {
                LOG.debug("Created a new session {} for user {}", sessionId, user.getUserId());
            }
            return session;
        }
    }

    @Override
    public void notify(String topic, ResourceNotification event) {
        if(LOG.isDebugEnabled()) {
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
                    LOG.error("Error notifiying session {} on topic {}", session.getSessionId(), topic);
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
    public SensiNactSession createNewAnonymousSession() {
        return createNewSession(UserInfo.ANONYMOUS);
    }
}
