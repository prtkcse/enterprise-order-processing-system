package enterprise.sharma.prateek.order.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import enterprise.sharma.prateek.common.events.OrderCreatedEvent;
import enterprise.sharma.prateek.order.dto.CancelOrderResponse;
import enterprise.sharma.prateek.order.dto.CreateOrderRequest;
import enterprise.sharma.prateek.order.dto.OrderItemRequest;
import enterprise.sharma.prateek.order.dto.OrderResponse;
import enterprise.sharma.prateek.order.entity.Order;
import enterprise.sharma.prateek.order.entity.OrderItem;
import enterprise.sharma.prateek.order.entity.OrderStatus;
import enterprise.sharma.prateek.order.entity.OutboxEvent;
import enterprise.sharma.prateek.order.exception.InvalidOrderException;
import enterprise.sharma.prateek.order.exception.OrderNotFoundException;
import enterprise.sharma.prateek.order.mapper.OrderMapper;
import enterprise.sharma.prateek.order.repository.OrderRepository;
import enterprise.sharma.prateek.order.repository.OutboxEventRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderServiceImpl implements OrderService {

	private final OrderRepository orderRepository;
	private final OutboxEventRepository outboxRepository;
	private final OrderMapper orderMapper;
	private final ObjectMapper objectMapper;

	public OrderServiceImpl(OrderRepository orderRepository, OutboxEventRepository outboxRepository,
			OrderMapper orderMapper, ObjectMapper objectMapper) {
		this.orderRepository = orderRepository;
		this.outboxRepository = outboxRepository;
		this.orderMapper = orderMapper;
		this.objectMapper = objectMapper;
	}

	@Override
	@Transactional
	public OrderResponse createOrder(CreateOrderRequest request) {

		Order order = new Order(request.customerId());

		for (OrderItemRequest itemRequest : request.items()) {

			OrderItem item = new OrderItem(itemRequest.productId(), itemRequest.quantity(), itemRequest.unitPrice());

			order.addItem(item);
		}

		order.calculateTotal();

		Order savedOrder = orderRepository.save(order);

		// Create event payload
		List<OrderCreatedEvent.OrderItemEvent> eventItems = savedOrder.getItems().stream()
				.map(item -> new OrderCreatedEvent.OrderItemEvent(item.getProductId(), item.getQuantity(),
						item.getUnitPrice()))
				.toList();

		OrderCreatedEvent event = new OrderCreatedEvent(UUID.randomUUID(), savedOrder.getId(),
				savedOrder.getCustomerId(), eventItems, savedOrder.getTotalAmount(), LocalDateTime.now());

		// Serialize event
		String payload = serialize(event);

		// Save event to Outbox in SAME transaction
		OutboxEvent outboxEvent = new OutboxEvent(savedOrder.getId(), "OrderCreated", "order.created",
				savedOrder.getId().toString(), payload);

		outboxRepository.save(outboxEvent);

		return orderMapper.toResponse(savedOrder);

	}

	@Override
	@Transactional(readOnly = true)
	public OrderResponse getOrder(UUID orderId) {

		Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));

		return orderMapper.toResponse(order);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<OrderResponse> getOrders(int page, int size) {

		validatePage(page, size);

		return orderRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
				.map(orderMapper::toResponse);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<OrderResponse> getCustomerOrders(String customerId, int page, int size) {

		validatePage(page, size);

		if (customerId == null || customerId.isBlank()) {
			throw new InvalidOrderException("Customer ID is required");
		}

		return orderRepository
				.findByCustomerId(customerId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
				.map(orderMapper::toResponse);
	}

	@Override
	@Transactional
	public CancelOrderResponse cancelOrder(UUID orderId) {

		Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));

		if (order.getStatus() == OrderStatus.CONFIRMED) {
			throw new InvalidOrderException("Confirmed orders cannot be cancelled through this API");
		}

		if (order.getStatus() == OrderStatus.CANCELLED) {
			return new CancelOrderResponse(order.getId(), order.getStatus().name(), "Order is already cancelled",
					order.getUpdatedAt());
		}

		if (order.getStatus() == OrderStatus.PAYMENT_PROCESSING) {
			throw new InvalidOrderException("Order cannot be cancelled while payment is processing");
		}

		order.updateStatus(OrderStatus.CANCELLED);

		// Persist a cancellation event atomically.
		String payload = serialize(java.util.Map.of("eventId", UUID.randomUUID().toString(), "orderId",
				order.getId().toString(), "customerId", order.getCustomerId(), "eventType", "OrderCancelled",
				"cancelledAt", LocalDateTime.now().toString()));

		outboxRepository.save(
				new OutboxEvent(order.getId(), "OrderCancelled", "order.cancelled", order.getId().toString(), payload));

		return new CancelOrderResponse(order.getId(), order.getStatus().name(), "Order cancelled successfully",
				order.getUpdatedAt());
	}

	private String serialize(Object object) {
		try {
			return objectMapper.writeValueAsString(object);
		} catch (JsonProcessingException ex) {
			throw new IllegalStateException("Unable to serialize event", ex);
		}
	}

	private void validatePage(int page, int size) {

		if (page < 0) {
			throw new InvalidOrderException("Page must not be negative");
		}

		if (size < 1 || size > 100) {
			throw new InvalidOrderException("Page size must be between 1 and 100");
		}
	}

}
