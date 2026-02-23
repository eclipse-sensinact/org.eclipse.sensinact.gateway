# Interacting with Eclipse sensiNact

In this example we will see how to interact with Eclipse sensiNact using the REST interface. This will help you to understand how to navigate the gateway, access the data from sensors, and update values stored in the digital twin.

To start with you will need a runnable gateway configured with REST access. If you don't have one of these then you may wish to consider starting with the earlier examples.

## Listing and Introspecting the gateway

Listing the providers present in the gateway is very simple. Assuming that you're using the configuration from [the previous example](../quick-start/Northbound.md) then issuing a GET request to `http://localhost:8082/sensinact/providers` will return something like:

```json
{
  "type": "PROVIDERS_LIST",
  "uri": "/",
  "statusCode": 200,
  "providers": ["sensiNact"]
}
```

This shows the result was successful, is of type `PROVIDERS_LIST` and the `providers` property contains a single provider `sensiNact`.

The `sensiNact` provider is a built in provider which we can further introspect at `http://localhost:8082/sensinact/providers/sensiNact`

```json
{
  "type": "DESCRIBE_PROVIDER",
  "uri": "/sensiNact",
  "statusCode": 200,
  "response": {
    "name": "sensiNact",
    "services": ["system","admin"]
  }
}
```
This shows us details about the `sensiNact` provider in the form of a `DESCRIBE_PROVIDER` response. We could get similar information by listing the services for the `sensiNact` provider at `http://localhost:8082/sensinact/providers/sensiNact/services`

```json
{
  "type": "SERVICES_LIST",
  "uri": "/sensiNact",
  "statusCode": 200,
  "services": ["system","admin"]}
```

We can further query the resources available for a given service at `http://localhost:8082/sensinact/providers/sensiNact/services/system` or `http://localhost:8082/sensinact/providers/sensiNact/services/system/resources`

```json
{
  "type": "DESCRIBE_SERVICE",
  "uri": "/sensiNact/system",
  "statusCode": 200,
  "response": {
    "name": "system",
    "resources": [
      {"name": "version", "rws": "RO", "type":"SENSOR"},
      {"name": "started", "rws": "RO", "type": "SENSOR"}
    ]
  }
}
```
```json
{
  "type": "RESOURCES_LIST",
  "uri": "/sensiNact/system",
  "statusCode": 200,
  "resources": ["started","version"]
}
```

### Querying resources

When you describe a resource such as `http://localhost:8082/sensinact/providers/sensiNact/services/system/resources` you get a list of all of the possible sensiNact verbs that can be used, and the parameters that they accept:

```json
{
  "type": "DESCRIBE_RESOURCE",
  "uri": "/sensiNact/system/started",
  "statusCode": 200,
  "response": {
    "name": "started",
    "type": "SENSOR",
    "attributes": [],
    "accessMethods":[
      {
        "name": "GET",
        "parameters": [
          {"name": "attributeName", "type": "string", "fixed": false, "constraints": []}
        ]
      },
      {
        "name": "SUBSCRIBE",
        "parameters": [
          {"name": "topics", "type": "array", "fixed": false, "constraints": []},
          {"name": "isDataListener", "type": "boolean", "fixed": false, "constraints":[]},
          {"name": "isMetadataListener", "type": "boolean", "fixed": false, "constraints": []},
          {"name": "isLifecycleListener", "type": "boolean", "fixed": false, "constraints": []},
          {"name": "isActionListener", "type": "boolean", "fixed": false, "constraints": []}
        ]
      },
      {
        "name": "UNSUBSCRIBE",
        "parameters": [
          {"name": "subscriptionId", "type":"string", "fixed":false, "constraints":[]}
        ]
      },
      {
        "name": "SET",
        "parameters": [
          {"name": "value", "type": "java.time.Instant", "fixed": false, "constraints": []}
        ]
      }
    ]
  }
}
```

We can then "GET" the value of the resource from `http://localhost:8082/sensinact/providers/sensiNact/services/system/resources/started/GET`

```json
{
  "type": "GET_RESPONSE",
  "uri": "/sensiNact/system/started",
  "statusCode": 200,
  "response": {
    "name": "started",
    "timestamp": 1693387668421,
    "type": "java.time.Instant",
    "value": "2023-08-30T09:27:48.421487Z"
  }
}
```


## Adding a southbound provider

In order to get more meaningful data from our sensiNact we can deploy a virtual sensor by updating the `configuration/configuration.json` to include the `virtual-temperature-sensor-feature`.

```json
{
  // ...
  "sensinact.launcher": {
    "features": [
      "core-feature",
      "jakarta-servlet-whiteboard-feature",
      "jakarta-rest-whiteboard-feature",
      "northbound-rest-feature",
      "virtual-temperature-sensor-feature"
    ],
    "repository": "repository",
    "featureDir": "features"
  },
  "sensinact.virtual.temperature": {
    "name": "temp1",
    "interval": 5000,
    "latitude": 1.0,
    "longitude": 2.0
  }
  // ...
}
```

We can query the value of the `sensor/temperature` resource as before at `http://localhost:8082/sensinact/providers/temp1/services/sensor/resources/temperature/GET`

```json
{
  "type": "GET_RESPONSE",
  "uri": "/temp1/sensor/temperature",
  "statusCode": 200,
  "response": {
    "name": "temperature",
    "timestamp": 1693394285012,
    "type": "double",
    "value": 29.60935577560438
  }
}
```

### Subscribing to updates

The virtual temperature sensor periodically updates its temperature reading. This could be read by repeatedly polling the value, however this is inefficient, and risks missing changes if the update is more frequent than the polling interval.

The SUBSCRIBE verb allows clients to register so that they can be notified when an update occurs, for the REST interface this is delivered as a Server Sent Event (SSE) stream, but other interfaces (such as Web Socket) may use other push mechanisms.

Making a GET request to `http://localhost:8082/sensinact/providers/temp1/services/sensor/resources/temperature/SUBSCRIBE` will result in a series of `data` events being delivered:

```js
event: data
data: {
  "provider": "temp1",
  "service": "sensor",
  "resource": "temperature",
  "timestamp": 1693402975313,
  "oldValue": 18.758596432570382,
  "newValue": 17.56708764568309
}

event: data
data: {
  "provider": "temp1",
  "service": "sensor",
  "resource": "temperature",
  "timestamp": 1693402980313,
  "oldValue": 17.56708764568309,
  "newValue": 8.2679466364907
}

...
```

In the REST interface unsubscription occurs automatically when the client closes the SSE connection. For other northbound providers the initial subscription response will include a subscription identifier. This identifier can be passed back in an unsubscription request to terminate the delivery of notifications.

### Updating values

If a resource is `MODIFIABLE` that means that it can have its value updated by an end user using the `SET` verb. Unlike the other requests so far a `SET` operation in the REST API is a `PUT`, not a `GET`. The body of the PUT request is an array of parameters with a `name`, `type` and `value`. We can test this using `curl`

```bash
curl -d "[{\"name\": \"value\", \"type\": \"string\", \"value\": \"Lassie\"}]" \
  -H "Content-Type: application/json" \
  http://localhost:8082/sensinact/providers/temp1/services/admin/resources/friendlyName/SET
```

A successful response will look like the following:

```json
{
  "type": "SET_RESPONSE",
  "uri": "/temp1/admin/friendlyName",
  "statusCode": 200,
  "response": {
    "name": "friendlyName",
    "timestamp": 1693407687936,
    "type": "java.lang.String",
    "value":"Lassie"
  }
}
```

After a `SET` subsequent `GET` requests will return the previously set value.

```{note}
If your resource is `FIXED` or `UPDATABLE` then it cannot be set by the northbound API. Also, the behaviour of `SET` requests may be slightly different if your resource is [pull-based](../southbound/custom/whiteboard.md#set-methods) and the device returns a different value.
```
