# The Eclipse sensiNact gateway southbound

The *southbound* API of Eclipse sensiNact gateway refers to the parts of the gateway which interface with the external device. This may be through a generic protocol (e.g. MQTT/HTTP) or through something custom (e.g. a native serial interface).

The gateway currently includes the following southbound providers:

* [Device factory](./device-factory/core.md)
* HTTP
  * [HTTP device factory](./http/http-device-factory.md)
  * [HTTP callback](./http/http-callback.md)
* MQTT
  * [MQTT client](./mqtt/mqtt-client.md)
  * [MQTT device factory](./mqtt/mqtt-device-factory.md)

The history of resource values is considered to be southbound provider linked to a time-series database:

* sensiNact History
  * Timescale provider
