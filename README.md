# The Eclipse sensiNact Gateway

This repository provides a lightweight yet richly featured gateway providing a digital twin for a wide variety of different devices and data sources. Modular, pluggable connectors allow any data source to integrate with sensiNact in a clean way, with additional modular, pluggable access layers providing simple access to that data.

## The core modules

The core reactor contains the core sensiNact API and the digital twin implementation.

### Annotation

The core annotation module contains the sources for annotations used to define the "code first" connectors

### Models

The models module contains the EMF models used to build the digital twin, and the code generated from them

### Sensinact-API

The sensinact-api module contains the API for the sensinact core

## Filters

The filters reactor provides different filter implementations that can be used to efficiently locate and extract data from the digital twin

## Southbound

The southbound reactor contains numerous connectors for different device types, allowing them to be represented in the sensiNact digital twin.

## Northbound

The northbound reactor contains numerous access layers using different protocols and standards to introspect and query the digital twin

## Distribution

The distribution reactor packages up the binaries from the sensiNact build into a set of launchable features and creates a simple launcher for assembling your gateway.

## Examples

The examples project contains examples demonstrating how to use the sensiNact API to populate and interact with the digital twin

# Documentation

https://eclipse-sensinact.readthedocs.io/en/latest/

# Infrastructure

Current Snapshot Repository: https://repo.eclipse.org/content/groups/sensinact/
Build: https://ci.eclipse.org/sensinact/

# Building the gateway

You can build your own copy of the gateway by running `mvn verify`

### Build profiles

There are several build profiles present in the sensiNact build that are used to maintain the quality and consistency of the code. For example:

     mvn -Dgenerate-depends=true generate-resources

Will enable the `generate-dependencies-file` profile and automatically update the DEPENDENCIES file for the project.

* The `dependabot` profile allows users to quickly re-resolve test run files after a dependency update
* The `ci-build` profile enables strict verification of test resolutions to ensure that the build is running as expected.

Other profiles can be found in the parent pom.xml file.
