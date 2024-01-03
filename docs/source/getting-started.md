# Quick start

## Overview of sensiNact concepts

* Model/Provider/Service/Resource/Metadata
* Northbound, Southbound providers / Bridges

## Setup the HTTP REST northbound provider

By default, sensiNact doesn't start any endpoint to access its content.
In this example, we will setup a sensiNact instance that will accept anonymous connections on its REST endpoint.

### Step 1: install

Installing sensiNact is as easy as can be: just uncompress the distribution ZIP file.
Here are the commands to do just that:

```bash
mvn dependency:get \
    -DremoteRepositories=https://repo.eclipse.org/content/groups/sensinact/ \
    -Dartifact=org.eclipse.sensinact.gateway.distribution:assembly:0.0.2-SNAPSHOT:zip \
    -Ddest=sensinact.zip

unzip sensinact.zip -d sensinact
```

You now have a sensiNact distribution in the `sensinact` folder, ready to be configured

:::{note}
It is possible that you need to let the start script to be executable on Unix systems:

```bash
chmod +x sensinact/start.sh
```
:::

```{seealso}
You can check the [](./setup.md#installation) section for more options to install sensiNact.
```

### Step 2: configure

[Northbound interfaces](northbound/_index.md) are externally facing components that allow users or machines to interact with the gateway. To add one we need to configure the [feature manager](distribution/Launcher.md#configuring-the-feature-manager) to include the necessary feature(s)

Adding the sensiNact REST interface requires us to install three new features:

1. `jakarta-servlet-whiteboard-feature` - A web container implementing the OSGi<sup>®</sup> servlet whiteboard
2. `jakarta-rest-whiteboard-feature` - A Jakarta RESTful Web Services whiteboard which uses the servlet whiteboard
3. `northbound-rest-feature` - The [northbound REST interface](northbound/RestDataAccess.md) for Eclipse sensiNact.

These features are added as entries in the `features` array of the `sensinact.launcher` configuration.

We can also configure these features to control the port used, and to enable anonymous access for the REST API. The exact configuration properties are defined by the implementations used in the features, for example the Apache Felix Http Jetty bundle in the `jakarta-servlet-whiteboard-feature`.

The updated configuration will look something like this:

```json
{
    ":configurator:resource-version": 1,
    ":configurator:symbolic-name": "org.eclipse.sensinact.gateway.configuration",
    ":configurator:version": "0.0.1",
    "sensinact.launcher": {
        "features": [
            "core-feature",
            "jakarta-servlet-whiteboard-feature",
            "jakarta-rest-whiteboard-feature",
            "northbound-rest-feature"
        ],
        "repository": "repository",
        "featureDir": "features"
    },
    "org.apache.felix.http": {
        "org.osgi.service.http.port": 8082,
        "org.apache.felix.http.name": "sensiNact"
    },
    "JakartarsServletWhiteboardRuntimeComponent": {
        "osgi.jakartars.name": "sensiNact.rest",
        "osgi.http.whiteboard.target": "(org.apache.felix.http.name=sensiNact)"
    },
    "sensinact.northbound.rest": {
        "allow.anonymous": true,
        "osgi.jakartars.whiteboard.target": "(osgi.jakartars.name=sensiNact.rest)"
    }
}
```

```{seealso}
You can take a look to more detailed guide [here](./examples/Configuring.md).
```

### Step 3: interact

Once the configuration is updated and saved the gateway will automatically install and configure the defined list of features.

You can start sensiNact using the start script:

```bash
cd sensinact/
./start.sh
```

:::{note}
On Windows, that would be:

```powershell
cd sensinact/
& java -D"sensinact.config.dir=configuration" -jar launch\launcher.jar
```
:::

You can verify if all went well by querying `http://localhost:8082/sensinact` in your browser, or from the command line by using a tool such as `curl`.

```bash
curl http://localhost:8082/sensinact
curl http://localhost:8082/sensinact/providers/sensiNact/services/system/resources/version/GET
```

The full set of endpoints available is listed in the [documentation for the northbound REST interface](./northbound/RestDataAccess.md#available-endpoints). For a guide to using the REST interface you may also wish to look at the [interacting with sensiNact example](examples/Interacting.md).


## Use the Device Factory to get weather data

Now that you can have access to sensiNact through HTTP, it is time to let it grab data from the outside world.

The easiest way to do that is to get simple JSON data from a public service and let the sensiNact device factory parse it.
The device factory gets raw data from a transport-dependent provider, gives it to a parser then extracts values from the parsed records following the mapping defined in its configuration.
In this example we will parse the current weather of two locations using the [Open-Meteo](https://open-meteo.com/) API, which is accessible in JSON format via HTTP.

```{warning}
Open-Meteo API is free for non-commercial use, but requests to avoid sending to many daily requests.

You can find more information in their [terms and conditions](https://open-meteo.com/en/terms).
```

### Download and add the required bundles

The Open-Meteo API returns a JSON object describing the weather at the location and with the details requested in the URL.

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

### Configure the weather feature

Now that we have all the bundles we require, we can describe the feature we want in sensiNact.
A feature is the definition of a set of bundles and/or a set of configurations.

In this example, we will create a feature to list all the bundles we require and we will add our mapping configuration in the main configuration file.

First, here is the content of the `features/device-factory-feature.json` feature file:
```json
{
  "id": "com.kentyou.datahub.features:device-factory-feature:osgifeature:0.0.2-SNAPSHOT",
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


Next we will need to add the feature to the overall configuration, by adding its base file name to the `features` array of the `sensinact.launcher` entry:
```json
{
  // ...
  "sensinact.launcher": {
    "features": [
      "core-feature",
      "jakarta-servlet-whiteboard-feature",
      "jakarta-rest-whiteboard-feature",
      "northbound-rest-feature",
      "weather-feature"
    ],
    "repository": "repository",
    "featureDir": "features"
  },
  // ...
}
```

Finally, we will add the definition of the HTTP device factory mapping.

```{seealso}
You can find more details about the HTTP device factory configuration [here](./southbound/http/http-device-factory.md) and about the device factory mapping configuration [here](southbound/device-factory/core.md#mapping-configuration).
```

Here is the configuration of the HTTP device factory, where we declare a periodic task that:

* will be executed every 10&nbsp;minutes (600&nbsp;seconds)
* will call the URL <https://api.open-meteo.com/v1/forecast?latitude=45.1889&longitude=5.7322&current_weather=true>, which gives the current weather at Grenoble, France.
  The result JSON file look like this:
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
* will map the response JSON as follows:
   * the result is associated to a fixed provider name: `grenoble_weather`. `@Provider` is a special placeholder to name the provider that holds the data we received, this is the only mandatory field in a mapping definition. Note that model, provider, service and resource names are normalized before being transmitted to the Eclipse sensiNact model. See [here](./southbound/device-factory/core.md#names-handling) for more information.
   * the provider friendly (human readable) name will be: `Grenoble Weather`. It is stored using the `@Name` placeholder.
   * the exact location of the weather data point is given by its latitude, longitude and elevation.
   * the weather was measured/computed at the time given in the `time` entry of the `current_weather` object. The timestamp is given as an [ISO 8601 date time](https://en.wikipedia.org/wiki/ISO_8601) (in UTC timezone), that can be given to the device factory using `@datetime` placeholder. Other time-related placeholders are: `@Date`, `@Time` and `@Timestamp` (for Unix timestamps).
   * Finally, we declare the resources we load from the JSON content, using the `<service>/<resource>` placeholder format. Here, all resources are stored in the `weather` service. Note that we didn't declare a model: it will be created dynamically and will have the same name as the provider.

```json
{
  // ...
  "configurations": {
    // ...
    "sensinact.http.device.factory~weather.grenoble": {
      "tasks.periodic": [
        {
          "period": 600,
          "url": "https://api.open-meteo.com/v1/forecast?latitude=45.1889&longitude=5.7322&current_weather=true",
          "mapping": {
            "parser": "json",
            "mapping": {
              "@provider": {
                "literal": "grenoble_weather"
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
              "weather/wind_speed": {
                "path": "current_weather/windspeed",
                "type": "float"
              },
              "weather/wind_direction": {
                "path": "current_weather/winddirection",
                "type": "int"
              },
              "weather/weather_code": "current_weather/weathercode"
            }
          }
        }
      ]
    }
  }
}
```

### Get the weather from sensiNact

You can (re)start the configured sensiNact instance, using `./start.sh`

:::{note}
On Windows, that would be:
```powershell
& java -D"sensinact.config.dir=configuration" -jar launch\launcher.jar
```
:::

The current temperature at Grenoble should now be accessible using:

```bash
curl http://localhost:8082/sensinact/providers/grenoble_weather/services/weather/resources/temperature/GET
```

And the result will have the following format:

```json
{
  "response": {
    "name": "temperature",
    "timestamp": 1693839600000,
    "type": "java.lang.Float",
    "value": 33.9
  },
  "statusCode": 200,
  "type": "GET_RESPONSE",
  "uri": "/grenoble_weather/weather/temperature"
}
```
