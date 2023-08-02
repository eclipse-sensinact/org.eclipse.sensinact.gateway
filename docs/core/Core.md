# The Eclipse sensiNact gateway core

The core of the Eclipse sensiNact gateway provides the essential functions for creating and interacting with the "digital twin" of a device.

The core provides three main things

* The [Eclipse sensiNact core model](CoreModel.md) defines the verbs and concepts that apply to the core sensiNact digital twin
* The [Eclipse sensiNact data model](data-model/DataModel.md) introduces ways to define a formal data model for your providers
* The [Eclipse sensiNact EMF model](data-model/EMFModel.md) describes how EMF is used within sensiNact to implement the digital twin
* The [Eclipse sensiNact gateway threading model](ThreadingModel.md) defines how threading works within the gateway.
* The [Update](ThreadingModel.md#push-based-providers) and [Notification](ThreadingModel.md#notifications) APIs define how event-based updates and notifications can be managed.
* The [metrics service](./metrics.md).
