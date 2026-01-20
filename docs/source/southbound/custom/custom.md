# Custom Southbound Connectors

Building a custom Southbound connector is a relatively simple task, however we recommend that you read about [the sensiNact core model](../../core/CoreModel.md) before you begin. Once you're familiar with the basic concepts of *Provider Models*, *Providers*, *Services* and *Resources* then you're ready to create your own custom connector.

There are two main ways to write a Southbound connector, known as *Pushed* and *Pulled* connectors.

## Push Connectors

A *Push* connector is the simplest (and most common) type of connector. It is known as a *Pushed* connector because it pushes data into the sensiNact digital twin. This push may be triggered by an event (e.g. an incoming MQTT message) or it may be periodic (e.g. gathering sensor data at fixed intervals).

Full details about [*Push* Connectors are available here](./push.md)

## Pull Connectors

A *Pull* connector allows the sensiNact digital twin to request (or *pull*) data updates from the connector. This may be as a result of a direct user request (e.g. a `GET`), or regular updates due to a `SUBSCRIBE` operation. *Pull* connectors operate using a whiteboard.

Full details about [*Pull* Connector whiteboard are available here](./whiteboard.md)

