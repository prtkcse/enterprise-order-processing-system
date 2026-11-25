package enterprise.sharma.prateek.inventory.service;

import java.util.UUID;

import org.springframework.data.domain.Page;

import enterprise.sharma.prateek.inventory.dto.CreateInventoryRequest;
import enterprise.sharma.prateek.inventory.dto.InventoryResponse;
import enterprise.sharma.prateek.inventory.dto.ReservationResponse;
import enterprise.sharma.prateek.inventory.dto.UpdateInventoryRequest;

public interface InventoryService {

	InventoryResponse createInventory(CreateInventoryRequest request);

	InventoryResponse getInventory(String productId);

	Page<InventoryResponse> getAllInventory(int page, int size);

	InventoryResponse updateInventory(String productId, UpdateInventoryRequest request);
	
	ReservationResponse getReservation(UUID orderId);

}
