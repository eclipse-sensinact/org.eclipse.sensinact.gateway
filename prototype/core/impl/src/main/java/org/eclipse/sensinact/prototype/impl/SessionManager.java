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
package org.eclipse.sensinact.prototype.impl;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.sensinact.prototype.SensiNactSession;
import org.eclipse.sensinact.prototype.SensiNactSessionManager;
import org.eclipse.sensinact.prototype.command.GatewayThread;
import org.eclipse.sensinact.prototype.notification.AbstractResourceNotification;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.typedevent.TypedEventHandler;
import org.osgi.service.typedevent.propertytypes.EventTopics;

@Component
@EventTopics({ "LIFECYCLE/*", "METADATA/*", "DATA/*", "ACTION/*" })
public class SessionManager implements SensiNactSessionManager, TypedEventHandler<AbstractResourceNotification> {

    @Reference
    GatewayThread thread;

    private final Object lock = new Object();

    private final Map<String, SensiNactSessionImpl> sessions = new HashMap<>();

    private final Map<String, Set<String>> sessionsByUser = new HashMap<>();

    private final Map<String, String> userDefaultSessionIds = new HashMap<>();

    @Override
    public SensiNactSession getDefaultSession(String userToken) {
        SensiNactSession session = null;
        String sessionId;

        synchronized (lock) {
            sessionId = userDefaultSessionIds.get(userToken);

            if (sessionId != null) {
                session = sessions.get(sessionId);
            }
        }

        // Outside of lock to check/create the session
        if (session == null) {
            if (sessionId != null) {
                removeSession(userToken, sessionId);
            }

            session = createNewSession(userToken);
            sessionId = session.getSessionId();

            synchronized (lock) {
                sessionId = userDefaultSessionIds.putIfAbsent(userToken, sessionId);
            }

            if (sessionId != null) {
                // Someone beat us to the punch
                removeSession(userToken, session.getSessionId());
                session.expire();
                return getSession(sessionId);
            }
        } else if (session.isExpired()) {
            removeSession(userToken, sessionId);
            session = getDefaultSession(userToken);
        }

        return session;
    }

    /**
     * @param userToken
     * @param sessionId
     */
    private void removeSession(String userToken, String sessionId) {
        synchronized (lock) {
            if (userToken == null) {
                for (Entry<String, Set<String>> e : sessionsByUser.entrySet()) {
                    if (e.getValue().contains(sessionId)) {
                        userToken = e.getKey();
                        break;
                    }
                }
            }
            userDefaultSessionIds.remove(userToken, sessionId);
            sessionsByUser.computeIfPresent(userToken,
                    (k, v) -> v.stream().filter(s -> !s.equals(sessionId)).collect(toCollection(LinkedHashSet::new)));
            sessions.remove(sessionId);
        }
    }

    @Override
    public SensiNactSession getSession(String sessionId) {
        SensiNactSessionImpl session;
        synchronized (lock) {
            session = sessions.get(sessionId);
        }

        if (session != null && session.isExpired()) {
            removeSession(null, sessionId);
            session = null;
        }

        return session;
    }

    @Override
    public List<String> getSessionIds(String userToken) {
        List<String> ids;
        synchronized (lock) {
            ids = sessionsByUser.getOrDefault(userToken, Set.of()).stream().collect(toList());
        }

        Iterator<String> it = ids.iterator();
        while (it.hasNext()) {
            SensiNactSession session = getSession(it.next());
            if (session == null) {
                it.remove();
            } else if (session.isExpired()) {
                removeSession(userToken, session.getSessionId());
                it.remove();
            }
        }
        return ids;
    }

    @Override
    public SensiNactSession createNewSession(String userToken) {
        SensiNactSessionImpl session = new SensiNactSessionImpl(thread);
        String sessionId = session.getSessionId();

        synchronized (lock) {
            sessionsByUser.merge(userToken, Set.of(sessionId),
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
                    continue;
                } catch (Exception e) {
                    // TODO log this
                }
            }
            removeSession(null, session.getSessionId());
        }
    }
}