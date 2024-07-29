# Installation

## Requirements

Eclipse sensiNact can work in various environment, from a Raspberry Pi 1 to a cloud instance.
Its requirements will depend mainly on the number of updates and queries it must handle.
Eclipse sensiNact has been tested on Linux, Mac OS and Windows.

Minimum requirements
* Linux, MacOS or Windows
* Java 17
* 256MB Java heap space
* 50MB disk space

Recommended requirements to support more users and sensors
* 2GB Java heap space
* multi-core processor

## Get the distribution zip file

Eclipse sensiNact is published to the Eclipse Nexus repository instance at [repo.eclipse.org](https://repo.eclipse.org) after each successful repository update.
You may either get the latest public distribution or generate the distribution from source.

### Get the zip file with a browser (easy)

You may download the distribution with your web browser via the [search link](https://repo.eclipse.org/#nexus-search;gav~org.eclipse.sensinact.gateway.distribution~assembly~~~). Just download the artifact (assembly-XXX.zip file) to your computer.

### Get the zip file with Maven (medium)

You can use maven to get the distribution zip file. You will need maven with the maven-dependency plugin.

Get the sensinact.zip file:
```bash
mvn dependency:get \
    -DremoteRepositories=https://repo.eclipse.org/content/groups/sensinact/ \
    -Dartifact=org.eclipse.sensinact.gateway.distribution:assembly:0.0.2-SNAPSHOT:zip \
    -Ddest=sensinact.zip
```

### Generate the zip file from source (hard)

You can clone and compile the project to generate the distribution ZIP file. You will need git, maven and a java jdk.

1. Clone the project [repository](https://github.com/eclipse/org.eclipse.sensinact.gateway): `git clone https://github.com/eclipse/org.eclipse.sensinact.gateway.git`
2. Compile it using Maven: `mvn clean install`
   ```{note}
   Some integration tests require Docker to run, for example to test the Timescale history provider.
   If Docker is not available, those tests will be skipped.
   ```
3. The distribution ZIP file will be generated at `distribution/assembly/target/gateway.zip`

## Install the distribution zip file

Once you have the distribution zip file (it may be named sensinact.zip, gateway.zip or assembly-XXX.zip), create a "sensiNact" directory on your computer and extract the zip file to that directory.

Extract the zip file with your favorite zip tool or use the following command:
```bash
unzip sensinact.zip
```

### File architecture

The zip will extract files and directories including:

* `configuration` - the folder containing the configuration for the gateway instance
* `features` - the folder containing the available features that can be installed into this gateway
* `launch` - the folder containing the sensiNact launcher responsible for bootstrapping the gateway
* `repository` - a maven layout repository containing the bundles used by the gateway features
* `start.sh` - a launch script for sensiNact

## Start sensiNact

Run the start.sh script to start sensiNact

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

Once you have started sensiNact you should see some logs written to the console indicating that the gateway is running.

You may now follow the quick-start tutorial and [configure a northbound provider](quick-start/Northbound.md)
