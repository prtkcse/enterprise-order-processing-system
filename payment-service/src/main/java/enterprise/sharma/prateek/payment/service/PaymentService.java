package enterprise.sharma.prateek.payment.service;

import java.util.UUID;

import enterprise.sharma.prateek.payment.dto.InitiatePaymentRequest;
import enterprise.sharma.prateek.payment.dto.PaymentResponse;

public interface PaymentService {

	PaymentResponse initiatePayment(InitiatePaymentRequest request);

	PaymentResponse getPaymentByOrderId(UUID orderId);

}
