package enterprise.sharma.prateek.common.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentCompletedEvent(UUID eventId, UUID orderId, UUID paymentId, BigDecimal amount,
		LocalDateTime completedAt) {

}
