# ShareIt

Spring Boot training project that demonstrates a layered architecture with:
- JPA repositories (data access)
- Service interfaces + implementations (application logic)
- DTO validation + business checks
- Startup `run` demo that executes service methods end-to-end

## What Was Implemented For The Task

### 1) Service interfaces and implementations (mirroring repositories)

Implemented service contracts and concrete classes for each main domain:

- Users:
  - `src/main/java/com/ct5121/shareit/user/service/UserService.java`
  - `src/main/java/com/ct5121/shareit/user/service/UserServiceImpl.java`
- Items:
  - `src/main/java/com/ct5121/shareit/item/service/ItemService.java`
  - `src/main/java/com/ct5121/shareit/item/service/ItemServiceImpl.java`
- Bookings:
  - `src/main/java/com/ct5121/shareit/booking/service/BookingService.java`
  - `src/main/java/com/ct5121/shareit/booking/service/BookingServiceImpl.java`

Repositories remain Spring Data interfaces (generated implementations at runtime):
- `src/main/java/com/ct5121/shareit/user/repository/UserRepository.java`
- `src/main/java/com/ct5121/shareit/item/repository/ItemRepository.java`
- `src/main/java/com/ct5121/shareit/item/repository/CommentRepository.java`
- `src/main/java/com/ct5121/shareit/booking/repository/BookingRepository.java`

### 2) Validation and business logic

Validation is implemented at two levels:

- DTO validation (`jakarta.validation`), for example:
  - `src/main/java/com/ct5121/shareit/user/dto/UserRequestDto.java`
  - `src/main/java/com/ct5121/shareit/item/dto/ItemRequestDto.java`
  - `src/main/java/com/ct5121/shareit/booking/dto/BookingRequestDto.java`
- Service-level business rules, for example:
  - unique user email check
  - only owner can update item
  - booking dates must be valid (`start < end`)
  - owner cannot book own item
  - only owner can approve/reject booking
  - overlapping booking prevention
  - comment allowed only after approved past booking

### 3) Running service methods in application `run` flow

Startup demo is implemented with `CommandLineRunner` in:

- `src/main/java/com/ct5121/shareit/config/DataInitializer.java`

On app start it:

1. Creates two users via `UserService`
2. Creates an item via `ItemService`
3. Creates booking via `BookingService`
4. Approves booking via `BookingService`
5. Reads booking and prints final state

This demonstrates that service wiring, validation, and business flow work together.

## Profiles

- `local` (default): local PostgreSQL + `schema.sql`
- `demo`: same bootstrap as `local`, plus demo seed data
- `prod`: Docker/Render profile, PostgreSQL only, no schema auto-init
- `test`: H2 in-memory profile for automated tests

## How To Run

Prerequisites:
- JDK 25
- Maven 3.9+

### Option A: Run tests

```bash
mvn test
```

Test config is in:
- `src/test/resources/application-test.properties`

### Option B: Run the application locally with PostgreSQL

The default profile is `local`, so `mvn spring-boot:run` uses PostgreSQL on
`localhost:5432/shareit` unless you override the datasource environment variables.

```bash
mvn spring-boot:run
```

Schema is loaded from:
- `src/main/resources/schema.sql`

Optional local datasource overrides:

```bash
DB_HOST=localhost
DB_PORT=5432
DB_NAME=shareit
DB_USERNAME=postgres
DB_PASSWORD=postgres
```

### Option C: Run the demo data profile

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=demo
```

## Render Deployment

- `Dockerfile` builds and runs the app with the `prod` profile
- `render.yaml` configures a Docker web service and health check
- Set `DATABASE_URL` in Render to the PostgreSQL internal connection string
- Set `APP_SECURITY_JWT_SECRET` in Render to a long random HS256 secret
- Set `APP_SECURITY_JWT_EXPIRATION` in Render to a Spring `Duration` value such as `24h`
- The `prod` profile never runs `schema.sql`; provision the production schema separately
- Deployed API base URL: `https://shareit-jd5v.onrender.com`

## Expected Demo Output

When the app starts successfully, console output from `DataInitializer` should include lines similar to:

- `Demo completed:`
- `Owner ID = ... , Booker ID = ...`
- `Item ID = ...`
- `Booking ID = ... , Status = APPROVED`

## Build

Compile only:

```bash
mvn -DskipTests compile
```

## Swagger / OpenAPI

Swagger support is enabled via:
- Dependency in `pom.xml`: `org.springdoc:springdoc-openapi-starter-webmvc-ui`
- OpenAPI metadata config:
  - `src/main/java/com/ct5121/shareit/config/SwaggerConfig.java`
- Endpoint-level annotations in controllers:
  - `@Tag` for grouping endpoints
  - `@Operation` for endpoint summary/description
  - `@Parameter` for path/header/query/body params
  - `@ApiResponse` for response descriptions

Local endpoints:
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON spec: `http://localhost:8080/v3/api-docs`

Render deployment endpoints:
- API base URL: `https://shareit-jd5v.onrender.com`
- Swagger UI: `https://shareit-jd5v.onrender.com/swagger-ui/index.html`
- OpenAPI JSON spec: `https://shareit-jd5v.onrender.com/v3/api-docs`

If you run locally on a different port, replace `8080` in the local URLs.
