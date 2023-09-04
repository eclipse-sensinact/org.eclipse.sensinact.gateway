# MQTT Device Factory

Eclipse sensiNact provides an MQTT device factory: received MQTT payloads will be given to the [device factory core](../device-factory/core.md).

## Bundles

The MQTT device factory is provided by: `org.eclipse.sensinact.gateway.southbound.mqtt:mqtt-device-factory:0.0.2`.

It relies on the device factory and on the sensiNact MQTT client and therefore requires the following bundles:
* `org.eclipse.sensinact.gateway.southbound.device-factory:device-factory-core:0.0.2`
* `org.eclipse.sensinact.gateway.southbound.mqtt:mqtt-client:0.0.2`
* `org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5`

Note that the device factory also requires [parsers](../device-factory/core.md#parsers) to work.

## Configuration

The MQTT device factory waits for configurations with PID `sensinact.mqtt.device.factory`.

**Note:** an MQTT client must be configured first with PID `sensinact.southbound.mqtt`. See the documentation of the [MQTT client](./mqtt-client.md) for more information.

The MQTT device factory configuration defines rules to accepts payloads from specific MQTT clients and/or MQTT topics:
* `mapping`: the device factory mapping configuration  (**mandatory**)
* `mqtt.handler.id`: ID of the only accepted source MQTT client. Set to null to accept payloads from any client.
* `mqtt.topics`: List of accepted explicit MQTT topics. MQTT wildcards are not supported. Set to null to accept any topic.

The configuration must define at least one of `mqtt.handler.id` or `mqtt.topics` or it will be rejected.

Here is an example configuration of the MQTT device factory:
```json
{
  "mqtt.handler.id": "mqtt-handler-example",
  "mapping": {
    "parser": "csv",
    "parser.options": {
      "header": true
    },
    "mapping": {
      "@provider": "Name",
      "@latitude": {
        "path": "Latitude",
        "type": "float"
      },
      "@longitude": {
        "path": "Longitude",
        "type": "float"
      },
      "@date": "Date",
      "@time": "Time",
      "data/value": {
        "path": "Value",
        "type": "int"
      }
    }
  }
}
```

The matching MQTT client could be defined with the following configuration:
```json
{
  "id": "mqtt-handler-example",
  "host": "some.broker",
  "port": 1883,
  "client.id": "mqtt.client.id",
  "topics": [ "sensinact/mqtt/test/+" ]
}
```

## Context variables

The MQTT device factory provides different forms of the input topic as context variables.
The MQTT client handler ID is also given in the context.
The device factory core will prefix them automatically with `$context.`.

For example, here are the context variables available for an MQTT payload received on topic `some/nice/topic` by handler `h1`:
* `${context.handlerId}`: `"h1"`
* `${context.topic}`: `"some/nice/topic"`
* `${context.topic-0}`: `"some"`
* `${context.topic-1}`: `"nice"`
* `${context.topic-2}`: `"topic"`
