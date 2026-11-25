package enterprise.sharma.prateek.payment.dto;

import java.time.LocalDateTime;

public record ErrorResponse(String message, LocalDateTime timestamp) {

}
