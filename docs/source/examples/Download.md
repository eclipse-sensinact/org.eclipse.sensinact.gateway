# Downloading and Launching sensiNact

This worked example aims to get you started with running sensiNact for the first time.

## Downloading sensiNact

Eclipse sensiNact is published to the Eclipse Nexus repository instance at [https://repo.eclipse.org](https://repo.eclipse.org). You can find it directly there, or use the maven-dependency plugin to find it for you.

```bash
mvn dependency:get \
    -DremoteRepositories=https://repo.eclipse.org/content/groups/sensinact/ \
    -Dartifact=org.eclipse.sensinact.gateway.distribution:assembly:0.0.2-SNAPSHOT:zip \
    -Ddest=sensinact.zip
```

## Installing sensiNact

Once you have obtained the sensiNact assembly you can unzip it using your favourite zip tool

    unzip sensinact.zip

The unzipped contents will include several folders and files including:

* `configuration` - the folder containing the configuration for the gateway instance
* `features` - the folder containing the available features that can be installed into this gateway
* `launch` - the folder containing the sensiNact launcher responsible for bootstrapping the gateway
* `repository` - a maven layout repository containing the bundles used by the gateway features
* `start.sh` - a launch script for sensiNact

## Starting sensiNact

Starting sensiNact can be achieved simply by executing the start script

    ./start.sh

```{warning}
If you are unable to execute the start script try setting the executable permission bit using `chmod +x start.sh`
```

Once you have started sensiNact you should see some logs written to the console indicating that the gateway is running. In order to interact with the gateway you will need to [configure a northbound provider](Configuring.md)
