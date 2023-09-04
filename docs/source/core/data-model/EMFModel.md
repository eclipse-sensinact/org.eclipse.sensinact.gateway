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

TODO: We need input from Jürgen or Mark here
