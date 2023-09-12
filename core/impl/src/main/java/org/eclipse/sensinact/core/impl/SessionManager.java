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
package org.eclipse.sensinact.core.impl;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

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
import org.eclipse.sensinact.core.notification.AbstractResourceNotification;
import org.eclipse.sensinact.core.security.UserInfo;
import org.eclipse.sensinact.core.session.SensiNactSession;
import org.eclipse.sensinact.core.session.SensiNactSessionManager;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.typedevent.TypedEventHandler;
import org.osgi.service.typedevent.propertytypes.EventTopics;

@Component(property = IMetricsGauge.NAME + "=sensinact.sessions")
@EventTopics({ "LIFECYCLE/*", "METADATA/*", "DATA/*", "ACTION/*" })
public class SessionManager
        implements SensiNactSessionManager, TypedEventHandler<AbstractResourceNotification>, IMetricsGauge {

    @Reference
    GatewayThread thread;

    private final Object lock = new Object();

    private final Map<String, SensiNactSessionImpl> sessions = new HashMap<>();

    private final Map<String, Set<String>> sessionsByUser = new HashMap<>();

    private final Map<String, String> userDefaultSessionIds = new HashMap<>();

    @Override
    public Object gauge() {
        return sessions.size();
    }

    @Override
    public SensiNactSession getDefaultSession(UserInfo user) {
        Objects.requireNonNull(user);
        SensiNactSession session = null;
        String userId = user.getUserId();
        String sessionId;

        synchronized (lock) {
            sessionId = userDefaultSessionIds.get(userId);

            if (sessionId != null) {
                session = sessions.get(sessionId);
            }
        }

        // Outside of lock to check/create the session
        if (session == null) {
            if (sessionId != null) {
                removeSession(userId, sessionId);
            }

            session = createNewSession(user);
            sessionId = session.getSessionId();

            synchronized (lock) {
                sessionId = userDefaultSessionIds.putIfAbsent(userId, sessionId);
            }

            if (sessionId != null) {
                // Someone beat us to the punch
                removeSession(userId, session.getSessionId());
                session.expire();
                return getSession(user, sessionId);
            }
        } else if (session.isExpired()) {
            removeSession(userId, sessionId);
            session = getDefaultSession(user);
        }

        return session;
    }

    /**
     * @param userToken
     * @param sessionId
     */
    private void removeSession(String userId, String sessionId) {
        synchronized (lock) {
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

        synchronized (lock) {
            if (sessionsByUser.getOrDefault(userId, Set.of()).contains(sessionId)) {
                session = sessions.get(sessionId);
            }
        }

        if (session != null && session.isExpired()) {
            removeSession(userId, sessionId);
            session = null;
        }

        return session;
    }

    @Override
    public List<String> getSessionIds(UserInfo user) {
        Objects.requireNonNull(user);
        String userId = user.getUserId();
        List<String> ids;
        synchronized (lock) {
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
        return ids;
    }

    @Override
    public SensiNactSession createNewSession(UserInfo user) {
        Objects.requireNonNull(user);
        SensiNactSessionImpl session = new SensiNactSessionImpl(user, thread);
        String sessionId = session.getSessionId();

        synchronized (lock) {
            sessionsByUser.merge(user.getUserId(), Set.of(sessionId),
                    (a, b) -> Stream.concat(b.stream(), a.stream()).collect(toCollection(LinkedHashSet::new)));
            sessions.put(sessionId, session);
        }
        return session;
    }

    @Override
    public void notify(String topic, AbstractResourceNotification event) {
        List<SensiNactSessionImpl> sessions;
        synchronized (lock) {
            sessions = new ArrayList<>(this.sessions.values());
        }
        for (SensiNactSessionImpl session : sessions) {
            if (!session.isExpired()) {
                try {
                    session.notify(topic, event);
                } catch (Exception e) {
                    // TODO log this
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
