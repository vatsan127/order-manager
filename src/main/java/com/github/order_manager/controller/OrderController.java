package com.github.order_manager.controller;

import com.github.order_manager.entity.OrderItems;
import com.github.order_manager.entity.Orders;
import com.github.order_manager.service.OrderService;
import com.github.order_manager.swagger.OrderApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for Order operations.
 * Implements OrderApi for Swagger documentation.
 */
@RestController
@RequestMapping("/orders")
public class OrderController implements OrderApi {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    @GetMapping
    public ResponseEntity<List<Orders>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<Orders> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @Override
    @PostMapping
    public ResponseEntity<Orders> createOrder(@RequestBody Orders order) {
        Orders created = orderService.createOrder(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<Orders> updateOrder(@PathVariable Long id, @RequestBody Orders order) {
        return ResponseEntity.ok(orderService.updateOrder(id, order));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PostMapping("/{id}/items")
    public ResponseEntity<Orders> addItemToOrder(@PathVariable Long id, @RequestBody OrderItems item) {
        Orders updated = orderService.addItemToOrder(id, item);
        return ResponseEntity.status(HttpStatus.CREATED).body(updated);
    }

    @Override
    @PutMapping("/{id}/items/{itemId}")
    public ResponseEntity<Orders> updateOrderItem(
            @PathVariable Long id,
            @PathVariable Long itemId,
            @RequestBody OrderItems item) {
        return ResponseEntity.ok(orderService.updateOrderItem(id, itemId, item));
    }

    @Override
    @DeleteMapping("/{id}/items/{itemId}")
    public ResponseEntity<Void> removeItemFromOrder(@PathVariable Long id, @PathVariable Long itemId) {
        orderService.removeItemFromOrder(id, itemId);
        return ResponseEntity.noContent().build();
    }
}
