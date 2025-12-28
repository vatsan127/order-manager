package com.github.order_manager.swagger;

import com.github.order_manager.entity.OrderItems;
import com.github.order_manager.entity.Orders;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Swagger/OpenAPI documentation interface for Order endpoints.
 * Separates API documentation from controller implementation.
 */
@Tag(name = "Orders", description = "Order management APIs")
public interface OrderApi {

    @Operation(summary = "Get all orders", description = "Retrieve all orders with their items")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all orders",
                    content = @Content(schema = @Schema(implementation = Orders.class)))
    })
    ResponseEntity<List<Orders>> getAllOrders();

    @Operation(summary = "Get order by ID", description = "Retrieve a specific order with its items")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved order",
                    content = @Content(schema = @Schema(implementation = Orders.class))),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content)
    })
    ResponseEntity<Orders> getOrderById(
            @Parameter(description = "Order ID", example = "1", required = true) @PathVariable Long id);

    @Operation(summary = "Create order", description = "Create a new order with items")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created successfully",
                    content = @Content(schema = @Schema(implementation = Orders.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content)
    })
    ResponseEntity<Orders> createOrder(
            @RequestBody(
                    description = "Order to create",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = Orders.class),
                            examples = @ExampleObject(
                                    name = "Create order with items",
                                    value = """
                                            {
                                              "customerName": "John Doe",
                                              "orderItems": [
                                                {
                                                  "productName": "Laptop",
                                                  "quantity": 1,
                                                  "unitPrice": 999.99
                                                },
                                                {
                                                  "productName": "Mouse",
                                                  "quantity": 2,
                                                  "unitPrice": 29.99
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody Orders order);

    @Operation(summary = "Update order", description = "Update an existing order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order updated successfully",
                    content = @Content(schema = @Schema(implementation = Orders.class))),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content)
    })
    ResponseEntity<Orders> updateOrder(
            @Parameter(description = "Order ID", example = "1", required = true) @PathVariable Long id,
            @RequestBody(
                    description = "Updated order data",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = Orders.class),
                            examples = @ExampleObject(
                                    name = "Update order",
                                    value = """
                                            {
                                              "customerName": "Jane Doe",
                                              "status": "PENDING"
                                            }
                                            """
                            )
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody Orders order);

    @Operation(summary = "Delete order", description = "Delete an order and all its items")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Order deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content)
    })
    ResponseEntity<Void> deleteOrder(
            @Parameter(description = "Order ID", example = "1", required = true) @PathVariable Long id);

    @Operation(summary = "Add item to order", description = "Add a new item to an existing order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Item added successfully",
                    content = @Content(schema = @Schema(implementation = Orders.class))),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content)
    })
    ResponseEntity<Orders> addItemToOrder(
            @Parameter(description = "Order ID", example = "1", required = true) @PathVariable Long id,
            @RequestBody(
                    description = "Item to add",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = OrderItems.class),
                            examples = @ExampleObject(
                                    name = "Add item",
                                    value = """
                                            {
                                              "productName": "Keyboard",
                                              "quantity": 1,
                                              "unitPrice": 79.99
                                            }
                                            """
                            )
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody OrderItems item);

    @Operation(summary = "Update order item", description = "Update an item in an order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item updated successfully",
                    content = @Content(schema = @Schema(implementation = Orders.class))),
            @ApiResponse(responseCode = "404", description = "Order or item not found", content = @Content)
    })
    ResponseEntity<Orders> updateOrderItem(
            @Parameter(description = "Order ID", example = "1", required = true) @PathVariable Long id,
            @Parameter(description = "Item ID", example = "1", required = true) @PathVariable Long itemId,
            @RequestBody(
                    description = "Updated item data",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = OrderItems.class),
                            examples = @ExampleObject(
                                    name = "Update item",
                                    value = """
                                            {
                                              "productName": "Gaming Keyboard",
                                              "quantity": 2,
                                              "unitPrice": 129.99
                                            }
                                            """
                            )
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody OrderItems item);

    @Operation(summary = "Remove item from order", description = "Remove an item from an order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Item removed successfully"),
            @ApiResponse(responseCode = "404", description = "Order or item not found", content = @Content)
    })
    ResponseEntity<Void> removeItemFromOrder(
            @Parameter(description = "Order ID", example = "1", required = true) @PathVariable Long id,
            @Parameter(description = "Item ID", example = "1", required = true) @PathVariable Long itemId);
}
