# ADR: Replaceable recording step of the history engine

- Status: **proposed** (option A selected; proposed to the maintainers with this pull
  request)
- Date: 2026-11-23
- Context: `history-core` (`IngestionPipeline`, `HistoryStorageManager`) and the storage SPI
  of `history-api` (`HistoryStorage`, `HistoryIngestFilter`)

## Problem

Once an update has passed the ingest filters, the engine hands the normalised record to
the storage with `storage.store(List.of(record))`. The call is synchronous, made on the
typed event handler thread, with one record per call. A `RuntimeException` is logged and
the record is lost.

A deployment that needs something else at this point, for example writing records in
groups, keeping them in memory while the database is briefly unavailable, or counting what
was recorded and what was dropped, cannot do it without patching `history-core`.

The recording step must become replaceable, with these constraints:

1. Without a replacement, the history of a plain sensiNact behaves exactly as today: same
   configuration, same calls to the storage, same ordering, same failure handling and
   same logs. Nothing has to be added or changed to keep it running.
2. A third party bundle provides the replacement, with no change to `history-core`.
3. The filtering decisions (include, exclude, change condition, last stored value) stay in
   the engine, so that every recording step sees the same decisions.
4. A replacement can be installed, removed or reconfigured at runtime, like storages and
   filters are today.
5. The API added to `history-api` is small and additive.
6. Several histories can run on the same sensiNact, each with its own provider name,
   storage and filters. The recording step is chosen and owned per history: a step never
   sees the records or the storage of another history, and two histories can use
   different steps.

## Current behaviour (facts)

- `IngestionPipeline.handle(event)` normalises the event, applies the filters and the
  change condition, then calls `storage.store(List.of(record))`.
- The last stored value of each resource is kept in memory by the pipeline and is passed
  to `HistoryIngestFilter.shouldStore`. It is updated only when `store` returned normally.
- `HistoryStorageManager.ManagedBackend` creates one `IngestionPipeline` per storage
  service, and updates it when the applicable filters change. Each `HistoryStorage`
  service with its own provider name is one history: every history listens to the typed
  events, applies its own filters and keeps its own last stored values.
- A second `HistoryStorage` registered with the same provider name replaces the first one
  ("the newest registration wins").

## Options

### Option A: whiteboard factory, one recording step per backend

A new consumer-type service creates a recording step for a backend, given a context with its
provider name and its storage. The engine selects the factory by provider name and service ranking, and
uses the built-in direct step when none matches.

```java
@ConsumerType
public interface HistoryRecordingStepFactory {
    /** Property listing the provider names served; absent means all. */
    String PROP_NAME = HistoryProvider.PROP_NAME;

    HistoryRecordingStep create(HistoryRecordingContext context);
}

@ProviderType
public interface HistoryRecordingContext {
    String providerName();

    HistoryStorage storage();
}

public interface HistoryRecordingStep extends AutoCloseable {
    /** Called on the event thread, in delivery order; must not block for long. */
    void record(HistoricalRecord record);

    /** Called when the backend stops or the step is replaced; flush best effort. */
    @Override
    default void close() {
    }
}
```

The default step keeps today's behaviour: `storage.store(List.of(record))`.

Pros:

- The extension point is declared: its contract, lifecycle and selection rules are written
  down and tested in `history-core`.
- Each history has its own step, with a lifecycle: buffers, writer threads and metrics are
  created with the history and closed with it (`close`).
- The step receives the storage it writes to, so it needs no map keyed by storage.
- Selection by provider name follows `HistoryIngestFilter.targets()`, a convention that
  integrators already know.
- With several histories, `create` is called once per history, so each history gets its own
  independent step; one factory can serve several histories, and the ranking is evaluated
  per history, so two histories can use different factories.
- A plain installation is untouched: without a factory, the built-in step is used.

Cons:

- Three new public types (step, factory and context) that the maintainers must accept and
  keep stable, against one in option B and none in option C.
- One more level of indirection: a factory creates the step, which takes more explaining
  than a plain service.
- The engine hands over one record at a time, on the event thread: a step that wants
  batches builds its own buffer.
- For a step that writes later, "recorded" means "handed over": the engine cannot see a
  later failure of the write.
- When several factories serve the same history, only the best ranked one is used; they
  cannot be combined.
- A replacement cannot delegate to the built-in step, which is not a service: it writes to
  the storage itself.

### Option B: whiteboard step service, called with the storage

A single consumer-type service is called for every record, with the storage as argument.

```java
@ConsumerType
public interface HistoryRecordingStep {
    void record(HistoryStorage storage, HistoricalRecord record);
}
```

Pros:

- The smallest API: one type with one method, and no factory to explain.
- The simplest engine change: pick one service and call it.
- A plain installation is untouched: without the service, the built-in step is used.

Cons:

- No lifecycle: a step that buffers cannot know when a storage goes away and cannot flush
  it, and a replaced step cannot be closed.
- With several histories, the same service serves all of them: the per history state
  (buffer, writer, metrics) lives in a map owned by the step, keyed by storage identity,
  which nothing cleans.
- Giving a different step to each history needs a property convention anyway, which is
  the selection of option A without its benefits.
- The storage is passed at every call, so a step cannot prepare anything for it in advance.

### Option C: no new SPI, a decorating `HistoryStorage`

The third party registers its own `HistoryStorage` that wraps the real one, buffers in
`store` and delegates everything else. The "newest registration wins" rule makes it replace
the real storage for the provider name.

Pros:

- No change to `history-core` or `history-api`: any bundle can do it today.
- No review by the maintainers and no new API for them to keep stable.
- The engine and a plain installation are untouched.

Cons:

- The decorator must delegate every read, aggregation and prune operation, and every
  future addition to `HistoryStorage`.
- With several histories, one decorator is needed for each, and each must know which real
  storage it wraps.
- It relies on the registration order ("the newest registration wins"), which is not
  guaranteed across restarts and configuration updates.
- The real storage must be registered under another name or without the provider name,
  which hides it from the engine, for example from housekeeping.
- The engine cannot tell a decorated storage from a plain one, so nothing in the engine
  documents or tests the extension point: behaviour is replaced through a side effect
  rather than a declared contract.

### Comparison

| Constraint | A: factory | B: step service | C: decorating storage |
| --- | --- | --- | --- |
| 1. Plain sensiNact unchanged | Yes | Yes | Yes |
| 2. Replacement from a third party bundle | Yes, with the new API | Yes, with the new API | Yes, with no new API |
| 3. Filtering decisions stay in the engine | Yes | Yes | Yes |
| 4. Install, remove, reconfigure at runtime | Yes, the step is closed | Partly: no close | Partly: depends on registration order |
| 5. Small and additive API | Two types | One type | No API, and no declared contract |
| 6. Several histories | Yes, one step per history | Partly: shared service, state keyed by storage | Partly: one decorator per history |

## Decision

**Option A: a whiteboard factory creating one recording step per backend.** Options B and
C are rejected for the reasons above.

The extension point is declared (constraint 2), per backend with a lifecycle (constraint
4), and small (constraint 5): one interface for the step, one for its factory and one for
the context the factory receives. The engine keeps the decisions (constraint 3), and
without a factory it uses the built-in step (constraint 1).

### Contract

- **Selection.** For each backend, the engine uses the factory with the highest
  `service.ranking` whose `sensinact.history.provider.name` property is absent or contains
  the backend's provider name. The factories are checked again when one appears,
  disappears or changes its properties; when the selected factory of a backend changes, its
  step is replaced in place: a new step is created, the previous one is closed, and the
  backend is not restarted. A factory that returns `null` declines the backend, and a
  factory that throws is logged: in both cases the next factory is tried, then the built-in
  step, and the factory is asked again on the next change of the factories.
- **Several histories.** The selection is made for each history, with its own provider
  name: a factory without the provider name property serves every history, a factory
  with names serves only those, and the factory chosen for one history does not affect
  the others. `create` is called once per history and the steps are independent: each
  receives only the records accepted by the filters of its own history, and writes to
  the storage of its own history.
- **Default.** The engine does not register a default factory. When none matches, it uses
  a built-in step that calls `storage.store(List.of(record))`.
- **Threading.** `record` is called on the typed event handler thread, in the order of
  delivery. A step must return quickly and must preserve the order of records of one
  resource when it writes them. It must never block: a step whose buffer is full drops the
  record and counts it. `create` is called while the engine holds its lock: it must return
  quickly, must not wait for another service, must not register or unregister services and
  must not call the history. When a backend is replaced by a newer registration with the
  same provider name, the new step is created before the previous one is closed.
- **Failure.** `record` may throw a `RuntimeException`: the engine logs it and does not
  update the last stored value, as today.
- **Last stored value.** The engine updates it when `record` returns normally. For the
  built-in step this is exactly today's behaviour. For a step that writes later, it means
  "handed over", not "written": a failure of the later write is not seen by the engine.
  This is documented in the interface.
- **Shutdown.** `close` is called when the backend stops or the step is replaced, once the
  step no longer receives records, outside the lock of the engine and before the storage
  is released. After a stop, a late record goes to the storage directly, as before the
  change; a backend without a factory is stopped as before, except that its storage is
  released once the lock of the engine is released.

### Design decisions inside the contract

Each decision of the contract is stated with what it brings, what it costs and the
alternative that was not taken.

**1. Selection: the best ranked factory serving the history is used.**

- Pros: one clear rule that OSGi users know (`service.ranking`); two histories can use
  different factories; a deployment can override a factory by registering a better ranked
  one.
- Cons: factories cannot be combined, since only one is used; a tie in ranking is broken by
  the OSGi service order, which an integrator does not control.
- Alternative: chain every matching factory. Pros: several concerns, for example metrics and
  buffering, could be separate bundles. Cons: the order and the hand-over between steps
  must be specified, and a failure in the middle of the chain has no obvious meaning.

**2. Default: the engine falls back to a built-in step; no default factory is registered.**

- Pros: a plain installation has no new service and no new configuration; the default
  cannot be removed by mistake.
- Cons: the default step is not visible in the service registry; a replacement cannot
  delegate to it and must write to the storage itself.
- Alternative: register a default factory with the lowest ranking. Pros: the default is a
  service like the others and could be wrapped. Cons: one more service and component on
  every installation, and an installation whose default factory is missing would stop
  recording.

**3. Threading: `record` is called for one record at a time, on the event thread, in the
order of delivery.**

- Pros: the default behaviour is exactly the current one; the API stays small; a step is
  free to buffer, batch or write at once.
- Cons: a slow step blocks the event thread, and every step that wants batches writes its
  own buffer and its own loss policy.
- Alternative: the engine owns a queue and calls the step with batches. Pros: the event
  thread is protected and batching is written once. Cons: the default behaviour changes
  (thread, timing), the engine must decide what to drop when the queue is full, and the
  change is much larger for the maintainers to review.

**4. Failure: a `RuntimeException` of `record` is logged and the last stored value is
left unchanged.**

- Pros: identical to the current handling of a failing storage; one failing update does not
  stop the following ones.
- Cons: the engine does not retry, so a step that wants retries implements them.
- Alternative: let the exception reach the event bus. Pros: the failure is visible to the
  bus. Cons: the effect on the other listeners of the same event depends on the event bus,
  and it changes the current behaviour.

**5. Last stored value: updated when `record` returns normally ("handed over").**

- Pros: the filters see the same last value for every step; the built-in step keeps its
  exact behaviour; the API needs no callback.
- Cons: for a step that writes later, the engine believes a value was stored when a later
  write may fail, so a change condition (deadband) can suppress a value that was never
  written.
- Alternative: the step reports success to the engine after the write. Pros: the last
  stored value is exact. Cons: a callback in the API, an engine that must handle late and
  out of order reports, and a harder contract for the step to implement.

**6. Shutdown: `close` is called when the history stops or the step is replaced.**

- Pros: a step can flush what it holds and release its threads; replacement leaves nothing
  behind.
- Cons: `close` runs on a service event thread, outside the lock of the engine, and the
  closes and service releases of all events run one after the other so that a storage is
  never released while a step still writes to it: a slow `close` delays the service
  events that end in a release, not the others. The contract has no timeout.
- Alternative: no `close`. Pros: smaller API. Cons: buffered records and threads are lost at
  every stop and every replacement.

**7. Change of factory: the step of the affected histories is replaced, the history is not
restarted.**

- Pros: the history provider and its twin provider stay registered, so clients see no gap;
  the same mechanism as a change of filters.
- Cons: each step has a lock that is held while it handles a record. The swap itself does
  not wait: the records that follow go to the new step at once, so they may be written
  before the records that the previous step still holds. Before `close`, the engine takes
  the lock of the previous step, outside its own lock, so it waits for the record being
  handled: a step never receives a record during or after `close`, at the cost of one
  uncontended lock for each record.
- Alternative: restart the history on every change of factory. Pros: no record is in flight
  during the swap. Cons: the provider disappears from the digital twin for a moment and its
  clients see it.

**8. Arguments of `create`: one context object, implemented by the engine.**

- Pros: the context is a provider type, so a method added to it later (another storage, a
  metrics registry, the configuration of the history) is a minor change of the package,
  while a parameter added to `create` would be a major change of the consumer-type
  factory.
- Cons: one more public type, and a factory reads two accessors instead of two parameters.
- Alternative: `create(String providerName, HistoryStorage storage)`. Pros: one type less
  and a factory can be a two-argument lambda. Cons: the signature cannot evolve without a
  major version.

### Consequences

- `IngestionPipeline` keeps the decision chain; the call to `storage.store` moves behind
  the step. `IngestionPipelineTest` keeps passing unchanged for the default step.
- `history-api` exports the three new types from the `storage` package, whose version moves
  from 1.0.0 to 1.1.0 (additive).
- Tests: the existing pipeline tests for the default step; new tests for a replacement
  step (receives every recorded record, receives none of the filtered ones), for the
  selection rules, for a throwing step, and for a swap that waits for the record being
  handled; integration tests with two histories on the same gateway and two factories, one
  for all histories and a better ranked one for the second history only, for a change of
  ranking, for a failing factory, and for a buffering step that writes what it holds when
  its factory is removed and before its storage is released.
- Documentation: a section in `history.md` on writing a recording step.

## Compatibility with a plain sensiNact

The history of a sensiNact without a recording step factory is not changed. This is a
requirement of the change, and it is checked, not assumed:

| Aspect | Why it does not change | How it is checked |
| --- | --- | --- |
| Calls to the storage | The built-in step calls `storage.store(List.of(record))` for each accepted record, on the same thread and in the same order as before | The pipeline tests that existed before the change are unchanged and pass |
| Failure handling | A `RuntimeException` of the storage is logged by the engine and leaves the last stored value unchanged, as before | The existing test of a failing storage, and a test of a throwing replacement step |
| Filtering decisions | The include, exclude and change conditions are evaluated before the step, by the same code | The existing filter and deadband tests |
| Configuration | No new configuration, property or required service; the factory reference is optional and dynamic | The integration tests that existed before the change are unchanged and pass |
| Logs | Without a factory, the engine logs nothing new and the existing messages are unchanged | Inspection of the integration test log |
| API | `history-api` only gains three types; its `storage` package moves to 1.1.0, so every consumer that imports 1.0 still resolves | The diff of `history-api`: three added types and the package version, no existing type touched |
| Cost | The recording step adds one uncontended lock for each accepted record | Inspection |

A deployment is affected only when a bundle registers a `HistoryRecordingStepFactory`
that serves one of its histories, and only that history.

## Open points

- Names: are `HistoryRecordingStep` and `HistoryRecordingStepFactory` acceptable to the
  maintainers?
- The factory receives one storage. If a history is later served by several storages, the
  context may need to give them; that change is out of scope here and would be a minor
  change of the context (decision 8).
- Batching is a concern of the step, not of the API: `record` receives one record at a
  time and a step groups them as it needs.
