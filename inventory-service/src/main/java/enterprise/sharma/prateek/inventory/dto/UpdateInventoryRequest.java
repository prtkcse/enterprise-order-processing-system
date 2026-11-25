package enterprise.sharma.prateek.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateInventoryRequest(

		@NotNull 
		@Min(value = 0) 
		Integer totalStock

) {

}
