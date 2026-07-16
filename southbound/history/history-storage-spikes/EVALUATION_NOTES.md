# Storage Backend Evaluation — Working Notes

Evidence collected for the ADR (see `HISTORY_PROVIDER_REWORK_PLAN.md`, work stream A).
Branch: `evaluation/history-backends`. This module is throwaway and never released.

## S0 — Connection pooling (2026-07-15)

`tx-control-provider-jdbc-local` 1.0.1 **embeds shaded HikariCP** (`com.zaxxer.hikari` inside the bundle).
The `JDBCConnectionProviderFactory` API accepts pool configuration via the map that
`TimescaleHistoricalStore.createProvider()` currently passes as `null`:
`osgi.connection.pooling.enabled` (default true), `osgi.connection.max` (default 10),
`osgi.connection.min`, `osgi.connection.timeout`, `osgi.idle.timeout`, `osgi.connection.lifetime`.
The "single shared Connection" is a thread-safe scoped proxy that checks a pooled physical
connection out per `txControl.required(...)` scope.

**Conclusion: the "no pooling" pain point is untuned configuration, not a missing capability.**

## Contract test results (2026-07-15)

`HistoryStorageContractTest` (33 tests: T1 round-trip fidelity, T2 query semantics,
T3 aggregation, T4 prune) passes on:

| Backend | Image | Notes |
|---|---|---|
| In-memory reference | — | validates the suite itself |
| Unified Postgres schema | `postgres:16-alpine` | `date_bin` aggregation |
| Same schema + Timescale | `timescale/timescaledb:latest-pg16` | hypertable + `time_bucket` |

Verified empirically on the unified single-table schema (`value_kind` discriminator +
`java_type` + `NUMERIC` + `JSONB`):

- GeoJSON `Point`, `Polygon`, `Feature`, `FeatureCollection` round-trip losslessly (JSONB).
- `BigDecimal` with 30+ digits, `NaN`, `±Infinity`, and the exact original Java types survive.
- Value-filter pushdown works in SQL including BigDecimal-precision comparison.
- Age / keep-count / scoped prune works on plain tables and hypertables.

**G2 (lossless round-trip) passes for candidates A and B. The current provider's GeoJSON and
precision losses are schema choices, not database limitations.**

NOTE: the test image should move to an `-oss` tag (e.g. `timescale/timescaledb:2.x-pgXX-oss`)
— see licensing findings below; default `timescale/timescaledb` tags are Community (TSL).

## Desk evaluation (web research, verified 2026-07-15)

### InfluxDB — reject (documented, no code spike)
- Licenses fine (v2 MIT; v3 Core MIT/Apache-2 dual) but: **no decimal type in any version**;
  no JSON/GeoJSON (strings ≤64 KB in v1/v2/serverless; v3 limit unverified); Flux in
  maintenance and absent from v3; v3 Core single query capped by Parquet-file-count
  (default ≈72 h of data); `influxdb:latest` Docker tag flips to v3 on 2026-09-15.
- **Both Java clients lack OSGi manifests** and are dash-licenses "restricted"
  (influxdb-client-java 7.3.0 = NOASSERTION, influxdb3-java 1.2.0 = never harvested);
  v3 client drags Arrow Flight + gRPC. **No testcontainers module for v3.**
- Fails: type fidelity (G2), OSGi (G3, wrapping needed), testcontainers for v3 (G4).

### QuestDB — reject for primary storage (documented)
- Server genuinely Apache-2.0 (Enterprise is a separate product). `SAMPLE BY` + `FILL` is a
  real time_bucket equivalent; TTL + `DROP PARTITION` retention (whole partitions only);
  standard pgjdbc works (forward-only cursors, `LIMIT lo,hi` dialect).
- But: **no JSON column type** (VARCHAR + `json_extract`), **geohash-only** geo (GIS issue
  #4899 open), DECIMAL new in 9.2 (2025) is fixed precision/scale per column (≤76 digits,
  no NaN/Inf; JDBC BigDecimal mapping unverified).
- dash-licenses: `org.questdb/questdb` restricted (never harvested) — irrelevant if only
  pgjdbc is used, but the type-system gaps already fail the elegance bar for G2.

### MongoDB — credible challenger, contract-level spike justified
- Client side is clean: mongodb-driver-sync/core/bson 5.5.1 all **Apache-2.0, dash-approved,
  real OSGi bundles**. Mature testcontainers module.
- Server **SSPL v1** (not OSI-approved) — optics/doc question for an Eclipse project, not an
  IP-review blocker (server is not shipped).
- Type fidelity caveats: Decimal128 = **34 significant digits** (BigDecimal beyond that cannot
  round-trip); FeatureCollection storable but **not indexable** as geometry (2dsphere covers
  geometry types only); number-type mapping needs a deliberate codec choice.
- Time-series collections: deletes OK since 7.0; **updates still metaField-only through 8.x**
  (cannot correct a measurement); no unique indexes; TTL is bucket-granular via a 60 s
  background task.

### PostgreSQL / TimescaleDB — evaluation leader
- PG NUMERIC: effectively unlimited precision; NaN storable, **±Infinity since PG14**
  (requires unconstrained NUMERIC — no `NUMERIC(p,s)`). JSONB lossless for nested JSON and
  all GeoJSON. `date_bin()` built-in since PG14 (stride must not contain month/year units).
- TimescaleDB licensing (2026, company now "TigerData"; TSL → "Tiger Data License"):
  - **Apache-2.0**: hypertables, `drop_chunks`, `time_bucket`, first/last/histogram.
  - **TSL-only**: continuous aggregates, compression/columnstore, `add_retention_policy`,
    `time_bucket_gapfill`.
  - Retention on Apache-2: manual/self-scheduled `drop_chunks` works — only the policy
    automation is TSL. Our housekeeping scheduler calls prune itself, so **Apache-2 suffices**.
  - Docker: default `timescale/timescaledb` tags = Community (TSL); **`-oss` tags are
    Apache-2-only**; `timescaledb-ha` = TSL by default.
- pgjdbc: BSD-2, ships as OSGi bundle, **approved in Eclipse's own IP DB (IPLab #11681)**.

### dash-licenses run (1.1.0, 2026-07-15)
```
org.mongodb/mongodb-driver-sync/5.5.1   Apache-2.0                   approved   clearlydefined
org.mongodb/mongodb-driver-core/5.5.1   Apache-2.0                   approved   clearlydefined
org.mongodb/bson/5.5.1                  Apache-2.0                   approved   clearlydefined
org.postgresql/postgresql/42.7.7        BSD-2 AND Apache-2.0         approved   IPLab #11681
com.influxdb/influxdb-client-java/7.3.0 NOASSERTION                  restricted
com.influxdb/influxdb3-java/1.2.0       (none)                       restricted
org.questdb/questdb/9.2.1               (none)                       restricted
```

### Flagged / unverified
- InfluxDB 3 Core max string field size.
- QuestDB DECIMAL → BigDecimal mapping over pgwire/JDBC (feature < 1 year old).
- MongoDB 8.3 rapid-release date (third-party source).

## Benchmark results (2026-07-15, local docker, 1M rows hot resource + 100k across 1k resources)

Run: `mvn test -pl southbound/history/history-storage-spikes -Dtest=PostgresBenchmarkTest -Dspike.benchmark=true`

| Operation | postgres:16 (date_bin) | timescale-pg16 (time_bucket) |
|---|---|---|
| ingest (JDBC batch 10k, reWriteBatchedInserts) | 115k rows/s | 105k rows/s |
| range ASC offset=500k limit=500 | p50 177 ms | p50 183 ms |
| range DESC limit=500 | p50 0.8 ms | p50 0.9 ms |
| latestValue | p50 0.1 ms | p50 0.1 ms |
| valueAt (point-in-time) | p50 0.1 ms | p50 0.1 ms |
| count (1M rows) | p50 59 ms | p50 44 ms |
| aggregate 1h buckets / 30 days | p50 788 ms | **p50 382 ms** |

Findings:
- Both variants are comfortably fast for the SensorThings workload; ingest overhead of the
  hypertable is ~9%.
- Timescale's chunk exclusion makes `time_bucket` aggregation ~2× faster — the argument for
  "Timescale as optional accelerator" (candidate B) rather than a requirement.
- Deep `OFFSET` costs ~180 ms at offset 500k (classic OFFSET degradation, identical on both).
  Design consequence for the new API: keep `HistoryQuery` additive for future **keyset
  pagination** (e.g. `after(Instant)`) — pages by time predicate instead of OFFSET.

## MongoDB contract spike result (2026-07-15)

`MongoHistoryStorage` (time-series collection, metaField `meta`, `$dateTrunc` aggregation,
delete-based prune) **passes all 33 contract tests** on `mongo:7.0`. Implementation notes:

- Numbers store a parallel `Decimal128` field for pushdown/aggregation **plus** the canonical
  string for reconstruction — Decimal128 alone caps at 34 significant digits, so full
  BigDecimal fidelity requires the same string trick Postgres needs for NaN/±Inf. Values
  beyond 34 digits silently lose pushdown/aggregation participation.
- BSON `Date` is **millisecond precision** — today's provider stores microseconds; a Mongo
  backend would need a separate high-resolution timestamp field or accept precision loss
  (the +1ms SensorThings issue class again).
- Keep-count prune requires a per-resource client-side pass (no window functions).
- GeoJSON/objects round-trip; stored as JSON strings in the spike (native BSON possible,
  contract-equivalent).

## Still to do
- [ ] ADR in docs/source/southbound/history/ with matrix + decision (draft in progress).
