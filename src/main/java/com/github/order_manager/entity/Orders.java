package com.github.order_manager.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Orders entity - the "One" side (inverse/parent) of the one-to-many relationship.
 *
 * This entity does NOT own the foreign key. The FK lives in OrderItems table.
 * We use mappedBy to indicate that OrderItems.order field owns the relationship.
 *
 * Key annotations:
 * - @OneToMany(mappedBy = "order"): Declares this as inverse side, "order" refers to field in OrderItems
 * - CascadeType.ALL: All operations (persist, merge, remove, refresh, detach) cascade to items
 * - orphanRemoval = true: Items removed from collection are deleted from database
 */
@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Order entity representing a customer order")
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(nullable = false, length = 100)
    @Schema(description = "Customer name", example = "John Doe", requiredMode = Schema.RequiredMode.REQUIRED)
    private String customerName;

    @Column(nullable = false)
    @Schema(description = "Order date", example = "2024-01-15T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Schema(description = "Order status", example = "PENDING", accessMode = Schema.AccessMode.READ_ONLY)
    private OrderStatus status;

    @Column(nullable = false, updatable = false)
    @Schema(description = "Creation timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @Schema(description = "Last update timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    /**
     * Bidirectional one-to-many mapping to OrderItems.
     *
     * - mappedBy = "order": The "order" field in OrderItems owns the FK (we don't define @JoinColumn here)
     * - CascadeType.ALL: When we save/delete an Order, all its items are saved/deleted too
     * - orphanRemoval = true: If an item is removed from this list, it's deleted from DB
     *   (different from CascadeType.REMOVE which only works when parent is deleted)
     * - FetchType.LAZY is default for @OneToMany, so we don't specify it explicitly
     *
     * Initialize with ArrayList to avoid NullPointerException when adding items.
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Schema(description = "List of order items")
    private List<OrderItems> orderItems = new ArrayList<>();

    /**
     * Helper method to add an item while keeping both sides of the relationship in sync.
     *
     * Why needed: The owning side (OrderItems.order) controls the FK value in database.
     * If we only add to this list without setting item.order, the FK stays NULL.
     *
     * Always use this method instead of orderItems.add() directly.
     */
    public void addItem(OrderItems item) {
        orderItems.add(item);
        item.setOrder(this);
    }

    /**
     * Helper method to remove an item while keeping both sides in sync.
     *
     * Setting item.order to null breaks the relationship from owning side.
     * Combined with orphanRemoval=true, this causes the item to be deleted from DB.
     */
    public void removeItem(OrderItems item) {
        orderItems.remove(item);
        item.setOrder(null);
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (orderDate == null) {
            orderDate = LocalDateTime.now();
        }
        if (status == null) {
            status = OrderStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
