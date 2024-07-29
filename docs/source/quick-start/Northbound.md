# Add a northbound interface

Now that you [downloaded and installed](../setup.md) sensiNact, you need to add a [northbound interface](../northbound/_index.md) for users to communicate with the sensiNact gateway.

To achieve this we need to configure the [feature manager](../distribution/Launcher.md#configuring-the-feature-manager) to include the necessary feature(s).

## The sensiNact configuration file

The sensiNact configuration file format is described [in detail here](../distribution/Launcher.md#the-configuration-file), but the important facts are that:

1. The configuration file lives in the `configuration` folder
2. The configuration file is `JSON` and uses the OSGi<sup>®</sup> [Configuration Resource Format](https://docs.osgi.org/specification/osgi.cmpn/8.0.0/service.configurator.html#d0e132453)
3. The configuration file is *live* and will be reloaded by the gateway whenever you save changes

## Add a simple rest Northbound interface

After you [downloaded and installed](../setup.md) sensiNact, the `configuration/configuration.json` file contains something similar to the following:

```json
{
  ":configurator:resource-version": 1,
  ":configurator:symbolic-name": "org.eclipse.sensinact.gateway.feature.northbound.rest.example",
  ":configurator:version": "0.0.1",
  "sensinact.launcher": {
    "features": [
      "core-feature"
    ],
    "repository": "repository",
    "featureDir": "features"
  }
}
```

This defines the `sensinact.launcher` configuration dictionary with an array of `features` and the locations to search when installing bundles and features.

We need to add the following features to install the REST northbound interface:

1. `jakarta-servlet-whiteboard-feature` - A web container implementing the OSGi<sup>®</sup> servlet whiteboard
2. `jakarta-rest-whiteboard-feature` - A Jakarta RESTful Web Services whiteboard which uses the servlet whiteboard
3. `northbound-rest-feature` - The [northbound REST interface](../northbound/RestDataAccess.md) for Eclipse sensiNact.

We will also configure the REST northbound interface with anonymous access and the local 8082 port.

Update the `configuration/configuration.json` file to match:
```json
{
  ":configurator:resource-version": 1,
  ":configurator:symbolic-name": "org.eclipse.sensinact.gateway.feature.northbound.rest.example",
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
    "osgi.jakartars.name": "sensiNact.rest"
  },
  "sensinact.session.manager": {
    "auth.policy": "ALLOW_ALL"
  },
  "sensinact.northbound.rest": {
    "allow.anonymous": true
  }
}
```

## Start sensiNact

The configuration file is *live* and will be reloaded by the gateway whenever you save changes, so the gateway will automatically install and configure the new features.  Still, you may stop sensiNact and restart it:
```bash
./start.sh
```

## Test the Northbound REST API

You may use your browser at `http://localhost:8082/sensinact/providers/sensiNact`, or use curl to query sensinact.
```
curl http://localhost:8082/sensinact/providers/sensiNact
```

Expected response
```json
{
  "type": "DESCRIBE_PROVIDER",
  "uri": "/sensiNact",
  "statusCode": 200,
  "response": {
    "name": "sensiNact",
    "services": [
      "system",
      "admin"
    ]
  }
}
```

You may want to read the [documentation for the northbound REST interface](../northbound/RestDataAccess.md#available-endpoints), or a [guide to using the REST interface](../examples/Interacting.md).

In the next section, you will [add a southbound interface](Southbound.md) to your sensiNact gateway.


