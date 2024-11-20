# Eclipse sensiNact gateway Filtering

The core of the Eclipse sensiNact gateway provides the essential functions for creating and interacting with the "digital twin" of a device. In general there will be many devices represented in the gateway, and clients will often be interested in locating devices based on their current state, rather than by their identifiers.

Filtering is the generic process of requesting devices based on their structure and data values. The sensiNact core provides an interface `ICriterion` which different filter implementations can provide to implement filtering. There are two main ways to use this filter:

1. The core API allows the creation of a *snapshot*, containing the state of zero or more providers, services and resources at a given point in time. When provided with a filter the core gateway will restrict the list of data in the snapshot reducing the overhead of snapshot generation, and the amount of data the caller has to process.

2. The core API generates *notification events* when data in the digital twin changes. A filter can be used to restrict the set of events that are delivered to the listener. 

## Filtering example

For example, given the following devices with the model `climate-sensor`:

* `climate-living-room`
    - admin/friendlyName: Living Room
    - sensor/temperature: 21 °C
    - sensor/humidity: 48 %
* `climate-kitchen`
    - admin/friendlyName: Kitchen
    - sensor/temperature: 24 °C
    - sensor/humidity: 61 %
* `climate-bathroom`
    - admin/friendlyName: Bathroom
    - sensor/temperature: 18 °C
    - sensor/humidity: 77 %
* `climate-bedroom`
    - admin/friendlyName: Bedroom
    - sensor/temperature: 18 °C
    - sensor/humidity: 51 %

The `ICriterion` interface allows filtering at five levels:

* Provider level - Provider level filtering has access to the *model* and *id* of the provider. This can be used for rapid, coarse grained filtering, e.g. to select only providers that use the `climate-sensor` model, or that have ids starting with `climate-`.
* Location level - Location filtering has access to the *location* of the provider. This can be used to identify providers within a particular geographic area. Due to the relative complexity of geodesy, not all filter implementations support location filtering.
* Service level - Service level filtering has access to the names of services in the provider. This can be used for reducing the volume of data contained in the snapshot, e.g. to select only the `sensor` service and ignore the `admin` service.
* Resource level - Resource level filtering is similar to service level filtering, in that it has access to the names of resources in the service. It is also used for reducing the amount of data in the snapshot, e.g. selecting the `temperature` resource and not the `humidity` resource.
* Resource value level - Resource value filtering operates on the value of the resources selected by the other filters, e.g. where the `sensor/humidity` is greater than 60 %. Only resources selected are available to have their values checked.

### Generating a snapshot

A client wishes to know which rooms are *uncomfortable* because they have a temperature over 22 °C or a humidity over 75 %. The filter could be constructed like so:

* A provider filter restricting the model to `climate-sensor` so that only sensors are selected from the gateway
* A service filter selecting the `sensor` service
* A resource filter selecting both the `temperature` and `humidity` sensors
* A resource value filter selecting where `temperature > 22` *or* `humidity > 75`

The result of this filter would be a snapshot containing `climate-kitchen` and `climate-bathroom`, with the `sensor` service and the `temperature` and `humidity` resources present.

### Listening to events

A client wishes to receive notifications whenever a room becomes cold (below 17 °C), so that they can activate a heater.

The filter could be constructed like so:

* A provider filter restricting the model to `climate-sensor` so that only sensors are selected from the gateway
* A service filter selecting the `sensor` service
* A resource filter selecting the `temperature` sensor
* A resource value filter selecting where `temperature <= 17`

An initial snapshot would provide no matching data. Some time later the providers update as follows:

* `climate-living-room`
    - admin/friendlyName: Living Room
    - sensor/temperature: 21 °C
    - sensor/humidity: 51 %
* `climate-kitchen`
    - admin/friendlyName: Kitchen
    - sensor/temperature: 22 °C
    - sensor/humidity: 60 %
* `climate-bathroom`
    - admin/friendlyName: Bathroom
    - sensor/temperature: 18 °C
    - sensor/humidity: 74 %
* `climate-bedroom`
    - admin/friendlyName: Bedroom
    - sensor/temperature: 17 °C
    - sensor/humidity: 53 %

Only one data event matches the filter provided by the client (`climate-bedroom/sensor/temperature`), and so the client receives this notification, allowing it to act.

## Obtaining a Filter

Filters are obtained in different ways depending on the filter language being used, and the object that you have to start with. Usually you can look up a service which will generate an `ICriterion` from your raw filter.

### The Filter Parser service 

One such service is the `IFilterHandler`, which is actually a parser collection. You pass a raw filter expression String, the filter language, and optionally some parser configuration parameters. These will delegate to a real `IFilterParser` instance which does the actual parsing.

## Filter implementations

There are several filter implementations available in Eclipse sensiNact

* [Resource Selectors](./ResourceSelector.md) - Resource Selectors are a native filter type designed to allow efficient selection of sensiNact resources.
* [LDAP](./LDAP.md) - LDAP filters are a commonly used filter type, and sensiNact defines how parts of the snapshot are mapped to keys which can be tested in the LDAP filter
* [Others](./Others.md) - Other filters are used in more limited scopes, and are usually specific to a single domain

```{toctree}
:hidden:

ResourceSelector
LDAP
Others
```