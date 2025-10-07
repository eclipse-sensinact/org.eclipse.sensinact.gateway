# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Eclipse sensiNact Gateway is a lightweight IoT digital twin gateway built on OSGi and Maven. It provides modular, pluggable connectivity for diverse IoT devices and data sources with clean northbound access layers.

## Common Development Commands

### Build Commands
```bash
# Standard build (run this first for new checkouts)
mvn install -DskipTests=true

# Full build with tests
mvn verify

# Quick dependabot update resolution
mvn -P dependabot verify

# Generate dependencies file
mvn -Dgenerate-depends=true generate-resources

# Check Eclipse-approved licenses
mvn -P eclipse-licenses-check org.eclipse.dash:license-tool-plugin:license-check
```

### Development Profiles
- `dev` (default): Development build with test bndrun flexibility
- `ci-build`: Strict CI build requiring correct bndrun resolution
- `dependabot`: Helps resolve version updates for bndrun files

### Testing
```bash
# Run unit tests only
mvn test

# Run integration tests
mvn integration-test

# Run tests for specific module
mvn test -pl core/api
```

## Architecture Overview

### Core Structure
- **core/**: Digital twin API (`SensinactDigitalTwin`) and EMF-based data models
- **southbound/**: Device connectors (MQTT, HTTP, WoT, Virtual devices) 
- **northbound/**: Access layers (REST API, WebSocket, SensorThings API)
- **filters/**: Query and data filtering implementations
- **distribution/**: OSGi features and launcher assembly
- **examples/**: Usage examples and patterns

### Key Architectural Patterns

**Provider-Service-Resource Hierarchy**: Digital twin organized as providers containing services containing resources. All operations follow this structure.

**OSGi Services**: Components register/consume services via OSGi service registry. Use `@Component` annotations for service lifecycle.

**Command Pattern**: All operations return `Promise<T>` via `AbstractSensinactCommand<T>`. Single-threaded execution via `GatewayThreadImpl`.

**Whiteboard Pattern**: Extensible functionality via `WhiteboardHandler` interfaces that can be registered/unregistered dynamically.

**EMF Models**: Data structures defined in `sensinact.ecore` with generated Java classes for type-safe digital twin operations.

## Key Interfaces and Classes

### Core API (`core/api/`)
- `SensinactDigitalTwin`: Main interface for GET/SET/ACT operations
- `SensinactModelManager`: Model creation and discovery
- `DataUpdate`: Push updates into digital twin
- `TimedValue<T>`: Timestamped resource values
- `Authorizer`: Security and permissions framework

### REST API Structure
```
GET  /providers - List all providers
GET  /providers/{id}/services/{service}/resources/{resource}/GET
POST /providers/{id}/services/{service}/resources/{resource}/SET
POST /providers/{id}/services/{service}/resources/{resource}/ACT
GET  /providers/{id}/services/{service}/resources/{resource}/SUBSCRIBE
```

## Development Guidelines

### Building New Connectors
1. Implement appropriate southbound interfaces (`IMqttMessageListener`, `IDeviceMappingHandler`)
2. Register as OSGi services with filtering properties
3. Use Device Factory pattern for complex payload parsing
4. Define JSON/XML mapping configurations

### Adding Custom Models
1. Create EMF `.ecore` model or use annotation-based approach
2. Register via `SensinactModelManager.createModel()`
3. Use `@Provider`, `@Service`, `@Resource` annotations for POJOs
4. Include metadata with `@Metadata` annotations

### Testing Requirements
- Use `.bndrun` files for OSGi integration testing
- JUnit 5 with OSGi support via `biz.aQute.tester.junit-platform`
- Mock OSGi services with Mockito for unit tests
- Integration tests should cover full provider lifecycle

### Configuration Patterns
- OSGi Configuration Admin with PID-based configuration
- Service properties for filtering and discovery
- Environment-specific Maven profiles for different deployments

## Code Generation Dependencies
This project uses EMF code generation. Always run `mvn install -DskipTests=true` before opening in IDE to ensure generated sources are available.

## Build Requirements
- Java 17+
- Maven 3.8.0+