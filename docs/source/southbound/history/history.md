# Eclipse sensiNact History providers

Multiple Eclipse sensiNact History providers can be implemented following the rules of this section.

Currently, Eclipse sensiNact comes with the following provider:
* [Timescale History provider](./timescale.md)

## Stored history

The history provider implementations must be able to store the history of resources with:
* the value timestamp;
* the target: at least the provider, service and resource;
* the value: it must at least support numerical, textual or geo-spatial value;

The implementation can define filters to store value updates only for part of the resources.

## Provider actions

The history provider implementations must have the following actions.
Additional actions can be defined but are out of the scope of this documentation.

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
