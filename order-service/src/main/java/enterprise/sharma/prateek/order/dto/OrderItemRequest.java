package enterprise.sharma.prateek.order.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(

		@NotBlank(message = "Product ID is required") 
		String productId,

		@NotNull @Min(value = 1, message = "Quantity must be at least 1") 
		Integer quantity,

		@NotNull @DecimalMin(value = "0.01") @Digits(integer = 10, fraction = 2) 
		BigDecimal unitPrice

) {}
