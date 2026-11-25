package enterprise.sharma.prateek.inventory.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import enterprise.sharma.prateek.common.events.InventoryReleaseRequestedEvent;
import enterprise.sharma.prateek.common.events.OrderCreatedEvent;

@Component
public class InventoryEventConsumer {

	private final InventoryEventProcessor processor;
	private final ObjectMapper objectMapper;

	public InventoryEventConsumer(InventoryEventProcessor processor, ObjectMapper objectMapper) {
		this.processor = processor;
		this.objectMapper = objectMapper;
	}

	@KafkaListener(topics = "order.created", groupId = "inventory-service-group")
	public void consumeOrderCreated(String payload, Acknowledgment acknowledgment) throws Exception {

		OrderCreatedEvent event = objectMapper.readValue(payload, OrderCreatedEvent.class);

		processor.processOrderCreated(event);

		// Acknowledge only after Oracle transaction commits.
		acknowledgment.acknowledge();

	}

	@KafkaListener(topics = "inventory.release.requested", groupId = "inventory-service-group")
	public void consumeInventoryRelease(String payload, Acknowledgment acknowledgment) throws Exception {

		InventoryReleaseRequestedEvent event = objectMapper.readValue(payload, InventoryReleaseRequestedEvent.class);

		processor.processInventoryRelease(event);

		acknowledgment.acknowledge();

	}

}
