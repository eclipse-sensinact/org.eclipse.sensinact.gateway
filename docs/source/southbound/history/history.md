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

## Recording step

Each record accepted by the filters is handed to the **recording step** of its backend. By
default, this step writes the record to the storage at once, one value at a time. A bundle
can replace it, for example to write in batches or to keep values while the database is
unavailable, by registering a `HistoryRecordingStepFactory` service (package
`org.eclipse.sensinact.gateway.southbound.history.storage`):

* `sensinact.history.provider.name` (optional): the history provider names the factory
  serves; without it, it serves every history provider
* `service.ranking`: when several factories serve a history provider, the highest ranking
  wins

```java
@Component(property = "sensinact.history.provider.name=timescale-history")
public class BatchingStepFactory implements HistoryRecordingStepFactory {

    @Override
    public HistoryRecordingStep create(HistoryRecordingContext context) {
        return new BatchingStep(context.storage());
    }
}
```

`create` receives the provider name and the storage of the history provider through its
context. It is called while the engine holds its lock, so it must return quickly, must not
wait for another service, must not register or unregister services and must not call the
history. It returns `null` to decline a history provider, which is then served by the next
factory. When a history provider is re-registered, the new step is created before the
previous one is closed, so a step that keeps a resource per provider name, such as a file,
must allow for two steps of the same name for a short time.

The step receives the records one by one through `record(HistoricalRecord)`, on the event
handler thread and in the order of delivery:

* `record` must return quickly, never block, and keep the order of the records of one
  resource. A step whose buffer is full drops the record and counts it.
* A record counts as stored when `record` returns normally: it becomes the last stored
  value used by the change conditions of the filters, even if the step writes it later.
* A `RuntimeException` thrown by `record` is logged, and the last stored value is left
  unchanged.
* `close()` is called when the backend stops or when the step is replaced, before the
  storage is released. No record is handed to the step during or after `close`, so this is
  where a step writes what it still holds. The records that follow a replacement go to the
  new step at once, so they may be written before the records the previous step still
  holds.

Factories can be added, changed and removed at runtime: the step of the affected history
providers is replaced without restarting them. When no factory serves a history provider, or
when its factories fail to create a step, the default step is used. The design is described
in the [recording step ADR](./history-recording-step-adr.md).

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
