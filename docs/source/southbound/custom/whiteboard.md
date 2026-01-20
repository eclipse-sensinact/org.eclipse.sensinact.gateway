# Pulling updates

Pulled updates differ from pushed updates in that the southbound adapter is called by the sensiNact core to request data, rather than calling the sensiNact core to provide data. This is an important difference when it comes to how you write your southbound connector.

## Whiteboard Services

All pulled updates are implemented using whiteboard services. These services must be registered with the property `sensiNact.whiteboard.resource` set to `true`. This can be easily achieved when writing a DS component by using the `WhiteboardResource` component property annotation.

### Provider Specific services

In general the same whiteboard service is called for all provider instances, however this can be restricted by using the `sensiNact.provider.name` service property to set a collection of provider ids for which this service should be called.

### Resource Methods

Whiteboard services add resources via *Resource Methods*. A *Resource Method* is a method annotated with one the sensiNact verbs and with parameter annotations indicating what parameters should be injected into the method.

The service instance should then have one or more methods annotated with `ACT`, `GET` or `SET`. These methods will be used to register the method with the model, and then be called whenever the relevant verb is used for the resource. All of these annotations are repeatable, meaning that multiple operations can be handled by a single method if that is appropriate, for example all `SET` operations for a model may be handled by the same method.

Resource Methods are typically asynchronous, and if so they should return a `Promise` which resolves with the result when it is available. If the result is not returned as a `Promise` then it will be assumed to be the result.

#### GET Methods

A *Resource Method* annotated with `GET` is called by sensiNact in response to a `GET` request, and is used to supply a value to the client. In order to reduce the burden on the connector and the sensor device results are cached in the digital twin and returned until they become stale. The cache expiry timeout can be set using the `cacheDuration` and `cacheDurationUnit` elements of the `GET` annotation.

In the simplest case the `model`, `service` and `resource` elements of the `GET` annotation can be used to define the resource for which method should be called. Various values can then be injected into the method invocation:

* `@UriParam String s` - Allows the injection of either the full URI, or parts of the URI, representing the GET request (e.g. the provider id)
* `@GetParam TimedValue oldValue` - Allows the injection of either the existing TimedValue from the digital twin, or the `Class` representing the type of the object being retrieved

```java
@GET(model = "myModel", service = "myService", resource = "myResource", cacheDuration = 2, cacheDurationUnit = MINUTES)
public Promise<Integer> getReading(@UriParam(PROVIDER) String provider, @GetParam TimedValue<Integer> oldValue) {
    ...
}
```

##### GET Method Resource Type

The type of the `GET` method is determined by the annotation. If this is not set then the return type of the method is introspected as follows:

1. If the type is a `Promise` then the generic type of the promise is introspected and used for further steps
2. If the type is a `TimedValue` then the value type is introspected and used for further steps
3. If the type is a `Collection` then the resource is defined as `upperBound = -1` and the generic type of the collection is introspected and used for further steps
4. If the type is an array then the resource is defined as `upperBound = -1` and the component type of the array is introspected and used for further steps
5. The type is then used as the type of the resource. If the resource model already exists then the value must be convertible to the resource type.

##### Aggregate GET Method Data

A `GET` method may also return a Custom DTO as used in a push update. This allows multiple resources to be retrieved and updated by a single method. The following restrictions apply:

* The model name must be statically defined, and cannot come from the value of a field in the DTO
* The service names must be statically defined for all data values in the DTO
* The resource names must be statically defined for all data values in the DTO

Whenever any of the resources handled by the DTO are requested then *all* the data returned in the DTO will be added to the digital twin.

#### SET Methods

A Resource Method annotated with `SET` is called by sensiNact in response to a `SET` request, and is used to update a value in the device.

The `model`, `service` and `resource` elements of the `SET` annotation are used to define the resource for which method should be called. The type to be set is determined from the `type` element of the `SET` annotation, or, if not set, from the generic type of the TimedValue received by the resource method. Various values can be injected into the method invocation:

* `@UriParam String s` - Allows the injection of either the full URI, or parts of the URI, representing the GET request (e.g. the provider id)
* `@SetParam TimedValue oldValue` - Allows the injection of either the existing TimedValue from the digital twin, the new value being set, or the `Class` representing the type of the object being set.

The return value from the `SET` method represents the eventual result of the `SET` operation and must either be:

* A `Promise` representing an asynchronous `SET`. The resolved value is then used according to this list.
* `void` - meaning that the `SET` completed with the updated value
* A `TimedValue` representing the updated value and its timestamp.
* The new value of the resource after the `SET`. This will normally be the value passed into the `SET` operation, but may be different if the resource is being updated by multiple users, or for some other reason
* An exception, indicating that the `SET` operation failed

```java
@SET(model = "myModel", service = "myService", resource = "myResource")
public Promise<Integer> getReading(@UriParam(PROVIDER) String provider, @SetParam TimedValue<Integer> newValue) {
    ...
}
```

#### ACT Methods

A Resource Method annotated with `ACT` is called by sensiNact in response to an `ACT` request, and is used to perform an action on a device. Actions differ from `GET` and `SET` requests because the caller is allowed to pass parameters to the action.

The `model`, `service` and `resource` elements of the `ACT` annotation are to define the action resource for which method should be called. The type of the resource is determined from the `type` element of the `ACT` annotation, or, if not set, from the returned by the resource method. Various values can be injected into the method invocation:

* `@UriParam String s` - Allows the injection of either the full URI, or parts of the URI, representing the GET request (e.g. the provider id)
* `@ActParam("foo") String parameter` - Allows customisation of a named parameter. If not set then the parameter name from source is used (if available)

The return value from the `ACT` method represents the eventual result of the `ACT` operation. If a `Promise` is returned the `ACT` will be treated as an asynchronous operation which completes when the `Promise` resolves.

```java
@ACT(model = "myModel", service = "myService", resource = "securityLight")
public Promise<Void> setLight(@ActParam("brightness") Integer b, @ActParam("time") Duration d) {
  ...
}
```

