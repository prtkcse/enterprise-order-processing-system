package enterprise.sharma.prateek.order.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	
	private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);
	private static final Logger kafkaConsumerLogger = LoggerFactory.getLogger("KAFKA_CONSUMER");

	private final OrderEventProcessor processor;
	private final ObjectMapper objectMapper;

	public OrderEventConsumer(OrderEventProcessor processor, ObjectMapper objectMapper) {
		this.processor = processor;
		this.objectMapper = objectMapper;
	}

	@KafkaListener(topics = "inventory.reserved", groupId = "order-service-group")
	public void onInventoryReserved(String payload, Acknowledgment acknowledgment) throws Exception {

		InventoryReservedEvent event = objectMapper.readValue(payload, InventoryReservedEvent.class);
		
		log.info("RECEIVED | Topic={} | OrderId={} | Event={}", "inventory.reserved", event.orderId(), event);
		kafkaConsumerLogger.info("RECEIVED | Topic={} | OrderId={} | Event={}", "inventory.reserved", event.orderId(), event);

		processor.handleInventoryReserved(event);

		acknowledgment.acknowledge();

	}

	@KafkaListener(topics = "inventory.failed", groupId = "order-service-group")
	public void onInventoryFailed(String payload, Acknowledgment acknowledgment) throws Exception {

		InventoryReservationFailedEvent event = objectMapper.readValue(payload, InventoryReservationFailedEvent.class);
		
		log.info("RECEIVED | Topic={} | OrderId={} | Event={}", "inventory.failed", event.orderId(), event);
		kafkaConsumerLogger.info("RECEIVED | Topic={} | OrderId={} | Event={}", "inventory.failed", event.orderId(), event);

		processor.handleInventoryFailed(event);

		acknowledgment.acknowledge();
	}

	@KafkaListener(topics = "payment.completed", groupId = "order-service-group")
	public void onPaymentCompleted(String payload, Acknowledgment acknowledgment) throws Exception {

		PaymentCompletedEvent event = objectMapper.readValue(payload, PaymentCompletedEvent.class);
		
		log.info("RECEIVED | Topic={} | OrderId={} | Event={}", "payment.completed", event.orderId(), event);
		kafkaConsumerLogger.info("RECEIVED | Topic={} | OrderId={} | Event={}", "payment.completed", event.orderId(), event);

		processor.handlePaymentCompleted(event);

		acknowledgment.acknowledge();
	}

	@KafkaListener(topics = "payment.failed", groupId = "order-service-group")
	public void onPaymentFailed(String payload, Acknowledgment acknowledgment) throws Exception {

		PaymentFailedEvent event = objectMapper.readValue(payload, PaymentFailedEvent.class);
		
		log.info("RECEIVED | Topic={} | OrderId={} | Event={}", "payment.failed", event.orderId(), event);
		kafkaConsumerLogger.info("RECEIVED | Topic={} | OrderId={} | Event={}", "payment.failed", event.orderId(), event);

		processor.handlePaymentFailed(event);

		acknowledgment.acknowledge();
	}

}
