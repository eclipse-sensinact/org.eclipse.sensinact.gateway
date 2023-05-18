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
 * Notified for all data events
 */
@Component
@EventTopics("DATA/*")
public class _01_SimpleNotification implements TypedEventHandler<ResourceDataNotification> {

    @Override
    public void notify(String topic, ResourceDataNotification event) {
        // TODO Auto-generated method stub

    }

}
