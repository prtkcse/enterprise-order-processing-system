package enterprise.sharma.prateek.common.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(

		UUID eventId, UUID orderId, String customerId, List<OrderItemEvent> items, BigDecimal totalAmount,
		LocalDateTime createdAt

) {

	public record OrderItemEvent(

			String productId, Integer quantity, BigDecimal unitPrice

	) {
	}

}
