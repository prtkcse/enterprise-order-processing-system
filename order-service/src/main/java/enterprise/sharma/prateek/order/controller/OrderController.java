package enterprise.sharma.prateek.order.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import enterprise.sharma.prateek.order.dto.CancelOrderResponse;
import enterprise.sharma.prateek.order.dto.CreateOrderRequest;
import enterprise.sharma.prateek.order.dto.OrderResponse;
import enterprise.sharma.prateek.order.service.OrderService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@PostMapping
	public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {

		OrderResponse response = orderService.createOrder(request);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);

	}

	@GetMapping("/{orderId}")
	public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID orderId) {
		return ResponseEntity.ok(orderService.getOrder(orderId));
	}

	@GetMapping
	public ResponseEntity<Page<OrderResponse>> getOrders(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return ResponseEntity.ok(orderService.getOrders(page, size));
	}

	@GetMapping("/customer/{customerId}")
	public ResponseEntity<Page<OrderResponse>> getCustomerOrders(@PathVariable String customerId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
		return ResponseEntity.ok(orderService.getCustomerOrders(customerId, page, size));
	}

	@PatchMapping("/{orderId}/cancel")
	public ResponseEntity<CancelOrderResponse> cancelOrder(@PathVariable UUID orderId) {
		return ResponseEntity.ok(orderService.cancelOrder(orderId));
	}

	@GetMapping("/{orderId}/status")
	public ResponseEntity<OrderResponse> getOrderStatus(@PathVariable UUID orderId) {
		return ResponseEntity.ok(orderService.getOrder(orderId));
	}

}
