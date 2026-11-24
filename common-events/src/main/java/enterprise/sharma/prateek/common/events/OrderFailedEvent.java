package enterprise.sharma.prateek.common.events;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderFailedEvent(UUID eventId, UUID orderId, String customerId, String reason, LocalDateTime failedAt) {

}
