package enterprise.sharma.prateek.inventory.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import enterprise.sharma.prateek.inventory.dto.CreateInventoryRequest;
import enterprise.sharma.prateek.inventory.dto.InventoryResponse;
import enterprise.sharma.prateek.inventory.dto.ReservationResponse;
import enterprise.sharma.prateek.inventory.dto.UpdateInventoryRequest;
import enterprise.sharma.prateek.inventory.service.InventoryService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

	private final InventoryService inventoryService;

	public InventoryController(InventoryService inventoryService) {
		this.inventoryService = inventoryService;
	}

	@PostMapping
	public ResponseEntity<InventoryResponse> createInventory(@Valid @RequestBody CreateInventoryRequest request) {

		InventoryResponse response = inventoryService.createInventory(request);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/{productId}")
	public ResponseEntity<InventoryResponse> getInventory(@PathVariable String productId) {

		return ResponseEntity.ok(inventoryService.getInventory(productId));
	}

	@GetMapping
	public ResponseEntity<Page<InventoryResponse>> getAllInventory(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {

		return ResponseEntity.ok(inventoryService.getAllInventory(page, size));
	}

	@PutMapping("/{productId}")
	public ResponseEntity<InventoryResponse> updateInventory(@PathVariable String productId,
			@Valid @RequestBody UpdateInventoryRequest request) {

		return ResponseEntity.ok(inventoryService.updateInventory(productId, request));
	}

	@GetMapping("/reservations/{orderId}")
	public ResponseEntity<ReservationResponse> getReservation(@PathVariable UUID orderId) {

		return ResponseEntity.ok(inventoryService.getReservation(orderId));
	}

}
