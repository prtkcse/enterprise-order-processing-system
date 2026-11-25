package com.enterprise.inventory.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import enterprise.sharma.prateek.common.events.OrderCreatedEvent;
import enterprise.sharma.prateek.inventory.entity.InventoryReservation;
import enterprise.sharma.prateek.inventory.entity.ProductInventory;
import enterprise.sharma.prateek.inventory.kafka.InventoryEventProcessor;
import enterprise.sharma.prateek.inventory.repository.InventoryReservationRepository;
import enterprise.sharma.prateek.inventory.repository.OutboxEventRepository;
import enterprise.sharma.prateek.inventory.repository.ProductInventoryRepository;

public class InventoryEventProcessorTest {

	private ProductInventoryRepository inventoryRepository;
	private InventoryReservationRepository reservationRepository;
	private OutboxEventRepository outboxRepository;

	private InventoryEventProcessor processor;

	@BeforeEach
	void setUp() {

		inventoryRepository = mock(ProductInventoryRepository.class);

		reservationRepository = mock(InventoryReservationRepository.class);

		outboxRepository = mock(OutboxEventRepository.class);

		processor = new InventoryEventProcessor(inventoryRepository, reservationRepository, outboxRepository,
				new ObjectMapper());

		when(reservationRepository.findByOrderId(any(UUID.class))).thenReturn(Optional.empty());
	}

	@Test
	void shouldReserveInventorySuccessfully() {

		UUID orderId = UUID.randomUUID();

		ProductInventory laptop = new ProductInventory("LAPTOP-001", "Laptop", 10);

		ProductInventory mouse = new ProductInventory("MOUSE-001", "Mouse", 100);

		when(inventoryRepository.findAllForUpdate(anyList())).thenReturn(List.of(laptop, mouse));

		OrderCreatedEvent event = new OrderCreatedEvent(UUID.randomUUID(), orderId, "CUST-1001",
				List.of(new OrderCreatedEvent.OrderItemEvent("LAPTOP-001", 2, new BigDecimal("75000")),
						new OrderCreatedEvent.OrderItemEvent("MOUSE-001", 1, new BigDecimal("1500"))),
				new BigDecimal("151500"), java.time.LocalDateTime.now());

		processor.processOrderCreated(event);

		assertEquals(8, laptop.getAvailableStock());
		assertEquals(2, laptop.getReservedStock());

		assertEquals(99, mouse.getAvailableStock());
		assertEquals(1, mouse.getReservedStock());

		verify(reservationRepository, times(1)).save(any());

		verify(outboxRepository, times(1)).save(any());
	}

	@Test
	void shouldNotReserveAnyStockWhenOneProductHasInsufficientStock() {

		UUID orderId = UUID.randomUUID();

		ProductInventory laptop = new ProductInventory("LAPTOP-001", "Laptop", 10);

		ProductInventory mouse = new ProductInventory("MOUSE-001", "Mouse", 1);

		when(inventoryRepository.findAllForUpdate(anyList())).thenReturn(List.of(laptop, mouse));

		OrderCreatedEvent event = new OrderCreatedEvent(UUID.randomUUID(), orderId, "CUST-1001",
				List.of(new OrderCreatedEvent.OrderItemEvent("LAPTOP-001", 2, new BigDecimal("75000")),
						new OrderCreatedEvent.OrderItemEvent("MOUSE-001", 5, new BigDecimal("1500"))),
				new BigDecimal("157500"), java.time.LocalDateTime.now());

		processor.processOrderCreated(event);

		// No stock should be deducted.
		assertEquals(10, laptop.getAvailableStock());
		assertEquals(0, laptop.getReservedStock());

		assertEquals(1, mouse.getAvailableStock());
		assertEquals(0, mouse.getReservedStock());

		verify(reservationRepository, times(1)).save(any());

		verify(outboxRepository, times(1)).save(any());
	}

	@Test
	void shouldNotProcessDuplicateOrder() {

		UUID orderId = UUID.randomUUID();

		when(reservationRepository.findByOrderId(orderId))
				.thenReturn(Optional.of(new InventoryReservation(orderId)));

		OrderCreatedEvent event = new OrderCreatedEvent(UUID.randomUUID(), orderId, "CUST-1001",
				List.of(new OrderCreatedEvent.OrderItemEvent("LAPTOP-001", 1, BigDecimal.ONE)), BigDecimal.ONE,
				java.time.LocalDateTime.now());

		processor.processOrderCreated(event);

		verify(inventoryRepository, never()).findAllForUpdate(anyList());

		verify(outboxRepository, never()).save(any());
	}

}
