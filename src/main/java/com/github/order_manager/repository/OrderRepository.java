package com.github.order_manager.repository;

import com.github.order_manager.entity.Orders;
import com.github.order_manager.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Orders entity.
 *
 * Spring Data JPA automatically implements CRUD operations.
 * Custom queries use JOIN FETCH to avoid N+1 problem when loading items.
 */
@Repository
public interface OrderRepository extends JpaRepository<Orders, Long> {

    /**
     * Find all orders by status.
     */
    List<Orders> findByStatus(OrderStatus status);

    /**
     * Find all orders with their items eagerly loaded.
     *
     * Uses JOIN FETCH to load items in a single query, avoiding N+1 problem.
     * Without this, fetching N orders would trigger N additional queries for items.
     */
    @Query("SELECT DISTINCT o FROM Orders o LEFT JOIN FETCH o.orderItems")
    List<Orders> findAllWithItems();

    /**
     * Find order by ID with items eagerly loaded.
     *
     * JOIN FETCH ensures items are loaded in the same query as the order.
     */
    @Query("SELECT o FROM Orders o LEFT JOIN FETCH o.orderItems WHERE o.id = :id")
    Optional<Orders> findByIdWithItems(@Param("id") Long id);
}
