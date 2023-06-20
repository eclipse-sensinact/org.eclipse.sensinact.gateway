# Tutorial: How to write a custom Device Factory Parser

It is possible and quite easy to create your own device factory parser.

First, choose an available parser ID: it will be used in a JSON configuration file and in a service property so you might want to avoid non-ASCII characters.
In this tutorial, we will consider the parser ID to be `tuto-custom-parser`.
Then, define a device mapping record type implementing the `org.eclipse.sensinact.gateway.southbound.device.factory.IDeviceMappingRecord` interface.
Finally, design your parser keeping in mind that:

* a parser is considered a singleton: it will be called each time a payload associated to that parser is received by the device factory core. Therefore, if you need to be stateful, you should keep track of the state based on the payload emitter.
* a parser must be thread safe: the device factory core might called concurrently by different threads.

## Parser interface

A device factory parser is registered as a service implementing the `org.eclipse.sensinact.gateway.southbound.device.factory.IDeviceMappingParser` interface and associated to two properties:
* `sensinact.southbound.mapping.parser`: the ID of the parser. The constant is defined by `org.eclipse.sensinact.gateway.southbound.device.factory.IDeviceMappingParser.PARSER_ID`
* `sensinact.southbound.mapping.types`: a list of MIME types supported by the parser. This is used if a payload is received with a content type but no parser ID has been configured for that entry. If can be set to `null` to only accept payloads explicitly associated to the parser. The constant is defined by `org.eclipse.sensinact.gateway.southbound.device.factory.IDeviceMappingParser.PARSER_SUPPORTED_TYPES`.

This `IDeviceMappingParser` interface defines only one method:
* ```java
  List<? extends IDeviceMappingRecord> parseRecords(byte[] rawInput, Map<String, Object> parserConfiguration, Map<String, String> context) throws ParserException
  ```

  It accepts the following arguments:
  * `rawInput`: the raw payload as given to the device factory core. Note that the transport that received it might have done some pre-treatment.
  * `parserConfiguration`: configuration for our parser as given in the device factory configuration.
  * `context`: context properties as defined by the transport implementation.

  The method must return a list of objects implementing the `IDeviceMappingRecord` interface.
  Returning an empty of `null` means that the payload didn't contain any value.
  The method can throw a `ParserException` to indicate an error while reading the payload.

## Record interface

The parser will extract records from the payload. Usually, we consider one record per provider update, but it is also possible to have one record per service or even per resource.
Note that it is not possible to have one record for multiple providers.

A parser record must implement the `org.eclipse.sensinact.gateway.southbound.device.factory.IDeviceMappingRecord` interface that defined 2 methods:
* ```java
  Object getField(RecordPath field, DeviceMappingOptionsDTO options)
  ```
  This method returns the field value, converted in the expected type.
  It accepts the following arguments:
  * `field`: path to the value, as defined in the device mapping configuration. This also contains the expected value type, the default value...
  * `options`: device mapping options as given in the configuration: numbers locale, date/time format...

* ```java
  String getFieldString(RecordPath field, DeviceMappingOptionsDTO options)
  ```
  This method returns the field value as a string.
  It accepts the following arguments:
  * `field`: path to the value, as defined in the device mapping configuration. This also contains the expected value type, the default value...
  * `options`: device mapping options as given in the configuration: numbers locale, date/time format...

Each parser should implement its own Record object.

## Real example: parse a custom JSON format

Consider that we are receiving payloads in the following format, similar to what a CSV-to-JSON converter could return:
```json
{
  "sensors": ["a", "b", "c"],
  "temperature": [42, 21, 38],
  "humidity": [0, 30, null]
}
```

Here, we have 3 providers (`a`, `b` and `c`) with two resources each.
We consider that a `null` value indicates that the sensor couldn't send a valid data and we will ignore it, which means that we won't set the value to `null` in the twin, but keep the previous value and its timestamp.

### The record

First, we will implement the record that will hold the values associated to a single provider.
As decided before, we will consider that `null` values will be ignored instead of given to the twin.

```java
import java.time.Instant;

import org.eclipse.sensinact.gateway.southbound.device.factory.Constants;
import org.eclipse.sensinact.gateway.southbound.device.factory.IDeviceMappingRecord;
import org.eclipse.sensinact.gateway.southbound.device.factory.RecordPath;
import org.eclipse.sensinact.gateway.southbound.device.factory.dto.DeviceMappingOptionsDTO;

/**
 * Implementation of a custom mapping record
 */
public class CustomMappingRecord implements IDeviceMappingRecord {

    /**
     * Update timestamp
     */
    private final Instant timestamp;

    /**
     * Provider name
     */
    private final String provider;

    /**
     * Temperature: use an object as it might be null
     */
    private final Float temperature;

    /**
     * Humidity: use an object as it might be null
     */
    private final Float humidity;

    /**
     * The constructor. Its signature depends on how you want to manage the records from the
     * parser.
     */
    public CustomMappingRecord(final Instant timestamp, final String provider, final Float temperature, final Float humidity) {
        this.timestamp = timestamp;
        this.provider = provider;
        this.temperature = temperature;
        this.humidity = humidity;
    }

    /**
     * Returns the field value, converted in the expected type
     *
     * @param field   Field path
     * @param options Mapping options
     * @return The field value, in the expected type
     */
    @Override
    public Object getField(final RecordPath path, final DeviceMappingOptionsDTO options) {
        // Here we only expect a simple path, equivalent to a column name
        // More complex payload could be managed with a more complex path, like the JSON parser
        final String strPath = path.asString();
        switch (strPath) {
            case "time":
                return timestamp;

            case "provider":
                return provider;

            case "temperature":
                // Ignore null value
                return temperature != null ? temperature : Constants.IGNORE;

            case "humidity":
                // Ignore null value
                return humidity != null ? humidity : Constants.IGNORE;

            default:
                // No value: ignore it
                return Constants.IGNORE;
        }
    }

    /**
     * Returns the field value as a string
     *
     * @param field   Field path
     * @param options Mapping options
     * @return The field value as a string
     */
    @Override
    public String getFieldString(final RecordPath path, final DeviceMappingOptionsDTO options) {
        final Object value = getField(path, options);
        // Here, we return null instead of IGNORE as we expect a string result to be converted
        return value != Constants.IGNORE ? String.valueOf(value) : null;
    }
```

### The parser

The second step is the implementation of the parser service.
It will be an OSGi Component (`@Component`) to simplify the tutorial, but it could also be registered as a service using a `BundleContext`.

```java
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import org.eclipse.sensinact.gateway.southbound.device.factory.IDeviceMappingParser;
import org.eclipse.sensinact.gateway.southbound.device.factory.IDeviceMappingRecord;
import org.eclipse.sensinact.gateway.southbound.device.factory.ParserException;
import org.osgi.service.component.annotations.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Implementation of the custom parser
 */
@Component(immediate = true, service = IDeviceMappingParser.class, property = IDeviceMappingParser.PARSER_ID + "=" + CustomParser.PARSER_ID)
public class CustomParser implements IDeviceMappingParser {

    /**
     * Constant that defines the parser ID
     */
    public static final String PARSER_ID = "tuto-custom-parser";

    /**
     * Define a class that represents the data we expect
     */
    final static class DataRecord {
        String[] sensors;
        Float[] temperature;
        Float[] humidity;
    }

    /**
     * JSON parser (Jackson)
     */
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Parse the records found in the given payload
     *
     * @param rawInput            RAW payload
     * @param parserConfiguration Parser configuration
     * @param context             Payload context (MQTT topic, ...)
     * @return The list of parsed records (can be null)
     * @throws ParserException Error parsing payload
     */
    @Override
    public List<? extends IDeviceMappingRecord> parseRecords(final byte[] payload,
            final Map<String, Object> parserConfiguration, final Map<String, String> context) throws ParserException  {
        if (payload.length == 0) {
            // Empty message: ignore
            return List.of();
        }

        // Get message reception timestamp
        final Instant messageTime = Instant.now();

        // Convert the payload to a string, getting the encoding from options
        final Charset charset;
        final String strEncoding = (String) parserConfiguration.get("encoding");
        if (strEncoding != null && !strEncoding.isBlank()) {
            charset = Charset.forName(strEncoding);
        } else {
            charset = StandardCharsets.UTF_8;
        }
        final String strContent = new String(payload, charset);

        // Read the content. Using a Jackson mapper eases the work.
        final DataRecord content = mapper.readValue(strContent, DataRecord.class);

        // Check content validity
        if(content == null || content.sensors == null || content.temperature == null || content.humidity == null) {
            // No content, don't consider it an error
            return List.of();
        } else if(content.sensors.length != content.temperature.length) {
            // Invalid content: we expect as many temperature entries as sensors ones
            throw new ParserException("Sensors and temperature lists don't match");
        } else if(content.sensors.length != content.humidity.length) {
            // Invalid content: we expect as many humidity entries as sensors ones
            throw new ParserException("Sensors and humidity lists don't match");
        }

        // Prepare the records
        final List<CustomRecord> records = new ArrayList<>();
        for(int i = 0 ; i < content.sensors.length; i++) {
            records.add(new CustomRecord(messageTime, content.sensors[i], content.temperature[i], content.humidity[i]))
        }
        return records;
    }
}
```

### The configuration

Finally, we can test the parser with a complement configuration.
Here we consider that the data is sent regularly on an MQTT topic.

First we configure the MQTT client by a configuration with PID `sensinact.southbound.mqtt`:
```json
{
  "id": "mqtt-tuto-parser",
  "host": "some.broker.com",
  "port": 1883,
  "topics": [ "custom/sensors/update" ]
}
```

Then we configure the MQTT device factory and tell it to use our parser by a configuration with PID `sensinact.mqtt.device.factory`:
```json
{
  "mqtt.handler.id": "mqtt-tuto-parser",
  "mapping": {
    "parser": "tuto-custom-parser",
    "parser.options": {
      "charset": "latin-1"
    },
    "mapping": {
      "$name": "provider",
      "@provider": "sensor-${name}",
      "@name": "Sensor ${name}",
      "@timestamp": "time",
      "sensor/temperature": "temperature",
      "sensor/humidity": "humidity"
    }
  }
}
```

With those configurations, the parser will be called by the device factory core each time the MQTT device factory will receive a notification.

### Sample payloads

1. Consider the MQTT client receive the following payload:
   ```json
   {
     "sensors": ["a", "c"],
     "temperature": [40, 38],
     "humidity": [0, 15]
   }
   ```
2. The MQTT client will notify its listeners, here the MQTT device factory, that it received the payload.
3. The MQTT device factory will prepare [context variables](../mqtt/mqtt-device-factory.md#context-variables) based on the notification topic, then call the device factory core with its mapping configuration.
4. The device factory core will look for the best service that matches the configured parser ID, here `tuto-custom-parser`, then call its `parseRecords` method.
5. The device factory parser prepares the records (one per provider, service or resource depending on the implementation) and returns them to the device factory core.
6. For each record, the device factory core will apply the mapping.

   **Note:** Each mapping configuration must at least contain the `@Provider` placeholder. Each record must therefore handle a path that returns a provider name.

   The result will be the update/creation of the following providers:
   * `sensor-a`
   * `admin`
       * `location`: `null` @ 2023-06-20T13:40:00+0200
       * `friendlyName`: `"Sensor a"` @ 2023-06-20T13:40:00+0200
   * `sensor`:
       * `temperature`: 40.0 @ 2023-06-20T13:40:00+0200
       * `humidity`: 0.0 @ 2023-06-20T13:40:00+0200
   * `sensor-c`
   * `admin`
       * `location`: `null` @ 2023-06-20T13:40:00+0200
       * `friendlyName`: `"Sensor c"` @ 2023-06-20T13:40:00+0200
   * `sensor`:
       * `temperature`: 38.0 @ 2023-06-20T13:40:00+0200
       * `humidity`: 15.0 @ 2023-06-20T13:40:00+0200
7. Now consider a second payload:
   ```json
   {
     "sensors": ["a", "b", "c"],
     "temperature": [42, 21, 38],
     "humidity": [0, 30, null]
   }
   ```

   Following the same steps as before, the providers will be updated as:
   * `sensor-a`
     * `admin`
       * `location`: `null` @ 2023-06-20T13:40:00+0200
       * `friendlyName`: `"Sensor a"` @ 2023-06-20T13:50:00+0200
     * `sensor`:
       * `temperature`: 42.0 @ 2023-06-20T13:50:00+0200
       * `humidity`: 0.0 @ 2023-06-20T13:50:00+0200
   * `sensor-b`
     * `admin`
       * `location`: `null` @ 2023-06-20T13:50:00+0200
       * `friendlyName`: `"Sensor b"` @ 2023-06-20T13:50:00+0200
     * `sensor`:
       * `temperature`: 21.0 @ 2023-06-20T13:50:00+0200
       * `humidity`: 30.0 @ 2023-06-20T13:50:00+0200
   * `sensor-c`
     * `admin`
       * `location`: `null` 2023-06-20T13:40:00+0200
       * `friendlyName`: `"Sensor c"` @ 2023-06-20T13:50:00+0200
     * `sensor`:
       * `temperature`: 38.0 @ 2023-06-20T13:50:00+0200
       * `humidity`: 15.0 @ 2023-06-20T13:40:00+0200 (*the value and timestamp haven't been updated*)
