package enterprise.sharma.prateek.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateInventoryRequest(

		@NotBlank(message = "Product ID is required") 
		String productId,

		@NotBlank(message = "Product name is required") 
		String productName,

		@NotNull(message = "Initial stock is required") 
		@Min(value = 0, message = "Initial stock cannot be negative") 
		Integer initialStock
		
	) {}
