# Custom Southbound Connectors

Building a custom Southbound connector is a relatively simple task, however we recommend that you read about [the sensiNact core model](../../core/CoreModel.md) before you begin. Once you're familiar with the basic concepts of *Provider Models*, *Providers*, *Services* and *Resources* then you're ready to create your own custom connector.

## Pushing updates

Pushing updates is the simplest way to set data in the sensiNact digital twin. Updates are pushed using the `PrototypePush` service which allows update objects to be sent to sensiNact.

### Using a Generic DTO

The `GenericDTO` allows data updates to be pushed by setting fields in the DTO. The fields correspond to information about the provider being updated, and the update to apply:

* `model` - the name of the model for the provider being updated. This can be omitted, but it is good practice to set it.
* `provider` - the id of the provider to be updated. This is mandatory
* `service` - the name of the service to be updated. This is mandatory
* `resource` - the name of the resource to be updated. This is mandatory
* `value` - the new value of the resource
* `type` - the type of the resource (optional). If `value` is non null then it must be assignable to `type`
* `timestamp` - the timestamp for this update (optional). If not set then the current time will be used for the timestamp
* `metadata` - a map of metadata values for this update (optional). The key in the map represents the name of the metadata property, and the value the metadata value. If the value is set to null then the metadata will be removed from the digital twin.

For example:

```
GenericDto dto = new GenericDto();
dto.model = "myModel";
dto.provider = "provider1";
dto.service = "service";
dto.service = "resource";
dto.value = 42;

push.pushUpdate(dto);
```

### Using a Custom DTO

Custom DTOs allow southbound providers to simplify the way in which they push updates by defining a DTO including mapping information. Custom DTOs can be used to push multiple data values in a single atomic update.

#### The Model annotation

The model annotation is used to define the name of the model to which an update is applied. The preferred usage is as a single element annotation applied to the DTO type.

```
@Model("myModel")
public class MyUpdateDTO {
    ....
}
```

The annotation can also be used as a marker annotation on a String field to identify that the value of the field should be used as the name of the model.

```
public class MyUpdateDTO {
    @Model
    public String model
}
```

#### The Provider annotation

The provider annotation is used to define the id of the provider to which an update is applied. The preferred usage is as a marker annotation applied to a String field in the DTO type.

```
public class MyUpdateDTO {
    @Provider
    public String provider
}
```

The annotation can also be used as a single element annotation on a type or field to identify that the update should be applied to the identified provider. This usage hard codes the id of the provider and should only be used in cases where the DTO will have limited reuse

```
@Provider("myProvider")
public class MyUpdateDTO {
    ...
}
```
or

```
public class MyUpdateDTO {
    @Provider("myProvider")
    @Service("myService")
    @Data
    public Integer counter;
}
```

#### The Service annotation

The service annotation is used to define the name of the service to which an update is applied. The preferred usage is as a single element annotation applied to a field containing the data to update, or to the type which will use that service for all data updates.

```
public class MyUpdateDTO {
    @Service("myService")
    @Data
    public Integer counter;
    ...
}
```
or

```
@Service("myService")
public class MyUpdateDTO {
    ...
}
```

The annotation can also be used as a marker annotation applied to a String field in the DTO type. This allows the service name to be set dynamically at runtime.


```
public class MyUpdateDTO {
    @Service
    public String service;
}
```

#### The Data annotation

The `Data` annotation is applied to a DTO field and used to define a data value which should be updated. The name of the field is used as the name of the resource to be updated.

```
public class MyUpdateDTO {
    @Data
    public Integer counter;
    ...
}
```

The type of the resource defaults to the type of the data field, but this can be customised using the `type` element of the `Data` annotation. Additionally the behaviour of `null` can be configured using the `onNull` element of the `Data` annotation - by default null values will not trigger an update, but this can be changed to explicitly set a `null` value.

#### The Resource annotation

The resource annotation is used to define the name of the resource to which an update is applied. The preferred usage is as a single element annotation applied to a field containing the data to update. This can be used to override the default resource name in the case where it does not match the DTO field name.

```
public class MyUpdateDTO {
    @Resource("myResource")
    @Data
    public Integer counter;
    ...
}
```

The annotation can also be used as a marker annotation applied to a String field in the DTO type. This allows the resource name to be set dynamically at runtime.

```
public class MyUpdateDTO {
    @Resource
    public String resource;
}
```

#### The Timestamp annotation

The `Timestamp` annotation can be applied to a field to indicate the time at which the data in the update was generated. The type of the field may be a `long` or `Long` representing time since EPOCH, or it may be an `Instant`, `OffsetDateTime` or `ZonedDateTime`.

```
public class MyUpdateDTO {
    @Timestamp
    public long timestamp;
}
```

#### The Metadata annotation

The `Metadata` annotation can be applied to a field to indicate that it contains metadata that should be updated.

```
public class MyUpdateDTO {
    @Metadata
    public String unit;
}
```

By default the name of the field will be used as the name of the metadata property, but this can be customised using the `value` element of the `Metadata` annotation. The behaviour of `null` can be configured using the `onNull` element of the `Metadata` annotation - by default null values will not trigger an update, but this can be changed to explicitly set a `null` value.

Additionally the `Metadata` annotation can be configured to have a special behaviour when the type of the annotated field is `Map`. By using the `onMap` element the DTO can be configured to use the keys of the map as metadata property names with the corresponding values. Furthermore the configuration can be used to specify that keys with `null` values should be removed from the metadata stored in the digital twin.


## Pulling updates

Pulled updates differ from pushed updates in that the southbound adapter is called by the sensiNact core to request data, rather than calling the sensiNact core to provide data. This is an important difference when it comes to how you write your southbound connector.

### Whiteboard Services

All pulled updates are implemented using whiteboard services. These services must be registered with the property `sensiNact.whiteboard.resource` set to `true`. This can be easily achieved when writing a DS component by using the `WhiteboardResource` component property annotation.

### Resource Methods

Whiteboard services add resources via *Resource Methods*. A *Resource Method* is a method annotated with one the sensiNact verbs and with parameter annotations indicating what parameters should be injected into the method.

The service instance should then have one or more methods annotated with `ACT`, `GET` or `SET`. These methods will be used to register the resource with the model, and called whenever the relevant verb is used for the resource. All of these annotations are repeatable, meaning that multiple operations can be handled by a single method if that is appropriate, for example all `SET` operations for a model may be handled by the same method.

Resource Methods are typically asynchronous, and if they return a `Promise` then this will be automatically flattened before updating the model.

In general the same Resource Method is called for all provider instances, however this can be restricted by using the `sensiNact.provider.name` service property to set a collection of provider ids for which this service should be called.

#### GET Methods

A *Resource Method* annotated with `GET` is called by sensiNact in response to a `GET` request, and is used to supply a value to the client. In order to reduce the burden on the connector and the sensor device results are cached in the digital twin and returned until they become stale. The cache expiry timeout can be set using the `cacheDuration` and `cacheDurationUnit` elements of the `GET` annotation.

In the simplest case the `model`, `service` and `resource` elements of the `GET` annotation can be used to define the resource for which method should be called. Various values can then be injected into the method invocation:

* `@UriParam String s` - Allows the injection of either the full URI, or parts of the URI, representing the GET request (e.g. the provider id)
* `@GetParam TimedValue oldValue` - Allows the injection of either the existing TimedValue from the digital twin, or the `Class` representing the type of the object being retrieved

```
@GET(model = "myModel", service = "myService", resource = "myResource", cacheDuration = 2, cacheDurationUnit = MINUTES)
public Promise<Integer> getReading(@UriParam(PROVIDER) String provider, @GetParam TimedValue<Integer> oldValue) {
    ...
}
```

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
* The new value of the resource after the `SET`. This will normally be the value passed into the `SET` operation, but may be different if the resource is being updated by multiple users, or for some other reason
* An exception, indicating that the `SET` operation failed

```
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

```
@ACT(model = "myModel", service = "myService", resource = "securityLight")
public Promise<Void> setLight(@ActParam("brightness") Integer b, @ActParam("time") Duration d) {
  ...
}
```

