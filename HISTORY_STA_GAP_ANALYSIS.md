# Gap Analysis: History Provider vs. OGC SensorThings API v1.1

> Companion document to [HISTORY_PROVIDER_REWORK_PLAN.md](HISTORY_PROVIDER_REWORK_PLAN.md).
> Question answered here: *what can the existing history provider do, and which OGC STA (Part 1: Sensing, v1.1) features are missing or limited because of it?*
> Reference: OGC 18-088 conformance classes (Core Read, Filtering, CRUD, Batch, MultiDatastream, Data Array, MQTT Create, MQTT Subscribe).

## 1. What the existing history provider can do

Contract: `southbound/history/history-api/.../HistoricalQueries.java` — three ACT actions on model `sensiNactHistory`, service `history`. Implementation: TimescaleDB (`southbound/history/timescale-provider/`).

| Capability | Status today |
|---|---|
| Store every resource update (typed events) | yes — scoped by `include.resources`/`exclude.resources` selectors |
| Value types | numeric (`NUMERIC`), text, geo **Point only** (`geography(POINT,4326)`); everything else lossy `toString()`; numeric read-back narrowed to `long`/`double` |
| Point-in-time lookup (`single`) | yes — value at-or-before a timestamp; earliest value when time is null |
| Time-range query (`range`) | yes — from/to (inclusive), chronological, max 500 rows + EMPTY-marker paging, `skip` offset |
| Count (`count`) | yes — per resource, optional time bounds |
| Sort direction | no (always chronological; reverse only implicitly when `fromTime == null`) |
| Value/content filtering (e.g. `value > 20`) | **no** — time + resource address only |
| Aggregation / downsampling | **no** |
| Multi-resource queries | **no** — one (provider, service, resource) per call |
| Per-record metadata (quality, parameters) | **no** — timestamp + value only |
| Delete / retention | **no** API (DB-level manual only) |
| Timestamp precision | microseconds in DB; consumers work around ms-vs-µs (`+1ms` hack) |

## 2. How STA uses the history provider today

- **Observations** (`GET .../Observations`): `HistoryResourceHelperSensinact` calls `count` then loops `range` (500/page) up to `sensinact.history.result.limit` (default **3000**), then maps rows to Observation DTOs in memory (`DtoMapper.toObservation`).
- **Single Observation by id**: id encodes an epoch-millis timestamp → `history/single` with the `+1ms` precision workaround (`ObservationsDelegateSensinact.java:68-82`).
- **HistoricalLocations**: same helper against hardcoded `admin/location`.
- All `$filter/$orderby/$top/$skip/$count/$select/$expand` are applied **after** materializing the full (≤3000) list, by jakarta-rs response filters and in-memory predicates.

## 3. STA feature coverage matrix

Status of the sensiNact STA northbound (`northbound/sensorthings/`), and whether the gap is caused/blocked by the history provider.

### Conformance class level

| STA conformance class | sensiNact status | History-provider dependency |
|---|---|---|
| A.1 Core Read (8 entities, nav links, `$ref`, `{prop}`) | **full** — all 8 entity sets incl. deep navigation (`RootResourceAccess.java:76-162`) | Observations/HistoricalLocations depth = history quality |
| A.2 Filtering Extension | **functionally broad, architecturally limited**: full OData grammar; operators/functions largely implemented; but everything evaluates **in-memory** on ≤`result.limit` rows; `has` operator unimplemented; some nav-path expressions rejected (`UnsupportedRuleException`) | **yes** — no pushdown of time/value/sort/paging into storage (issue #634) |
| A.3 Create-Update-Delete | endpoints exist for all entities (POST/PUT/PATCH/DELETE in `rest.api` `create/update/delete/`), served by the STA-native-model delegates + `rest.gateway.extra`; **not declared in the conformance array**; generic twin providers are read-only projections | partial — Observation DELETE has no history-delete API to map to; created Observations lose `parameters`/`resultQuality` when they round-trip through history |
| A.4 Batch Requests (`$batch`) | **absent** (no handler anywhere) | no |
| A.5 MultiDatastream | **absent** (zero references in the codebase) | partial — complex/array results currently `toString()`-lossy in history; typed JSON storage is a prerequisite |
| A.6 Data Array | GET direction implemented (`ResultFormatFilter`, `$resultFormat=dataArray`); dataArray **creation** (POST) absent | GET path fine; efficiency limited by in-memory materialization |
| A.7 MQTT Create | **absent** (broker doesn't accept entity creation) | no |
| A.8 MQTT Subscribe | **implemented** — embedded Moquette broker publishing entity updates (`SensorthingsMqttNorthbound.java`) | no |

### Query options / features in detail

| Feature | Northbound status | History-related gap |
|---|---|---|
| `$top`, `$skip` | implemented (`TopFilter`/`SkipFilter`, `@PaginationLimit` server paging, `@iot.nextLink`) | applied in-memory after fetching up to 3000 rows → **pushdown gap** (rework M4) |
| `$count` | implemented (`CountFilter`) | count of the *materialized* list; with `$filter` it's computed post-filter (correct but expensive); silently wrong if data exceeds `result.limit` |
| `$orderby` | implemented, multi-key, nested paths (`OrderByFilter`) | timestamp ordering not pushed down; DESC over long series requires full fetch (rework M4) |
| `$filter` time constraints (`phenomenonTime ge/le …`) | evaluated in-memory | history supports time ranges — but the northbound never extracts them from `$filter` (rework M6 `TimeRangeExtractor`) |
| `$filter` on `result` values | evaluated in-memory | **history provider has no value predicate at all** — every value filter forces a full fetch; closed by rework milestone M7 (`ValueFilter` + `VALUE_FILTERING` capability) |
| `$filter` geospatial (`st_within`, `geo.distance`, …) | implemented via ESRI engine, in-memory | geo history is Point-only; PostGIS spatial indexes exist but are never used for filtering — spatial pushdown is a future capability |
| `$filter` `has` operator | **unimplemented** (`AnyMatch.java:187`) | no |
| `$select`, `$expand` (incl. deep) | implemented (contra stale "rejection" comments in `ExpandFilter`/`FilterFilter`) | no |
| Interval `phenomenonTime` | **unsupported** — DTO is scalar `Instant`; `phenomenonTime == resultTime` always | history stores one timestamp per record; intervals unrepresentable |
| Observation `validTime`, `parameters` | never populated (null in `DtoMapper.toObservation`) | history has no per-record metadata to carry them |
| Observation `resultQuality` | from live resource metadata only | not historized → historical Observations lose quality info |
| FeaturesOfInterest | synthesized from the provider's *current* Location (one FoI per provider) | historical FoI per Observation not stored; FoI with Polygon/FeatureCollection geometry is lossy in history (Point-only) |
| HistoricalLocations | implemented via `admin/location` history | non-Point locations (e.g. FeatureCollection) degrade to `toString()` → **broken geometry in history** (rework G2 fixes) |
| Result completeness | `sensinact.history.result.limit` (default 3000) caps what any query can see | nextLink paging beyond the cap is not guaranteed → correctness risk on large series; disappears with real pushdown paging |

## 4. Gap summary

**Gaps the history provider causes (closed by the rework plan):**
1. No query pushdown ($top/$skip/$orderby/time-$filter) → in-memory filtering over capped fetches — *plan M4/M6*.
2. Lossy GeoJSON (Point-only) → broken HistoricalLocations/FoI geometries — *plan G2 + M3 schema*.
3. Lossy complex values (`toString()`) → blocks MultiDatastream-style array results, arbitrary JSON results — *plan `ValueKind.OBJECT`/`GEOJSON`*.
4. Numeric narrowing (BigDecimal → long/double) → `$filter` comparisons on precise values unreliable — *plan `NUMBER` = BigDecimal*.
5. No delete/retention API → STA Observation DELETE can't reach history — *plan `prune(PruneRequest)` gives the primitive; wiring STA DELETE to it is a possible follow-up*.
6. The 500-cap/EMPTY-marker + 3000-row ceiling → completeness risk — *plan `HistoryPage`/`hasMore` paging*.

**Gaps the history provider contributes to but the rework does not yet close (candidate plan extensions):**
7. **Value-predicate pushdown** (`$filter result gt 20` at DB level) — ~~decide: in scope or deferred~~ **decided: rework milestone M7** (`ValueFilter` in the M1 contract, execution + STA `$filter` extraction in M7).
8. **Per-record metadata** (`parameters`, `resultQuality`, interval `phenomenonTime`) — would require extending `HistoricalRecord` (e.g. optional metadata map / start+end timestamps). Decide before M1 freezes the SPI: even if unused at first, the record shape should not preclude it.
9. **Spatial pushdown** (`st_within` on location history at DB level) — future capability; keep `HistoryQuery` additive.

**Pure northbound gaps (independent of the history rework):**
10. MultiDatastream entity (absent entirely).
11. `$batch` (absent).
12. `has` operator + rejected `$filter` navigation paths + `PathUtils` geometry/metadata FIXMEs.
13. MQTT Create extension; dataArray on Observation creation.
14. Conformance array under-claims: CRUD endpoints exist but `create-update-delete` (and dataArray/MQTT) URIs are not declared in the service root (`RootResourceAccess.java:46-61`).
15. Interval `phenomenonTime` DTO support (scalar today) — northbound half of gap 8.

## 5. Consequences for the rework plan

- Confirms M4/M6 (pushdown) and the G2 round-trip gate as the highest-value items for STA.
- **Open decision for M1 (SPI freeze):** shape `HistoricalRecord` so per-record metadata and time intervals are *representable later* without breaking backends (gap 8) — e.g. an optional `Map<String,Object> annotations` and nullable `endTimestamp`, even if nothing writes them in v1.
- **Decided:** `VALUE_FILTERING` (gap 7) is rework milestone M7 (`HistoryQuery.valueFilter(...)` + backend translation + STA `$filter` extraction). It is the single biggest remaining STA-performance item after time/paging pushdown.
- Items 10–14 are northbound scope — track them as separate issues, not in the history rework.
