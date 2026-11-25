package enterprise.sharma.prateek.payment.kafka;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import enterprise.sharma.prateek.common.events.PaymentCompletedEvent;
import enterprise.sharma.prateek.common.events.PaymentFailedEvent;
import enterprise.sharma.prateek.payment.entity.OutboxEvent;
import enterprise.sharma.prateek.payment.entity.Payment;
import enterprise.sharma.prateek.payment.repository.OutboxEventRepository;

@Component
public class PaymentResultPublisher {

	private final OutboxEventRepository outboxEventRepository;
	private final ObjectMapper objectMapper;

	public PaymentResultPublisher(OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {

		this.outboxEventRepository = outboxEventRepository;
		this.objectMapper = objectMapper;
	}

	public void publishPaymentCompleted(Payment payment) {

		PaymentCompletedEvent event = new PaymentCompletedEvent(UUID.randomUUID(), payment.getId(),
				payment.getOrderId(), payment.getAmount(), payment.getCurrency(), payment.getTransactionReference(),
				LocalDateTime.now());

		saveEvent(payment.getOrderId(), "PAYMENT_COMPLETED", "payment.completed", event);
	}

	public void publishPaymentFailed(Payment payment) {

		PaymentFailedEvent event = new PaymentFailedEvent(UUID.randomUUID(), payment.getId(), payment.getOrderId(),
				payment.getAmount(), payment.getCurrency(), payment.getFailureReason(), LocalDateTime.now());

		saveEvent(payment.getOrderId(), "PAYMENT_FAILED", "payment.failed", event);
	}

	private void saveEvent(UUID aggregateId, String eventType, String topic, Object event) {
		try {

			String payload = objectMapper.writeValueAsString(event);

			OutboxEvent outboxEvent = new OutboxEvent(aggregateId, eventType, topic, aggregateId.toString(), payload);

			outboxEventRepository.save(outboxEvent);

		} catch (JsonProcessingException ex) {

			throw new IllegalStateException("Unable to serialize payment event", ex);
		}

	}

}
