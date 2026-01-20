# The EMF data model

The sensiNact data model is defined in EMF and has a number of key parts:

## The Provider

The Provider is the top part of the sensiNact EMF Model, and represents the template for a provider in the digital twin. All providers have a number of common attributes.

### Inheritance

All providers are instances of the `Provider` interface, providing direct access to the provider `id` and the `admin` service

### The Admin Service

The `admin` service is a special service of type `Admin` which extends `Service` and provides easy access to the built in resources such as `friendlyName` and `location`. All providers always have an `admin` service.

### Services

The provider model class uses `EReference`s of type `Service` for each of the services in the model.

### Resources

Each `Service` uses `EAttribute`s for each data resource in the model. The type of the `EAttribute` is the type of the resource. Action resources are represented using an `EOperation` where the parameter types of the operation represent the parameters to the action, and the return type of the `EOperation` provides the type of the action resource.

#### Multiple Resources

Multiple resources are represented in the EMF model using the `upperBound` of the `EAtrribute`. Typically this will be set to 1 (representing a single-valued resource) or -1 (representing an unlimited multi-valued resource). It is valid to use other values for the `upperBound`, as well as using the `lowerBound` for the `EAttribute`. If these constraints are set then they will be enforced by the model, for example it is possible to require that a resource has exactly two values set and to reject any update that does not contain exactly two values.

### Variable metadata

Every resource in sensiNact has associated metadata. This metadata is stored in a `Metadata` object at the `Service` level. The `Service` uses an `EMap` where the key is the `ETypedElement` representing the `EAttribute` or `EOperation` for the resource, and the value is the `Metadata` object. The `Metadata` object has a `timestamp` which represents the most recent update time for the resource (i.e. the most recent update to the resource value or resource metadata). The `Metadata` also provides a list of `FeatureCustomMetadata` representing the individual metadata values for the resource. Each `FeatureCustomMetadata` has a name, value and timestamp representing the metadata value and when it was last updated.

```{important}
The `timestamp` is used to determine the last update time for the resource. If the resource has never been set then there will be no timestamp set. Also, the timestamp represents the time of the last update, which may be *later* than the timestamps for each individual metadata timestamp if, for example, the most recent change was to delete a metadata value.
```

### Annotated metadata

Provider model classes are annotated with an `EAnnotation` named `model`, the value of the `name` property in the annotation details is the name of the model.

### Linked Providers

The `Provider` type includes a collection reference which can be used to *link* to other providers. This can be used to assemble a provider from other providers by listing them as links.

Linking a provider `B` into provider `A` does not cause any of the linked provider `B`'s services or resources to become directly visible in provider `A`, however it does change the result of a `GET` for the `admin/location` resource. The location for a provider with links is calculated as follows:

 1. A GeoJSON Feature Collection object is created to hold the location
 2. The provider's location (if set) is added to the feature collection
   - If the location is a Geometry type then it is wrapped in a feature with the property `sensinact.provider.id` set to the id of the provider
   - If the location is a Feature then it is used directly, with an additional property `sensinact.provider.id` set to the id of the provider
   - If the location is a FeatureCollection then it is unwrapped and each feature is added to the created Feature Collection, with an additional property `sensinact.provider.id` set to the id of the provider in each feature
 3. For each linked provider get the location (if set) and repeat step 2. If at any point a cycle is detected then the link causing the cycle is skipped.


## Creating a custom model

Creating a custom EMF model allows you to define structured, typed data models for your sensiNact providers. This is particularly useful when you want to enforce strict schemas, reuse model definitions, or work with existing EMF models from other systems.

### Using an existing EMF model

If you already have an EMF model (e.g., from Eclipse Ecore Designer or another EMF-based system), you can use it directly with sensiNact. There are several approaches depending on whether your provider model is static (predefined services) or dynamic (services created at runtime).

#### Static providers with EMF models

For providers with a fixed, predefined structure, you can reference the EMF model classes directly using annotations:

```java
@Provider("temperatureSensor1")
public class TemperatureSensorDTO {

    @Model
    public EClass providerModel = MyModelPackage.Literals.TEMPERATURE_SENSOR;

    @Service
    public EReference serviceRef = MyModelPackage.Literals.TEMPERATURE_SENSOR__TEMPERATURE;

    @Resource("value")
    @Data
    public Double temperature;

    @Timestamp(ChronoUnit.MILLIS)
    public long timestamp;
}
```

In this example:
- The `@Model` annotation specifies which EClass defines the provider structure
- The `@Service` annotation with an `EReference` indicates which service to use (the reference must exist in the provider model)
- The resource is mapped to the `temperature` attribute defined in the EMF model

#### Dynamic providers with EMF models

For dynamic providers (where services can be created at runtime), you need to specify the service structure. You can do this using either EClass references or String-based service model names.

**Using EClass for service models:**

```java
@Provider("sensor1")
public class DynamicSensorDTO {

    @Model
    public EClass providerModel = MyModelPackage.Literals.DYNAMIC_SENSOR;

    @ServiceModel
    public EClass serviceType = MyModelPackage.Literals.TEMPERATURE_SERVICE;

    @Service
    public String serviceName = "temperature";

    @Resource("value")
    @Data
    public Double value;

    @Timestamp(ChronoUnit.MILLIS)
    public long timestamp;
}
```

**Using String for service models:**

For convenience, you can also specify the service model by its EClass name as a String:

```java
@Provider("sensor1")
public class DynamicSensorDTO {

    @Model
    public EClass providerModel = MyModelPackage.Literals.DYNAMIC_SENSOR;

    @ServiceModel
    public String serviceModel = "TemperatureService";

    @Service
    public String serviceName = "temperature";

    @Resource("value")
    @Data
    public Double value;

    @Timestamp(ChronoUnit.MILLIS)
    public long timestamp;
}
```

The String-based approach is particularly useful when:
- You want to keep your DTOs independent of EMF model dependencies
- You're generating DTOs dynamically or from configuration
- You want simpler, more readable code

```{important}
When using `@ServiceModel` with a String value, sensiNact will look up the EClass by name within the provider's model package. The String must exactly match the name of an EClass defined in your EMF model.
```

#### Understanding @Service vs @ServiceModel

It's important to understand the difference between these two annotations:

- **`@Service`**: Specifies the *name* of the service instance (e.g., "temperature", "humidity"). For static providers, it can also be an `EReference` pointing to a specific service in the provider model.

- **`@ServiceModel`**: Specifies the *type* of service to create (the EClass that defines the service structure). This is only needed for dynamic providers where services are created at runtime. It can be either an `EClass` reference or a String containing the EClass name.

**Example combining both:**

```java
@Provider("weatherStation")
public class WeatherUpdateDTO {

    @Model
    public EClass providerModel = WeatherPackage.Literals.WEATHER_STATION;

    // Define the type of service to create
    @ServiceModel
    public String serviceModel = "EnvironmentalSensor";

    // Define the name of the service instance
    @Service
    public String serviceName = "outdoor";

    @Resource("temperature")
    @Data
    public Double temp;
}
```

This creates a service named "outdoor" with the structure defined by the "EnvironmentalSensor" EClass.

#### Dynamic vs Static Providers

The key difference between dynamic and static providers in EMF models:

**Static Providers:**
- Services are defined as `EReference` fields in the provider's EClass
- The structure is fixed and cannot be modified at runtime
- Use `@Service` with an `EReference` to reference existing services
- Example: `TestSensor` with predefined `temp` and `admin` services

**Dynamic Providers:**
- Services can be created at runtime
- The provider's EClass has an `EMap<String, Service>` to hold services
- Use `@ServiceModel` to specify what type of service to create
- Use `@Service` to specify the name of the service instance
- Example: `DynamicTestSensor` that can have services added dynamically

```{note}
Attempting to use `@ServiceModel` with a static (non-dynamic) provider will result in an error, as static providers cannot have services created at runtime.
```

### Deploying EMF models in OSGi

When deploying custom EMF models in an OSGi environment (which sensiNact runs in), you need to ensure your EMF model is properly registered. The recommended approach uses the Gecko EMF OSGi code generator, which automatically generates all the required registration code from your EMF model.

#### Using the Gecko EMF Code Generator

The easiest way to deploy an EMF model is to use the BND Gecko EMF code generator. This automatically generates:
- The model implementation classes (EPackage, EFactory, etc.)
- OSGi service registration components
- Proper lifecycle management code

**Step 1: Add the BND Generate Plugin**

Add the `bnd-generate-maven-plugin` to your project's `pom.xml`:

```xml
<build>
  <plugins>
    <plugin>
      <groupId>biz.aQute.bnd</groupId>
      <artifactId>bnd-generate-maven-plugin</artifactId>
      <version>${bnd.version}</version>
      <configuration>
        <externalPlugins>
          <dependency>
            <groupId>org.geckoprojects.emf</groupId>
            <artifactId>org.gecko.emf.osgi.codegen</artifactId>
          </dependency>
        </externalPlugins>
        <steps>
          <step>
            <trigger>src/main/resources/model/mymodel.genmodel</trigger>
            <generateCommand>geckoEMF</generateCommand>
            <output>src/main/java</output>
            <clear>false</clear>
            <properties>
              <genmodel>src/main/resources/model/mymodel.genmodel</genmodel>
            </properties>
          </step>
        </steps>
      </configuration>
      <executions>
        <execution>
          <phase>generate-sources</phase>
          <goals>
            <goal>generate</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

**Step 2: Add Required Dependencies**

Add the required dependencies for EMF model development:

```xml
<dependencies>
  <!-- SensiNact Provider Model - Required for code generation -->
  <dependency>
    <groupId>org.eclipse.sensinact.gateway.core.models</groupId>
    <artifactId>provider</artifactId>
    <version>${project.version}</version>
  </dependency>

  <!-- Gecko EMF OSGi API -->
  <dependency>
    <groupId>org.geckoprojects.emf</groupId>
    <artifactId>org.gecko.emf.osgi.api</artifactId>
  </dependency>
</dependencies>
```

```{important}
The sensiNact core provider model dependency is **required** for the code generator to work correctly. It provides the base `Provider`, `DynamicProvider`, and `Service` EClasses that your model must extend.
```

**Step 3: Create Your EMF Model**

Create your EMF model using the Eclipse Ecore Designer or any EMF tooling. Your model must follow these structural requirements:

**Model Structure Requirements:**

1. **Provider Classes** - Your provider EClass must extend one of:
   - `Provider` (from `http://eclipse.org/sensinact/core/provider/1.0`) - For static providers with predefined services
   - `DynamicProvider` (from `http://eclipse.org/sensinact/core/provider/1.0`) - For providers where services can be created at runtime

2. **Service Classes** - All service EClasses must extend:
   - `Service` (from `http://eclipse.org/sensinact/core/provider/1.0`) - Base class for all services

**Example EMF Model Structure:**

```
MyModel.ecore
├── MySensor (extends Provider)
│   ├── temperature : EReference (type: TemperatureService)
│   └── humidity : EReference (type: HumidityService)
│
├── TemperatureService (extends Service)
│   └── value : EAttribute (type: EDouble)
│
└── HumidityService (extends Service)
    └── value : EAttribute (type: EDouble)
```

For dynamic providers:

```
MyModel.ecore
└── MyDynamicSensor (extends DynamicProvider)
    └── services : EMap<String, Service> (inherited from DynamicProvider)
```

Ensure you have:
- An `.ecore` file defining your model structure with proper inheritance
- A `.genmodel` file configuring code generation
- References to the sensiNact provider model package (`http://eclipse.org/sensinact/core/provider/1.0`)

**Step 4: Generate the Code**

Run Maven to generate the model implementation and OSGi registration code:

```bash
mvn clean generate-sources
```

The generator will create:
- Model classes in `src/main/java`
- A `configuration` package with:
  - `[YourModel]EPackageConfigurator` - Handles EPackage registration
  - `[YourModel]ConfigurationComponent` - OSGi component for lifecycle management

**Step 5: Use the Model**

Once generated and built, you can reference your model's EClasses in your data transfer objects as shown in the examples above. The OSGi service registry will ensure your model is available to sensiNact and other bundles.

```{tip}
The generated code follows best practices for OSGi lifecycle management, including proper service registration and cleanup in the `@Activate` and `@Deactivate` methods.
```

#### Manual Registration (Advanced)

```{warning}
Manual registration is only needed if you cannot use the code generator. The generator is the recommended approach as it ensures all components are correctly configured.
```

If you must manually register your EMF model, you need to create two classes:

1. **EPackage Configurator** - Implements `org.gecko.emf.osgi.configurator.EPackageConfigurator`
   - `configureEPackage()`: Registers the EPackage in the EMF registry
   - `unconfigureEPackage()`: Unregisters on deactivation
   - `getServiceProperties()`: Provides service metadata (model name, NS URI, version)

2. **Configuration Component** - OSGi Declarative Services component
   - Annotated with `@Component`
   - `@Activate` method: Registers EPackage, EFactory, and EPackageConfigurator services
   - `@Deactivate` method: Unregisters all services and removes from registry

The generator creates these files in a `configuration` package. You can examine the generated code in `core/models/testdata/src/main/java/org/eclipse/sensinact/model/core/testdata/configuration/` for a complete reference implementation.

Required bundle imports:
```
Import-Package: org.eclipse.emf.ecore,
                org.eclipse.emf.common.util,
                org.gecko.emf.osgi.configurator,
                org.gecko.emf.osgi.constants,
                org.osgi.framework,
                org.osgi.service.component.annotations
```

### Best practices

When working with deployed EMF models:

1. **Model reusability**: Define service types (EClasses) that can be reused across different providers
2. **Type safety**: Use EClass references during development for compile-time safety, but consider String-based references for configuration-driven systems
3. **Documentation**: Document your EMF model's EClasses and their intended usage
4. **Validation**: EMF models can include validation rules that sensiNact will respect
5. **Versioning**: Plan for model evolution by using EMF's built-in versioning and migration support
