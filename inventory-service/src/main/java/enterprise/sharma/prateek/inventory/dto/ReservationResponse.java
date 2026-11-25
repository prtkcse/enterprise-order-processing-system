package enterprise.sharma.prateek.inventory.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationResponse(

		UUID reservationId,

		UUID orderId,

		String status,

		String message,

		LocalDateTime createdAt

) {

}
