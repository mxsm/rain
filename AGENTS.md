# Repository Guidelines

## Project Structure

Rain generates MySQL-backed segment IDs and Snowflake IDs. The root Maven build includes five modules.

- `rain-uidgenerator-core/` contains shared generators, result types, and error codes.
- `rain-uidgenerator-server/` exposes the Spring Boot REST API. Its `src/main/resources/` holds Spring profiles, MyBatis XML in `mapper/`, and Flyway SQL in `db/migration/`.
- `rain-uidgenerator-client/` provides the Java SDK. `rain-uidgenerator-jmh/` benchmarks client calls and segment refills.
- `distribution/` assembles server releases. Kubernetes manifests live in `deploy/kubernetes/` and HTTP load scripts in `scripts/perf/`.

## Build, Test, and Run

Set `JAVA_HOME` to JDK 25 and run the Maven 3.9.15 wrapper from the repository root. In PowerShell, use `.\mvnw.cmd` in place of `sh mvnw`.

- `sh mvnw "-Dflatten.skip=true" clean verify` builds all modules and runs their tests.
- `sh mvnw "-Dflatten.skip=true" -pl rain-uidgenerator-core test` runs the core generator tests.
- `sh mvnw "-Dflatten.skip=true" -pl rain-uidgenerator-server -am package` builds the runnable server JAR and its dependencies.

For a local run, create the `uidgenerator` database in MySQL 8. Set `RAIN_DATASOURCE_USERNAME` and `RAIN_DATASOURCE_PASSWORD` for that database. Override `RAIN_DATASOURCE_URL` if MySQL runs elsewhere. Start the server with `java -jar rain-uidgenerator-server/target/rain-uidgenerator-server-1.0.1.jar --spring.profiles.active=local`. Flyway applies migrations at startup.

## Coding Style

Indent Java with four spaces. Use `UpperCamelCase` for types, `lowerCamelCase` for methods and fields, and `UPPER_SNAKE_CASE` for constants. Use constructor injection for Spring dependencies. Match the surrounding code's formatting. Maven has no configured Java formatter or linter.

## Testing Guidelines

Mirror the production package from `src/main/java/` under the module's `src/test/java/`. Name JUnit Jupiter classes `*Test` and describe behavior in method names such as `generatesUniqueIdsUnderConcurrency`. Use MockMvc to check the API's `Result<T>` envelope and error codes. Start Docker to include `FlywayMysqlIntegrationTest`, which uses Testcontainers with MySQL 8.4 and skips automatically when Docker is unavailable. Maven sets no minimum coverage percentage.

## Commits and Pull Requests

Recent commit subjects start with an issue tag such as `[ISSUE #8]` followed by a short description. In the PR, link the related issue, explain the behavior change, and list the checks you ran. For public contract changes, update the [API guide](docs/api.md) or [SDK guide](docs/sdk.md). PRs targeting `main` trigger Maven verification, a Docker image build and scan, and a Kubernetes manifest dry run.
