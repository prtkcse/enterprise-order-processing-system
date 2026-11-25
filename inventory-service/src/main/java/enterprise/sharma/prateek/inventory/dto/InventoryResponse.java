package enterprise.sharma.prateek.inventory.dto;

import java.time.LocalDateTime;

public record InventoryResponse(

		String productId,

		String productName,

		Integer totalStock,

		Integer availableStock,

		Integer reservedStock,

		LocalDateTime createdAt,

		LocalDateTime updatedAt

) {

}
