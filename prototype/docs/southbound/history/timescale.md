# Timescale history provider

Eclipse sensiNact comes with an history provider based on [Timescale](https://www.timescale.com/) and [PostGIS](https://postgis.net/), [PostGreSQL](https://www.postgresql.org/) extensions that respectively add support for time series and geospatial data.

The timescale database configuration and launch are outside the scope of Eclipse sensiNact.

## Bundles

The Timescale history provider for sensiNact is provided by bundle `org.eclipse.sensinact.gateway.southbound.history:timescale-provider:0.0.2`.

It requires the sensiNact core feature bundles, the sensiNact history API bundle, OSGi JDBC API and Apache Aries transaction control and the PostgreSQL JDBC driver:
* `org.eclipse.sensinact.gateway.southbound.history:history-api:0.0.2`
* `org.osgi:org.osgi.service.jdbc:1.0.0`
* `org.postgresql:postgresql:42.5.1`
* `org.apache.aries.tx-control:tx-control-service-local:1.0.1`
* `org.apache.aries.tx-control:tx-control-provider-jdbc-local:1.0.1`

## Configuration

The Timescale history provider expects a configuration with PID `sensinact.history.timescale` with the following entries:

* `url`: JDBC URL to the PostgreSQL instance with both Timescale and PostGIS extensions enabled
* `user`: JDBC user to connect the database
* `.password`: JDBC password to connect the database
* `provider`: Name of the sensiNact provider that will provide the history actions (`sensiNactHistory` by default).

The JDBC user must have the rights to call the following SQL statements:
* `CREATE SCHEMA sensinact`
* `CREATE TABLE sensinact.numeric_data`: creates the table that will hold numeric resources values
* `CREATE TABLE sensinact.text_data`: creates the table that will hold string resources values
* `CREATE TABLE sensinact.geo_data`: creates the table that will hold location-based resources values

## Provided actions

The Timescale History provider implements the [Eclipse sensiNact History Provider actions](./history.md#provider-actions).
No additional actions are defined.

## Sample configuration

First, it is required to have a Timescale instance up and running.
Here is an example of running a Docker container based on the [timescaledb-ha](https://hub.docker.com/r/timescale/timescaledb-ha/) image:

```bash
docker run --rm -d --name timescale-db -p 5432:5432 \
    -e POSTGRES_DB=sensinactHistory \
    -e POSTGRES_USER=snaHistory \
    -e POSTGRES_PASSWORD=test.password \
    timescale/timescaledb-ha:pg15-all-amd64
```

The matching history provider configuration with PID `sensinact.history.timescale` could be:
```json
{
    "url": "jdbc://localhost:5432/sensinactHistory",
    "user": "snaHistory",
    ".password": "test.password",
    "provider": "history"
}
```
