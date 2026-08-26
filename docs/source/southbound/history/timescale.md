# PostgreSQL / Timescale history provider

Eclipse sensiNact comes with a history storage backend based on [PostgreSQL](https://www.postgresql.org/),
optionally accelerated by the [TimescaleDB](https://www.timescale.com/) extension for time-series data.
The extension is detected at startup: when it is available the history table becomes a hypertable and
aggregation uses `time_bucket`; on plain PostgreSQL (14 or newer) the provider falls back to the built-in
`date_bin`. PostGIS is no longer required — GeoJSON values of any type (including `FeatureCollection`)
are stored losslessly as JSONB.

The database configuration and launch are outside the scope of Eclipse sensiNact. The storage decision
and its evaluation evidence are documented in the [storage ADR](./history-storage-adr.md).

## Bundles

The provider is contained in bundle `org.eclipse.sensinact.gateway.southbound.history:timescale-provider`.

It requires the sensiNact core feature bundles, the history API and engine bundles, OSGi JDBC API,
Apache Aries transaction control and the PostgreSQL JDBC driver:
* `org.eclipse.sensinact.gateway.southbound.history:history-api`
* `org.eclipse.sensinact.gateway.southbound.history:history-core`
* `org.osgi:org.osgi.service.jdbc:1.0.0`
* `org.postgresql:postgresql:42.7.9`
* `org.apache.aries.tx-control:tx-control-service-local:1.0.1`
* `org.apache.aries.tx-control:tx-control-provider-jdbc-local:1.0.1`

All of them are part of the `timescale-history-provider-feature`.

## Configuration

The provider expects a configuration with PID `sensinact.history.timescale` with the following entries:

* `url`: JDBC URL to the PostgreSQL instance
* `user`: JDBC user to connect the database
* `.password`: JDBC password to connect the database
* `provider`: Name of the sensiNact provider that offers the history actions and of the
  `HistoryProvider` service (`timescale-history` by default)
* `include.resources`: array of JSON-encoded resource selectors choosing the resources to historize
  (defaults to everything)
* `exclude.resources`: array of JSON-encoded resource selectors excluding resources after the include
  selection
* `max.page.size`: largest page a single range query returns (10000 by default)

Changes to the selector properties take effect immediately, without restarting the database connection.
For richer runtime filtering — including change-based suppression (deadband) — see the historization
filters of the history engine ([history documentation](./history.md)).

The JDBC user must have the rights to execute:
* `CREATE SCHEMA sensinact`
* `CREATE EXTENSION timescaledb` (only when the extension is available)
* `CREATE TABLE sensinact.history`: the unified table holding all historized values
* `ALTER TABLE` / `INSERT` on the legacy `sensinact.numeric_data` / `text_data` / `geo_data` tables
  if a pre-existing database is migrated (see below)

## Data migration from older versions

Databases written by the previous provider version (three tables: `numeric_data`, `text_data`,
`geo_data`) are migrated automatically on first start: all rows are copied into
`sensinact.history` and the old tables are renamed with a `_migrated` suffix — they can be dropped
once the migration is verified. Migrated numeric values keep their historical read shape
(whole numbers as `Long`, everything else as `double`); values stored after the migration
additionally preserve their exact Java type.

## Provided services and actions

Through the history engine the backend is available to OSGi consumers as a
[`HistoryProvider` service](./history.md#the-historyprovider-service) and implements the
[legacy ACT actions](./history.md#legacy-act-actions). No additional actions are defined.

## Sample configuration

First, a PostgreSQL instance is required — with or without the TimescaleDB extension. Here is an
example of running a Docker container based on the Apache-2-licensed
[timescaledb](https://hub.docker.com/r/timescale/timescaledb/) image (prefer the `-oss` tags; the
default tags and the `timescaledb-ha` image contain Timescale-License features the provider does
not use):

```bash
docker run --rm -d --name timescale-db -p 5432:5432 \
    -e POSTGRES_DB=sensinactHistory \
    -e POSTGRES_USER=snaHistory \
    -e POSTGRES_PASSWORD=test.password \
    timescale/timescaledb:latest-pg16-oss
```

The matching history provider configuration with PID `sensinact.history.timescale` could be:
```json
{
    "url": "jdbc:postgresql://localhost:5432/sensinactHistory",
    "user": "snaHistory",
    ".password": "test.password",
    "provider": "history"
}
```
