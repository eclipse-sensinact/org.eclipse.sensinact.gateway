/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package io.moquette.spi.impl;

import io.moquette.parser.proto.messages.PublishMessage;
import io.moquette.parser.proto.messages.SubscribeMessage;
import io.netty.channel.Channel;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleWiring;

/**
 * Class responsible to handle the logic of MQTT protocol it's the director of
 * the protocol execution.
 * <p>
 * Used by the front facing class ProtocolProcessorBootstrapper.
 *
 * @author andrea
 */
public class SensiNactProtocolProcessor extends io.moquette.spi.impl.ProtocolProcessor {

    private final BundleContext bundleContext;

    public SensiNactProtocolProcessor(BundleContext bundleContext) {
        super();
        this.bundleContext = bundleContext;
    }

    public void processPublish(Channel channel, PublishMessage msg) {

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(((BundleWiring) bundleContext.getBundle().adapt(BundleWiring.class)).getClassLoader());
        try {
            super.processPublish(channel, msg);

        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    public void processSubscribe(Channel channel, SubscribeMessage msg) {

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(((BundleWiring) bundleContext.getBundle().adapt(BundleWiring.class)).getClassLoader());
        try {
            super.processSubscribe(channel, msg);

        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

}