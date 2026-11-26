package enterprise.sharma.prateek.payment.kafka;

import java.math.BigDecimal;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import enterprise.sharma.prateek.common.events.InventoryReservedEvent;
import enterprise.sharma.prateek.payment.entity.Payment;
import enterprise.sharma.prateek.payment.repository.PaymentRepository;
import enterprise.sharma.prateek.payment.service.PaymentProcessingResult;
import enterprise.sharma.prateek.payment.service.PaymentProcessor;

@Component
public class PaymentEventProcessor {

	private static final Logger log = LoggerFactory.getLogger(PaymentEventProcessor.class);

	private static final Logger errorLogger = LoggerFactory.getLogger("ERROR_LOGGER");

	private final PaymentRepository paymentRepository;
	private final PaymentProcessor paymentProcessor;
	private final PaymentResultPublisher paymentResultPublisher;

	public PaymentEventProcessor(PaymentRepository paymentRepository, PaymentProcessor paymentProcessor,
			PaymentResultPublisher paymentResultPublisher) {

		this.paymentRepository = paymentRepository;
		this.paymentProcessor = paymentProcessor;
		this.paymentResultPublisher = paymentResultPublisher;
	}

	@Transactional
	public void processInventoryReserved(InventoryReservedEvent event) {		

		UUID orderId = event.orderId();		

		/*
		 * Idempotency: If payment already exists, do not process again.
		 */
		var existingPayment = paymentRepository.findByOrderId(orderId);

		if (existingPayment.isPresent()) {
			return;
		}

		BigDecimal amount = event.totalAmount();

		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("Payment amount must be greater than zero");
		}

		Payment payment = new Payment(orderId, "ORDER-" + orderId + "-PAYMENT", amount, "INR", "UPI");
		payment.markProcessing();
		Payment savedPayment = paymentRepository.save(payment);		

		/*
		 * Simulated gateway call.
		 */
		
		PaymentProcessingResult result = paymentProcessor.processPayment(savedPayment.getAmount(),
				savedPayment.getCurrency(), savedPayment.getPaymentMethod());

		if (result.success()) {
			
			savedPayment.markSuccess(result.transactionReference());			

			paymentResultPublisher.publishPaymentCompleted(savedPayment);			

		} else {
			
			log.error("Payment failed with the reason : "+result.failureReason());
			errorLogger.error("Payment failed with the reason : "+result.failureReason());

			savedPayment.markFailed(result.failureReason());

			paymentResultPublisher.publishPaymentFailed(savedPayment);
			log.error("Failed payment event published to outbox");
			errorLogger.error("Failed payment event published to outbox");
		}

	}
}
