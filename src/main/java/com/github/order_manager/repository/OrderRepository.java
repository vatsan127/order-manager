package com.github.order_manager.repository;

import com.github.order_manager.entity.Orders;
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
     * Find all orders with their items eagerly loaded.
     *
     * Uses JOIN FETCH to load items in a single query, avoiding N+1 problem.
     * Without this, fetching N orders would trigger N additional queries for items.
     */
    @Query("SELECT DISTINCT o FROM Orders o LEFT JOIN FETCH o.orderItems")
    List<Orders> findAllWithItems();

    /**
     * Find order by ID with items eagerly loaded (Native Query example).
     *
     * This method demonstrates native SQL query usage. Compare with findAllWithItems() which uses JPQL.
     *
     * JPQL vs Native Query comparison:
     * - JPQL (findAllWithItems): Database-agnostic, easier to maintain, handles entity mapping automatically
     * - Native SQL (this method): PostgreSQL-specific, useful for DB-specific features (CTEs, window functions, jsonb)
     * - Performance: Nearly identical - Hibernate generates optimal SQL from JPQL
     * - Recommendation: Prefer JPQL unless you need DB-specific features
     */
    @Query(value = "SELECT DISTINCT o.* FROM orders o LEFT JOIN order_items oi ON o.id = oi.order_id WHERE o.id = :id",
           nativeQuery = true)
    Optional<Orders> findByIdWithItems(@Param("id") Long id);
}
