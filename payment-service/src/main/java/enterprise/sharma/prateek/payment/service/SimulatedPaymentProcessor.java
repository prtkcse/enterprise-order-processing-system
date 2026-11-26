package enterprise.sharma.prateek.payment.service;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import enterprise.sharma.prateek.payment.kafka.PaymentEventProcessor;

@Component
public class SimulatedPaymentProcessor implements PaymentProcessor {
	
	private static final Logger log = LoggerFactory.getLogger(SimulatedPaymentProcessor.class);

	@Value("${payment.processing.success-rate:90}")
	private int successRate;

	@Value("${payment.processing.delay-ms:1000}")
	private long processingDelay;

	@Override
	public PaymentProcessingResult processPayment(BigDecimal amount, String currency, String paymentMethod) {

		try {
			Thread.sleep(processingDelay);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();

			return PaymentProcessingResult.failure("Payment processing interrupted");
		}

		int randomNumber = ThreadLocalRandom.current().nextInt(1, 101);

		if (randomNumber <= successRate) {

			String transactionReference = "TXN-" + UUID.randomUUID();
			
			log.info("Generated transaction reference "+ transactionReference);

			return PaymentProcessingResult.success(transactionReference);
		}

		return PaymentProcessingResult.failure("Simulated payment gateway declined the transaction");

	}

}
