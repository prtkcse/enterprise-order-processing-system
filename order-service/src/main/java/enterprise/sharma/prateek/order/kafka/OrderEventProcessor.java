package enterprise.sharma.prateek.order.kafka;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import enterprise.sharma.prateek.common.events.InventoryReservationFailedEvent;
import enterprise.sharma.prateek.common.events.InventoryReservedEvent;
import enterprise.sharma.prateek.common.events.PaymentCompletedEvent;
import enterprise.sharma.prateek.common.events.PaymentFailedEvent;
import enterprise.sharma.prateek.order.entity.Order;
import enterprise.sharma.prateek.order.entity.OrderStatus;
import enterprise.sharma.prateek.order.entity.OutboxEvent;
import enterprise.sharma.prateek.order.exception.OrderNotFoundException;
import enterprise.sharma.prateek.order.repository.OrderRepository;
import enterprise.sharma.prateek.order.repository.OutboxEventRepository;
import jakarta.transaction.Transactional;

@Service
public class OrderEventProcessor {

	private final OrderRepository orderRepository;
	private final OutboxEventRepository outboxRepository;
	private final ObjectMapper objectMapper;

	public OrderEventProcessor(OrderRepository orderRepository, OutboxEventRepository outboxRepository,
			ObjectMapper objectMapper) {
		this.orderRepository = orderRepository;
		this.outboxRepository = outboxRepository;
		this.objectMapper = objectMapper;
	}

	@Transactional
	public void handleInventoryReserved(InventoryReservedEvent event) {

		Order order = orderRepository.findById(event.orderId())
				.orElseThrow(() -> new OrderNotFoundException(event.orderId()));

		// Ignore events for terminal orders.
		if (isTerminal(order.getStatus())) {
			return;
		}

		if (order.getStatus() != OrderStatus.PENDING) {
			return;
		}

		order.updateStatus(OrderStatus.PAYMENT_PROCESSING);

		// Tell Payment Service to begin processing.
		saveOutbox(order.getId(), "PaymentRequested", "payment.requested", order.getId().toString(),
				Map.of("eventId", UUID.randomUUID().toString(), "orderId", order.getId().toString(), "customerId",
						order.getCustomerId(), "amount", order.getTotalAmount(), "requestedAt",
						LocalDateTime.now().toString()));

	}

	@Transactional
	public void handleInventoryFailed(InventoryReservationFailedEvent event) {

		Order order = orderRepository.findById(event.orderId())
				.orElseThrow(() -> new OrderNotFoundException(event.orderId()));

		if (isTerminal(order.getStatus())) {
			return;
		}

		order.updateStatus(OrderStatus.INVENTORY_FAILED);

		saveOutbox(order.getId(), "OrderFailed", "order.failed", order.getId().toString(),
				Map.of("eventId", UUID.randomUUID().toString(), "orderId", order.getId().toString(), "customerId",
						order.getCustomerId(), "reason", event.reason(), "failedAt", LocalDateTime.now().toString()));

	}

	@Transactional
	public void handlePaymentCompleted(PaymentCompletedEvent event) {

		Order order = orderRepository.findById(event.orderId())
				.orElseThrow(() -> new OrderNotFoundException(event.orderId()));

		if (isTerminal(order.getStatus())) {
			return;
		}

		if (order.getStatus() != OrderStatus.PAYMENT_PROCESSING) {
			return;
		}

		order.updateStatus(OrderStatus.CONFIRMED);

		saveOutbox(order.getId(), "OrderConfirmed", "order.confirmed", order.getId().toString(),
				Map.of("eventId", UUID.randomUUID().toString(), "orderId", order.getId().toString(), "customerId",
						order.getCustomerId(), "confirmedAt", LocalDateTime.now().toString()));
	}

	@Transactional
	public void handlePaymentFailed(PaymentFailedEvent event) {

		Order order = orderRepository.findById(event.orderId())
				.orElseThrow(() -> new OrderNotFoundException(event.orderId()));

		if (isTerminal(order.getStatus())) {
			return;
		}

		order.updateStatus(OrderStatus.PAYMENT_FAILED);

		saveOutbox(order.getId(), "OrderFailed", "order.failed", order.getId().toString(),
				Map.of("eventId", UUID.randomUUID().toString(), "orderId", order.getId().toString(), "customerId",
						order.getCustomerId(), "reason", event.failureReason(), "failedAt", LocalDateTime.now().toString()));

		// Compensation event: Payment failure should release reserved stock.
		saveOutbox(order.getId(), "InventoryReleaseRequested", "inventory.release.requested", order.getId().toString(),
				Map.of("eventId", UUID.randomUUID().toString(), "orderId", order.getId().toString(), "reason",
						"PAYMENT_FAILED"));
	}

	private boolean isTerminal(OrderStatus status) {

		return status == OrderStatus.CONFIRMED || status == OrderStatus.INVENTORY_FAILED
				|| status == OrderStatus.PAYMENT_FAILED || status == OrderStatus.CANCELLED;
	}

	private void saveOutbox(UUID orderId, String eventType, String topic, String eventKey, Object payload) {
		try {
			String json = objectMapper.writeValueAsString(payload);

			outboxRepository.save(new OutboxEvent(orderId, eventType, topic, eventKey, json));

		} catch (Exception ex) {
			throw new IllegalStateException("Failed to persist outgoing event", ex);
		}
	}

}
