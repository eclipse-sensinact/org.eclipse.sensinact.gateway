# MQTT client

Eclipse sensiNact provides a utility callback-based MQTT client.
Multiple MQTT clients can be defined by configuration. They will callback listeners registered using the white board pattern.

## Bundles

The MQTT is provided bu the bundle `org.eclipse.sensinact.gateway.southbound.mqtt:mqtt-client:0.0.2`.

It requires the Eclipse sensiNact core bundles and the Eclipse Paho MQTT client.
* `org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5`

## Configuration

The MQTT client can be created by defining a configuration with PID `sensinact.southbound.mqtt`.

The supported configuration keys are:
* `id`: internal ID of the MQTT client. It will be given to the notified listeners as handler ID
* `client.id`: MQTT client ID, as given to the borker during connection
* `client.reconnect.delay`: MQTT client reconnection delay in milliseconds (500ms by default)
* `host`: host name or IP address of the MQTT broker to connect
* `port`: port of the MQTT broker to connect (1883 by default)
* `topics`: list of topic patterns the client must subscribe to. Joker characters `#` and `+` are supported

Here is an example of configuration of an MQTT client:
```json
{
  "id": "mqtt-handler-example",
  "host": "some.broker.com",
  "port": 1883,
  "client.id": "mqtt.client.id",
  "topics": [ "sensinact/mqtt/test/+" ]
}
```

## MQTT listeners

To be notified of MQTT messages, the listener must be registered as a `org.eclipse.sensinact.gateway.southbound.mqtt.api.IMqttMessageListener` service.
The service can have a `sensinact.mqtt.topics.filters` property that contains an array of topic patterns (as an array of String).

The listener will be detected by all MQTT clients and will be notified if the message topic matches one of the defined topic patterns.
If no topic pattern has been defined, all messsages will be notified.

The listener will be given the internal ID of the MQTT client that received the payload, the message topic, and the description of the message.
The payload can be found in the message description.
The description contains at least the message topic and its payload.

Here is an example of an MQTT client message handler.
```java
@Component(service = {IMqttMessageListener.class}, property = {"sensinact.mqtt.topics.filters=sensinact/mqtt/test/+"})
public class MqttDeviceFactoryHandler implements IMqttMessageListener {
    @Override
    public void onMqttMessage(String handlerId, String topic, IMqttMessage message) {
        System.out.println("Got MQTT message from handler " + handlerId + " on topic " + topic);
        byte[] raw = message.getPayload();
        System.out.println("Content: " + new String(raw));
    }
}
```
