package org.sensinact.mqtt.server.internal;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import io.moquette.interception.HazelcastMsg;
import io.moquette.parser.proto.messages.AbstractMessage;
import io.moquette.parser.proto.messages.PublishMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * Created by mackristof on 28/05/2016.
 */
public class HazelcastListener implements MessageListener<HazelcastMsg> {
    private static final Logger LOG = LoggerFactory.getLogger(HazelcastListener.class);

    private final SensiNactServer server;

    public HazelcastListener(SensiNactServer server) {
        this.server = server;
    }

    public void onMessage(Message<HazelcastMsg> msg) {
        try {
            if (!msg.getPublishingMember().equals(server.getHazelcastInstance().getCluster().getLocalMember())) {
                HazelcastMsg hzMsg = msg.getMessageObject();
                PublishMessage publishMessage = new PublishMessage();
                publishMessage.setTopicName(hzMsg.getTopic());
                publishMessage.setQos(AbstractMessage.QOSType.valueOf(hzMsg.getQos()));
                publishMessage.setPayload(ByteBuffer.wrap(hzMsg.getPayload()));
                publishMessage.setLocal(false);
                publishMessage.setClientId(hzMsg.getClientId());
                server.internalPublish(publishMessage);
            }
        } catch (Exception ex) {
            LOG.error("error polling hazelcast msg queue", ex);
        }
    }
}
