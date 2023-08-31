# Configuring sensiNact

In order to configure sensiNact you will need to have a launchable gateway. If you haven't already got one then [this example](Download.md) will tell you how to obtain one.

## The sensiNact configuration file

The sensiNact configuration file format is described [in detail here](../distribution/Launcher.md#the-configuration-file), but the important facts are that:

1. The configuration file lives in the `configuration` folder
2. The configuration file is `JSON` and uses the OSGi<sup>®</sup> [Configuration Resource Format](https://docs.osgi.org/specification/osgi.cmpn/8.0.0/service.configurator.html#d0e132453)
3. The configuration file is *live* and will be reloaded by the gateway whenever you save changes

## Adding a Northbound interface

[Northbound interfaces](../northbound/_index.md) are externally facing components that allow users or machines to interact with the gateway. To add one we need to configure the [feature manager](../distribution/Launcher.md#configuring-the-feature-manager) to include the necessary feature(s)

A clean gateway instance will start with a `configuration.json` containing something similar to the following:

```js
"sensinact.launcher": {
  "features": [
    "core-feature"
  ],
  "repository": "repository",
  "featureDir": "features"
}
```

This defines the `sensinact.launcher` configuration dictionary with an array of `features` and the locations to search when installing bundles and features.

Adding the sensiNact REST interface requires us to install three new features:

1. `jakarta-servlet-whiteboard-feature` - A web container implementing the OSGi<sup>®</sup> servlet whiteboard
2. `jakarta-rest-whiteboard-feature` - A Jakarta RESTful Web Services whiteboard which uses the servlet whiteboard
3. `northbound-rest-feature` - The [northbound REST interface](../northbound/RestDataAccess.md) for Eclipse sensiNact.

These features are added as entries in the `features` array of the `sensinact.launcher` configuration.

We can also configure these features to control the port used, and to enable anonymous access for the REST API. The exact configuration properties are defined by the implementations used in the features, for example the Apache Felix Http Jetty bundle in the `jakarta-servlet-whiteboard-feature`.

The updated configuration will look something like this:

```js
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
```

## Accessing the Northbound REST API

Once the configuration is updated and saved the gateway will automatically install and configure the defined list of features. You can verify this by querying `http://localhost:8082/sensinact` in your browser, or from the command line by using a tool such as `curl`.

    curl http://localhost:8082/sensinact

The full set of endpoints available is listed in the [documentation for the northbound REST interface](../northbound/RestDataAccess.md#available-endpoints). For a guide to using the REST interface you may also wish to look at the [interacting with sensiNact example](Interacting.md).
