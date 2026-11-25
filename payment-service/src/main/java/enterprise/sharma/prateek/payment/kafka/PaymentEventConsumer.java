package enterprise.sharma.prateek.payment.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import enterprise.sharma.prateek.common.events.InventoryReservedEvent;

@Component
public class PaymentEventConsumer {

	private final PaymentEventProcessor paymentEventProcessor;
	private final ObjectMapper objectMapper;

	public PaymentEventConsumer(PaymentEventProcessor paymentEventProcessor, ObjectMapper objectMapper) {

		this.paymentEventProcessor = paymentEventProcessor;
		this.objectMapper = objectMapper;
	}

	@KafkaListener(topics = "inventory.reserved", groupId = "payment-service-group")
	public void consumeInventoryReserved(String message, Acknowledgment acknowledgment) {

		try {

			InventoryReservedEvent event = objectMapper.readValue(message, InventoryReservedEvent.class);

			paymentEventProcessor.processInventoryReserved(event);

			/*
			 * Acknowledge only after successful processing.
			 */
			acknowledgment.acknowledge();

		} catch (Exception ex) {

			/*
			 * Do not acknowledge. Kafka may redeliver the message.
			 */
			throw new RuntimeException("Failed to process inventory.reserved event", ex);
		}
	}

}
