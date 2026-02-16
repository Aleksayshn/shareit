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

## How To Run

Prerequisites:
- JDK 25
- Maven 3.9+

### Option A: Run tests (in-memory H2 already configured for tests)

```bash
mvn test
```

Test H2 config is in:
- `src/test/resources/application.properties`

### Option B: Run the application with in-memory DB (H2)

Default `src/main/resources/application.properties` points to PostgreSQL.
If you want to run the app without PostgreSQL, start it with runtime overrides:

```bash
mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.datasource.url=jdbc:h2:mem:shareit;DB_CLOSE_DELAY=-1 --spring.datasource.driver-class-name=org.h2.Driver --spring.datasource.username=sa --spring.datasource.password= --spring.jpa.hibernate.ddl-auto=none --spring.sql.init.mode=always --spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
```

Schema is loaded from:
- `src/main/resources/schema.sql`

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

