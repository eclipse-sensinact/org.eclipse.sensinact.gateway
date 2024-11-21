# Easy Rules Integration

The [Easy Rules Project](https://github.com/dvgaba/easy-rules) provides a simple, lightweight Rule Engine written in Java. Eclipse sensiNact offers support for integrating Easy Rules with the Rules Whiteboard

## The easy-rules.osgi Module

The latest version of Easy Rules does not provide an OSGi bundle packaging, so it is necessary for Eclipse sensiNact to repackage the core library as an OSGi bundle. This bundle exports the three main packages provided by `easy-rules-core`:

* org.jeasy.rules.annotation;version="4.1"
* org.jeasy.rules.api;version="4.1"
* org.jeasy.rules.core;version="4.1";uses:="org.jeasy.rules.api"

### Easy Rules Helpers

In addition to the core Rule Engine implementation the `easy-rules.osgi` bundle provides helper classes for building a `RuleDefinition` from a set of `Rule` instances, and for turning a `List<ProviderSnapshot>` into a `Facts` object suitable for rule execution

## The easy-rules.config Module

The `easy-rules.config` module extends the integration provided by `easy-rules.osgi` by allowing Easy Rules based `RuleDefinition` objects to be created purely using configuration.

### Configuration properties

Rules can be created using factory configurations with a factory PID of `sensinact.rules.easyrules` and the following properties:

* `sensinact.rule.name` - the name of the rule
* `resource.selectors` - A String+ property containing one or more JSON serialized [Resource Selectors](../../filters/ResourceSelector), which will be combined in an OR semantic.
* `rule.definitions` - A String+ property containing one or more JSON serialized Rules

The Rule Defnition schema is as follows:

```json
{
  "name": "Rule Name",
  "description": "Rule Description",
  "condition": "JEXL expression returning a boolean",
  "action": [
    "JEXL script performing an action",
    "Another action script"
  ],
  "priority": 7
}
```

### JEXL execution

The JEXL condition and JEXL actions have access to the following context properties:

```
{
  "$updater": "Resource Updater instance",
  "$providers", ["providerA", "providerB"],
  "$data": {
    "providerA": {
      "$services": ["serviceA", "serviceB"],
      "serviceA": {
        "$resources": ["foo", "bar"],
        "foo": {
          "$set": true,
          "$value": 5
          "$timestamp": "2024-01-01T00:00:00.000Z",
          "metadata1": "ping",
          "metadata2": "pong"
        },
        "bar": {
          "$set": false,
          "metadata1": "pingpong",
        }
      },
      "serviceB": { ... }
    },
    "providerB": { ... }
  }
}
```

These values can be used when determining whether the `Rule` has been satisified, and when calculating an appropriate action.


