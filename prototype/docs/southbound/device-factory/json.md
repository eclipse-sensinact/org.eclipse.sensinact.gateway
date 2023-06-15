# JSON parser for the device factory

Eclipse sensiNact provides a JSON parser for its device factory.
It uses the parser ID `json`.

## Bundles

The JSON parser bundle is: `org.eclipse.sensinact.gateway.southbound.device-factory:parser-json:0.0.2`.

It requires the following bundles to be resolved:
* the device factory core
* the Jackson JSON data bind library:
  * `com.fasterxml.jackson.core:jackson-annotations:2.14.0`
  * `com.fasterxml.jackson.core:jackson-core:2.14.0`
  * `com.fasterxml.jackson.core:jackson-databind:2.14.0`

## Parser paths

The JSON parser accepts key names for objects and integers for arrays.
They can be combined as a path string.

The parsed value keep their JSON type: strings numbers and booleans can be stored as is in the resource.

For example, considering the following JSON payload:
```json
{
    "name": "Some name",
    "properties": [
        {
            "name": "reserved",
            "value": null
        },
        {
            "name": "serial",
            "value": 42
        }
    ]
}
```

The accepted paths would be:
* `name`: `"Some name"`
* `properties/1/name`: `"serial"`
* `properties/1/value`: `42`

## Parser configuration

The JSON parser has the ID `json`.
It accepts the following options:
* `encoding`: the payload encoding, as supported by [`java.nio.charset.Charset`](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/nio/charset/Charset.html), *e.g.* `"UTF-8"`, `"latin-1"`.
**Note:** The UTF-8 Byte-Order-Mark (BOM) is ignored if found.
* `base`: the payload base to consider for the mapping, as described below.

### Payload base

A JSON payload can be complex and can describe multiple providers at once or can have the provider description deep inside its hierarchy.

Defining a payload base allows to handle those cases.
The base is defined with a parser path that can walk through object entries or specific array indices. The JSON parser doesn't support walking through all entries of an array.

If the base targets an array, the mapping will be applied for each item individually.

In the first example, we'll consider the following payload, containing an array of arrays of objects:
```json
[
  [
    {
      "Date": "20.10.2021",
      "Time": "18:14",
      "Name": "JsonSubArray1",
      "Latitude": 1.2,
      "Longitude": 3.4,
      "Value": 94
    },
    {
      "Date": "20.10.2021",
      "Time": "18:17",
      "Name": "JsonSubArray2",
      "Latitude": 5.6,
      "Longitude": 7.8,
      "Value": 28
    }
  ],
  [
    {
      "Date": "01.01.1970",
      "Time": "00:01",
      "Name": "JsonSubArray-Ignore",
      "Latitude": 9.1,
      "Longitude": 2.3,
      "Value": 12345
    }
  ]
]
```

Here we define the `base` to be `0`, *i.e.* to work as if the payload was the first array of the 2 arrays contained in the root array:
```json
{
  "parser": "json",
  "parser.options": {
    "base": "0"
  },
  "mapping": {
    "@provider": "Name",
    "data/value": "Value",
    "@latitude": "Latitude",
    "@longitude": "Longitude",
    "@altitude": "Altitude",
    "@date": "Date",
    "@time": "Time"
  },
  "mapping.options": {
    "format.date": "d.M.y",
    "format.time": "H:m"
  }
}
```

The parser would consider each array under that base as a provider update, and would update providers `JsonSubArray1` and `JsonSubArray2`. The other description would be ignored.

If we consider a more complex JSON payload, like the following one:
```json
{
  "nhits": 51,
  "parameters": {
    "dataset": "test2",
    "timezone": "UTC",
    "facet": [ "date", "name", "type", "value" ]
  },
  "records": [
    {
      "datasetid": "test2",
      "recordid": "14a489fe93443d9febd13971d5b24e976f852eec",
      "fields": {
        "name": "Sensor O",
        "id": "1452",
        "type": "GOOSE",
        "value": 0,
        "date": "2021-10-26T15:28:00+00:00",
        "geo": [ 48.849577, 2.350867 ]
      },
      "record_timestamp": "2021-10-26T15:29:08.889000+00:00"
    },
    {
      "datasetid": "test2",
      "recordid": "69a8e6410e64b573c9b2e20a40f5987615088ca6",
      "fields": {
        "name": "Sensor R",
        "id": "1851",
        "type": "DUCK",
        "value": 7,
        "date": "2021-10-26T15:27:00+00:00",
        "geo": [ 48.858396, 2.350484 ]
      },
      "record_timestamp": "2021-10-26T15:29:03.729000+00:00"
    }
  ]
}
```

We can define the following mapping, using the array under the `records` key as base:
```json
{
  "parser": "json",
  "parser.options": {
    "base": "records"
  },
  "mapping": {
    "@provider": "fields/id",
    "@name": "fields/name",
    "@datetime": "fields/date",
    "@latitude": "fields/geo/0",
    "@longitude": "fields/geo/1",
    "$type": "fields/type",
    "data/${type}_value": "fields/value"
  }
}
```
