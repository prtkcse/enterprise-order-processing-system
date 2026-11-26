package enterprise.sharma.prateek.payment.kafka;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import enterprise.sharma.prateek.payment.entity.OutboxEvent;
import enterprise.sharma.prateek.payment.repository.OutboxEventRepository;

@Component
public class OutboxPublisher {

	private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

	private static final Logger kafkaProducerLogger = LoggerFactory.getLogger("KAFKA_PRODUCER");

	private final OutboxEventRepository outboxEventRepository;
	private final KafkaTemplate<String, String> kafkaTemplate;

	@Value("${payment.outbox.enabled:true}")
	private boolean enabled;

	@Value("${payment.outbox.batch-size:50}")
	private int batchSize;

	@Value("${payment.outbox.retry.claim-timeout-minutes:5}")
	private int claimTimeoutMinutes;

	public OutboxPublisher(OutboxEventRepository outboxEventRepository, KafkaTemplate<String, String> kafkaTemplate) {

		this.outboxEventRepository = outboxEventRepository;
		this.kafkaTemplate = kafkaTemplate;
	}

	@Scheduled(fixedDelayString = "${payment.outbox.publish-delay-ms:3000}")
	public void publishPendingEvents() {

		if (!enabled) {
			return;
		}

		recoverStaleEvents();

		var events = outboxEventRepository.findEligibleEvents(PageRequest.of(0, batchSize));

		for (OutboxEvent event : events) {

			int claimed = outboxEventRepository.claimEvent(event.getId());

			if (claimed == 0) {
				continue;
			}

			publishEvent(event);
		}

	}

	private void publishEvent(OutboxEvent event) {

		try {

			kafkaTemplate.send(event.getTopic(), event.getEventKey(), event.getPayload()).get(10, TimeUnit.SECONDS);

			log.info("KAFKA PUBLISH SUCCESS | EventId={} | Topic={} | Key={} | Payload={}", event.getId(),
					event.getTopic(), event.getEventKey(), event.getPayload());

			kafkaProducerLogger.info("KAFKA PUBLISH SUCCESS | EventId={} | Topic={} | Key={} | Payload={}",
					event.getId(), event.getTopic(), event.getEventKey(), event.getPayload());

			outboxEventRepository.markPublished(event.getId());

		} catch (Exception ex) {

			String error = ex.getMessage();

			if (error != null && error.length() > 1900) {
				error = error.substring(0, 1900);
			}

			log.error("KAFKA PUBLISH FAILURE | EventId={} | Topic={} | Key={} | Payload={}",
					event.getId(), event.getTopic(), event.getEventKey(), event.getPayload(), ex);

			kafkaProducerLogger.error("KAFKA PUBLISH FAILURE | EventId={} | Topic={} | Key={} | Payload={}",
					event.getId(), event.getTopic(), event.getEventKey(), event.getPayload(), ex);

			outboxEventRepository.markFailed(event.getId(), error == null ? "Unknown Kafka publishing error" : error);
		}
	}

	@Transactional
	public void recoverStaleEvents() {

		LocalDateTime cutoff = LocalDateTime.now().minusMinutes(claimTimeoutMinutes);

		outboxEventRepository.recoverStaleEvents(cutoff);
	}

}
