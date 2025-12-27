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
| OpenAPI/Swagger Config | ✅ Done | `OpenApiConfig.java` |
| AOP Logging Aspect | ✅ Done | `LoggingAspect.java` |
| Entity Classes | ❌ TODO | Orders, OrderItems |
| Repositories | ❌ TODO | OrderRepository, OrderItemRepository |
| Services | ❌ TODO | OrderService, OrderItemService |
| Controllers | ❌ TODO | OrderController |
| DTOs | ❌ TODO | Request/Response objects |
| Exception Handling | ❌ TODO | @ControllerAdvice |
| Database Schema | ❌ TODO | SQL scripts for tables |

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

## Architecture

### Package Structure

**Implemented:**
- `aspect/` - AOP aspects for cross-cutting concerns (LoggingAspect.java)
- `config/` - Application configuration classes (OpenApiConfig.java)

**To Be Implemented:**
- `entity/` - JPA entities (Orders, OrderItems)
- `repository/` - Spring Data JPA repositories
- `service/` - Business logic layer
- `controller/` - REST API endpoint implementations
- `dto/` - Request/Response DTOs
- `exception/` - Custom exceptions and handlers

### Entity Relationship

```
┌─────────────────┐              ┌─────────────────┐
│     Orders      │              │   OrderItems    │
├─────────────────┤              ├─────────────────┤
│ id (PK)         │              │ id (PK)         │
│ orderNumber     │──────1:N────>│ order_id (FK)   │
│ customerName    │              │ productName     │
│ orderDate       │              │ quantity        │
│ status          │              │ unitPrice       │
└─────────────────┘              └─────────────────┘
```

### One-to-Many Relationship Implementation

**Orders (Inverse Side - One)**
- Contains a collection of OrderItems
- Uses `@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)`
- Does NOT own the foreign key

**OrderItems (Owning Side - Many)**
- Contains the foreign key (`order_id`) to Orders
- Uses `@ManyToOne` with `@JoinColumn` to define the relationship
- The foreign key constraint is named `FK_ITEM_TO_ORDER`

### Key Implementation Details

1. **Foreign Key Ownership**: OrderItems entity owns the relationship via the `order_id` column
2. **Orphan Removal**: `orphanRemoval = true` ensures removed items are deleted from DB
3. **Cascade Operations**: `CascadeType.ALL` propagates all operations from Order to Items
4. **Bidirectional Sync**: Both sides of relationship must be synchronized when adding/removing items
5. **Hibernate DDL Mode**: Set to `none` - schema changes are not auto-applied
6. **SQL Logging**: Via `org.hibernate.SQL=DEBUG` in logback (not `show-sql` to avoid duplicate logging)
7. **Lombok Usage**: `@Data` annotation generates getters/setters/toString/equals/hashCode
8. **Context Path**: API is served at `/order-manager/v1` (constructed from app name and version)
9. **AOP Logging**: Centralized logging via `LoggingAspect` - logs method entry, exit, execution time, and exceptions

### Unidirectional vs Bidirectional

**Unidirectional (One side only knows about relationship)**
```java
// Only Orders has the collection, OrderItems has no back-reference
@OneToMany(cascade = CascadeType.ALL)
@JoinColumn(name = "order_id") // FK is here, not in @ManyToOne
private List<OrderItems> orderItems;
```

**Bidirectional (Both sides know about each other)**
```java
// Orders (inverse side)
@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
private List<OrderItems> orderItems;

// OrderItems (owning side)
@ManyToOne
@JoinColumn(name = "order_id")
private Orders order;
```

## API Endpoints

### Order Endpoints

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| GET | `/orders` | Retrieve all orders with items | 200 OK |
| GET | `/orders/{id}` | Retrieve order by id | 200 OK |
| POST | `/orders` | Create new order with items | 201 Created |
| PUT | `/orders/{id}` | Update existing order and items | 200 OK |
| DELETE | `/orders/{id}` | Delete order and all items | 204 No Content |

### Order Items Endpoints

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| POST | `/orders/{id}/items` | Add item to existing order | 201 Created |
| PUT | `/orders/{id}/items/{itemId}` | Update item in order | 200 OK |
| DELETE | `/orders/{id}/items/{itemId}` | Remove item from order | 204 No Content |

**Full API Path**: `http://localhost:8080/order-manager/v1{endpoint}`

## Important Notes

- The application uses constructor-based dependency injection (no `@Autowired`)
- JPA configuration has `open-in-view: false` to prevent lazy loading issues
- Use `@JsonIgnore` on the `order` field in OrderItems to prevent infinite recursion during JSON serialization
- Always synchronize both sides of the bidirectional relationship when adding/removing items
- JPA one-to-many mapping notes documented in `README.md`

## Code Comments Guidelines

When implementing entities, repositories, services, and controllers, add explanatory comments for:

1. **Entity Relationships**: Explain why bidirectional is used, what `mappedBy` means, why Many side owns FK
2. **Cascade & Orphan Removal**: Comment on what operations cascade and why orphanRemoval is enabled
3. **Performance Considerations**: Note why LAZY fetch is used, how to avoid N+1 queries
4. **Helper Methods**: Explain why `addItem()`/`removeItem()` sync both sides of relationship
5. **SQL Behavior**: Document when extra UPDATE statements occur (unidirectional) vs direct INSERT (bidirectional)

Example:
```java
// Bidirectional mapping - child owns FK, parent uses mappedBy
// This avoids extra UPDATE statements that unidirectional @OneToMany generates
@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
private List<OrderItems> orderItems = new ArrayList<>();
```

## Helper Methods for Bidirectional Sync

Add convenience methods to Orders entity:
```java
public void addItem(OrderItems item) {
    orderItems.add(item);
    item.setOrder(this);
}

public void removeItem(OrderItems item) {
    orderItems.remove(item);
    item.setOrder(null);
}
```

## Common Pitfalls

### N+1 Query Problem
- **Issue**: Fetching N orders triggers N additional queries for items
- **Solution**: Use `JOIN FETCH` or `@EntityGraph`

### Orphan Removal vs Cascade Remove
- `CascadeType.REMOVE`: Deletes items when order is deleted
- `orphanRemoval = true`: Also deletes items when removed from collection

### Bidirectional Relationship Not Synced
- **Issue**: Adding item to collection without setting back-reference
- **Solution**: Use helper methods that sync both sides

## Current File Structure

```
src/main/java/com/github/order_manager/
├── OrderManagerApplication.java        # Main entry point
├── aspect/
│   └── LoggingAspect.java              # AOP logging for controllers/services
└── config/
    └── OpenApiConfig.java              # Swagger/OpenAPI configuration

src/main/resources/
├── application.yaml                    # Main configuration
├── application-postgres.yaml           # PostgreSQL profile config
└── logback-spring.xml                  # Logging configuration (console + file)
```
