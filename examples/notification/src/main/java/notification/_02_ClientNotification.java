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
package notification;

import java.util.List;

import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * This component uses the session notification process to be notified of all
 * data events visible by the client.
 */
@Component
public class _02_ClientNotification {

    // This is probably not how we should retrieve the session in real life!
    @Reference
    SensiNactSession session;

    /**
     * ID of the listener we registered
     */
    private String listenerId;

    /**
     * Component is activated: register the listener
     */
    @Activate
    void start() {
        // We ask for * but only events visible to the user will be seen
        // Note that we only register a handler for data notifications
        listenerId = session.addListener(List.of("*"), this::notify, null, null, null);
    }

    /**
     * Component is deactivated: don't forget to unregister the listener
     */
    @Deactivate
    void stop() {
        if (listenerId != null) {
            session.removeListener(listenerId);
            listenerId = null;
        }
    }

    /**
     * Called when a visible data notification is received
     *
     * @param topic Topic of the notification typed event
     * @param event The data event
     */
    private void notify(String topic, ResourceDataNotification event) {
        // TODO Auto-generated method stub
    }
}
