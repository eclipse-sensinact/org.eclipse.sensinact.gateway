/*********************************************************************
* Copyright (c) 2024 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.gateway.commands.gogo;

import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.session.SensiNactSession;
import org.eclipse.sensinact.core.session.SensiNactSessionManager;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.promise.Promise;

/**
 * We want to reuse the same session for all of the commands,
 * so it is easier to have a single reference to this class, rather than
 * trying to coordinate access to the same service from each of the
 * separate components in this bundle.
 *
 * @author David Leangen
 */
@Component(service = SensiNactCommandSession.class)
public class SensiNactCommandSession {

    @Reference private SensiNactSessionManager sessionManager;
    private SensiNactSession session;

    // Not technically a session, but still a good place to keep this reference
    @Reference private DataUpdate push;

    @Activate
    void activate() {
        session = sessionManager.createNewAnonymousSession();
    }

    /**
     * Returns the command session. Value will never be null;
     */
    public SensiNactSession get() {
        // TODO: Can the session expire?
        //       What to do in that case??
        return session;
    }

    public Promise<?> push(Object o) {
        if (o == null)
            throw new NullPointerException("Cannot push a null object");

        // TODO: What else could go wrong here??
        return push.pushUpdate(o);
    }
}
