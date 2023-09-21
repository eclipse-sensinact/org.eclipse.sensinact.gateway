# Device factory

The device factory handles raw update notifications from transport handlers.
The payload is parsed and its content is mapped to the Eclipse sensiNact model according to a configuration.


![An example transform from JSON](../../_static/southbound/device-factory-transform-white.png){.block-center}

The device factory core is not intended to be used directly, but through a transport-specific device factory.


```{toctree}
:maxdepth: 2
:glob:

core
csv
json
tuto-parser
```
