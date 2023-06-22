# Eclipse sensiNact SensorThings MQTT access

 The OGC SensorThings 1.1 specification defines a data model and MQTT interface for accessing data from sensor devices. Eclipse sensiNact includes a northbound adapter which translates from the sensiNact [core data model](../core/DataModel.md) event notifications to the SensorThings API MQTT notifications. The implementation uses [Moquette](https://github.com/moquette-io/moquette) as an MQTT broker.

## Bundles

 The SensorThings REST interface data type bundle is: `org.eclipse.sensinact.gateway.northbound.sensorthings:rest.api:0.0.2`, and the implementation bundle is: `org.eclipse.sensinact.gateway.northbound.sensorthings:mqtt:0.0.2`.

 The SensorThings interface requires the sensinact `core` feature.

# Available endpoints

The SensorThings MQTT interface is available to all those with subscribe access to the MQTT broker. The topics defined are described in [OGC Sensorthings 1.1](https://docs.ogc.org/is/18-088/18-088.html#receive-mqtt-subscribe).

# Basic configuration

The MQTT interface will run without any configuration, however the `jakarta-rest-whiteboard` must have a configuration defined in order to activate. A minimal configuration would therefore be:

```json
{
  ":configurator:resource-version": 1,
  ":configurator:symbolic-name": "org.eclipse.sensinact.gateway.feature.northbound.rest.example",
  ":configurator:version": "0.0.1",
  "sensinact.launcher": {
    "features": [
      "core-feature",
      "northbound-ogc-sensorthings-mqtt-feature"
    ]
  },
  "sensiNact.northbound.sensorthings.mqtt": {}
}
```
## Advanced configuration

 The MQTT broker can be configured using the pid `sensiNact.northbound.sensorthings.mqtt` and the following properties

 * `host` - the host interface to listen on - defaults to `"0.0.0.0"`;
 * `port` - the insecure port - defaults to `1883`, set to `-1` to disable
 * `secure.port` - the secure port - defaults to `8883`, only used if `keystore.file` is set
 * `websocked.enable` - if true then websocket access is enabled - defaults to `true`
 * `websocket.port` - the insecure websocket port - defaults to `8884`, set to `-1` to disable
 * `websocket.secure.port` - the secure websocket port - defaults to `8885`, only used if `keystore.file` is set
 * `keystore.file` - the keystore to use for securing connections
 * `keystore.type` - the keystore type - defaults to `"jks"`
 * `.keystore.password` - the keystore password
 * `.keymanager.password` - the key password


