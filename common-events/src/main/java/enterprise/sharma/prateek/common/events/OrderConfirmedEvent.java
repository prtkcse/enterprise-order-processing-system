package enterprise.sharma.prateek.common.events;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderConfirmedEvent(UUID eventId, UUID orderId, String customerId, LocalDateTime confirmedAt) {

}
