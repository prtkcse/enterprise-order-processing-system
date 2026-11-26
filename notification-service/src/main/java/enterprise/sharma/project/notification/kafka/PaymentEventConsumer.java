package enterprise.sharma.project.notification.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import enterprise.sharma.prateek.common.events.PaymentCompletedEvent;
import enterprise.sharma.prateek.common.events.PaymentFailedEvent;

@Component
public class PaymentEventConsumer {

	private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);
	private static final Logger kafkaConsumerLogger = LoggerFactory.getLogger("KAFKA_CONSUMER");

	private final ObjectMapper objectMapper;
	private final NotificationEventProcessor eventProcessor;

	public PaymentEventConsumer(ObjectMapper objectMapper, NotificationEventProcessor eventProcessor) {
		this.objectMapper = objectMapper;
		this.eventProcessor = eventProcessor;
	}

	@KafkaListener(topics = "${app.kafka.topic.payment-completed}", groupId = "${spring.kafka.consumer.group-id}")
	public void consumePaymentCompleted(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {

		PaymentCompletedEvent event = null;

		try {
			event = objectMapper.readValue(record.value(), PaymentCompletedEvent.class);

			log.info("RECEIVED | Topic={} | OrderId={} | Event={}", record.topic(), event.orderId(), event);

			kafkaConsumerLogger.info("RECEIVED | Topic={} | OrderId={} | Event={}", record.topic(), event.orderId(),
					event);

			eventProcessor.processPaymentCompleted(event);

			acknowledgment.acknowledge();

		} catch (Exception exception) {

			log.error("Exception | Topic={} | OrderId={} | Event={} | Exception={}", record.topic(), event.orderId(),
					event, exception.getMessage());

			kafkaConsumerLogger.error("Exception | Topic={} | OrderId={} | Event={} | Exception={}", record.topic(),
					event.orderId(), event, exception.getMessage());

			throw new IllegalStateException("Payment completed notification processing failed", exception);
		}

	}

	@KafkaListener(topics = "${app.kafka.topic.payment-failed}", groupId = "${spring.kafka.consumer.group-id}")
	public void consumePaymentFailed(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {

		PaymentFailedEvent event = null;

		try {
			event = objectMapper.readValue(record.value(), PaymentFailedEvent.class);

			log.info("RECEIVED | Topic={} | OrderId={} | Event={}", record.topic(), event.orderId(), event);

			kafkaConsumerLogger.info("RECEIVED | Topic={} | OrderId={} | Event={}", record.topic(), event.orderId(),
					event);

			eventProcessor.processPaymentFailed(event);

			acknowledgment.acknowledge();

		} catch (Exception exception) {

			log.error("Exception | Topic={} | OrderId={} | Event={} | Exception={}", record.topic(), event.orderId(),
					event, exception.getMessage());

			kafkaConsumerLogger.error("Exception | Topic={} | OrderId={} | Event={} | Exception={}", record.topic(),
					event.orderId(), event, exception.getMessage());

			throw new IllegalStateException("Payment failed notification processing failed", exception);
		}
	}

}
