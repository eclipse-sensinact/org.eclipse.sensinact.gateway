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

* Build: [GitHub Actions](https://github.com/eclipse-sensinact/org.eclipse.sensinact.gateway/actions)
* SNAPSHOT repository: https://central.sonatype.com/repository/maven-snapshots/
* Releases: [Maven Central](https://central.sonatype.com/search?namespace=org.eclipse.sensinact.gateway)

# Building the gateway

You can build your own copy of the gateway by running `mvn verify`

The `build` workflow builds every push and pull request the same way:

1. `build` compiles everything without tests, then every module is tested
   in its own job and the results are published as a check run
   ("Test Results") with every test, grouped by suite.
2. `docs` builds the Sphinx documentation with warnings treated as errors,
   so a broken link, a missing page or an invalid directive fails the
   build, and keeps the HTML as a workflow artifact (`docs-html`).

Every push to `master` also deploys a fresh SNAPSHOT of all modules to the
Sonatype Central snapshot repository once every job above has passed, and a
weekly schedule keeps it fresh in quiet times. The deploy refuses to run
when `changelist` is not `-SNAPSHOT`.

### Build profiles

There are several build profiles present in the sensiNact build that are used to maintain the quality and consistency of the code. For example:

     mvn -Dgenerate-depends=true generate-resources

Will enable the `generate-dependencies-file` profile and automatically update the DEPENDENCIES file for the project.

* The `dependabot` profile allows users to quickly re-resolve test run files after a dependency update
* The `ci-build` profile enables strict verification of test resolutions to ensure that the build is running as expected.

Other profiles can be found in the parent pom.xml file.

# Versioning and releases

All modules in this repository share one version. That version lives in
exactly one place, [`.mvn/maven.config`](.mvn/maven.config), as two lines:

```
-Drevision=0.0.2
-Dchangelist=-SNAPSHOT
```

`revision` is the plain `x.y.z` version. `changelist` is `-SNAPSHOT` during
development and empty (`-Dchangelist=`) for a release. The poms only
reference `${revision}${changelist}` - never add a second copy of the
version anywhere else. The documentation reads its version from the
`revision` line.

A release is the creation of a git tag whose name equals `revision`,
without any prefix: `0.0.2`, not `v0.0.2`.

1. Open a pull request that empties the changelist (`-Dchangelist=`) in
   `.mvn/maven.config` and merge it. From now on pushes to `master` do
   not deploy a SNAPSHOT any more.
2. Go to **Releases -> Draft a new release**, type `0.0.2` under
   "Choose a tag" and select "Create new tag on publish". A saved draft
   does not create the tag yet, so a draft is a safe intermediate state.
3. Leave **Target: `master`** (the default) - the tag will point at the
   merge commit.
4. Click **Publish release**. Publishing creates the tag, and the tag
   triggers the `release` workflow.
5. Open a pull request that sets the next development version, for example
   `-Drevision=0.0.3` and `-Dchangelist=-SNAPSHOT`, and merge it. SNAPSHOT deploys resume.

The command-line equivalent of steps 2 to 4 is
`gh release create 0.0.2 --target master --generate-notes`.

The `release` workflow refuses a tag that does not exactly match `revision`
in `.mvn/maven.config`, and refuses a non-empty `changelist`, so a tag alone can
never publish an unintended version. It builds and tests the repository,
then signs the artifacts and publishes them to Maven Central automatically.
There is no manual step after the tag, so the tag check is the only gate.
The built jars and test reports are kept as workflow artifacts.

The workflows use the Central portal token and the GPG key that the Eclipse
Foundation provides as repository secrets (`CENTRAL_SONATYPE_TOKEN_USERNAME`,
`CENTRAL_SONATYPE_TOKEN_PASSWORD`, `GPG_PRIVATE_KEY`, `GPG_PASSPHRASE`).
