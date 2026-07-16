# ADR: Storage backend for the reworked history provider

- Status: **proposed** (evaluation evidence complete, decision pending committer review)
- Date: 2026-07-15
- Context: history provider rework (see `HISTORY_PROVIDER_REWORK_PLAN.md`, work stream A);
  spike code and raw evidence on branch `evaluation/history-backends`
  (`southbound/history/history-storage-spikes/`, incl. `EVALUATION_NOTES.md`)

## Problem

The current TimescaleDB history provider loses data fidelity (GeoJSON limited to `Point`
via a `geography(POINT,4326)` column, everything else `toString()`; `BigDecimal` narrowed
to `long`/`double` on read), offers no aggregation or query pushdown, and has no
retention API. The rework introduces a backend-agnostic storage SPI; this ADR selects the
backend for the reference implementation, with lossless round-trip of GeoJSON
(incl. `FeatureCollection`), arbitrary-precision numerics, aggregation, sort/paging
pushdown, and prune/retention as hard requirements.

## Baseline findings (invalidating parts of the original problem statement)

1. The Point-only geography, `toString()` losses, and numeric narrowing are **sensiNact
   schema/code choices, not TimescaleDB limitations** — proven empirically: a unified
   single-table schema on the same database passes the full fidelity contract (below).
2. The "single shared connection, no pooling" pain point is **untuned configuration**:
   Aries `tx-control-provider-jdbc-local` embeds HikariCP and accepts pool settings
   (`osgi.connection.max`, …) via the properties map the current code passes as `null`;
   the shared `Connection` is a scoped proxy over the pool.

## Evidence

A 33-test contract suite (T1 round-trip fidelity incl. FeatureCollection/BigDecimal-30-digit/
NaN/±Infinity/exact Java types; T2 query semantics incl. paging, bound inclusivity, value-filter
pushdown; T3 time-bucket aggregation; T4 age/keep-count/scoped prune) was run against
candidate adapters, plus a 1M-row benchmark:

| | postgres:16 (unified schema, date_bin) | + TimescaleDB (hypertable, time_bucket) | MongoDB 7 time-series |
|---|---|---|---|
| Contract suite (33 tests) | **pass** | **pass** | **pass** (with caveats below) |
| Ingest (batch) | 115k rows/s | 105k rows/s | not benchmarked (contract-level spike) |
| range DESC limit 500 / latest / point-in-time | ≤1 ms | ≤1 ms | — |
| count of 1M rows | 59 ms | 44 ms | — |
| aggregate 1h buckets over 30 days | 788 ms | **382 ms** | — |
| deep OFFSET (500k) | 177 ms | 183 ms | — |

Desk evaluation eliminated **InfluxDB** (no decimal type in any version, no JSON/GeoJSON,
v3 query-window caps, Java clients without OSGi manifests and dash-licenses "restricted",
no testcontainers for v3) and **QuestDB** (Apache-2 server and genuine `SAMPLE BY`, but
JSON/GeoJSON as VARCHAR only, geohash-only geo, fixed-scale DECIMAL new in 9.2) for primary
storage. MongoDB caveats: Decimal128 caps at 34 significant digits (full fidelity needs a
parallel raw-string field, excluded values silently drop out of pushdown/aggregation),
BSON timestamps are millisecond-only (the current provider stores microseconds),
time-series updates remain metaField-only, keep-count prune needs client-side passes,
and the server is SSPL (an optics question for Eclipse documentation, not an IP blocker —
the Java driver itself is Apache-2, OSGi-ready, and dash-approved).

Licensing facts (verified, incl. an Eclipse dash-licenses run):
- pgjdbc: BSD-2, OSGi bundle, approved in Eclipse's IP database (IPLab #11681).
- TimescaleDB Apache-2 edition covers hypertables, `time_bucket`, and `drop_chunks`;
  continuous aggregates, compression, `add_retention_policy`, and `time_bucket_gapfill`
  are TSL-only. Since the reworked housekeeping scheduler drives pruning itself
  (`drop_chunks`/`DELETE`), **the Apache-2 edition suffices**.
- Docker: default `timescale/timescaledb` tags are Community (TSL); `-oss` tags are
  Apache-2-only. Documentation must stop pointing exclusively at `timescaledb-ha` (TSL).

## Decision (proposed)

**Stay on PostgreSQL. Replace the three-table schema with the unified single-table schema
(type discriminator + exact-Java-type column + `NUMERIC` + `JSONB`). Treat TimescaleDB as
an optional accelerator (candidate B): use `time_bucket`/hypertables when the extension is
present, plain PostgreSQL `date_bin` otherwise. Do not switch database products.**

Rationale: every hard requirement is met on the database already deployed, with zero new
IP review, zero new operational burden for users, and a straightforward data migration.
The only measurable Timescale advantage in our workload (2× on bucket aggregation) is
available opt-in without making the extension mandatory. MongoDB, the only credible
challenger, passes the contract only with fidelity workarounds and brings a second
operational stack plus SSPL optics for no capability gain.

Design consequences carried into the rework:
- PG14 is the minimum server version (`date_bin`, ±Infinity in `NUMERIC`); the numeric
  column must be unconstrained `NUMERIC` (no precision/scale) to store ±Infinity.
- Keep `HistoryQuery` additive for keyset pagination (`after(Instant)`) — deep `OFFSET`
  costs ~180 ms at offset 500k on both variants.
- Recommend `-oss` Timescale images (or plain `postgres`) in documentation.

## Migration outline

One-time SQL migration from `sensinact.numeric_data`/`text_data`/`geo_data` into the
unified table: numeric rows map directly (`value_num`), text rows to JSONB strings, geo
rows via `ST_AsGeoJSON` into JSONB. Legacy `java_type` is unknown for old rows — reads
fall back to the current narrowing heuristic (scale ≤ 0 → `Long`, else `Double`),
preserving today's observable behavior for pre-migration data.

## Rejected alternatives

- **InfluxDB** — fails type fidelity (G2), OSGi/IP readiness (G3), testcontainers for v3 (G4).
- **QuestDB** — fails JSON/GeoJSON typing; DECIMAL too new/fixed-scale; would keep pgjdbc anyway.
- **MongoDB** — passes functionally, loses on fidelity workarounds, ms-only timestamps,
  restricted updates, second ops stack, SSPL optics.
- **ClickHouse / IoTDB / VictoriaMetrics / CrateDB** — rejected at desk level (ops fit,
  ecosystem risk, float-only models, no advantage over PostgreSQL).
- Embedded engines (SQLite/H2) — out of scope; the storage SPI deliberately permits a
  future embedded backend.
