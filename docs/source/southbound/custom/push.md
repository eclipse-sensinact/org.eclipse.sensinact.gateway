# Pushing updates

Pushing updates is the simplest way to set data in the sensiNact digital twin. Updates are pushed using the `DataUpdate` service which allows update objects to be sent to sensiNact.

## The Data Update Service

The `DataUpdate` service is obtained from the OSGi service registry.

```java
@Component
MyConnector {
    @Reference
    DataUpdate dataUpdate;
    ...
}
```

Once you have the Data Update service you can push updates into the digital twin at any time using the `pushUpdate` method. The update that you push is a DTO (data transfer object) - this can be a POJO with public fields, or a Java Record.

The value returned by `pushUpdate` is a `Promise` which will resolve when the pushed update is complete and the data is available in the digital twin. If any part of the update fails then the failure will be used to fail the returned `Promise`.

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

```java
GenericDto dto = new GenericDto();
dto.model = "myModel";
dto.provider = "provider1";
dto.service = "service";
dto.resource = "resource";
dto.value = 42;

push.pushUpdate(dto);
```

### Using a Custom DTO

Custom DTOs allow southbound providers to simplify the way in which they push updates by defining a DTO including mapping information. Custom DTOs can be used to push multiple data values in a single atomic update.

#### The Model annotation

The model annotation is used to define the name of the model to which an update is applied. The preferred usage is as a single element annotation applied to the DTO type.

```java
@Model("myModel")
public class MyUpdateDTO {
    ....
}
```

```java
@Model("myModel")
public record MyUpdateDTO (
    ....) {
}
```

The annotation can also be used as a marker annotation on a String field to identify that the value of the field should be used as the name of the model.

```java
public class MyUpdateDTO {
    @Model
    public String model
}
```

```java

public record MyUpdateDTO (
    @Model String model,
    ....) {
}
```

#### The Provider annotation

The provider annotation is used to define the id of the provider to which an update is applied. The preferred usage is as a marker annotation applied to a String field in the DTO type.

```java
public class MyUpdateDTO {
    @Provider
    public String provider
}
```

```java

public record MyUpdateDTO (
    @Provider String provider,
    ....) {
}
```

The annotation can also be used as a single element annotation on a type or field to identify that the update should be applied to the identified provider. This usage hard codes the id of the provider and should only be used in cases where the DTO will have limited reuse

```java
@Provider("myProvider")
public class MyUpdateDTO {
    ...
}
```
or

```java
public class MyUpdateDTO {
    @Provider("myProvider")
    @Service("myService")
    @Data
    public Integer counter;
}
```

#### The Service annotation

The service annotation is used to define the name of the service to which an update is applied. The preferred usage is as a single element annotation applied to a field containing the data to update, or to the type which will use that service for all data updates.

```java
public class MyUpdateDTO {
    @Service("myService")
    @Data
    public Integer counter;
    ...
}
```
or

```java
@Service("myService")
public class MyUpdateDTO {
    ...
}
```
or

```java
@Service("myService")
public record MyUpdateDTO (
    @Service("myService")
    @Data Integer counter,
    ....)
}
```

The annotation can also be used as a marker annotation applied to a String field in the DTO type. This allows the service name to be set dynamically at runtime.


```java
public class MyUpdateDTO {
    @Service
    public String service;
}
```

#### The Data annotation

The `Data` annotation is applied to a DTO field and used to define a data value which should be updated. The name of the field is used as the name of the resource to be updated.

```java
public class MyUpdateDTO {
    @Data
    public Integer counter;
    ...
}
```

```java

public record MyUpdateDTO (
    @Data Integer counter,
    ....) {
}
```

The type of the resource defaults to the type of the data field, but this can be customised using the `type` element of the `Data` annotation. Additionally the behaviour of `null` can be configured using the `onNull` element of the `Data` annotation - by default null values will not trigger an update, but this can be changed to explicitly set a `null` value.

#### The Resource annotation

The resource annotation is used to define the name of the resource to which an update is applied. The preferred usage is as a single element annotation applied to a field containing the data to update. This can be used to override the default resource name in the case where it does not match the DTO field name.

```java
public class MyUpdateDTO {
    @Resource("myResource")
    @Data
    public Integer counter;
    ...
}
```

The annotation can also be used as a marker annotation applied to a String field in the DTO type. This allows the resource name to be set dynamically at runtime.

```java
public class MyUpdateDTO {
    @Resource
    public String resource;
}
```

#### The Timestamp annotation

The `Timestamp` annotation can be applied to a field to indicate the time at which the data in the update was generated. The type of the field may be a `long` or `Long` representing time since EPOCH, or it may be an `Instant`, `OffsetDateTime` or `ZonedDateTime`.

```java
public class MyUpdateDTO {
    @Timestamp
    public long timestamp;
}
```

#### The Metadata annotation

The `Metadata` annotation can be applied to a field to indicate that it contains metadata that should be updated.

```java
public class MyUpdateDTO {
    @Metadata
    public String unit;
}
```

By default the name of the field will be used as the name of the metadata property, but this can be customised using the `value` element of the `Metadata` annotation. The behaviour of `null` can be configured using the `onNull` element of the `Metadata` annotation - by default null values will not trigger an update, but this can be changed to explicitly set a `null` value.

Additionally the `Metadata` annotation can be configured to have a special behaviour when the type of the annotated field is `Map`. By using the `onMap` element the DTO can be configured to use the keys of the map as metadata property names with the corresponding values. Furthermore the configuration can be used to specify that keys with `null` values should be removed from the metadata stored in the digital twin.

### Pushing multiple updates

If your connector creates multiple updates then this can be applied as a single batch:

```java
MyUpdateDto dto1 = getDtoData(1);
MyUpdateDto dto2 = getDtoData(2);
MyUpdateDto dto3 = getDtoData(3);

push.pushUpdate(List.of(dto1, dto2, dto3);
```

Adding the events in a single batch has the advantage that all of the data updates are made together, and no partial updates will ever be visible.

### Advanced models

The `Model` and `ServiceModel` annotations can be used in more advanced cases, such as when the provider is a `DynamicProvider`. This behaviour is described in detail in the [EMF Mode section discussing dynamic providers](../../core/data-model/EMFModel.md#dynamic-providers-with-emf-models).
