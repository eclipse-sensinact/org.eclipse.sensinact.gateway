# Eclipse sensiNact LDAP filters

LDAP filters are defined by [IETF RFC 4515](https://datatracker.ietf.org/doc/html/rfc4515) and are a query syntax used for selecting records containing one or more a key/value pairs.

For example LDAP filters please refer to the RFC document.


## Creating a sensiNact Filter from an LDAP string

A filter can easily be created from an LDAP string using the `IFilterHandler` service and specifying a filter language `ldap`. This service will generate an `ICriterion` based on the supplied LDAP filter.

### Limitations of LDAP filters

1. LDAP filters offer no mechanism for geographic filtering. If you need to perform any location-based filtering then this will need to be achieved in some other way.

2. LDAP filters can rapidly increase in complexity and become hard to read. These filters are also usually less efficient when subscribing for data notifications.

3. SensiNact LDAP filters do not restrict the services or resources returned in snapshots. If you wish to restrict to a subset of services or resources then you will need to use a different sort of filter.


## Filtering Provider Data

The following special keys are defined, allowing users to filter on properties of the sensiNact Provider:

* `PACKAGE` - The Package URI associated with the Provider model
* `MODEL` - The name of the Provider model
* `PROVIDER` - The id of the Provider

These values can be selected using standard filter syntax, e.g.

```
(MODEL=testModel)
(PROVIDER=Temp_*)
```

Filter elements using these special keys will be applied in the provider filter of the generated `ICriterion`.

### Filtering Resource Values

The remaining parts of the LDAP filter are applied to the *values* of any and all resources defined by the Provider. Note that this means *all* services and resources for a Provider are always selected by an LDAP filter.

Resource Value filters have the following form:

```
(service-name.resource-name=42)
```

For example to select all providers that have the `bar` resource in the `foo` service with a value greater than or equal to `7`

```
(foo.bar>=7)
```
