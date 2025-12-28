# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Spring Boot 4.0.1 application demonstrating JPA one-to-many relationships between Orders and OrderItems entities. Uses Java 21, PostgreSQL, and Lombok.

**Learning Objectives:**
- Unidirectional one-to-many mapping
- Bidirectional one-to-many mapping
- Understanding owning side vs inverse side
- Cascade operations and orphan removal

## Implementation Status

| Component | Status | Notes |
|-----------|--------|-------|
| Project Infrastructure | ✅ Done | Maven, Spring Boot config |
| Configuration Files | ✅ Done | application.yaml, postgres profile, logback |
| OpenAPI/Swagger Config | ✅ Done | `OpenApiConfig.java` with server info |
| AOP Logging Aspect | ✅ Done | `LoggingAspect.java` |
| Entity Classes | ✅ Done | Orders, OrderItems, OrderStatus |
| Repositories | ✅ Done | OrderRepository, OrderItemRepository |
| Services | ✅ Done | OrderService |
| Controllers | ✅ Done | OrderController |
| Swagger Documentation | ✅ Done | All Swagger code in `swagger/` package only |
| Database Schema | ✅ Done | `schema.sql` |

## Key Dependencies

| Dependency | Version | Purpose |
|------------|---------|---------|
| Spring Boot | 4.0.1 | Parent/Framework |
| Java | 21 | Language version |
| spring-boot-starter-data-jpa | Parent | JPA/Hibernate ORM |
| spring-boot-starter-webmvc | Parent | REST API |
| spring-boot-starter-actuator | Parent | Health/Metrics |
| spring-aop | 7.0.2 | Aspect-Oriented Programming |
| springdoc-openapi-starter-webmvc-ui | 2.7.0 | Swagger UI |
| postgresql | Parent | Database driver |
| lombok | Parent | Boilerplate reduction |

## Build and Run Commands

```bash
# Build the project
./mvnw clean package

# Run the application
./mvnw spring-boot:run

# Run with specific profile
SPRING_PROFILES_ACTIVE=postgres ./mvnw spring-boot:run

# Compile only
./mvnw compile

# Run tests (when available)
./mvnw test

# Run a single test class
./mvnw test -Dtest=ClassName

# Run a single test method
./mvnw test -Dtest=ClassName#methodName
```

## Database Configuration

The application uses PostgreSQL with configuration in `application-postgres.yaml`:
- Host: `localhost:5432`
- Database: `order_db`
- Schema: `public`
- Username: `srivatsan`
- Password: `password`

**Setup**: Run `src/main/resources/schema.sql` to create tables before starting the application.

## Architecture

### Package Structure

```
src/main/java/com/github/order_manager/
├── OrderManagerApplication.java        # Main entry point
├── aspect/
│   └── LoggingAspect.java              # AOP logging for controllers/services
├── config/
│   └── OpenApiConfig.java              # Swagger/OpenAPI configuration
├── controller/
│   └── OrderController.java            # REST endpoints (implements OrderApi)
├── entity/
│   ├── Orders.java                     # Parent entity (inverse side)
│   ├── OrderItems.java                 # Child entity (owning side)
│   └── OrderStatus.java                # Status enum
├── repository/
│   ├── OrderRepository.java            # With JOIN FETCH queries
│   └── OrderItemRepository.java
├── service/
│   └── OrderService.java               # Business logic
└── swagger/
    └── OrderApi.java                   # Swagger documentation interface
```

### Entity Relationship

```
┌─────────────────┐              ┌─────────────────┐
│     Orders      │              │   OrderItems    │
├─────────────────┤              ├─────────────────┤
│ id (PK)         │──────1:N────>│ id (PK)         │
│ customerName    │              │ order_id (FK)   │
│ orderDate       │              │ productName     │
│ status          │              │ quantity        │
│ createdAt       │              │ unitPrice       │
│ updatedAt       │              │ createdAt       │
│                 │              │ updatedAt       │
└─────────────────┘              └─────────────────┘
```

### One-to-Many Relationship Implementation

**Orders (Inverse Side - One)**
- Contains a collection of OrderItems
- Uses `@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)`
- Does NOT own the foreign key
- Has `addItem()` and `removeItem()` helper methods for bidirectional sync

**OrderItems (Owning Side - Many)**
- Contains the foreign key (`order_id`) to Orders
- Uses `@ManyToOne(fetch = FetchType.LAZY)` with `@JoinColumn` to define the relationship
- Uses `@JsonIgnore` on the `order` field to prevent infinite recursion
- The foreign key constraint is named `FK_ITEM_TO_ORDER`

### Key Implementation Details

1. **Foreign Key Ownership**: OrderItems entity owns the relationship via the `order_id` column
2. **Orphan Removal**: `orphanRemoval = true` ensures removed items are deleted from DB
3. **Cascade Operations**: `CascadeType.ALL` propagates all operations from Order to Items
4. **Bidirectional Sync**: Helper methods `addItem()`/`removeItem()` in Orders entity sync both sides
5. **Hibernate DDL Mode**: Set to `none` - run `schema.sql` manually
6. **SQL Logging**: Via `org.hibernate.SQL=DEBUG` in logback (not `show-sql` to avoid duplicate logging)
7. **Lombok Usage**: `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor` (no `@Builder`)
8. **Context Path**: API is served at `/order-manager/v1` (constructed from app name and version)
9. **AOP Logging**: Uses only `@Around` advice to avoid duplicate logging (no `@Before`/`@AfterThrowing`)
10. **No Intermediate Table**: One-to-many uses direct FK in child table, not a join table (join tables are for many-to-many)
11. **N+1 Prevention**: `OrderRepository` uses `JOIN FETCH` in custom queries
12. **JSON Serialization**: `@JsonIgnore` on `OrderItems.order` prevents infinite loop
13. **Column Naming**: No `name` attribute in `@Column` - Hibernate auto-converts camelCase to snake_case
14. **Automatic Timestamps**: `@PrePersist` and `@PreUpdate` lifecycle callbacks set `createdAt`/`updatedAt`

### Unidirectional vs Bidirectional

**Unidirectional (One side only knows about relationship)**
```java
// Only Orders has the collection, OrderItems has no back-reference
@OneToMany(cascade = CascadeType.ALL)
@JoinColumn(name = "order_id") // FK is here, not in @ManyToOne
private List<OrderItems> orderItems;
```

**Bidirectional (Both sides know about each other) - IMPLEMENTED**
```java
// Orders (inverse side)
@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
private List<OrderItems> orderItems;

// OrderItems (owning side)
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "order_id")
@JsonIgnore
private Orders order;
```

## API Endpoints

### Order Endpoints

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| GET | `/orders` | Retrieve all orders with items | 200 OK |
| GET | `/orders/{id}` | Retrieve order by id | 200 OK |
| POST | `/orders` | Create new order with items | 201 Created |
| PUT | `/orders/{id}` | Update existing order | 200 OK |
| DELETE | `/orders/{id}` | Delete order and all items | 204 No Content |

### Order Items Endpoints

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| POST | `/orders/{id}/items` | Add item to existing order | 201 Created |
| PUT | `/orders/{id}/items/{itemId}` | Update item in order | 200 OK |
| DELETE | `/orders/{id}/items/{itemId}` | Remove item from order | 204 No Content |

**Full API Path**: `http://localhost:8080/order-manager/v1{endpoint}`

**Swagger UI**: `http://localhost:8080/order-manager/v1/swagger-ui.html`

## Important Notes

- Constructor-based dependency injection (no `@Autowired`)
- JPA `open-in-view: false` to prevent lazy loading issues
- `@JsonIgnore` on `OrderItems.order` prevents infinite recursion
- Always use helper methods `addItem()`/`removeItem()` for bidirectional sync
- All Swagger/OpenAPI code is isolated in `swagger/` package only (no `@Schema` annotations in entities)
- `LoggingAspect` uses only `@Around` advice (avoids duplicate entry/exception logs)
- No `@Builder` annotation - use setters or all-args constructor
- No `name` in `@Column` - Hibernate naming strategy handles camelCase → snake_case

## Helper Methods for Bidirectional Sync

**Why needed:** The owning side (`@ManyToOne`) controls the FK. If you only update the inverse side (`@OneToMany`), the FK stays null in database.

**Rule:** Always sync both sides when adding/removing items. Use helper methods to avoid mistakes:

```java
// In Orders.java
public void addItem(OrderItems item) {
    orderItems.add(item);   // Parent knows child
    item.setOrder(this);    // Child knows parent (sets FK!)
}

public void removeItem(OrderItems item) {
    orderItems.remove(item);
    item.setOrder(null);
}
```

## Common Pitfalls

### N+1 Query Problem
- **Issue**: Fetching N orders triggers N additional queries for items
- **Solution**: Use `JOIN FETCH` queries in `OrderRepository`

### Orphan Removal vs Cascade Remove
- `CascadeType.REMOVE`: Deletes items when order is deleted
- `orphanRemoval = true`: Also deletes items when removed from collection

### Bidirectional Relationship Not Synced
- **Issue**: Adding item to collection without setting back-reference
- **Solution**: Use `addItem()`/`removeItem()` helper methods

### JSON Infinite Recursion
- **Issue**: Jackson tries to serialize Order -> Items -> Order -> Items...
- **Solution**: `@JsonIgnore` on `OrderItems.order` field

## File Structure

```
src/main/java/com/github/order_manager/
├── OrderManagerApplication.java
├── aspect/
│   └── LoggingAspect.java
├── config/
│   └── OpenApiConfig.java
├── controller/
│   └── OrderController.java
├── entity/
│   ├── Orders.java
│   ├── OrderItems.java
│   └── OrderStatus.java
├── repository/
│   ├── OrderRepository.java
│   └── OrderItemRepository.java
├── service/
│   └── OrderService.java
└── swagger/
    └── OrderApi.java

src/main/resources/
├── application.yaml
├── application-postgres.yaml
├── logback-spring.xml
└── schema.sql
```
