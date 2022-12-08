# Data access behaviour

## Admin resources

Pre-defined resources of the `admin` service (defined in the data model) should always be considered set, even if they were not given a value. As a result, when a provider instance is created, its pre-defined resources should be given a default value (can be `null`) and a timestamp (creation time of instance).
Status code based northbound endpoints will return a 200 (OK) when accessing pre-defined admin resources.

Access to additional resources must follow the same rules as resources from other services.

## Existing items

Status code based northbound endpoints will return a 200 (OK).

## Non-existing items

### Providers and services

Missing providers and missing services are handled the same: the session will return `null` when trying to describe a missing provider or service and when trying to describe or read its resources.
Status code based northbound endpoints will return a 404 (Not found).

## Resources

When trying to describe or read an undefined resource from an existing service, we won't create that resource but will return a `TimedValue` with a `null` timestamp (and a `null` value).
Status code based northbound endpoints will return a 204 (No content).

When setting a value to a resource, its whole path is created: if its provider and/or service don't exist, they will be created with default values for the admin resources.
Status code based northbound endpoints will return a 200 (OK).
