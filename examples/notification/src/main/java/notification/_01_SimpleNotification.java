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

import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.typedevent.TypedEventHandler;
import org.osgi.service.typedevent.propertytypes.EventTopics;

/**
 * This component is registered as typed event handler that listens to all data
 * events. This type of listener is notified of all events from the sensiNact
 * core and is not associated to a session.
 */
@Component // The TypedEventHandler interface will automatically be registered as a service
@EventTopics("DATA/*") // Filter on typed event topics
public class _01_SimpleNotification implements TypedEventHandler<ResourceDataNotification> {

    /**
     * Method called when a typed event with a matching topic is received
     *
     * @param topic Event topic
     * @param event Received data event
     */
    @Override
    public void notify(String topic, ResourceDataNotification event) {
        // TODO Auto-generated method stub
    }
}
