package com.github.order_manager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * OrderItems entity - the "Many" side (owning side) of the one-to-many relationship.
 *
 * This entity OWNS the foreign key (order_id column). The @JoinColumn annotation
 * defines the FK column name and constraint.
 *
 * Why this side owns the relationship:
 * - In a one-to-many, the "many" side always has the FK column in relational databases
 * - JPA follows this pattern: the side with @JoinColumn is the owning side
 * - Changes to this.order field directly affect the FK value in database
 */
@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItems {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Many-to-one relationship to Orders (the owning side).
     *
     * - @ManyToOne: Many items belong to one order
     * - FetchType.LAZY: Don't load the parent Order until accessed (performance optimization)
     * - @JoinColumn: Defines the FK column in this table
     *   - name: The FK column name in order_items table
     *   - nullable = false: Every item must belong to an order
     *   - foreignKey: Names the constraint for clearer error messages
     *
     * @JsonIgnore prevents infinite recursion during JSON serialization:
     * Order -> items -> item.order -> items -> ... (stack overflow)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "order_id",
            nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "FK_ITEM_TO_ORDER")
    )
    @JsonIgnore
    private Orders order;

    @Column(nullable = false, length = 100)
    private String productName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * JPA lifecycle callback - called automatically before INSERT.
     * Sets timestamps when entity is first persisted.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * JPA lifecycle callback - called automatically before UPDATE.
     * Updates the timestamp when entity is modified.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
