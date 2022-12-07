# The Eclipse sensiNact gateway core

The core of the Eclipse sensiNact gateway provides the essential functions for creating and interacting with the "digital twin" of a device.

The core provides three main things

* The [Eclipse sensiNact data model](DataModel.md) defines the data structures and verbs that apply to the digital twins
* The [Eclipse sensiNact gateway threading model](ThreadingModel.md) defines how threading works within the gateway.
* The [Update](ThreadingModel.md#push-based-providers) and [Notification](ThreadingModel.md#notifications) APIs define how event-based updates and notifications can be managed.
