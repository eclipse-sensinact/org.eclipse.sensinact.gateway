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
* `client.id`: MQTT client ID, as given to the broker during connection
* `client.reconnect.delay`: MQTT client reconnection delay in milliseconds (500ms by default)
* `protocol`: either `tcp` (default) or `ssl` to choose between plain and SSL connection
* `host`: host name or IP address of the MQTT broker to connect
* `port`: port of the MQTT broker to connect (1883 by default)
* `user`: user name
* `.password`: user password
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

### MQTT client TLS authentication

The MQTT client supports certificate authentication, using either a keystore or a set of PEM files.

```{note}
If either a keystore or client certificate is given, the `protocol` configuration will become `ssl` if not explicitly set.
```

Some options are commons to both kinds of keystore:
* `auth.allow.expired`: A boolean flag to indicate that expired certificates are considered valid (false by default).
* `auth.truststore.default.merge`: A boolean flag to indicate if we want to use the Java default trust store in addition to the configured one (true by default).

When relying on a PKCS#12 or a Java (JKS) key store, you can use the following configuration options:
* `auth.keystore.type`: Type of Java keystore, either `JKS` or `PKCS12` (default)
* `auth.keystore.path`: Path to the Java keystore, containing the client certificate
* `.auth.keystore.password`: Password of the Java keystore
* `auth.truststore.type`:  Type of Java trust store, either `JKS` or `PKCS12` (default)
* `auth.truststore.path`: Path to the Java trust store, containing the certificate of the authority that signed the client (and optionally the server) certificate(s).
* `.auth.truststore.password`: Password of the Java trust store

Here is an example:
```json
{
  "id": "mqtt-handler-client-auth",
  "host": "some.broker.com",
  "port": 8883,
  "client.id": "mqtt.client.id",
  "topics": [ "sensinact/mqtt/test/+" ],
  "auth.keystore.type": "PKCS12",
  "auth.keystore.path": "/secure/client.p12",
  ".auth.keystore.password": "secret",
  "auth.truststore.type": "JKS",
  "auth.truststore.path": "/secure/trusted.jks",
  ".auth.truststore.password": "secret"
}
```

When relying on a set of PEM files, you can use the following options:
* `auth.clientcert.path`: Path to the client certificate
* `auth.clientcert.key`: Path to the client private key
* `auth.clientcert.key.algorithm`: Explicit algorithm of the private key (`RSA` by default if not found in the file)
* `.auth.clientcert.key.password`: Password of the client private key
* `auth.clientcert.ca.path`: Path to the certificate of the authority that signed the client certificate, if not included. Also added at the end of the certification chain when loading the client private key.
* `auth.trusted.certs`: Comma-separated list of paths to trusted certificates that are not in the Java default trust store. Note that the certificate authority that signed the client is already considered trusted.

```{note}
If both a keystore and a PEM file client certificate are given, the keystore will be used.
```

Here is an example:
```json
{
  "id": "mqtt-handler-client-auth",
  "host": "some.broker.com",
  "port": 8883,
  "client.id": "mqtt.client.id",
  "topics": [ "sensinact/mqtt/test/+" ],
  "auth.clientcert.ca.path": "/secure/ca.pem",
  "auth.clientcert.path": "/secure/client.crt",
  "auth.clientcert.key": "/secure/client.key",
  "auth.clientcert.key.password": "secret",
  "auth.allow.expired": true
}
```

## MQTT listeners

To be notified of MQTT messages, the listener must be registered as a `org.eclipse.sensinact.gateway.southbound.mqtt.api.IMqttMessageListener` service.
The service can have a `sensinact.mqtt.topics.filters` property that contains an array of topic patterns (as an array of String).

The listener will be detected by all MQTT clients and will be notified if the message topic matches one of the defined topic patterns.
If no topic pattern has been defined, all messages will be notified.

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
