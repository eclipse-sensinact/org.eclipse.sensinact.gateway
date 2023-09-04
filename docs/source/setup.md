# Installation and configuration

## Platform Recommendations

Eclipse sensiNact can work in various environment, from a Raspberry Pi 1 to a cloud instance.
Its requirements will depend mainly on the number of updates and queries it must handle.

The platform **requires** to run in a Java 17+ environment, with a minimum of 256MB of Java Heap space, 50 MB of disk space and a single core processor.
For larger deployments with more users and sensors you will want to run with at least 2GB of available heap and a multi-core processor.

## Installation

The installation of Eclipse sensiNact is pretty simple: just unzip the distribution ZIP somewhere and configure it according to your need.

For the rest of this document, we consider that Java 17 is installed.

Eclipse sensiNact has been tested on Linux, Mac OS and Windows.

### Get the distribution

Eclipse sensiNact is published to the Eclipse Nexus repository instance at [repo.eclipse.org](https://repo.eclipse.org) after each successful repository update.

#### Download the distribution manually

If you just want to download the distribution file without having installed Maven, you can obtain it using this [search link](https://repo.eclipse.org/#nexus-search;gav~org.eclipse.sensinact.gateway.distribution~assembly~~~).

For now, we don't provide a stable link to the latest version of Eclipse sensiNact.

#### Download the distribution with Maven

If Maven is installed, you can download the distribution ZIP file using the maven-dependency plugin:

```bash
mvn dependency:get \
    -DremoteRepositories=https://repo.eclipse.org/content/groups/sensinact/ \
    -Dartifact=org.eclipse.sensinact.gateway.distribution:assembly:0.0.2-SNAPSHOT:zip \
    -Ddest=sensinact.zip
```

#### Compiling the project

Finally, you can clone and compile the project to generate the distribution ZIP file:

1. Clone the project [repository](https://github.com/eclipse/org.eclipse.sensinact.gateway): `git clone https://github.com/eclipse/org.eclipse.sensinact.gateway.git`
2. Compile it using Maven: `mvn clean install`
   ```{note}
   Some integration tests require Docker to run, for example to test the Timescale history provider.
   If Docker is not available, those tests will be skipped.
   ```
3. Get the distribution ZIP file at `distribution/assembly/target/gateway.zip`

### Distribution architecture

Once you have obtained the sensiNact assembly you can unzip it using your favourite zip tool

```bash
unzip sensinact.zip
```

The unzipped contents will include several folders and files including:

* `configuration` - the folder containing the configuration for the gateway instance
* `features` - the folder containing the available features that can be installed into this gateway
* `launch` - the folder containing the sensiNact launcher responsible for bootstrapping the gateway
* `repository` - a maven layout repository containing the bundles used by the gateway features
* `start.sh` - a launch script for sensiNact

## Configuration

The sensiNact configuration file format is described [in detail here](distribution/Launcher.md#the-configuration-file), but the important facts are that:

1. The configuration file lives in the `configuration` folder
2. The configuration file is `JSON` and uses the OSGi<sup>®</sup> [Configuration Resource Format](https://docs.osgi.org/specification/osgi.cmpn/8.0.0/service.configurator.html#d0e132453)
3. The configuration file is *live* and will be reloaded by the gateway whenever you save changes


```{admonition} **TODO**
:class: error

Possibly describe the configuration format as in Configuring
```

## Starting sensiNact

Starting sensiNact can be achieved simply by executing the start script

```bash
./start.sh
```

```{warning}
If you are unable to execute the start script try setting the executable permission bit using `chmod +x start.sh`
```

:::{note}
On Windows, you will need to run sensiNact using the following command line:

```powershell
java -D"sensinact.config.dir=configuration" -jar launch\launcher.jar
```
:::

Once you have started sensiNact you should see some logs written to the console indicating that the gateway is running. In order to interact with the gateway you will need to [configure a northbound provider](examples/Configuring.md)
