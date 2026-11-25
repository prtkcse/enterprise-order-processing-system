package enterprise.sharma.prateek.common.events;

import java.time.LocalDateTime;
import java.util.UUID;

public record InventoryReleaseRequestedEvent(

		UUID eventId,

		UUID orderId,

		String reason,

		LocalDateTime requestedAt) {
}
