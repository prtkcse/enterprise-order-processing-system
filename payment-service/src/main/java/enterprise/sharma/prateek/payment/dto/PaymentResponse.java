package enterprise.sharma.prateek.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import enterprise.sharma.prateek.payment.entity.PaymentStatus;

public record PaymentResponse(

		UUID paymentId,

		UUID orderId,

		BigDecimal amount,

		String currency,

		PaymentStatus status,

		String paymentMethod,

		String transactionReference,

		String failureReason,

		LocalDateTime createdAt,

		LocalDateTime updatedAt

) {

}
