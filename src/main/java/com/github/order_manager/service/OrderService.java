package com.github.order_manager.service;

import com.github.order_manager.entity.OrderItems;
import com.github.order_manager.entity.Orders;
import com.github.order_manager.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Service layer for Order operations.
 * Uses constructor-based dependency injection (no @Autowired).
 */
@Service
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Get all orders with their items.
     * Uses JOIN FETCH to avoid N+1 query problem.
     */
    public List<Orders> getAllOrders() {
        return orderRepository.findAllWithItems();
    }

    /**
     * Get order by ID with items.
     */
    public Orders getOrderById(Long id) {
        return orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new NoSuchElementException("Order not found with id: " + id));
    }

    /**
     * Create a new order with items.
     * Uses helper method to sync bidirectional relationship.
     */
    public Orders createOrder(Orders order) {
        // Sync bidirectional relationship for each item
        if (order.getOrderItems() != null) {
            for (OrderItems item : order.getOrderItems()) {
                item.setOrder(order);
            }
        }

        return orderRepository.save(order);
    }

    /**
     * Update an existing order.
     */
    public Orders updateOrder(Long id, Orders orderDetails) {
        Orders order = getOrderById(id);

        order.setCustomerName(orderDetails.getCustomerName());
        order.setOrderDate(orderDetails.getOrderDate());
        order.setStatus(orderDetails.getStatus());

        return orderRepository.save(order);
    }

    /**
     * Delete an order and all its items (cascade).
     */
    public void deleteOrder(Long id) {
        Orders order = getOrderById(id);
        orderRepository.delete(order);
    }

    /**
     * Add item to existing order.
     * Uses helper method to maintain bidirectional consistency.
     */
    public Orders addItemToOrder(Long orderId, OrderItems item) {
        Orders order = getOrderById(orderId);
        order.addItem(item);
        return orderRepository.save(order);
    }

    /**
     * Update an item in an order.
     */
    public Orders updateOrderItem(Long orderId, Long itemId, OrderItems itemDetails) {
        Orders order = getOrderById(orderId);

        OrderItems item = order.getOrderItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("OrderItem not found with id: " + itemId));

        item.setProductName(itemDetails.getProductName());
        item.setQuantity(itemDetails.getQuantity());
        item.setUnitPrice(itemDetails.getUnitPrice());

        return orderRepository.save(order);
    }

    /**
     * Remove item from order.
     * Uses helper method and orphanRemoval deletes from DB.
     */
    public Orders removeItemFromOrder(Long orderId, Long itemId) {
        Orders order = getOrderById(orderId);

        OrderItems item = order.getOrderItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("OrderItem not found with id: " + itemId));

        order.removeItem(item);
        return orderRepository.save(order);
    }
}
