# CSV parser for the device factory

Eclipse sensiNact provides a CSV parser for its device factory.
It uses the parser ID `csv`.

## Bundles

The CSV parser bundle is: `org.eclipse.sensinact.gateway.southbound.device-factory:parser-csv:0.0.2`.

It requires the following bundles to be resolved:
* the device factory core
* the Apache Commons CSV parsing library `org.apache.commons:commons-csv:1.9.0`.

## Parser paths

The CSV data can be mapped using the integer column index as the path, with the first column having index 0.

If the payload has a header, the column name can also be used as path. If multiple columns have the same name, the leftmost one will be used.

**Note:** the CSV parser consider that everything it reads is a string. If necessary, the value type must be given explicitly in the mapping configuration.

For example, consider the following payload with a header:
```csv
Name,Value,Value2,Value
A,1,2,3
```

Here are the paths that can be used:
* `Name`: `"A"`
* `Value`: `"1"`
* `Value2`: `"2"`
* `0`: `"A"`
* `1`: `"1"`
* `2`: `"2"`
* `3`: `"3"`

## Parser configuration

The CSV parser has the ID `csv`.
It accepts the following options:
* `encoding`: the payload encoding, as supported by [`java.nio.charset.Charset`](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/nio/charset/Charset.html), *e.g.* `"UTF-8"`, `"latin-1"`.
* `delimiter`: the CSV delimiter character, *e.g.* ",", ";", "\t".
* `header`: a boolean flag to indicate if the CSV payload has a header

## Example

In this section, we will consider the following payload:
```csv
Date,Time,Name,Latitude,Longitude,Value,Street
20.10.2021,18:14,sample-provider,1.2,3.4,21,Cours Bériat
```

Here is an example configuration to parse that payload:
```json
{
  "parser": "csv",
  "parser.options": {
    "header": true
  },
  "mapping": {
    "@provider": "Name",
    "@latitude": {
      "path": "Latitude",
      "type": "float"
    },
    "@longitude": {
      "path": "Longitude",
      "type": "float"
    },
    "@date": "Date",
    "@time": "Time",
    "data/value": {
      "path": "Value",
      "type": "int"
    },
    "sensor/city": {
      "literal": "Grenoble"
    },
    "sensor/street": {
      "path": "Street",
      "default": "n/a",
      "type": "string"
    }
  },
  "mapping.options": {
    "format.date": "d.M.y",
    "format.time": "H:m"
  }
}
```
