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
  * `rawInput`: the raw payload as given to the device factory core. Note that the transport that received it might have done some pre-treatment, for example unzipping data that was compressed for transmission.
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

### Setup the project

The custom parser we will create in this section will be structured as a Maven parent project and 2 modules:
  * Parent Maven project: defines commons properties and dependencies versions
    * Implementation project: implementation of the custom device factory parser
    * Feature project: definition of the sensiNact feature JSON file and creation of the dependencies repository

This structure is recommended to write a new module, as it eases the creation of the repository to add your feature, its modules and its dependencies to a sensiNact distribution.

#### Parent POM file

First, we need to create the parent project folder, then named `${PROJECT_ROOT}` in this tutorial.
We then create the the file `${PROJECT_ROOT}/pom.xml` with the following content.
It defines the parent Maven project with the implementation submodule, the common properties and the dependencies versions.
We will add the feature submodule later.

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <!-- Update according to your project -->
  <groupId>org.eclipse.sensinact.doc.tutorial</groupId>
  <artifactId>tuto-device-factory-parser-parent</artifactId>
  <version>0.0.2</version>
  <packaging>pom</packaging>
  <name>Custom Device Factory Parser - Parent</name>
  <description>Parent project of the Eclipse sensiNact sample to write a parser for the Device Factory</description>

  <!-- Definition of the submodules. We'll add the feature module afterwards -->
  <modules>
    <module>parser</module>
  </modules>

  <properties>
    <!-- Build properties: Eclipse sensiNact runs on Java 11+ -->
    <maven.compiler.source>11</maven.compiler.source>
    <maven.compiler.target>11</maven.compiler.target>
    <maven.compiler.release>11</maven.compiler.release>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>

    <!-- Dependencies versions -->
    <sensinact.version>0.0.2</sensinact.version>
    <osgi.ds.version>1.5.0</osgi.ds.version>

    <!-- Plugin dependency versions -->
    <bnd.version>7.0.0-SNAPSHOT</bnd.version>
  </properties>

  <dependencyManagement>
    <dependencies>
      <!-- Eclipse sensiNact Device Factory Core: required for its API -->
      <dependency>
        <groupId>org.eclipse.sensinact.gateway.southbound.device-factory</groupId>
        <artifactId>device-factory-core</artifactId>
        <version>${sensinact.version}</version>
      </dependency>
      <!-- OSGi Declarative Services: the parser will be an OSGi component -->
      <dependency>
        <groupId>org.osgi</groupId>
        <artifactId>org.osgi.service.component.annotations</artifactId>
        <version>${osgi.ds.version}</version>
      </dependency>
    </dependencies>
  </dependencyManagement>

  <build>
    <pluginManagement>
      <plugins>
        <!-- The Bnd Maven plugin generates a MANIFEST.MF file with OSGi headers -->
        <plugin>
          <groupId>biz.aQute.bnd</groupId>
          <artifactId>bnd-maven-plugin</artifactId>
          <version>${bnd.version}</version>
          <executions>
            <execution>
              <goals>
                <goal>bnd-process</goal>
              </goals>
              <configuration>
                <bnd>
                <![CDATA[
                Bundle-SymbolicName: ${project.groupId}.${project.artifactId}
                Bundle-Description: ${project.description}
                -noextraheaders: true
                -reproducible: true
                ]]>
                </bnd>
              </configuration>
            </execution>
          </executions>
        </plugin>
        <!-- Use the Maven JAR plugin to use the MANIFEST.MF file generated by Bnd -->
        <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-jar-plugin</artifactId>
          <version>3.3.0</version>
          <configuration>
            <archive>
              <manifestFile>${project.build.outputDirectory}/META-INF/MANIFEST.MF</manifestFile>
            </archive>
            <skipIfEmpty>true</skipIfEmpty>
          </configuration>
        </plugin>
      </plugins>
    </pluginManagement>
  </build>
</project>
```


#### Implementation POM file

The implementation of the custom parser will be done in the `${PROJECT_ROOT}/parser` folder.

Here is the POM file to construct the parser bundle, defined in `${PROJECT_ROOT}/parser/pom.xml`:
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <!-- Update according to your project -->
  <parent>
    <groupId>org.eclipse.sensinact.doc.tutorial</groupId>
    <artifactId>tuto-device-factory-parser-parent</artifactId>
    <version>0.0.2</version>
    <relativePath>../pom.xml</relativePath>
  </parent>
  <artifactId>tuto-device-factory-parser</artifactId>
  <name>Custom Device Factory Parser - Implementation</name>
  <description>Eclipse sensiNact sample to write a parser for the Device Factory</description>

  <dependencies>
    <!-- Eclipse sensiNact Device Factory Core: required for its API -->
    <dependency>
      <groupId>org.eclipse.sensinact.gateway.southbound.device-factory</groupId>
      <artifactId>device-factory-core</artifactId>
    </dependency>
    <!-- OSGi Declarative Services: the parser will be an OSGi component -->
    <dependency>
      <groupId>org.osgi</groupId>
      <artifactId>org.osgi.service.component.annotations</artifactId>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <!--
        The Bnd Maven plugin generates a MANIFEST.MF file with OSGi headers.
        The plugin gets its configuration from the parent POM file.
      -->
      <plugin>
        <groupId>biz.aQute.bnd</groupId>
        <artifactId>bnd-maven-plugin</artifactId>
      </plugin>
      <!--
        Use the Maven JAR plugin to use the MANIFEST.MF file generated by Bnd.
        The plugin gets its configuration from the parent POM file.
      -->
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-jar-plugin</artifactId>
      </plugin>
    </plugins>
  </build>
</project>
```

### The device mapping record

First, we will implement the record data class that will hold the values the parser associates to a single provider.
This class must implement the interface `org.eclipse.sensinact.gateway.southbound.device.factory.IDeviceMappingRecord` as it will be used by the device factory core.
As decided before, we will consider that `null` values will be ignored instead of given to the twin.

Here is the content of the file `${PROJECT_ROOT}/parser/src/main/java/org/eclipse/sensinact/doc/tutorial/parser/CustomMappingRecord.java`:
```java
package org.eclipse.sensinact.doc.tutorial.parser;

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
}
```

### The parser

The second step is the implementation of the parser service.
It will be an OSGi Component (`@Component`) to simplify the tutorial, but it could also be registered as a service using a `BundleContext`.

Here is the content of the file `${PROJECT_ROOT}/parser/src/main/java/org/eclipse/sensinact/doc/tutorial/parser/CustomParser.java`:
```java
package org.eclipse.sensinact.doc.tutorial.parser;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
     * Define a class that represents the data we expect.
     * Note that all fields found in the JSON input should be public.
     * You can also use Jackson databind annotations to control deserialization
     */
    final static class DataRecord {
        public String[] sensors;
        public Float[] temperature;
        public Float[] humidity;
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
        final DataRecord content;
        try {
            content = mapper.readValue(strContent, DataRecord.class);
        } catch (Exception ex) {
            throw new ParserException("Error parsing mapping record", ex);
        }

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
        final List<CustomMappingRecord> records = new ArrayList<>();
        for (int i = 0; i < content.sensors.length; i++) {
            records.add(new CustomMappingRecord(messageTime, content.sensors[i], content.temperature[i], content.humidity[i]));
        }
        return records;
    }
}
```

#### Prepare the feature and repository

To prepare a sensiNact feature, we first need to choose a unique ID. It is suppose to be in the short artifact description format: `groupId:artifactId:version`.
In this example, we will use the feature ID: `org.eclipse.sensinact.gateway.doc.tutorials.features:custom-parser:osgifeature:0.0.2`.

Then, we have to determine which bundles are required for our project to work.
According to our Maven project dependencies, we need:
* the OSGi Declarative Services annotations bundle
* the device factory core bundle

The Declarative Services bundles are included in the sensiNact gateway core feature.
On the other hand, we will have to indicate in our feature that we need the device factory core bundle.
We should also add any of its transitive dependency, but those are all included in the gateway core.

In our example, we will use an MQTT client, so we also need:
* the sensiNact MQTT client
* the sensiNact MQTT device factory, which links the MQTT client to the device factory core
* the Eclipse Paho client

First, we will update the parent POM file (`${PROJECT_ROOT}/pom.xml`) to add the features modules:
```xml
  <!-- Definition of the submodules -->
  <modules>
    <module>parser</module>
    <module>feature</module>
  </modules>
```

Then we create the feature project POM file: `${PROJECT_ROOT}/feature/pom.xml`:
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <!-- Update according to your project -->
  <parent>
    <groupId>org.eclipse.sensinact.doc.tutorial</groupId>
    <artifactId>tuto-device-factory-parser-parent</artifactId>
    <version>0.0.2</version>
    <relativePath>../pom.xml</relativePath>
  </parent>
  <artifactId>tuto-device-factory-parser-feature</artifactId>
  <packaging>pom</packaging>
  <name>Custom Device Factory Parser - Feature</name>
  <description>Feature to deploy the sample Device Factory Parser to a sensiNact instance</description>

  <!-- Dependencies here are fined in the runtime scope, to be included in the repository -->
  <dependencies>
    <!-- Define our bundle(s) as dependencies, to be included in the repository -->
    <dependency>
      <groupId>org.eclipse.sensinact.doc.tutorial</groupId>
      <artifactId>tuto-device-factory-parser</artifactId>
      <version>${project.version}</version>
      <scope>runtime</scope>
    </dependency>

    <!--
      Eclipse sensiNact Device Factory Core:  required as it is not included in the sensiNact distribution
    -->
    <dependency>
      <groupId>org.eclipse.sensinact.gateway.southbound.device-factory</groupId>
      <artifactId>device-factory-core</artifactId>
      <version>${sensinact.version}</version>
      <scope>runtime</scope>
    </dependency>

    <!-- MQTT device factory and its dependencies, as they are not included in the sensiNact distribution -->
    <dependency>
      <groupId>org.eclipse.paho</groupId>
      <artifactId>org.eclipse.paho.client.mqttv3</artifactId>
      <version>1.2.5</version>
    </dependency>
    <dependency>
      <groupId>org.eclipse.sensinact.gateway.southbound.mqtt</groupId>
      <artifactId>mqtt-client</artifactId>
      <version>${sensinact.version}</version>
    </dependency>
    <dependency>
      <groupId>org.eclipse.sensinact.gateway.southbound.mqtt</groupId>
      <artifactId>mqtt-device-factory</artifactId>
      <version>${sensinact.version}</version>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-dependency-plugin</artifactId>
        <version>3.6.0</version>
        <executions>
          <execution>
            <!-- Copy dependencies of this project in a Maven repository-structured folder -->
            <id>create-feature-repo</id>
            <phase>prepare-package</phase>
            <goals>
              <goal>copy-dependencies</goal>
            </goals>
            <configuration>
              <!--
                It is recommended to exclude transitive dependencies
                and to explicitly declare them as feature dependencies
              -->
              <excludeTransitive>true</excludeTransitive>
              <!--
                Only dependencies declared in the runtime scope will be included.
                This allows to have test dependencies if the feature has integration tests.
              -->
              <includeScope>runtime</includeScope>
              <!-- Configuration to have a Maven repository layout in the target directory -->
              <useRepositoryLayout>true</useRepositoryLayout>
              <outputDirectory>${project.build.directory}/repository</outputDirectory>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
```

Next, we write our feature file as `${PROJECT_ROOT}/feature/src/main/resources/custom-parser-feature.json`.
We declare its ID and list the bundles we need using the short artifact description format:
```json
{
  "id": "org.eclipse.sensinact.gateway.doc.tutorials.features:custom-parser:osgifeature:0.0.2",
  "bundles": [
    { "id": "org.eclipse.sensinact.doc.tutorial:tuto-device-factory-parser:0.0.2" },
    { "id": "org.eclipse.sensinact.gateway.southbound.device-factory:device-factory-core:0.0.2" },
    { "id": "org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5" },
    { "id": "org.eclipse.sensinact.gateway.southbound.mqtt:mqtt-client:0.0.2" },
    { "id": "org.eclipse.sensinact.gateway.southbound.mqtt:mqtt-device-factory:0.0.2" }
  ]
}
```

Finally, we construct the repository using: `mvn clean install`.
The repository can be found in `target/repository` with a Maven layout.

### Expected configuration

In this example, we consider that the data will be sent regularly on an MQTT topic.

First we configure the MQTT client by a configuration with PID `sensinact.southbound.mqtt`:
```json
{
  "id": "mqtt-tuto-parser",
  "host": "some.broker.com",
  "port": 1883,
  "topics": [ "custom/sensors/update" ]
}
```
For this example, we will use the [HiveMQ public MQTT broker](https://www.mqtt-dashboard.com/).

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
      "@provider": {
          "literal": "sensor_${name}"
        },
        "@name": {
          "literal": "Sensor ${name}"
        },
      "@timestamp": "time",
      "sensor/temperature": "temperature",
      "sensor/humidity": "humidity"
    }
  }
}
```

The device factory configuration format is described [here](./core.md#device-factory-configuration).

With those configurations, the parser will be called by the device factory core each time the MQTT device factory will receive a notification from the MQTT client.

### Description of the expected behavior

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
   * `sensor_a`
     * `admin`
       * `location`: `null` @ 2023-06-20T13:40:00+0200
       * `friendlyName`: `"Sensor a"` @ 2023-06-20T13:40:00+0200
     * `sensor`:
       * `temperature`: 40.0 @ 2023-06-20T13:40:00+0200
       * `humidity`: 0.0 @ 2023-06-20T13:40:00+0200
   * `sensor_c`
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
   * `sensor_a`
     * `admin`
       * `location`: `null` @ 2023-06-20T13:40:00+0200
       * `friendlyName`: `"Sensor a"` @ 2023-06-20T13:50:00+0200
     * `sensor`:
       * `temperature`: 42.0 @ 2023-06-20T13:50:00+0200
       * `humidity`: 0.0 @ 2023-06-20T13:50:00+0200
   * `sensor_b`
     * `admin`
       * `location`: `null` @ 2023-06-20T13:50:00+0200
       * `friendlyName`: `"Sensor b"` @ 2023-06-20T13:50:00+0200
     * `sensor`:
       * `temperature`: 21.0 @ 2023-06-20T13:50:00+0200
       * `humidity`: 30.0 @ 2023-06-20T13:50:00+0200
   * `sensor_c`
     * `admin`
       * `location`: `null` 2023-06-20T13:40:00+0200
       * `friendlyName`: `"Sensor c"` @ 2023-06-20T13:50:00+0200
     * `sensor`:
       * `temperature`: 38.0 @ 2023-06-20T13:50:00+0200
       * `humidity`: 15.0 @ 2023-06-20T13:40:00+0200 (*the value and timestamp haven't been updated*)

### Deployment

Now that we have the Maven project and the Java code, we can prepare the deployment of our device factory parser.

#### Package the project

The first step is to install the project in the local Maven repository.
This will ensure it is visible by other Maven projects and ease the creation of the deployment repository.

1. Go to the project root folder
2. Run `mvn clean install`

#### Setup a sensiNact instance

1. Download or generate the Eclipse distribution file
   * To generate it, [clone](https://github.com/eclipse/org.eclipse.sensinact.gateway) and build the project and get the `distribution/assembly/target/gateway.zip` file
2. Decompress it somewhere, we'll call the folder containing `start.sh`: `${SENSINACT_HOME}`.
3. Copy your JSON feature file (`custom-parser-feature.json`) in `${SENSINACT_HOME}/features`
4. Copy the content of `feature/target/repository` in `${SENSINACT_HOME}/repository`
5. Update the configuration of sensiNact by editing the file `${SENSINACT_HOME}/configuration/configuration.json`
   * Add the `"custom-parser-feature"` item in the `features` array of `sensinact.launcher`.
   * In the root object, add configurations of the MQTT client and of the custom parser:
     ```json
      "sensinact.southbound.mqtt~tuto-parser": {
        "id": "mqtt-tuto-parser",
        "host": "broker.hivemq.com",
        "port": 1883,
        "topics": [
          "custom/sensors/update"
        ]
      },
      "sensinact.mqtt.device.factory~tuto-parser": {
        "mqtt.handler.id": "mqtt-tuto-parser",
        "mapping": {
          "parser": "tuto-custom-parser",
          "parser.options": {
            "charset": "latin-1"
          },
          "mapping": {
            "$name": "provider",
            "@provider": {
              "literal": "sensor_${name}"
            },
            "@name": {
              "literal": "Sensor ${name}"
            },
            "@timestamp": "time",
            "sensor/temperature": "temperature",
            "sensor/humidity": "humidity"
          }
        }
      }
     ```
6. To be able to test the project, we will also add the HTTP REST endpoint
   * Add the following items in the `features` array of `sensinact.launcher`:
     * `"jakarta-servlet-whiteboard-feature"`
     * `"jakarta-rest-whiteboard-feature"`
     * `"northbound-rest-feature"`
   * In the root object, add the HTTP REST configuration to ensure it loads correctly and accepts anonymous connections:
     ```json
      "JakartarsServletWhiteboardRuntimeComponent": {
        "osgi.http.whiteboard.target": "(osgi.http.endpoint=*)"
      },
      "sensinact.northbound.rest": {
        "allow.anonymous": true
      }
     ```

After all those steps, the configuration file `${SENSINACT_HOME}/configuration/configuration.json` should look like this:
```json
{
  ":configurator:resource-version": 1,
  ":configurator:symbolic-name": "org.eclipse.sensinact.gateway.feature.gogo.test",
  ":configurator:version": "0.0.1",
  "sensinact.launcher": {
    "features": [
      "core-feature",
      "custom-parser-feature",
      "jakarta-servlet-whiteboard-feature",
      "jakarta-rest-whiteboard-feature",
      "northbound-rest-feature"
    ],
    "repository": "repository",
    "featureDir": "features"
  },
  "sensinact.southbound.mqtt~tuto-parser": {
    "id": "mqtt-tuto-parser",
    "host": "broker.hivemq.com",
    "port": 1883,
    "topics": [
      "custom/sensors/update"
    ]
  },
  "sensinact.mqtt.device.factory~tuto-parser": {
    "mqtt.handler.id": "mqtt-tuto-parser",
    "mapping": {
      "parser": "tuto-custom-parser",
      "parser.options": {
        "charset": "latin-1"
      },
      "mapping": {
        "$name": "provider",
        "@provider": {
          "literal": "sensor_${name}"
        },
        "@name": {
          "literal": "Sensor ${name}"
        },
        "@timestamp": "time",
        "sensor/temperature": "temperature",
        "sensor/humidity": "humidity"
      }
    }
  },
  "JakartarsServletWhiteboardRuntimeComponent": {
    "osgi.http.whiteboard.target": "(osgi.http.endpoint=*)"
  },
  "sensinact.northbound.rest": {
    "allow.anonymous": true
  }
}
```

#### Start the sensiNact instance

* In the `${SENSINACT_HOME}` folder, either run `./start.sh` or the following command:
  `java -D"sensinact.config.dir=configuration" -jar launch/launcher.jar`

* To check the value of a resource, you can use any HTTP client (`curl`, `httPIE`, a browser...).
  With the payload we will send via MQTT, below are the URLs where we will find our resources values.
  Note that accessing those URLs before sending any data will return 404 errors.
  * <http://localhost:8080/sensinact/providers/sensor_a/services/sensor/resources/temperature/GET>
  * <http://localhost:8080/sensinact/providers/sensor_a/services/sensor/resources/humidity/GET>
  * <http://localhost:8080/sensinact/providers/sensor_b/services/sensor/resources/temperature/GET>
  * <http://localhost:8080/sensinact/providers/sensor_b/services/sensor/resources/humidity/GET>

* To send messages, we will use the [MQTT dashboard websocket client](https://www.hivemq.com/demos/websocket-client/):
  1. Connect the broker using the default configuration
  2. Publish the following message on topic `custom/sensors/update`:
     ```json
     {
       "sensors": ["a", "c"],
       "temperature": [40, 38],
       "humidity": [0, 15]
     }
     ```

* You can now access the links above to check if the resources have been created or updated.
  You can also edit the JSON payload to try different values.
