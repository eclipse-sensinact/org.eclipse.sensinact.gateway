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
import org.eclipse.sensinact.core.session.SensiNactSession;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Notified for all data events visible to the client
 */
@Component
public class _02_ClientNotification {

    // This is probably not how we should retrieve the session in real life!
    @Reference
    SensiNactSession session;

    @Activate
    void start() {
        // We ask for * but only events visible to the user will be seen
        session.addListener(List.of("*"), this::notify, null, null, null);
    }

    private void notify(String topic, ResourceDataNotification event) {
        // TODO Auto-generated method stub

    }

}
