package enterprise.sharma.prateek.payment.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InitiatePaymentRequest(

		@NotNull UUID orderId,

		@NotNull @DecimalMin(value = "0.01") BigDecimal amount,

		@NotBlank String currency,

		@NotBlank String paymentMethod,

		@NotBlank String idempotencyKey

) {

}
