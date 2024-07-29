# Add a southbound interface

Now that you added a [northbound interface](Northbound.md), you will add a southbound interface to connect your sensiNact gateway to a device.

To achieve this we need to configure the [feature manager](../distribution/Launcher.md#configuring-the-feature-manager) to include the necessary feature(s).

## OpenMeteo

We will connect our sensiNact gateway to the [Open-Meteo](https://open-meteo.com/) API to fetch the current meteo. This API is accessible in JSON format via HTTP.

The easiest way to do that is to get simple JSON data from a public service and let the sensiNact device factory parse it.
The device factory gets raw data from a transport-dependent provider, gives it to a parser then extracts values from the parsed records following the mapping defined in its configuration.

```{warning}
Open-Meteo API is free for non-commercial use, but requests to avoid sending to many daily requests.

You can find more information in their [terms and conditions](https://open-meteo.com/en/terms).
```

The OpenMeteo API provides rest endpoints. We send the following query to get the weather at Grenoble, France:

`https://api.open-meteo.com/v1/forecast?latitude=45.1889&longitude=5.7322&current_weather=true`

The response is a JSON similar to:
```json
{
  "current_weather": {
    "is_day": 1,
    "temperature": 33.5,
    "time": "2023-09-04T14:00",
    "weathercode": 3,
    "winddirection": 53,
    "windspeed": 5.4
  },
  "elevation": 213.0,
  "generationtime_ms": 0.2560615539550781,
  "latitude": 45.18,
  "longitude": 5.7399993,
  "timezone": "GMT",
  "timezone_abbreviation": "GMT",
  "utc_offset_seconds": 0
}
```

## Add the required bundles

The Open-Meteo API returns a JSON object describing the weather at the location and with the details requested in the URL. We will use the JSON sensiNact device factory to parse its result, and the jetty HTTP client to make the query.

We will therefore need to add to our sensiNact distribution:
* the sensiNact device factory core
   * `org.eclipse.sensinact.gateway.southbound.device-factory:device-factory-core:0.0.2-SNAPSHOT`
* the sensiNact device factory JSON parser
   * `org.eclipse.sensinact.gateway.southbound.device-factory:parser-json:0.0.2-SNAPSHOT`
* the sensiNact HTTP device factory and its dependencies (the Jetty HTTP client):
   * `org.eclipse.sensinact.gateway.southbound.http:http-device-factory:0.0.2-SNAPSHOT`
   * `org.eclipse.jetty:jetty-client:11.0.13`
   * `org.eclipse.jetty:jetty-alpn-client:11.0.13`
   * `org.eclipse.jetty:jetty-http:11.0.13`
   * `org.eclipse.jetty:jetty-io:11.0.13`
   * `org.eclipse.jetty:jetty-util:11.0.13`

The easiest way to add those JARs to our sensiNact instance repository is to create a Maven POM file to declare those dependencies, then use a Maven plugin to download them in a Maven local repository layout.

1. Create the file `pom-http-device-factory.xml` file, where will declare a temporary project with our dependencies:
   ```xml
   <project xmlns="http://maven.apache.org/POM/4.0.0"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
     <modelVersion>4.0.0</modelVersion>

     <!-- Temporary project definition -->
     <groupId>org.eclipse.sensinact.doc</groupId>
     <artifactId>http-device-factory-repo</artifactId>
     <version>0.0.2</version>
     <packaging>pom</packaging>
     <name>HTTP device factory repository addition</name>

     <properties>
       <sensinact.version>0.0.2-SNAPSHOT</sensinact.version>
       <jetty.version>11.0.13</jetty.version>
     </properties>

     <dependencies>
       <!-- Eclipse sensiNact Device Factory Core -->
       <dependency>
         <groupId>org.eclipse.sensinact.gateway.southbound.device-factory</groupId>
         <artifactId>device-factory-core</artifactId>
         <version>${sensinact.version}</version>
       </dependency>

       <!-- Eclipse sensiNact Device Factory JSON parser -->
       <dependency>
         <groupId>org.eclipse.sensinact.gateway.southbound.device-factory</groupId>
         <artifactId>parser-json</artifactId>
         <version>${sensinact.version}</version>
       </dependency>

       <!-- HTTP device factory -->
       <dependency>
         <groupId>org.eclipse.sensinact.gateway.southbound.http</groupId>
         <artifactId>http-device-factory</artifactId>
         <version>${sensinact.version}</version>
       </dependency>

       <!-- Jetty client -->
       <dependency>
         <groupId>org.eclipse.jetty</groupId>
         <artifactId>jetty-client</artifactId>
         <version>${jetty.version}</version>
       </dependency>
       <dependency>
         <groupId>org.eclipse.jetty</groupId>
         <artifactId>jetty-alpn-client</artifactId>
         <version>${jetty.version}</version>
       </dependency>
       <dependency>
         <groupId>org.eclipse.jetty</groupId>
         <artifactId>jetty-http</artifactId>
         <version>${jetty.version}</version>
       </dependency>
       <dependency>
         <groupId>org.eclipse.jetty</groupId>
         <artifactId>jetty-io</artifactId>
         <version>${jetty.version}</version>
       </dependency>
       <dependency>
         <groupId>org.eclipse.jetty</groupId>
         <artifactId>jetty-util</artifactId>
         <version>${jetty.version}</version>
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
                 <!-- Configuration to have a Maven repository layout in the target directory -->
                 <useRepositoryLayout>true</useRepositoryLayout>
                 <!-- Add the JARs to the repository folder -->
                 <outputDirectory>repository</outputDirectory>
               </configuration>
             </execution>
           </executions>
         </plugin>
       </plugins>
     </build>
   </project>
   ```
2. Let Maven add the dependencies to the `repository` folder:
   ```bash
   mvn -f pom-http-device-factory.xml prepare-package
   ```
3. You can now remove the `pom-http-device-factory.xml` file

## Define the device-factory feature

Now that we have all the bundles we require, we can add the weather feature in sensiNact.
A feature is the definition of a set of bundles and/or a set of configurations. First we define the device-factory feature.

Add the `features/device-factory-feature.json` file to your sensiNact directory:
```json
{
  "id": "org.eclipse.sensinact.quick-start:device-factory-feature:osgifeature:0.0.2-SNAPSHOT",
  "bundles": [
    { "id": "org.eclipse.sensinact.gateway.southbound.device-factory:device-factory-core:0.0.2-SNAPSHOT" },
    { "id": "org.eclipse.sensinact.gateway.southbound.device-factory:parser-json:0.0.2-SNAPSHOT" },
    { "id": "org.eclipse.sensinact.gateway.southbound.http:http-device-factory:0.0.2-SNAPSHOT" },
    { "id": "org.eclipse.jetty:jetty-client:11.0.13" },
    { "id": "org.eclipse.jetty:jetty-alpn-client:11.0.13" },
    { "id": "org.eclipse.jetty:jetty-http:11.0.13" },
    { "id": "org.eclipse.jetty:jetty-io:11.0.13" },
    { "id": "org.eclipse.jetty:jetty-util:11.0.13" }
  ]
}
```

## Register the device-factory feature
We then register the new feature to sensiNact.

Add the new feature filename to the list of features in `configuration/configuration.json`:
```json
{
  // ...
  "sensinact.launcher": {
    "features": [
      "core-feature",
      "jakarta-servlet-whiteboard-feature",
      "jakarta-rest-whiteboard-feature",
      "northbound-rest-feature",
      "device-factory-feature"
    ],
    "repository": "repository",
    "featureDir": "features"
  },
  // ...
}
```

## Add the weather-grenoble provider
Now that the feature has been defined and registered, we can use it in a new weather-grenoble provider.

Update the `configuration/configuration.json` file:
```json
{
  // ...
  "sensinact.northbound.rest": {
    "allow.anonymous": true
  },
  "sensinact.http.device.factory~weather.grenoble": {
    "tasks.periodic": [
      {
        "period": 600,
        "url": "https://api.open-meteo.com/v1/forecast?latitude=45.1889&longitude=5.7322&current_weather=true",
        "mapping": {
          "parser": "json",
          "mapping": {
            "@provider": {
              "literal": "grenoble-weather"
            },
            "@name": {
              "literal": "Grenoble Weather"
          },
          "@latitude": "latitude",
          "@longitude": "longitude",
          "@elevation": "elevation",
          "@datetime": "current_weather/time",
          "weather/temperature": {
            "path": "current_weather/temperature",
            "type": "float"
          },
          "weather/wind-speed": {
            "path": "current_weather/windspeed",
            "type": "float"
          },
          "weather/wind-direction": {
            "path": "current_weather/winddirection",
            "type": "int"
            },
            "weather/weather-code": "current_weather/weathercode"
          }
        }
      }
    ]
  }
}
```

The above configuration of the HTTP device factory declares a periodic task that:

* will be executed every 600&nbsp;seconds (10&nbsp;minutes)
* will call the URL <https://api.open-meteo.com/v1/forecast?latitude=45.1889&longitude=5.7322&current_weather=true>, which gives the current weather at Grenoble, France.
* maps the response JSON as follows:
   * the result is associated to a fixed provider name: `grenoble-weather`. `@Provider` is a special placeholder to name the provider that holds the data we received, this is the only mandatory field in a mapping definition.
   * the provider friendly (human readable) name will be: `Grenoble Weather`. It is stored using the `@Name` placeholder.
   * the exact location of the weather data point is given by its latitude, longitude and elevation.
   * the weather was measured/computed at the time given in the `time` entry of the `current_weather` object. The timestamp is given as an [ISO 8601 date time](https://en.wikipedia.org/wiki/ISO_8601) (in UTC timezone), that can be given to the device factory using `@datetime` placeholder. Other time-related placeholders are: `@Date`, `@Time` and `@Timestamp` (for Unix timestamps).
   * Finally, we declare the resources we load from the JSON content, using the `<service>/<resource>` placeholder format. Here, all resources are stored in the `weather` service. Note that we didn't declare a model: it will be created dynamically and will have the same name as the provider.

```{seealso}
You can find more details about the HTTP device factory configuration [here](../southbound/http/http-device-factory.md) and about the device factory mapping configuration [here](../southbound/device-factory/core.md#mapping-configuration).
```

### Get the weather from sensiNact

You can (re)start the configured sensiNact instance, using `./start.sh`

:::{note}
On Windows, that would be:
```powershell

& java -D"sensinact.config.dir=configuration" -jar launch\launcher.jar
```
:::

If all went well, you can get the current temperature at Grenoble from sensiNact with the command:

```bash
curl http://localhost:8082/sensinact/providers/grenoble-weather/services/weather/resources/temperature/GET
```

Example response:

```json

{
  "type": "GET_RESPONSE",
  "uri": "/grenoble-weather/weather/temperature",
  "statusCode": 200,
  "response": {
    "name": "temperature",
    "timestamp": 1693839600000,
    "type": "java.lang.Float",
    "value": 33.9
  }
}
```
