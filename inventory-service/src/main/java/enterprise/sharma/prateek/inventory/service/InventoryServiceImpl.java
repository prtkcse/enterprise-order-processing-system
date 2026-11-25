package enterprise.sharma.prateek.inventory.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import enterprise.sharma.prateek.inventory.dto.CreateInventoryRequest;
import enterprise.sharma.prateek.inventory.dto.InventoryResponse;
import enterprise.sharma.prateek.inventory.dto.ReservationResponse;
import enterprise.sharma.prateek.inventory.dto.UpdateInventoryRequest;
import enterprise.sharma.prateek.inventory.entity.InventoryReservation;
import enterprise.sharma.prateek.inventory.entity.ProductInventory;
import enterprise.sharma.prateek.inventory.exception.ProductNotFoundException;
import enterprise.sharma.prateek.inventory.mapper.InventoryMapper;
import enterprise.sharma.prateek.inventory.repository.InventoryReservationRepository;
import enterprise.sharma.prateek.inventory.repository.ProductInventoryRepository;

@Service
public class InventoryServiceImpl implements InventoryService {

	private final ProductInventoryRepository inventoryRepository;
	private final InventoryReservationRepository reservationRepository;
	private final InventoryMapper inventoryMapper;

	public InventoryServiceImpl(ProductInventoryRepository inventoryRepository,
			InventoryReservationRepository reservationRepository, InventoryMapper inventoryMapper) {
		this.inventoryRepository = inventoryRepository;
		this.reservationRepository = reservationRepository;
		this.inventoryMapper = inventoryMapper;
	}

	@Override
	@Transactional
	public InventoryResponse createInventory(CreateInventoryRequest request) {

		if (inventoryRepository.findByProductId(request.productId()).isPresent()) {
			throw new IllegalArgumentException("Product already exists: " + request.productId());
		}

		ProductInventory inventory = new ProductInventory(request.productId(), request.productName(),
				request.initialStock());

		return inventoryMapper.toResponse(inventoryRepository.save(inventory));

	}

	@Override
	@Transactional(readOnly = true)
	public InventoryResponse getInventory(String productId) {

		ProductInventory inventory = inventoryRepository.findByProductId(productId)
				.orElseThrow(() -> new ProductNotFoundException(productId));

		return inventoryMapper.toResponse(inventory);

	}

	@Override
	@Transactional(readOnly = true)
	public Page<InventoryResponse> getAllInventory(int page, int size) {

		validatePage(page, size);

		return inventoryRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "productId")))
				.map(inventoryMapper::toResponse);
	}

	@Override
	@Transactional
	public InventoryResponse updateInventory(String productId, UpdateInventoryRequest request) {
		ProductInventory inventory = inventoryRepository.findByProductId(productId)
				.orElseThrow(() -> new ProductNotFoundException(productId));

		inventory.updateStock(request.totalStock());

		return inventoryMapper.toResponse(inventory);
	}

	@Override
	@Transactional(readOnly = true)
	public ReservationResponse getReservation(UUID orderId) {
		InventoryReservation reservation = reservationRepository.findByOrderId(orderId)
				.orElseThrow(() -> new IllegalArgumentException("Reservation not found for order: " + orderId));

		return new ReservationResponse(reservation.getId(), reservation.getOrderId(), reservation.getStatus().name(),
				reservation.getFailureReason() == null ? "Inventory reservation retrieved"
						: reservation.getFailureReason(),
				reservation.getCreatedAt());
	}

	private void validatePage(int page, int size) {

		if (page < 0) {
			throw new IllegalArgumentException("Page cannot be negative");
		}

		if (size < 1 || size > 100) {
			throw new IllegalArgumentException("Page size must be between 1 and 100");
		}
	}

}
