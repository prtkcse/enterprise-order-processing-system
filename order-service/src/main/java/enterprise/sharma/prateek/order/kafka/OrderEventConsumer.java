package enterprise.sharma.prateek.order.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import enterprise.sharma.prateek.common.events.InventoryReservationFailedEvent;
import enterprise.sharma.prateek.common.events.InventoryReservedEvent;
import enterprise.sharma.prateek.common.events.PaymentCompletedEvent;
import enterprise.sharma.prateek.common.events.PaymentFailedEvent;

@Component
public class OrderEventConsumer {

	private final OrderEventProcessor processor;
	private final ObjectMapper objectMapper;

	public OrderEventConsumer(OrderEventProcessor processor, ObjectMapper objectMapper) {
		this.processor = processor;
		this.objectMapper = objectMapper;
	}

	@KafkaListener(topics = "inventory.reserved", groupId = "order-service-group")
	public void onInventoryReserved(String payload, Acknowledgment acknowledgment) throws Exception {

		InventoryReservedEvent event = objectMapper.readValue(payload, InventoryReservedEvent.class);

		processor.handleInventoryReserved(event);

		acknowledgment.acknowledge();

	}

	@KafkaListener(topics = "inventory.failed", groupId = "order-service-group")
	public void onInventoryFailed(String payload, Acknowledgment acknowledgment) throws Exception {

		InventoryReservationFailedEvent event = objectMapper.readValue(payload, InventoryReservationFailedEvent.class);

		processor.handleInventoryFailed(event);

		acknowledgment.acknowledge();
	}

	@KafkaListener(topics = "payment.completed", groupId = "order-service-group")
	public void onPaymentCompleted(String payload, Acknowledgment acknowledgment) throws Exception {

		PaymentCompletedEvent event = objectMapper.readValue(payload, PaymentCompletedEvent.class);

		processor.handlePaymentCompleted(event);

		acknowledgment.acknowledge();
	}

	@KafkaListener(topics = "payment.failed", groupId = "order-service-group")
	public void onPaymentFailed(String payload, Acknowledgment acknowledgment) throws Exception {

		PaymentFailedEvent event = objectMapper.readValue(payload, PaymentFailedEvent.class);

		processor.handlePaymentFailed(event);

		acknowledgment.acknowledge();
	}

}
