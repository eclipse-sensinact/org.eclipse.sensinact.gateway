# The Eclipse sensiNact gateway northbound

The *northbound* API of Eclipse sensiNact gateway refers to the parts of the gateway which interface with users and/or machines to provide access to the digital twin. This is typically using a specific protocol over a more generic transport (e.g. REST/HTTP or JSON-RPC/Websocket).

The gateway currently includes the following northbound providers:

* A [REST interface](RestDataAccess.md) using Jakarta RESTful Web Services
* A [Websocket interface](WebsocketDataAccess.md) using Jetty Websockets
* An [OGC Sensorthings 1.1](https://docs.ogc.org/is/18-088/18-088.html#sensorthings-serviceinterface) [REST interface](SensorthingsRestAccess.md) using Jakarta RESTful Web Services
* An [OGC Sensorthings 1.1](https://docs.ogc.org/is/18-088/18-088.html#receive-mqtt-subscribe) [MQTT interface](SensorthingsMqttAccess.md) using [Moquette](https://github.com/moquette-io/moquette)

