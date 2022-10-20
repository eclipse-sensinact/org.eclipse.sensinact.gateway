# Eclipse sensiNact OGC Sensorthnings mappings

## Thing

A Sensorthings Thing is most closely akin to a sensiNact provider:

 * id - this is the provider id
 * name - this is the admin/friendlyName
 * description - this is the admin/description
 * locations - a link to admin/location
 * historicalLocations - a link to a collection with only the current location
 * properties - empty
 
## Location

 A location corresponds to the value of the admin/location resource for a provider

 * id - this is the provider id plus the hex encoded timestamp
 * name - this is the name field of the feature
 * description - this is the description field of the feature
 * encodingType - always Geo JSON
 * location - the geo json object
 * historicalLocations - a link to the single current historical location
 * thingslink - a link back to the provider
 
## HistoricalLocation
 
 * id - this is the provider id plus the hex encoded timestamp
 * locations - this is a link to the location of the provider now
 * thing - this is a link to the provider
 * time - this is the time at which the location was last updated
 
## Datastreams

 A datastream loosely corresponds to a resource, and it only ever has the current data point as its only observation

* id - the provider/service/resource
* name - the friendlyName metadata for the resource
* description - the description metadata for the resource
* Observations - a link to the one and only observation
* observationType - the sensor things observation type metadata for the resource
* unitOfMeasurement - generated with symbol from the unit in the resource metadata and the sensorthings.unit.definition, sensorthings.unit.name
* observedArea - if adminLocation is a Polygon or MultiPolygon, or a Feature/FeatureCollection containing one, then use that
* observedProperty - a link to the observed property for the resource
* phenomenonTime - not used
* resultTime - not used
* sensorLink - a link to the sensor type
* thing link - a link to the provider

## Sensors

 A sensor loosely corresponds to a resource
 
 * id - the provider/service/resource
 * name - the friendlyName metadata for the resource
 * description - the description metadata for the resource
 * encodingType - the sensorthings.sensor.encodingType, or text/plain
 * metadata - the sensorthings.sensor.metadata, or "No metadata"
 * properties - the full set of metadata from the resource
 
## Observations

 An observation corresponds to the reading from a resource
 
  * id the provider/service/resource plus a hex timestamp
  * phenomenonTime - the time of the reading
  * result - the value of the resource
  * resultTime - the time of the reading
  * resultQuality - sensorthings.observation.quality, or null
  * validTime - null
  * parameters - null;
 

## ObservedProperty

 An observed property corresponds to a resource
 
 * id - the provider/service/resource
 * name - the friendlyName metadata for the resource
 * description - the description metadata for the resource
 * definition - the sensorthings.observedproperty.definition
 * properties - the full set of metadata from the resource

## FeatureOfInterest

 Corresponds to the location or observed area of the provider
 
 * id - this is the provider id plus the hex encoded timestamp
 * name - this is the name field of the feature
 * description - this is the description field of the feature
 * encodingType - always Geo JSON
 * feature - the geo json object
 * historicalLocations - a link to the single current historical location
 * observationsLink - a link back to the observations (resources) for this provider

 
# Future changes
 
 * Support for history in data streams?
 * Link to the model so that ObservedProperty is shared? 