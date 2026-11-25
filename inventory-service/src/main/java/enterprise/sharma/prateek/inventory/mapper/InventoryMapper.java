package enterprise.sharma.prateek.inventory.mapper;

import org.springframework.stereotype.Component;

import enterprise.sharma.prateek.inventory.dto.InventoryResponse;
import enterprise.sharma.prateek.inventory.entity.ProductInventory;

@Component
public class InventoryMapper {

	public InventoryResponse toResponse(ProductInventory inventory) {

		return new InventoryResponse(inventory.getProductId(), inventory.getProductName(), inventory.getTotalStock(),
				inventory.getAvailableStock(), inventory.getReservedStock(), inventory.getCreatedAt(),
				inventory.getUpdatedAt());
	}

}
