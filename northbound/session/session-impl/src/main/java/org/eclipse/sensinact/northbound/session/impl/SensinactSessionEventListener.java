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

import static org.eclipse.sensinact.core.authorization.PermissionLevel.ACT;
import static org.eclipse.sensinact.core.authorization.PermissionLevel.DESCRIBE;
import static org.eclipse.sensinact.core.authorization.PermissionLevel.READ;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sensinact.core.authorization.Authorizer;
import org.eclipse.sensinact.core.notification.ClientActionListener;
import org.eclipse.sensinact.core.notification.ClientDataListener;
import org.eclipse.sensinact.core.notification.ClientLifecycleListener;
import org.eclipse.sensinact.core.notification.ClientMetadataListener;
import org.eclipse.sensinact.core.notification.LifecycleNotification;
import org.eclipse.sensinact.core.notification.ResourceActionNotification;
import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.core.notification.ResourceMetaDataNotification;
import org.eclipse.sensinact.core.notification.ResourceNotification;
import org.osgi.framework.BundleContext;
import org.osgi.service.typedevent.TypedEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SensinactSessionEventListener extends AbstractSensinactSessionEventManager
        implements TypedEventHandler<ResourceNotification> {

    private static final Logger LOG = LoggerFactory.getLogger(SensinactSessionEventListener.class);

    private final ClientLifecycleListener lifecycleListener;
    private final ClientDataListener dataListener;
    private final ClientMetadataListener metadataListener;
    private final ClientActionListener actionListener;

    private final List<String> registeredTopics;


    public SensinactSessionEventListener(String sessionId,
            String subscriptionId, List<String> topics, Authorizer authorizer,
            ClientLifecycleListener lifecycleListener, ClientDataListener dataListener,
            ClientMetadataListener metadataListener, ClientActionListener actionListener) {
        super(sessionId, subscriptionId, topics, authorizer);

        List<String> prefixes = new ArrayList<>(4);

        if(lifecycleListener == null) {
            this.lifecycleListener = this::missingAction;
        } else {
            prefixes.add("LIFECYCLE/");
            this.lifecycleListener = lifecycleListener;
        }

        if(dataListener == null) {
            this.dataListener = this::missingAction;
        } else {
            prefixes.add("DATA/");
            this.dataListener = dataListener;
        }

        if(metadataListener == null) {
            this.metadataListener = this::missingAction;
        } else {
            prefixes.add("METADATA/");
            this.metadataListener = metadataListener;
        }

        if(actionListener == null) {
            this.actionListener = this::missingAction;
        } else {
            prefixes.add("ACTION/");
            this.actionListener = actionListener;
        }

        if(prefixes.isEmpty()) {
            throw new IllegalArgumentException("At least one listener type must be specified");
        }
        registeredTopics = prefixes.stream().flatMap(p -> topics.stream().map(p::concat)).toList();
    }

    public List<String> getRegisteredTopics() {
        return registeredTopics;
    }

    protected void notifyLifecycle(String topic, LifecycleNotification ln) {

        switch(ln.status()) {
            case PROVIDER_CREATED:
            case PROVIDER_DELETED:
                if(!authorizer.hasProviderPermission(DESCRIBE, ln.modelPackageUri(), ln.model(), ln.provider())) {
                    return;
                }
                break;
            case RESOURCE_CREATED:
            case RESOURCE_DELETED:
                if(!authorizer.hasResourcePermission(DESCRIBE, ln.modelPackageUri(), ln.model(), ln.provider(), ln.service(), ln.resource())) {
                    return;
                }
                break;
            case SERVICE_CREATED:
            case SERVICE_DELETED:
                if(!authorizer.hasServicePermission(DESCRIBE, ln.modelPackageUri(), ln.model(), ln.provider(), ln.service())) {
                    return;
                }
                break;
            default:
                LOG.warn("Unrecognised lifecycle status {}. Denying access to the notification", ln.status());
                return;
        }
        lifecycleListener.notify(topic, ln);
    }

    protected void notifyData(String topic, ResourceDataNotification notification) {
        if(!authorizer.hasResourcePermission(READ, notification.modelPackageUri(), notification.model(), notification.provider(), notification.service(), notification.resource())) {
            return;
        }
        dataListener.notify(topic, notification);
    }

    protected void notifyMetdata(String topic, ResourceMetaDataNotification notification) {
        if(!authorizer.hasResourcePermission(READ, notification.modelPackageUri(), notification.model(), notification.provider(), notification.service(), notification.resource())) {
            return;
        }

        metadataListener.notify(topic, notification);
    }

    protected void notifyAction(String topic, ResourceActionNotification notification) {
        if(!authorizer.hasResourcePermission(ACT, notification.modelPackageUri(), notification.model(), notification.provider(), notification.service(), notification.resource())) {
            return;
        }
        actionListener.notify(topic, notification);
    }

    private void missingAction(String topic, ResourceNotification notification) {
        LOG.debug("Event received on topic {} for subscription {} in session {}, but there was no listener registered to handle it",
                topic, subscriptionId, sessionId);
    }
}
