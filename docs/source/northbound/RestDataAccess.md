# Eclipse sensiNact REST interface

 The REST interface is a user-facing API for Eclipse sensiNact designed to fit naturally with the [core model](../core/CoreModel.md). The implementation uses Jakarta RESTful Web Services via the OSGi Jakarta REST Whiteboard specification, and follows RESTful principles.

## Bundles

 The REST interface bundle is: `org.eclipse.sensinact.gateway.northbound:rest:0.0.2` and it makes use of the sensiNact Northbound Query Handler to access sensiNact data.

 The REST interface requires the sensinact `core` feature, as well as the `jakarta-rest-whiteboard` feature. It also requires that either:

 * The interface is configured to permit anonymous access
 * An authentication implementation is provided

## Available endpoints

The REST interface provides resources relative to the root context of the configured Jakarta REST whiteboard.

### GET resources
  The following `GET` resources are available:

  * `/sensinact` - The root of the REST interface returns rich details of all available providers in the system
  * `/sensinact/runtime/bundles` - Returns the list of all registered OSGI bundles
  * `/sensinact/providers/` - Returns an array containing the names of all providers in the system
  * `/sensinact/providers/{id}` - Returns rich details of the provider with id `id`
  * `/sensinact/providers/{id}/services` - Returns an array containing the names of all services for the provider with id `id`
  * `/sensinact/providers/{provider}/services/{id}` - Returns rich details of the service with id `id` in provider `provider`
  * `/sensinact/providers/{provider}/services/{id}/resources` - Returns an array containing the names of all resources for the service with id `id` in provider `provider`
  * `/sensinact/providers/{provider}/services/{service}/resources/{id}` - Returns rich details of the resource with id `id` in service `service` with provider `provider`
  * `/sensinact/providers/{provider}/services/{id}/resources/{id}/GET` - Gets the value of a resource
  * `/sensinact/providers/{provider}/services/{id}/resources/{id}/SUBSCRIBE` - Gets a Server Sent Event Stream which is notified for each change to the value of a resource

### POST resources

  * `/sensinact/providers/{provider}/services/{id}/resources/{id}/SET` - Sets the value of a resource
  * `/sensinact/providers/{provider}/services/{id}/resources/{id}/ACT` - Acts on an action resource

### Responses

  All of the defined REST resources return a response body. The response body is used to return information to the client.

#### Response URI

  The `uri` of the response corresponds to the URI of the incoming request, and identifies the provider, service, or resource being returned.

#### Response types

  The `type` of the response is one of:
  * `ERROR` - an error response
  * `COMPLETE_LIST` - a detailed list of all known providers, contained as an Array property named `providers` (see also `DESCRIBE_PROVIDER`)
  * `PROVIDERS_LIST` - a list of known provider names, contained as an Array property named `providers`
  * `SERVICES_LIST` - a list of known service names, contained as an Array property named `services`
  * `RESOURCES_LIST` - a list of known resource names, contained as an Array property named `resources`
  * `DESCRIBE_PROVIDER` - a description of a provider including an array of `services`, and optionally a `location` and `friendlyName`, contained in a property named `response` (see also `DESCRIBE_SERVICE`)
  * `DESCRIBE_SERVICE` - a description of a service including an array of `resources`, contained in a property named `response` (see also `DESCRIBE_RESOURCE`)
  * `DESCRIBE_RESOURCE` - a description of a resource including the resource `type` and read write mode (`rws`), contained in a property named `response`
  * `GET_RESPONSE` - the current value of a resource including the resource `type`, `value`, and `timestamp`, contained in a property named `response`
  * `SET_RESPONSE` - the updated value of a resource including the resource `type`, `value`, and `timestamp`, contained in a property named `response`
  * `ACT_RESPONSE` - the JSON serialized result of performing an action, contained in a property named `response`
  * `SUBSCRIPTION_NOTIFICATION` - For server sent event streams, events will be JSON objects in the `notification` property,

#### Response status codes

 The REST interface resources do not use the HTTP response status code directly unless authentication is required. The HTTP response status code will therefore, in general, be 200. Instead the status of the response is indicated in the `statusCode` property of the response body.

##### Providers and services

   Where providers and services exist, the `statusCode` set will be 200 (OK). Missing providers and missing services will use 404 (Not found).

##### Resources

   If the provider or service for the resource do not exist then a 404 will be returned. When trying to describe or read a resource which exists but has never been given a value then the `statusCode` will be 204 (No content).

   When setting a value to a resource, its whole path is created: if its provider and/or service don't exist, they will be created with default values. Status code based northbound endpoints will return a 200 (OK).

##### Admin resources

   Pre-defined resources of the `admin` service (defined in the data model) should always be considered set, even if they have not been given a value. As a result, when a provider instance is created, its pre-defined resources will be given a default value (can be `null`) and a timestamp (creation time of instance).

   Status code based northbound endpoints will return a 200 (OK) when accessing pre-defined admin resources.

   Access to admin resources follows the same rules as resources from other services.

#### Error responses

  If an error occurs while running a command then the `error` property will be set with a String containing error information.


## Basic configuration

The REST interface will run without any configuration, however the `jakarta-rest-whiteboard` must have a configuration defined in order to activate. A minimal configuration would therefore be:

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
    ]
  },
  "JakartarsServletWhiteboardRuntimeComponent": {}
}
```

This configuration, however, is not very useful. It will return a `503` status for all access as no authentication provider is defined and no anonymous access is permitted. A more useful configuration including an Open ID Connect Authenticator follows:

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
      "northbound-rest-feature",
      "northbound-oidc-authenticator-feature"
    ]
  },
  "JakartarsServletWhiteboardRuntimeComponent": {},
  "sensinact.northbound.auth.oidc~example": {
    "realm": "test",
    "discoveryURL": "http://127.0.0.1:24680/discovery"
  }
}
```

Once an authenticator is present calls to the API will return `401` responses indicating the realm(s) to which the client is able to authenticate

## Authenticating

 The REST interface supports authentication using the `Authorization` request header. The supported scheme(s) depend on the configured authenticators. For example the Open ID Connect Authenticator provides `Bearer` authentication.

 As REST is a stateless model the `Authorization` header must be sent and validated with each request.

### Allowing anonymous access

 In some cases, for example when testing, it is desirable to allow anonymous access to the REST interface. This can be enabled in configuration

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
    ]
  },
  "JakartarsServletWhiteboardRuntimeComponent": {},
  "sensinact.northbound.rest": {
    "allow.anonymous": true
  }
}
```


