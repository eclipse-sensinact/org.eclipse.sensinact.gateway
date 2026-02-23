# Internationalisation of sensiNact data

The [sensiNact core data model](../core/CoreModel.md) defines the high-level concepts of *Resources*, *Services* and *Providers*. When applying this in practice to build a [specific data model](../core/data-model/DataModel.md) there are a number of ways to support internationalisation.

## Resource Values

The most important (and common) requirements for internationalisation and localisation are for the data included in resources.

### Numeric data

Numeric data should be represented using one of the Java `Number` types. This can then be localised when displayed to the user using the appropriate decimal separator, and any other separators. For example `1,000,000.00` or `1.000.000,00`.

### Date/Time data

Dates and times should be represented using the relevant *ISO-8601* representations from the Java Time API. Where times are included they should preferentially be stored:

1. In UTC as an `Instant`
2. In a local timezone including the Zone Id using a `ZonedDateTime`
3. In a local timezone with a timezone offset using an `OffsetDateTime`

This can then be localised when displayed to the user using the appropriate time zone and calendar system. Note that "appropriate" may not relate to the locale of the user or of the gateway, for example times may make most sense in the locale of the physical sensor represented by the digital twin (e.g. solar panel output will peak around midday for the panel, not the user).

If no timezone or offset information is included then data cannot be safely localised.

```{important}
All resource timestamps (the time associated with the data reading) are stored by the gateway in UTC. Every effort is made to preserve accuracy, but sub-microsecond level precision is not guaranteed.
```

### Text data

Text data values are often displayed to the user, which can cause problems when localising the data values. In general it is expected that the provider instances in the gateway will have a default language associated with them. This should be the language most commonly expected by users of the digital twin, and may or may not be the same as the local language used where the sensor is deployed. To identify the language used in the resource value users should check the metadata for the resource. The value of the `language` metadata property should be the *ISO 639* code for the language used by the resource value.

#### Translations of Text Data

Where translations are available for the text data then these should be made available in the metadata using property names of the form *value_[code]* where *[code]* is the *ISO 639* language code for the translation. Users may then select the language that they wish to view by including metadata in their query.

```{important}
Users must be careful when updating mutable resource values. If they change the language of the data value then they must also update the `language` metadata property, and also update or remove any translations present in the metadata.
```

### Resource Descriptions

Resource descriptions can be included using the `description` metadata property. The language for the description should match the language defined by the `language` metadata property. Alternate translations can be made available in the metadata using property names of the form *description_[code]* where *[code]* is the *ISO 639* language code for the translation.

## Provider identifiers

All *Providers* have a unique immutable identifier which therefore cannot be localized based on the user. This should therefore be considered in cases where identifiers will be exposed directly to users. In general each provider should use the `friendlyName` resource in the `admin` service to provide a name which can be translated as described in the [Text data section](#text-data).

## Service and Resource Names

*Services* and *Resources* are defined by the [data model](../core/data-model/DataModel.md) and therefore use fixed string names. These names cannot be translated, but they can be defined in a language suited to the users of the model. The names must be valid Java identifiers and can therefore use unicode characters, however it is recommended to use to ASCII compatible characters where possible.
