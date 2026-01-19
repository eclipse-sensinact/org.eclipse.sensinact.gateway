/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.gateway.northbount.sensorthings.mqtt;

import static io.moquette.broker.config.IConfig.HOST_PROPERTY_NAME;
import static io.moquette.broker.config.IConfig.JKS_PATH_PROPERTY_NAME;
import static io.moquette.broker.config.IConfig.KEY_MANAGER_PASSWORD_PROPERTY_NAME;
import static io.moquette.broker.config.IConfig.KEY_STORE_PASSWORD_PROPERTY_NAME;
import static io.moquette.broker.config.IConfig.KEY_STORE_TYPE;
import static io.moquette.broker.config.IConfig.PERSISTENCE_ENABLED_PROPERTY_NAME;
import static io.moquette.broker.config.IConfig.PORT_PROPERTY_NAME;
import static io.moquette.broker.config.IConfig.SSL_PORT_PROPERTY_NAME;
import static io.moquette.broker.config.IConfig.WEB_SOCKET_PORT_PROPERTY_NAME;
import static io.moquette.broker.config.IConfig.WSS_PORT_PROPERTY_NAME;
import static io.netty.handler.codec.mqtt.MqttQoS.AT_MOST_ONCE;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.notification.ResourceNotification;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.typedevent.TypedEventHandler;
import org.osgi.service.typedevent.propertytypes.EventTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.moquette.broker.Server;
import io.moquette.broker.config.IConfig;
import io.moquette.broker.config.MemoryConfig;
import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.messages.InterceptSubscribeMessage;
import io.moquette.interception.messages.InterceptUnsubscribeMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.MqttMessageBuilders;
import io.netty.handler.codec.mqtt.MqttPublishMessage;

@Component(service = TypedEventHandler.class, configurationPid = "sensiNact.northbound.sensorthings.mqtt", configurationPolicy = ConfigurationPolicy.REQUIRE)
@EventTopics({ "DATA/*", "LIFECYCLE/*", "METADATA/*" })
public class SensorthingsMqttNorthbound extends AbstractInterceptHandler
        implements TypedEventHandler<ResourceNotification> {

    private static final Logger LOG = LoggerFactory.getLogger(SensorthingsMqttNorthbound.class);

    /**
     * Access to sensinact gateway
     */
    @Reference
    private GatewayThread gatewayThread;

    private final ObjectMapper mapper = JsonMapper.builder().addModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false).build();

    private final Object lock = new Object();
    private final Map<String, Integer> subscriptionCounts = new HashMap<>();
    private final Map<String, SensorthingsMapper<?>> subscriptions = new HashMap<>();

    private Server mqttBroker;

    /**
     * Executor to queue notifications publications
     */
    private ExecutorService executor;

    public @interface Config {

        String host() default "0.0.0.0";

        int port() default 1883;

        int secure_port() default 8883;

        boolean websocket_enable() default true;

        int websocket_port() default 8884;

        int websocket_secure_port() default 8885;

        String keystore_file() default "";

        String keystore_type() default "jks";

        String _keystore_password() default "";

        String _keymanager_password() default "";

    }

    @Activate
    void start(Config config) throws IOException {
        // Setup the queue handler
        executor = Executors.newSingleThreadExecutor();

        try {
            mqttBroker = new Server();

            Properties props = new Properties();
            props.setProperty(HOST_PROPERTY_NAME, config.host());
            if (config.port() >= 0) {
                props.setProperty(PORT_PROPERTY_NAME, String.valueOf(config.port()));
            }
            if (!config.keystore_file().isBlank()) {
                props.setProperty(SSL_PORT_PROPERTY_NAME, String.valueOf(config.secure_port()));
                if (config.websocket_enable()) {
                    props.setProperty(WSS_PORT_PROPERTY_NAME, String.valueOf(config.websocket_secure_port()));
                }

                props.setProperty(JKS_PATH_PROPERTY_NAME, config.keystore_file());
                props.setProperty(KEY_STORE_TYPE, config.keystore_type());
                props.setProperty(KEY_STORE_PASSWORD_PROPERTY_NAME, config._keystore_password());
                props.setProperty(KEY_MANAGER_PASSWORD_PROPERTY_NAME, config._keymanager_password());
            }
            if (config.websocket_enable() && config.websocket_port() >= 0) {
                props.setProperty(WEB_SOCKET_PORT_PROPERTY_NAME, String.valueOf(config.websocket_port()));
            }
            props.setProperty(PERSISTENCE_ENABLED_PROPERTY_NAME, "false");

            IConfig serverConfig = new MemoryConfig(props);

            mqttBroker.startServer(serverConfig, List.of(this));
        } catch (IOException e) {
            executor.shutdownNow();
            executor = null;
            throw e;
        }
    }

    @Deactivate
    void stop(Config config) {
        if (executor != null) {
            executor.shutdownNow();
            try {
                executor.awaitTermination(500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                LOG.warn("Queued notifications not sent in time");
            }
        }

        mqttBroker.stopServer();
        mqttBroker = null;
        executor = null;
    }

    @Override
    public String getID() {
        return "Eclipse sensiNact Sensorthings subscription listener";
    }

    @Override
    public void onSubscribe(InterceptSubscribeMessage msg) {
        String topicFilter = msg.getTopicFilter();
        if (topicFilter.indexOf('+') != -1 || topicFilter.indexOf('#') != -1) {
            LOG.warn("The topic filter {} contains wildcards which is not supported. It will be ignored");
        }
        synchronized (lock) {
            if (subscriptionCounts.merge(topicFilter, 1, (a, b) -> a + b) == 1) {
                subscriptions.put(topicFilter, SensorthingsMapper.create(topicFilter, mapper, gatewayThread));
            }
        }
    }

    @Override
    public void onUnsubscribe(InterceptUnsubscribeMessage msg) {
        String topicFilter = msg.getTopicFilter();
        synchronized (lock) {
            if (subscriptionCounts.computeIfPresent(topicFilter, (k, v) -> v == 1 ? null : v - 1) == null) {
                subscriptions.remove(topicFilter);
            }
        }
    }

    @Override
    public void notify(String topic, ResourceNotification event) {
        final List<SensorthingsMapper<?>> listeners;
        synchronized (lock) {
            listeners = List.copyOf(subscriptions.values());
            if (listeners.isEmpty()) {
                return;
            }
        }

        for (SensorthingsMapper<?> l : listeners) {
            executor.execute(() -> {
                try {
                    for (Object payload : l.toPayload(event).getValue().collect(Collectors.toList())) {
                        notifyListeners(l.getTopicFilter(), payload);
                    }
                } catch (InvocationTargetException | InterruptedException e) {
                    LOG.error("Error while preparing MQTT payload", e);
                }
            });
        }
    }

    private void notifyListeners(String topic, Object data) {
        try {
            ByteBuf payload = Unpooled.wrappedBuffer(mapper.writeValueAsBytes(data));

            MqttPublishMessage message = MqttMessageBuilders.publish().topicName(topic).qos(AT_MOST_ONCE)
                    .retained(false).payload(payload).build();

            mqttBroker.internalPublish(message, "sensinact.sensorthings");
        } catch (JsonProcessingException e) {
            LOG.warn("An error occurred creating a notification for topic {}", topic, e);
        }
    }

    @Override
    public void onSessionLoopError(Throwable error) {
        LOG.error("An unknown error occurred", error);
    }

}
