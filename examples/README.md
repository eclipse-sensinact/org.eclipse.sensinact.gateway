# Prototype examples

This project contains example code demonstrating the use of various techniques for pushing data into sensiNact

## Code first

These modules use code to define data objects/methods which is used to infer a data model, rather than having a formal model declaration

#### Push-based

The push-based module shows how to make data update DTOs and "push" them to sensiNact. These are best for periodic or "pushed" sensor events (e.g. an on/off notification)

#### Pull-based

The pull-based module shows how to make services that can "get" a sensor value. These are best for low power sensors that you only want to query if necessary.

#### Action

The action module contains examples showing how to define "action" resources using the whiteboard

#### Writable

The writable module contains examples showing how to set data in a device

#### Generic

The generic module contains examples showing how to perform a generic update without having up-front code

## Model first

These modules define a data model directly, then set data into it

#### Reflective

The reflective module contains examples showing how to define a model using method calls

#### Model-based

The model-based module contains examples showing how to define a model using a resource descriptor

