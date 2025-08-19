# CLAUDE.md

Apache ShardingSphere - Distributed SQL transaction & query engine. Java 8+ Maven project following "Database Plus" concept.

## Commands

```bash
# Build
./mvnw clean install -DskipTests        # Quick build
./mvnw clean install -T1C -DskipTests   # Parallel build
./mvnw clean install -pl <module> -am   # Module build

# Test
./mvnw test -pl test/it/parser           # Parser tests
./mvnw test -pl test/it/rewriter         # Rewriter tests
./mvnw test -pl features/sharding/core   # Sharding tests

# Quality (required before commit)
./mvnw spotless:apply                    # Code formatting
./mvnw clean install -Pcheck -T1C       # Full quality gate
```

## Architecture

**Core Modules**: infra/ (infrastructure), kernel/ (SQL parsing), features/ (sharding/encryption), jdbc/ (driver), proxy/ (server), parser/ (SQL dialects)

**Supported DBs**: MySQL, PostgreSQL, SQL Server, Oracle, ClickHouse, H2, etc.

## Development

- **Style**: Java 8, Lombok, Google Java Style, SPI pattern
- **Tests**: Unit (`src/test/`), Integration (`test/it/`), E2E (`test/e2e/`)
- **ANTLR**: After grammar changes: `./mvnw generate-sources -pl parser/sql/dialect/<db-type>`