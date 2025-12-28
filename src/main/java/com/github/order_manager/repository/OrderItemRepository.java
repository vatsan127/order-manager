package com.github.order_manager.repository;

import com.github.order_manager.entity.OrderItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for OrderItems entity.
 *
 * Most item operations are done through the Orders entity (cascade),
 * but this repository is useful for direct item queries.
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItems, Long> {

    /**
     * Find all items for a specific order.
     */
    List<OrderItems> findByOrderId(Long orderId);

    /**
     * Find items by product name (case-insensitive search).
     */
    List<OrderItems> findByProductNameContainingIgnoreCase(String productName);

    /**
     * Delete all items for a specific order.
     * Note: Usually handled by cascade/orphanRemoval, but useful for bulk operations.
     */
    void deleteByOrderId(Long orderId);
}
