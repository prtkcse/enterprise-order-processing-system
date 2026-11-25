package enterprise.sharma.prateek.payment.mapper;

import org.springframework.stereotype.Component;

import enterprise.sharma.prateek.payment.dto.PaymentResponse;
import enterprise.sharma.prateek.payment.entity.Payment;

@Component
public class PaymentMapper {

	public PaymentResponse toResponse(Payment payment) {

		return new PaymentResponse(payment.getId(), payment.getOrderId(), payment.getAmount(), payment.getCurrency(),
				payment.getStatus(), payment.getPaymentMethod(), payment.getTransactionReference(),
				payment.getFailureReason(), payment.getCreatedAt(), payment.getUpdatedAt());
	}

}
