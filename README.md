# JPA One-to-Many Mapping Notes

## Project Setup

```bash
# Create the database
psql -d postgres

create database order_db;

\q
```

---

## Entity Relationship

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

---

## Overview

A one-to-many relationship means that one entity (parent) is associated with multiple instances of another entity (children). This is the most common relationship type in database design.

---

## Key Concepts

### Bidirectional vs Unidirectional
- **Unidirectional**: Only one entity knows about the relationship
  - Parent-to-Child: Parent has collection, child has no back-reference
  - Child-to-Parent: Child has reference, parent has no collection
- **Bidirectional**: Both entities know about each other (recommended)

### Owning Side vs Inverse Side
- **Owning Side**: Always the "Many" side (child) - contains the foreign key
- **Inverse Side**: The "One" side (parent) - uses `mappedBy` to reference owning side
- In one-to-many, the **Many side always owns the relationship** because it has the FK column

### Why Many Side Owns?
- Foreign key is in the child table (`order_id` in `order_items`)
- Parent table cannot have multiple FK values for multiple children
- This is opposite of one-to-one where either side can own

---

## Important Annotations

### @OneToMany
Marks the "One" side of the relationship (parent/inverse side)

**Attributes:**
- `mappedBy`: Field name in child entity that owns the relationship (required for bidirectional)
- `cascade`: Defines cascade operations (PERSIST, MERGE, REMOVE, REFRESH, DETACH, ALL)
- `fetch`: Lazy or Eager loading (default is **LAZY** for @OneToMany)
- `orphanRemoval`: Whether to remove children when removed from collection (default is false)

**Orphan Removal Explained**: When `orphanRemoval = true`, removing an item from the collection (`order.getItems().remove(item)`) will delete it from the database. Without it, the item remains in DB as an orphan.

### @ManyToOne
Marks the "Many" side of the relationship (child/owning side)

**Attributes:**
- `fetch`: Lazy or Eager loading (default is **EAGER** for @ManyToOne)
- `optional`: Whether the relationship is optional (default is true)
- `cascade`: Defines cascade operations (rarely used on Many side)

### @JoinColumn
Specifies the foreign key column (used on owning side)

**Attributes:**
- `name`: Name of the foreign key column
- `referencedColumnName`: Column in the target entity (usually primary key)
- `nullable`: Whether the foreign key can be null
- `foreignKey`: Foreign Key constraint configurations

**What if @JoinColumn is not specified?**
- JPA will create a default foreign key column name
- Default naming: `{property_name}_{referenced_column_name}`
- Example: If property is "order" and Order's PK is "id", column will be "order_id"
- It's better to explicitly specify @JoinColumn for clarity!

---

## Unidirectional vs Bidirectional

### Unidirectional @OneToMany (Parent only)
- Parent has `@OneToMany` with `@JoinColumn`
- Child has NO reference to parent
- **Drawback**: Hibernate generates extra UPDATE statements to set FK after insert

**Why extra UPDATE?** Child entity has no `order` field, so Hibernate can't set `order_id` during INSERT. It must come back and UPDATE.

```java
// Parent has collection, child knows nothing
@Entity
public class Orders {
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "order_id")  // FK defined here, not in child
    private List<OrderItems> orderItems;
}
```

**SQL Generated (3 statements instead of 2):**
```sql
INSERT INTO orders (order_number) VALUES ('ORD-001');
INSERT INTO order_items (product_name) VALUES ('Laptop');  -- No FK!
UPDATE order_items SET order_id = 1 WHERE id = 1;          -- Extra UPDATE
```

**Bidirectional fix:** When child has `@ManyToOne`, FK is set during INSERT - no extra UPDATE needed.

### Unidirectional @ManyToOne (Child only)
- Child has `@ManyToOne` with `@JoinColumn`
- Parent has NO collection
- **Drawback**: Cannot navigate from parent to children without query

**Simply put**: Child remembers its parent, but parent doesn't keep track of its children.

```java
// Child CAN find its parent
OrderItems item = ...;
Orders parent = item.getOrder();  // ✅ Works

// Parent CANNOT find its children
Orders order = ...;
order.getItems();  // ❌ No such method exists
```

To get all items for an order, you must run a separate database query.

### Bidirectional (Recommended)
- Parent has `@OneToMany(mappedBy = "order")`
- Child has `@ManyToOne` with `@JoinColumn`
- **Advantage**: Navigate both directions, most efficient SQL

---

## Cascade Types

- **CascadeType.PERSIST**: When you save an Order, its OrderItems are also automatically saved
  - Example: `em.persist(order)` → all items also persisted

- **CascadeType.MERGE**: When you merge (update) an Order, its OrderItems are also merged
  - Example: `em.merge(order)` → all items also merged

- **CascadeType.REMOVE**: When you delete an Order, its OrderItems are also deleted
  - Example: `em.remove(order)` → all items also removed

- **CascadeType.REFRESH**: When you refresh an Order from DB, its OrderItems are also refreshed
  - Example: `em.refresh(order)` → all items also refreshed

- **CascadeType.DETACH**: When you detach an Order from persistence context, its OrderItems are also detached
  - Example: `em.detach(order)` → all items also detached

- **CascadeType.ALL**: All of the above operations cascade to related entities

---

## Orphan Removal vs Cascade Remove

| Scenario | CascadeType.REMOVE | orphanRemoval = true |
|----------|-------------------|---------------------|
| Delete parent entity | Children deleted ✓ | Children deleted ✓ |
| Remove child from collection | Child stays in DB ✗ | Child deleted ✓ |
| Clear collection | Items stay in DB ✗ | All items deleted ✓ |
| Set collection to null | Items stay in DB ✗ | All items deleted ✓ |

**Rule**: Use `orphanRemoval = true` when children cannot exist without parent.

---

## Fetch Types

- **FetchType.LAZY**: Related entities loaded on-demand
  - Default for `@OneToMany`
  - Recommended for collections to avoid loading unnecessary data

- **FetchType.EAGER**: Related entities loaded immediately with parent
  - Default for `@ManyToOne`
  - Avoid for `@OneToMany` collections (causes N+1 problem)

**Recommendation**:
- `@OneToMany`: Keep default LAZY
- `@ManyToOne`: Consider LAZY if parent is rarely needed

---

## Key Implementation Points

1. OrderItems entity has `@ManyToOne` with `@JoinColumn` (owning side - has FK)
2. Orders entity has `@OneToMany` with `mappedBy` (inverse side) - **bidirectional relationship**
3. Using `CascadeType.ALL` means saving an Order will also save its OrderItems
4. Using `orphanRemoval = true` ensures removing item from collection deletes it from DB
5. Default `FetchType.LAZY` is used for @OneToMany; keep it for performance
6. Always initialize collections: `List<OrderItems> items = new ArrayList<>()`
7. Use helper methods to synchronize both sides of bidirectional relationship

---

## Common Pitfalls

### N+1 Query Problem

**Why "N+1"?** Imagine fetching 100 orders:
- 1 query to get all orders: `SELECT * FROM orders` (this is the "1")
- Then N queries (100 queries) to get each order's items:
  - `SELECT * FROM order_items WHERE order_id = ?` (order 1)
  - `SELECT * FROM order_items WHERE order_id = ?` (order 2)
  - ... (100 times - this is the "N")
- **Total = 1 + 100 = 101 queries!** This is the N+1 problem

**Solution**: Use JOIN FETCH when you need the collection
```sql
SELECT o FROM Orders o LEFT JOIN FETCH o.orderItems
```
This executes only 1 query instead of 101!

### Bidirectional Relationship Not Synced
- **Issue**: Adding item to collection without setting back-reference (or vice versa)
- **Solution**: Use helper methods that sync both sides (addItem/removeItem)

### Unidirectional @OneToMany Performance
- **Issue**: Extra UPDATE statements generated to set FK
- **Solution**: Use bidirectional mapping instead

### Infinite Recursion in JSON Serialization
- **Issue**: In bidirectional relationships, serializing to JSON causes infinite loop (Order → Items → Order → ...)
- **Solution**: Add `@JsonIgnore` on the child's back-reference field (the @ManyToOne side)

### Collection Not Initialized
- **Issue**: NullPointerException when adding items to uninitialized collection
- **Solution**: Always initialize: `private List<OrderItems> items = new ArrayList<>()`

### Missing Cascade
- **Issue**: Saving parent doesn't save new children
- **Solution**: Add `cascade = CascadeType.ALL` or at least `CascadeType.PERSIST`

---

## Database Schema

```sql
CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    order_number VARCHAR(20) NOT NULL UNIQUE,
    customer_name VARCHAR(100) NOT NULL,
    order_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
);

CREATE TABLE order_items (
    id SERIAL PRIMARY KEY,
    order_id INTEGER NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    CONSTRAINT FK_ITEM_TO_ORDER FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- Index on FK for better join performance
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
```

---

## Sample Data

```sql
INSERT INTO orders (order_number, customer_name, status)
VALUES ('ORD-001', 'John Doe', 'PENDING');

INSERT INTO order_items (order_id, product_name, quantity, unit_price)
VALUES
    (1, 'Laptop', 1, 999.99),
    (1, 'Mouse', 2, 29.99),
    (1, 'Keyboard', 1, 79.99);
```

---

## One-to-One vs One-to-Many Comparison

| Aspect | One-to-One | One-to-Many |
|--------|------------|-------------|
| FK Location | Either side can own | Many side always owns |
| Collection | No | Yes (on One side) |
| Default Fetch (@One side) | EAGER | LAZY |
| Default Fetch (@Many side) | - | EAGER |
| mappedBy location | On inverse side | On One side (parent) |
| Unique FK | Yes (enforced) | No (multiple children) |
| @JoinColumn location | Owning side | Many side (child) |
