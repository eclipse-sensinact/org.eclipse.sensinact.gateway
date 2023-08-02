# The sensiNact data model

The [sensiNact core data model](../CoreModel.md) defines the high-level concepts of *Resources*, *Services* and *Providers*. These are important parts of sensiNact, and can be extensively customised.

## The Eclipse Modelling Framework (EMF)

EMF is used internally to define the models for objects stored in the sensiNact digital twin. Every Provider instance in the twin has a corresponding EMF model, even if no model is explicitly created or set for the provider. Usually many provider instances will share a single model. For more details about the way EMF is used in sensiNact look [here](EMFModel.md).

The model name for a provider is available as a filterable property with the name `MODEL`, allowing similar devices to be easily selected.

The EMF model for a provider defines which services and resources exist. Furthermore it provides type information for the resources, and can include restrictions such as allowable value ranges.

## Creating a model

Creating a model can be achieved in several different ways

### The data first approach

The data first approach appears, at first, to avoid creating a model entirely. In the data first approach users simply push data updates into sensiNact using the `PrototypePush` service

```java
GenericDto dto = new GenericDto();
dto.provider = "provider1";
dto.service = "service";
dto.resource = "resource";
dto.value = 42;

push.pushUpdate(dto);
```

In this approach the sensiNact core will automatically create a model for `provider1` if it doesn't already exist. The model will automatically be extended with a service called `service` and a resource called `resource` if needed, and then the resource will be set with a value of 42.

In fact the process becomes more obvious if the `model` field of the `GenericDto` is set. This indicates the model that should be used for the provider and allows different providers to share the same model.

> [!IMPORTANT]
> All automatically created resources are set to be `UPDATABLE`, meaning that they can be updated by pushing data updates, but not using a `SET` operation.

#### Modifying the model

When using a data first approach the model can always be updated and dynamically expanded. This means that the sending a new update dto targeting a different resource in an existing provider will implicitly extend the model for that provider to include any new service or resource as needed.

This process gives maximum flexibility, but it is important to remember that any mistakes when pushing updates will potentially lead to unexpected content in the model for your provider.

### The code first approach

The code first approach is similar to the data first approach in that no formal model is created, and that users simply push data updates into sensiNact using the `PrototypePush` service. The key difference is that unlike the data first approach users define one or more custom DTOs representing the resources that they want to update. These DTOs form the template for the model, with the field names and type information being used to generate the model as updates are received.

For example, the following DTO:

```java
@Model("WeatherStation")
public class WeatherStation {
  @Provider
  public String provider;

  @Service("power")
  @Data
  public Integer status;

  @Service("weather")
  @Data
  public Double temperature;

  @Service("weather")
  @Data
  public Double humidity;

}
```

This DTO defines a model called `WeatherStation`. It supplies the `Provider` id in the `provider` field, and defines two services `power` and `weather`. The `power` service has a `status` resource of type `Integer`, and the `weather` service has resources called `temperature` and `humidity` both of type `Double`.

In this approach the sensiNact core will automatically create a model for `provider1` if it doesn't already exist. The model will automatically be extended with a service called `service` and a resource called `resource` if needed, and then the resource will be set with a value of 42.

In fact the process becomes more obvious if the `model` field of the `GenericDto` is set. This indicates the model that should be used for the provider and allows different providers to share the same model.

> [!IMPORTANT]
> All automatically created resources are set to be `UPDATABLE`, meaning that they can be updated by pushing data updates, but not using a `SET` operation.

#### Modifying the model

When using a code first approach the model can always be updated and dynamically expanded. This means that the sending a new custom dto will extend the model to include any new services or resources as needed.

A significant advantage of the code-first model is that it is not as easy to make mistakes when pushing updates, as the schema for the resources is defined in a class. This reduces the likelihood that the model for the providers will be incorrectly extended.

### The explicit approach

In some cases auto-generating the model gives insufficient control over the model that gets created. In this case a model can be explicitly generated in code by using the `SensinactModelManager` which gets passed to a sensiNact command when it is run. The following example code creates the same model as the code-first example above:

```java
public class WeatherStationModel extends AbstractSensinactCommand<Void> {
    @Override
    protected Promise<Void> call(SensinactDigitalTwin twin, SensinactModelManager modelMgr,
            PromiseFactory promiseFactory) {
        modelMgr.createModel("WeatherStation")
            .withService("power")
                .withResource("status")
                    .withType(Integer.class)
                    .withValueType(ValueType.UPDATABLE)
                    .build()
                .build()
            .withService("weather")
                .withResource("temperature")
                    .withType(Double.class)
                    .withValueType(ValueType.UPDATABLE)
                    .build()
                .withResource("humidity")
                    .withType(Double.class)
                    .withValueType(ValueType.UPDATABLE)
                    .buildAll();
        return promiseFactory.resolved(null);
    }
}
```


### Importing models

If you have created a suitable model for use in sensiNact then you can use the `SensinactModelManager` to import it directly.


```java
public class WeatherStationModel extends AbstractSensinactCommand<Void> {
    @Override
    protected Promise<Void> call(SensinactDigitalTwin twin, SensinactModelManager modelMgr,
            PromiseFactory promiseFactory) {
        modelMgr.registerModel(new FileInputStream("myModel.xmi"));
        return promiseFactory.resolved(null);
    }
}
```

## Whiteboard Resources

In addition to pushed updates sensiNact allows resources to be registered using the [whiteboard pattern](https://docs.osgi.org/whitepaper/whiteboard-pattern/). This allows for *pull based* data resources, where the gateway will query the connector to get or set an up to date value for a resource, and for action resources where the supplied function will be made available as an action to be called.

### Whiteboard Services

All Resource whiteboard services must be registered with the property `sensiNact.whiteboard.resource` set to `true`. This can be easily achieved using the `WhiteboardResource` component property annotation.

The service instance should then have one or more methods annotated with `ACT`, `GET` or `SET`. These methods will be used to register the resource with the model, and called whenever the relevant verb is used for the resource.



