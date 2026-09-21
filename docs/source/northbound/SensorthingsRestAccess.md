# Eclipse sensiNact SensorThings REST access

 The OGC SensorThings 1.1 specification defines a data model and REST interface for accessing data from sensor devices. Eclipse sensiNact includes a northbound adapter which translates from the sensiNact [core model](../core/CoreModel.md) to the SensorThings API. The implementation uses Jakarta RESTful Web Services via the OSGi Jakarta REST Whiteboard specification.

## Bundles

 The SensorThings REST interface data type bundle is: `org.eclipse.sensinact.gateway.northbound.sensorthings:rest.api:0.0.2`, and the implementation bundle is: `org.eclipse.sensinact.gateway.northbound.sensorthings:rest.gateway:0.0.2`.

 The SensorThings interface requires the sensinact `core` feature, as well as the `jakarta-rest-whiteboard` feature.

## Available endpoints

The SensorThings REST interface is available at `/v1.1` relative to the root context of the REST whiteboard. The endpoints defined are described in [OGC Sensorthings 1.1](https://docs.ogc.org/is/18-088/18-088.html#sensorthings-serviceinterface).

## Basic configuration

The REST interface will run without any configuration, however the `jakarta-rest-whiteboard` must have a configuration defined in order to activate. A minimal configuration would therefore be:

```json
{
  ":configurator:resource-version": 1,
  ":configurator:symbolic-name": "org.eclipse.sensinact.gateway.feature.northbound.rest.example",
  ":configurator:version": "0.0.1",
  "sensinact.launcher": {
    "features": [
      "core-feature",
      "jakarta-servlet-whiteboard-feature",
      "jakarta-rest-whiteboard-feature",
      "northbound-ogc-sensorthings-feature"
    ]
  },
  "JakartarsServletWhiteboardRuntimeComponent": {}
}
```

## History configuration

To retrieve historical data from the SensorThings REST interface, a
[History provider](../southbound/history/history.md) must be deployed. The SensorThings
implementation binds the `HistoryProvider` service directly: when exactly one provider is
available it is used automatically; with several providers, the one to use is named in the
configuration with the PID `sensinact.sensorthings.northbound.rest`:
```
    "sensinact.sensorthings.northbound.rest" : {
        "history.provider": "history"
    }
```

### Query pushdown

Requests for `Observations` and `HistoricalLocations` execute their query options inside
the history database whenever possible: `$top`, `$skip` and a time-field `$orderby`
(`phenomenonTime`/`resultTime`, or `time` for historical locations) become the query's
pagination and ordering, and a `$filter` that reduces to time-range bounds and — if the
provider supports value filtering — numeric `result` comparisons becomes the query's
constraints. For such requests `@iot.count` and the `@iot.nextLink` chain are computed
database-side and cover the **full filtered dataset**. Other filters (`or`, `not`,
functions, string comparisons, non-time `$orderby` fields) are evaluated in memory over
the newest `history.results.max` values.

