package enterprise.sharma.prateek.payment.service;

import java.math.BigDecimal;

public interface PaymentProcessor {

	PaymentProcessingResult processPayment(BigDecimal amount, String currency, String paymentMethod);

}
