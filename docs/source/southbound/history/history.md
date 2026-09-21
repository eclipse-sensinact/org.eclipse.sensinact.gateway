# Eclipse sensiNact History providers

Eclipse sensiNact can store the history of resource value updates and offer it for querying.
The history support is split into three layers:

* **Storage backends** implement the `HistoryStorage` SPI and register it as an OSGi service.
  sensiNact ships the [PostgreSQL/Timescale backend](./timescale.md) and a non-persistent
  in-memory backend (`org.eclipse.sensinact.gateway.southbound.history:history-inmemory`,
  PID `sensinact.history.inmemory`) for tests and demos.
* The **history engine** (`org.eclipse.sensinact.gateway.southbound.history:history-core`)
  picks up every registered backend, subscribes it to the resource update notifications and
  applies the configured [historization filters](#historization-filters) and
  [housekeeping](#housekeeping).
* Each backend is exposed twice: as a **`HistoryProvider` OSGi service** — the primary
  consumer contract — and as a synthetic sensiNact provider offering the
  [legacy ACT actions](#legacy-act-actions).

Multiple backends can be active at the same time; each is published as its own
`HistoryProvider` with its configured name.

## Stored history

Every stored entry carries:
* the value timestamp (full `Instant` precision);
* the target: model package URI, model, provider, service and resource;
* the value, normalized to one of the `ValueKind`s:
  * `NUMBER` — full-precision numerics, the Java type is preserved on read;
  * `BOOLEAN`;
  * `STRING`;
  * `GEOJSON` — any GeoJSON object, including `FeatureCollection`, stored losslessly;
  * `OBJECT` — other values, stored as canonical JSON together with the type name.

## The HistoryProvider service

Consumers look up the `HistoryProvider` service; with several backends the service property
`sensinact.history.provider.name` selects one by its configured name. The main operations:

* `getValueAt(path, instant)` — the value at or before the given time;
* `getFirstValue(path)` / `getLatestValue(path)`, and the paged variants
  `getFirstValues(path, limit, skip)` / `getLastValues(path, limit, skip)`;
* `getValueCount(path, range)` — the number of stored values in a time range, optionally
  restricted by a value filter;
* `getValues(query)` — the paged range query. A `HistoryQuery` combines a `ResourcePath`,
  a `TimeRange` (explicit bound inclusivity, `millisecondOf` for millisecond-precision
  lookups), a `SortOrder`, offset/limit pagination and an optional `ValueFilter`. The
  returned `HistoryPage` reports whether more data exists (`hasMore`) — there is no magic
  result-count convention;
* `streamValues(query, maxTotal)` — transparent iteration across pages;
* `aggregate(aggregationQuery)` — time-bucketed MIN/MAX/AVG/SUM/COUNT/FIRST/LAST.

Operations beyond plain range queries are capability-gated: `getCapabilities()` reports
`AGGREGATION`, `VALUE_FILTERING` (the backend executes `ValueFilter` conditions, including
filtered counts), `TOTAL_COUNT` and `PRUNING` (the backend can delete stored values, making
[housekeeping](#housekeeping) applicable). Both shipped backends support aggregation, value
filtering and pruning. Page sizes are clamped to the backend's `getMaxPageSize()`.

The [SensorThings northbound](../../northbound/SensorthingsRestAccess.md) uses this contract to
push `$top`/`$skip`/`$orderby` and reducible `$filter` constraints down into the database.

## Historization filters

Which resources are historized is decided per backend by the `include.resources` /
`exclude.resources` selector properties of its configuration (see the
[Timescale provider](./timescale.md#configuration)).

On top of that, **historization filter** configurations refine the decision at runtime,
without touching the backend: each configuration with the factory PID
`sensinact.history.filter` (i.e. `sensinact.history.filter~myfilter`) registers a filter
with the entries:

* `name`: filter name (defaults to the configuration PID)
* `target`: array of history provider names the filter applies to (empty = all)
* `include.resources` / `exclude.resources`: arrays of JSON-encoded resource selectors;
  a resource update is stored only if at least one active filter includes it and no
  active filter excludes it
* `change.mode`: `all` (default), `on-change` (store only when the value changed) or
  `deadband` (store only when the change exceeds a threshold)
* `change.threshold` / `change.threshold.percent`: the deadband — absolute difference or
  percentage relative to the last stored value
* `change.max.interval`: heartbeat (ISO-8601 duration) — store a value regardless of the
  deadband when the last stored value is older than this

Deadband comparisons are always made against the last *stored* value, so a slow drift is
recorded once it accumulates past the threshold. Filter configurations can be added,
changed and removed at runtime; stored data is not affected.

Third parties can plug their own logic by registering a `HistoryIngestFilter` service
(package `org.eclipse.sensinact.gateway.southbound.history.storage`): `targets()` scopes it
to specific providers and `shouldStore(record, lastStored)` takes the storage decision.

## Housekeeping

Stored data can be pruned periodically. Each configuration with the factory PID
`sensinact.history.housekeeping` (i.e. `sensinact.history.housekeeping~cleanup`) defines a
policy with the entries:

* `name`: policy name (defaults to the configuration PID)
* `target`: array of history provider names the policy applies to (empty = all)
* `retention.period`: ISO-8601 duration; values older than this are deleted
* `keep.count`: keep at most this many newest values per resource
* `max.delete`: safety cap on the number of rows a single run may delete (a warning is
  logged when a run hits the cap)
* `schedule.period`: ISO-8601 duration between runs (`PT24H` by default); the first run
  happens one full period after activation

At least one of `retention.period` and `keep.count` is required. Backend-native retention
mechanisms are used where available. Policies only apply to backends declaring the `PRUNING`
capability — a backend that cannot delete, such as an adapter for an external history
service, is skipped with a warning.

## Legacy ACT actions

Before the `HistoryProvider` service existed, history was queried through ACT resources on
the synthetic history provider. These actions remain available unchanged for compatibility,
but new code should use the `HistoryProvider` service — the ACT contract caps results at
500 rows per call and cannot push filters or ordering into the database.

### `history/single` (ACT)

This action returns the value that a resource had at the given time.
If no exact match exists then the most recent value *before* the supplied time will be returned.

It requires the following arguments:

* `provider`: Resource provider name
* `service`: Resource service name
* `resource`: Resource name
* `time`: The zoned date time to check in ISO-8601 format. If `null`, the earliest possible result will be return.

For example, considering the history provider being `sensiNactHistory` and the REST gateway endpoint listening on port 8080, the following payload can be sent with a POST request on `http://localhost:8080/sensinact/providers/sensiNactHistory/services/history/resources/single/ACT` with an application/json content-type header:

```json
{
    "parameters": [
        {
            "name": "provider",
            "type": "string",
            "value": "sensorA"
        },
        {
            "name": "service",
            "type": "string",
            "value": "weather"
        },
        {
            "name": "resource",
            "type": "string",
            "value": "temperature"
        },
        {
            "name": "time",
            "type": "string",
            "value": "2024-08-20T10:23:45+02:00"
        }
    ]
}
```

The result will be in the following format:
```json
{
    "type": "ACT_RESPONSE",
    "uri": "sensiNactHistory/history/single",
    "statusCode": 200,
    "response": {
        "timestamp": "2024-08-20T08:20:00.000Z",
        "value": 26
    }
}
```

### `history/range` (ACT)

This action returns a list of values that a resource had between the given times.

All data values will have timestamps equal to or after `fromTime` and equal to or before `toTime`.
If `fromTime` is `null` then the latest possible results will be returned.
If `toTime` is `null` then results will be returned up to the present.

A maximum of 500 results will be returned.
If more than 500 results exist for the query then the 501st result will be empty (null timestamp and value) to indicate that an additional query is needed to see the full dataset.

This action requires the following arguments:

* `provider`: Resource provider name
* `service`: Resource service name
* `resource`: Resource name
* `fromTime`: The zoned date time to start from. If `null` then the latest values before `toTime` will be returned.
* `toTime`: The zoned date time to finish at. If `null` then there is no finishing time limit.
* `skip`: Number of values to skip in the result set. If `fromTime` is `null` then this will be skipped from the end not the start of the results.


For example, considering the history provider being `sensiNactHistory` and the REST gateway endpoint listening on port 8080, the following payload can be sent with a POST request on `http://localhost:8080/sensinact/providers/sensiNactHistory/services/history/resources/range/ACT`  with an application/json content-type header:

```json
{
    "parameters": [
        {
            "name": "provider",
            "type": "string",
            "value": "sensorA"
        },
        {
            "name": "service",
            "type": "string",
            "value": "weather"
        },
        {
            "name": "resource",
            "type": "string",
            "value": "temperature"
        },
        {
            "name": "fromTime",
            "type": "string",
            "value": "2023-06-10T00:00:00.000Z"
        },
        {
            "name": "toTime",
            "type": "string",
            "value": "2023-06-20T00:00:00.000Z"
        }
    ]
}
```

The result will be in the following format:
```json
{
    "type": "ACT_RESPONSE",
    "uri": "sensiNactHistory/history/range",
    "statusCode": 200,
    "response": [
        {
            "timestamp": "2023-06-10T08:20:00.000Z",
            "value": 18
        },
        {
            "timestamp": "2023-06-10T09:22:00.000Z",
            "value": 19
        },
        {
            "timestamp": "2023-06-10T10:30:00.000Z",
            "value": 20
        },
        {
            "timestamp": "2023-06-10T10:12:00.000Z",
            "value": 19
        }
    ]
}
```

### `history/count` (ACT)

This action returns the number of stored values for a given resource.

It requires the following arguments:

* `provider`: Resource provider name
* `service`: Resource service name
* `resource`: Resource name
* `fromTime`: The zoned date time to start from. If `null` then all values before `toTime` will be counted.
* `toTime`: The zoned date time to finish at. If `null` then there is no finishing time limit.


For example, considering the history provider being `sensiNactHistory` and the REST gateway endpoint listening on port 8080, the following payload can be sent with a `POST` request on `http://localhost:8080/sensinact/providers/sensiNactHistory/services/history/resources/count/ACT` with an application/json content-type header:

```json
{
    "parameters": [
        {
            "name": "provider",
            "type": "string",
            "value": "sensorA"
        },
        {
            "name": "service",
            "type": "string",
            "value": "weather"
        },
        {
            "name": "resource",
            "type": "string",
            "value": "temperature"
        },
        {
            "name": "fromTime",
            "type": "string",
            "value": "2023-06-10T00:00:00.000Z"
        },
        {
            "name": "toTime",
            "type": "string",
            "value": "2023-06-20T00:00:00.000Z"
        }
    ]
}
```

The result will be in the following format:
```json
{
    "type": "ACT_RESPONSE",
    "uri": "sensiNactHistory/history/count",
    "statusCode": 200,
    "response": 4
}
```
