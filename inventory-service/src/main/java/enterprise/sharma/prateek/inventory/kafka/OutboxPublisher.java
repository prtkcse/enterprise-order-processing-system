package enterprise.sharma.prateek.inventory.kafka;

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

import enterprise.sharma.prateek.inventory.entity.OutboxEvent;
import enterprise.sharma.prateek.inventory.repository.OutboxEventRepository;

@Component
public class OutboxPublisher {

	private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

	private final OutboxEventRepository outboxRepository;
	private final KafkaTemplate<String, String> kafkaTemplate;

	@Value("${app.outbox.publisher.enabled:true}")
	private boolean enabled;

	@Value("${app.outbox.publisher.batch-size:50}")
	private int batchSize;

	@Value("${app.outbox.publisher.claim-timeout-minutes:5}")
	private int claimTimeoutMinutes;

	public OutboxPublisher(OutboxEventRepository outboxRepository, KafkaTemplate<String, String> kafkaTemplate) {
		this.outboxRepository = outboxRepository;
		this.kafkaTemplate = kafkaTemplate;
	}

	@Scheduled(fixedDelayString = "${app.outbox.publisher.fixed-delay-ms:3000}")
	public void publishPendingEvents() {

		if (!enabled) {
			return;
		}

		recoverStaleEvents();

		List<OutboxEvent> events = outboxRepository.findEligiblePendingEvents(PageRequest.of(0, batchSize));

		for (OutboxEvent event : events) {
			publishSingleEvent(event);
		}

	}

	private void recoverStaleEvents() {

		int recovered = outboxRepository.recoverStaleEvents(claimTimeoutMinutes);

		if (recovered > 0) {
			log.warn("Recovered {} stale inventory outbox events", recovered);
		}

	}

	private void publishSingleEvent(OutboxEvent event) {
		UUID eventId = event.getId();

		try {

			int claimed = outboxRepository.claimEvent(eventId);

			if (claimed == 0) {
				return;
			}

			kafkaTemplate.send(event.getTopic(), event.getEventKey(), event.getPayload()).get(10, TimeUnit.SECONDS);

			outboxRepository.markPublished(eventId);

			log.info("Inventory event published. eventId={}, topic={}", eventId, event.getTopic());

		} catch (Exception ex) {

			log.error("Inventory event publishing failed. eventId={}", eventId, ex);

			String error = ex.getMessage();

			if (error == null) {
				error = "Unknown Kafka publishing error";
			}

			if (error.length() > 1900) {
				error = error.substring(0, 1900);
			}

			outboxRepository.markFailed(eventId, error);
		}
	}

}
