# History Provider Rework — Plan

> Status: **in implementation** — M0 merged (#755); stream A evaluation complete with ADR
> *proposed* (`docs/source/southbound/history/history-storage-adr.md` on branch
> `evaluation/history-backends`: stay on Postgres, unified schema, Timescale optional); M1 started.
>
> Companions: [HISTORY_STA_GAP_ANALYSIS.md](HISTORY_STA_GAP_ANALYSIS.md) (STA feature gaps) and
> [NOTIFICATION_PROXY_ALIGNMENT.md](NOTIFICATION_PROXY_ALIGNMENT.md) (third-party ingest-filter
> alignment — source of the exported `HistoryIngestFilter` contract in M1).

## Context

The sensiNact history provider (`southbound/history/`) stores resource updates in TimescaleDB and exposes queries only as three ACT actions (`single`, `range`, `count`) defined in `HistoricalQueries.java`. This design has accumulated problems:

- **GeoJSON is lossy**: only `Point` is stored (`geography(POINT,4326)` column); Polygon/Feature/FeatureCollection fall back to `toString()` in the text table. `ST_GeomFromGeoJSON` can't even ingest Feature/FeatureCollection.
- **Numeric precision is lost**: values stored as `NUMERIC` but read back as `long`/`double` via a `BigDecimal.scale()` heuristic (`2.0` comes back as `2L`).
- **No query pushdown** (GitHub issue #634): SensorThings fetches thousands of rows and applies `$top/$skip/$orderby` in the REST layer (`TopFilter`/`SkipFilter`/`OrderByFilter` slice the built `ResultList`). PR #616 tried pushdown and died from combinatorial SQL-template explosion — the current impl already has ~13 near-duplicate templates (3 verbs × 4 time variants × 3-table UNION ALL).
- **Awkward contract**: 500-row cap with a magic 501st `DefaultTimedValue.EMPTY` "more data" marker; every consumer re-implements count+skip pagination loops; discovery is by provider-name string convention (`sensinact.history.provider`), not service lookup; a `+1ms` hack compensates micro-vs-millisecond timestamp precision (`ObservationsDelegateSensinact.java:73-78`).
- Known bug: `@Modified update()` in `TimescaleHistoricalStore` ignores `include.resources`/`exclude.resources` changes.

**Decisions so far**: requirements issue is #634; storage backend gets an *evaluation phase with a decision gate* (suspicion: Timescale limits, esp. GeoJSON); the new primary contract is an **OSGi service interface**; existing consumers and the ACT contract **must keep working unchanged** (facade).

**Honest finding to carry into the evaluation**: the Point-only geography, the toString() losses, and the BigDecimal heuristic are sensiNact schema/code choices, *not* TimescaleDB limitations — the fallback recommendation is a unified Postgres schema (jsonb + type discriminator) with Timescale as optional accelerator. The evaluation must prove any DB switch on evidence, not "new database smell".

Branch `feature/history_provider_rework` is rebased onto master (2026-07-14, contains only this plan on top). Relevant incoming master changes accounted for below: typed-event **topic escaping** (`TopicUtils`, escaped `ICriterion.dataTopics()`, PRs #738/#741 — touches the exact `registerListener()` code M2 absorbs) and **resource-selector matching fixes** (PR #744 — the filter/housekeeping selector semantics inherit these).

## Additional requirements (to be filled in)

<!-- Collected requirements go here before the plan is finalized. -->

- [x] **Configurable historization filter (ConfigAdmin)**: a filter in front of the history provider decides per resource whether it is historized. Configurable and changeable at runtime via ConfigAdmin, independent of the storage backend. → designed in [Configurable historization filter](#configurable-historization-filter-configadmin), lands in M2.
- [x] **Configurable housekeeping of stored data (ConfigAdmin)**: retention policies that clean up stored history (by age and/or count), configurable at runtime, independent of the storage backend. → designed in [Configurable housekeeping / data retention](#configurable-housekeeping--data-retention-configadmin), SPI in M1, engine in M2, backend-native in M3.
- [x] **Change-based filtering (deadband)**: the historization filter can suppress updates whose value change is below a threshold — e.g. last stored temperature 21.0°C: an update to 21.2°C is ignored, an update to 22.0°C is stored. → designed as the *change condition* in [Configurable historization filter](#configurable-historization-filter-configadmin), lands in M2.
- [x] **Value-filter pushdown**: `$filter` predicates on the stored value (e.g. `result gt 20`) are executed by the storage backend instead of in memory (from the STA gap analysis, gap 7). → `ValueFilter` type in M1, execution + STA integration as milestone **M7**.
- [ ] TBD

---

## Work stream A — Storage backend evaluation (parallel to M1–M2, gates M3)

### Candidates

| | GeoJSON/JSON | Decimal | Aggregation | Driver/OSGi/License | Depth |
|---|---|---|---|---|---|
| **A. Unified Postgres schema** (single table: `value_type` discriminator, `java_type`, `value_num NUMERIC`, `value_json JSONB`, optional `value_geom geometry(GEOMETRY,4326)`) | jsonb: lossless incl. FeatureCollection | exact | `date_bin()` (PG14+) | unchanged stack, zero new IP review | full spike |
| **B. A + optional TimescaleDB extension** (hypertables + `time_bucket` when present, `date_bin` fallback) | same as A | same | continuous aggregates/compression are TSL-only — document the Apache-2 vs TSL split; stop recommending only `timescaledb-ha` (TSL) image | same | full spike |
| **C. MongoDB time-series collections** | native BSON docs | Decimal128 | `$dateTrunc` pipeline | driver Apache-2 OK; **server SSPL = optics question for Eclipse docs**; new resource-mgmt path (no tx-control) | contract-level spike |
| **D. InfluxDB** | no JSON type → lossy strings | **no decimal — fails** | excellent | version/license churn (v2 maintenance, Flux deprecated) | desk eval only |
| **E. QuestDB** | VARCHAR blob only, geohash-only geo | no decimal | `SAMPLE BY`, PG wire (reuses pg JDBC!) | server Apache-2 | desk eval only |

Rejected without spike (log in ADR): ClickHouse (ops-heavy), IoTDB (ecosystem risk), VictoriaMetrics (float-only), CrateDB. Keep the SPI embeddable-friendly (future SQLite/H2 backend).

### Gates (pass/fail before scoring)

- **G1** license/IP clearable (client libs EPL-2.0-compatible)
- **G2** lossless round-trip (FeatureCollection + nested JSON + BigDecimal/long/double/NaN/±Inf, value AND Java type)
- **G3** driver runs as OSGi bundle in bnd IT harness
- **G4** testcontainers support

Weighted criteria for gate-passers: aggregation/pushdown 20, query perf at 1M–10M rows 15, type fidelity ergonomics 15, self-hoster ops fit 15, impl/maintenance cost 10, ecosystem/longevity 10, spatial potential 5, ingest throughput 5, project consistency 5.

### Spike harness (throwaway module on an evaluation branch, never merged)

One shared harness, thin per-candidate adapters (~300 LoC each) against frozen "SPI v0":

1. `HistoryStorageContractTest` (abstract, parameterized): T1 round-trip fidelity suite, T2 exact `HistoricalQueries` semantics parity, T3 bucket aggregation over mixed types, T4 prune semantics (age cutoff, keep-count, selector-scoped deletes; note native retention support per candidate).
2. Benchmark (timed JUnit, fixed seed): 1M rows single resource + 100k across 1k resources; range+sort+offset+limit, latest-value, count, 1h-buckets/30d, ingest rate.
3. **S0 first (0.5d)**: verify Aries tx-control-jdbc-local already embeds HikariCP — pooling may be a config fix (`getProviderFor(ds, props)` currently passes `null`), changing the "no pooling" pain point.
4. Per-candidate license/OSGi checklist (`bnd print` manifests, dash-licenses/ClearlyDefined).

### Decision gate

- Artifact: ADR at `docs/source/southbound/history/` (baseline analysis, matrix with spike numbers, license inventory, decision, migration outline, rejected alternatives). Decided by committer PR review; flag SSPL/TSL optics to the project mailing list in the PR description.
- Timebox 3 weeks; unmeasured = "not demonstrated".
- Fallback if inconclusive: **A/B (unified Postgres schema, Timescale optional)** — zero new IP review, guaranteed migration path, fixes GeoJSON/precision/aggregation.
- Effort: ~13–16 days total (S0 0.5d; harness 3–4d; A/B 2–3d; Mongo 3d; D/E desk 1–2d; inventory 1d; ADR 2–3d).

---

## Work stream B — New architecture (milestones M0–M8, each one green reviewable PR)

### Module layout (`southbound/history/`)

- `history-api` (existing artifact, three packages): `...history.api` @Version 0.2.0→**0.2.1** (only `@Deprecated` added); NEW `...history.provider` @1.0.0 (consumer contract); NEW `...history.storage` @1.0.0 (backend SPI). No new deps (core/api + geo-json suffice).
- `history-core` (NEW, pure impl): ingestion pipeline, engine, ACT compat facade, twin-provider lifecycle, contract-test test-jar.
- `history-inmemory` (NEW): reference `HistoryStorage` — evaluation baseline, docker-free consumer ITs, demo deployments.
- `timescale-provider` (existing artifactId, PID `sensinact.history.timescale`, all config keys unchanged): shrinks to DataSource/tx-control lifecycle + `HistoryStorage` impl; **may be replaced by the evaluation outcome — only M3 is at stake**.
- Distribution: `timescale-history-provider-feature.json` gains `history-core` (+ new backend feature if the DB switches).

### New consumer contract (`HistoryProvider`, key signatures)

```java
public interface HistoryProvider {
    String PROP_NAME = "sensinact.history.provider.name";   // service property
    String getName();
    Set<HistoryCapability> getCapabilities();                // AGGREGATION, TOTAL_COUNT, ...
    Optional<TimedValue<?>> getValueAt(ResourcePath p, Instant at);  // at-or-before
    Optional<TimedValue<?>> getFirstValue(ResourcePath p);
    Optional<TimedValue<?>> getLatestValue(ResourcePath p);
    long getValueCount(ResourcePath p, TimeRange range);
    HistoryPage getValues(HistoryQuery q);                   // no cap, no magic markers
    default Stream<TimedValue<?>> streamValues(HistoryQuery q, long maxTotal);
    int getMaxPageSize();
    List<AggregateBucket> aggregate(AggregationQuery q);     // capability-gated
}
```

Supporting types:

- `ResourcePath(provider, service, resource)` record.
- `TimeRange(from, fromInclusive, to, toInclusive)` with `millisecondOf(t)` → kills the +1ms hack.
- `SortOrder` enum.
- **`HistoryQuery` builder — every optional dimension (range/order/offset/limit/valueFilter) is an independent field composed into ONE query: the anti-PR#616 device.**
- `ValueFilter` — optional declarative value predicate on the stored value: comparison (`EQ/NE/LT/LE/GT/GE`) against a typed literal, AND-combinable; `HistoryQuery.valueFilter()` defaults to none. Part of the M1 contract (so the SPI signature never changes), but execution is capability-gated (`VALUE_FILTERING`) and lands in M7 — backends without the capability cause the engine to reject the query (consumers fall back to in-memory filtering).
- `HistoryPage(values, offset, hasMore, totalCount)`.
- `AggregationQuery`/`AggregateBucket` (MIN/MAX/AVG/SUM/COUNT/FIRST/LAST per `Duration` bucket).
- Errors: unchecked `HistoryQueryException` (+ `UnsupportedHistoryOperationException`); sync API, async `Promise` defaults addable later (decide before M1 freeze).
- Multiple providers coexist via `PROP_NAME`; existing `history.provider` config value keeps selecting the same name (`timescale-history` default).

### Storage SPI (`HistoryStorage`, @ConsumerType)

`capabilities()`, `maxPageSize()`, `store(List<HistoricalRecord>)` (batch-ready), `valueAt/firstValue/latestValue/count/values/aggregate` mirroring the query model, plus `long prune(PruneRequest)` for housekeeping (see below).

Normalized `HistoricalRecord(modelPackageUri, model, path, timestamp, ValueKind, value)` with `ValueKind`:

- `NUMBER` — BigDecimal; Double only for NaN/±Inf
- `BOOLEAN`, `STRING`
- `GEOJSON` — **any** GeoJsonObject, lossless round-trip required
- `OBJECT` — canonical JSON + original type name (replaces lossy toString())

Normalization happens once in history-core (adapted from `TimescaleDatabaseWorker.notify()`), so every backend receives identical typed input.

history-core manager tracks `HistoryStorage` services (MULTIPLE/DYNAMIC); per service it registers the typed-event listener (ICriterion include/exclude from `PROP_INCLUDE`/`PROP_EXCLUDE` props, as today via `ResourceSelectorFilterFactory`), the `HistoryProvider` engine (limit clamping, capability gating), the ACT facade, and the synthetic twin provider (serialize create/delete per name). Config changes re-register the service with new properties → **structurally fixes the @Modified selector bug**.

### Configurable historization filter (ConfigAdmin)

A standalone, backend-independent filter stage in the history-core ingestion pipeline decides per resource whether an update is historized. Sits *in front of* every `HistoryStorage`, replacing the current situation where filtering is a private config detail of the TimescaleDB provider.

- **Factory PID `sensinact.history.filter`** (DS factory component in history-core), so several filter configurations can coexist. Config properties per instance:
  - `include.resources` / `exclude.resources` — JSON `ResourceSelector` arrays, parsed via the existing `ResourceSelectorFilterFactory` into `ICriterion` (reuses `dataTopics()` for topic subscription narrowing and `dataEventFilter()` for the predicate — same machinery as today).
  - `target` — optional history-provider name(s) the filter applies to; default `*` (all providers). Enables e.g. "provider A stores everything, provider B only locations".
  - **Change condition (deadband)** — optional per filter instance, suppresses updates that changed too little since the last *stored* value:
    - `change.mode` — `all` (default: every matching update is stored), `on-change` (store only when the value differs from the last stored value), `deadband` (numeric threshold).
    - `change.threshold` — absolute delta for `deadband` (e.g. `0.5`: last stored 21.0 → 21.2 suppressed, → 22.0 stored). `change.threshold.percent` as relative alternative (one of the two).
    - `change.max.interval` — optional heartbeat (ISO-8601 duration): store anyway when the last stored record is older than this, so flat signals still leave periodic samples and gaps stay bounded.
- **Semantics**: a record is stored for a given provider when it matches at least one applicable include selector **and that filter's change condition passes** and no applicable exclude selector. No filter configured for a provider → fall back to that backend's own `PROP_INCLUDE`/`PROP_EXCLUDE` service properties (compat: existing `sensinact.history.timescale` configs keep behaving identically). If both exist, the ConfigAdmin filters win.
- **Deadband semantics**: comparison is always against the last value **stored** for that (provider, resource) — not the last received — so small drifts accumulate and cross the threshold. The first update after activation/restart is always stored (self-healing; per-resource state is an in-memory map in history-core, not persisted — lazily seeding from `latestValue()` is a possible later optimization). `deadband` applies to `ValueKind.NUMBER`; all other kinds under `deadband` degrade to `on-change` (equality). Suppression happens after selector matching, before `store()`.
- **Dynamic**: filter add/update/remove takes effect immediately (DS factory lifecycle re-registers the typed-event listener with recomputed topics/predicates) — no backend restart, and it makes the fix for the current `@Modified` selector bug user-visible as a feature.
- **Internal shape**: each config instance is registered by the factory component as a `HistoryIngestFilter` service (name, targets, include/exclude `ICriterion`); the manager consumes them via MULTIPLE/DYNAMIC reference and recomputes the union of `dataTopics()` per provider so unneeded events are never delivered, not merely dropped. Note: since PRs #738/#741, `dataTopics()` returns **escaped** topic filters (`TopicUtils`) — the pipeline must use them as-is (no double-escaping), and tests need provider/resource names containing characters that require escaping.
- Deliberately declarative (`ResourceSelector`), not a Java predicate SPI — keeps filters expressible in plain configuration files/ConfigAdmin tooling; a programmatic `HistoryIngestFilter` registration path exists implicitly for advanced users since it is a whiteboard service.

### Configurable housekeeping / data retention (ConfigAdmin)

Backend-independent cleanup of stored history, driven by ConfigAdmin, executed by history-core against the storage SPI.

- **Factory PID `sensinact.history.housekeeping`** (DS factory component in history-core) — multiple independent policies. Config properties per instance:
  - `target` — history-provider name(s) the policy applies to; default `*`.
  - `include.resources` / `exclude.resources` — optional JSON `ResourceSelector` arrays scoping the policy to specific resources (same format/machinery as the historization filter).
  - `retention.period` — ISO-8601 duration (e.g. `P90D`): records older than `now - period` are deleted.
  - `keep.count` — optional: keep only the newest N records per resource (a record is deleted if it violates *either* bound when both are set).
  - `schedule.period` — how often the policy runs, ISO-8601 duration, default `PT24H`; first run delayed one period after activation (no surprise mass-delete on startup).
- **Execution**: a single scheduler in history-core (`ScheduledExecutorService`) resolves each policy to the targeted `HistoryStorage` services and calls `prune(PruneRequest)`; runs are serialized per backend, logged with the deleted-record count, and never overlap ingestion-critical paths (prune is a backend-side operation).
- **SPI addition**: `long prune(PruneRequest)` with `PruneRequest(scope include/exclude selectors or paths, Instant olderThan, OptionalLong keepLatestPerResource)` → returns deleted count. Backends may optimize natively: TimescaleDB `drop_chunks`/retention policies when the scope is table-wide (age-only, no selector) and plain `DELETE` otherwise; MongoDB TTL indexes only cover the age-only whole-collection case (TTL is collection-global on time-series collections — scoped policies need explicit deletes); in-memory trivially. Whether native retention exists and how well scoped deletes perform becomes an **evaluation criterion in stream A** (folded into ops-fit scoring, measured by a T4 prune suite in the contract test).
- **Safety**: a policy with neither `retention.period` nor `keep.count` is invalid and ignored (logged); deletions are irreversible, so each run logs policy id, scope, cutoff, and count at INFO. A future `mode=downsample` (aggregate-then-delete, AGGREGATION-capability-gated) is noted as deferred — the `PruneRequest`/config shape stays additive for it.

### Compatibility facade (bit-identical, in history-core)

Registered as `HistoricalQueries` with `sensiNact.whiteboard.resource=true` + `sensiNact.provider.name=<name>` (whiteboard tracks by property, not objectClass — `GatewayThreadImpl.java:145`). Delegation rules (verified against the SQL templates):

| ACT action | Facade behavior |
|---|---|
| `single(time!=null)` | `getValueAt`; absent → empty `DefaultTimedValue` |
| `single(time==null)` | `getFirstValue` |
| `range(from!=null)` | ASC, offset=skip, limit 500; **append `DefaultTimedValue.EMPTY` if `hasMore`** (templates use LIMIT 501) |
| `range(from==null)` | DESC, offset=skip (skips from the END), limit 500, reverse to chronological, **no EMPTY marker** (WITHOUT_START templates use LIMIT 500) |
| `count` | inclusive bounds |
| null `skip` | treated as 0 |
| errors | rethrown as RuntimeException (current behavior) |

Numeric read-shape: facade re-applies the legacy scale≤0→long, else→double narrowing; `HistoryProvider` returns full-precision BigDecimal.

### Consumer migration (SensorThings)

- Binding follows the existing DS→properties-map pattern (`SensinactSessionProvider`): `SensinactSensorthingsApplication` and `SensorthingsFilterComponent` add `@Reference(MULTIPLE, DYNAMIC, GREEDY) List<HistoryProvider>`, select by configured name (or sole provider), publish under new key `sensinact.history.service`; keep publishing the old `sensinact.history.provider` string for ≥1 release.
- `HistoryResourceHelperSensinact`: replace count+500-skip loops with `getValueCount` + one `getValues(range, order, offset=$skip, limit=$top)` when pushdown applies, else `streamValues(query, maxResults)`. HistoricalLocations keeps admin/location hardcoding (unchanged semantics).
- Pushdown contract with rest.api response filters: new `PaginationConstants` + `PAGINATION_APPLIED` request property; `TopFilter`/`SkipFilter`/`OrderByFilter` return early when set; `hasMore` drives nextLink.
- Pushdown rules: `$orderby` only for phenomenonTime/resultTime → `SortOrder`, else in-memory fallback. **Push `$skip/$top` down only when no `$filter` OR the `$filter` reduces entirely to a time constraint** (M6: `TimeRangeExtractor` over the existing OData ANTLR parse tree) — otherwise `@iot.count`/paging would be wrong.
- `ObservationsDelegateSensinact.getObservation`: `+1ms` hack → `TimeRange.millisecondOf(timestamp)`.
- `HistoryResourceHelperSensorthings` (filter module): same collapse via `streamValues`; delete duplicated pagination loop.
- Generic REST/gogo: untouched (facade preserves the ACT surface).

### Milestones

0. **M0 — Test hardening** *(before everything else; mergeable to master independently of the rework)*: close the coverage gaps at the exact cut lines of the rework.
   - **Unit tests for the seven `rest.api` query-option filters** (`TopFilter`, `SkipFilter`, `OrderByFilter`, `CountFilter`, `SelectFilter`, `ExpandFilter`, `ResultFormatFilter`) — the module currently has **zero** tests and M4 modifies these classes. Characterize: nextLink `skip+top` arithmetic, `@PaginationLimit` default/capping, `@iot.count` add/remove with `$count=true/false`, multi-key/nested-path `$orderby` comparators, `$select` field retention on list vs single object, dataArray conversion.
   - **Golden-response REST IT for the ACT history actions**: capture the exact JSON of `history/single|range|count` via the generic REST ACT endpoint (numeric long-vs-double narrowing, geo values, the EMPTY marker, empty results) — this is the facade-parity baseline the Verification section relies on, and it pins risk #2 (numeric wire shape) before any code moves.
   - **Pin the marker asymmetry in `TimescaleHistoryTest`** (new method only): >500 rows with `fromTime == null` must return exactly 500 values, **no** EMPTY marker, with `skip` counting from the end — the one facade-critical semantic not currently asserted (the existing 501-marker test runs with a `fromTime`).
   - *(Optional)* filter-module inventory test documenting which `$filter` expressions are rejected (`UnsupportedRuleException` paths) — maps the fallback surface for M6/M7.
   - Explicitly out of scope: characterizing the `@Modified` reconfiguration path (known-buggy lifecycle that M2/M3 replace; the new behavior gets its own tests there).
   - Effort: ~2–4 days. Gate for starting M4; M1 may start in parallel.
1. **M1 — Contracts**: new `provider` + `storage` packages (incl. `PruneRequest`/`prune` and the `ValueFilter` type carried by `HistoryQuery`), `@Deprecated` on `HistoricalQueries`, version bumps. No runtime change. *(SPI v0 was validated by the stream A spikes: 33-test contract suite green on in-memory/Postgres/Timescale/Mongo.)*
   Decisions folded in: **synchronous API** (Promise-returning `default` methods addable later without breaking `@ProviderType`); **`HistoricalRecord` reserves `annotations` map and `endTimestamp`** (future STA `parameters`/`resultQuality`/interval `phenomenonTime` — v1 never writes them, backends persist when present); **`HistoryIngestFilter` is exported API** with declarative selectors for topic narrowing plus an optional stateful `shouldStore(HistoricalRecord, Optional<TimedValue<?>> lastStored)` callback (`history-core` supplies the last-stored value) — the built-in ConfigAdmin filter becomes one implementation; third parties (notification proxy) implement the same contract.
2. **M2 — Engine**: history-core (manager, pipeline, **historization filter factory `sensinact.history.filter`**, **housekeeping factory `sensinact.history.housekeeping` + scheduler**, engine, facade) + history-inmemory + contract test-jar + facade unit tests.
3. **M3 — Chosen backend on the SPI** *(after stream A decision gate)*: rewrite timescale-provider (or implement the evaluation winner) with a composable query builder (one `if` per dimension, no template matrix), including an efficient `prune` (native retention where the scope allows). Gate: **unmodified `TimescaleHistoryTest` green** (config + facade bit-compat proof). GeoJSON losslessness lands here per the ADR schema; legacy Point-only rows must stay readable (migration note/carve-out).
4. **M4 — rest.gateway migration**: PaginationConstants, service binding, helper rewrite, +1ms removal, pushdown for the no-$filter case. Existing ITs green + new pushdown ITs (in-memory backend, docker-free).
5. **M5 — filter-module migration**: binding + helper rewrite, delete duplication.
6. **M6 — $filter time pushdown**: TimeRangeExtractor + extended rule + mixed-filter fallback ITs.
7. **M7 — Value-filter pushdown (`VALUE_FILTERING`)**: implement the `ValueFilter` execution path end-to-end — engine gating, backend translation (type-aware SQL on the numeric/text value columns; one `if` per predicate in the query builder), in-memory backend support, and northbound integration: extend M6's extractor to also lift `result <op> literal` conjunctions out of `$filter`; push down **only when the residual filter is empty** (so `@iot.count`/paging stay correct), otherwise keep the in-memory fallback. Tests: contract-test suite additions (typed comparisons incl. BigDecimal precision, string equality, combined time+value queries with paging) + STA ITs (`$filter=result gt 20` with `$top/$skip/$count`, mixed pushable/non-pushable filters falling back).
8. **M8 — Docs & polish**: rewrite `docs/source/southbound/history/history.md` around the service contract (ACT demoted to legacy) **including the `sensinact.history.filter` and `sensinact.history.housekeeping` factory PIDs with config examples**, update `timescale.md` (also fix stale versions: says timescale-provider 0.0.2 / postgresql 42.5.1 vs shipped 42.7.9; provider-name default mismatch `timescale-history` vs docs' `sensiNactHistory`), `SensorthingsRestAccess.md`, migration notes; optional Timescale `AGGREGATION` capability (`time_bucket`).

Ordering: M0 first (independent PR to master; hard prerequisite for M4, recommended before M2's facade work); M1→M2 strict; stream A runs parallel to M0/M1/M2 and gates M3; M4/M5 can develop against history-inmemory in parallel with M3; M7 builds on M6's extractor.

### Testing

- history-api: builder/TimeRange edge-case units.
- history-core: normalization matrix, engine clamping/gating, **facade semantics units (EMPTY-marker asymmetry, reverse-skip, null-skip) against in-memory**; ingestion with escaped topics (provider/resource names needing `TopicUtils` escaping); historization filter: include/exclude precedence (against the post-#744 selector semantics), `target` scoping, backend-property fallback, **runtime config change taking effect without backend restart** (in-memory backend IT), and change-condition cases (deadband accumulation across suppressed drifts, on-change for strings/geo, heartbeat via `change.max.interval` with injectable clock, first-update-after-restart always stored, threshold reconfiguration at runtime); housekeeping: age/keep-count/combined bounds, selector scoping, invalid-policy rejection, schedule execution + policy add/remove at runtime (in-memory backend, injectable clock/scheduler).
- `HistoryStorageContractTest` test-jar: extended by history-inmemory (fast) and every backend (testcontainers).
- **`TimescaleHistoryTest.java` kept byte-for-byte as regression gate**; extended (new methods only) for DESC/offset+limit/exclusive-bounds.
- SensorThings: existing `ObservationHistorySensinactTest`/`...SensorthingsTest` must stay green; new pushdown ITs on the in-memory backend.

### Key files

- `southbound/history/history-api/.../api/HistoricalQueries.java` — deprecate; facade must match exactly
- `southbound/history/timescale-provider/.../TimescaleDatabaseWorker.java` — semantics source; split into pipeline + SPI backend
- `southbound/history/timescale-provider/.../TimescaleHistoricalStore.java` — lifecycle/config to move into history-core; @Modified bug
- `southbound/history/timescale-provider/src/test/.../integration/TimescaleHistoryTest.java` — untouchable regression gate
- `northbound/sensorthings/rest.gateway/.../sensinact/HistoryResourceHelperSensinact.java`, `ObservationsDelegateSensinact.java` — consumer rewrite
- `northbound/sensorthings/filter/.../HistoryResourceHelperSensorthings.java`, `AbstractPathHandlerSensorthings.java` — consumer rewrite
- `northbound/sensorthings/rest.api` `TopFilter`/`SkipFilter`/`OrderByFilter` — pushdown contract
- `distribution/features/timescale-history-provider-feature/...json`, `docs/source/southbound/history/*` — packaging + docs

## Risks / open questions

1. `@iot.count`-under-`$filter` correctness is the top pushdown risk — every pushdown condition needs a test.
2. BigDecimal JSON representation through northbound (`1.0` vs `1`) — check serialization when facade narrowing is bypassed.
3. Legacy geo rows (Point-only geography) readability after schema change — migration note required.
4. ~~Sync-vs-async / HistoricalRecord reservation~~ **decided** (see M1): synchronous API; `annotations` + `endTimestamp` reserved.
5. Rolling-upgrade window: old timescale-provider + new history-core would double-register ACT resources — document "swap features atomically".
6. Multi-resource/selector queries deliberately deferred (builder stays additive; future `MULTI_RESOURCE` capability).
7. ~~Skim PR #616 review threads~~ **done** (2026-07-17): the maintainer objections were *correctness*, not complexity — pushdown paginating while `$filter`/value-`$orderby` ran post-hoc ("almost certainly broken"), count without filters, missing DB-side-filtering tests. All addressed by design: residual-filter-empty rule (M4/M6), value filters in DB (M7), count rule, pushdown ITs. The review *advocates* DB-side filtering done right — cite it in the M4–M7 PRs.
8. Housekeeping deletes are irreversible and run unattended — misconfigured selectors/periods can wipe wanted data. Mitigations: invalid-policy rejection, delayed first run, INFO logging of every run; consider a `dry.run` config flag if reviewers want more safety.
9. Deadband filtering makes history intentionally incomplete — consumers that assume "history == every update" (e.g. exact SensorThings Observation counts, the facade's count action) simply see fewer records; document this clearly. Per-resource deadband state is in-memory: bounded by resource count, but a very large twin plus many filter instances should be kept in mind (state is per (provider, resource), shared across filters).

## Verification

- Per milestone: `mvn clean verify` (full), plus `mvn verify -pl southbound/history/...` / `-pl northbound/sensorthings/...` for the touched modules.
- M3 gate: unmodified `TimescaleHistoryTest` passes against the new stack (docker required).
- M4/M6: run the SensorThings ITs and manually exercise `GET .../Observations?$top=5&$skip=10&$orderby=phenomenonTime desc` against a seeded gateway, verifying the SQL (log/explain) shows LIMIT/OFFSET pushdown and `@iot.count`/nextLink stay correct with and without `$filter`.
- Facade parity: REST ACT calls to `history/single|range|count` return byte-identical JSON before/after (golden responses captured in M0).
- Docs build: `docs/` sphinx build green; examples in history.md/timescale.md executed against the sample docker config.
