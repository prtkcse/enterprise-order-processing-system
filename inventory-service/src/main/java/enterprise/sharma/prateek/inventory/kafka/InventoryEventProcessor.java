package enterprise.sharma.prateek.inventory.kafka;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import enterprise.sharma.prateek.common.events.InventoryReleaseRequestedEvent;
import enterprise.sharma.prateek.common.events.InventoryReservationFailedEvent;
import enterprise.sharma.prateek.common.events.InventoryReservedEvent;
import enterprise.sharma.prateek.common.events.OrderCreatedEvent;
import enterprise.sharma.prateek.inventory.entity.InventoryReservation;
import enterprise.sharma.prateek.inventory.entity.OutboxEvent;
import enterprise.sharma.prateek.inventory.entity.ProductInventory;
import enterprise.sharma.prateek.inventory.entity.ReservationItem;
import enterprise.sharma.prateek.inventory.entity.ReservationStatus;
import enterprise.sharma.prateek.inventory.repository.InventoryReservationRepository;
import enterprise.sharma.prateek.inventory.repository.OutboxEventRepository;
import enterprise.sharma.prateek.inventory.repository.ProductInventoryRepository;

@Service
public class InventoryEventProcessor {

	private final ProductInventoryRepository inventoryRepository;
	private final InventoryReservationRepository reservationRepository;
	private final OutboxEventRepository outboxRepository;
	private final ObjectMapper objectMapper;

	public InventoryEventProcessor(ProductInventoryRepository inventoryRepository,
			InventoryReservationRepository reservationRepository, OutboxEventRepository outboxRepository,
			ObjectMapper objectMapper) {
		this.inventoryRepository = inventoryRepository;
		this.reservationRepository = reservationRepository;
		this.outboxRepository = outboxRepository;
		this.objectMapper = objectMapper;
	}

	@Transactional
	public void processOrderCreated(OrderCreatedEvent event) {

		UUID orderId = event.orderId();
		BigDecimal totalAmount = event.totalAmount();

		// Idempotency: do not process the same order twice.
		Optional<InventoryReservation> existing = reservationRepository.findByOrderId(orderId);

		if (existing.isPresent()) {
			return;
		}

		if (event.items() == null || event.items().isEmpty()) {
			saveFailedReservation(orderId, "Order contains no inventory items");
			return;
		}

		// Combine duplicate product IDs and sort them.
		Map<String, Integer> requestedItems = new TreeMap<>();

		for (OrderCreatedEvent.OrderItemEvent item : event.items()) {

			if (item.productId() == null || item.productId().isBlank() || item.quantity() == null
					|| item.quantity() <= 0) {

				saveFailedReservation(orderId, "Invalid product or quantity in order");
				return;
			}

			requestedItems.merge(item.productId(), item.quantity(), Math::addExact);
		}

		List<String> productIds = new ArrayList<>(requestedItems.keySet());

		// Acquire Oracle pessimistic write locks.
		List<ProductInventory> inventories = inventoryRepository.findAllForUpdate(productIds);

		Map<String, ProductInventory> inventoryMap = new HashMap<>();

		for (ProductInventory inventory : inventories) {
			inventoryMap.put(inventory.getProductId(), inventory);
		}

		// Validate ALL products before modifying any stock.
		for (Map.Entry<String, Integer> item : requestedItems.entrySet()) {

			ProductInventory inventory = inventoryMap.get(item.getKey());

			if (inventory == null) {
				saveFailedReservation(orderId, "Product not found: " + item.getKey());
				return;
			}

			if (inventory.getAvailableStock() < item.getValue()) {
				saveFailedReservation(orderId, "Insufficient stock for product: " + item.getKey() + ". Available: "
						+ inventory.getAvailableStock() + ", Requested: " + item.getValue());

				return;
			}
		}

		// All validation passed. Reserve all items.
		InventoryReservation reservation = new InventoryReservation(orderId);

		for (Map.Entry<String, Integer> item : requestedItems.entrySet()) {

			ProductInventory inventory = inventoryMap.get(item.getKey());

			inventory.reserveStock(item.getValue());

			reservation.addItem(new ReservationItem(item.getKey(), item.getValue()));

		}

		reservationRepository.save(reservation);

		InventoryReservedEvent reservedEvent = new InventoryReservedEvent(UUID.randomUUID(), orderId,
				totalAmount, LocalDateTime.now());

		saveOutbox(orderId, "InventoryReserved", "inventory.reserved", orderId.toString(), reservedEvent);
	}

	@Transactional
	public void processInventoryRelease(InventoryReleaseRequestedEvent event) {

		UUID orderId = event.orderId();

		InventoryReservation reservation = reservationRepository.findByOrderId(orderId).orElse(null);

		// A release is idempotent.
		if (reservation == null || reservation.getStatus() == ReservationStatus.RELEASED
				|| reservation.getStatus() == ReservationStatus.FAILED) {
			return;
		}

		if (reservation.getStatus() != ReservationStatus.RESERVED) {
			return;
		}

		List<String> productIds = reservation.getItems().stream().map(ReservationItem::getProductId).distinct().sorted()
				.toList();

		List<ProductInventory> inventories = inventoryRepository.findAllForUpdate(productIds);

		Map<String, ProductInventory> inventoryMap = new HashMap<>();

		for (ProductInventory inventory : inventories) {
			inventoryMap.put(inventory.getProductId(), inventory);
		}

		// Validate before modifying any stock.
		for (ReservationItem item : reservation.getItems()) {

			ProductInventory inventory = inventoryMap.get(item.getProductId());

			if (inventory == null || inventory.getReservedStock() < item.getQuantity()) {

				throw new IllegalStateException("Cannot release inventory for product: " + item.getProductId());
			}

		}

		// Release all reserved quantities.
		for (ReservationItem item : reservation.getItems()) {

			ProductInventory inventory = inventoryMap.get(item.getProductId());

			inventory.releaseStock(item.getQuantity());
		}

		reservation.markReleased();

		saveOutbox(orderId, "InventoryReleased", "inventory.released", orderId.toString(),
				Map.of("eventId", UUID.randomUUID().toString(), "orderId", orderId.toString(), "releasedAt",
						LocalDateTime.now().toString()));

	}

	private void saveFailedReservation(UUID orderId, String reason) {

		InventoryReservation reservation = new InventoryReservation(orderId);

		reservation.markFailed(reason);

		reservationRepository.save(reservation);

		InventoryReservationFailedEvent event = new InventoryReservationFailedEvent(UUID.randomUUID(), orderId, reason,
				LocalDateTime.now());

		saveOutbox(orderId, "InventoryReservationFailed", "inventory.failed", orderId.toString(), event);

	}

	private void saveOutbox(UUID aggregateId, String eventType, String topic, String eventKey, Object payload) {
		try {

			String json = objectMapper.writeValueAsString(payload);

			outboxRepository.save(new OutboxEvent(aggregateId, eventType, topic, eventKey, json));

		} catch (Exception ex) {

			throw new IllegalStateException("Failed to serialize inventory event", ex);
		}
	}

}
