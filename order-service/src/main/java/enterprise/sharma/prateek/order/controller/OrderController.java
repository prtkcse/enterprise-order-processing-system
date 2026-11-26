package enterprise.sharma.prateek.order.controller;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	
	private static final Logger log = LoggerFactory.getLogger(OrderController.class);

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@PostMapping
	public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
		
		log.info("CreateOrderRequest received : "+request);

		OrderResponse response = orderService.createOrder(request);
		
		log.info("OrderResponse sent : "+response);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);

	}

	@GetMapping("/{orderId}")
	public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID orderId) {
		
		log.info("getOrder request received with orderId : "+orderId);
		return ResponseEntity.ok(orderService.getOrder(orderId));
	}

	@GetMapping
	public ResponseEntity<Page<OrderResponse>> getOrders(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		log.info("getOrders request received");
		return ResponseEntity.ok(orderService.getOrders(page, size));
	}

	@GetMapping("/customer/{customerId}")
	public ResponseEntity<Page<OrderResponse>> getCustomerOrders(@PathVariable String customerId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
		log.info("getCustomerOrders request received with customerId : "+customerId);
		return ResponseEntity.ok(orderService.getCustomerOrders(customerId, page, size));
	}

	@PatchMapping("/{orderId}/cancel")
	public ResponseEntity<CancelOrderResponse> cancelOrder(@PathVariable UUID orderId) {
		log.info("cancelOrder request received with orderId : "+orderId);
		return ResponseEntity.ok(orderService.cancelOrder(orderId));
	}

	@GetMapping("/{orderId}/status")
	public ResponseEntity<OrderResponse> getOrderStatus(@PathVariable UUID orderId) {
		log.info("getOrderStatus request received with orderId : "+orderId);
		return ResponseEntity.ok(orderService.getOrder(orderId));
	}

}
