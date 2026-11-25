package enterprise.sharma.prateek.common.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentCompletedEvent(
		
		UUID eventId,

		UUID paymentId,

		UUID orderId,

		BigDecimal amount,

		String currency,

		String transactionReference,

		LocalDateTime completedAt
		
		) {}
