package enterprise.sharma.prateek.payment.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import enterprise.sharma.prateek.common.events.InventoryReservedEvent;

@Component
public class PaymentEventConsumer {

	private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);

	private static final Logger kafkaConsumerLogger = LoggerFactory.getLogger("KAFKA_CONSUMER");

	private final PaymentEventProcessor paymentEventProcessor;
	private final ObjectMapper objectMapper;

	public PaymentEventConsumer(PaymentEventProcessor paymentEventProcessor, ObjectMapper objectMapper) {

		this.paymentEventProcessor = paymentEventProcessor;
		this.objectMapper = objectMapper;
	}

	@KafkaListener(topics = "inventory.reserved", groupId = "payment-service-group")
	public void consumeInventoryReserved(String message, Acknowledgment acknowledgment) {
		
		InventoryReservedEvent event = null;

		try {

			event = objectMapper.readValue(message, InventoryReservedEvent.class);

			log.info("RECEIVED | Topic={} | OrderId={} | Event={}", "inventory.reserved", event.orderId(), event);

			kafkaConsumerLogger.info("RECEIVED | Topic={} | OrderId={} | Event={}", "inventory.reserved",
					event.orderId(), event);

			paymentEventProcessor.processInventoryReserved(event);

			/*
			 * Acknowledge only after successful processing.
			 */
			acknowledgment.acknowledge();

		} catch (Exception ex) {

			kafkaConsumerLogger.error("PROCESSING FAILED | Topic={} | OrderId={}", "inventory.reserved", event.orderId(), ex);

			log.error("Payment event processing failed. OrderId={}", event.orderId(), ex);

			/*
			 * Do not acknowledge. Kafka may redeliver the message.
			 */
			throw new RuntimeException("Failed to process inventory.reserved event", ex);
		}
	}

}
