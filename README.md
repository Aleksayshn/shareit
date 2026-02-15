# java-shareit
Template repository for Shareit project.

## SOLID Principles in This Project

This project applies SOLID mostly at the data/domain boundary level and through Spring abstractions.

| Principle | Where it is applied | How it is applied |
|---|---|---|
| Single Responsibility Principle (SRP) | `com.ct5121.shareit.user.model.User`, `com.ct5121.shareit.item.model.Item`, `com.ct5121.shareit.user.repository.UserRepository`, `com.ct5121.shareit.item.repository.ItemRepository`, `com.ct5121.shareit.config.DataInitializer` | Each class/interface has one core reason to change: entities describe persistence state, repositories declare data access contracts, initializer seeds data at startup. |
| Open/Closed Principle (OCP) | `UserRepository`, `ItemRepository` | Repositories are open for extension through new query methods (`findBy...`, custom `@Query`) without modifying Spring Data infrastructure. |
| Liskov Substitution Principle (LSP) | `UserRepository extends JpaRepository<User, Long>`, `ItemRepository extends JpaRepository<Item, Long>` | Repository instances can be used via `JpaRepository` contracts, and Spring-generated implementations remain substitutable for the declared interfaces. |
| Interface Segregation Principle (ISP) | `UserRepository`, `ItemRepository` | Repository interfaces expose focused methods needed by their aggregate (`User` vs `Item`) rather than one large generic DAO. |
| Dependency Inversion Principle (DIP) | `DataInitializer` constructor injection of `UserRepository` and `ItemRepository` | The configuration class depends on repository abstractions (interfaces), while Spring injects concrete implementations at runtime. |

Notes:
- The current codebase does not yet have a dedicated service/use-case layer, so business rules are minimal and close to persistence.
- A stricter SOLID application for growing features would typically introduce service interfaces and separate DTO/mapping layers.

## Clean Architecture Mapping

The current implementation is close to a simplified layered architecture. The table below maps each component to the nearest Clean Architecture role.

| Code component | Layer (Clean Architecture) | Role |
|---|---|---|
| `src/main/java/com/ct5121/shareit/ShareItApp.java` | Frameworks and Drivers | Spring Boot entry point and runtime bootstrap. |
| `src/main/java/com/ct5121/shareit/config/DataInitializer.java` | Interface Adapters / Framework glue | Startup adapter that populates data using repository ports. |
| `src/main/java/com/ct5121/shareit/user/model/User.java` | Entities (Domain) | Core domain entity persisted as `users`. |
| `src/main/java/com/ct5121/shareit/item/model/Item.java` | Entities (Domain) | Core domain entity persisted as `items` with owner relation. |
| `src/main/java/com/ct5121/shareit/user/repository/UserRepository.java` | Interface Adapters (Repository Port + Adapter via Spring Data) | Declares user persistence operations and query contracts. |
| `src/main/java/com/ct5121/shareit/item/repository/ItemRepository.java` | Interface Adapters (Repository Port + Adapter via Spring Data) | Declares item persistence operations and search/query contracts. |
| `src/main/resources/schema.sql` | Frameworks and Drivers (Database) | Relational schema for persistence layer. |
| `src/main/resources/application.properties` | Frameworks and Drivers (Configuration) | Runtime infrastructure configuration. |
| `src/main/resources/application-test.properties` | Frameworks and Drivers (Test configuration) | Test runtime configuration. |
| `src/test/java/com/ct5121/shareit/ShareItTests.java` | Tests (cross-layer verification) | Integration/smoke validation of application context behavior. |

### Dependency Direction (Current)

- Domain entities (`user.model`, `item.model`) are used by repository contracts and infrastructure.
- Repository interfaces are consumed by configuration/bootstrap (`DataInitializer`) through dependency injection.
- Spring Boot and JPA infrastructure provide concrete implementations and runtime wiring.

### Future Clean Architecture Alignment 

1. Add `usecase/service` layer for application rules.
2. Introduce explicit repository ports in the application core and keep Spring Data adapters in infrastructure.
3. Add controller/API layer with DTOs and mappers to isolate transport from domain entities.
