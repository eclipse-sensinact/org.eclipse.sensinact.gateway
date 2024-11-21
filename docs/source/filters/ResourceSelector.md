# Eclipse sensiNact Resource Selectors

A resource selector is a simple type of filter designed to easily select a sensinact provider and its resources. It uses simple key/value statements and is easily represented in JSON or other serializations.


```json
{
  "model": { /* Selection */ },
  "provider": { /* Selection */ },
  "service": { /* Selection */ },
  "resource": { /* Selection */ },
  "value": { /* Value Selection */ },
  "location": { /* Location Selection */ }
}
```

## Creating a Filter from a Resource Selector

A filter can easily be created from a Resource Selector using the `ResourceSelectorFilterFactory` service. This service can take a single Resource Selector and turn it into an `ICriterion`, or combine many Resource Selectors into a single `ICriterion` using an `OR` semantic.

### Limitations of Resource Selectors

Resource selectors are designed to efficiently select and subscribe to single resource values, and are not well suited to more complex `AND` scenarios, particularly when subscribing.

If you need your selection tests to be based on the values of multiple resources simultaneously then you are probably best using a different filter type, or rethinking your filter so that it can be expressed more simply.


## Selections

A selection is the simplest part of a resource selector, and applies to the `model`, `provider`, `service` and `resource` properties. Each of these properties corresponds to a component of the full *name* of the resource, for example the provider name or the service name.

```json
{
  "type": "EXACT",
  "value": "foobar"
}
```

Selections must always have a `value` property, which is the value to be tested against. The type of test is determined by the `type` property, which is one of:

* `EXACT` - the selection value must exactly match the name component
* `REGEX` - the selection value is used as a regular expression which must must match the entire name component
* `REGEX_REGION` - the selection value is used as a regular expression which must match some part of the name component.

If the `type` property is not specified then it has the value `EXACT`.

### Wildcard selections

If there should be no restriction on the name component then it should be omitted, or set to `null`. For example the following:

```json
{
  "model": { "value": "foo", "type": "EXACT" },
  "provider": null,
  "service": { "value": "bar", "type": "EXACT" },
  "resource": { "value": "foobar", "type": "EXACT" },
}
```

selects all the `foobar` resource in the `bar` service for *all* providers that use the `foo` model.


### Negation

Selections can be negated using the property `negate` which is a `boolean`. If omitted then the value is `false`. If the value is `true` then the selection rule is negated, meaning that only name components which fail the test are selected.

### Optimising filters

In general it is best to use `EXACT` matches which have `"negate": false` when listening for data updates. This allows sensiNact to minimise the number of events that have to be tested and discarded.

### Multiple Selections

Each selection is a single object, and does not support multiple selections. If you need to select multiple resources then this is achieved using multiple resource selectors.

## Value Selections

Value Selections are filters which are applied to the *value* of any and all resources which match the [Selections](#selections) present in the Resource Selector.

Value Selections have the following form:

```json
{
  "value": "17",
  "operation": "EQUALS"
}
```

### Operation Types

The `operation` of a value selection is one of: `EQUALS`, `LESS_THAN`, `GREATER_THAN`, `LESS_THAN_OR_EQUAL`, `GREATER_THAN_OR_EQUAL`, `REGEX`, `REGEX_REGION`, or `IS_SET`.

Mathematical operators apply by converting `value` into the same type as the resource value being tested, and then either using `equals` or by treating the resource value as being `Comparable`. If the resource value *is not* `Comparable` then the operation returns `false`.

`REGEX` and `REGEX_REGION` behave as described for [Selections](#selections).

`IS_SET` is a special operator which returns true if the resource has a non-null, non-empty value. Empty values include the empty string, empty collections and empty maps.

The default value for `operation` if left unspecified is `EQUALS`.

### Multiple Value Selections

Value selections may be provided singly, or in an array:

```json
{
  "model": {
    "value": "testModel"
  },
  "service": {
    "value": "testService"
  },
  "resource": {
    "value": "testResource"
  },
  "value": {
    "value": "17",
    "operation": "EQUALS",
  }
}
```

or

```json
{
  "model": {
    "value": "testModel"
  },
  "service": {
    "value": "testService"
  },
  "resource": {
    "value": "testResource"
  },
  "value": [
    {
      "value": "5",
      "operation": "GREATER_THAN",
    },
    {
      "value": "17",
      "operation": "LESS_THAN",
    }
  ]
}
```

Where more than one value is supplied the filters are combined using a logical `AND` semantic, meaning that the above Resource Selector selects the resource `testResource` from the service `testService` for all providers using the model `testModel`, restricted to only those resources with values between 5 and 17 (exclusive).

### Negation

Value Selections can be negated using the property `negate` which is a `boolean`. If omitted then the value is `false`. If the value is `true` then the selection rule is negated, meaning that only name components which fail the test are selected.

## Location Selections

```{warning}
Location selections are a complex form of matching and the current implementation is very limited
```

Location Selections are used to limit the geographic area within which a Resource Selector operates:

```json
{
  "value": {  /* A GeoJSON Object */ }
  "radius": 100.0
  "type": "CONTAINS"
}
```

Unlike [Value Selections](#value-selections) the Resource Selector does not need to select the `location` resource from the `admin` service in order to filter its value.

The `value` property of the Location Selection is a GeoJSON object representing the area to be tested against. The `radius` property is a number representing the tolerance, in meters, with which the test should be applied. This means that if `value` is a GeoJSON `Point` then the `radius` defines a circle around that point on the earth's surface.

### Operation Types

The `operation` of a value selection is one of:

* `CONTAINS` - The location of the provider is *fully contained* within the region defined by this Location Selection
* `INTERSECTS` - The location of the provider is at least partially contained within the region defined by this Location selection
* `DISJOINT` - The location of the provider is *totally separate* from the region defined by this Location Selection.

There is no default value for the `operation` property and it must be specified.

### Multiple Location Selections

Multiple Location Selections behave in the same way as [Multiple Value Selections](#multiple-value-selections) using an `AND` semantic when combining the tests.


