# The Eclipse sensiNact gateway launcher

The Launcher is the one part of the sensiNact gateway that you are guaranteed to use. At a high level It has two main functions:

1. Deploying Features
2. Managing Configuration

The Eclipse sensiNact gateway uses an OSGi<sup>®</sup> runtime and related specifications, therefore when we talk about *features* we are referring to the standard [Feature Service](https://docs.osgi.org/specification/osgi.cmpn/8.0.0/service.feature.html) definition of a feature, and when we talk about *configurations* we are referring to the [Configuration Admin Service](https://docs.osgi.org/specification/osgi.cmpn/8.0.0/service.cm.html) definitions.


The overall functional layout is as follows:

![The sensiNact gateway launcher architecture](../_static/distribution/architecture_diagram.png){width=800px}

## The Eclipse sensiNact gateway feature manager

The feature manager is responsible for finding, loading, installing and uninstalling features within the gateway's OSGi<sup>®</sup> framework instance.

### Configuring the feature manager

The feature manager is configured in the same way as any other gateway component, using the `configuration.json` file.

The name of the configuration is `sensinact.launcher` and it defines three properties:

* `features` - a list of features to install, in the order that they are to be installed
* `repository` - a Maven repository containing bundles and optionally features for installation. This can be a single folder or a list of folders.
* `featureDir` - a folder containing feature files that are candidates for installation. This can be a single folder or a list of folders.

An example configuration might look like the following:

```js
    "sensinact.launcher": {
      "features": [
        "core-feature",
        "jakarta-servlet-whiteboard-feature",
        "jakarta-rest-whiteboard-feature",
        "gogo-shell-feature"
      ],
      "repository": "target/it/repository",
      "featureDir": "target/it/features"
    }
```

### Features provided by the Eclipse sensiNact gateway

The Eclipse sensiNact gateway includes a number of features which can be deployed:

* `core-feature` - This feature contains the core of the gateway runtime, such as the resource model, notifications and push-based data updates.
* `gogo-shell-feature` - This feature provides the gogo shell and related commands
* `jakarta-servlet-whiteboard-feature` - This feature provides a servlet whiteboard implementation
* `jakarta-rest-whiteboard-feature` - This feature provides a REST whiteboard implementation
* `northbound-rest-feature` - This feature provides a northbound API using REST

### The feature lifecycle

A feature follows approximately the following lifecycle:

1. Finding - feature files are often found in the `features` directory, where they are named `<artifactId>.json`. They may also be found in the `repository` directory, where they are named according to the standard [Maven repository layout](https://maven.apache.org/repository/layout.html). If multiple directories are listed in the `repository` configuration, the first matching feature found will be returned.
2. Loading - features are loaded using a standards compliant Feature Service implementation. Feature extensions are not supported, except where specifically stated, by the feature manager. Therefore features with mandatory kind feature extensions will not be processed further.
3. Installing - the bundles listed in the feature are installed in the order defined by the feature. Each bundle is located by searching the `repository` directories. If a bundle is already installed (for example by another feature) or no suitable bundle can be found in the repository then installation is skipped. Once all bundles have been installed or skipped the newly installed bundles are started.
4. Uninstalling - the bundles listed in the feature are uninstalled in the reverse of the order defined by the feature. If a bundle is required by another feature then it will not be uninstalled. The bundles to be uninstalled are all stopped, then all uninstalled, in the defined order.

If the list of features installed in the gateway changes at runtime then:

1. The removed features are identified, and removed in the reverse order of installation
2. The remaining features are processed in order and either:
    - If the feature is already present they are checked for changes. If the feature model has changed the runtime is updated by removing and re-adding the feature
    - If the feature is new then it is installed


### Adding feature dependencies

Features do not exist in isolation, and most gateways will deploy multiple features simultaneously. If these features are to interact then there will typically be a dependency between them. There is no standard way to model dependencies between features, therefore Eclipse sensiNact provides a feature extension named `sensinact.feature.depends` which can be used for this purpose.

The `sensinact.feature.depends` extension is of type `artifacts`, and if present it is always enforced, regardless of the kind of the extension. The artifacts listed are the ids of the features upon which the feature depends. For example:

```js
    {
      // Bundles skipped for brevity
      "extensions": {
        "sensinact.feature.depends": {
          "kind": "mandatory",
          "type": "artifacts",
          "artifacts": [
            {
              "id": "org.eclipse.sensinact.gateway.distribution.features:jakarta-servlet-whiteboard-feature:osgifeature:0.0.1-SNAPSHOT"
            }
          ]
        }
      }
    }
```

When dependencies are expressed in this way then all dependencies must be installed before the dependent feature can successfully install. If any dependencies are missing then the feature will not be installed.


### Configuration in features

The Feature Service provides features with a way to include configuration inside the feature, as defined in [Feature Service Specification](https://docs.osgi.org/specification/osgi.cmpn/8.1.0/service.feature.html).
Configuration variables are also supported, but only with those declared in the feature description.
It is possible to define a configuration-only feature, in which case the `bundles` can be omitted from the file.

Here is an example of configuration in a feature:

```js
  {
    "feature-resource-version": "1.0",
    "id": "org.example:feature_id:1.0.0",
    "configurations": {
      "pid1": {
        "text": "Some string value",
        "value": 42
      },
      "sensinact.http.device.factory~1": {
        "tasks.oneshot": [
          {
            "url": "http://example.org:${http.port}/data.json",
            "mapping": {
              "parser": "json",
              "mapping": {
                "@provider": "Name",
                "@latitude": "Latitude",
                "@longitude": "Longitude",
                "data/value": "Value"
              }
            }
          }
        ]
      }
    },
    "variables": {
      "http.port": 8080
    }
  }
```

### Adding your own features

Adding your own features can be achieved by writing a valid feature document and including it in the `features` directory, or installing it into the `repository`. It is also necessary to make sure that any bundles used in your feature are also installed into the `repository`.

## The Eclipse sensiNact gateway configuration manager

The configuration manager is responsible for keeping the runtime configuration synchronized with the gateway `configuration.json` file. Any changes made to the file are reflected in the running framework.

### The configuration file

The `configuration.json` file uses the [Configuration Resource Format](https://docs.osgi.org/specification/osgi.cmpn/8.0.0/service.configurator.html#d0e132453). The symbolic name of the configuration is `org.eclipse.sensinact.gateway` and the version is the release version of the gateway.

Comments are permitted in the configuration file using `//` line comments or `/* */` comment blocks.

An example file follows:

```js
    {
      ":configurator:resource-version": 1,
      ":configurator:symbolic-name": "org.eclipse.sensinact.gateway",
      ":configurator:version": "0.0.2-SNAPSHOT",
      "sensinact.launcher": {
        "features": [
          "jakarta-servlet-whiteboard-feature",
          "jakarta-rest-whiteboard-feature",
          "gogo-shell-feature"
        ],
        "repository": "target/it/repository",
        "featureDir": "target/it/features"
      },
      "org.apache.felix.http": {
        "org.osgi.service.http.port": 8082
      },
      "JakartarsServletWhiteboardRuntimeComponent": {
        "osgi.jakartars.name": "sensiNact.rest",
        "osgi.http.whiteboard.target": "(osgi.http.endpoint=*)"
      }
    }
```

### Setting the configuration file location

The folder containing the `configuration.json` file is found in following search pattern:

1. The `sensinact.config.dir` is queried. If it is set then this is the configuration folder
2. The configuration folder is set to `./config`

### Migrating configuration versions

Migrating configuration versions is not currently supported and must be performed manually.
