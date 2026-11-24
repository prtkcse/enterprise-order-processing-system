package enterprise.sharma.prateek.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import enterprise.sharma.prateek.order.dto.CreateOrderRequest;
import enterprise.sharma.prateek.order.dto.OrderItemRequest;
import enterprise.sharma.prateek.order.dto.OrderResponse;
import enterprise.sharma.prateek.order.entity.Order;
import enterprise.sharma.prateek.order.entity.OutboxEvent;
import enterprise.sharma.prateek.order.mapper.OrderMapper;
import enterprise.sharma.prateek.order.repository.OrderRepository;
import enterprise.sharma.prateek.order.repository.OutboxEventRepository;

public class OrderServiceImplTest {

	private OrderRepository orderRepository;
	private OutboxEventRepository outboxRepository;
	private OrderMapper orderMapper;
	private OrderServiceImpl orderService;

	@BeforeEach
	void setUp() {

		orderRepository = mock(OrderRepository.class);
		outboxRepository = mock(OutboxEventRepository.class);
		orderMapper = new OrderMapper();

		orderService = new OrderServiceImpl(orderRepository, outboxRepository, orderMapper,
				new com.fasterxml.jackson.databind.ObjectMapper());

		when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

		when(outboxRepository.save(any(OutboxEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));
	}

	@Test
	void shouldCreateOrderAndOutboxEvent() {

		CreateOrderRequest request = new CreateOrderRequest("CUST-1001",
				List.of(new OrderItemRequest("LAPTOP-001", 2, new BigDecimal("75000.00")),
						new OrderItemRequest("MOUSE-001", 1, new BigDecimal("1500.00"))));

		OrderResponse response = orderService.createOrder(request);

		assertNotNull(response.orderId());

		assertEquals(new BigDecimal("151500.00"), response.totalAmount());

		assertEquals("CUST-1001", response.customerId());

		assertEquals("PENDING", response.status().name());

		assertEquals(2, response.items().size());

		verify(orderRepository, times(1)).save(any(Order.class));

		verify(outboxRepository, times(1)).save(any(OutboxEvent.class));
	}

}
