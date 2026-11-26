package enterprise.sharma.prateek.inventory.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import enterprise.sharma.prateek.common.events.InventoryReleaseRequestedEvent;
import enterprise.sharma.prateek.common.events.OrderCreatedEvent;

@Component
public class InventoryEventConsumer {

	private static final Logger log = LoggerFactory.getLogger(InventoryEventConsumer.class);

	private static final Logger kafkaConsumerLogger = LoggerFactory.getLogger("KAFKA_CONSUMER");

	private final InventoryEventProcessor processor;
	private final ObjectMapper objectMapper;

	public InventoryEventConsumer(InventoryEventProcessor processor, ObjectMapper objectMapper) {
		this.processor = processor;
		this.objectMapper = objectMapper;
	}

	@KafkaListener(topics = "order.created", groupId = "inventory-service-group")
	public void consumeOrderCreated(String payload, Acknowledgment acknowledgment) throws Exception {

		OrderCreatedEvent event = objectMapper.readValue(payload, OrderCreatedEvent.class);

		log.info("RECEIVED | Topic={} | OrderId={} | Event={}", "order.created", event.orderId(), event);
		kafkaConsumerLogger.info("RECEIVED | Topic={} | OrderId={} | Event={}", "order.created", event.orderId(),
				event);

		processor.processOrderCreated(event);

		// Acknowledge only after Oracle transaction commits.
		acknowledgment.acknowledge();

	}

	@KafkaListener(topics = "inventory.release.requested", groupId = "inventory-service-group")
	public void consumeInventoryRelease(String payload, Acknowledgment acknowledgment) throws Exception {

		InventoryReleaseRequestedEvent event = objectMapper.readValue(payload, InventoryReleaseRequestedEvent.class);

		log.info("RECEIVED | Topic={} | OrderId={} | Event={}", "inventory.release.requested", event.orderId(), event);
		kafkaConsumerLogger.info("RECEIVED | Topic={} | OrderId={} | Event={}", "inventory.release.requested", event.orderId(),
				event);

		processor.processInventoryRelease(event);

		acknowledgment.acknowledge();

	}

}
