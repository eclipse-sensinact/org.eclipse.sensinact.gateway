# ADR : Strategies for Referencing Parent Properties in Hierarchical JSON Mapping

## Status
Proposed / Under Discussion

## Context
When parsing nested or hierarchical JSON payloads using a mapping configuration with a restricted evaluation `base` (e.g., pointing directly to a child array), the mapping engine's context shifts entirely to the child level. Consequently, child objects lose visibility into their parent or ancestor nodes, making it impossible to directly reference parent attributes (such as identifiers or metadata) within the child's mapping definition.

We need to establish a standard architectural approach to handle parent-to-child property referencing across our JSON mapping pipelines.

## Decision Drivers
* **Maintainability:** Resilience against minor changes in JSON tree depth or structure.
* **Readability:** Clarity of mapping files for developers maintaining integrations.
* **Performance and Complexity:** Minimizing heavy pre-processing overhead or complex custom parser modifications where possible.

---

## Considered Options

### Option 1: Relative Path Traversal (`../../../`)
Rely on relative path navigation syntax (similar to an extended JSON Pointer or XPath) to step out of the current child scope and target ancestor nodes directly within the mapping definitions.

#### Example Option 1
Mapping Configuration:
```json
{
  "parser": "json",
  "parser.options": {
    "base": "company/departments/teams/members"
  },
  "mapping": {
    "member/id": "id",
    "member/name": "fullName",
    "team/parentTeamId": "../../../id"
  }
}
```

Input JSON:
```json
{
  "company": {
    "id": "comp-001",
    "departments": {
      "teams": {
        "id": "team-002",
        "name": "Engineering",
        "members": [
          {
            "id": "usr-101",
            "fullName": "Alice"
          }
        ]
      }
    }
  }
}
```

* **Pros:**
  * Zero modifications to the core parser architecture for mappings that don't need parent node values.
  * Keeps the mapping configuration self-contained within a single file.
* **Cons:**
  * Highly brittle; if the JSON schema structure changes depth, all relative traversal counts (`../`) must be manually refactored.
  * Decreases mapping readability and increases debugging friction.
  * Needs review of implementation to correctly retrieve parent nodes from a child context.

---

### Option 2: Named Context Bases / Aliases (Recommended)
Extend parser options to accept a dictionary of named context bases (e.g., `$parent`, `$root`) instead of a single static text path. Mapping rules can then explicitly prefix fields with these defined scope aliases.

#### Example Option 2
Mapping Configuration:
```json
{
  "parser": "json",
  "parser.options": {
    "bases": {
      "default": "company/departments/teams/members",
      "team": "company/departments/teams",
      "company": "company"
    }
  },
  "mapping": {
    "member/id": "id",
    "member/name": "fullName",
    "team/parentTeamId": "$team.id",
    "company/parentCompanyId": "$company.id"
  }
}
```

Input JSON:
```json
{
  "company": {
    "id": "comp-001",
    "departments": {
      "teams": {
        "id": "team-002",
        "name": "Engineering",
        "members": [
          {
            "id": "usr-101",
            "fullName": "Alice"
          }
        ]
      }
    }
  }
}
```

* **Pros:**
  * Completely decouples mapping configurations from absolute structural depth.
  * Highly readable and self-documenting syntax (`$team.id`).
  * Easily extensible for complex hierarchies involving multiple ancestor levels.
* **Cons:**
  * Requires a minor modification to the parser configuration options and resolution logic to support named context scopes.

---

### Option 3: Absolute Root Path Referencing (`$/` or `$.`)

Allow mapping rules to bypass the restricted evaluation base by specifying absolute paths starting from the root of the input JSON document (using a dedicated root indicator prefix).

#### Example Option 3

Mapping Configuration:
```json
{
  "parser": "json",
  "parser.options": {
    "base": "company/departments/teams/members"
  },
  "mapping": {
    "member/id": "id",
    "member/name": "fullName",
    "team/parentTeamId": "$/company/departments/teams/id"
  }
}
```

Input JSON:
```json
{
  "company": {
    "id": "comp-001",
    "departments": {
      "teams": {
        "id": "team-002",
        "name": "Engineering",
        "members": [
          {
            "id": "usr-101",
            "fullName": "Alice"
          }
        ]
      }
    }
  }
}
```

- **Pros:**
  - Direct and explicit targeting from the root without needing relative traversal counters.
  - Leverages standard JSONPath absolute notation concepts.
- **Cons:**
  - Still couples the mapping configuration to the absolute structure of the JSON payload.
  - Verbose paths for deeply nested schemas.

---

## Decision Outcome
**Option 2 (Named Context Bases / Aliases)** is chosen as the primary architectural target. It offers the best balance between long-term maintainability, configuration clarity, and structural decoupling.

If engineering constraints prevent updating the parser engine in the short term, **Option 3 (Absolute Root Path Referencing )** will be used as a temporary fallback, while **Option 1** is strictly discouraged due to its high fragility.
