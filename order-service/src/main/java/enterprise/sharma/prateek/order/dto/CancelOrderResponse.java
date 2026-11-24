package enterprise.sharma.prateek.order.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CancelOrderResponse(

		UUID orderId, String status, String message, LocalDateTime updatedAt

) {}
