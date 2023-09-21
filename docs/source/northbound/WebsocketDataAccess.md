# Eclipse sensiNact Websocket interface

 The Websocket interface is a user-facing API for Eclipse sensiNact designed to fit naturally with the [core model](../core/CoreModel.md). The implementation uses Jetty Websockets via the OSGi Servlet Whiteboard specification.

## Bundles

 The Websocket interface bundle is: `org.eclipse.sensinact.gateway.northbound:websocket:0.0.2`, and it makes use of the sensiNact Northbound Query Handler to access sensiNact data.

 The Websocket interface requires the sensinact `core` feature, as well as the `jakarta-servlet-whiteboard` feature. It currently has no authentication.

## Making a Websocket connection

The Websocket interface provides a connection URL at `ws/sensinact/` relative to the root context of the configured Jakarta Servlet whiteboard. It follows the normal upgrade handshake mechanism.

### Websocket commands

 The Websocket commands map closely to those of the Query Handler. Requests are sent as serialized JSON.

### Common Query properties

  The following properties are common to all request types.

#### The Operation

   The `operation` property defines the type of operation being requested and is mandatory.

   * `LIST` - List a set of items, such as the names of services for a given provider
   * `DESCRIBE` - Describe an object, such as a provider or a resource
   * `GET` - Get the value of a resource
   * `SET` - Set the value of a resource
   * `ACT` - ACT on a resource
   * `SUBSCRIBE` - SUBSCRIBE to notifications from a resource or set of resources
   * `UNSUBSCRIBE` - Close an existing subscription

#### The Target URI

   The target uri is set using the String `uri` property and is optional. If set it identifies a provider, service or resource that is targeted by the command. The syntax is `/<provider>[/<service[/resource]]`

#### The Request Id

   The request id is a String set using the `requestId` property and is optional. If set then the same `requestId` will be set on the response which corresponds to the request. This is useful when multiple concurrent requests are made, for example when multiplexing over a single web socket.

### LIST queries

  A List query can optionally define a `filter` property and a `filterLanguage` property which will be used to restrict the data returned by the query.

  The response will be of `type`:

  * `PROVIDERS_LIST` with a list of known provider names contained as an Array property named `providers`
  * `SERVICES_LIST` with a list of known service names contained as an Array property named `services`
  * `RESOURCES_LIST` with a list of known resource names contained as an Array property named `resources`

  Depending on the target `uri` of the request.

### DESCRIBE queries

  A List query can optionally define a `filter` property and a `filterLanguage` property which will be used to restrict the data returned by the query. A list of String `attrs` can also be included when describing providers to request that the `icon`, `location` and `friendlyName` be populated in the response.

  The response will be of `type`:

  * `DESCRIBE_PROVIDER` - a description of provider(s) including an array of `services`, and optionally a `location` and `friendlyName`, contained in a property named `response` (see also `DESCRIBE_SERVICE`)
  * `DESCRIBE_SERVICE` - a description of service(s) including an array of `resources`, contained in a property named `response` (see also `DESCRIBE_RESOURCE`)
  * `DESCRIBE_RESOURCE` - a description of resource(s) including the resource `type` and read write mode (`rws`), contained in a property named `response`

  Depending on the target `uri` of the request.

### GET queries

  A GET query must target a specific resource using its `uri`

  The response will be of `type` `GET_RESPONSE` including the current value of a resource including the resource `type`, `value`, and `timestamp`, contained in a property named `response`

### SET queries

  A SET query must target a specific resource using its `uri` and provide a `value` property containing the value to set

  The response will be of `type` `SET_RESPONSE` including the updated value of a resource including the resource `type`, `value`, and `timestamp`, contained in a property named `response`

### ACT queries

  An ACT query can supply `parameters` as named key/value pairs

  The response will be of `type` `ACT_RESPONSE` - the JSON serialized result of performing an action, contained in a property named `response`

### SUBSCRIBE queries

  A SUBSCRIBE query can optionally define a `filter` property and a `filterLanguage` property which will be used to restrict the data returned in subsequent notifications.

  The response will be of `type` `SUBSCRIPTION_RESPONSE` including a `subscriptionId` property that identifies the subscription. After this response subsequent responses of type `SUBSCRIPTION_NOTIFICATION` will include the `subscriptionId` and `notification`

### UNSUBSCRIBE queries

  An UNSUBSCRIBE query must define a `subscriptionId` property containing the id of the subscription to close.

  The response will be of `type` `UNSUBSCRIPTION_RESPONSE` including a `subscriptionId` property that identifies the subscription that was closed.

## Basic configuration

The Websocket interface will run without any configuration. A minimal configuration would therefore be:

```json
{
  ":configurator:resource-version": 1,
  ":configurator:symbolic-name": "org.eclipse.sensinact.gateway.feature.northbound.rest.example",
  ":configurator:version": "0.0.1",
  "sensinact.launcher": {
    "features": [
      "core-feature",
      "jakarta-servlet-whiteboard-feature",
      "northbound-websocket-feature"
    ]
  },
  "JakartarsServletWhiteboardRuntimeComponent": {}
}
```




