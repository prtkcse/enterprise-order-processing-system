package enterprise.sharma.prateek.common.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record InventoryReservedEvent(

		UUID eventId, UUID orderId, BigDecimal totalAmount, LocalDateTime reservedAt

) {}
