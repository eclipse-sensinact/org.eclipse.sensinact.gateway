# HTTP Device Factory

Eclipse sensiNact provides an HTTP device factory: HTTP requests can be sent at regular intervals and their content handled by the [device factory core](../device-factory/core.md).

## Bundles

The HTTP device factory is provided by: `org.eclipse.sensinact.gateway.southbound.http:http-device-factory:0.0.2`.

It relies on the device factory and on the Eclipse Jetty client and therefore requires the following bundles:
* `org.eclipse.sensinact.gateway.southbound.device-factory:device-factory-core:0.0.2`
* `org.eclipse.jetty:jetty-util:11.0.13`
* `org.eclipse.jetty:jetty-io:11.0.13`
* `org.eclipse.jetty:jetty-alpn-client:11.0.13`
* `org.eclipse.jetty:jetty-client:11.0.13`

Note that the device factory also requires [parsers](../device-factory/core.md#parsers) to work.

## Configuration

The HTTP device factory waits for configurations with PID `sensinact.http.device.factory`.

The configuration can describe "one-shot" and "periodic" tasks.
One-shot tasks will be executed when the configuration is created or updated.
They can be used to load the static description of providers, *e.g.* the location of a bus stop.

Here are the available configuration options:
* `mapping`: the device factory mapping configuration (**mandatory**)
* `url`: target URL (**mandatory**)
* `timeout`: HTTP request timeout, in seconds (30s by default)
* `bufferSize`: maximum size of the response payload, in kilo-bytes (512B minimum)
* `method`: HTTP method/verb to use (GET by default)
* `headers`: HTTP request headers
* `http.followRedirect`: follow the HTTP redirection responses (3xx)
* `auth.user`: basic authentication login
* `auth.password`: basic authentication password
* `ssl.ignoreErrors`: ignore SSL errors
* `ssl.keystore`: path to the SSL key store
* `ssl.keystore.password`: password of the SSL key store
* `ssl.truststore`: path to the SSL trust store
* `ssl.truststore.password`: password of the SSL trust store


Periodic tasks use the same configuration as one-shot tasks, with the additional options:
* `period`: number of "period units" to wait between requests
* `period.unit`: the unit of time of the "period" entry, as the name of a [java.time.temporal.ChronoUnit](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/time/temporal/ChronoUnit.html) constant. Defaults to seconds.

Here is an example of configuration for HTTP device factory that will get the description of cycling stations using a one-shot request, while updating their status every minute:
```json
{
  "tasks.oneshot": [
    {
      "url": "xxx-opendata.xxx.pro/opendata/cycling/station_information.json",
      "mapping": {
        "parser": "json",
        "parser.options": {
          "base": "data/stations"
        },
        "mapping": {
          "$station_id": "station_id",
          "@provider": {
            "literal": "cycling-${station_id}"
          },
          "@name": "name",
          "@latitude": "lat",
          "@longitude": "lon",
          "station/capacity": "capacity"
        }
      }
    }
  ],
  "tasks.periodic": [
    {
      "period": 60,
      "url": "https://xxx-opendata.xxx.pro/opendata/cycling/station_status.json",
      "mapping": {
        "parser": "json",
        "parser.options": {
          "base": "data/stations"
        },
        "mapping": {
          "$station_id": "station_id",
          "@provider": {
            "literal": "cycling-${station_id}"
          },
          "station/active": "is_installed",
          "station/stationCode": "stationCode",
          "cycling/docks_available": "num_docks_available",
          "cycling/bikes_available": "num_bikes_available"
        }
      }
    }
  ]
}
```

**Important notes:**
* the order of execution of the requests is not defined.
* avoid periodic tasks with a period less than a second (and be nice to the servers).

## Context variables

The HTTP device factory provides the HTTP response headers as context variables to the device factory.
The device factory core will prefix them automatically with `$context.`.

For example, the response content type will be available in the mapping variables as `${context.Content-Type}`.
