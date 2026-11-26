package enterprise.sharma.prateek.order.kafka;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import enterprise.sharma.prateek.order.entity.OutboxEvent;
import enterprise.sharma.prateek.order.repository.OutboxEventRepository;

@Component
public class OutboxPublisher {

	private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);
	private static final Logger kafkaProducerLogger = LoggerFactory.getLogger("KAFKA_PRODUCER");

	private final OutboxEventRepository outboxRepository;
	private final KafkaTemplate<String, String> kafkaTemplate;

	@Value("${app.outbox.publisher.enabled:true}")
	private boolean enabled;

	@Value("${app.outbox.publisher.batch-size:50}")
	private int batchSize;

	public OutboxPublisher(OutboxEventRepository outboxRepository, KafkaTemplate<String, String> kafkaTemplate) {
		this.outboxRepository = outboxRepository;
		this.kafkaTemplate = kafkaTemplate;
	}

	@Scheduled(fixedDelayString = "${app.outbox.publisher.fixed-delay-ms:3000}")
	public void publishPendingEvents() {

		if (!enabled) {
			return;
		}

		List<OutboxEvent> events = outboxRepository.findByStatusOrderByCreatedAtAsc("PENDING",
				PageRequest.of(0, batchSize));

		for (OutboxEvent event : events) {
			publishSingleEvent(event);
		}

	}

	private void publishSingleEvent(OutboxEvent event) {

		UUID eventId = event.getId();

		try {

			// Claim the event. Only one publisher should claim it.
			int claimed = outboxRepository.claimEvent(eventId);

			if (claimed == 0) {
				return;
			}

			// Wait for Kafka acknowledgment.
			kafkaTemplate.send(event.getTopic(), event.getEventKey(), event.getPayload()).get(10, TimeUnit.SECONDS);

			outboxRepository.markPublished(eventId);

			log.info("KAFKA PUBLISH SUCCESS | EventId={} | Topic={} | Key={} | Payload={}", event.getId(),
					event.getTopic(), event.getEventKey(), event.getPayload());

			kafkaProducerLogger.info("KAFKA PUBLISH SUCCESS | EventId={} | Topic={} | Key={} | Payload={}",
					event.getId(), event.getTopic(), event.getEventKey(), event.getPayload());

		} catch (Exception ex) {

			log.error("KAFKA PUBLISH FAILURE | EventId={} | Topic={} | Key={} | Payload={}", event.getId(),
					event.getTopic(), event.getEventKey(), event.getPayload(), ex);

			kafkaProducerLogger.error("KAFKA PUBLISH FAILURE | EventId={} | Topic={} | Key={} | Payload={}",
					event.getId(), event.getTopic(), event.getEventKey(), event.getPayload(), ex);

			outboxRepository.markFailed(eventId, truncate(ex.getMessage()));
		}

	}

	private String truncate(String message) {

		if (message == null) {
			return "Unknown publishing error";
		}

		return message.length() > 1900 ? message.substring(0, 1900) : message;
	}

}
